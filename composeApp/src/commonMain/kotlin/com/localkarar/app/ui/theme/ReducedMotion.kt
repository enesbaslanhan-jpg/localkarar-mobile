package com.localkarar.app.ui.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

/*
 * HAREKET KISITLAMA — `DESIGN.md` §12 ve §19.
 *
 * §12: `prefers-reduced-motion: reduce` → tum animasyon/gecis 0.001ms.
 * §19: "Reduced motion — §12 global kural."
 *
 * 🔴 BU BORCU BU TURDA BIZ URETTIK. Daha once uygulamada hic animasyon yoktu,
 * dolayisiyla kisilacak bir sey de yoktu. `LkMotion` ve basma/nabiz
 * animasyonlari eklendigi anda §12'nin bu maddesi baglayici hale geldi.
 *
 * Compose Multiplatform'un ORTAK katmaninda boyle bir sorgu yok; her platform
 * kendi isletim sistemi ayarindan okur:
 *   Android → Settings.Global.ANIMATOR_DURATION_SCALE == 0
 *   iOS     → UIAccessibility.isReduceMotionEnabled
 */

/**
 * Kullanici isletim sisteminde hareketi kisitlamis mi?
 *
 * ⚠️ Deger okuma anindaki durumdur. Kullanici uygulama acikken ayari
 * degistirirse Android tarafinda yeni bir composition'a kadar eski deger
 * gecerli kalir; ayar nadiren degistigi icin dinleyici kurulmadi.
 */
@Composable
@ReadOnlyComposable
expect fun isReducedMotionEnabled(): Boolean

/**
 * Hareket kisitliyken ANINDA biten, degilken verilen spec.
 *
 * §12 "0.001ms" diyor; Compose'da bunun dogru karsiligi `snap()` -- sifir
 * sureli bir `tween` yerine hic ara kare uretmeyen spec.
 *
 * Kullanim: `animateFloatAsState(hedef, lkAnim(LkMotion.fast()))`
 */
@Composable
@ReadOnlyComposable
fun <T> lkAnim(spec: FiniteAnimationSpec<T>): FiniteAnimationSpec<T> =
    if (isReducedMotionEnabled()) snap() else spec

/**
 * Suslemesel, kendiliginden donen animasyonlar (nabiz gibi) icin.
 *
 * Bunlar kisitlamada YAVASLATILMAZ, TAMAMEN DURDURULUR: sonsuz donen bir
 * animasyonu hizli oynatmak kisitlamanin amacina aykiri.
 */
@Composable
@ReadOnlyComposable
fun lkAllowDecorativeMotion(): Boolean = !isReducedMotionEnabled()
