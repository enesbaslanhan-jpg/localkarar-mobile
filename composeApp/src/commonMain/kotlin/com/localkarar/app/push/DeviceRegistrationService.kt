package com.localkarar.app.push

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
data class DeviceRegistrationRequest(
    val pushToken: String,
    val platform: String,
    val appVersion: String,
    val locale: String
)

class DeviceRegistrationService(
    private val httpClient: HttpClient
) {
    suspend fun registerDevice(
        installationId: String,
        pushToken: String,
        appVersion: String,
        locale: String
    ): Result<Unit> {
        return try {
            httpClient.put("/devices/$installationId") {
                contentType(ContentType.Application.Json)
                setBody(
                    DeviceRegistrationRequest(
                        pushToken = pushToken,
                        platform = "android",
                        appVersion = appVersion,
                        locale = locale
                    )
                )
            }
            Result.success(Unit)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun unregisterDevice(installationId: String): Result<Unit> {
        return try {
            httpClient.delete("/devices/$installationId")
            Result.success(Unit)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }
}
