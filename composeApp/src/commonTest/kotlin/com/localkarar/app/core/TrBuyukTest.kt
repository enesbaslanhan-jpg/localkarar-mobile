package com.localkarar.app.core

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 🦷 TURKCE BUYUK HARF.
 *
 * `String.uppercase()` yerelden bagimsiz calisiyor ve `i` -> `I`
 * yapiyor. Olculdu (08.09.2026, emulator): karar makbuzunda "İNDIRIM
 * ÖNCESI KATKI" yaziyordu, avatar bas harfleri de yanlisti.
 *
 * Bu test dogru davranisi kilitliyor: biri `trBuyuk()`u tekrar
 * `uppercase()`e cevirirse duser.
 */
class TrBuyukTest {

    @Test
    fun noktaliIBuyurken_noktasiniKoruyor() {
        assertEquals("İNDİRİM", "indirim".trBuyuk())
        assertEquals("İREM", "irem".trBuyuk())
        /* Makbuzda gorulen gercek metin. */
        assertEquals("İNDİRİM ÖNCESİ KATKI", "İndirim öncesi katkı".trBuyuk())
    }

    @Test
    fun noktasizIBuyurken_noktasiz_kaliyor() {
        assertEquals("IŞIK", "ışık".trBuyuk())
        /* Ayni kelimede iki i turu birden. */
        assertEquals("ILIK İÇECEK", "ılık içecek".trBuyuk())
    }

    @Test
    fun zatenBuyukHarfler_bozulmuyor() {
        assertEquals("TUTAR", "TUTAR".trBuyuk())
        assertEquals("₺1.250", "₺1.250".trBuyuk())
    }
}
