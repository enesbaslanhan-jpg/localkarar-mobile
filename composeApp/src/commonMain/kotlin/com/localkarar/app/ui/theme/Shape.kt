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
 * §24.3 — MOBIL ISTISNASI.
 *
 * §3.3 "24px radius her karta verilmez; 16px ustu yalniz feature kart ve
 * hero yuzeylerdedir" diyor. §24.3 bunu mobil icin GEVSETIYOR: dokunmatik
 * arayuzde kart daha buyuk yaricapla dokunulabilir bir nesne gibi okunuyor.
 *
 * Gevseme SINIRLI: 24dp ustu hala her yuzeye verilmez, yalniz Card ve
 * Sheet kademelerine. Web yuzeylerinde §3.3 yasagi aynen gecerli.
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

    /** §24.3 mobil — chip, badge, kucuk etiket. */
    val Small = RoundedCornerShape(10.dp)

    /** §24.3 mobil — KART. Web §3.3 12-16dp; mobilde 20dp. */
    val Card = RoundedCornerShape(20.dp)

    /** §24.3 mobil — cekmece ve binen yuzey. Yalniz UST kose. */
    val Sheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 0.dp, bottomEnd = 0.dp)

    /** §24.3 mobil — her kosesi yuvarlak cekmece (yuzen panel). */
    val SheetAll = RoundedCornerShape(28.dp)

    /** §3.3 radius-full — pill buton, badge, avatar. */
    val FULL = RoundedCornerShape(999.dp)
}
