package com.localkarar.app.settings

import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonObject
import com.localkarar.app.auth.UserDto
import com.localkarar.app.network.ApiConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

@Serializable
data class ChangeEmailRequest(
    val newEmail: String,
    val currentPassword: String
)

@Serializable
data class DeleteAccountRequest(
    val currentPassword: String,
    val confirmation: String
)

@Serializable
data class ChangeEmailResponse(
    val token: String,
    val user: UserDto
)

class SettingsRepository(
    private val client: HttpClient,
    private val baseUrl: String = ApiConfig.baseUrl
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val base = "$baseUrl/api/auth"

    private suspend fun errorMessage(response: io.ktor.client.statement.HttpResponse): String {
        return try {
            val text = response.bodyAsText()
            if (text.isBlank()) return "İşlem başarısız (${response.status.value})"
            val element = json.parseToJsonElement(text).jsonObject
            (element["message"]?.jsonPrimitive?.content)
                ?: (element["error"]?.jsonObject?.get("message")?.jsonPrimitive?.content)
                ?: (element["error"]?.jsonPrimitive?.content)
                ?: "İşlem başarısız (${response.status.value})"
        } catch (_: Exception) {
            "İşlem başarısız (${response.status.value})"
        }
    }

    suspend fun getMe(): Result<UserDto> {
        return try {
            val response = client.get("$base/me")
            if (response.status.isSuccess()) {
                Result.success(json.decodeFromString(UserDto.serializer(), response.bodyAsText()))
            } else {
                Result.failure(Exception(errorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val response = client.put("$base/password") {
                contentType(ContentType.Application.Json)
                setBody(ChangePasswordRequest(currentPassword, newPassword))
            }
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception(errorMessage(response)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changeEmail(newEmail: String, currentPassword: String): Result<ChangeEmailResponse> {
        return try {
            val response = client.put("$base/email") {
                contentType(ContentType.Application.Json)
                setBody(ChangeEmailRequest(newEmail, currentPassword))
            }
            if (response.status.isSuccess()) {
                Result.success(json.decodeFromString(ChangeEmailResponse.serializer(), response.bodyAsText()))
            } else {
                Result.failure(Exception(errorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(currentPassword: String): Result<Unit> {
        return try {
            val response = client.delete("$base/account") {
                contentType(ContentType.Application.Json)
                setBody(DeleteAccountRequest(currentPassword, "HESABIMI SİL"))
            }
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception(errorMessage(response)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadAvatar(name: String, bytes: ByteArray): Result<String> {
        return try {
            val response = client.post("$base/avatar") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append(
                                "avatar",
                                bytes,
                                Headers.build {
                                    append(HttpHeaders.ContentType, "image/jpeg")
                                    append(HttpHeaders.ContentDisposition, "filename=\"$name\"")
                                }
                            )
                        }
                    )
                )
            }
            if (response.status.isSuccess()) {
                val text = response.bodyAsText()
                val avatarUrl = try {
                    json.parseToJsonElement(text).jsonObject["avatarUrl"]?.jsonPrimitive?.content
                } catch (_: Exception) {
                    null
                }
                if (avatarUrl != null) Result.success(avatarUrl)
                else Result.failure(Exception("avatarUrl alınamadı"))
            } else {
                Result.failure(Exception(errorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAvatar(): Result<Unit> {
        return try {
            val response = client.delete("$base/avatar")
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception(errorMessage(response)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}