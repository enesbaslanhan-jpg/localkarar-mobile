package com.localkarar.app.community

import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonObject
import com.localkarar.app.network.ApiConfig
import com.localkarar.app.network.dto.CommunityFeedResponseDto
import com.localkarar.app.network.dto.CommunityPostDto
import com.localkarar.app.network.dto.CreateCommunityPostRequestDto
import com.localkarar.app.network.dto.ReportPostRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

class CommunityRepository(
    private val client: HttpClient,
    private val baseUrl: String = ApiConfig.baseUrl
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val base = "$baseUrl/api/community"

    suspend fun getFeed(type: String?, cursor: String?): Result<CommunityFeedResponseDto> {
        return try {
            val response = client.get(base) {
                if (!type.isNullOrBlank()) parameter("type", type)
                if (!cursor.isNullOrBlank()) parameter("cursor", cursor)
            }
            if (response.status.isSuccess()) {
                Result.success(response.body<CommunityFeedResponseDto>())
            } else {
                Result.failure(Exception("Gönderiler yüklenemedi (${response.status.value})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPost(title: String, summary: String): Result<CommunityPostDto> {
        return try {
            val response = client.post("$base/posts") {
                contentType(ContentType.Application.Json)
                setBody(CreateCommunityPostRequestDto(title = title, summary = summary))
            }
            if (response.status.isSuccess()) {
                Result.success(response.body<CommunityPostDto>())
            } else {
                Result.failure(Exception(errorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reportPost(postId: String, reason: String, details: String?): Result<Unit> {
        return try {
            val response = client.post("$base/$postId/reports") {
                contentType(ContentType.Application.Json)
                setBody(ReportPostRequestDto(reason = reason, details = details))
            }
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception(errorMessage(response)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun errorMessage(response: io.ktor.client.statement.HttpResponse): String {
        return try {
            val text = response.bodyAsText()
            if (text.isBlank()) return "İşlem başarısız (${response.status.value})"
            val element = json.parseToJsonElement(text).jsonObject
            (element["message"]?.jsonPrimitive?.content)
                ?: (element["error"]?.jsonPrimitive?.content)
                ?: "İşlem başarısız (${response.status.value})"
        } catch (_: Exception) {
            "İşlem başarısız (${response.status.value})"
        }
    }
}