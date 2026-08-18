package com.localkarar.app.home

import com.localkarar.app.auth.SecureStorage
import com.localkarar.app.network.dto.DashboardResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class DashboardRepository(
    private val httpClient: HttpClient,
    private val secureStorage: SecureStorage
) {
    suspend fun getDashboard(): Result<DashboardResponse> {
        return try {
            val token = secureStorage.readToken()
            if (token == null) {
                return Result.failure(Exception("Not authenticated"))
            }

            val response = httpClient.get("/dashboard")

            if (response.status.isSuccess()) {
                val data = response.body<DashboardResponse>()
                Result.success(data)
            } else if (response.status == HttpStatusCode.Unauthorized) {
                Result.failure(Exception("UNAUTHORIZED"))
            } else {
                Result.failure(Exception("Dashboard fetch failed: ${response.status}"))
            }
        } catch (e: Exception) {
            println("Dashboard fetch exception: $e"); Result.failure(e)
        }
    }
}
