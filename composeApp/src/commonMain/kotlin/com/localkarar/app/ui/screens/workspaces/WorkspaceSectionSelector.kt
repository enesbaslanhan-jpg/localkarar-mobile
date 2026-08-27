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
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.localkarar.app.navigation.Destination
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
            WorkspaceSectionItem("overview", "Genel Bakış", "Özet metrikler ve durum", Icons.Default.Dashboard) { wsId -> Destination.WorkspaceHome(wsId) },
            WorkspaceSectionItem("records", "Kayıtlar", "Alacak, borç ve işlemler", Icons.Default.ReceiptLong) { wsId -> Destination.Records(wsId) }
        )
    ),
    WorkspaceSectionGroup(
        groupTitle = "TİCARET",
        items = listOf(
            WorkspaceSectionItem("orders", "Siparişler", "Gelen ve giden siparişler", Icons.Default.ShoppingCart) { wsId -> Destination.Orders(wsId) },
            WorkspaceSectionItem("products", "Ürünler", "Ürün ve hizmet kataloğu", Icons.Default.Inventory2) { wsId -> Destination.Products(wsId) }
        )
    ),
    WorkspaceSectionGroup(
        groupTitle = "OPERASYON",
        items = listOf(
            WorkspaceSectionItem("documents", "Belgeler", "Sözleşme ve dökümanlar", Icons.Default.AttachFile) { wsId -> Destination.Documents(wsId) },
            WorkspaceSectionItem("calendar", "Takvim", "Vade ve operasyon takvimi", Icons.Default.CalendarMonth) { wsId -> Destination.Calendar(wsId) },
            WorkspaceSectionItem("notifications", "Bildirimler", "Sistem ve süreç uyarıları", Icons.Default.Notifications) { wsId -> Destination.Notifications(wsId) }
        )
    ),
    WorkspaceSectionGroup(
        groupTitle = "İNSANLAR",
        items = listOf(
            WorkspaceSectionItem("team", "Ekip", "Çalışanlar ve yetkiler", Icons.Default.Group) { wsId -> Destination.Team(wsId) },
            WorkspaceSectionItem("contacts", "Kişiler", "Müşteri ve tedarikçiler", Icons.Default.Contacts) { wsId -> Destination.Contacts(wsId) }
        )
    ),
    WorkspaceSectionGroup(
        groupTitle = "YÖNETİM",
        items = listOf(
            WorkspaceSectionItem("activity", "Aktiviteler", "İşlem ve değişiklik günlüğü", Icons.Default.History) { wsId -> Destination.Activity(wsId) },
            WorkspaceSectionItem("settings", "İşletme Ayarları", "İşletme profili ve yapılandırma", Icons.Default.Settings) { wsId -> Destination.WorkspaceSettings(wsId) }
        )
    )
)

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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LkSurfaceCanvas, shape = LkShapes.LG)
            .padding(horizontal = LkSpacing.Space6, vertical = LkSpacing.Space6)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "İşletme Bölümleri",
                    style = LkTypography.getSectionTitle(),
                    color = LkTextPrimary
                )
                Text(
                    text = workspaceName ?: "İşletme Takibi",
                    style = LkTypography.getMicro(),
                    color = LkPrimary
                )
            }

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Kapat",
                    tint = LkTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(LkSpacing.Space4))
        Divider(color = LkLineSoft)
        Spacer(modifier = Modifier.height(LkSpacing.Space4))

        // Groups
        WORKSPACE_SECTION_GROUPS.forEachIndexed { groupIndex, group ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = group.groupTitle,
                    style = LkTypography.getMetadata(),
                    color = LkPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = LkSpacing.Space2)
                )

                group.items.forEach { item ->
                    val isSelected = item.id == currentSectionId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(LkShapes.MD)
                            .background(if (isSelected) LkSurfaceRaised else LkSurfacePanel)
                            .border(1.dp, if (isSelected) LkPrimary else LkLineSoft, LkShapes.MD)
                            .clickable {
                                onNavigate(item.getDestination(workspaceId))
                                onClose()
                            }
                            .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space3),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(LkShapes.SM)
                                .background(if (isSelected) LkPrimary else LkSurfaceSunken),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (isSelected) LkOnPrimary else LkPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(LkSpacing.Space3))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = LkTypography.getBodyStrong(),
                                color = if (isSelected) LkPrimary else LkTextPrimary
                            )
                            Text(
                                text = item.description,
                                style = LkTypography.getMicro(),
                                color = LkTextSecondary
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Seçili",
                                tint = LkPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            if (groupIndex < WORKSPACE_SECTION_GROUPS.size - 1) {
                Spacer(modifier = Modifier.height(LkSpacing.Space4))
            }
        }

        Spacer(modifier = Modifier.height(LkSpacing.Space5))
        Divider(color = LkLineSoft)
        Spacer(modifier = Modifier.height(LkSpacing.Space4))

        // Switch workspace footer button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(LkShapes.MD)
                .background(LkSurfaceSunken)
                .border(1.dp, LkLineSoft, LkShapes.MD)
                .clickable {
                    onOpenAllWorkspaces()
                    onClose()
                }
                .padding(LkSpacing.Space4),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = LkPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(LkSpacing.Space3))
                Text(
                    text = "Tüm İşletmeler / İşletme Değiştir",
                    style = LkTypography.getBodySmall(),
                    color = LkTextPrimary
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = LkTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(LkSpacing.Space6))
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
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Bölüm Seçici",
            tint = LkPrimary,
            modifier = Modifier.size(16.dp)
        )
    }
}
