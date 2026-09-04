package com.localkarar.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween

/*
 * HAREKET — `DESIGN.md` §12.
 *
 * 🔴 UYGULAMADA HIC HAREKET YOKTU. Ne basma geri bildirimi, ne sayfa gecisi,
 * ne durum degisimi animasyonu vardi; arayuz ani zipliyordu.
 *
 * §12'nin iki sert kurali:
 *   1. Component basina YENI EASING YOK. Tek egri: cubic-bezier(.4, 0, .2, 1).
 *   2. 500ms+ gecis varsayilan DEGIL. Etkilesim hizli hissettirmeli.
 *
 * ⚠️ `prefers-reduced-motion` karsiligi: Compose Multiplatform'da ortak
 * katmanda boyle bir sorgu YOK. §12 bu tercihe uyulmasini istiyor; platform
 * bazli `actual` gerekiyor ve HENUZ YAZILMADI -- bilinen eksik.
 */
object LkMotion {

    /** §12: component basina yeni easing uretilmez. Tek standart egri. */
    val Standard: Easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

    /** §12 `fast` 120–150ms — buton basma, hover, focus, ikon kaymasi. */
    const val FAST = 140

    /** §12 `normal` 200–260ms — kart hover, panel acilisi, sayfa gecisi. */
    const val NORMAL = 220

    /** §12 `slow` 320–360ms — modal, drawer, buyuk yuzey. */
    const val SLOW = 340

    fun <T> fast(): FiniteAnimationSpec<T> = tween(FAST, easing = Standard)
    fun <T> normal(): FiniteAnimationSpec<T> = tween(NORMAL, easing = Standard)
    fun <T> slow(): FiniteAnimationSpec<T> = tween(SLOW, easing = Standard)
}
