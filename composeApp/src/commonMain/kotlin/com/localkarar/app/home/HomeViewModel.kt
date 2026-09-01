package com.localkarar.app.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.decision.DecisionRepository
import com.localkarar.app.network.dto.BusinessRecordDto
import com.localkarar.app.network.dto.DashboardResponse
import com.localkarar.app.network.dto.DecisionHistorySessionDto
import com.localkarar.app.network.dto.TrackerSummaryDto
import com.localkarar.app.workspaces.ActiveWorkspaceStore
import com.localkarar.app.workspaces.WorkspaceRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Content(
        val dashboardData: DashboardResponse,
        val trackerSummary: TrackerSummaryDto?,
        val trackerRecords: List<BusinessRecordDto>,
        val decisionHistory: List<DecisionHistorySessionDto>?,
        val activeWorkspaceId: String?
    ) : HomeUiState()
    data class Error(val message: String, val isAuthError: Boolean = false) : HomeUiState()
}

class HomeViewModel(
    private val dashboardRepository: DashboardRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val decisionRepository: DecisionRepository,
    private val activeWorkspaceStore: ActiveWorkspaceStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        // Reload dashboard whenever active workspace changes
        activeWorkspaceStore.activeWorkspaceId
            .onEach { loadDashboard() }
            .launchIn(viewModelScope)
    }

    fun loadDashboard(isRefresh: Boolean = false) {
        if (isRefresh) {
            _isRefreshing.value = true
        } else {
            _uiState.value = HomeUiState.Loading
        }

        viewModelScope.launch {
            var workspaceId = activeWorkspaceStore.activeWorkspaceId.value
            if (workspaceId == null) {
                val workspacesResult = workspaceRepository.listWorkspaces()
                if (workspacesResult.isSuccess) {
                    val list = workspacesResult.getOrThrow().workspaces
                    if (list.isNotEmpty()) {
                        val first = list.first()
                        activeWorkspaceStore.setActive(first.id, first.name)
                        workspaceId = first.id
                    }
                }
            }
            
            // Parallel fetches
            val dashboardDeferred = async { dashboardRepository.getDashboard() }
            val decisionDeferred = async { decisionRepository.getSessionHistory() }
            
            val trackerSummaryDeferred = if (workspaceId != null) {
                async { workspaceRepository.getTrackerSummary(workspaceId) }
            } else null
            
            val trackerRecordsDeferred = if (workspaceId != null) {
                async { workspaceRepository.getRecords(workspaceId) }
            } else null

            val dashboardResult = dashboardDeferred.await()
            val decisionResult = decisionDeferred.await()
            val trackerSummaryResult = trackerSummaryDeferred?.await()
            val trackerRecordsResult = trackerRecordsDeferred?.await()
            
            _isRefreshing.value = false
            
            dashboardResult.onSuccess { data ->
                _uiState.value = HomeUiState.Content(
                    dashboardData = data,
                    trackerSummary = trackerSummaryResult?.getOrNull(),
                    trackerRecords = trackerRecordsResult?.getOrNull()?.records ?: emptyList(),
                    decisionHistory = decisionResult.getOrNull(),
                    activeWorkspaceId = workspaceId
                )
            }.onFailure { exception ->
                val errorMsg = exception.message ?: "Bilinmeyen bir hata oluştu"
                val isAuthError = exception is com.localkarar.app.network.ApiError.Unauthorized || errorMsg == "UNAUTHORIZED"
                
                _uiState.value = HomeUiState.Error(
                    if (isAuthError) "Oturum süreniz doldu." else "Bağlantı hatası veya sunucuya ulaşılamıyor.",
                    isAuthError
                )
            }
        }
    }
}
