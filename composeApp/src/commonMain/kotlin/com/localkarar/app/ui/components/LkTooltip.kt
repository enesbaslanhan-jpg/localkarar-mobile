package com.localkarar.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.LkElevation
import com.localkarar.app.ui.theme.LkOnStage
import com.localkarar.app.ui.theme.LkShapes
import com.localkarar.app.ui.theme.LkSpacing
import com.localkarar.app.ui.theme.LkSurfaceStage
import com.localkarar.app.ui.theme.LkTypography
import com.localkarar.app.ui.theme.lkElevation

/**
 * §11 `Tooltip` — devir belgesi §6'daki ikinci eksik bilesen.
 *
 * ⚠️ MOBILDE HOVER YOK. Masaustu tooltip'i imlecin uzerinde durmasiyla
 * acilir; dokunmatikte boyle bir olay yok. Bu yuzden tooltip DOKUNUNCA
 * aciliyor ve tekrar dokununca ya da disina dokununca kapaniyor.
 *
 * ⚠️ TOOLTIP ZORUNLU BILGI TASIYAMAZ. Acilmasi kullanicinin bir eylemine
 * bagli; ekranin anlasilmasi icin gereken hicbir sey buraya konmaz —
 * yalniz "bu rakam nereden geliyor" turu ek aciklamalar.
 *
 * Metin ekran okuyucuya `contentDescription` ile de veriliyor: gorunur
 * balonu hic acmayan kullanici da bilgiye erisebilsin.
 */
@Composable
fun LkTooltip(
    metin: String,
    modifier: Modifier = Modifier,
    icerik: @Composable () -> Unit
) {
    var acik by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedVisibility(
                visible = acik,
                enter = fadeIn(tween(140)),
                exit = fadeOut(tween(140))
            ) {
                Column {
                    Text(
                        text = metin,
                        style = LkTypography.getMetadata(),
                        color = LkOnStage,
                        modifier = Modifier
                            .widthIn(max = 260.dp)
                            .lkElevation(LkElevation.MD, LkShapes.SM)
                            .clip(LkShapes.SM)
                            .background(LkSurfaceStage)
                            .padding(horizontal = LkSpacing.Space3, vertical = LkSpacing.Space2)
                    )
                    Spacer(Modifier.height(LkSpacing.Space1))
                }
            }

            Box(
                modifier = Modifier
                    .clickable { acik = !acik }
                    .semantics { contentDescription = metin }
            ) { icerik() }
        }
    }
}
