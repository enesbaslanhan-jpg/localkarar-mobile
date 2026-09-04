package com.localkarar.app.ui.screens.workspaces

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.core.LkFormatting
import com.localkarar.app.network.dto.RecordInputDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkChip
import com.localkarar.app.ui.components.LkDateField
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkNumericField
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.components.LkSectionHeader
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.RecordEditUiState
import com.localkarar.app.workspaces.RecordEditViewModel

private val RECORD_TYPES = listOf(
    "payment", "receivable", "promissory_note", "purchase",
    "shipment", "task", "deferred", "other"
)

private val RECURRENCE_OPTIONS = listOf<String?>(null, "weekly", "monthly", "quarterly", "yearly")

@Composable
fun RecordEditScreen(
    viewModel: RecordEditViewModel,
    presetType: String? = null,
    presetDirection: String? = null,
    isEdit: Boolean,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var actionError by rememberSaveable { mutableStateOf<String?>(null) }

    LkPageLayout(title = if (isEdit) "Kaydı Düzenle" else "Yeni Kayıt", onBack = onBack) {
        when (val state = uiState) {
            is RecordEditUiState.Loading -> LkLoadingState()
            is RecordEditUiState.Error -> LkErrorState(
                message = state.message,
                onRetry = { viewModel.load() }
            )
            is RecordEditUiState.Content -> {
                val initial = state.record

                var type by rememberSaveable(initial?.id) { mutableStateOf(initial?.type ?: presetType ?: "payment") }
                var title by rememberSaveable(initial?.id) { mutableStateOf(initial?.title ?: "") }
                var description by rememberSaveable(initial?.id) { mutableStateOf(initial?.description ?: "") }
                var direction by rememberSaveable(initial?.id) { mutableStateOf(initial?.direction ?: presetDirection ?: "neutral") }
                var amount by rememberSaveable(initial?.id) { mutableStateOf(initial?.amount?.let { LkFormatting.formatNumber(it) } ?: "") }
                var currency by rememberSaveable(initial?.id) { mutableStateOf(initial?.currency ?: "TRY") }
                var priority by rememberSaveable(initial?.id) { mutableStateOf(initial?.priority ?: "normal") }
                var dueDate by rememberSaveable(initial?.id) { mutableStateOf(LkDateUtils.parseDate(initial?.dueAt)) }
                var contactId by rememberSaveable(initial?.id) { mutableStateOf(initial?.contactId) }
                var assignedToId by rememberSaveable(initial?.id) { mutableStateOf(initial?.assignedToId) }
                var recurrence by rememberSaveable(initial?.id) { mutableStateOf(initial?.recurrenceRule) }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(LkSpacing.Space4),
                    verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
                ) {
                    item {
                        LkSectionHeader(title = "Tür")
                        Spacer(modifier = Modifier.height(LkSpacing.Space2))
                        Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                            RECORD_TYPES.take(4).forEach { t ->
                                LkChip(
                                    text = recordTypeLabel(t),
                                    background = if (type == t) LkPrimary else LkSurfaceRaised,
                                    contentColor = if (type == t) LkOnPrimary else LkTextSecondary,
                                    modifier = Modifier.clickable { type = t }
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                            RECORD_TYPES.drop(4).forEach { t ->
                                LkChip(
                                    text = recordTypeLabel(t),
                                    background = if (type == t) LkPrimary else LkSurfaceRaised,
                                    contentColor = if (type == t) LkOnPrimary else LkTextSecondary,
                                    modifier = Modifier.clickable { type = t }
                                )
                            }
                        }
                    }

                    item {
                        LkTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = "Başlık",
                            placeholder = "Örn: Elektrik faturası"
                        )
                    }

                    item {
                        LkTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = "Açıklama (isteğe bağlı)",
                            placeholder = "Detay"
                        )
                    }

                    item {
                        LkSectionHeader(title = "Yön")
                        Spacer(modifier = Modifier.height(LkSpacing.Space2))
                        Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                            listOf("payable" to "Borç", "receivable" to "Alacak", "neutral" to "Nötr").forEach { (value, label) ->
                                LkChip(
                                    text = label,
                                    background = if (direction == value) LkPrimary else LkSurfaceRaised,
                                    contentColor = if (direction == value) LkOnPrimary else LkTextSecondary,
                                    modifier = Modifier.clickable { direction = value }
                                )
                            }
                        }
                    }

                    item {
                        LkNumericField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = "Tutar (isteğe bağlı)",
                            placeholder = "0,00",
                            suffix = currency
                        )
                    }

                    item {
                        LkDateField(
                            label = "Son Tarih",
                            date = dueDate,
                            onDateSelected = { dueDate = it }
                        )
                    }

                    item {
                        LkSectionHeader(title = "Öncelik")
                        Spacer(modifier = Modifier.height(LkSpacing.Space2))
                        Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                            listOf("low" to "Düşük", "normal" to "Normal", "high" to "Yüksek", "urgent" to "Acil").forEach { (value, label) ->
                                LkChip(
                                    text = label,
                                    background = if (priority == value) LkPrimary else LkSurfaceRaised,
                                    contentColor = if (priority == value) LkOnPrimary else LkTextSecondary,
                                    modifier = Modifier.clickable { priority = value }
                                )
                            }
                        }
                    }

                    if (state.contacts.isNotEmpty()) {
                        item {
                            LkSectionHeader(title = "Kişi")
                            Spacer(modifier = Modifier.height(LkSpacing.Space2))
                            Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                                LkChip(
                                    text = "Yok",
                                    background = if (contactId == null) LkPrimary else LkSurfaceRaised,
                                    contentColor = if (contactId == null) LkOnPrimary else LkTextSecondary,
                                    modifier = Modifier.clickable { contactId = null }
                                )
                                state.contacts.take(6).forEach { contact ->
                                    LkChip(
                                        text = contact.name,
                                        background = if (contactId == contact.id) LkPrimary else LkSurfaceRaised,
                                        contentColor = if (contactId == contact.id) LkOnPrimary else LkTextSecondary,
                                        modifier = Modifier.clickable { contactId = contact.id }
                                    )
                                }
                            }
                        }
                    }

                    if (state.members.isNotEmpty()) {
                        item {
                            LkSectionHeader(title = "Sorumlu")
                            Spacer(modifier = Modifier.height(LkSpacing.Space2))
                            Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                                LkChip(
                                    text = "Yok",
                                    background = if (assignedToId == null) LkPrimary else LkSurfaceRaised,
                                    contentColor = if (assignedToId == null) LkOnPrimary else LkTextSecondary,
                                    modifier = Modifier.clickable { assignedToId = null }
                                )
                                state.members.forEach { member ->
                                    LkChip(
                                        text = member.name,
                                        background = if (assignedToId == member.userId) LkPrimary else LkSurfaceRaised,
                                        contentColor = if (assignedToId == member.userId) LkOnPrimary else LkTextSecondary,
                                        modifier = Modifier.clickable { assignedToId = member.userId }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        LkSectionHeader(title = "Tekrar")
                        Spacer(modifier = Modifier.height(LkSpacing.Space2))
                        Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                            RECURRENCE_OPTIONS.forEach { option ->
                                val label = when (option) {
                                    null -> "Yok"
                                    "weekly" -> "Haftalık"
                                    "monthly" -> "Aylık"
                                    "quarterly" -> "Üç Aylık"
                                    else -> "Yıllık"
                                }
                                LkChip(
                                    text = label,
                                    background = if (recurrence == option) LkPrimary else LkSurfaceRaised,
                                    contentColor = if (recurrence == option) LkOnPrimary else LkTextSecondary,
                                    modifier = Modifier.clickable { recurrence = option }
                                )
                            }
                        }
                    }

                    item {
                        if (actionError != null) {
                            Text(
                                text = actionError!!,
                                style = LkTypography.getBodySmall(),
                                color = LkDanger
                            )
                            Spacer(modifier = Modifier.height(LkSpacing.Space2))
                        }
                        LkButton(
                            text = if (isEdit) "Kaydet" else "Oluştur",
                            onClick = {
                                actionError = null
                                if (title.isBlank()) {
                                    actionError = "Başlık gerekli"
                                    return@LkButton
                                }
                                val parsedAmount = amount.trim().ifBlank { null }?.let { LkFormatting.parseDecimal(it) }
                                if (amount.isNotBlank() && parsedAmount == null) {
                                    actionError = "Geçersiz tutar"
                                    return@LkButton
                                }
                                val input = RecordInputDto(
                                    type = type,
                                    title = title.trim(),
                                    description = description.trim().ifBlank { null },
                                    direction = direction,
                                    amount = parsedAmount,
                                    currency = currency,
                                    priority = priority,
                                    dueAt = dueDate?.let { "${it}T12:00:00.000Z" },
                                    contactId = contactId,
                                    assignedToId = assignedToId,
                                    recurrenceRule = recurrence
                                )
                                viewModel.save(
                                    input = input,
                                    onSuccess = onSaved,
                                    onError = { actionError = it }
                                )
                            },
                            enabled = !state.isSaving,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}