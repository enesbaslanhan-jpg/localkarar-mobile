package com.localkarar.app.ui.screens.workspaces

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.core.LkFormatting
import com.localkarar.app.network.dto.BusinessRecordDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkChip
import com.localkarar.app.ui.components.LkEmptyState
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.RecordsUiState
import com.localkarar.app.workspaces.RecordsViewModel

fun recordTypeLabel(type: String): String {
    return when (type) {
        "payment" -> "Ödeme"
        "receivable" -> "Tahsilat"
        "promissory_note" -> "Senet"
        "purchase" -> "Satın Alma"
        "shipment" -> "Sevkiyat"
        "task" -> "Görev"
        "deferred" -> "Ertelenen"
        else -> "Diğer"
    }
}

fun recordStatusLabel(status: String): String {
    return when (status) {
        "open" -> "Açık"
        "in_progress" -> "Devam Ediyor"
        "completed" -> "Tamamlandı"
        "cancelled" -> "İptal"
        "deferred" -> "Ertelendi"
        else -> status
    }
}

private val STATUS_FILTERS = listOf<String?>(null, "open", "in_progress", "completed", "deferred")

@Composable
fun RecordsScreen(
    viewModel: RecordsViewModel,
    onOpenRecord: (String) -> Unit,
    onAddRecord: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var statusFilter by remember { mutableStateOf<String?>(null) }

    LkPageLayout(title = "Takip", onBack = onBack) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space2),
                        horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)
                    ) {
                        STATUS_FILTERS.forEach { filter ->
                            val label = when (filter) {
                                null -> "Tümü"
                                else -> recordStatusLabel(filter)
                            }
                            val selected = statusFilter == filter
                            LkChip(
                                text = label,
                                background = if (selected) LkPrimary else LkSurfaceRaised,
                                contentColor = if (selected) LkOnPrimary else LkTextSecondary,
                                modifier = Modifier.clickable {
                                    statusFilter = filter
                                    viewModel.setFilter(filter, null)
                                }
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(5f)) {
                when (val state = uiState) {
                    is RecordsUiState.Loading -> LkLoadingState()
                    is RecordsUiState.Error -> LkErrorState(
                        message = state.message,
                        onRetry = { viewModel.load() }
                    )
                    is RecordsUiState.Content -> {
                        if (state.records.isEmpty()) {
                            LkEmptyState(
                                title = "Kayıt bulunamadı",
                                description = "Bu filtreye uygun kayıt yok."
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(LkSpacing.Space4),
                                verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
                            ) {
                                items(state.records, key = { it.id }) { record ->
                                    RecordCard(record = record, onClick = { onOpenRecord(record.id) })
                                }
                            }
                        }
                    }
                }
            }

            LkButton(
                text = "Yeni Kayıt",
                onClick = onAddRecord,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(LkSpacing.Space4)
            )
        }
    }
}

@Composable
fun RecordCard(
    record: BusinessRecordDto,
    onClick: () -> Unit
) {
    val dueDate = LkDateUtils.parseDate(record.dueAt)
    val overdue = dueDate?.let { LkDateUtils.daysUntil(it) } ?: 0L
    val isActive = record.status == "open" || record.status == "in_progress"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LkSurfacePanel, LkShapes.MD)
            .border(1.dp, LkLineStrong, LkShapes.MD)
            .clickable(onClick = onClick)
            .padding(LkSpacing.PadPanel)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = record.title,
                style = LkTypography.getBodyStrong(),
                color = LkTextPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(LkSpacing.Space3))
            record.amount?.let {
                Text(
                    text = LkFormatting.formatMoney(it, record.currency),
                    style = LkTypography.getBodyStrong(),
                    color = when (record.direction) {
                        "payable" -> LkDanger
                        "receivable" -> LkSuccess
                        else -> LkTextPrimary
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(LkSpacing.Space2))
        Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2), verticalAlignment = Alignment.CenterVertically) {
            LkChip(text = recordTypeLabel(record.type))
            LkChip(
                text = recordStatusLabel(record.status),
                background = LkSurfaceRaised,
                contentColor = if (record.status == "completed") LkSuccess
                else if (record.status == "cancelled") LkTextMuted
                else if (record.status == "deferred") LkWarning
                else LkTextSecondary
            )
            if (record.dueAt != null && isActive) {
                Text(
                    text = if (overdue < 0) "Gecikti" else LkDateUtils.formatShortDate(dueDate!!),
                    style = LkTypography.getMicro(),
                    color = if (overdue < 0) LkDanger else LkTextMuted
                )
            }
        }
    }
}