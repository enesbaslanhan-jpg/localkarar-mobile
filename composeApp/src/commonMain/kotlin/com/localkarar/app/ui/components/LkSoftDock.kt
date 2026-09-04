package com.localkarar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.*

/** Dock sekmesi. Etiket ve ikon disinda hedef bilgisi tasimaz. */
data class LkDockTab(
    val label: String,
    val icon: ImageVector,
    val selected: Boolean,
    val onClick: () -> Unit
)

/**
 * YUZEN ALT DOCK — prototipteki `.soft-dock`.
 *
 * Onceki hali Material `BottomNavigation`'di: ekranin alt kenarina yapisik,
 * tam genislikte, ustunde cizgi olan klasik cubuk. Onaylanan tasarim bunun
 * yerine kenarlardan iceride duran, yuvarlak (radius 36) ve zeminden ayrik
 * bir dock istiyor.
 *
 * ⚠️ `backdrop-filter` (blur) KULLANILMIYOR. Prototipte CSS blur var; Compose'da
 * karsiligi RenderEffect ile mumkun ama her karede tum arka plani yeniden
 * cizmek demek -- liste kaydirirken gorulur bir maliyet. Yerine yuksek alfali
 * duz zemin kullaniliyor; gorsel fark durgun ekranda ayirt edilemiyor.
 *
 * ⚠️ Bu dock icerigin USTUNDE duruyor (Scaffold'un bottomBar'i degil). Yani
 * ekranlarin alt dolgusu kendisi vermeli, yoksa son satir dock'un altinda
 * kalir. `LkDockHeight` bunun icin disari aciliyor.
 */
val LkDockHeight = 76.dp

@Composable
fun LkSoftDock(
    tabs: List<LkDockTab>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            // Prototipte `--dock-shadow: 0 16px 36px rgba(0,0,0,.08)`.
            // Dock sayfanin UZERINDE yuzuyor; golgesi olmadan zemine
            // yapisik duruyordu ve icerik altindan gectigi anlasilmiyordu.
            .lkShadow(LkElevation.DOCK, LkShapes.FULL)
            .background(LkSurfacePanel.copy(alpha = 0.96f), LkShapes.FULL)
            .border(1.dp, LkLineSoft, LkShapes.FULL)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { tab ->
            /*
             * Dalgalanma (ripple) kapali: yuvarlak dock icinde dikdortgen
             * dalga tasiyor. Secili durum zaten renkle belirtiliyor.
             */
            val interaction = remember { MutableInteractionSource() }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
                    .clickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = tab.onClick
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .widthIn(min = 48.dp)
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.label,
                    tint = if (tab.selected) LkPrimary else LkTextMuted,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = tab.label,
                    style = LkTypography.getNavLabel(),
                    color = if (tab.selected) LkPrimary else LkTextMuted,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
