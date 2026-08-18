package com.localkarar.app.ui.screens.calculations

import com.localkarar.app.ui.components.LkButton
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.calculations.RunDetailUiState
import com.localkarar.app.calculations.RunDetailViewModel
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.core.displayValue
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkInfoPanel
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.components.LkResultRow
import com.localkarar.app.ui.components.LkSectionHeader
import com.localkarar.app.ui.theme.*

@Composable
fun RunDetailScreen(
    viewModel: RunDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LkPageLayout(title = "Çalışma Detayı", onBack = onBack) {
        when (val state = uiState) {
            is RunDetailUiState.Loading -> LkLoadingState()
            is RunDetailUiState.Error -> LkErrorState(
                message = state.message,
                onRetry = { viewModel.load() }
            )
            is RunDetailUiState.Content -> {
                val run = state.run
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(LkSpacing.Space4),
                    verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
                ) {
                    item {
                        LkSectionHeader(
                            title = run.model?.name ?: "Finansal Model",
                            subtitle = run.scenarioName?.takeIf { it != "base" }?.let { "Senaryo: $it" }
                                ?: run.scenarioName
                        )
                        if (run.createdAt != null) {
                            Spacer(modifier = Modifier.height(LkSpacing.Space1))
                            Text(
                                text = LkDateUtils.formatDateTime(run.createdAt),
                                style = LkTypography.getMetadata(),
                                color = LkTextSecondary
                            )
                        }
                    }

                    item {
                        LkInfoPanel(title = "Sonuç") {
                            if (run.outputs.isEmpty()) {
                                Text(
                                    text = "Sonuç verisi bulunamadı.",
                                    style = LkTypography.getBodySmall(),
                                    color = LkTextSecondary
                                )
                            }
                            run.outputs.forEach { (key, value) ->
                                val definition = run.model?.outputs?.find { it.key == key }
                                LkResultRow(
                                    label = definition?.label ?: key.replace('_', ' ').replaceFirstChar { it.uppercase() },
                                    value = value.displayValue() + if (!definition?.unit.isNullOrBlank() && value.displayValue().isNotBlank()) " ${definition.unit}" else ""
                                )
                                Spacer(modifier = Modifier.height(LkSpacing.Space2))
                            }
                        }
                    }

                    if (run.checks.isNotEmpty()) {
                        item {
                            LkInfoPanel(title = "Kontroller") {
                                run.checks.forEach { check ->
                                    com.localkarar.app.ui.screens.calculations.LkValidationCheckRowPublic(check)
                                    Spacer(modifier = Modifier.height(LkSpacing.Space2))
                                }
                            }
                        }
                    }

                    if (run.warnings.isNotEmpty()) {
                        item {
                            LkInfoPanel(title = "Uyarılar", icon = Icons.Default.Warning) {
                                run.warnings.forEach { warning ->
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

                    if (run.calculationTrace.isNotEmpty()) {
                        item {
                            var expanded by remember { mutableStateOf(false) }
                            LkInfoPanel(title = "Hesaplama Adımları") {
                                val steps = if (expanded) run.calculationTrace else run.calculationTrace.take(3)
                                steps.forEach { step ->
                                    com.localkarar.app.ui.screens.calculations.LkCalculationStepRowPublic(step)
                                    Spacer(modifier = Modifier.height(LkSpacing.Space2))
                                }
                                if (run.calculationTrace.size > 3) {
                                    LkButton(
                                        text = if (expanded) "Daha Az Göster" else "Tüm Adımları Göster (${run.calculationTrace.size})",
                                        onClick = { expanded = !expanded },
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