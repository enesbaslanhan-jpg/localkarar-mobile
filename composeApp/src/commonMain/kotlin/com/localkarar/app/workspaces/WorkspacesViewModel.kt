package com.localkarar.app.workspaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.CreateWorkspaceRequestDto
import com.localkarar.app.network.dto.WorkspaceSummaryDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class WorkspacesUiState {
    object Loading : WorkspacesUiState()
    data class Content(
        val workspaces: List<WorkspaceSummaryDto> = emptyList(),
        val isCreating: Boolean = false
    ) : WorkspacesUiState()
    data class Error(val message: String) : WorkspacesUiState()
}

class WorkspacesViewModel(
    private val repository: WorkspaceRepository,
    private val activeWorkspaceStore: ActiveWorkspaceStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<WorkspacesUiState>(WorkspacesUiState.Loading)
    val uiState: StateFlow<WorkspacesUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = WorkspacesUiState.Loading
        viewModelScope.launch {
            val result = repository.listWorkspaces()
            if (result.isSuccess) {
                val workspaces = result.getOrThrow().workspaces
                if (activeWorkspaceStore.activeWorkspaceId.value == null && workspaces.isNotEmpty()) {
                    activeWorkspaceStore.setActive(workspaces.first().id, workspaces.first().name)
                }
                _uiState.value = WorkspacesUiState.Content(workspaces)
            } else {
                _uiState.value = WorkspacesUiState.Error(
                    result.exceptionOrNull()?.message ?: "İşletmeler yüklenemedi."
                )
            }
        }
    }

    fun createWorkspace(
        name: String,
        legalName: String?,
        sector: String?,
        city: String?,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val current = _uiState.value
        if (current is WorkspacesUiState.Content) {
            _uiState.value = current.copy(isCreating = true)
        }
        viewModelScope.launch {
            val result = repository.createWorkspace(
                CreateWorkspaceRequestDto(
                    name = name.trim(),
                    legalName = legalName?.trim()?.ifBlank { null },
                    sector = sector?.trim()?.ifBlank { null },
                    city = city?.trim()?.ifBlank { null },
                    currency = "TRY"
                )
            )
            if (result.isSuccess) {
                activeWorkspaceStore.setActive(result.getOrThrow().id, name.trim())
                load()
                onSuccess(result.getOrThrow().id)
            } else {
                if (current is WorkspacesUiState.Content) {
                    _uiState.value = current.copy(isCreating = false)
                }
                onError(result.exceptionOrNull()?.message ?: "İşletme oluşturulamadı.")
            }
        }
    }

    fun deleteWorkspace(workspaceId: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteWorkspace(workspaceId)
            onDone(result.isSuccess)
            if (result.isSuccess) {
                if (activeWorkspaceStore.activeWorkspaceId.value == workspaceId) {
                    activeWorkspaceStore.setActive(null)
                }
                load()
            }
        }
    }
}