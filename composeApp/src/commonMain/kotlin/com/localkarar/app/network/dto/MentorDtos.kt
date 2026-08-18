package com.localkarar.app.network.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ConversationListItemDto(
    val id: Int,
    val title: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val lastMessageAt: String? = null,
    val model: String? = null,
    val provider: String? = null,
    val lastMessage: ConversationLastMessageDto? = null,
    val messageCount: Int = 0
)

@Serializable
data class ConversationLastMessageDto(
    val content: String? = null,
    val role: String? = null,
    val createdAt: String? = null
)

@Serializable
data class ConversationListResponseDto(
    val conversations: List<ConversationListItemDto> = emptyList()
)

@Serializable
data class ConversationDetailDto(
    val id: Int,
    val title: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val lastMessageAt: String? = null,
    val model: String? = null,
    val provider: String? = null
)

@Serializable
data class MessageDto(
    val id: Int,
    val role: String = "assistant",
    val content: String = "",
    val citations: List<CitationDto>? = null,
    val knowledgeObjects: List<CitationDto>? = null,
    val tokenUsage: JsonElement? = null,
    val error: String? = null,
    val generationStatus: String? = null,
    val regeneratedFromMessageId: Int? = null,
    val editedFromMessageId: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class CitationDto(
    val id: String? = null,
    val title: String? = null,
    val url: String? = null,
    val snippet: String? = null,
    val page: Int? = null,
    val sourceType: String? = null
)

@Serializable
data class ConversationDetailResponseDto(
    val conversation: ConversationDetailDto,
    val messages: List<MessageDto> = emptyList()
)

@Serializable
data class CreateConversationRequestDto(
    val title: String,
    val contextSnapshot: String? = null
)

@Serializable
data class RenameConversationRequestDto(
    val title: String
)

@Serializable
data class SendMessageRequestDto(
    val message: String,
    val knowledgeObjectCode: String? = null
)

@Serializable
data class TokenUsageDto(
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalTokens: Int = 0
)

@Serializable
data class MemoryDto(
    val id: Int,
    val type: String = "fact",
    val key: String? = null,
    val value: String = "",
    val summary: String? = null,
    val status: String = "active",
    val validationStatus: String? = null,
    val importance: Double? = null,
    val confidence: Double? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class MemoryListResponseDto(
    val memories: List<MemoryDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val pageSize: Int = 50
)

@Serializable
data class CreateMemoryRequestDto(
    val type: String,
    val value: String,
    val key: String? = null,
    val summary: String? = null,
    val importance: Double? = null,
    val confidence: Double? = null
)

@Serializable
data class MemoryResponseDto(
    val memory: MemoryDto
)

sealed interface MentorStreamEvent {
    data class Start(val conversationId: Int?, val userMessageId: Int?) : MentorStreamEvent
    data class Provider(val provider: String?, val model: String?) : MentorStreamEvent
    data class Delta(val delta: String) : MentorStreamEvent
    data class Done(
        val assistantMessage: MessageDto?,
        val tokenUsage: TokenUsageDto?,
        val sources: List<CitationDto>
    ) : MentorStreamEvent
    data class Cancelled(val assistantMessage: MessageDto?) : MentorStreamEvent
    data class StreamError(val code: String?, val message: String?) : MentorStreamEvent
}