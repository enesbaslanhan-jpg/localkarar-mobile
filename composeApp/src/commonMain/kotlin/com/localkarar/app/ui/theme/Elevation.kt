package com.localkarar.app.ui.theme

import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/*
 * YUKSELTME KADEMELERI — `DESIGN.md` §3.2 + §24.5.
 *
 * 🔴 §24.5: KOYU TEMADA GOLGE YOKTUR.
 *
 * Onceki surum iki temada da golge ciziyor, koyu tarafta rengi neredeyse
 * opak siyah yapiyordu. Siyah zeminde siyah golge gorunmez; kademeler
 * birbirinden ayirt edilemiyordu. Koyu temada derinlik iki sey tasir:
 *   1. yuzey tonu yukseltmesi (`surfaceRaised` > `surfacePanel` > `canvas`)
 *   2. ust kenarda 1dp beyaz %6 isik cizgisi — isigin yukaridan geldigi
 *      hissi, fiziksel arayuzlerin standardi
 *
 * 🔴 `isSystemInDarkTheme()` KULLANILMIYOR — DENETLEYICIYI ATLIYORDU.
 *
 * Kullanici Ayarlar'dan "Acik" sectiginde ama isletim sistemi koyu
 * temadayken o cagri yine `true` donuyor ve acik temaya koyu tema golgesi
 * ciziliyordu. Dogru kaynak `LocalLkIsDark`: temanin kendi karari.
 *
 * §3.1'deki oncelik sirasi korunuyor:
 *     surface contrast → border → subtle shadow → glow
 * Golge yuzeyi AYIRAN sey degil, ayrimi PEKISTIREN sey.
 */
object LkElevation {
    /** §3.2 shadow-sm — kart varsayilani. */
    val SM: Dp = 2.dp

    /** §3.2 shadow-md — yuzen panel, one cikan kart. */
    val MD: Dp = 8.dp

    /** §3.2 shadow-overlay — YALNIZ modal / cekmece / popover. */
    val OVERLAY: Dp = 24.dp

    /** Yuzen dock. */
    val DOCK: Dp = 16.dp

    /** §24.5 — koyu temada ust kenar isigi. */
    val EdgeLight: Color = Color(0x0FFFFFFF)   // beyaz %6
}

/** Yururlukteki temanin koyu olup olmadigi. Sistem cagrisi DEGIL. */
@Composable
@ReadOnlyComposable
fun lkIsDark(): Boolean = LocalLkIsDark.current

/**
 * §24.5 — tema duyarli yukseltme.
 *
 * Acik temada yumusak golge; koyu temada golge yerine ust kenar isigi.
 * Bilesenler kademe ADI ister, ham deger degil.
 *
 * `clip = false`: golge yuzeyin disina tasar; `true` olsaydi kirpilirdi.
 */
@Composable
fun Modifier.lkElevation(
    elevation: Dp,
    shape: Shape,
    clip: Boolean = false
): Modifier =
    if (lkIsDark()) {
        /*
         * Koyu tema: golge cizilmiyor. Derinligi yuzey tonu tasiyor,
         * ust kenardaki 1dp isik pekistiriyor.
         */
        this.border(1.dp, LkElevation.EdgeLight, shape)
    } else {
        val renk = Color(0xFF10181F)   // notr degil, markaya dogru hafif mavi
        this.shadow(
            elevation = elevation,
            shape = shape,
            clip = clip,
            ambientColor = renk,
            spotColor = renk
        )
    }

/**
 * Eski cagri adi.
 *
 * 50'ye yakin cagri yeri var; hepsini tek seferde degistirmek yerine
 * `lkElevation`e yonlendiriliyor. Yeni kod `lkElevation` kullanir.
 */
@Composable
fun Modifier.lkShadow(
    elevation: Dp,
    shape: Shape,
    clip: Boolean = false
): Modifier = this.lkElevation(elevation, shape, clip)
