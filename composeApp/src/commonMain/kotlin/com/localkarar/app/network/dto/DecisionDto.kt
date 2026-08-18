package com.localkarar.app.network.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class DecisionCheckListDto(
    val code: String,
    val title: String,
    val description: String,
    val category: String,
    @Serializable(with = StringListSerializer::class) val targetRoles: List<String>? = null,
    val currentVersion: String? = null,
    // Mobile specific appended field
    val status: String? = null,
    val sessionId: String? = null
)

@Serializable
data class DecisionCheckStartResponseDto(
    val sessionId: String,
    val status: String
)

@Serializable
data class DecisionCheckSessionDto(
    val id: String,
    val status: String,
    val decisionCheckCode: String,
    val decisionCheckTitle: String,
    val decisionCheckDescription: String,
    val definition: List<DecisionQuestionDto> = emptyList(),
    val toolMeta: JsonElement? = null,
    val answers: List<DecisionAnswerDto> = emptyList()
)

@Serializable
data class DecisionOptionDto(
    val value: Float,
    val label: String,
    val description: String? = null
)

@Serializable
data class DecisionQuestionDto(
    val code: String,
    val label: String,
    val description: String,
    val type: String, // 'money', 'percentage', 'number', 'days', 'months', 'choice', 'boolean'
    val required: Boolean = true,
    val allowUnknown: Boolean = false,
    val min: Float? = null,
    val max: Float? = null,
    val step: Float? = null,
    val suffix: String? = null,
    val order: Int = 0,
    val options: List<DecisionOptionDto>? = null
)

@Serializable
data class DecisionAnswerDto(
    val questionCode: String,
    val valueJson: JsonElement? = null,
    val isUnknown: Boolean = false
)

@Serializable
data class DecisionUpdateAnswerRequestDto(
    val questionCode: String,
    val value: JsonElement? = null,
    val isUnknown: Boolean = false
)

@Serializable
data class DecisionUpdateAnswerResponseDto(
    val success: Boolean
)

@Serializable
data class DecisionCompleteResponseDto(
    val resultId: String,
    val snapshot: JsonElement? = null
)

@Serializable
data class DecisionResultDto(
    val id: String,
    val status: String,
    val riskLevel: String? = null,
    val snapshot: JsonElement? = null
)

@Serializable
data class DecisionHistorySessionDto(
    val id: String,
    val decisionCheckCode: String,
    val decisionCheckTitle: String,
    val status: String,
    val startedAt: String,
    val updatedAt: String,
    val completedAt: String? = null
)

