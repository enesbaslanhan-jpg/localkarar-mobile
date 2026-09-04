package com.localkarar.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localkarar.app.ui.theme.*

/*
 * ORTAK YUZEY DILI — ONAYLANAN MOBIL PROTOTIPTEN.
 *
 * 🔴 ILK UYARLAMADA TEMEL BIR HATA YAPILDI: her bolum karta alinmisti.
 *
 * Prototipte `.section-block` bir kart DEGIL:
 *     .section-block { display:flex; flex-direction:column; gap:16px; }
 * Ne zemini, ne kenarligi, ne kose yaricapi var. Sayfa acik bloklardan
 * olusuyor ve bloklari BOSLUK ayiriyor (`.page-view { gap:24px }`), cerceve
 * degil. Kart zemini (`--surface-card`) yalniz KUCUK KONTROLLERDE kullaniliyor:
 * `.pill-chip`, `.icon-circle-btn`, `.form-control`, `.tactile-icon-box`.
 *
 * Her bolumu cerceveye almak butun bloklari esit agirliga dusuruyor ve
 * hiyerarsiyi yok ediyordu. `LkSection` dogru olan: cerceve yok, ritim
 * boslukla kuruluyor.
 */

/**
 * Acik bolum — prototipteki `.section-block`.
 *
 * Cerceve YOK. Baslik satiri ile icerik arasi 16dp; bolumler arasi 24dp
 * bosluk cagiran tarafta verilir (`Arrangement.spacedBy(LkSpacing.Space6)`).
 */
@Composable
fun LkSection(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = LkTypography.getSectionTitle(),
                color = LkTextPrimary,
                fontWeight = FontWeight.Bold
            )
            when {
                trailing != null -> trailing()
                actionLabel != null && onAction != null -> Text(
                    text = actionLabel,
                    style = LkTypography.getBodySmall(),
                    color = LkPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onAction)
                )
            }
        }
        content()
    }
}

/**
 * Sac teli ayirici — prototipteki `--border-hairline`.
 *
 * Satirlari AYIRIR, cerceveye almaz. Listenin son satirindan sonra cizilmez
 * (`.task-row:last-child { border-bottom: none }`).
 */
@Composable
fun LkHairline(modifier: Modifier = Modifier) {
    Divider(color = LkLineSoft, thickness = 1.dp, modifier = modifier)
}

/**
 * KART SEVIYELERI — `DESIGN.md` §10.
 *
 * §10 UC seviye tanimliyor ve "baska kart tasarimi yok" diyor:
 *
 *   compact   dolgu 12dp / radius-md / ic bosluk  8dp  — yogun listeler
 *   standard  dolgu 16dp / radius-md / ic bosluk 12dp  — VARSAYILAN
 *   feature   dolgu 24dp / radius-lg / ic bosluk 16dp  — hero, akis basi
 *
 * ⚠️ Bunlar BOLUM icin degil. Bolumler `LkSection` ile cerceve almadan
 * cizilir (dosyanin basindaki nota bak). Kart, kendi basina duran bir nesne
 * icindir: takvim gunu, urun karti, one cikan icerik.
 *
 * §10: "Kart tipik olarak shadow-sm + hover shadow-md; surekli glow yok."
 * Mobilde hover yok, bu yuzden yalniz `shadow-sm`.
 */
enum class LkCardLevel(
    val padding: androidx.compose.ui.unit.Dp,
    val gap: androidx.compose.ui.unit.Dp
) {
    COMPACT(12.dp, 8.dp),
    STANDARD(16.dp, 12.dp),
    FEATURE(24.dp, 16.dp)
}

@Composable
fun LkSectionCard(
    modifier: Modifier = Modifier,
    level: LkCardLevel = LkCardLevel.STANDARD,
    content: @Composable ColumnScope.() -> Unit
) {
    // §10: feature radius-lg (16dp), digerleri radius-md (12dp).
    val bicim = if (level == LkCardLevel.FEATURE) LkShapes.LG else LkShapes.MD

    Surface(
        color = LkSurfaceRaised,
        shape = bicim,
        elevation = 0.dp,
        border = BorderStroke(1.dp, LkLineStrong),
        // §3.1 oncelik: surface contrast → border → subtle shadow.
        // Golge ayrimi KURAN sey degil, PEKISTIREN sey.
        modifier = modifier.fillMaxWidth().lkShadow(LkElevation.SM, bicim)
    ) {
        Column(
            modifier = Modifier.padding(level.padding),
            verticalArrangement = Arrangement.spacedBy(level.gap),
            content = content
        )
    }
}

/**
 * Bolum basligi — geriye donuk uyumluluk icin duruyor.
 *
 * Yeni ekranlarda `LkSection` tercih edilmeli; bu yalniz basligi cizer.
 */
@Composable
fun LkSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = LkTypography.getSectionTitle(),
            color = LkTextPrimary,
            fontWeight = FontWeight.Bold
        )
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                style = LkTypography.getBodySmall(),
                color = LkPrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onAction)
            )
        }
    }
}

/**
 * Dokunsal eylem dosemesi — prototipteki `.tactile-action-btn`.
 *
 * "Dokunsal" adi bos degil: basildiginda kuculuyor
 * (`.tactile-action-btn:active { transform: scale(0.92) }`) ve ikon kutusunun
 * kenarligi marka rengine geciyor (`:hover .tactile-icon-box`). Ilk
 * uyarlamada ikisi de yoktu; doseme dokunuldugunda hicbir sey yapmiyordu.
 *
 * Olculer prototipten: kutu 44dp, yaricap 12dp, zemin `--surface-subtle`.
 * Dokunma hedefi etiketle birlikte ~68dp -- erisilebilirlik esigi asiliyor.
 */
@Composable
fun LkTactileAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val etkilesim = remember { MutableInteractionSource() }
    val basili by etkilesim.collectIsPressedAsState()
    val olcek by animateFloatAsState(
        targetValue = if (basili) 0.92f else 1f,
        animationSpec = lkAnim(LkMotion.fast())
    )

    Column(
        modifier = modifier
            .clip(LkShapes.MD)
            .clickable(
                interactionSource = etkilesim,
                indication = null,
                onClick = onClick
            )
            .scale(olcek),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(LkSurfaceSunken, LkShapes.MD)
                .border(1.dp, if (basili) LkPrimary else LkLineSoft, LkShapes.MD),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (basili) LkPrimary else LkTextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.height(LkSpacing.Space2))
        // Dar sutunda tasmasin: ortalanir ve gerekirse iki satira iner.
        // Kirpmak ("Yeni Tahsilaᵗ") etiketi okunmaz yapiyordu.
        Text(
            text = label,
            style = LkTypography.getMicro(),
            color = LkTextSecondary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Filtre hapi — prototipteki `.pill-chip`.
 *
 * Bu GERCEKTEN kart zeminli: prototipte `.pill-chip` arka plani
 * `--surface-card`. Secili haldeyken marka rengine geciyor.
 */
@Composable
fun LkPillChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 🔴 KONTRAST HATASI DUZELTILDI. Secili hap `LkPrimary` zemin + beyaz
    // yaziyla ciziliyordu; koyu temada `LkPrimary` = brand-300 (#7BA2B3) ve
    // uzerine beyaz 1.9:1 veriyor -- §19'un 4.5:1 esiginin cok altinda.
    // `primaryFill` (brand-500) beyazla her iki modda 4.6:1 (§8.1 zaten
    // secili/primary yuzeyler icin solid brand-500 istiyor).
    val zemin: Color = if (selected) LkPrimaryFill else LkSurfaceRaised
    val yazi: Color = if (selected) LkOnPrimary else LkTextSecondary
    val kenar: Color = if (selected) LkPrimaryFill else LkLineStrong

    // §11 Chip = `control-sm` (32dp) gorsel yukseklik.
    // §19 + §6.3: dokunma hedefi mobilde en az 44dp -- gorsel olcuyu
    // buyutmeden `sizeIn` ile saglaniyor.
    Box(
        modifier = modifier
            .heightIn(min = 44.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
            .height(32.dp)
            .clip(LkShapes.FULL)
            .background(zemin, LkShapes.FULL)
            .border(1.dp, kenar, LkShapes.FULL)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = LkTypography.getBodySmall(),
            color = yazi,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Nabizli donem rozeti — prototipteki `.live-pulse-badge` + `.pulse-dot`.
 *
 * Nokta yanip sonuyor (`animation: uiverse-pulse 1.8s infinite`). Renk
 * prototipte MARKA DEGIL, olumlu yesil (`--positive-green`); ilk uyarlamada
 * yanlislikla marka rengi kullanilmisti.
 *
 * ⚠️ Rozet canli bir baglantiyi DEGIL, kapsanan donemi anlatir.
 */
@Composable
fun LkPulseBadge(label: String, modifier: Modifier = Modifier) {
    // §12: susleme amacli sonsuz animasyon kisitlamada YAVASLATILMAZ,
    // tamamen durur -- hizli oynatmak kisitlamanin amacina aykiri.
    val hareketVar = lkAllowDecorativeMotion()
    val gecis = rememberInfiniteTransition(label = "nabiz")
    val nabiz by gecis.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "nabizSaydamlik"
    )
    val saydamlik = if (hareketVar) nabiz else 1f

    Row(
        modifier = modifier
            .background(LkSuccess.copy(alpha = 0.14f), LkShapes.FULL)
            .padding(horizontal = 9.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .alpha(saydamlik)
                .background(LkSuccess)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label.uppercase(),
            style = LkTypography.getMicro(),
            color = LkSuccess,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
