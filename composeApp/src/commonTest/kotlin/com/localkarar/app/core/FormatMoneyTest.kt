package com.localkarar.app.core

import kotlin.test.Test
import kotlin.test.assertEquals

/* Para biçimi web ile aynı: kuruş varsa iki hane, yoksa hiç (15.09.2026). */
class FormatMoneyTest {
    @Test
    fun kurusVarsaIkiHane() {
        assertEquals("₺2.508,90", LkFormatting.formatMoney(2508.9))
        assertEquals("₺5.298,79", LkFormatting.formatMoney(5298.79))
        assertEquals("₺0,05", LkFormatting.formatMoney(0.05))
    }

    @Test
    fun kurusYoksaHaneYok() {
        assertEquals("₺5.600", LkFormatting.formatMoney(5600.0))
        assertEquals("₺0", LkFormatting.formatMoney(0.0))
        assertEquals("₺-1.000", LkFormatting.formatMoney(-1000.0))
    }
}
