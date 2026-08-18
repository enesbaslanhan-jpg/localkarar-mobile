package com.localkarar.app.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class SafeApiClient(
    @PublishedApi internal val httpClient: HttpClient,
    private val moduleName: String
) {

    private inner class ApiContractViolation(
        userMessage: String,
        technical: String
    ) : Exception(userMessage) {
        init {
            println("$moduleName contract violation: $technical")
        }
    }

    @PublishedApi
    internal fun genericMessage(): String = "$moduleName yüklenemedi. Lütfen tekrar deneyin."

    @PublishedApi
    internal fun HttpResponse.requireJson() {
        val contentType = contentType()
        if (contentType != null && !contentType.match(ContentType.Application.Json)) {
            throw ApiContractViolation(
                userMessage = genericMessage(),
                technical = "Expected application/json but received '$contentType'"
            )
        }
    }

    @PublishedApi
    internal suspend fun HttpResponse.serverMessage(): String? {
        return try {
            val text = bodyAsText()
            val obj = Json.parseToJsonElement(text).jsonObject
            val message = obj["message"]?.jsonPrimitive?.contentOrNull
                ?: obj["error"]?.jsonPrimitive?.contentOrNull
            if (message != null && message.length < 300 && !message.contains('\n')) message else null
        } catch (e: Exception) {
            null
        }
    }

    @PublishedApi
    internal suspend fun HttpResponse.toApiError(): ApiError {
        val serverMessage = serverMessage()
        return when (status) {
            HttpStatusCode.Unauthorized -> ApiError.Unauthorized()
            HttpStatusCode.Forbidden -> ApiError.Forbidden(serverMessage ?: "Bu işlem için yetkiniz bulunmuyor.")
            HttpStatusCode.NotFound -> ApiError.NotFound(serverMessage ?: "İstenen kaynak bulunamadı.")
            HttpStatusCode.UnprocessableEntity -> ApiError.ValidationError(serverMessage ?: "Girdiğiniz bilgiler geçersiz.")
            HttpStatusCode.Conflict -> ApiError.ValidationError(serverMessage ?: "Bu işlem şu anda yapılamıyor.")
            else -> {
                if (status.value >= 500) ApiError.ServerError()
                else ApiError.UnknownError(serverMessage ?: "Beklenmeyen bir hata oluştu.")
            }
        }
    }

    private fun mapException(e: Exception): ApiError {
        return when (e) {
            is ApiError -> e
            else -> {
                println("$moduleName request failed: ${e.message}")
                ApiError.UnknownError(genericMessage())
            }
        }
    }

    suspend inline fun <reified T> get(path: String): Result<T> = try {
        val response = httpClient.get(path)
        if (response.status.isSuccess()) {
            response.requireJson()
            Result.success(response.body())
        } else {
            Result.failure(response.toApiError())
        }
    } catch (e: Exception) {
        Result.failure(mapException(e))
    }

    suspend inline fun <reified T> post(path: String, body: Any? = null): Result<T> = try {
        val response = httpClient.post(path) {
            if (body != null) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }
        if (response.status.isSuccess()) {
            response.requireJson()
            Result.success(response.body())
        } else {
            Result.failure(response.toApiError())
        }
    } catch (e: Exception) {
        Result.failure(mapException(e))
    }

    suspend inline fun <reified T> patch(path: String, body: Any? = null): Result<T> = try {
        val response = httpClient.patch(path) {
            if (body != null) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }
        if (response.status.isSuccess()) {
            response.requireJson()
            Result.success(response.body())
        } else {
            Result.failure(response.toApiError())
        }
    } catch (e: Exception) {
        Result.failure(mapException(e))
    }

    suspend inline fun <reified T> put(path: String, body: Any? = null): Result<T> = try {
        val response = httpClient.put(path) {
            if (body != null) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }
        if (response.status.isSuccess()) {
            response.requireJson()
            Result.success(response.body())
        } else {
            Result.failure(response.toApiError())
        }
    } catch (e: Exception) {
        Result.failure(mapException(e))
    }

    suspend fun delete(path: String): Result<Unit> = try {
        val response = httpClient.delete(path)
        if (response.status.isSuccess()) {
            Result.success(Unit)
        } else {
            Result.failure(response.toApiError())
        }
    } catch (e: Exception) {
        Result.failure(mapException(e))
    }
}