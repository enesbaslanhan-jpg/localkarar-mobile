package com.localkarar.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.LkDanger
import com.localkarar.app.ui.theme.LkElevation
import com.localkarar.app.ui.theme.LkOnStage
import com.localkarar.app.ui.theme.LkShapes
import com.localkarar.app.ui.theme.LkSpacing
import com.localkarar.app.ui.theme.LkSuccess
import com.localkarar.app.ui.theme.LkSurfaceStage
import com.localkarar.app.ui.theme.LkTypography
import com.localkarar.app.ui.theme.isReducedMotionEnabled
import com.localkarar.app.ui.theme.lkElevation
import kotlinx.coroutines.delay

/** Bildirimin tonu. Renk TEK basina tasimiyor; ikon da degisiyor (§19). */
enum class LkToastTonu { BILGI, BASARI, HATA }

/**
 * §11 `Toast` — devir belgesi §6'da "hala yok" diye duran iki bilesenden biri.
 *
 * ⚠️ `LkNotice` ILE KARISTIRILMAZ:
 *   - `LkNotice` akisin ICINDE durur, yer kaplar, kullanici kapatana kadar
 *     kalir — form sonucu gibi kaliciligi olan mesajlar icin.
 *   - `LkToast` ekranin USTUNDE yuzer, kendi kendine kaybolur ve yer
 *     kaplamaz — "kopyalandi", "kaydedildi" gibi gecici bildirimler icin.
 *
 * Hareket kisitliyken kayma yok, yalnizca beliriyor (§24.8). Sure de
 * kisitli modda uzuyor: animasyonu kapatan kullanicinin okuma hizi da
 * genelde farkli.
 */
@Composable
fun BoxScope.LkToast(
    metin: String,
    gorunur: Boolean,
    onKapandi: () -> Unit,
    modifier: Modifier = Modifier,
    ton: LkToastTonu = LkToastTonu.BILGI,
    eylemMetni: String? = null,
    onEylem: (() -> Unit)? = null,
    sureMs: Long = 3200
) {
    val kisitli = isReducedMotionEnabled()

    LaunchedEffect(gorunur, metin) {
        if (gorunur) {
            delay(if (kisitli) sureMs + 1500 else sureMs)
            onKapandi()
        }
    }

    AnimatedVisibility(
        visible = gorunur,
        enter = if (kisitli) fadeIn(tween(120))
            else fadeIn(tween(180)) + slideInVertically(tween(220)) { it / 2 },
        exit = if (kisitli) fadeOut(tween(120))
            else fadeOut(tween(180)) + slideOutVertically(tween(220)) { it / 2 },
        modifier = modifier
            .align(Alignment.BottomCenter)
            .padding(LkSpacing.Space4)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .lkElevation(LkElevation.OVERLAY, LkShapes.MD)
                .clip(LkShapes.MD)
                .background(LkSurfaceStage)
                .padding(
                    start = LkSpacing.Space4,
                    end = if (eylemMetni != null) LkSpacing.Space2 else LkSpacing.Space4,
                    top = LkSpacing.Space3,
                    bottom = LkSpacing.Space3
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = when (ton) {
                    LkToastTonu.BASARI -> Icons.Outlined.CheckCircle
                    LkToastTonu.HATA -> Icons.Outlined.ErrorOutline
                    LkToastTonu.BILGI -> Icons.Outlined.Info
                },
                contentDescription = null,
                tint = when (ton) {
                    LkToastTonu.BASARI -> LkSuccess
                    LkToastTonu.HATA -> LkDanger
                    LkToastTonu.BILGI -> LkOnStage
                },
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(LkSpacing.Space3))
            Text(
                text = metin,
                style = LkTypography.getBodySmall(),
                color = LkOnStage,
                modifier = Modifier.weight(1f)
            )
            if (eylemMetni != null && onEylem != null) {
                TextButton(onClick = onEylem) {
                    Text(eylemMetni, style = LkTypography.getLabel(), color = LkOnStage)
                }
            }
        }
    }
}

/**
 * Toast kabini: ekranin en ustunde tek bir yerde durur.
 *
 * Toast'un `BoxScope` istemesinin sebebi bu — her ekran kendi kosesine
 * toast koymasin diye hizalama kabin isi.
 */
@Composable
fun LkToastHost(
    metin: String?,
    onKapandi: () -> Unit,
    modifier: Modifier = Modifier,
    ton: LkToastTonu = LkToastTonu.BILGI
) {
    Box(modifier = modifier.fillMaxWidth()) {
        LkToast(
            metin = metin.orEmpty(),
            gorunur = metin != null,
            onKapandi = onKapandi,
            ton = ton
        )
    }
}
