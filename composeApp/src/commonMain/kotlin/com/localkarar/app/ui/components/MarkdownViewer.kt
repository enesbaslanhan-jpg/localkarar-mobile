package com.localkarar.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * OKUMA OLCUSU — mockup "Akademi 3 / Ders okuyucu".
 *
 * 65 karakterlik satir. Telefonda zaten bu genisligin altinda kaliyor;
 * kural tablet ve katlanabilirde is goruyor — 900dp genisligindeki bir
 * ekranda satir 130 karaktere cikar ve goz satir basini kaybeder.
 */
val LkOkumaGenisligi: Dp = 640.dp

/**
 * Uygulamanin tek "okunan" yuzeyi — ders govdesi.
 *
 * 🔴 DIS MARKDOWN KUTUPHANESI (mikepenz) DERS ICERIGINE YETMIYORDU.
 * Iki seyi birden kaybediyordu:
 *
 *   - GFM TABLOLARI: hucreler alt alta duz paragraf olarak basiliyordu.
 *     "Maliyet Bileşeni / Sinem'in Varsaydığı / 120 TL / 120 TL" seklinde
 *     anlamsiz bir liste cikiyordu.
 *   - LaTeX: `$...$` ifadeleri EKRANDA HIC GORUNMUYORDU. Hesap
 *     satirlarinin sayilari yok oluyor, geriye "İşçilik Saat Ücreti:"
 *     gibi bos basliklar kaliyordu — dersin anlattigi tek sey oydu.
 *     (Webde `remark-math` + `rehype-katex` ile ciziliyor.)
 *
 * Uygulamanin kendi cizicisinde (`LkMarkdown`) ikisi de var: matematik
 * `LkMath`, tablolar `LkMarkdownTablo`. `okuma = true` mockup "Akademi
 * 3"un okuma olcusunu veriyor: 15sp govde, 1,7 satir yuksekligi.
 */
@Composable
fun MarkdownViewer(
    content: String,
    modifier: Modifier = Modifier
) {
    LkMarkdown(
        content = content,
        modifier = modifier.fillMaxWidth(),
        okuma = true
    )
}
