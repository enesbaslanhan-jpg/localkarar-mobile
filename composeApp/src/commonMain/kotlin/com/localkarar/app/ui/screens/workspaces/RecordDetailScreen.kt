package com.localkarar.app.ui.screens.workspaces

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.core.LkFormatting
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkInfoPanel
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.components.LkResultRow
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.RecordDetailUiState
import com.localkarar.app.workspaces.RecordDetailViewModel
import kotlinx.datetime.LocalDate

@Composable
fun RecordDetailScreen(
    viewModel: RecordDetailViewModel,
    onEdit: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var actionError by remember { mutableStateOf<String?>(null) }
    var showDeferDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LkPageLayout(title = "Kayıt Detayı", onBack = onBack) {
        when (val state = uiState) {
            is RecordDetailUiState.Loading -> LkLoadingState()
            is RecordDetailUiState.Error -> LkErrorState(
                message = state.message,
                onRetry = { viewModel.load() }
            )
            is RecordDetailUiState.Content -> {
                val record = state.record
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(LkSpacing.Space4),
                    verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
                ) {
                    item {
                        if (actionError != null) {
                            Text(
                                text = actionError!!,
                                style = LkTypography.getBodySmall(),
                                color = LkDanger
                            )
                            Spacer(modifier = Modifier.height(LkSpacing.Space2))
                        }
                        LkInfoPanel(title = record.title) {
                            LkResultRow(
                                label = "Tür",
                                value = recordTypeLabel(record.type)
                            )
                            Spacer(modifier = Modifier.height(LkSpacing.Space2))
                            LkResultRow(
                                label = "Durum",
                                value = recordStatusLabel(record.status)
                            )
                            Spacer(modifier = Modifier.height(LkSpacing.Space2))
                            record.amount?.let {
                                LkResultRow(
                                    label = "Tutar",
                                    value = LkFormatting.formatMoney(it, record.currency),
                                    valueColor = when (record.direction) {
                                        "payable" -> LkDanger
                                        "receivable" -> LkSuccess
                                        else -> LkTextPrimary
                                    }
                                )
                                Spacer(modifier = Modifier.height(LkSpacing.Space2))
                            }
                            record.dueAt?.let {
                                LkResultRow(label = "Son Tarih", value = LkDateUtils.formatDateTime(it))
                                Spacer(modifier = Modifier.height(LkSpacing.Space2))
                            }
                            record.contact?.let {
                                LkResultRow(label = "Kişi", value = it.name)
                                Spacer(modifier = Modifier.height(LkSpacing.Space2))
                            }
                            record.assignedTo?.let {
                                LkResultRow(label = "Sorumlu", value = it.name)
                                Spacer(modifier = Modifier.height(LkSpacing.Space2))
                            }
                            record.recurrenceRule?.let {
                                LkResultRow(label = "Tekrar", value = it.replaceFirstChar { c -> c.uppercase() })
                                Spacer(modifier = Modifier.height(LkSpacing.Space2))
                            }
                            if (!record.description.isNullOrBlank()) {
                                Text(
                                    text = record.description,
                                    style = LkTypography.getBodySmall(),
                                    color = LkTextSecondary
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
                        ) {
                            LkButton(
                                text = "Tamamla",
                                variant = LkButtonVariant.SECONDARY,
                                enabled = record.status != "completed" && !state.isActing,
                                onClick = {
                                    actionError = null
                                    viewModel.setStatus("completed") { actionError = it }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            LkButton(
                                text = "İptal",
                                variant = LkButtonVariant.QUIET,
                                enabled = record.status != "cancelled" && !state.isActing,
                                onClick = {
                                    actionError = null
                                    viewModel.setStatus("cancelled") { actionError = it }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
                        ) {
                            LkButton(
                                text = "Düzenle",
                                variant = LkButtonVariant.SECONDARY,
                                onClick = onEdit,
                                modifier = Modifier.weight(1f)
                            )
                            LkButton(
                                text = "Ertele",
                                variant = LkButtonVariant.SECONDARY,
                                enabled = record.status != "deferred" && !state.isActing,
                                onClick = { showDeferDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        LkButton(
                            text = "Kaydı Sil",
                            variant = LkButtonVariant.GHOST,
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    if (showDeferDialog) {
        DeferRecordDialog(
            isActing = (uiState as? RecordDetailUiState.Content)?.isActing == true,
            onDismiss = { showDeferDialog = false },
            onDefer = { dueDate, reason ->
                actionError = null
                viewModel.defer(
                    dueAt = "${dueDate}T12:00:00.000Z",
                    reason = reason,
                    onError = { actionError = it }
                )
                showDeferDialog = false
            }
        )
    }

    if (showDeleteConfirm) {
        androidx.compose.material.AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            backgroundColor = LkSurfacePanel,
            title = {
                Text(
                    text = "Kaydı Sil",
                    style = LkTypography.getBodyStrong(),
                    color = LkTextPrimary
                )
            },
            text = {
                Text(
                    text = "Bu kayıt kalıcı olarak arşivlenecek. Devam etmek istiyor musunuz?",
                    style = LkTypography.getBodySmall(),
                    color = LkTextSecondary
                )
            },
            confirmButton = {
                LkButton(
                    text = "Evet, Sil",
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.delete { success ->
                            if (success) onBack() else actionError = "Kayıt silinemedi."
                        }
                    }
                )
            },
            dismissButton = {
                LkButton(
                    text = "Vazgeç",
                    variant = LkButtonVariant.QUIET,
                    onClick = { showDeleteConfirm = false }
                )
            }
        )
    }
}

@Composable
private fun DeferRecordDialog(
    isActing: Boolean,
    onDismiss: () -> Unit,
    onDefer: (LocalDate, String) -> Unit
) {
    var dueDate by remember { mutableStateOf(LkDateUtils.dateAt(1)) }
    var reason by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    androidx.compose.material.AlertDialog(
        onDismissRequest = { if (!isActing) onDismiss() },
        backgroundColor = LkSurfacePanel,
        title = {
            Text(
                text = "Kaydı Ertele",
                style = LkTypography.getBodyStrong(),
                color = LkTextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)) {
                if (error != null) {
                    Text(text = error!!, style = LkTypography.getBodySmall(), color = LkDanger)
                }
                com.localkarar.app.ui.components.LkDateField(
                    label = "Yeni Tarih",
                    date = dueDate,
                    onDateSelected = { dueDate = it }
                )
                com.localkarar.app.ui.components.LkTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = "Gerekçe",
                    placeholder = "Erteleme nedeni"
                )
            }
        },
        confirmButton = {
            LkButton(
                text = if (isActing) "Erteleniyor..." else "Ertele",
                enabled = reason.isNotBlank() && !isActing,
                onClick = {
                    if (reason.isBlank()) {
                        error = "Gerekçe yazın"
                    } else {
                        onDefer(dueDate, reason.trim())
                    }
                }
            )
        },
        dismissButton = {
            LkButton(
                text = "Vazgeç",
                variant = LkButtonVariant.QUIET,
                onClick = onDismiss
            )
        }
    )
}