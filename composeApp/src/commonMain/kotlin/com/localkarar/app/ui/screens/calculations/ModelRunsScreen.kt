package com.localkarar.app.ui.screens.calculations

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.ChevronRight
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
import com.localkarar.app.ui.components.LkHeroPage
import com.localkarar.app.ui.components.LkHairline
import com.localkarar.app.ui.components.LkListRow
import com.localkarar.app.ui.components.LkRowGroup
import com.localkarar.app.ui.theme.*

@Composable
fun ModelRunsScreen(
    viewModel: ModelRunsViewModel,
    onRunSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LkHeroPage(
        title = "Çalıştırmalar",
        onBack = onBack,
        heroExtra = {
            val count = (uiState as? ModelRunsUiState.Content)?.runs?.size
            count?.let { Text("$it kayıt", style = LkTypography.getMetadata(), color = LkHero.OnHeroSecondary,
                modifier = Modifier.padding(start = LkSpacing.Space5, top = LkSpacing.Space2)) }
        }
    ) {
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
                    return@LkHeroPage
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(LkSpacing.Space4),
                    verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
                ) {
                    item {
                        LkRowGroup {
                            state.runs.forEachIndexed { index, run ->
                                LkListRow(
                                    baslik = run.model?.name ?: "Finansal Model",
                                    altBaslik = listOfNotNull(
                                        LkDateUtils.formatDateTime(run.createdAt),
                                        run.scenarioName?.takeIf { it != "base" }?.let { "Senaryo: $it" }
                                    ).joinToString(" · "),
                                    onClick = { onRunSelected(run.id) },
                                    ikon = { Icon(Icons.Outlined.Calculate, null, tint = LkPrimary) },
                                    sag = { Icon(Icons.Outlined.ChevronRight, null, tint = LkTextSecondary) }
                                )
                                if (index < state.runs.lastIndex) LkHairline()
                            }
                        }
                    }
                }
            }
        }
    }
}
