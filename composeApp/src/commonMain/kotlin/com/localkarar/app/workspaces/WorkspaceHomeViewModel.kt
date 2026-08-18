package com.localkarar.app.workspaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.TrackerSummaryDto
import com.localkarar.app.network.dto.WorkspaceDetailDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class WorkspaceHomeUiState {
    object Loading : WorkspaceHomeUiState()
    data class Content(
        val workspace: WorkspaceDetailDto,
        val summary: TrackerSummaryDto? = null,
        val summaryFailed: Boolean = false
    ) : WorkspaceHomeUiState()
    data class Error(val message: String) : WorkspaceHomeUiState()
}

class WorkspaceHomeViewModel(
    private val workspaceId: String,
    private val repository: WorkspaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WorkspaceHomeUiState>(WorkspaceHomeUiState.Loading)
    val uiState: StateFlow<WorkspaceHomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = WorkspaceHomeUiState.Loading
        viewModelScope.launch {
            val workspaceResult = repository.getWorkspace(workspaceId)
            if (workspaceResult.isFailure) {
                _uiState.value = WorkspaceHomeUiState.Error(
                    workspaceResult.exceptionOrNull()?.message ?: "İşletme yüklenemedi."
                )
                return@launch
            }
            val workspace = workspaceResult.getOrThrow()
            val summaryResult = repository.getTrackerSummary(workspaceId)
            _uiState.value = WorkspaceHomeUiState.Content(
                workspace = workspace,
                summary = summaryResult.getOrNull(),
                summaryFailed = summaryResult.isFailure
            )
        }
    }
}