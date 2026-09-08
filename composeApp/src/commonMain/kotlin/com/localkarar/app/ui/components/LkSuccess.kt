package com.localkarar.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material.Text
import com.localkarar.app.ui.theme.LkShapes
import com.localkarar.app.ui.theme.LkSpacing
import com.localkarar.app.ui.theme.LkSuccess
import com.localkarar.app.ui.theme.LkTextPrimary
import com.localkarar.app.ui.theme.LkTextSecondary
import com.localkarar.app.ui.theme.LkTypography
import com.localkarar.app.ui.theme.isReducedMotionEnabled
import kotlin.math.sin
import kotlin.random.Random

/*
 * §24 — DORT ZORUNLU ANIMASYONDAN UCUNCUSU: BASARI.
 *
 * ⚠️ SADECE GERCEK TAMAMLANMA ANLARINDA. Her kaydetmede konfeti atan bir
 * finans araci oyuncak gibi gorunur. Kural (devir belgesi §4):
 *   - tik: senaryo kaydi gibi tamamlanan islemler,
 *   - konfeti: YALNIZ karar oturumunun bitisi.
 *
 * Hareket kisitliyken ikisi de cizilmez; tik son haliyle durur, konfeti
 * hic gorunmeden gecer (§24.8).
 */

// ──────────────────────────────────────────────────────────────────
// TIK
// ──────────────────────────────────────────────────────────────────

/**
 * Cizilerek tamamlanan onay tiki.
 *
 * Tik iki dogru parcasi: kisa inis + uzun cikis. Ikisi SIRAYLA ciziliyor,
 * ayni anda degil — ayni anda cizilirse el hareketi degil iki ayri cizgi
 * gibi gorunuyor.
 */
@Composable
fun LkSuccessTick(
    modifier: Modifier = Modifier,
    boyut: Dp = 64.dp,
    renk: Color = LkSuccess
) {
    val kisitli = isReducedMotionEnabled()
    val ilerleme = remember { Animatable(if (kisitli) 1f else 0f) }

    LaunchedEffect(kisitli) {
        if (kisitli) {
            ilerleme.snapTo(1f)
        } else {
            ilerleme.snapTo(0f)
            ilerleme.animateTo(1f, tween(520, easing = LinearEasing))
        }
    }

    Box(
        modifier = modifier.size(boyut).clip(LkShapes.FULL).background(renk.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(boyut * 0.5f)) {
            val w = size.width
            val h = size.height
            val p0 = Offset(w * 0.04f, h * 0.54f)
            val p1 = Offset(w * 0.38f, h * 0.86f)
            val p2 = Offset(w * 0.96f, h * 0.16f)
            val kalinlik = w * 0.13f
            val t = ilerleme.value

            /* Yolun ilk %38'i birinci parca, kalani ikinci parca. */
            val t1 = (t / 0.38f).coerceIn(0f, 1f)
            if (t1 > 0f) {
                drawLine(
                    color = renk,
                    start = p0,
                    end = Offset(p0.x + (p1.x - p0.x) * t1, p0.y + (p1.y - p0.y) * t1),
                    strokeWidth = kalinlik,
                    cap = StrokeCap.Round
                )
            }
            val t2 = ((t - 0.38f) / 0.62f).coerceIn(0f, 1f)
            if (t2 > 0f) {
                drawLine(
                    color = renk,
                    start = p1,
                    end = Offset(p1.x + (p2.x - p1.x) * t2, p1.y + (p2.y - p1.y) * t2),
                    strokeWidth = kalinlik,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────
// KONFETI
// ──────────────────────────────────────────────────────────────────

private class Parcacik(
    val x0: Float,
    val hiz: Float,
    val yon: Float,
    val donus: Float,
    val en: Float,
    val boy: Float,
    val renk: Color,
    val gecikme: Float
)

/**
 * Ustten dusen parcaciklar.
 *
 * Parcaciklar SABIT bir tohumla uretiliyor: her yeniden bilesimde yeniden
 * rastgelelestirilirse parcaciklar animasyonun ortasinda yer degistirir.
 *
 * Tek seferlik — bittiginde `onBitti` cagriliyor, cagiran taraf bileseni
 * kaldiriyor. Surekli akan konfeti dekoratif gurultu olurdu.
 */
@Composable
fun LkConfetti(
    modifier: Modifier = Modifier,
    adet: Int = 90,
    sure: Int = 2200,
    tohum: Int = 0,
    onBitti: () -> Unit = {}
) {
    if (isReducedMotionEnabled()) {
        LaunchedEffect(tohum) { onBitti() }
        return
    }

    val renkler = listOf(
        Color(0xFF306D88), Color(0xFF7BA2B3), Color(0xFFE2B04A),
        Color(0xFF4C9A6A), Color(0xFFD1DEE4)
    )
    val parcaciklar = remember(tohum, adet) {
        val r = Random(tohum + 7391)
        List(adet) {
            Parcacik(
                x0 = r.nextFloat(),
                hiz = 0.62f + r.nextFloat() * 0.55f,
                yon = (r.nextFloat() - 0.5f) * 0.42f,
                donus = (r.nextFloat() - 0.5f) * 14f,
                en = 5f + r.nextFloat() * 5f,
                boy = 9f + r.nextFloat() * 8f,
                renk = renkler[r.nextInt(renkler.size)],
                gecikme = r.nextFloat() * 0.28f
            )
        }
    }

    val t = remember { Animatable(0f) }
    LaunchedEffect(tohum) {
        t.snapTo(0f)
        t.animateTo(1f, tween(sure, easing = LinearEasing))
        onBitti()
    }

    Canvas(modifier.fillMaxSize()) {
        parcaciklar.forEach { p ->
            val yerel = ((t.value - p.gecikme) / (1f - p.gecikme)).coerceIn(0f, 1f)
            if (yerel <= 0f) return@forEach
            val y = -30f + yerel * (size.height + 60f) * p.hiz
            val x = p.x0 * size.width + sin(yerel * 6.2f + p.x0 * 9f) * size.width * p.yon
            val solma = if (yerel > 0.82f) (1f - yerel) / 0.18f else 1f
            rotate(degrees = yerel * 360f * p.donus, pivot = Offset(x, y)) {
                drawRect(
                    color = p.renk.copy(alpha = solma.coerceIn(0f, 1f)),
                    topLeft = Offset(x - p.en / 2f, y - p.boy / 2f),
                    size = Size(p.en, p.boy)
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────
// BASARI PANELI — mockup "Senaryo kaydedildi" karesi
// ──────────────────────────────────────────────────────────────────

/**
 * Tamamlanma bildirimi: tik + tek satir baslik + gerekcesi + tek dugme.
 *
 * Karartma YOK; panel yuzeyin ustunde duruyor ve arkasi okunur kaliyor.
 * Mockup'ta da boyle: islem bitti, ekran degismedi.
 */
@Composable
fun LkSuccessPanel(
    baslik: String,
    modifier: Modifier = Modifier,
    altBaslik: String? = null,
    konfeti: Boolean = false,
    dugmeMetni: String = "Tamam",
    onKapat: () -> Unit
) {
    Box(modifier.fillMaxWidth()) {
        LkCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
            ) {
                LkSuccessTick()
                Text(
                    text = baslik,
                    style = LkTypography.getCardTitle(),
                    color = LkTextPrimary,
                    textAlign = TextAlign.Center
                )
                if (altBaslik != null) {
                    Text(
                        text = altBaslik,
                        style = LkTypography.getMetadata(),
                        color = LkTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.height(LkSpacing.Space1))
                LkButton(
                    text = dugmeMetni,
                    onClick = onKapat,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        if (konfeti) {
            LkConfetti(Modifier.matchParentSize())
        }
    }
}
