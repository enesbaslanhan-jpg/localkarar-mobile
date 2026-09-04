package com.localkarar.app.ui.screens.workspaces

import com.localkarar.app.ui.components.LkButton
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Today
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.core.LkFormatting
import com.localkarar.app.network.dto.BusinessRecordDto
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkInfoPanel
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkMetricCard
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.components.LkSection
import com.localkarar.app.ui.components.LkTactileAction
import com.localkarar.app.ui.components.LkHairline
import com.localkarar.app.ui.components.LkSectionHeader
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.WorkspaceHomeUiState
import com.localkarar.app.workspaces.WorkspaceHomeViewModel

@Composable
fun WorkspaceHomeScreen(
    viewModel: WorkspaceHomeViewModel,
    onOpenRecords: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenProducts: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenDocuments: () -> Unit,
    onOpenTeam: () -> Unit,
    onOpenContacts: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenActivity: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRecord: (String) -> Unit,
    onAddRecord: () -> Unit,
    onOpenSectionSelector: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LkPageLayout(
        title = "İşletme Takibi",
        onBack = onBack,
        actions = {
            WorkspaceSectionPill(
                sectionName = "Genel Bakış",
                onClick = onOpenSectionSelector,
                modifier = Modifier.padding(end = 12.dp)
            )
        }
    ) {
        when (val state = uiState) {
            is WorkspaceHomeUiState.Loading -> LkLoadingState()
            is WorkspaceHomeUiState.Error -> LkErrorState(
                message = state.message,
                onRetry = { viewModel.load() }
            )
            is WorkspaceHomeUiState.Content -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(LkSpacing.Space4),
                    verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
                ) {
                    item {
                        LkSectionHeader(
                            title = state.workspace.name,
                            subtitle = listOfNotNull(
                                state.workspace.sector,
                                state.workspace.city
                            ).joinToString(" • ").ifBlank { null }
                        )
                    }

                    state.summary?.let { summary ->
                        item {
                            // Prototipteki `metrics-row`: kutu YOK, bolum acik.
                            // Onceden dort ayri `LkMetricCard` vardi ve sayfa
                            // kart yigini gibi duruyordu.
                            LkSection(title = "Özet") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
                                ) {
                                    OzetMetrik("Açık Kayıt", summary.counts.open.toString(), LkTextPrimary, Modifier.weight(1f))
                                    OzetMetrik(
                                        "Geciken",
                                        summary.counts.overdue.toString(),
                                        if (summary.counts.overdue > 0) LkDanger else LkTextPrimary,
                                        Modifier.weight(1f)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
                                ) {
                                    OzetMetrik(
                                        "30 Gün Alacak",
                                        LkFormatting.formatMoney(summary.nextThirtyDays.receivable, state.workspace.currency),
                                        LkTextPrimary,
                                        Modifier.weight(1f)
                                    )
                                    OzetMetrik(
                                        "30 Gün Borç",
                                        LkFormatting.formatMoney(summary.nextThirtyDays.payable, state.workspace.currency),
                                        LkTextPrimary,
                                        Modifier.weight(1f)
                                    )
                                }

                                // Yonu belirsiz kayitlar hicbir toplama girmiyor;
                                // kendi satiri olmadan ekranda hic gorunmuyorlar.
                                val bekleyen = summary.awaitingDirection
                                if (bekleyen != null && bekleyen.count > 0) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(LkWarning.copy(alpha = 0.12f), LkShapes.SM)
                                            .padding(LkSpacing.Space3),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Outlined.HelpOutline,
                                            contentDescription = null,
                                            tint = LkWarning,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(LkSpacing.Space3))
                                        Column {
                                            Text(
                                                "Yön bekliyor · " + LkFormatting.formatMoney(bekleyen.amount, state.workspace.currency),
                                                style = LkTypography.getBodyStrong(),
                                                color = LkTextPrimary
                                            )
                                            Text(
                                                bekleyen.count.toString() + " kayıt · borç mu alacak mı belirsiz",
                                                style = LkTypography.getMetadata(),
                                                color = LkTextMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (summary.upcoming.isNotEmpty()) {
                            item {
                                LkSection(title = "Yaklaşan Kayıtlar") {
                                    summary.upcoming.take(5).forEachIndexed { i, record ->
                                        UpcomingRecordRow(record, onOpen = { onOpenRecord(record.id) })
                                        if (i != minOf(4, summary.upcoming.lastIndex)) LkHairline()
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LkSectionHeader(title = "İşletme Bölümleri")
                            Text(
                                text = "Tümünü Seç",
                                style = LkTypography.getMicro(),
                                color = LkPrimary,
                                modifier = Modifier.clickable(onClick = onOpenSectionSelector)
                            )
                        }
                        Spacer(modifier = Modifier.height(LkSpacing.Space3))
                        SectionNavRow(
                            items = listOf(
                                SectionNavItem("Kayıtlar", Icons.Outlined.ReceiptLong, onOpenRecords),
                                SectionNavItem("Siparişler", Icons.Outlined.ShoppingCart, onOpenOrders),
                                SectionNavItem("Ürünler", Icons.Outlined.Inventory2, onOpenProducts),
                                SectionNavItem("Belgeler", Icons.Outlined.AttachFile, onOpenDocuments),
                                // Sira webdeki WORKSPACE_NAV_TABS ile AYNI olmali:
                                // bildirimler takvimden ONCE. Web tarafinda bu sirayi
                                // koruyan bir regresyon testi var (navigation.js notu).
                                SectionNavItem("Bildirimler", Icons.Outlined.Notifications, onOpenNotifications),
                                SectionNavItem("Takvim", Icons.Outlined.CalendarMonth, onOpenCalendar),
                                SectionNavItem("Ekip", Icons.Outlined.Group, onOpenTeam),
                                SectionNavItem("Kişiler", Icons.Outlined.Contacts, onOpenContacts),
                                SectionNavItem("Aktiviteler", Icons.Outlined.Construction, onOpenActivity),
                                SectionNavItem("Ayarlar", Icons.Outlined.Settings, onOpenSettings)
                            )
                        )
                    }

                    item {
                        LkButton(
                            text = "Yeni Kayıt Ekle",
                            onClick = onAddRecord,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OzetMetrik(
    etiket: String,
    deger: String,
    renk: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(etiket, style = LkTypography.getBodySmall(), color = LkTextMuted)
        Text(
            text = deger,
            style = LkTypography.getMetric().copy(fontFeatureSettings = "tnum"),
            color = renk,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}

private data class SectionNavItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
private fun SectionNavRow(items: List<SectionNavItem>) {
    // Prototipteki `actions-grid` + `tactile-action-btn` deseni.
    //
    // Onceden her bolum tam genislikte KART idi: 11 bolum icin ekranin
    // tamami kart yigini oluyordu ve Ana Sayfa'daki "Hizli Islemler"
    // izgarasindan farkli bir dil konusuyordu. Ayni sey ayni gorunmeli.
    //
    // Dort sutun: 11 oge uc satira sigiyor, dokunma hedefi korunuyor
    // (`LkTactileAction` icinde 44dp kutu + etiket).
    val satirlar = items.chunked(4)
    Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)) {
        satirlar.forEach { satir ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
            ) {
                satir.forEach { item ->
                    LkTactileAction(
                        icon = item.icon,
                        label = item.label,
                        onClick = item.onClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Son satir eksikse hizalama bozulmasin diye bosluk.
                repeat(4 - satir.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun UpcomingRecordRow(
    record: BusinessRecordDto,
    onOpen: () -> Unit
) {
    val dueDate = LkDateUtils.parseDate(record.dueAt)
    val overdue = dueDate?.let { LkDateUtils.daysUntil(it) } ?: 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.title,
                style = LkTypography.getBodySmall(),
                color = LkTextPrimary,
                maxLines = 1
            )
            Text(
                text = record.type.replace('_', ' ').replaceFirstChar { it.uppercase() },
                style = LkTypography.getMicro(),
                color = LkTextMuted
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = record.amount?.let { LkFormatting.formatMoney(it, record.currency) } ?: "—",
                style = LkTypography.getBodyStrong(),
                color = when (record.direction) {
                    "payable" -> LkDanger
                    "receivable" -> LkSuccess
                    else -> LkTextPrimary
                }
            )
            if (record.dueAt != null && dueDate != null) {
                Text(
                    text = if (overdue < 0) "Gecikti" else LkDateUtils.formatShortDate(dueDate),
                    style = LkTypography.getMicro(),
                    color = if (overdue < 0) LkDanger else LkTextMuted
                )
            }
        }
    }
}