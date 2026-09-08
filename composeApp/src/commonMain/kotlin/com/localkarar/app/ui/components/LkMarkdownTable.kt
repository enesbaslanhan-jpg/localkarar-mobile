package com.localkarar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.LkLineSoft
import com.localkarar.app.ui.theme.LkShapes
import com.localkarar.app.ui.theme.LkSpacing
import com.localkarar.app.ui.theme.LkSurfaceSunken
import com.localkarar.app.ui.theme.LkTextPrimary
import com.localkarar.app.ui.theme.LkTextSecondary
import com.localkarar.app.ui.theme.LkTypography

/*
 * MARKDOWN TABLOLARI.
 *
 * 🔴 DERS ICERIGININ BUYUK KISMI OKUNAMIYORDU.
 *
 * Ders govdeleri GFM tablosu iceriyor ("Maliyet Bileşeni | Varsayılan |
 * Gerçekleşen | Gerekçe" gibi). Web bunu `remark-gfm` ile ciziyor
 * (frontend/package.json); mobilin kullandigi markdown cizici tabloyu
 * TANIMIYOR ve hucreleri alt alta duz paragraflar olarak basiyordu.
 * Ekranda "Maliyet Bileşeni / Sinem / in Varsaydığı / 120 TL / 120 TL"
 * diye anlamsiz bir liste cikiyordu — kullanicinin "içerik mockuptaki
 * gibi değil" dedigi sey buydu.
 *
 * Yeni kutuphane EKLENMIYOR: tablo bloklari govdeden ayrilip kendi
 * bilesenimizle ciziliyor, arada kalan metin yine ayni markdown
 * ciziciye gidiyor.
 */

/** Ayrilmis bir tablo: baslik satiri + govde satirlari. */
data class LkMdTablo(
    val basliklar: List<String>,
    val satirlar: List<List<String>>
)

/** Govde parcasi: ya duz markdown metni ya da bir tablo. */
sealed interface LkMdParca {
    data class Metin(val icerik: String) : LkMdParca
    data class Tablo(val tablo: LkMdTablo) : LkMdParca
}

/**
 * Hizalama satiri mi? GFM'de baslik ile govde arasindaki
 * `| --- | :---: |` satiri.
 */
private fun ayracSatiriMi(satir: String): Boolean {
    val t = satir.trim()
    if (!t.contains('-') || !t.contains('|')) return false
    return t.trim('|').split('|').all { hucre ->
        val h = hucre.trim()
        h.isNotEmpty() && h.all { it == '-' || it == ':' || it == ' ' } && h.contains('-')
    }
}

private fun hucrelereAyir(satir: String): List<String> =
    satir.trim().trim('|').split('|').map { it.trim() }

/**
 * Govdeyi metin ve tablo parcalarina boler.
 *
 * ⚠️ Kod blogu icindeki `|` satirlari tablo SAYILMIYOR; orada boru
 * isareti cogu zaman kodun kendisi.
 */
fun lkMarkdownParcala(icerik: String): List<LkMdParca> {
    val satirlar = icerik.lines()
    val parcalar = mutableListOf<LkMdParca>()
    val metin = StringBuilder()
    var kodBlogunda = false
    var i = 0

    fun metniBosalt() {
        if (metin.isNotBlank()) parcalar.add(LkMdParca.Metin(metin.toString().trim('\n')))
        metin.clear()
    }

    while (i < satirlar.size) {
        val satir = satirlar[i]

        if (satir.trim().startsWith("```")) {
            kodBlogunda = !kodBlogunda
            metin.append(satir).append('\n')
            i++
            continue
        }

        val tabloBasiMi = !kodBlogunda &&
            satir.contains('|') &&
            i + 1 < satirlar.size &&
            ayracSatiriMi(satirlar[i + 1])

        if (tabloBasiMi) {
            val basliklar = hucrelereAyir(satir)
            var j = i + 2
            val govdeSatirlari = mutableListOf<List<String>>()
            while (j < satirlar.size && satirlar[j].contains('|') && satirlar[j].isNotBlank()) {
                val hucreler = hucrelereAyir(satirlar[j])
                /* Eksik hucreler bos birakiliyor, fazlasi kirpiliyor:
                   sunucu icerigi elle yazilmis, her satir esit uzunlukta
                   olmayabiliyor. */
                govdeSatirlari.add(
                    List(basliklar.size) { k -> hucreler.getOrElse(k) { "" } }
                )
                j++
            }
            if (govdeSatirlari.isNotEmpty()) {
                metniBosalt()
                parcalar.add(LkMdParca.Tablo(LkMdTablo(basliklar, govdeSatirlari)))
                i = j
                continue
            }
        }

        metin.append(satir).append('\n')
        i++
    }

    metniBosalt()
    return parcalar
}

/**
 * Tablo gorunumu.
 *
 * Iki sutunlu tablo telefonda ETIKET/DEGER listesi olarak ciziliyor —
 * yatay kaydirma gerektirmiyor ve okumasi kolay. Uc ve uzeri sutunda
 * tablo yapisi korunuyor, kabi yatay kaydiriliyor (§19: govde asla yatay
 * kaymaz, kayan yalniz tablonun kendi kabi).
 */
@Composable
fun LkMarkdownTablo(
    tablo: LkMdTablo,
    modifier: Modifier = Modifier
) {
    if (tablo.basliklar.size <= 2) {
        EtiketDegerTablosu(tablo, modifier)
        return
    }

    val kaydirma = rememberScrollState()

    Column(modifier = modifier.fillMaxWidth()) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LkShapes.SM)
            .border(1.dp, LkLineSoft, LkShapes.SM)
            .horizontalScroll(kaydirma)
    ) {
        Row(
            modifier = Modifier
                .background(LkSurfaceSunken)
                .height(IntrinsicSize.Min)
        ) {
            tablo.basliklar.forEachIndexed { index, baslik ->
                Text(
                    text = baslik,
                    style = LkTypography.getMicro(),
                    color = LkTextSecondary,
                    modifier = Modifier
                        .width(if (index == 0) 168.dp else 148.dp)
                        .padding(horizontal = LkSpacing.Space3, vertical = LkSpacing.Space3)
                )
            }
        }

        tablo.satirlar.forEach { satir ->
            LkHairline()
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                satir.forEachIndexed { index, hucre ->
                    Text(
                        text = parseInlineMarkdown(hucre, LkTextPrimary),
                        style = LkTypography.getBodySmall(),
                        color = LkTextPrimary,
                        modifier = Modifier
                            .width(if (index == 0) 168.dp else 148.dp)
                            .padding(horizontal = LkSpacing.Space3, vertical = LkSpacing.Space3)
                    )
                }
            }
        }
    }

        /* Sag sutun kirpildiginda kaydirilabildigi SOYLENIYOR: kirpik
           hucre tek basina her kullaniciya bunu anlatmiyor. */
        if (kaydirma.maxValue > 0) {
            Text(
                text = "Tabloyu yana kaydırabilirsiniz",
                style = LkTypography.getMicro(),
                color = LkTextSecondary,
                modifier = Modifier.padding(top = LkSpacing.Space2)
            )
        }
    }
}

@Composable
private fun EtiketDegerTablosu(tablo: LkMdTablo, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(LkShapes.SM)
            .border(1.dp, LkLineSoft, LkShapes.SM)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LkSurfaceSunken)
                .padding(horizontal = LkSpacing.Space3, vertical = LkSpacing.Space3),
            horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
        ) {
            tablo.basliklar.forEachIndexed { index, baslik ->
                Text(
                    text = baslik,
                    style = LkTypography.getMicro(),
                    color = LkTextSecondary,
                    modifier = Modifier.weight(if (index == 0) 0.45f else 0.55f)
                )
            }
        }
        tablo.satirlar.forEach { satir ->
            LkHairline()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LkSpacing.Space3, vertical = LkSpacing.Space3),
                horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
            ) {
                satir.forEachIndexed { index, hucre ->
                    Text(
                        text = parseInlineMarkdown(hucre, LkTextPrimary),
                        style = LkTypography.getBodySmall(),
                        color = LkTextPrimary,
                        modifier = Modifier.weight(if (index == 0) 0.45f else 0.55f)
                    )
                }
            }
        }
    }
}
