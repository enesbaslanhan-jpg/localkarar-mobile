package com.localkarar.app.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * SAYI ALANLARINDA CANLI BINLIK AYRACI (29.09.2026).
 *
 * "10000" yazan kullanıcı ekranda "10.000" görür. Ürün sahibi: "sayılar
 * noktalı yazılmıyor, 10.000 gibi yazılmalı".
 *
 * 🔴 YALNIZ GÖRÜNÜM: alanın ALTINDAKİ metin (state) olduğu gibi kalır —
 * rakamlar ve en fazla bir virgül. Noktalar ekrana ekleniyor, değere
 * girmiyor; böylece `LkFormatting.parseDecimal` ve `replace(",", ".")`
 * kullanan tüm çağıranlar aynen çalışıyor. Metin zaten nokta içeriyorsa
 * (ör. "2.508,90" biçimli ön doldurma) hiçbir şey yapılmaz; iki kez
 * gruplama olmaz.
 */
class BinlikAyiracDonusumu : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val ham = text.text
        val eslesme = KALIP.matchEntire(ham) ?: return TransformedText(text, OffsetMapping.Identity)
        val isaret = eslesme.groupValues[1]
        val tam = eslesme.groupValues[2]
        val ondalik = eslesme.groupValues[3]
        val gruplu = tam.reversed().chunked(3).joinToString(".").reversed()
        val cikti = isaret + gruplu + ondalik
        if (cikti == ham) return TransformedText(text, OffsetMapping.Identity)

        return TransformedText(
            AnnotatedString(cikti),
            object : OffsetMapping {
                /* İmleç, o ana kadar yazılan karakterlerin hemen ardında (sonraki noktanın önünde) kalır. */
                override fun originalToTransformed(offset: Int): Int {
                    var tuketilen = 0
                    var t = 0
                    while (t < cikti.length && tuketilen < offset) {
                        if (cikti[t] != '.') tuketilen++
                        t++
                    }
                    return t
                }

                override fun transformedToOriginal(offset: Int): Int {
                    val sinir = offset.coerceIn(0, cikti.length)
                    return sinir - cikti.take(sinir).count { it == '.' }
                }
            }
        )
    }

    private companion object {
        val KALIP = Regex("^(-?)([0-9]+)(,[0-9]*)?$")
    }
}
