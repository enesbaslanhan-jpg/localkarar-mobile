package com.localkarar.app.network

import com.localkarar.app.auth.SecureStorage
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

import com.localkarar.app.auth.RefreshTokenRequest
import com.localkarar.app.auth.SessionDto
import com.localkarar.app.auth.UserDto
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*

private fun parseUserFacingErrorMessage(rawBody: String): String {
    if (rawBody.isBlank()) return "İşlem gerçekleştirilemedi. Lütfen tekrar deneyin."
    return try {
        val trimmed = rawBody.trim()
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            val json = Json { ignoreUnknownKeys = true }
            val element = json.parseToJsonElement(trimmed) as? JsonObject
            val errorVal = element?.get("error")?.let { (it as? JsonPrimitive)?.content }
            val msgVal = element?.get("message")?.let { (it as? JsonPrimitive)?.content }
            val resolved = errorVal ?: msgVal ?: ""
            when {
                resolved.contains("Email already in use", ignoreCase = true) -> "Bu e-posta adresi zaten kullanımda."
                resolved.contains("Invalid credentials", ignoreCase = true) -> "E-posta adresi veya şifre hatalı."
                resolved.contains("User not found", ignoreCase = true) -> "Kullanıcı bulunamadı."
                resolved.contains("Registration is closed", ignoreCase = true) -> "Kayıtlar şu an kapalıdır."
                resolved.contains("VALIDATION_ERROR", ignoreCase = true) -> "Girdiğiniz bilgileri kontrol edip tekrar deneyin."
                resolved.contains("INVALID_RESET_TOKEN", ignoreCase = true) -> "Sıfırlama kodu geçersiz ya da süresi dolmuş."
                resolved.contains("PASSWORD_UNCHANGED", ignoreCase = true) -> "Yeni şifre mevcut şifreyle aynı olamaz."
                resolved.isNotBlank() && !resolved.startsWith("{") -> resolved
                else -> "Girdiğiniz bilgileri kontrol edin."
            }
        } else {
            "İşlem gerçekleştirilemedi. Lütfen tekrar deneyin."
        }
    } catch (e: Exception) {
        "İşlem gerçekleştirilemedi. Lütfen tekrar deneyin."
    }
}

fun createHttpClient(
    secureStorage: SecureStorage,
    onUserUpdated: ((UserDto) -> Unit)? = null,
    onSessionExpired: (() -> Unit)? = null
): HttpClient {
    val jsonSerializer = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    return HttpClient {
        expectSuccess = true

        install(ContentNegotiation) {
            json(jsonSerializer)
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30000L
            connectTimeoutMillis = 15000L
            socketTimeoutMillis = 30000L
        }

        install(Auth) {
            bearer {
                loadTokens {
                    val token = secureStorage.readToken()
                    val refresh = secureStorage.readRefreshToken()
                    if (!token.isNullOrBlank()) {
                        BearerTokens(accessToken = token, refreshToken = refresh ?: "")
                    } else {
                        null
                    }
                }

                refreshTokens {
                    val currentRefresh = secureStorage.readRefreshToken()
                        ?: oldTokens?.refreshToken
                    if (currentRefresh.isNullOrBlank()) {
                        secureStorage.clearAll()
                        onSessionExpired?.invoke()
                        return@refreshTokens null
                    }

                    try {
                        val refreshResponse = client.post("${ApiConfig.baseUrl}/auth/refresh") {
                            contentType(ContentType.Application.Json)
                            setBody(RefreshTokenRequest(refreshToken = currentRefresh))
                            markAsRefreshTokenRequest()
                        }

                        if (refreshResponse.status.isSuccess()) {
                            val session = jsonSerializer.decodeFromString(
                                SessionDto.serializer(),
                                refreshResponse.bodyAsText()
                            )
                            secureStorage.saveToken(session.token)
                            if (!session.refreshToken.isNullOrBlank()) {
                                secureStorage.saveRefreshToken(session.refreshToken)
                            }
                            onUserUpdated?.invoke(session.user)
                            BearerTokens(
                                accessToken = session.token,
                                refreshToken = session.refreshToken ?: currentRefresh
                            )
                        } else {
                            secureStorage.clearAll()
                            onSessionExpired?.invoke()
                            null
                        }
                    } catch (e: Exception) {
                        secureStorage.clearAll()
                        onSessionExpired?.invoke()
                        null
                    }
                }

                sendWithoutRequest { request ->
                    val path = request.url.encodedPath
                    !path.contains("/auth/login") &&
                    !path.contains("/auth/register") &&
                    !path.contains("/auth/refresh") &&
                    !path.contains("/auth/password-reset")
                }
            }
        }

        defaultRequest {
            url(ApiConfig.baseUrl)
            contentType(ContentType.Application.Json)
        }

        HttpResponseValidator {
            validateResponse { response ->
                val contentType = response.contentType()
                val isSupportedResponse = contentType == null ||
                    contentType.match(ContentType.Application.Json) ||
                    contentType.match(ContentType.Text.EventStream)
                if (!isSupportedResponse) {
                    println("API_ERROR: Expected JSON but got ${contentType.contentType}/${contentType.contentSubtype} for ${response.request.url}")
                    throw ApiError.ServerError("Beklenmeyen yanıt formatı alındı. (Sunucu Hatası)")
                }
            }

            handleResponseExceptionWithRequest { exception, request ->
                val clientException = exception as? ClientRequestException ?: return@handleResponseExceptionWithRequest
                val exceptionResponse = clientException.response
                val exceptionResponseText = try {
                    exceptionResponse.bodyAsText()
                } catch (e: Exception) {
                    ""
                }
                
                when (exceptionResponse.status) {
                    HttpStatusCode.Unauthorized -> {
                        if (request.url.encodedPath.contains("/auth/login")) {
                            throw ApiError.Unauthorized(message = "E-posta veya şifre hatalı.")
                        } else {
                            throw ApiError.Unauthorized(message = "Oturumunuzun süresi doldu. Lütfen tekrar giriş yapın.")
                        }
                    }
                    HttpStatusCode.Forbidden -> throw ApiError.Forbidden("Bu işlem için yetkiniz bulunmuyor.")
                    HttpStatusCode.NotFound -> throw ApiError.NotFound("Aranan kaynak bulunamadı.")
                    HttpStatusCode.Conflict -> throw ApiError.UnknownError(message = "Veri çakışması oluştu.")
                    HttpStatusCode.UnprocessableEntity, HttpStatusCode.BadRequest -> {
                        val friendlyMessage = parseUserFacingErrorMessage(exceptionResponseText)
                        throw ApiError.ValidationError(message = friendlyMessage)
                    }
                    else -> {
                        println("API_ERROR: HTTP ${exceptionResponse.status.value} - $exceptionResponseText")
                        throw ApiError.ServerError("Sunucuyla iletişim kurulurken bir sorun oluştu.")
                    }
                }
            }

            handleResponseExceptionWithRequest { exception, _ ->
                when (exception) {
                    is kotlinx.io.IOException -> throw ApiError.NetworkUnavailable()
                    is HttpRequestTimeoutException -> throw ApiError.Timeout()
                }
            }
        }
    }
}
