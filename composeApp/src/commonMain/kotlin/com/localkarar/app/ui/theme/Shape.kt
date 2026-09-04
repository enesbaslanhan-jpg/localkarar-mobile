package com.localkarar.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/*
 * YARICAP SISTEMI — `DESIGN.md` §3.3.
 *
 * Iki deger dokumana gore duzeltildi:
 *   XS  4dp → 6dp   (chip, badge, kucuk etiket)
 *   LG 14dp → 16dp  (feature kart, auth paneli)
 *
 * §3.3 yasagi: "24px radius her karta verilmez; 16px ustu yalniz feature
 * kart ve hero yuzeylerdedir." Bu yuzden 24dp'lik bir kademe TANIMLI DEGIL --
 * tanimlansa kullanilirdi.
 */
object LkShapes {
    /** §3.3 radius-xs — chip, badge, kucuk etiket. */
    val XS = RoundedCornerShape(6.dp)

    /** §3.3 radius-sm — input, select, button, liste satiri, tooltip. */
    val SM = RoundedCornerShape(8.dp)

    /** §3.3 radius-md — kart (compact/standard), panel, modal. */
    val MD = RoundedCornerShape(12.dp)

    /** §3.3 radius-lg — feature kart, auth paneli, buyuk kart. */
    val LG = RoundedCornerShape(16.dp)

    /** §3.3 radius-full — pill buton, badge, avatar. */
    val FULL = RoundedCornerShape(999.dp)
}
