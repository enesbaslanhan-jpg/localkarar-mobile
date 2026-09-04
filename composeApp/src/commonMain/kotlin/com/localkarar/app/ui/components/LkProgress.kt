package com.localkarar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.*

/*
 * ILERLEME — `DESIGN.md` §11 ve §22.
 *
 * 🔴 22 DOSYA KENDI ILERLEME CUBUGUNU KURUYORDU. Olculen yukseklikler:
 * 2, 4, 6, 8, 12, 16dp. §11 tek deger soyluyor (`yukseklik 6px, dolgu brand`)
 * ve §22 "ayni componentin sayfa sayfa farkli olcuye sahip olmasi" durumunu
 * acikca yasakliyor. Bu, §0'in "page-level CSS yeni component olusturamaz"
 * kuralinin en yaygin ihlaliydi.
 *
 * §11: Progress — linear (varsayilan) / circular, yukseklik 6px, dolgu brand.
 */

/** §11 linear progress — TEK yukseklik: 6dp. */
val LkProgressHeight: Dp = 6.dp

/**
 * Belirli ilerleme (0f..1f).
 *
 * Dolgu `primaryFill` (brand-500): §8.1 ilerleme dolgusunu brand-500 olarak
 * tanimliyor ve bu deger her iki temada ayni -- ilerleme oraninin algisi
 * temaya gore degismemeli.
 *
 * Deger degisimi animasyonlu (§12 `normal`); ani ziplama ilerlemenin
 * arttigini gizliyordu.
 */
@Composable
fun LkProgress(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val oran by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = lkAnim(LkMotion.normal())
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(LkProgressHeight)
            .clip(LkShapes.FULL)
            .background(LkSurfaceSunken)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(oran)
                .height(LkProgressHeight)
                .clip(LkShapes.FULL)
                .background(LkPrimaryFill)
        )
    }
}

/**
 * Belirsiz ilerleme — §11 `circular`.
 *
 * Ekranin tamamini kaplayan bekleme durumu icin `LkLoadingState` var; bu
 * yalniz satir ici / buton ici bekleme icin.
 */
@Composable
fun LkProgressCircular(
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = LkPrimary,
        strokeWidth = 2.dp
    )
}
