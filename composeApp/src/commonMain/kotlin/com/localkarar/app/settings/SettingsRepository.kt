package com.localkarar.app.settings

import com.localkarar.app.auth.ConsentsResponseDto
import com.localkarar.app.auth.LegalDocumentDto
import com.localkarar.app.auth.LegalDocumentsResponseDto
import com.localkarar.app.auth.ProfileUpdateDto
import com.localkarar.app.auth.SessionDto
import com.localkarar.app.auth.UserDto
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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
data class ProfileUpdateRequest(
    val name: String? = null,
    val bio: String? = null,
    val location: String? = null,
    val websiteUrl: String? = null
)

class SettingsRepository(
    private val client: HttpClient
) {
    private val json = Json { ignoreUnknownKeys = true }

    private suspend fun errorMessage(response: HttpResponse): String {
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
            val response = client.get("/auth/me")
            if (response.status.isSuccess()) {
                Result.success(json.decodeFromString(UserDto.serializer(), response.bodyAsText()))
            } else {
                Result.failure(Exception(errorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(name: String): Result<ProfileUpdateDto> {
        return try {
            val response = client.patch("/auth/profile") {
                contentType(ContentType.Application.Json)
                setBody(ProfileUpdateRequest(name = name.trim()))
            }
            if (response.status.isSuccess()) {
                Result.success(json.decodeFromString(ProfileUpdateDto.serializer(), response.bodyAsText()))
            } else {
                Result.failure(Exception(errorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<SessionDto> {
        return try {
            val response = client.put("/auth/password") {
                contentType(ContentType.Application.Json)
                setBody(ChangePasswordRequest(currentPassword, newPassword))
            }
            if (response.status.isSuccess()) {
                Result.success(json.decodeFromString(SessionDto.serializer(), response.bodyAsText()))
            } else {
                Result.failure(Exception(errorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changeEmail(newEmail: String, currentPassword: String): Result<SessionDto> {
        return try {
            val response = client.put("/auth/email") {
                contentType(ContentType.Application.Json)
                setBody(ChangeEmailRequest(newEmail.trim().lowercase(), currentPassword))
            }
            if (response.status.isSuccess()) {
                Result.success(json.decodeFromString(SessionDto.serializer(), response.bodyAsText()))
            } else {
                Result.failure(Exception(errorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logoutAll(): Result<SessionDto> {
        return try {
            val response = client.post("/auth/logout-all") {
                contentType(ContentType.Application.Json)
                setBody("{}")
            }
            if (response.status.isSuccess()) {
                Result.success(json.decodeFromString(SessionDto.serializer(), response.bodyAsText()))
            } else {
                Result.failure(Exception(errorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(currentPassword: String): Result<Unit> {
        return try {
            val response = client.delete("/auth/account") {
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
            val extension = if (name.endsWith(".png", ignoreCase = true)) "png" else "jpg"
            val mimeType = if (extension == "png") "image/png" else "image/jpeg"
            val response = client.post("/auth/avatar") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append(
                                "avatar",
                                bytes,
                                Headers.build {
                                    append(HttpHeaders.ContentType, mimeType)
                                    append(HttpHeaders.ContentDisposition, "filename=\"avatar.$extension\"")
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
            val response = client.delete("/auth/avatar")
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception(errorMessage(response)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getConsents(): Result<ConsentsResponseDto> {
        return try {
            val response = client.get("/auth/consents")
            if (response.status.isSuccess()) {
                Result.success(json.decodeFromString(ConsentsResponseDto.serializer(), response.bodyAsText()))
            } else {
                Result.failure(Exception(errorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptConsents(): Result<Unit> {
        return try {
            val response = client.post("/auth/consents") {
                contentType(ContentType.Application.Json)
                setBody("{}")
            }
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(errorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLegalDocuments(): Result<List<LegalDocumentDto>> {
        return try {
            val response = client.get("/auth/legal-documents")
            if (response.status.isSuccess()) {
                val dto = json.decodeFromString(LegalDocumentsResponseDto.serializer(), response.bodyAsText())
                Result.success(dto.documents)
            } else {
                Result.failure(Exception(errorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}