package com.localkarar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.LkLineStrong
import com.localkarar.app.ui.theme.LkSpacing
import com.localkarar.app.ui.theme.LkSurfaceSunken
import com.localkarar.app.ui.theme.LkTextPrimary
import com.localkarar.app.ui.theme.LkTypography

/**
 * LaTeX ifadesini cizer.
 *
 * Ayristirma `LkMath.kt` icinde; buradaki is yalnizca yerlesim.
 */
@Composable
fun LkMath(
    latex: String,
    blok: Boolean,
    modifier: Modifier = Modifier,
    renk: Color = LkTextPrimary
) {
    val dugumler = remember(latex) { parseLatex(latex) }

    if (blok) {
        /*
         * BLOK FORMUL: kendi zemininde, yatay KAYDIRILABILIR.
         *
         * Kaydirma sart: formuller uzun Turkce terimlerden kuruluyor
         * ("Net Tahsilat = Brut Satis - (Komisyon + Sanal POS + Kargo...)").
         * Dar bir telefonda sarmalamak formulu okunmaz hale getirir; webde de
         * KaTeX bloklari yatay kayar.
         */
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(LkSurfaceSunken, RoundedCornerShape(8.dp))
                .padding(LkSpacing.Space3)
        ) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DugumleriCiz(dugumler, renk)
            }
        }
    } else {
        // SATIR ICI: cevresindeki metinle ayni satirda akiyor.
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DugumleriCiz(dugumler, renk)
        }
    }
}

@Composable
private fun DugumleriCiz(dugumler: List<MathNode>, renk: Color) {
    dugumler.forEach { dugum ->
        when (dugum) {
            is MathNode.Text -> Text(
                text = dugum.value,
                style = LkTypography.getBodySmall(),
                color = renk,
                softWrap = false
            )

            /*
             * KESIR: pay ustte, payda altta, aralarinda cizgi.
             *
             * Satir ici `(a) / (b)` yazmak daha kolay olurdu ama formulu
             * okumayi zorlastirir: bu icerikte pay ve payda cogu zaman
             * birden fazla terim tasiyor ve duz bolu isareti onceligi
             * belirsiz birakir.
             */
            is MathNode.Fraction -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                /*
                 * 🔴 `IntrinsicSize.Max` ZORUNLU.
                 *
                 * Blok formuller yatay KAYDIRILABILIR bir Row icinde ciziliyor;
                 * orada genislik kisiti SONSUZ. Sonsuz kisit altinda
                 * `fillMaxWidth()` anlamli bir genislik uretmez ve kesir
                 * cizgisi HIC CIZILMEZ -- pay ile payda alt alta durur ama
                 * aralarindaki cizgi kaybolur, yani ifade artik kesir gibi
                 * okunmaz.
                 *
                 * Emulatorde goruldu (03.09.2026). `IntrinsicSize.Max`, Column'a
                 * "en genis cocugun genisligini al" diyerek cizgiye olcebilecegi
                 * bir kisit veriyor.
                 */
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .width(IntrinsicSize.Max)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DugumleriCiz(dugum.numerator, renk)
                }
                Box(
                    modifier = Modifier
                        .padding(vertical = 3.dp)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(LkLineStrong)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DugumleriCiz(dugum.denominator, renk)
                }
            }
        }
    }
}

/**
 * Icinde matematik gecebilen bir metin parcasi.
 *
 * Duz metin ve satir ici formuller birlikte akiyor; blok formuller kendi
 * satirlarini aliyor.
 */
@Composable
fun LkMathText(
    raw: String,
    modifier: Modifier = Modifier,
    renk: Color = LkTextPrimary
) {
    val parcalar = remember(raw) { matematikAyir(raw) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
        parcalar.forEach { parca ->
            when (parca) {
                is MetinParcasi.Duz -> {
                    val metin = parca.value.trim()
                    if (metin.isNotEmpty()) {
                        Text(
                            text = metin,
                            style = LkTypography.getBodySmall(),
                            color = renk,
                            textAlign = TextAlign.Start
                        )
                    }
                }
                is MetinParcasi.Matematik -> LkMath(
                    latex = parca.latex,
                    blok = parca.blok,
                    renk = renk
                )
            }
        }
    }
}
