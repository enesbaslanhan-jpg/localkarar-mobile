package com.localkarar.app.ui.screens.decision

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.decision.DecisionToolsUiState
import com.localkarar.app.decision.DecisionToolsViewModel
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.components.decision.LkDecisionToolCard
import com.localkarar.app.ui.theme.LkSpacing
import com.localkarar.app.ui.theme.*

@Composable
fun DecisionToolsScreen(
    viewModel: DecisionToolsViewModel,
    onNavigateToSession: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var actionError by remember { mutableStateOf<String?>(null) }

    LkPageLayout(title = "Karar Araçları", onBack = onBack) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (actionError != null) {
                Text(
                    text = actionError!!,
                    color = LkDanger,
                    style = LkTypography.getBodySmall(),
                    modifier = Modifier.padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space2)
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                when (val state = uiState) {
                    is DecisionToolsUiState.Loading -> LkLoadingState()
                    is DecisionToolsUiState.Error -> LkErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadTools() }
                    )
                    is DecisionToolsUiState.Content -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(LkSpacing.Space4),
                            verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
                        ) {
                            items(state.tools) { tool ->
                                LkDecisionToolCard(
                                    title = tool.title,
                                    description = tool.description,
                                    category = tool.category,
                                    code = tool.code,
                                    status = tool.status,
                                    onClick = {
                                        if (tool.sessionId != null && tool.status != "not_started" && tool.status != null) {
                                            onNavigateToSession(tool.sessionId)
                                        } else {
                                            viewModel.startSession(
                                                code = tool.code,
                                                onSessionStarted = onNavigateToSession,
                                                onError = { actionError = it }
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


