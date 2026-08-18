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
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.JsonElement

class DecisionRepository(private val httpClient: HttpClient) {

    private val userMessage = "Karar aracı yüklenemedi. Lütfen tekrar deneyin."

    private class ApiContractViolation(
        userMessage: String,
        technical: String
    ) : Exception(userMessage) {
        init {
            println("DecisionRepository contract violation: $technical")
        }
    }

    private fun HttpResponse.requireJson() {
        val contentType = contentType()
        if (contentType != null && !contentType.match(ContentType.Application.Json)) {
            throw ApiContractViolation(
                userMessage = userMessage,
                technical = "Expected application/json but received '$contentType'"
            )
        }
    }

    private fun HttpResponse.failForStatus(): Result<Nothing> {
        println("DecisionRepository request failed: HTTP ${status.value}")
        return Result.failure(Exception(userMessage))
    }

    suspend fun getDecisionChecks(): Result<List<DecisionCheckListDto>> = try {
        val response = httpClient.get("${ApiConfig.baseUrl}/api/v1/decision-checks")
        if (response.status.isSuccess()) {
            response.requireJson()
            Result.success(response.body())
        } else {
            response.failForStatus()
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun startSession(code: String): Result<DecisionCheckStartResponseDto> = try {
        val response = httpClient.post("${ApiConfig.baseUrl}/api/v1/decision-checks/$code/start") {
            contentType(ContentType.Application.Json)
            setBody("{}")
        }
        if (response.status.isSuccess()) {
            response.requireJson()
            Result.success(response.body())
        } else {
            response.failForStatus()
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getSession(id: String): Result<DecisionCheckSessionDto> = try {
        val response = httpClient.get("${ApiConfig.baseUrl}/api/v1/decision-checks/sessions/$id")
        if (response.status.isSuccess()) {
            response.requireJson()
            Result.success(response.body())
        } else {
            response.failForStatus()
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
        val response = httpClient.patch("${ApiConfig.baseUrl}/api/v1/decision-checks/sessions/$sessionId/answers") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
        if (response.status.isSuccess()) {
            response.requireJson()
            Result.success(response.body())
        } else {
            response.failForStatus()
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun completeSession(id: String): Result<DecisionCompleteResponseDto> = try {
        val response = httpClient.post("${ApiConfig.baseUrl}/api/v1/decision-checks/sessions/$id/complete") {
            contentType(ContentType.Application.Json)
            setBody("{}")
        }
        if (response.status.isSuccess()) {
            response.requireJson()
            Result.success(response.body())
        } else {
            response.failForStatus()
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getResult(id: String): Result<DecisionResultDto> = try {
        val response = httpClient.get("${ApiConfig.baseUrl}/api/v1/decision-checks/sessions/$id/result")
        if (response.status.isSuccess()) {
            response.requireJson()
            Result.success(response.body())
        } else {
            response.failForStatus()
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getSessionHistory(): Result<List<DecisionHistorySessionDto>> = try {
        val response = httpClient.get("${ApiConfig.baseUrl}/api/v1/decision-checks/sessions/me")
        if (response.status.isSuccess()) {
            response.requireJson()
            Result.success(response.body())
        } else {
            response.failForStatus()
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
