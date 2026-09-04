package com.localkarar.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.*

/*
 * SEKMELER — `DESIGN.md` §11.
 *
 * §11: Tabs — underline (varsayilan) / segmented, satir `40px`.
 *
 * 🔴 ORTAK BIR SEKME BILESENI YOKTU. Alt bolum gecisi gereken ekranlar
 * (Isletme Takibi, Ayarlar, Topluluk) kendi hap seritlerini ya da Row +
 * Text kombinasyonlarini kuruyordu.
 *
 * ⚠️ Sekme SAYFA DEGISTIRMEZ, ayni ekranin bolumleri arasinda gecer.
 * Ekranlar arasi gecis alt dock'un isi.
 */

enum class LkTabStyle {
    /** §11 varsayilani — altta marka rengi cizgi. */
    UNDERLINE,
    /** §11 alternatif — dolu segment; dar alanda 2-3 secenek icin. */
    SEGMENTED
}

/**
 * @param tabs Sekme etiketleri.
 * @param selectedIndex Secili sekmenin sirasi.
 */
@Composable
fun LkTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    style: LkTabStyle = LkTabStyle.UNDERLINE
) {
    when (style) {
        LkTabStyle.UNDERLINE -> UnderlineTabs(tabs, selectedIndex, onSelect, modifier)
        LkTabStyle.SEGMENTED -> SegmentedTabs(tabs, selectedIndex, onSelect, modifier)
    }
}

@Composable
private fun UnderlineTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier
) {
    // Yatay kaydirma: dort ve uzeri sekme 360dp genislige sigmiyor ve
    // sikistirmak etiketleri kirpardi (dockta tam bu yasandi).
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        tabs.forEachIndexed { i, etiket ->
            val secili = i == selectedIndex
            val yazi by animateColorAsState(
                targetValue = if (secili) LkPrimary else LkTextSecondary,
                animationSpec = lkAnim(LkMotion.fast())
            )

            Column(
                modifier = Modifier
                    // §11 satir 40dp; §19 dokunma hedefi 44dp.
                    .heightIn(min = 44.dp)
                    .clickable { onSelect(i) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = etiket,
                    style = LkTypography.getLabel(),
                    color = yazi,
                    fontWeight = if (secili) FontWeight.W700 else FontWeight.W600,
                    modifier = Modifier.padding(horizontal = LkSpacing.Space4)
                )
                Spacer(Modifier.height(LkSpacing.Space2))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(if (secili) LkPrimary else androidx.compose.ui.graphics.Color.Transparent)
                )
            }
        }
    }
}

@Composable
private fun SegmentedTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(LkShapes.SM)
            .background(LkSurfaceSunken)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        tabs.forEachIndexed { i, etiket ->
            val secili = i == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    // §7.1 control-sm; §19 icin dis satir zaten 40dp+.
                    .height(34.dp)
                    .clip(LkShapes.SM)
                    // Secili segment `primaryFill`: uzerindeki beyaz yazi her
                    // iki temada AA gecer (bkz. LkPillChip'teki ayni karar).
                    .background(if (secili) LkPrimaryFill else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable { onSelect(i) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = etiket,
                    style = LkTypography.getLabel(),
                    color = if (secili) LkOnPrimary else LkTextSecondary,
                    fontWeight = FontWeight.W600
                )
            }
        }
    }
}
