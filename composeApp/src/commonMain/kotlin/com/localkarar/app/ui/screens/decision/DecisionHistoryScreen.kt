package com.localkarar.app.ui.screens.decision

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.decision.DecisionHistoryViewModel
import com.localkarar.app.network.dto.DecisionHistorySessionDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.LkDanger
import com.localkarar.app.ui.theme.LkPrimary
import com.localkarar.app.ui.theme.LkSurfacePanel
import com.localkarar.app.ui.theme.LkTextPrimary
import com.localkarar.app.ui.theme.LkTextSecondary
import com.localkarar.app.ui.theme.LkTypography

@Composable
fun DecisionHistoryScreen(
    viewModel: DecisionHistoryViewModel,
    onOpenSession: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LkPageLayout(title = "Karar Geçmişi", onBack = null) {
        when (val state = uiState) {
            is DecisionHistoryViewModel.UiState.Loading -> LkLoadingState()
            is DecisionHistoryViewModel.UiState.Error -> LkErrorState(
                message = state.message,
                onRetry = { viewModel.refresh() }
            )
            is DecisionHistoryViewModel.UiState.Content -> {
                if (state.sessions.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Henüz karar oturumu yok",
                            style = LkTypography.getBodyStrong(),
                            color = LkTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Karar Araçları bölümünden bir karar aracı başlatın.",
                            style = LkTypography.getBodySmall(),
                            color = LkTextSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.sessions, key = { it.id }) { session ->
                            HistoryCard(
                                session = session,
                                onClick = { onOpenSession(session.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(
    session: DecisionHistorySessionDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        backgroundColor = LkSurfacePanel
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = session.decisionCheckTitle,
                style = LkTypography.getBodyStrong(),
                color = LkTextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Kod: ${session.decisionCheckCode}",
                style = LkTypography.getMetadata(),
                color = LkTextSecondary
            )
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(status = session.status)
                Spacer(Modifier.weight(1f))
                Text(
                    text = LkDateUtils.formatDateTime(session.updatedAt),
                    style = LkTypography.getMetadata(),
                    color = LkTextSecondary
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val (label, color) = when (status) {
        "completed" -> "Tamamlandı" to LkPrimary
        "in_progress" -> "Devam ediyor" to androidx.compose.ui.graphics.Color(0xFFF9A825)
        else -> status to LkDanger
    }
    Box(
        modifier = Modifier
            .padding(vertical = 2.dp)
            .let { it }
    ) {
        Row {
            Text(
                text = label,
                style = LkTypography.getMetadata(),
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
