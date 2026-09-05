package com.localkarar.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.LkElevation
import com.localkarar.app.ui.theme.LkLineSoft
import com.localkarar.app.ui.theme.LkSurfaceTile
import com.localkarar.app.ui.theme.LkTileInk
import com.localkarar.app.ui.theme.LkShapes
import com.localkarar.app.ui.theme.LkSpacing
import com.localkarar.app.ui.theme.LkSurfaceRaised
import com.localkarar.app.ui.theme.LkTextMuted
import com.localkarar.app.ui.theme.LkTextPrimary
import com.localkarar.app.ui.theme.LkTextSecondary
import com.localkarar.app.ui.theme.LkTypography
import com.localkarar.app.ui.theme.LkTypography.numeric
import com.localkarar.app.ui.theme.lkElevation
import com.localkarar.app.ui.theme.lkIsDark
import com.localkarar.app.ui.theme.isReducedMotionEnabled

/*
 * §24 BILESEN KATMANI — mockup'taki desenlerin Kotlin karsiliklari.
 *
 * Mockup HTML; buradan alinan sey OLCU, RITIM ve DESEN. Kod degil.
 */


// ──────────────────────────────────────────────────────────────────
// BASINCA OLCEKLENEN SARMALAYICI
// ──────────────────────────────────────────────────────────────────

/*
 * ⚠️ LkTactileAction BUNUN YERINE KULLANILAMAZ: o kendi ikonu ve etiketi
 * olan tam bir bilesen, sarmalayici degil. Once oyle varsayildi ve derleme
 * hatasi verdi — imzayi okumadan varsaymanin bedeli.
 *
 * Olcek 0.97, sure 120ms (§24.8 "instant"). Hareket kisitliyken olcek
 * hic degismez.
 */
@Composable
fun LkPressable(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val kaynak = remember { MutableInteractionSource() }
    val basili by kaynak.collectIsPressedAsState()
    val kisitli = isReducedMotionEnabled()
    val olcek by animateFloatAsState(
        targetValue = if (basili && !kisitli) 0.97f else 1f,
        animationSpec = tween(120),
        label = "press"
    )
    Box(
        modifier = modifier
            .scale(olcek)
            .clickable(interactionSource = kaynak, indication = null, onClick = onClick)
    ) { content() }
}

// ──────────────────────────────────────────────────────────────────
// KART — §24.3 (20dp) + §24.4 (20dp dolgu) + §24.5 (tema duyarli derinlik)
// ──────────────────────────────────────────────────────────────────

/**
 * Standart kart.
 *
 * ⚠️ Icinde NEGATIF bosluk kullanilmaz. Kaydirilabilir bir kabin icindeyken
 * negatif ust bosluk kartin ustunu kirptirir — mockup'ta bu tuzaga dusuldu.
 */
@Composable
fun LkCard(
    modifier: Modifier = Modifier,
    elevation: Dp = LkElevation.SM,
    padding: Dp = LkSpacing.PadCard,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .lkElevation(elevation, LkShapes.Card)
            .clip(LkShapes.Card)
            .background(LkSurfaceRaised)
            .padding(padding),
        content = content
    )
}

// ──────────────────────────────────────────────────────────────────
// METRIK — ekranin tek hakim sayisi, sayarak yukselir
// ──────────────────────────────────────────────────────────────────

/**
 * §24.2 `display` olceginde tek hakim rakam + sayac animasyonu.
 *
 * 🔴 IKI TUZAK:
 *
 * 1. TABULAR FIGURLER SART. Orantili rakamlarla her karede basamak
 *    genislikleri degisir ve sayi saga sola ziplar. `numeric()` bunu cozer;
 *    Manrope'de `tnum` ozelliginin bulundugu font dosyasi okunarak dogrulandi.
 *
 * 2. HER YENIDEN BILESIMDE BASTAN SAYMAZ. `LaunchedEffect` anahtari
 *    `hedef` — yalniz deger GERCEKTEN degistiginde calisir. Anahtar
 *    `Unit` olsaydi her dokunusta ekran titrerdi.
 *
 * Hareket kisitlamasi acikken animasyon tamamen atlanir (§24.8).
 */
@Composable
fun LkMetric(
    hedef: Double,
    bicimle: (Double) -> String,
    modifier: Modifier = Modifier,
    etiket: String? = null,
    renk: Color = LkTextPrimary
) {
    val kisitli = isReducedMotionEnabled()
    val anim = remember { Animatable(if (kisitli) hedef.toFloat() else 0f) }

    LaunchedEffect(hedef, kisitli) {
        if (kisitli) {
            anim.snapTo(hedef.toFloat())
        } else {
            anim.snapTo(0f)
            anim.animateTo(hedef.toFloat(), tween(480))
        }
    }

    Column(modifier) {
        if (etiket != null) {
            Text(
                text = etiket,
                style = LkTypography.getLabelM(),
                color = LkTextSecondary
            )
            Box(Modifier.height(LkSpacing.Space2))
        }
        Text(
            text = bicimle(anim.value.toDouble()),
            style = LkTypography.getDisplayXL().numeric(),
            color = renk,
            maxLines = 1
        )
    }
}

// ──────────────────────────────────────────────────────────────────
// IKON DOSEMESI — hizli islem izgarasi
// ──────────────────────────────────────────────────────────────────

/** 20dp yaricapli, iki tonlu zeminli ikon kutucugu. */
@Composable
fun LkIconTile(
    etiket: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    ikon: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)
    ) {
        LkPressable(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(LkSurfaceTile),
                contentAlignment = Alignment.Center
            ) { ikon() }
        }
        Text(
            text = etiket,
            style = LkTypography.getMetadata(),
            color = LkTextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ──────────────────────────────────────────────────────────────────
// LISTE SATIRI — ikon | baslik + zaman | kategori | tutar
// ──────────────────────────────────────────────────────────────────

/**
 * Referans kitlerin satir anatomisi.
 *
 * Kategori sutunu DIKEY ayraclarla ayrilir, yatay cizgiyle degil; yatay
 * cizgi satirlari birbirinden ayirir, dikey ayrac satir ICINDEKI alanlari.
 */
@Composable
fun LkListRow(
    baslik: String,
    modifier: Modifier = Modifier,
    altBaslik: String? = null,
    kategori: String? = null,
    tutar: String? = null,
    tutarRengi: Color = LkTextPrimary,
    onClick: (() -> Unit)? = null,
    ikon: (@Composable () -> Unit)? = null,
    sag: (@Composable RowScope.() -> Unit)? = null
) {
    val icerik: @Composable RowScope.() -> Unit = {
        if (ikon != null) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(LkSurfaceTile),
                contentAlignment = Alignment.Center
            ) { ikon() }
            Box(Modifier.width(LkSpacing.Space3))
        }

        Column(Modifier.weight(1f)) {
            Text(
                text = baslik,
                style = LkTypography.getBodyStrong(),
                color = LkTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (altBaslik != null) {
                Text(
                    text = altBaslik,
                    style = LkTypography.getMetadata(),
                    color = LkTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (kategori != null) {
            Box(
                Modifier
                    .padding(horizontal = LkSpacing.Space3)
                    .width(1.dp)
                    .height(28.dp)
                    .background(LkLineSoft)
            )
            Text(
                text = kategori,
                style = LkTypography.getMetadata(),
                color = LkTextSecondary,
                maxLines = 1
            )
            Box(
                Modifier
                    .padding(start = LkSpacing.Space3)
                    .width(1.dp)
                    .height(28.dp)
                    .background(LkLineSoft)
            )
        }

        if (tutar != null) {
            Text(
                text = tutar,
                style = LkTypography.getBodyStrong().numeric(),
                color = tutarRengi,
                maxLines = 1,
                modifier = Modifier.padding(start = LkSpacing.Space3)
            )
        }

        sag?.invoke(this)
    }

    if (onClick != null) {
        LkPressable(onClick = onClick, modifier = modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space3),
                verticalAlignment = Alignment.CenterVertically,
                content = icerik
            )
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space3),
            verticalAlignment = Alignment.CenterVertically,
            content = icerik
        )
    }
}

/** Satir grubunu tek yuzeyde toplayan kap. */
@Composable
fun LkRowGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .lkElevation(LkElevation.SM, LkShapes.Card)
            .clip(LkShapes.Card)
            .background(LkSurfaceRaised),
        content = content
    )
}

// ──────────────────────────────────────────────────────────────────
// SKELETON — §11'de tanimliydi, hic yazilmamisti
// ──────────────────────────────────────────────────────────────────

/**
 * Parlayan yer tutucu.
 *
 * Donen bir halka yerine icerigin SEKLINI alan kutucuklar; bekleme suresi
 * kullaniciya daha kisa hissettiriyor.
 *
 * Hareket kisitlamasi acikken parlama durur, duz yuzey kalir (§24.8).
 */
@Composable
fun LkSkeleton(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp)
) {
    val kisitli = isReducedMotionEnabled()
    val koyu = lkIsDark()
    val taban = LkLineSoft
    val parlak = if (koyu) Color(0x14FFFFFF) else Color(0x8CFFFFFF)

    if (kisitli) {
        Box(modifier.clip(shape).background(taban))
        return
    }

    val gecis = rememberInfiniteTransition(label = "skeleton")
    val x by gecis.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1350, easing = LinearEasing)),
        label = "shimmer"
    )

    Box(
        modifier
            .clip(shape)
            .background(taban)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color.Transparent, parlak, Color.Transparent),
                    start = Offset(x * 320f, 0f),
                    end = Offset((x + 1f) * 320f, 0f)
                )
            )
    )
}

/** Liste yuklenirken gosterilen uc satirlik yer tutucu. */
@Composable
fun LkSkeletonRows(satir: Int = 3, modifier: Modifier = Modifier) {
    LkRowGroup(modifier) {
        repeat(satir) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space3),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LkSkeleton(Modifier.size(44.dp), RoundedCornerShape(15.dp))
                Box(Modifier.width(LkSpacing.Space3))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    LkSkeleton(Modifier.fillMaxWidth(0.62f).height(11.dp))
                    LkSkeleton(Modifier.fillMaxWidth(0.38f).height(11.dp))
                }
                LkSkeleton(Modifier.width(52.dp).height(11.dp))
            }
        }
    }
}
