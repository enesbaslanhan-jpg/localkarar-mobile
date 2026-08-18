package com.localkarar.app.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class NewsArticleDto(
    val id: String,
    val title: String,
    val category: String,
    val canonicalUrl: String? = null,
    val imageId: String? = null,
    val imagePath: String? = null,
    val sourceName: String,
    val sourcePublishedAt: String,
    val summary: String? = null,
    val whyItMatters: String? = null,
    val tags: List<String> = emptyList(),
    val affectedAudience: List<String> = emptyList(),
    val importance: String? = null
)

@Serializable
data class NewsFeedResponseDto(
    val items: List<NewsArticleDto> = emptyList(),
    val nextCursor: String? = null
)

@Serializable
data class NewsDetailResponseDto(
    val article: NewsArticleDto
)