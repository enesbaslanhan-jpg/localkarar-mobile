package com.localkarar.app.ui.screens.decision

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.localkarar.app.decision.DecisionToolUiState
import com.localkarar.app.decision.DecisionToolViewModel
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkPageLayout

@Composable
fun DecisionToolScreen(
    viewModel: DecisionToolViewModel,
    onSessionReady: (sessionId: String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is DecisionToolUiState.Ready) {
            onSessionReady(state.sessionId)
        }
    }

    LkPageLayout(
        title = "Karar Aracı",
        onBack = onBack
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is DecisionToolUiState.Loading,
                is DecisionToolUiState.Ready -> LkLoadingState()
                is DecisionToolUiState.Error -> LkErrorState(
                    message = state.message,
                    onRetry = { viewModel.startOrResume() }
                )
            }
        }
    }
}
