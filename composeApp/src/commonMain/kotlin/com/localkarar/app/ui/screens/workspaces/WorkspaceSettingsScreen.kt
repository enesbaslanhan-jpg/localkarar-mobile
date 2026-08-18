package com.localkarar.app.ui.screens.workspaces

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkChip
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.components.LkSectionHeader
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.WorkspaceSettingsUiState
import com.localkarar.app.workspaces.WorkspaceSettingsViewModel

private val CURRENCY_OPTIONS = listOf("TRY", "USD", "EUR")

@Composable
fun WorkspaceSettingsScreen(
    viewModel: WorkspaceSettingsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var actionError by remember { mutableStateOf<String?>(null) }
    var savedNotice by remember { mutableStateOf(false) }

    LkPageLayout(title = "İşletme Ayarları", onBack = onBack) {
        when (val state = uiState) {
            is WorkspaceSettingsUiState.Loading -> LkLoadingState()
            is WorkspaceSettingsUiState.Error -> LkErrorState(
                message = state.message,
                onRetry = { viewModel.load() }
            )
            is WorkspaceSettingsUiState.Content -> {
                var currency by remember(state.settings.id) { mutableStateOf(state.settings.defaultCurrency) }
                var timezone by remember(state.settings.id) { mutableStateOf(state.settings.timezone) }
                var locale by remember(state.settings.id) { mutableStateOf(state.settings.locale) }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(LkSpacing.Space4),
                    verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
                ) {
                    item {
                        LkSectionHeader(title = "Para Birimi")
                        Spacer(modifier = Modifier.height(LkSpacing.Space2))
                        Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                            CURRENCY_OPTIONS.forEach { option ->
                                LkChip(
                                    text = option,
                                    background = if (currency == option) LkPrimary else LkSurfaceRaised,
                                    contentColor = if (currency == option) LkOnPrimary else LkTextSecondary,
                                    modifier = Modifier.clickable { currency = option }
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Saat Dilimi",
                            style = LkTypography.getBodyStrong().copy(color = LkTextSecondary)
                        )
                        Spacer(modifier = Modifier.height(LkSpacing.Space2))
                        Text(
                            text = timezone,
                            style = LkTypography.getBody(),
                            color = LkTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Saat dilimi değişikliği web sürümünden yapılabilir.",
                            style = LkTypography.getMetadata(),
                            color = LkTextMuted
                        )
                    }

                    item {
                        Text(
                            text = "Dil / Bölge",
                            style = LkTypography.getBodyStrong().copy(color = LkTextSecondary)
                        )
                        Spacer(modifier = Modifier.height(LkSpacing.Space2))
                        Text(
                            text = locale,
                            style = LkTypography.getBody(),
                            color = LkTextPrimary
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
                        if (savedNotice) {
                            Text(
                                text = "Ayarlar kaydedildi.",
                                style = LkTypography.getBodySmall(),
                                color = LkSuccess
                            )
                            Spacer(modifier = Modifier.height(LkSpacing.Space2))
                        }
                        LkButton(
                            text = "Kaydet",
                            onClick = {
                                actionError = null
                                savedNotice = false
                                viewModel.save(
                                    defaultCurrency = currency,
                                    timezone = timezone,
                                    locale = locale,
                                    weekStartsOn = 1,
                                    onError = { actionError = it }
                                )
                                savedNotice = true
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