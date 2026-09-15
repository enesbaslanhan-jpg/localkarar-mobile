package com.localkarar.app.network

import com.localkarar.app.network.dto.ConnectionSettingsRequestDto
import com.localkarar.app.network.dto.IntegrationConnectionDto
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * PAZARYERI AYARLARI SOZLESMESI (Faz 3, 15.09.2026).
 * PATCH /integrations/:id/settings: iki alan da her seferinde gidiyor,
 * null ACIKCA yaziliyor (temizle). Eski sunucu yaniti (alanlar yok) hala
 * cozumleniyor.
 */
class ConnectionSettingsContractTest {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    @Test
    fun nullAlanlarGovdedeAcikcaYaziliyor() {
        val govde = json.encodeToString(ConnectionSettingsRequestDto.serializer(), ConnectionSettingsRequestDto(14, null))
        assertTrue(govde.contains("\"payoutDelayDays\":14"), govde)
        assertTrue(govde.contains("\"avgCommissionPercent\":null"), govde)
    }

    @Test
    fun eskiVeYeniSunucuYanitiCozumlenir() {
        val eski = json.decodeFromString<IntegrationConnectionDto>("""{"id":"c1","provider":"TRENDYOL","status":"ACTIVE"}""")
        assertNull(eski.payoutDelayDays); assertNull(eski.avgCommissionPercent)
        val yeni = json.decodeFromString<IntegrationConnectionDto>("""{"id":"c1","provider":"TRENDYOL","status":"ACTIVE","payoutDelayDays":12,"avgCommissionPercent":18.5}""")
        assertEquals(12, yeni.payoutDelayDays); assertEquals(18.5, yeni.avgCommissionPercent)
    }
}
