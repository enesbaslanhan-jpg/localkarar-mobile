package com.localkarar.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/*
 * GOLGE KADEMELERI — `DESIGN.md` §3.2.
 *
 * 🔴 GOLGE HIC YOKTU. Butun yuzeyler `elevation = 0` ciziliyordu; kart ile
 * zemin arasindaki tek fark 1px kenarlikti ve acik temada iki yuzey %3 ton
 * farkiyla birbirine karisiyordu.
 *
 * §3.1'deki oncelik sirasi korunuyor:
 *     surface contrast → border → subtle shadow → glow
 * Yani golge yuzeyi AYIRAN sey degil, ayrimi PEKISTIREN sey. Kart varsayilani
 * `sm`, hover `md`, yalniz modal/drawer/popover `overlay` (§3.2).
 *
 * ⚠️ Compose'un `shadow()` API'si CSS gibi ofset/yayilma almiyor; tek bir
 * `elevation` degeri var. Asagidaki degerler CSS blur yaricaplarindan
 * turetilmis en yakin karsiliklar, birebir kopya degil. Koyu modda CSS golgesi
 * cok daha opak (`rgba(0,0,0,.45-.72)`); Compose'da bunu `ambientColor` ve
 * `spotColor` ile veriyoruz, yoksa koyu zeminde golge hic gorunmuyordu.
 */
object LkElevation {
    /** §3.2 shadow-sm — kart varsayilani. */
    val SM: Dp = 2.dp

    /** §3.2 shadow-md — kart hover, yuzen panel. */
    val MD: Dp = 6.dp

    /** §3.2 shadow-overlay — YALNIZ modal / drawer / popover. */
    val OVERLAY: Dp = 24.dp

    /** Yuzen dock. Prototipteki `--dock-shadow: 0 16px 36px`. */
    val DOCK: Dp = 16.dp
}

/**
 * Tema duyarli golge.
 *
 * Koyu modda golge rengi neredeyse opak siyah olmali (§3.2 dark sutunu:
 * `rgba(0,0,0,.45)`–`.72`); Compose'un varsayilan golge rengi koyu zeminde
 * fiilen gorunmez.
 */
@Composable
@ReadOnlyComposable
fun lkShadowColor(): Color =
    if (isSystemInDarkTheme()) Color.Black else Color(0xFF1A1C1E)

/**
 * `DESIGN.md` §3.2'ye uygun golge.
 *
 * `clip = false`: golge yuzeyin disina tasar; `true` olsaydi kirpilirdi.
 */
@Composable
fun Modifier.lkShadow(
    elevation: Dp,
    shape: Shape,
    clip: Boolean = false
): Modifier {
    val renk = lkShadowColor()
    return this.shadow(
        elevation = elevation,
        shape = shape,
        clip = clip,
        ambientColor = renk,
        spotColor = renk
    )
}
