package com.localkarar.app.ui.screens.calculations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.calculations.FinancialModelUiState
import com.localkarar.app.calculations.FinancialModelViewModel
import com.localkarar.app.core.LkFormatting
import com.localkarar.app.core.displayValue
import com.localkarar.app.network.dto.CalculationStepDto
import com.localkarar.app.network.dto.ValidationCheckDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkChip
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkInfoPanel
import com.localkarar.app.ui.components.LkNumericField
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.components.LkResultRow
import com.localkarar.app.ui.components.LkSectionHeader
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.theme.*
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray

@Composable
fun FinancialModelScreen(
    viewModel: FinancialModelViewModel,
    workspaceName: String?,
    onOpenWorkspace: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var actionError by remember { mutableStateOf<String?>(null) }

    LkPageLayout(title = "Finansal Görünüm", onBack = onBack) {
        when (val state = uiState) {
            is FinancialModelUiState.Loading -> com.localkarar.app.ui.components.LkLoadingState()
            is FinancialModelUiState.Error -> LkErrorState(
                message = state.message,
                onRetry = { viewModel.load() }
            )
            is FinancialModelUiState.Content -> {
                val model = state.model
                val inputValues = remember(model.code) {
                    mutableStateMapOf<String, String>()
                }
                val inputErrors = remember(model.code) {
                    mutableStateMapOf<String, String>()
                }
                var scenarioName by remember(model.code) { mutableStateOf("") }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(LkSpacing.Space4),
                    verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
                ) {
                    item {
                        LkSectionHeader(
                            title = model.name,
                            subtitle = model.description
                        )
                        Spacer(modifier = Modifier.height(LkSpacing.Space2))
                        Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                            LkChip(text = modelCategoryLabel(model.category))
                            if (!model.level.isNullOrBlank()) {
                                LkChip(text = model.level.replaceFirstChar { it.uppercase() })
                            }
                        }
                        if (!model.formula.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(LkSpacing.Space3))
                            LkInfoPanel(title = "Formül") {
                                Text(
                                    text = model.formula,
                                    style = LkTypography.getBody(),
                                    color = LkTextPrimary
                                )
                            }
                        }
                    }

                    if (workspaceName == null) {
                        item {
                            LkInfoPanel(title = "İşletme gerekli", icon = Icons.Default.Info) {
                                Text(
                                    text = "Bu modeli çalıştırmak için bir işletme seçmeniz gerekir.",
                                    style = LkTypography.getBodySmall(),
                                    color = LkTextSecondary
                                )
                                Spacer(modifier = Modifier.height(LkSpacing.Space3))
                                LkButton(
                                    text = "İşletme Seç",
                                    onClick = onOpenWorkspace,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    item {
                        LkSectionHeader(title = "Girdiler")
                    }

                    model.inputs.forEach { input ->
                        item {
                            LkNumericField(
                                value = inputValues[input.key] ?: "",
                                onValueChange = { newValue ->
                                    inputValues[input.key] = newValue
                                    inputErrors.remove(input.key)
                                },
                                label = input.label,
                                placeholder = if (input.type == "number_array") "Virgülle ayırarak girin" else "Değer girin",
                                error = inputErrors[input.key],
                                suffix = input.unit.ifBlank { null }
                            )
                            if (input.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = input.description,
                                    style = LkTypography.getMetadata(),
                                    color = LkTextMuted
                                )
                            }
                        }
                    }

                    item {
                        LkTextField(
                            value = scenarioName,
                            onValueChange = { scenarioName = it },
                            label = "Senaryo Adı (isteğe bağlı)",
                            placeholder = "Örn: İyimser Senaryo"
                        )
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
                            text = if (state.runResult == null) "Modeli Çalıştır" else "Yeniden Çalıştır",
                            onClick = {
                                actionError = null
                                val inputs = mutableMapOf<String, kotlinx.serialization.json.JsonElement>()
                                var valid = true
                                model.inputs.forEach { input ->
                                    val raw = inputValues[input.key]?.trim().orEmpty()
                                    if (raw.isEmpty()) {
                                        if (input.required) {
                                            inputErrors[input.key] = "Değer girin"
                                            valid = false
                                        }
                                    } else if (input.type == "number_array") {
                                        val parts = raw.split(',').map { it.trim() }
                                        val values = mutableListOf<JsonPrimitive>()
                                        parts.forEach { part ->
                                            val parsed = LkFormatting.parseDecimal(part)
                                            if (parsed == null) {
                                                inputErrors[input.key] = "Geçersiz sayı: $part"
                                                valid = false
                                            } else {
                                                values.add(JsonPrimitive(parsed))
                                            }
                                        }
                                        if (valid && values.isNotEmpty()) {
                                            inputs[input.key] = buildJsonArray { values.forEach { add(it) } }
                                        }
                                    } else {
                                        val parsed = LkFormatting.parseDecimal(raw)
                                        if (parsed == null) {
                                            inputErrors[input.key] = "Geçersiz sayı"
                                            valid = false
                                        } else {
                                            if (input.min != null && parsed < input.min) {
                                                inputErrors[input.key] = "En az ${input.min} olmalı"
                                                valid = false
                                            } else if (input.max != null && parsed > input.max) {
                                                inputErrors[input.key] = "En fazla ${input.max} olmalı"
                                                valid = false
                                            } else {
                                                inputs[input.key] = JsonPrimitive(parsed)
                                            }
                                        }
                                    }
                                }
                                if (valid && inputs.isNotEmpty()) {
                                    viewModel.run(inputs, scenarioName.trim().ifBlank { "base" }) { message ->
                                        actionError = message
                                    }
                                }
                            },
                            enabled = !state.isRunning && workspaceName != null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    state.runResult?.let { runResult ->
                        item {
                            LkInfoPanel(title = "Sonuç") {
                                runResult.outputs.forEach { (key, value) ->
                                    val definition = model.outputs.find { it.key == key }
                                    LkResultRow(
                                        label = definition?.label ?: key.replace('_', ' ').replaceFirstChar { it.uppercase() },
                                        value = value.displayValue() + if (!definition?.unit.isNullOrBlank() && value.displayValue().isNotBlank()) " ${definition.unit}" else ""
                                    )
                                    Spacer(modifier = Modifier.height(LkSpacing.Space2))
                                }
                            }
                        }

                        if (runResult.checks.isNotEmpty()) {
                            item {
                                LkInfoPanel(title = "Kontroller") {
                                    runResult.checks.forEach { check ->
                                        LkValidationCheckRowPublic(check)
                                        Spacer(modifier = Modifier.height(LkSpacing.Space2))
                                    }
                                }
                            }
                        }

                        if (runResult.warnings.isNotEmpty()) {
                            item {
                                LkInfoPanel(title = "Uyarılar", icon = Icons.Default.Warning) {
                                    runResult.warnings.forEach { warning ->
                                        Text(
                                            text = "• $warning",
                                            style = LkTypography.getBodySmall(),
                                            color = LkWarning
                                        )
                                        Spacer(modifier = Modifier.height(LkSpacing.Space2))
                                    }
                                }
                            }
                        }

                        runResult.confidence?.let { confidence ->
                            item {
                                LkInfoPanel(title = "Güven Düzeyi") {
                                    LkResultRow(
                                        label = "Güven",
                                        value = "${LkFormatting.formatNumber(confidence.score * 100)}% (${confidence.label})"
                                    )
                                    if (confidence.disclaimer.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(LkSpacing.Space2))
                                        Text(
                                            text = confidence.disclaimer,
                                            style = LkTypography.getMetadata(),
                                            color = LkTextMuted
                                        )
                                    }
                                }
                            }
                        }

                        if (runResult.trace.isNotEmpty()) {
                            item {
                                var traceExpanded by remember { mutableStateOf(false) }
                                LkInfoPanel(title = "Hesaplama Adımları") {
                                    val steps = if (traceExpanded) runResult.trace else runResult.trace.take(3)
                                    steps.forEach { step ->
                                        LkCalculationStepRowPublic(step)
                                        Spacer(modifier = Modifier.height(LkSpacing.Space2))
                                    }
                                    if (runResult.trace.size > 3) {
                                        LkButton(
                                            text = if (traceExpanded) "Daha Az Göster" else "Tüm Adımları Göster (${runResult.trace.size})",
                                            onClick = { traceExpanded = !traceExpanded },
                                            variant = com.localkarar.app.ui.components.LkButtonVariant.QUIET,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LkValidationCheckRowPublic(check: ValidationCheckDto) {
    val (icon, color) = when {
        check.passed -> Icons.Default.CheckCircle to LkSuccess
        check.severity == "error" -> Icons.Default.Error to LkDanger
        check.severity == "warning" -> Icons.Default.Warning to LkWarning
        else -> Icons.Default.Info to LkTextSecondary
    }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = check.label.ifBlank { check.code },
                style = LkTypography.getBodySmall(),
                color = LkTextPrimary
            )
            if (check.detail.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = check.detail,
                    style = LkTypography.getMetadata(),
                    color = LkTextSecondary
                )
            }
        }
    }
}

@Composable
fun LkCalculationStepRowPublic(step: CalculationStepDto) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = step.label.ifBlank { step.key },
            style = LkTypography.getBodySmall(),
            color = LkTextPrimary
        )
        if (step.formula.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = step.formula,
                style = LkTypography.getMicro(),
                color = LkTextMuted
            )
        }
        if (step.result != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "= ${step.result.displayValue()}",
                style = LkTypography.getBodyStrong(),
                color = LkPrimary
            )
        }
    }
}