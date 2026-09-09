package com.localkarar.app.ui.screens.workspaces

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.localkarar.app.navigation.Destination
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.localkarar.app.ui.components.LkMenuGrubu
import com.localkarar.app.ui.components.LkMenuOgesi
import com.localkarar.app.ui.components.LkMenuSheet
import com.localkarar.app.ui.theme.*

data class WorkspaceSectionItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val getDestination: (String) -> Destination
)

data class WorkspaceSectionGroup(
    val groupTitle: String,
    val items: List<WorkspaceSectionItem>
)

val WORKSPACE_SECTION_GROUPS = listOf(
    WorkspaceSectionGroup(
        groupTitle = "GENEL",
        items = listOf(
            WorkspaceSectionItem("overview", "Genel Bakış", "Özet metrikler ve durum", Icons.Outlined.Dashboard) { wsId -> Destination.WorkspaceHome(wsId) },
            WorkspaceSectionItem("records", "Kayıtlar", "Alacak, borç ve işlemler", Icons.Outlined.ReceiptLong) { wsId -> Destination.Records(wsId) }
        )
    ),
    WorkspaceSectionGroup(
        groupTitle = "TİCARET",
        items = listOf(
            WorkspaceSectionItem("orders", "Siparişler", "Gelen ve giden siparişler", Icons.Outlined.ShoppingCart) { wsId -> Destination.Orders(wsId) },
            WorkspaceSectionItem("products", "Ürünler", "Ürün ve hizmet kataloğu", Icons.Outlined.Inventory2) { wsId -> Destination.Products(wsId) }
        )
    ),
    WorkspaceSectionGroup(
        groupTitle = "OPERASYON",
        items = listOf(
            WorkspaceSectionItem("documents", "Belgeler", "Sözleşme ve dökümanlar", Icons.Outlined.AttachFile) { wsId -> Destination.Documents(wsId) },
            WorkspaceSectionItem("calendar", "Takvim", "Vade ve operasyon takvimi", Icons.Outlined.CalendarMonth) { wsId -> Destination.Calendar(wsId) },
            WorkspaceSectionItem("notifications", "Bildirimler", "Sistem ve süreç uyarıları", Icons.Outlined.Notifications) { wsId -> Destination.Notifications(wsId) },
            // Takvimin yaninda: ikisi de "geriye donup bak" ekrani.
            WorkspaceSectionItem("decisions", "Karar Raporu", "Hedeflenen ve gerçekleşen sonuç", Icons.Outlined.Gavel) { wsId -> Destination.KararRaporu(wsId) }
        )
    ),
    WorkspaceSectionGroup(
        groupTitle = "İNSANLAR",
        items = listOf(
            WorkspaceSectionItem("team", "Ekip", "Çalışanlar ve yetkiler", Icons.Outlined.Group) { wsId -> Destination.Team(wsId) },
            WorkspaceSectionItem("contacts", "Kişiler", "Müşteri ve tedarikçiler", Icons.Outlined.Contacts) { wsId -> Destination.Contacts(wsId) }
        )
    ),
    WorkspaceSectionGroup(
        groupTitle = "YÖNETİM",
        items = listOf(
            WorkspaceSectionItem("activity", "Aktiviteler", "İşlem ve değişiklik günlüğü", Icons.Outlined.History) { wsId -> Destination.Activity(wsId) },
            // Bu giris YOKTU: kullanici mobilden hicbir pazaryeri
            // baglayamiyordu, dolayisiyla Siparisler ve Urunler ekranlarina
            // gercek veri hicbir zaman gelmiyordu.
            WorkspaceSectionItem("integrations", "Pazaryeri Entegrasyonları", "Trendyol, Hepsiburada, N11, Shopify", Icons.Outlined.Link) { wsId -> Destination.WorkspaceIntegrations(wsId) },
            WorkspaceSectionItem("settings", "İşletme Ayarları", "İşletme profili ve yapılandırma", Icons.Outlined.Settings) { wsId -> Destination.WorkspaceSettings(wsId) }
        )
    )
)
/**
 * ISLETME BOLUMLERI CEKMECESI.
 *
 * Ortak `LkMenuSheet` uzerine kuruluyor; acik bolum tepede tam
 * genislikte "one cikan" blokta duruyor ve izgarada da tik rozetiyle
 * isaretli.
 */
@Composable
fun WorkspaceSectionSheet(
    workspaceId: String,
    workspaceName: String?,
    currentSectionId: String,
    onNavigate: (Destination) -> Unit,
    onOpenAllWorkspaces: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val acikBolum = WORKSPACE_SECTION_GROUPS
        .flatMap { it.ogeleri() }
        .firstOrNull { it.id == currentSectionId }

    LkMenuSheet(
        baslik = "İşletme Bölümleri",
        altBaslik = workspaceName ?: "İşletme Takibi",
        seciliId = currentSectionId,
        onClose = onClose,
        modifier = modifier,
        gruplar = WORKSPACE_SECTION_GROUPS.map { grup ->
            LkMenuGrubu(
                baslik = grup.groupTitle,
                ogeler = grup.items.map { oge ->
                    LkMenuOgesi(
                        id = oge.id,
                        baslik = oge.title,
                        aciklama = oge.description,
                        ikon = oge.icon,
                        onClick = { onNavigate(oge.getDestination(workspaceId)) }
                    )
                }
            )
        },
        oneCikan = acikBolum?.let { bolum ->
            {
                AcikBolumBlogu(
                    baslik = bolum.title,
                    aciklama = bolum.description,
                    ikon = bolum.icon
                )
            }
        },
        altEylem = {
            MenuAltEylem(
                metin = "Tüm İşletmeler / İşletme değiştir",
                ikon = Icons.Outlined.SwapHoriz,
                onClick = onOpenAllWorkspaces
            )
        }
    )
}

private fun WorkspaceSectionGroup.ogeleri(): List<WorkspaceSectionItem> = items

/**
 * Cekmecenin tepesindeki "su an buradasin" blogu.
 *
 * Menuyu acan kullanicinin ilk sorusu "neredeyim"; on bir kutucugu
 * tarayarak cevaplamak yerine ustte yaziyor.
 */
@Composable
private fun AcikBolumBlogu(
    baslik: String,
    aciklama: String,
    ikon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .lkShadow(LkElevation.MD, LkShapes.MD)
            .clip(LkShapes.MD)
            .background(
                Brush.linearGradient(listOf(LkBrand.B700, LkBrand.B500))
            )
            .padding(LkSpacing.Space4),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(LkShapes.SM)
                .background(Color(0x33FFFFFF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ikon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(LkSpacing.Space3))
        Column(Modifier.weight(1f)) {
            Text(
                text = "AÇIK BÖLÜM",
                style = LkTypography.getMicro(),
                color = Color(0xCCFFFFFF)
            )
            Text(
                text = baslik,
                style = LkTypography.getBodyStrong(),
                color = Color.White
            )
            Text(
                text = aciklama,
                style = LkTypography.getMicro(),
                color = Color(0xB3FFFFFF),
                maxLines = 1
            )
        }
    }
}

/** Cekmecenin en altindaki ikincil eylem satiri. */
@Composable
internal fun MenuAltEylem(
    metin: String,
    ikon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LkShapes.MD)
            .background(LkSurfaceSunken)
            .border(1.dp, LkLineSoft, LkShapes.MD)
            .clickable(onClick = onClick)
            .heightIn(min = 52.dp)
            .padding(horizontal = LkSpacing.Space4),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ikon,
            contentDescription = null,
            tint = LkPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(LkSpacing.Space3))
        Text(
            text = metin,
            style = LkTypography.getBodySmall(),
            color = LkTextPrimary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = LkTextSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun WorkspaceSectionPill(
    sectionName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(LkShapes.FULL)
            .background(LkSurfacePanel)
            .border(1.dp, LkLineStrong, LkShapes.FULL)
            .clickable(onClick = onClick)
            .padding(horizontal = LkSpacing.Space3, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = sectionName,
            style = LkTypography.getMicro(),
            color = LkPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Icon(
            imageVector = Icons.Outlined.KeyboardArrowDown,
            contentDescription = "Bölüm Seçici",
            tint = LkPrimary,
            modifier = Modifier.size(16.dp)
        )
    }
}
