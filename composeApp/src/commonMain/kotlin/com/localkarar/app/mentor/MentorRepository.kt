package com.localkarar.app.mentor

import kotlinx.coroutines.launch
import com.localkarar.app.network.ApiConfig
import com.localkarar.app.network.dto.ConversationListItemDto
import com.localkarar.app.network.dto.ConversationDetailDto
import com.localkarar.app.network.dto.ConversationDetailResponseDto
import com.localkarar.app.network.dto.ConversationListResponseDto
import com.localkarar.app.network.dto.CreateConversationRequestDto
import com.localkarar.app.network.dto.MemoryDto
import com.localkarar.app.network.dto.MemoryListResponseDto
import com.localkarar.app.network.dto.MemoryResponseDto
import com.localkarar.app.network.dto.MentorStreamEvent
import com.localkarar.app.network.dto.MessageDto
import com.localkarar.app.network.dto.RenameConversationRequestDto
import com.localkarar.app.network.dto.SendMessageRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class MentorRepository(
    private val client: HttpClient,
    private val baseUrl: String = ApiConfig.baseUrl
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val base = "$baseUrl/api/mentor"

    private suspend fun errorMessage(response: io.ktor.client.statement.HttpResponse): String {
        return try {
            val text = response.bodyAsText()
            if (text.isBlank()) return "Sunucu hatası (${response.status.value})"
            val element = json.parseToJsonElement(text).jsonObject
            (element["message"]?.jsonPrimitive?.content)
                ?: (element["error"]?.jsonObject?.get("message")?.jsonPrimitive?.content)
                ?: "Sunucu hatası (${response.status.value})"
        } catch (_: Exception) {
            "Sunucu hatası (${response.status.value})"
        }
    }

    suspend fun listConversations(): Result<List<ConversationListItemDto>> {
        return try {
            val response = client.get("$base/conversations")
            if (response.status.isSuccess()) {
                Result.success(response.body<ConversationListResponseDto>().conversations)
            } else {
                Result.failure(Exception(errorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createConversation(title: String): Result<ConversationDetailDto> {
        return try {
            val response = client.post("$base/conversations") {
                contentType(ContentType.Application.Json)
                setBody(CreateConversationRequestDto(title = title))
            }
            if (response.status.isSuccess()) {
                Result.success(response.body<ConversationDetailDto>())
            } else {
                Result.failure(Exception(errorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getConversation(id: Int): Result<ConversationDetailResponseDto> {
        return try {
            val response = client.get("$base/conversations/$id")
            if (response.status.isSuccess()) {
                Result.success(response.body<ConversationDetailResponseDto>())
            } else {
                Result.failure(Exception(errorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun renameConversation(id: Int, title: String): Result<ConversationDetailDto> {
        return try {
            val response = client.patch("$base/conversations/$id") {
                contentType(ContentType.Application.Json)
                setBody(RenameConversationRequestDto(title))
            }
            if (response.status.isSuccess()) {
                Result.success(response.body<ConversationDetailDto>())
            } else {
                Result.failure(Exception(errorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun archiveConversation(id: Int): Result<Unit> {
        return try {
            val response = client.patch("$base/conversations/$id/archive")
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception(errorMessage(response)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unarchiveConversation(id: Int): Result<Unit> {
        return try {
            val response = client.patch("$base/conversations/$id/unarchive")
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception(errorMessage(response)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteConversation(id: Int): Result<Unit> {
        return try {
            val response = client.delete("$base/conversations/$id")
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception(errorMessage(response)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun streamMessage(conversationId: Int, message: String): Flow<MentorStreamEvent> = callbackFlow {
        val job = launch {
            try {
                val response = client.post("$base/conversations/$conversationId/messages/stream") {
                    contentType(ContentType.Application.Json)
                    setBody(SendMessageRequestDto(message = message))
                }
                if (!response.status.isSuccess()) {
                    trySend(MentorStreamEvent.StreamError(null, errorMessage(response)))
                    close()
                    return@launch
                }
                val channel = response.bodyAsChannel()
                var eventName = ""
                val dataBuilder = StringBuilder()
                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: break
                    when {
                        line.startsWith("event:") -> {
                            eventName = line.removePrefix("event:").trim()
                        }
                        line.startsWith("data:") -> {
                            if (dataBuilder.isNotEmpty()) dataBuilder.append('\n')
                            dataBuilder.append(line.removePrefix("data:").trim())
                        }
                        line.isEmpty() -> {
                            if (eventName.isNotEmpty()) {
                                parseEvent(eventName, dataBuilder.toString())?.let { trySend(it) }
                            }
                            eventName = ""
                            dataBuilder.clear()
                        }
                    }
                }
                if (eventName.isNotEmpty()) {
                    parseEvent(eventName, dataBuilder.toString())?.let { trySend(it) }
                }
                close()
            } catch (e: Exception) {
                trySend(MentorStreamEvent.StreamError(null, e.message ?: "Bağlantı hatası"))
                close()
            }
        }
        awaitClose { job.cancel() }
    }

    private fun parseEvent(eventName: String, data: String): MentorStreamEvent? {
        if (data.isBlank()) return null
        return try {
            val element = json.parseToJsonElement(data).jsonObject
            when (eventName) {
                "start" -> MentorStreamEvent.Start(
                    conversationId = element["conversationId"]?.jsonPrimitive?.content?.toIntOrNull(),
                    userMessageId = element["userMessageId"]?.jsonPrimitive?.content?.toIntOrNull()
                )
                "provider" -> MentorStreamEvent.Provider(
                    provider = element["provider"]?.jsonPrimitive?.content,
                    model = element["model"]?.jsonPrimitive?.content
                )
                "delta" -> MentorStreamEvent.Delta(element["delta"]?.jsonPrimitive?.content ?: "")
                "done" -> {
                    val assistant = element["assistantMessage"]?.let {
                        json.decodeFromString(MessageDto.serializer(), it.toString())
                    }
                    val tokenUsage = element["tokenUsage"]?.let {
                        if (it.toString() == "null") null
                        else try {
                            json.decodeFromString(
                                com.localkarar.app.network.dto.TokenUsageDto.serializer(),
                                it.toString()
                            )
                        } catch (_: Exception) {
                            null
                        }
                    }
                    val sources = element["sources"]?.let {
                        try {
                            json.decodeFromString(
                                kotlinx.serialization.builtins.ListSerializer(
                                    com.localkarar.app.network.dto.CitationDto.serializer()
                                ),
                                it.toString()
                            )
                        } catch (_: Exception) {
                            emptyList()
                        }
                    } ?: emptyList()
                    MentorStreamEvent.Done(assistant, tokenUsage, sources)
                }
                "cancelled" -> {
                    val assistant = element["assistantMessage"]?.let {
                        json.decodeFromString(MessageDto.serializer(), it.toString())
                    }
                    MentorStreamEvent.Cancelled(assistant)
                }
                "error" -> MentorStreamEvent.StreamError(
                    code = element["error"]?.jsonObject?.get("code")?.jsonPrimitive?.content,
                    message = element["error"]?.jsonObject?.get("message")?.jsonPrimitive?.content
                )
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun listMemories(): Result<List<MemoryDto>> {
        return try {
            val response = client.get("$baseUrl/api/memory?pageSize=100")
            if (response.status.isSuccess()) {
                Result.success(response.body<MemoryListResponseDto>().memories)
            } else {
                Result.failure(Exception(errorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createMemory(type: String, value: String): Result<MemoryDto> {
        return try {
            val response = client.post("$baseUrl/api/memory") {
                contentType(ContentType.Application.Json)
                setBody(
                    com.localkarar.app.network.dto.CreateMemoryRequestDto(
                        type = type,
                        value = value,
                        confidence = 0.95,
                        importance = 0.8
                    )
                )
            }
            if (response.status.isSuccess()) {
                Result.success(response.body<MemoryResponseDto>().memory)
            } else {
                Result.failure(Exception(errorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMemory(id: Int): Result<Unit> {
        return try {
            val response = client.delete("$baseUrl/api/memory/$id")
            if (response.status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception(errorMessage(response)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAllMemories(): Result<Int> {
        return try {
            val response = client.delete("$baseUrl/api/memory") {
                contentType(ContentType.Application.Json)
                setBody("""{"confirmation":"DELETE_ALL_MEMORIES"}""")
            }
            if (response.status.isSuccess()) {
                val text = response.bodyAsText()
                val count = try {
                    json.parseToJsonElement(text).jsonObject["deletedCount"]?.jsonPrimitive?.content?.toIntOrNull()
                } catch (_: Exception) {
                    null
                }
                Result.success(count ?: 0)
            } else {
                Result.failure(Exception(errorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
