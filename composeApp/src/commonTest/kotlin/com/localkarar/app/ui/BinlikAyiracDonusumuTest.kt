package com.localkarar.app.ui

import androidx.compose.ui.text.AnnotatedString
import com.localkarar.app.ui.components.BinlikAyiracDonusumu
import kotlin.test.Test
import kotlin.test.assertEquals

/*
 * Sayı alanlarındaki canlı binlik ayracı (29.09.2026). Alttaki metin
 * DEĞİŞMEZ; yalnız görünüm. İmleç eşlemesi yanlışsa yazarken imleç
 * sıçrar, o yüzden iki yön de sınanıyor.
 */
class BinlikAyiracDonusumuTest {
    private fun goster(ham: String) = BinlikAyiracDonusumu().filter(AnnotatedString(ham)).text.text

    @Test fun binlikler() {
        assertEquals("10.000", goster("10000"))
        assertEquals("1.234.567", goster("1234567"))
        assertEquals("999", goster("999"))
        assertEquals("1.000", goster("1000"))
    }

    @Test fun ondalikVeEksi() {
        assertEquals("10.000,5", goster("10000,5"))
        assertEquals("1.000,", goster("1000,"))
        assertEquals("-12.500,75", goster("-12500,75"))
        assertEquals("0,5", goster("0,5"))
    }

    @Test fun zatenNoktaliyseDokunmaz() {
        assertEquals("2.508,90", goster("2.508,90"))
        assertEquals("10.5", goster("10.5"))
        assertEquals("", goster(""))
        assertEquals("abc", goster("abc"))
    }

    @Test fun imlecEslemesi() {
        val t = BinlikAyiracDonusumu().filter(AnnotatedString("1234567"))
        val m = t.offsetMapping
        // "1.234.567": ham sonu (7) -> 9; ham 1 ("1|") -> 1; ham 4 ("1234|") -> 5
        assertEquals(9, m.originalToTransformed(7))
        assertEquals(1, m.originalToTransformed(1))
        assertEquals(5, m.originalToTransformed(4))
        assertEquals(0, m.originalToTransformed(0))
        assertEquals(7, m.transformedToOriginal(9))
        assertEquals(4, m.transformedToOriginal(5))
        assertEquals(1, m.transformedToOriginal(2)) // noktanın ardı = 1 rakam
    }
}
