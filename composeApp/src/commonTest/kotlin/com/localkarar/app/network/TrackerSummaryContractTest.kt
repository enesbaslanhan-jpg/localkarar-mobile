package com.localkarar.app.network

import com.localkarar.app.network.dto.TrackerSummaryDto
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * ISLETME TAKIBI OZETI SOZLESMESI (15.09.2026).
 *
 * Sunucu `tracker/summary`ye plan30 / overdueSplit / periods / currency
 * ekledi (tracker-periods.ts). Iki sey kilitleniyor:
 *  1. YENI sunucu yaniti eksiksiz cozumlenir (pazaryeri hakedisi, tahmini
 *     bayragi, para birimi kirilimi).
 *  2. ESKI sunucu yaniti (yeni alanlar yok) hala cozumlenir — kurulu
 *     uygulama sunucudan once ya da sonra guncellenebilir.
 */
class TrackerSummaryContractTest {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    @Test
    fun yeniSunucuYanitiCozumlenir() {
        val dto = json.decodeFromString<TrackerSummaryDto>(YENI)
        assertEquals("TRY", dto.currency)
        val plan = dto.plan30!!
        assertEquals(5298.79, plan.hakedis.net.amount)
        assertEquals(5, plan.hakedis.orderCount)
        assertTrue(plan.estimated)
        assertEquals(listOf("payoutDelay"), plan.estimatedReasons)
        assertEquals(20850.75, dto.overdueSplit!!.payable.amount)
        assertEquals(1, dto.overdueSplit!!.payable.otherCurrencies.size)
        assertEquals("USD", dto.overdueSplit!!.payable.otherCurrencies[0].currency)
        assertEquals(2508.9, dto.periods!!.today.pazaryeriBrut.amount)
        assertEquals("week", dto.periods!!.week.range.key)
    }

    @Test
    fun eskiSunucuYanitiHalaCozumlenir() {
        val dto = json.decodeFromString<TrackerSummaryDto>(ESKI)
        assertNull(dto.plan30)
        assertNull(dto.periods)
        assertEquals(1200.0, dto.nextThirtyDays.receivable)
    }

    companion object {
        private const val YENI = """{"thisWeek":{"payable":0,"payableCount":0,"receivable":0,"receivableCount":0},"overdueTotals":{"amount":26450.75,"count":3},"cash":null,"counts":{"open":6,"overdue":3,"dueToday":0,"shipments":0,"deferred":0,"awaitingDirection":0},"nextThirtyDays":{"payable":0,"receivable":0,"net":0},"currency":"TRY","plan30":{"receivable":{"amount":0,"currency":"TRY","otherCurrencies":[]},"payable":{"amount":0,"currency":"TRY","otherCurrencies":[]},"hakedis":{"gross":{"amount":6527.4,"currency":"TRY","otherCurrencies":[]},"net":{"amount":5298.79,"currency":"TRY","otherCurrencies":[]},"returns":{"amount":0,"currency":"TRY","otherCurrencies":[]},"orderCount":5,"estimated":true,"estimatedReasons":["payoutDelay"],"byProvider":[{"provider":"TRENDYOL","orderCount":2,"gross":4239.9,"net":3504.11,"estimated":true}]},"net":5298.79,"counts":{"receivable":0,"payable":0,"hakedisOrders":5},"estimated":true,"estimatedReasons":["payoutDelay"]},"overdueSplit":{"payable":{"amount":20850.75,"currency":"TRY","otherCurrencies":[{"currency":"USD","amount":1000,"count":1}]},"receivable":{"amount":5600,"currency":"TRY","otherCurrencies":[]},"count":3},"periods":{"today":{"tahsilat":{"amount":0,"currency":"TRY","otherCurrencies":[]},"odeme":{"amount":0,"currency":"TRY","otherCurrencies":[]},"pazaryeriBrut":{"amount":2508.9,"currency":"TRY","otherCurrencies":[]},"pazaryeriNet":{"amount":2000,"currency":"TRY","otherCurrencies":[]},"iade":{"amount":0,"currency":"TRY","otherCurrencies":[]},"net":2000,"kayitSayisi":{"tahsilat":0,"odeme":0},"siparisSayisi":2,"range":{"key":"today","from":"2026-09-14T21:00:00.000Z","to":"2026-09-15T21:00:00.000Z","timezone":"Europe/Istanbul"}},"week":{"tahsilat":{"amount":0,"currency":"TRY","otherCurrencies":[]},"odeme":{"amount":0,"currency":"TRY","otherCurrencies":[]},"pazaryeriBrut":{"amount":2508.9,"currency":"TRY","otherCurrencies":[]},"pazaryeriNet":{"amount":2000,"currency":"TRY","otherCurrencies":[]},"iade":{"amount":0,"currency":"TRY","otherCurrencies":[]},"net":2000,"kayitSayisi":{"tahsilat":0,"odeme":0},"siparisSayisi":2,"range":{"key":"week","from":"2026-09-13T21:00:00.000Z","to":"2026-09-20T21:00:00.000Z","timezone":"Europe/Istanbul"}},"month":{"tahsilat":{"amount":0,"currency":"TRY","otherCurrencies":[]},"odeme":{"amount":0,"currency":"TRY","otherCurrencies":[]},"pazaryeriBrut":{"amount":7317.3,"currency":"TRY","otherCurrencies":[]},"pazaryeriNet":{"amount":6000,"currency":"TRY","otherCurrencies":[]},"iade":{"amount":789.9,"currency":"TRY","otherCurrencies":[]},"net":6000,"kayitSayisi":{"tahsilat":0,"odeme":0},"siparisSayisi":6,"range":{"key":"month","from":"2026-08-31T21:00:00.000Z","to":"2026-09-30T21:00:00.000Z","timezone":"Europe/Istanbul"}}},"marketplace":{"gross":{"amount":6527.4,"currency":"TRY","otherCurrencies":[]},"net":{"amount":5298.79,"currency":"TRY","otherCurrencies":[]},"returns":{"amount":0,"currency":"TRY","otherCurrencies":[]},"orderCount":5,"estimated":true,"estimatedReasons":["payoutDelay"],"byProvider":[]},"awaitingDirection":{"count":0,"amount":0},"upcoming":[]}"""
        private const val ESKI = """{"thisWeek":{"payable":100,"payableCount":1,"receivable":0,"receivableCount":0},"overdueTotals":{"amount":0,"count":0},"cash":null,"counts":{"open":1,"overdue":0,"dueToday":0,"shipments":0,"deferred":0,"awaitingDirection":0},"nextThirtyDays":{"payable":100,"receivable":1200,"net":1100},"awaitingDirection":{"count":0,"amount":0},"upcoming":[]}"""
    }
}
