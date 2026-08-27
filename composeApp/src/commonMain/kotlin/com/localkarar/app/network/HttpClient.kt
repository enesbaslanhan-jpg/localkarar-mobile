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
import io.ktor.utils.io.errors.IOException

fun createHttpClient(secureStorage: SecureStorage): HttpClient {
    return HttpClient {
        expectSuccess = true

        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
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

        defaultRequest {
            url(ApiConfig.baseUrl)
            contentType(ContentType.Application.Json)
            
            val token = secureStorage.readToken()
            if (!token.isNullOrBlank()) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        HttpResponseValidator {
            validateResponse { response ->
                val contentType = response.contentType()
                if (contentType != null && !contentType.match(ContentType.Application.Json)) {
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
                    HttpStatusCode.Forbidden -> throw ApiError.Forbidden()
                    HttpStatusCode.NotFound -> throw ApiError.NotFound()
                    HttpStatusCode.Conflict -> throw ApiError.UnknownError(message = "Veri çakışması oluştu.")
                    HttpStatusCode.UnprocessableEntity, HttpStatusCode.BadRequest -> {
                        throw ApiError.ValidationError(message = "Geçersiz istek: $exceptionResponseText")
                    }
                    else -> {
                        println("API_ERROR: HTTP ${exceptionResponse.status.value} - $exceptionResponseText")
                        throw ApiError.ServerError("Sunucu hatası: ${exceptionResponse.status.value}")
                    }
                }
            }

            handleResponseExceptionWithRequest { exception, _ ->
                when (exception) {
                    is IOException -> throw ApiError.NetworkUnavailable()
                    is HttpRequestTimeoutException -> throw ApiError.Timeout()
                }
            }
        }
    }
}
