package com.localkarar.app.ui.screens.decision

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.localkarar.app.decision.DecisionToolsUiState
import com.localkarar.app.decision.DecisionToolsViewModel
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.components.decision.LkDecisionToolCard
import com.localkarar.app.ui.theme.*
import com.localkarar.app.network.dto.DecisionCheckListDto

@Composable
fun DecisionToolsScreen(
    viewModel: DecisionToolsViewModel,
    onNavigateToSession: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var actionError by remember { mutableStateOf<String?>(null) }

    LkPageLayout(
        title = "Karar Araçları", 
        onBack = onBack,
        actions = {
            androidx.compose.material.TextButton(onClick = { viewModel.updateStatusFilter("completed") }) {
                Text("Geçmiş kararlar", color = LkPrimary, style = LkTypography.getBodySmall())
            }
        }
    ) {
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
                        val filtersActive = state.statusFilter != "all" || state.searchQuery.isNotBlank()

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(LkSpacing.Space4),
                            verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
                        ) {
                            // Hero Panel
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(LkSurfaceSignature)
                                        .padding(24.dp)
                                ) {
                                    Text(
                                        text = "Karar öncesi kontrol",
                                        style = LkTypography.getMicro(),
                                        color = LkOnSignatureDim
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Karar vermeden önce rakamlara bakın",
                                        style = LkTypography.getSectionTitle(),
                                        color = LkOnSignature
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Önemli iş kararlarını vermeden önce temel riskleri, maliyetleri ve sonraki adımları hızlıca kontrol edin.",
                                        style = LkTypography.getBody(),
                                        color = LkOnSignatureDim
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    // Search Bar
                                    androidx.compose.material.OutlinedTextField(
                                        value = state.searchQuery,
                                        onValueChange = { viewModel.updateSearchQuery(it) },
                                        placeholder = { Text("Araç ara", color = LkTextMuted) },
                                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = LkTextMuted) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        colors = androidx.compose.material.TextFieldDefaults.outlinedTextFieldColors(
                                            backgroundColor = LkSurfacePanel,
                                            textColor = LkTextPrimary,
                                            cursorColor = LkPrimary,
                                            focusedBorderColor = LkLineStrong,
                                            unfocusedBorderColor = LkLineSoft
                                        ),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                }
                            }

                            // Status Filters
                            item {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                ) {
                                    val filters = listOf(
                                        "all" to "Tümü",
                                        "in_progress" to "Devam eden",
                                        "completed" to "Tamamlanan"
                                    )
                                    val counts = mapOf(
                                        "all" to state.allTools.size,
                                        "in_progress" to state.allTools.count { it.status == "in_progress" || it.status == "started" },
                                        "completed" to state.allTools.count { it.status == "completed" || it.status == "complete" }
                                    )

                                    items(filters) { (id, label) ->
                                        val isActive = state.statusFilter == id
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(if (isActive) LkPrimary else LkSurfacePanel)
                                                .clickable { viewModel.updateStatusFilter(id) }
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = label,
                                                    style = LkTypography.getBodySmall(),
                                                    color = if (isActive) LkOnPrimary else LkTextPrimary
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(if (isActive) LkSurfaceCanvas.copy(alpha = 0.2f) else LkSurfaceCanvas)
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = (counts[id] ?: 0).toString(),
                                                        style = LkTypography.getMicro(),
                                                        color = if (isActive) LkOnPrimary else LkTextMuted
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Summary
                            item {
                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                    Text(
                                        text = "İşinize uygun araçlar",
                                        style = LkTypography.getSectionTitle(),
                                        color = LkTextPrimary
                                    )
                                    Text(
                                        text = if (filtersActive) "${state.visibleTools.size} / ${state.allTools.size} araç gösteriliyor." else "${state.allTools.size} karar aracı kullanıma hazır.",
                                        style = LkTypography.getBody(),
                                        color = LkTextSecondary
                                    )
                                }
                            }

                            // Recommended Tool
                            if (state.visibleTools.size > 1 && !filtersActive) {
                                item {
                                    val recommended = state.visibleTools[0]
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(LkSurfaceRaised)
                                            .clickable {
                                                handleToolClick(recommended, viewModel, onNavigateToSession) { actionError = it }
                                            }
                                            .padding(16.dp)
                                    ) {
                                        Text("Bağlamınıza göre önerilen", style = LkTypography.getMicro(), color = LkPrimary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(recommended.title, style = LkTypography.getCardTitle(), color = LkTextPrimary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(recommended.description, style = LkTypography.getBodySmall(), color = LkTextSecondary)
                                    }
                                }
                            }

                            if (state.visibleTools.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                        Text("Bu süzgece uyan araç yok.", color = LkTextMuted)
                                    }
                                }
                            }

                            items(state.visibleTools) { tool ->
                                LkDecisionToolCard(
                                    title = tool.title,
                                    description = tool.description,
                                    category = tool.category ?: "",
                                    code = tool.code,
                                    status = tool.status,
                                    onClick = {
                                        handleToolClick(tool, viewModel, onNavigateToSession) { actionError = it }
                                    }
                                )
                            }
                            
                            // Recent Sessions
                            val recent = state.allTools.filter { it.sessionId != null }.take(5)
                            if (recent.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text("Son oturumlar", style = LkTypography.getSectionTitle(), color = LkTextPrimary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                items(recent) { tool ->
                                    val isCompleted = tool.status == "completed" || tool.status == "complete"
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(LkSurfacePanel)
                                            .clickable { handleToolClick(tool, viewModel, onNavigateToSession) { actionError = it } }
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(tool.title, style = LkTypography.getBody(), color = LkTextPrimary)
                                            Text(if (isCompleted) "Sonuç hazır" else "Sürdürmeye hazır", style = LkTypography.getMicro(), color = LkTextMuted)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(if (isCompleted) "Aç" else "Sürdür", style = LkTypography.getMicro(), color = LkPrimary)
                                            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = LkPrimary, modifier = Modifier.size(16.dp))
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
}

private fun handleToolClick(
    tool: DecisionCheckListDto,
    viewModel: DecisionToolsViewModel,
    onNavigateToSession: (String) -> Unit,
    onError: (String) -> Unit
) {
    if (tool.sessionId != null && tool.status != "not_started" && tool.status != null) {
        onNavigateToSession(tool.sessionId)
    } else {
        viewModel.startSession(
            code = tool.code,
            onSessionStarted = onNavigateToSession,
            onError = onError
        )
    }
}
