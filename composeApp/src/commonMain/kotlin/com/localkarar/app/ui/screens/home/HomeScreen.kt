package com.localkarar.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.localkarar.app.home.HomeUiState
import com.localkarar.app.home.HomeViewModel
import com.localkarar.app.network.dto.DashboardResponse
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.LkSpacing

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel, 
    onNavigateToCourses: () -> Unit,
    onNavigateToCourseDetail: (Int) -> Unit
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
                        data = state.data,
                        onNavigateToCourses = onNavigateToCourses,
                        onNavigateToCourseDetail = onNavigateToCourseDetail
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
    data: DashboardResponse,
    onNavigateToCourses: () -> Unit,
    onNavigateToCourseDetail: (Int) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = LkSpacing.Space6),
        verticalArrangement = Arrangement.spacedBy(LkSpacing.Space8)
    ) {
        DashboardHeader(user = data.user)

        val firstTask = data.upcomingTasks?.firstOrNull { it.status != "completed" }
        TodayInsightWidget(
            task = firstTask,
            resumeItem = data.resumeItem,
            onActionClick = onNavigateToCourses
        )

        if (data.resumeItem != null) {
            ResumeWidget(
                item = data.resumeItem,
                onClick = { onNavigateToCourseDetail(data.resumeItem.courseId) }
            )
        }

        LearningProgressWidget(stats = data.stats)

        if (!data.recommendations.isNullOrEmpty()) {
            RecommendationsWidget(recommendations = data.recommendations)
        }

        if (!data.recentActivity.isNullOrEmpty()) {
            RecentActivityWidget(activities = data.recentActivity)
        }

        Spacer(modifier = Modifier.height(LkSpacing.Space10))
    }
}
