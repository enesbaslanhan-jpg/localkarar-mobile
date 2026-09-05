package com.localkarar.app.ui.screens.workspaces

import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.NorthEast
import androidx.compose.material.icons.outlined.SouthWest
import androidx.compose.material.icons.Icons
import androidx.compose.material.Icon
import com.localkarar.app.ui.components.LkHairline
import com.localkarar.app.ui.components.LkRowGroup
import com.localkarar.app.ui.components.LkListRow
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
import com.localkarar.app.ui.components.LkHeroPage
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

    LkHeroPage(title = "Takip", onBack = onBack) {
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
                            /* Satirlar TEK yukseltilmis yuzeyde; kart yigini degil. */
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(LkSpacing.Space4)
                            ) {
                                item {
                                    LkRowGroup {
                                        state.records.forEachIndexed { i, record ->
                                            RecordCard(
                                                record = record,
                                                onClick = { onOpenRecord(record.id) }
                                            )
                                            if (i != state.records.lastIndex) LkHairline()
                                        }
                                    }
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
    val overdue = dueDate?.let { LkDateUtils.daysUntil(it) } ?: 0
    val isActive = record.status == "open" || record.status == "in_progress"

    /*
     * §24 satir anatomisi — ikon kutucugu | baslik + durum/vade | tur | tutar.
     *
     * 🔴 ONCEDEN KART YIGINIYDI: her kayit kendi kenarlikli kutusundaydi ve
     * icinde iki hap vardi. On kayitlik bir liste on ayri cerceve demekti;
     * goz nereye bakacagini bilmiyordu. Artik tek yuzey, satirlar dikey
     * ayracla ayriliyor.
     *
     * Gecikme SADECE RENKLE degil "Gecikti" kelimesiyle de belirtiliyor.
     */
    val gecikti = record.dueAt != null && isActive && overdue < 0
    val altSatir = listOfNotNull(
        recordStatusLabel(record.status).takeIf { it.isNotBlank() },
        when {
            gecikti -> "Gecikti"
            record.dueAt != null && isActive && dueDate != null -> LkDateUtils.formatShortDate(dueDate)
            else -> null
        }
    ).joinToString(" · ").ifBlank { null }

    LkListRow(
        baslik = record.title,
        altBaslik = altSatir,
        kategori = recordTypeLabel(record.type),
        tutar = record.amount?.let { LkFormatting.formatMoney(it, record.currency) },
        tutarRengi = if (gecikti) LkDanger else LkTextPrimary,
        onClick = onClick,
        ikon = {
            Icon(
                when (record.direction) {
                    "receivable" -> Icons.Outlined.SouthWest
                    "payable" -> Icons.Outlined.NorthEast
                    else -> Icons.Outlined.ReceiptLong
                },
                contentDescription = null,
                tint = if (gecikti) LkDanger else LkTileInk,
                modifier = Modifier.size(21.dp)
            )
        }
    )
}