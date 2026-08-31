package com.localkarar.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.localkarar.app.home.HomeUiState
import com.localkarar.app.home.HomeViewModel
import com.localkarar.app.network.dto.BusinessRecordDto
import com.localkarar.app.network.dto.DecisionHistorySessionDto
import com.localkarar.app.network.dto.TrackerSummaryDto
import com.localkarar.app.network.dto.DashboardResponse
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*

fun formatMoney(amount: Double?): String {
    if (amount == null) return "₺0"
    return "₺${amount.toInt()}" // Simplified formatter for parity matching
}

fun shortDate(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return ""
    return dateStr.take(10) // Fallback for simple display
}

fun priorityLevel(raw: String?): String {
    val v = raw?.lowercase() ?: ""
    if (v in listOf("high", "urgent", "critical", "yüksek", "yuksek")) return "high"
    if (v in listOf("low", "düşük", "dusuk")) return "low"
    return "medium"
}

val PRIORITY_LABEL = mapOf("low" to "Düşük", "medium" to "Orta", "high" to "Yüksek")

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel, 
    onNavigateToCourses: () -> Unit,
    onNavigateToCourseDetail: (Int) -> Unit,
    onNavigateToCalculations: () -> Unit,
    onNavigateToMentor: () -> Unit,
    onNavigateToDecisions: () -> Unit,
    onNavigateToDecisionDetail: (String) -> Unit,
    onNavigateToWorkspaces: () -> Unit,
    onNavigateToTracker: (String) -> Unit,
    onNavigateToEnrollments: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.loadDashboard(isRefresh = true) }
    )

    LkPageLayout {
        Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    LkLoadingState()
                }
                is HomeUiState.Error -> {
                    LkErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadDashboard() }
                    )
                }
                is HomeUiState.Content -> {
                    DashboardContent(
                        state = state,
                        onNavigateToCourses = onNavigateToCourses,
                        onNavigateToCourseDetail = onNavigateToCourseDetail,
                        onNavigateToCalculations = onNavigateToCalculations,
                        onNavigateToMentor = onNavigateToMentor,
                        onNavigateToDecisions = onNavigateToDecisions,
                        onNavigateToDecisionDetail = onNavigateToDecisionDetail,
                        onNavigateToWorkspaces = onNavigateToWorkspaces,
                        onNavigateToTracker = onNavigateToTracker,
                        onNavigateToEnrollments = onNavigateToEnrollments
                    )
                }
            }
            
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun DashboardContent(
    state: HomeUiState.Content,
    onNavigateToCourses: () -> Unit,
    onNavigateToCourseDetail: (Int) -> Unit,
    onNavigateToCalculations: () -> Unit,
    onNavigateToMentor: () -> Unit,
    onNavigateToDecisions: () -> Unit,
    onNavigateToDecisionDetail: (String) -> Unit,
    onNavigateToWorkspaces: () -> Unit,
    onNavigateToTracker: (String) -> Unit,
    onNavigateToEnrollments: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = LkSpacing.Space6),
        verticalArrangement = Arrangement.spacedBy(LkSpacing.Space8)
    ) {
        // PageHead
        Column(modifier = Modifier.padding(top = LkSpacing.Space6)) {
            Text("Kontrol Merkezi", style = LkTypography.getPageTitle(), color = LkTextPrimary)
            Text("Bugün işletmenizde ne önemli?", style = LkTypography.getBody(), color = LkTextSecondary)
            Spacer(modifier = Modifier.height(LkSpacing.Space4))
            Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)) {
                OutlinedButton(onClick = onNavigateToCalculations, modifier = Modifier.weight(1f)) {
                    Text("Hesapla", style = LkTypography.getBodySmall(), maxLines = 1)
                }
                OutlinedButton(onClick = onNavigateToMentor, modifier = Modifier.weight(1f)) {
                    Text("Mentor", style = LkTypography.getBodySmall(), maxLines = 1)
                }
                Button(onClick = onNavigateToDecisions, modifier = Modifier.weight(1f)) {
                    Text("Karar Ver", style = LkTypography.getBodySmall(), maxLines = 1)
                }
            }
        }

        // StatusPanel (Bugünkü Durum)
        StatusPanel(
            tracker = state.trackerSummary,
            onNavigateToWorkspaces = onNavigateToWorkspaces
        )

        // TasksPanel (Sıradaki işler)
        TasksPanel(
            records = state.trackerRecords,
            upcomingTasks = state.dashboardData.upcomingTasks,
            activeWorkspaceId = state.activeWorkspaceId,
            onNavigateToTracker = onNavigateToTracker
        )

        // ResumePanel (Kaldığın yer)
        ResumePanel(
            resumeItem = state.dashboardData.resumeItem,
            onNavigateToCourses = onNavigateToCourses,
            onNavigateToEnrollments = onNavigateToEnrollments
        )

        // DecisionsPanel (Son kararlar)
        DecisionsPanel(
            decisionHistory = state.decisionHistory,
            onNavigateToDecisions = onNavigateToDecisions,
            onNavigateToDecisionDetail = onNavigateToDecisionDetail
        )

        Spacer(modifier = Modifier.height(LkSpacing.Space10))
    }
}

@Composable
private fun StatusPanel(tracker: TrackerSummaryDto?, onNavigateToWorkspaces: () -> Unit) {
    val overdue = tracker?.counts?.overdue ?: 0
    val net = tracker?.nextThirtyDays?.net ?: 0.0

    val statusHeadline = if (tracker != null) {
        if (overdue > 0) "İşletmeniz dengeli, $overdue konu dikkat istiyor."
        else if (net < 0) "Önümüzdeki 30 gün için nakit planı gerekiyor."
        else "İşletmeniz dengeli, takip düzenli ilerliyor."
    } else "İşletme görünümünüzü kurarak başlayın."

    var statusSentence = "Gerçek işletme metrikleri için işletme profilinizi ve takip kayıtlarınızı oluşturun."
    if (tracker != null) {
        val parts = mutableListOf<String>()
        if (net < 0) parts.add("önümüzdeki 30 günde ${formatMoney(kotlin.math.abs(net))} nakit açığın görünüyor")
        else parts.add("önümüzdeki 30 günde ${formatMoney(net)} net nakit girişin görünüyor")
        
        if (overdue > 0) parts.add("$overdue kayıt gecikmiş durumda")
        else parts.add("geciken kaydın yok")
        
        statusSentence = parts.joinToString(", ") + "."
        statusSentence = statusSentence.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    // Equivalent to DarkPanel
    Surface(
        color = LkSurfacePanel,
        shape = LkShapes.MD,
        elevation = 2.dp,
        modifier = Modifier.fillMaxWidth().border(1.dp, LkLineStrong, LkShapes.MD)
    ) {
        Column(modifier = Modifier.padding(LkSpacing.Space6)) {
            Text("Bugünkü durum", style = LkTypography.getBodySmall(), color = LkTextSecondary)
            Spacer(modifier = Modifier.height(LkSpacing.Space2))
            Text(statusHeadline, style = LkTypography.getSectionTitle(), color = LkTextPrimary)
            Spacer(modifier = Modifier.height(LkSpacing.Space2))
            Text(statusSentence, style = LkTypography.getBody(), color = LkTextSecondary)
            
            Spacer(modifier = Modifier.height(LkSpacing.Space6))
            
            if (tracker != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Tahsilat", style = LkTypography.getBodySmall(), color = LkTextSecondary)
                        Text(formatMoney(tracker.nextThirtyDays.receivable), style = LkTypography.getMetric(), color = LkTextPrimary)
                        Text("30 gün", style = LkTypography.getMicro(), color = LkTextSecondary)
                    }
                    Column {
                        Text("Ödeme", style = LkTypography.getBodySmall(), color = LkTextSecondary)
                        Text(formatMoney(tracker.nextThirtyDays.payable), style = LkTypography.getMetric(), color = LkTextPrimary)
                        Text("30 gün", style = LkTypography.getMicro(), color = LkTextSecondary)
                    }
                    Column {
                        Text("Net görünüm", style = LkTypography.getBodySmall(), color = LkTextSecondary)
                        Text(formatMoney(net), style = LkTypography.getMetric(), color = if (net < 0) LkDanger else LkTextPrimary)
                        Text(if (net < 0) "Planlama gerekli" else "Olumlu", style = LkTypography.getMicro(), color = LkTextSecondary)
                    }
                }
            } else {
                OutlinedButton(onClick = onNavigateToWorkspaces) {
                    Text("İşletmeyi kur")
                }
            }
        }
    }
}

@Composable
private fun TasksPanel(
    records: List<BusinessRecordDto>,
    upcomingTasks: List<com.localkarar.app.network.dto.UpcomingTaskDto>?,
    activeWorkspaceId: String?,
    onNavigateToTracker: (String) -> Unit
) {
    Surface(
        color = LkSurfacePanel,
        shape = LkShapes.MD,
        modifier = Modifier.fillMaxWidth().border(1.dp, LkLineSoft, LkShapes.MD)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(LkSpacing.Space6),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sıradaki işler", style = LkTypography.getSectionTitle(), color = LkTextPrimary)
                if (activeWorkspaceId != null) {
                    Text(
                        "Tümünü gör", 
                        style = LkTypography.getBodySmall(), 
                        color = LkPrimary,
                        modifier = Modifier.clickable { onNavigateToTracker(activeWorkspaceId) }
                    )
                }
            }
            Divider(color = LkLineSoft)
            
            val validRecords = records.filter { it.status != "completed" && it.status != "cancelled" }.take(3)
            val taskRows = if (validRecords.isNotEmpty()) {
                validRecords.map { r ->
                    mapOf(
                        "id" to r.id,
                        "title" to r.title,
                        "done" to (r.status == "completed"),
                        "priority" to priorityLevel(r.priority),
                        "date" to shortDate(r.dueAt),
                        "kind" to (mapOf("payment" to "Ödeme", "receivable" to "Tahsilat", "promissory_note" to "Senet", "purchase" to "Satın alma", "shipment" to "Sevkiyat", "task" to "Görev", "deferred" to "Ertelenen", "other" to "Kayıt")[r.type] ?: "Kayıt")
                    )
                }
            } else {
                (upcomingTasks ?: emptyList()).take(3).map { t ->
                    mapOf(
                        "id" to t.id,
                        "title" to t.title,
                        "done" to (t.status == "completed"),
                        "priority" to null,
                        "date" to shortDate(t.updatedAt ?: t.createdAt),
                        "kind" to "Öğrenme"
                    )
                }
            }

            if (taskRows.isEmpty()) {
                Text("Şu an sırada bir iş yok.", modifier = Modifier.padding(LkSpacing.Space6), style = LkTypography.getBody(), color = LkTextSecondary)
            } else {
                taskRows.forEach { row ->
                    val done = row["done"] as Boolean
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { if (activeWorkspaceId != null) onNavigateToTracker(activeWorkspaceId) }
                            .padding(horizontal = LkSpacing.Space6, vertical = LkSpacing.Space4),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, 
                            contentDescription = null,
                            tint = if (done) LkSuccess else LkTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(LkSpacing.Space3))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(row["title"] as String, style = LkTypography.getBodyStrong(), color = LkTextPrimary)
                            Text(row["date"] as String, style = LkTypography.getMicro(), color = LkTextSecondary)
                        }
                        Text(row["kind"] as String, style = LkTypography.getMicro(), color = LkTextSecondary)
                        Spacer(modifier = Modifier.width(LkSpacing.Space2))
                        val prio = row["priority"] as? String
                        if (prio != null) {
                            val prioColor = when (prio) {
                                "high" -> LkDanger
                                "low" -> LkSuccess
                                else -> LkWarning
                            }
                            Text(PRIORITY_LABEL[prio] ?: "Orta", style = LkTypography.getMicro(), color = prioColor, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(LkSpacing.Space2))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = LkTextSecondary, modifier = Modifier.size(16.dp))
                    }
                    Divider(color = LkLineSoft)
                }
            }
        }
    }
}

@Composable
private fun ResumePanel(
    resumeItem: com.localkarar.app.network.dto.ResumeItemDto?,
    onNavigateToCourses: () -> Unit,
    onNavigateToEnrollments: () -> Unit
) {
    Surface(
        color = LkSurfacePanel,
        shape = LkShapes.MD,
        modifier = Modifier.fillMaxWidth().border(1.dp, LkLineSoft, LkShapes.MD)
    ) {
        Column {
            Text("Kaldığın yer", style = LkTypography.getSectionTitle(), color = LkTextPrimary, modifier = Modifier.padding(LkSpacing.Space6))
            Divider(color = LkLineSoft)
            
            if (resumeItem != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToEnrollments() }.padding(LkSpacing.Space6),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(LkShapes.SM).background(LkPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("LK", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(LkSpacing.Space4))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ÖĞRENMEYE DEVAM", style = LkTypography.getMicro(), color = LkPrimary)
                        Text(resumeItem.courseTitle, style = LkTypography.getBodyStrong(), color = LkTextPrimary)
                        Spacer(modifier = Modifier.height(LkSpacing.Space2))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LinearProgressIndicator(
                                progress = (resumeItem.progress / 100f).coerceIn(0f, 1f),
                                modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = LkPrimary,
                                backgroundColor = LkLineSoft
                            )
                            Spacer(modifier = Modifier.width(LkSpacing.Space2))
                            Text("%${resumeItem.progress}", style = LkTypography.getMicro(), color = LkTextSecondary)
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.padding(LkSpacing.Space6)) {
                    Text("Yeni bir öğrenme rotası seçin.", style = LkTypography.getBodyStrong(), color = LkTextPrimary)
                    Text("İlerlemeniz burada kaldığınız yerden devam edecek.", style = LkTypography.getBody(), color = LkTextSecondary)
                    Spacer(modifier = Modifier.height(LkSpacing.Space4))
                    OutlinedButton(onClick = onNavigateToCourses) {
                        Text("Kurslara git")
                    }
                }
            }
        }
    }
}

@Composable
private fun DecisionsPanel(
    decisionHistory: List<DecisionHistorySessionDto>?,
    onNavigateToDecisions: () -> Unit,
    onNavigateToDecisionDetail: (String) -> Unit
) {
    Surface(
        color = LkSurfacePanel,
        shape = LkShapes.MD,
        modifier = Modifier.fillMaxWidth().border(1.dp, LkLineSoft, LkShapes.MD)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(LkSpacing.Space6),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Son kararlar", style = LkTypography.getSectionTitle(), color = LkTextPrimary)
                Text(
                    "Tümünü gör", 
                    style = LkTypography.getBodySmall(), 
                    color = LkPrimary,
                    modifier = Modifier.clickable { onNavigateToDecisions() }
                )
            }
            Divider(color = LkLineSoft)
            
            val validSessions = decisionHistory?.filter { it.status == "completed" && it.completedAt != null }
                ?.sortedByDescending { it.completedAt }?.take(4) ?: emptyList()
                
            if (validSessions.isEmpty()) {
                Text("Henüz tamamlanmış bir karar yok.", modifier = Modifier.padding(LkSpacing.Space6), style = LkTypography.getBody(), color = LkTextSecondary)
            } else {
                validSessions.forEachIndexed { index, session ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                // The web app opens receipt for the first one, but for simplicity we navigate to the detail/receipt logic
                                onNavigateToDecisionDetail(session.id) 
                            }
                            .padding(horizontal = LkSpacing.Space6, vertical = LkSpacing.Space4),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = LkPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(LkSpacing.Space3))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(session.decisionCheckTitle, style = LkTypography.getBodyStrong(), color = LkTextPrimary)
                            Text(shortDate(session.completedAt), style = LkTypography.getMicro(), color = LkTextSecondary)
                        }
                        Text("Karar", style = LkTypography.getMicro(), color = LkTextSecondary)
                        Spacer(modifier = Modifier.width(LkSpacing.Space3))
                        Text(if (index == 0) "İncele" else "Tamam", style = LkTypography.getMicro(), color = LkTextSecondary)
                        Spacer(modifier = Modifier.width(LkSpacing.Space2))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = LkTextSecondary, modifier = Modifier.size(16.dp))
                    }
                    Divider(color = LkLineSoft)
                }
            }
        }
    }
}
