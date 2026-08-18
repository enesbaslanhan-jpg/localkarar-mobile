package com.localkarar.app.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class CommunityPostDto(
    val id: String,
    val title: String,
    val summary: String,
    val postType: String = "user",
    val status: String = "published",
    val publishedAt: String? = null,
    val createdAt: String? = null,
    val author: CommunityAuthorDto? = null,
    val media: List<CommunityMediaDto> = emptyList(),
    val rejectionReason: String? = null
)

@Serializable
data class CommunityAuthorDto(
    val id: String,
    val name: String,
    val email: String? = null
)

@Serializable
data class CommunityMediaDto(
    val id: String,
    val originalName: String? = null,
    val mimeType: String? = null,
    val width: Int? = null,
    val height: Int? = null
)

@Serializable
data class CommunityFeedResponseDto(
    val posts: List<CommunityPostDto> = emptyList(),
    val nextCursor: String? = null
)

@Serializable
data class CreateCommunityPostRequestDto(
    val title: String,
    val summary: String,
    val mediaId: String? = null
)

@Serializable
data class ReportPostRequestDto(
    val reason: String,
    val details: String? = null
)