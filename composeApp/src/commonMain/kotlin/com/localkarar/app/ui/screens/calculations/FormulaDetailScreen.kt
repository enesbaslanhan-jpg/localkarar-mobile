package com.localkarar.app.ui.screens.calculations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.calculations.FormulaCalculatorUiState
import com.localkarar.app.calculations.FormulaCalculatorViewModel
import com.localkarar.app.core.LkFormatting
import com.localkarar.app.core.displayValue
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkChip
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkInfoPanel
import com.localkarar.app.ui.components.LkNumericField
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.components.LkResultRow
import com.localkarar.app.ui.components.LkSectionHeader
import com.localkarar.app.ui.theme.*

@Composable
fun FormulaDetailScreen(
    viewModel: FormulaCalculatorViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var actionError by remember { mutableStateOf<String?>(null) }

    LkPageLayout(title = "Hızlı Hesaplama", onBack = onBack) {
        when (val state = uiState) {
            is FormulaCalculatorUiState.Error -> LkErrorState(
                message = state.message,
                onRetry = null
            )
            is FormulaCalculatorUiState.Content -> {
                val formula = state.formula
                val inputValues = remember(formula.id) {
                    mutableStateMapOf<String, String>()
                }
                val inputErrors = remember(formula.id) {
                    mutableStateMapOf<String, String>()
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(LkSpacing.Space4),
                    verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
                ) {
                    item {
                        LkSectionHeader(
                            title = formula.name,
                            subtitle = formula.description
                        )
                        Spacer(modifier = Modifier.height(LkSpacing.Space2))
                        LkChip(text = formulaCategoryLabel(formula.category))
                    }

                    if (!formula.warning.isNullOrBlank()) {
                        item {
                            LkInfoPanel(title = "Uyarı", icon = Icons.Default.Warning) {
                                Text(
                                    text = formula.warning,
                                    style = LkTypography.getBodySmall(),
                                    color = LkWarning
                                )
                            }
                        }
                    }

                    item {
                        LkSectionHeader(title = "Girdiler")
                    }

                    formula.inputs.forEach { input ->
                        item {
                            LkNumericField(
                                value = inputValues[input.name] ?: "",
                                onValueChange = { newValue ->
                                    inputValues[input.name] = newValue
                                    inputErrors.remove(input.name)
                                },
                                label = input.label,
                                placeholder = "Değer girin",
                                error = inputErrors[input.name],
                                suffix = input.unit
                            )
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
                            text = if (state.result == null) "Hesapla" else "Yeniden Hesapla",
                            onClick = {
                                actionError = null
                                val inputs = mutableMapOf<String, Double>()
                                var valid = true
                                formula.inputs.forEach { input ->
                                    val raw = inputValues[input.name]?.trim().orEmpty()
                                    if (raw.isEmpty()) {
                                        inputErrors[input.name] = "Değer girin"
                                        valid = false
                                    } else {
                                        val parsed = LkFormatting.parseDecimal(raw)
                                        if (parsed == null) {
                                            inputErrors[input.name] = "Geçersiz sayı"
                                            valid = false
                                        } else {
                                            if (input.min != null && parsed < input.min) {
                                                inputErrors[input.name] = "En az ${input.min} olmalı"
                                                valid = false
                                            } else if (input.max != null && parsed > input.max) {
                                                inputErrors[input.name] = "En fazla ${input.max} olmalı"
                                                valid = false
                                            } else {
                                                inputs[input.name] = parsed
                                            }
                                        }
                                    }
                                }
                                if (valid) {
                                    viewModel.calculate(inputs) { message ->
                                        actionError = message
                                    }
                                }
                            },
                            enabled = !state.isCalculating,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (state.result != null) {
                        item {
                            LkInfoPanel(title = "Sonuç") {
                                state.result.result.forEach { (key, value) ->
                                    LkResultRow(
                                        label = key.replace('_', ' ').replaceFirstChar { it.uppercase() },
                                        value = value.displayValue()
                                    )
                                    Spacer(modifier = Modifier.height(LkSpacing.Space2))
                                }
                                state.result.warnings.forEach { warning ->
                                    Spacer(modifier = Modifier.height(LkSpacing.Space2))
                                    Text(
                                        text = warning,
                                        style = LkTypography.getMetadata(),
                                        color = LkWarning
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