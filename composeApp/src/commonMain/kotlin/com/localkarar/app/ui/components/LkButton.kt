package com.localkarar.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.*

/*
 * BUTON SOZLESMESI — `DESIGN.md` §6.
 *
 * 🔴 ONCEDEN TEK BOYUT VARDI. Buton hep 40dp; §6.1'in `sm` (32) ve `lg` (48)
 * kademeleri yoktu, basma geri bildirimi yoktu, ikon destegi yoktu. Ekranlar
 * ihtiyac duyduklarinda kendi Surface+Text kombinasyonlarini kuruyordu --
 * §0'in "sayfa kendi buton olcusunu olusturamaz" kuralinin ihlali.
 *
 * §6.1 olculeri (md VARSAYILAN):
 *   sm  32dp / yatay 12dp / radius-sm / label 12sp  / ikon 14dp / bosluk 6dp
 *   md  40dp / yatay 20dp / radius-sm / body-sm 13  / ikon 16dp / bosluk 8dp
 *   lg  48dp / yatay 24dp / radius-md / body 14sp   / ikon 18dp / bosluk 8dp
 *
 * §6.1 kisiti: `lg` YALNIZ hero CTA, onboarding ve tek onemli primary action
 * icin. "Normal sayfalarda dev buton yok."
 */

enum class LkButtonVariant {
    /** §6.2 — tek ana eylem. Koyu modda da solid brand-500 (§8.1). */
    PRIMARY,
    /** §6.2 — ikincil eylem. */
    SECONDARY,
    /** §6.2 — ucuncul / nav ici eylem. */
    GHOST,
    /** §6.2 — yikici islem. */
    DANGER,
    /** §6.2 — dusuk onem eylem. */
    QUIET
}

/** §6.1 boyut kademeleri. `MD` varsayilandir. */
enum class LkButtonSize(
    val height: Dp,
    val horizontalPadding: Dp,
    val iconSize: Dp,
    val gap: Dp
) {
    SM(32.dp, 12.dp, 14.dp, 6.dp),
    MD(40.dp, 20.dp, 16.dp, 8.dp),
    LG(48.dp, 24.dp, 18.dp, 8.dp)
}

@Composable
fun LkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: LkButtonVariant = LkButtonVariant.PRIMARY,
    size: LkButtonSize = LkButtonSize.MD,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val etkilesim = remember { MutableInteractionSource() }
    val basili by etkilesim.collectIsPressedAsState()

    // §12 `fast`: basma geri bildirimi 120-150ms, tek standart easing.
    val olcek by animateFloatAsState(
        targetValue = if (basili && enabled) 0.97f else 1f,
        // §12: hareket kisitliyken ani gecis.
        animationSpec = lkAnim(LkMotion.fast())
    )

    val zemin = when (variant) {
        // §8.1: primary CTA her iki modda solid brand-500 -- tonal yuzeye
        // donusturulmez. Bu yuzden `primaryFill`, `primary` degil.
        LkButtonVariant.PRIMARY -> LkPrimaryFill
        LkButtonVariant.SECONDARY -> LkSurfaceHighlight
        LkButtonVariant.GHOST -> Color.Transparent
        LkButtonVariant.DANGER -> LkDanger
        LkButtonVariant.QUIET -> LkSurfacePanel
    }

    val icerik = when (variant) {
        LkButtonVariant.PRIMARY -> LkOnPrimary
        LkButtonVariant.SECONDARY -> LkTextPrimary
        LkButtonVariant.GHOST -> LkPrimary
        LkButtonVariant.DANGER -> Color.White
        LkButtonVariant.QUIET -> LkTextSecondary
    }

    val kenar = when (variant) {
        LkButtonVariant.SECONDARY -> BorderStroke(1.dp, LkLineStrong)
        else -> null
    }

    // §6.1: sm → label, md → body-sm, lg → body.
    val yaziStili: TextStyle = when (size) {
        LkButtonSize.SM -> LkTypography.getLabel()
        LkButtonSize.MD -> LkTypography.getBodySmall()
        LkButtonSize.LG -> LkTypography.getBody()
    }.copy(color = icerik, fontWeight = androidx.compose.ui.text.font.FontWeight.W600)

    Button(
        onClick = onClick,
        // §3.2 `shadow-sm` — buton zeminden AYRILMALI.
        //
        // 🔴 ONCEDEN HER BUTON `elevation = 0` IDI. §6.2 "normal state'te
        // BUYUK golge yok" diyor; sifir golge demiyor. Ozellikle SECONDARY
        // (beyaz zemin, #EDF0F2 canvas uzerinde) tamamen duz duruyordu --
        // sayfaya ait olmayan bir dikdortgen gibi.
        //
        // GHOST haric: seffaf zeminli bir butonun golgesi, olmayan bir
        // yuzeyin golgesi olurdu.
        modifier = modifier
            .then(
                if (variant == LkButtonVariant.GHOST) Modifier
                else Modifier.lkShadow(
                    LkElevation.SM,
                    if (size == LkButtonSize.LG) LkShapes.MD else LkShapes.SM
                )
            )
            .height(size.height)
            .scale(olcek),
        enabled = enabled,
        interactionSource = etkilesim,
        // §6.1: sm/md radius-sm, lg radius-md.
        shape = if (size == LkButtonSize.LG) LkShapes.MD else LkShapes.SM,
        border = kenar,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = zemin,
            contentColor = icerik,
            // §6.2 disabled: opaklik 0.5 + golgesiz.
            disabledBackgroundColor = zemin.copy(alpha = 0.5f),
            disabledContentColor = icerik.copy(alpha = 0.5f)
        ),
        // §6.2: normal state'te buyuk golge yok; glow yalniz focus/active.
        elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
        contentPadding = PaddingValues(horizontal = size.horizontalPadding)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = icerik,
                modifier = Modifier.size(size.iconSize)
            )
            Spacer(Modifier.width(size.gap))
        }
        Text(text = text, style = yaziStili)
    }
}
