package com.localkarar.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.LkElevation
import com.localkarar.app.ui.theme.LkLineSoft
import com.localkarar.app.ui.theme.LkOnPrimary
import com.localkarar.app.ui.theme.LkPrimary
import com.localkarar.app.ui.theme.LkPrimaryFill
import com.localkarar.app.ui.theme.LkPrimarySoft
import com.localkarar.app.ui.theme.LkShapes
import com.localkarar.app.ui.theme.LkSpacing
import com.localkarar.app.ui.theme.LkSurfaceElevated
import com.localkarar.app.ui.theme.LkSurfacePanel
import com.localkarar.app.ui.theme.LkSurfaceTile
import com.localkarar.app.ui.theme.LkTextMuted
import com.localkarar.app.ui.theme.LkTextPrimary
import com.localkarar.app.ui.theme.LkTextSecondary
import com.localkarar.app.ui.theme.LkTileInk
import com.localkarar.app.ui.theme.LkTypography
import com.localkarar.app.ui.theme.lkShadow

/*
 * MENU CEKMECESI — Urun Merkezi ve Isletme Bolumleri icin ORTAK.
 *
 * 🔴 IKI MENU DE "TASARLANMAMIS" DURUYORDU. Once kart izgarasiydi (urun
 * sahibi begenmedi), sonra duz gruplanmis listeye alindi — o da fazla
 * sade kaldi: on bir satir alt alta, hepsi ayni agirlikta, secili olan
 * yalnizca ince bir kenarlikla belli oluyordu.
 *
 * Bu bilesenin uc isi var:
 *   1. HIYERARSI — grup basligi + cizgi, iki sutunlu kutucuklar, en cok
 *      kullanilan giris istenirse tepede tam genislikte one cikan blok.
 *   2. DERINLIK — kutucuklar yuzeyden yukselir (`lkShadow`), basilinca
 *      ICERI cokerler (golge kalkar + kucuk olcek). Urun sahibinin
 *      "sadece isik goruyorum, derinlik istiyorum" dedigi sey bu.
 *   3. SECILI DURUM — secili kutucuk hem daha KOYU golgeli hem cerceveli
 *      hem de tik rozetli: durum yalniz renge yaslanmiyor (§19).
 */

data class LkMenuOgesi(
    val id: String,
    val baslik: String,
    val aciklama: String? = null,
    val ikon: ImageVector,
    val onClick: () -> Unit
)

data class LkMenuGrubu(
    val baslik: String,
    val ogeler: List<LkMenuOgesi>
)

@Composable
fun LkMenuSheet(
    baslik: String,
    gruplar: List<LkMenuGrubu>,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    altBaslik: String? = null,
    seciliId: String? = null,
    /** Tepede tam genislikte one cikan giris (ornegin acik bolum). */
    oneCikan: (@Composable () -> Unit)? = null,
    /** En altta duran ikincil eylem ("İşletme değiştir" gibi). */
    altEylem: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(LkSurfaceElevated)
            .verticalScroll(rememberScrollState())
            .padding(bottom = LkSpacing.Space8)
    ) {
        LkSheetHandle()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = LkSpacing.Space5,
                    end = LkSpacing.Space3,
                    top = LkSpacing.Space2,
                    bottom = LkSpacing.Space4
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = baslik,
                    style = LkTypography.getTitleS(),
                    color = LkTextPrimary
                )
                if (!altBaslik.isNullOrBlank()) {
                    Text(
                        text = altBaslik,
                        style = LkTypography.getMetadata(),
                        color = LkTextSecondary
                    )
                }
            }
            /* Kapat dugmesi kendi tonal dairesinde: ciplak ikon 44dp
               hedefi doldurmuyordu. */
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(LkSurfaceTile)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "Kapat",
                    tint = LkTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (oneCikan != null) {
            Box(Modifier.padding(horizontal = LkSpacing.Space5, vertical = LkSpacing.Space1)) {
                oneCikan()
            }
            Spacer(Modifier.height(LkSpacing.Space4))
        }

        gruplar.forEach { grup ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = LkSpacing.Space5,
                        end = LkSpacing.Space5,
                        top = LkSpacing.Space2,
                        bottom = LkSpacing.Space2
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = grup.baslik,
                    style = LkTypography.getMicro(),
                    color = LkTextSecondary
                )
                /* Baslik cizgisi grubu GORSEL olarak kapatiyor; on bir
                   satir arasinda nerede oldugun buradan okunuyor. */
                Box(
                    Modifier
                        .padding(start = LkSpacing.Space3)
                        .weight(1f)
                        .height(1.dp)
                        .background(LkLineSoft)
                )
            }

            grup.ogeler.chunked(2).forEach { satir ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = LkSpacing.Space5,
                            vertical = LkSpacing.Space1
                        ),
                    horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
                ) {
                    satir.forEach { oge ->
                        MenuKutucugu(
                            oge = oge,
                            secili = oge.id == seciliId,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    /* Tek kalan oge satiri germesin: yariya kadar dursun. */
                    if (satir.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(LkSpacing.Space3))
        }

        if (altEylem != null) {
            Box(
                Modifier.padding(
                    start = LkSpacing.Space5,
                    end = LkSpacing.Space5,
                    top = LkSpacing.Space2
                )
            ) {
                altEylem()
            }
        }
    }
}

/**
 * Menu kutucugu.
 *
 * DERINLIK BURADA: durgunken yuzeyden 1 kademe yukselir, basilinca golge
 * kalkar ve kutucuk kuculur — parmagin altinda ICERI coker. Secili
 * kutucuk bir kademe DAHA yukselir ve cercevelenir.
 */
@Composable
private fun MenuKutucugu(
    oge: LkMenuOgesi,
    secili: Boolean,
    modifier: Modifier = Modifier
) {
    val etkilesim = remember { MutableInteractionSource() }
    val basili by etkilesim.collectIsPressedAsState()

    val olcek by animateFloatAsState(if (basili) 0.96f else 1f)

    Column(
        modifier = modifier
            .scale(olcek)
            .lkShadow(
                when {
                    basili -> 0.dp
                    secili -> LkElevation.MD
                    else -> LkElevation.SM
                },
                LkShapes.MD
            )
            .clip(LkShapes.MD)
            .background(if (secili) LkPrimarySoft else LkSurfacePanel)
            .then(
                if (secili) Modifier.border(1.5.dp, LkPrimary, LkShapes.MD)
                else Modifier.border(1.dp, LkLineSoft, LkShapes.MD)
            )
            .clickable(
                interactionSource = etkilesim,
                indication = null,
                onClick = oge.onClick
            )
            .heightIn(min = 108.dp)
            .padding(LkSpacing.Space3),
        verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (secili) LkPrimaryFill else LkSurfaceTile),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = oge.ikon,
                    contentDescription = null,
                    tint = if (secili) LkOnPrimary else LkTileInk,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            if (secili) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(LkPrimaryFill),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Check,
                        contentDescription = "Açık bölüm",
                        tint = LkOnPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        Text(
            text = oge.baslik,
            style = LkTypography.getBodySmall(),
            color = if (secili) LkPrimary else LkTextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (!oge.aciklama.isNullOrBlank()) {
            Text(
                text = oge.aciklama,
                style = LkTypography.getMicro(),
                color = LkTextMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

}
