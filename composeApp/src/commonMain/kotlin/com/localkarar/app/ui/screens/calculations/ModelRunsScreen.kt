package com.localkarar.app.ui.screens.calculations

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.calculations.ModelRunsUiState
import com.localkarar.app.calculations.ModelRunsViewModel
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.ui.components.LkChip
import com.localkarar.app.ui.components.LkEmptyState
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*

@Composable
fun ModelRunsScreen(
    viewModel: ModelRunsViewModel,
    onRunSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LkPageLayout(title = "Model Çalışmaları", onBack = onBack) {
        when (val state = uiState) {
            is ModelRunsUiState.Loading -> LkLoadingState()
            is ModelRunsUiState.Error -> LkErrorState(
                message = state.message,
                onRetry = { viewModel.load() }
            )
            is ModelRunsUiState.Content -> {
                if (state.runs.isEmpty()) {
                    LkEmptyState(
                        title = "Henüz model çalışması yok",
                        description = "Finansal görünüm modellerini çalıştırdığınızda sonuçlar burada listelenir."
                    )
                    return@LkPageLayout
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(LkSpacing.Space4),
                    verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
                ) {
                    items(state.runs) { run ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(LkSurfacePanel, LkShapes.MD)
                                .border(1.dp, LkLineStrong, LkShapes.MD)
                                .clickable { onRunSelected(run.id) }
                                .padding(LkSpacing.PadPanel)
                        ) {
                            Text(
                                text = run.model?.name ?: "Finansal Model",
                                style = LkTypography.getBodyStrong(),
                                color = LkTextPrimary
                            )
                            Spacer(modifier = Modifier.height(LkSpacing.Space1))
                            Text(
                                text = LkDateUtils.formatDateTime(run.createdAt),
                                style = LkTypography.getMetadata(),
                                color = LkTextSecondary
                            )
                            if (!run.scenarioName.isNullOrBlank() && run.scenarioName != "base") {
                                Spacer(modifier = Modifier.height(LkSpacing.Space2))
                                LkChip(text = "Senaryo: ${run.scenarioName}")
                            }
                        }
                    }
                }
            }
        }
    }
}