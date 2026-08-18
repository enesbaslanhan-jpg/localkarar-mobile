package com.localkarar.app.news

import com.localkarar.app.network.ApiConfig
import com.localkarar.app.network.dto.NewsFeedResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess

class NewsRepository(
    private val client: HttpClient,
    private val baseUrl: String = ApiConfig.PRODUCTION_API_URL
) {
    suspend fun getFeed(category: String?, cursor: String?, limit: Int = 20): Result<NewsFeedResponseDto> {
        return try {
            val response = client.get("$baseUrl/api/news") {
                parameter("limit", limit)
                if (!category.isNullOrBlank()) parameter("category", category)
                if (!cursor.isNullOrBlank()) parameter("cursor", cursor)
            }
            if (response.status.isSuccess()) {
                Result.success(response.body<NewsFeedResponseDto>())
            } else {
                Result.failure(Exception("Haberler yüklenemedi (${response.status.value})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}