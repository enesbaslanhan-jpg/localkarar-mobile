package com.localkarar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.*

/*
 * ROZET — `DESIGN.md` §11.
 *
 * §11: Badge — neutral / brand / success / warning / danger,
 *      `radius-full`, `label` font, padding `4px 10px`.
 *
 * 🔴 ORTAK BIR ROZET YOKTU. Ekranlar durum etiketi gerektiginde `LkChip`i
 * tikanabilir olmayan haliyle ya da elle `Text` + `background` kurarak
 * cikariyordu; ayni "gecikmis" etiketi farkli ekranlarda farkli tonda
 * duruyordu.
 *
 * ⚠️ Rozet TIKLANMAZ. Tiklanabilir bir sey gerekiyorsa `LkChip` ya da
 * `LkPillChip` kullanilir -- rozetin dokunma hedefi de yok, olmamali.
 */

enum class LkBadgeTone { NEUTRAL, BRAND, SUCCESS, WARNING, DANGER }

@Composable
fun LkBadge(
    text: String,
    tone: LkBadgeTone = LkBadgeTone.NEUTRAL,
    modifier: Modifier = Modifier
) {
    // §1.5: zemin varyantlari tint; metin tam doygunlukta. Dolgu yerine tint
    // kullaniliyor cunku §1.5 "success/warning/error hicbir yerde brand ile
    // ayni alanda yarismaz" diyor -- solid dolgu birincil eylemle yarisirdi.
    val yazi: Color = when (tone) {
        LkBadgeTone.NEUTRAL -> LkTextSecondary
        LkBadgeTone.BRAND -> LkPrimary
        LkBadgeTone.SUCCESS -> LkSuccess
        LkBadgeTone.WARNING -> LkWarning
        LkBadgeTone.DANGER -> LkDanger
    }
    val zemin: Color = when (tone) {
        LkBadgeTone.NEUTRAL -> LkSurfaceSunken
        else -> yazi.copy(alpha = 0.14f)
    }

    Box(
        modifier = modifier
            .background(zemin, LkShapes.FULL)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = LkTypography.getLabel(), color = yazi)
    }
}
