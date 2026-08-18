package com.localkarar.app.workspaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.WorkspaceActivityDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ActivityUiState {
    object Loading : ActivityUiState()
    data class Content(val items: List<WorkspaceActivityDto>) : ActivityUiState()
    data class Error(val message: String) : ActivityUiState()
}

class ActivityViewModel(
    private val workspaceId: String,
    private val repository: WorkspaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ActivityUiState>(ActivityUiState.Loading)
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = ActivityUiState.Loading
        viewModelScope.launch {
            val result = repository.getActivity(workspaceId)
            if (result.isSuccess) {
                _uiState.value = ActivityUiState.Content(result.getOrThrow().items)
            } else {
                _uiState.value = ActivityUiState.Error(
                    result.exceptionOrNull()?.message ?: "Etkinlikler yüklenemedi."
                )
            }
        }
    }
}