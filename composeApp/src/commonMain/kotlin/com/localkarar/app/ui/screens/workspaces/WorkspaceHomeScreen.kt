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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
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
import com.localkarar.app.ui.components.LkSectionHeader
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.WorkspaceHomeUiState
import com.localkarar.app.workspaces.WorkspaceHomeViewModel

@Composable
fun WorkspaceHomeScreen(
    viewModel: WorkspaceHomeViewModel,
    onOpenRecords: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenDocuments: () -> Unit,
    onOpenTeam: () -> Unit,
    onOpenContacts: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenActivity: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRecord: (String) -> Unit,
    onAddRecord: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LkPageLayout(title = "İşletme", onBack = onBack) {
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
                            ) {
                                LkMetricCard(
                                    label = "Açık Kayıt",
                                    value = summary.counts.open.toString(),
                                    icon = Icons.Default.EventNote,
                                    modifier = Modifier.weight(1f)
                                )
                                LkMetricCard(
                                    label = "Geciken",
                                    value = summary.counts.overdue.toString(),
                                    icon = Icons.Default.Today,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(LkSpacing.Space3))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
                            ) {
                                LkMetricCard(
                                    label = "30 Gün Alacak",
                                    value = LkFormatting.formatMoney(summary.nextThirtyDays.receivable, state.workspace.currency),
                                    icon = Icons.Default.ReceiptLong,
                                    modifier = Modifier.weight(1f)
                                )
                                LkMetricCard(
                                    label = "30 Gün Borç",
                                    value = LkFormatting.formatMoney(summary.nextThirtyDays.payable, state.workspace.currency),
                                    icon = Icons.Default.Description,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        if (summary.upcoming.isNotEmpty()) {
                            item {
                                LkInfoPanel(title = "Yaklaşan Kayıtlar") {
                                    summary.upcoming.take(5).forEach { record ->
                                        UpcomingRecordRow(record, onOpen = { onOpenRecord(record.id) })
                                        Spacer(modifier = Modifier.height(LkSpacing.Space2))
                                    }
                                }
                            }
                        }
                    }

                    item {
                        LkSectionHeader(title = "Menü")
                        Spacer(modifier = Modifier.height(LkSpacing.Space3))
                        SectionNavRow(
                            items = listOf(
                                SectionNavItem("Takip", Icons.Default.ReceiptLong, onOpenRecords),
                                SectionNavItem("Takvim", Icons.Default.CalendarMonth, onOpenCalendar),
                                SectionNavItem("Belgeler", Icons.Default.AttachFile, onOpenDocuments),
                                SectionNavItem("Ekip", Icons.Default.Group, onOpenTeam),
                                SectionNavItem("Kişiler", Icons.Default.Contacts, onOpenContacts),
                                SectionNavItem("Bildirimler", Icons.Default.Notifications, onOpenNotifications),
                                SectionNavItem("Etkinlik", Icons.Default.Construction, onOpenActivity),
                                SectionNavItem("Ayarlar", Icons.Default.Settings, onOpenSettings)
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

private data class SectionNavItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
private fun SectionNavRow(items: List<SectionNavItem>) {
    val rows = items.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
            ) {
                rowItems.forEach { item ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(LkSurfacePanel, LkShapes.MD)
                            .border(1.dp, LkLineStrong, LkShapes.MD)
                            .clickable(onClick = item.onClick)
                            .padding(LkSpacing.PadPanel),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = LkPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(LkSpacing.Space2))
                        Text(
                            text = item.label,
                            style = LkTypography.getBodySmall(),
                            color = LkTextPrimary
                        )
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
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