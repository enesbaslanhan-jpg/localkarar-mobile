package com.localkarar.app.decision

import com.localkarar.app.network.ApiConfig
import com.localkarar.app.network.ApiError
import com.localkarar.app.network.dto.DecisionCheckListDto
import com.localkarar.app.network.dto.DecisionCheckStartResponseDto
import com.localkarar.app.network.dto.DecisionCheckSessionDto
import com.localkarar.app.network.dto.DecisionUpdateAnswerRequestDto
import com.localkarar.app.network.dto.DecisionUpdateAnswerResponseDto
import com.localkarar.app.network.dto.DecisionCompleteResponseDto
import com.localkarar.app.network.dto.DecisionResultDto
import com.localkarar.app.network.dto.DecisionHistorySessionDto
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.JsonElement

import io.ktor.client.HttpClient

class DecisionRepository(private val httpClient: HttpClient) {

    suspend fun getDecisionChecks(): Result<List<DecisionCheckListDto>> = try {
        val response = httpClient.get("${ApiConfig.baseUrl}/api/v1/decision-checks")
        if (response.status.isSuccess()) {
            Result.success(response.body())
        } else {
            Result.failure(Exception("API error: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun startSession(code: String): Result<DecisionCheckStartResponseDto> = try {
        val response = httpClient.post("${ApiConfig.baseUrl}/api/v1/decision-checks/$code/start")
        if (response.status.isSuccess()) {
            Result.success(response.body())
        } else {
            Result.failure(Exception("API error: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getSession(id: String): Result<DecisionCheckSessionDto> = try {
        val response = httpClient.get("${ApiConfig.baseUrl}/api/v1/decision-check-sessions/$id")
        if (response.status.isSuccess()) {
            Result.success(response.body())
        } else {
            Result.failure(Exception("API error: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateAnswer(sessionId: String, questionCode: String, value: JsonElement?, isUnknown: Boolean = false): Result<DecisionUpdateAnswerResponseDto> = try {
        val requestBody = DecisionUpdateAnswerRequestDto(
            questionCode = questionCode,
            value = value,
            isUnknown = isUnknown
        )
        val response = httpClient.patch("${ApiConfig.baseUrl}/api/v1/decision-check-sessions/$sessionId/answers") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
        if (response.status.isSuccess()) {
            Result.success(response.body())
        } else {
            Result.failure(Exception("API error: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun completeSession(id: String): Result<DecisionCompleteResponseDto> = try {
        val response = httpClient.post("${ApiConfig.baseUrl}/api/v1/decision-check-sessions/$id/complete")
        if (response.status.isSuccess()) {
            Result.success(response.body())
        } else {
            Result.failure(Exception("API error: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getResult(id: String): Result<DecisionResultDto> = try {
        val response = httpClient.get("${ApiConfig.baseUrl}/api/v1/decision-check-sessions/$id/result")
        if (response.status.isSuccess()) {
            Result.success(response.body())
        } else {
            Result.failure(Exception("API error: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getSessionHistory(): Result<List<DecisionHistorySessionDto>> = try {
        val response = httpClient.get("${ApiConfig.baseUrl}/api/v1/decision-checks/sessions/me")
        if (response.status.isSuccess()) {
            Result.success(response.body())
        } else {
            Result.failure(Exception("API error: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}



