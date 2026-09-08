package com.localkarar.app.network.dto

import kotlinx.serialization.Serializable

/**
 * `GET /api/v2/search?q=` — GENEL ARAMA.
 *
 * 🔴 Mobilde HIC KULLANILMIYORDU. Her ekranin kendi aramasi vardi ama
 * "kisi, paylasim, kurs, karar araci, haber" tek yerde aranamiyordu.
 *
 * ⚠️ Sunucu `knowledge` alanini da donuyor ama ARAYUZDE GOSTERILMIYOR:
 * Bilgi Kutuphanesi urunden kaldirildi ve web de bu grubu cizmiyor.
 * Alan yine de tanimli degil -- okunmayan bir seyi cozmeye gerek yok.
 */
@Serializable
data class GenelAramaDto(
    val courses: List<AramaKursDto> = emptyList(),
    val decisionChecks: List<AramaKararAraciDto> = emptyList(),
    val news: List<AramaHaberDto> = emptyList(),
    val people: List<AramaKisiDto> = emptyList(),
    val posts: List<AramaGonderiDto> = emptyList()
)

@Serializable
data class AramaKursDto(
    val id: Int,
    val title: String,
    val description: String? = null,
    val category: String? = null,
    val level: String? = null,
    val slug: String? = null
)

@Serializable
data class AramaKararAraciDto(
    val id: String,
    val code: String,
    val title: String,
    val description: String? = null,
    val category: String? = null
)

@Serializable
data class AramaHaberDto(
    val id: String,
    val title: String,
    val summary: String? = null,
    val category: String? = null,
    val sourcePublishedAt: String? = null
)

@Serializable
data class AramaKisiDto(
    val id: Int,
    val name: String,
    val bio: String? = null,
    val avatarUrl: String? = null
)

@Serializable
data class AramaGonderiDto(
    val id: String,
    val ozet: String = "",
    val publishedAt: String? = null,
    val author: CommunityAuthorDto? = null
)
