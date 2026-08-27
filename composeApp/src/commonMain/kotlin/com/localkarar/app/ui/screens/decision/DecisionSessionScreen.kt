package com.localkarar.app.ui.screens.decision

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.decision.DecisionSessionUiState
import com.localkarar.app.decision.DecisionSessionViewModel
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.components.decision.LkDecisionInput
import com.localkarar.app.ui.components.decision.LkDecisionResultPanel
import com.localkarar.app.ui.theme.LkSpacing
import com.localkarar.app.ui.theme.*
import kotlinx.serialization.json.JsonElement

@Composable
fun DecisionSessionScreen(
    viewModel: DecisionSessionViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var actionError by remember { mutableStateOf<String?>(null) }

    LkPageLayout(title = "Karar Aracı", onBack = onBack) {
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
                    is DecisionSessionUiState.Loading -> LkLoadingState()
                    is DecisionSessionUiState.Error -> LkErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadSession() }
                    )
                    is DecisionSessionUiState.Content -> {
                        val session = state.session
                        
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(LkSpacing.Space4),
                            verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
                        ) {
                            item {
                                Text(
                                    text = session.decisionCheckTitle,
                                    style = LkTypography.getPageTitle(),
                                    color = LkTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = session.decisionCheckDescription,
                                    style = LkTypography.getBody(),
                                    color = LkTextSecondary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            if (state.result != null) {
                                item {
                                    LkDecisionResultPanel(
                                        snapshot = state.result.snapshot,
                                        toolCode = session.decisionCheckCode,
                                        onRestart = {
                                            actionError = null
                                            viewModel.restartSession(session.decisionCheckCode, onError = { actionError = it })
                                        },
                                        onListClick = onBack
                                    )
                                }
                            } else {
                                items(session.definition) { question ->
                                    val currentAnswer = session.answers.find { it.questionCode == question.code }
                                    LkDecisionInput(
                                        question = question,
                                        value = currentAnswer?.valueJson,
                                        isUnknown = currentAnswer?.isUnknown == true,
                                        error = state.errors[question.code],
                                        onValueChange = { newValue ->
                                            viewModel.updateAnswer(question.code, newValue, currentAnswer?.isUnknown == true)
                                        },
                                        onUnknownChange = { unknown ->
                                            viewModel.updateAnswer(question.code, currentAnswer?.valueJson, unknown)
                                        }
                                    )
                                }
                                
                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    LkButton(
                                        text = "Sonucu hesapla",
                                        onClick = { 
                                            actionError = null
                                            viewModel.completeSession(onError = { actionError = it }) 
                                        },
                                        enabled = !state.isSubmitting,
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



