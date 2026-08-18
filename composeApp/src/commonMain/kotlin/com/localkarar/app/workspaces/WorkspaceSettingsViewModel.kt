package com.localkarar.app.workspaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.UpdateSettingsRequestDto
import com.localkarar.app.network.dto.WorkspaceSettingsDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class WorkspaceSettingsUiState {
    object Loading : WorkspaceSettingsUiState()
    data class Content(
        val settings: WorkspaceSettingsDto,
        val isSaving: Boolean = false
    ) : WorkspaceSettingsUiState()
    data class Error(val message: String) : WorkspaceSettingsUiState()
}

class WorkspaceSettingsViewModel(
    private val workspaceId: String,
    private val repository: WorkspaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WorkspaceSettingsUiState>(WorkspaceSettingsUiState.Loading)
    val uiState: StateFlow<WorkspaceSettingsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = WorkspaceSettingsUiState.Loading
        viewModelScope.launch {
            val result = repository.getSettings(workspaceId)
            if (result.isSuccess) {
                _uiState.value = WorkspaceSettingsUiState.Content(result.getOrThrow())
            } else {
                _uiState.value = WorkspaceSettingsUiState.Error(
                    result.exceptionOrNull()?.message ?: "Ayarlar yüklenemedi."
                )
            }
        }
    }

    fun save(
        defaultCurrency: String,
        timezone: String,
        locale: String,
        weekStartsOn: Int,
        onError: (String) -> Unit
    ) {
        val current = _uiState.value
        if (current !is WorkspaceSettingsUiState.Content || current.isSaving) return
        _uiState.value = current.copy(isSaving = true)
        viewModelScope.launch {
            val result = repository.updateSettings(
                workspaceId,
                UpdateSettingsRequestDto(
                    defaultCurrency = defaultCurrency,
                    timezone = timezone,
                    locale = locale,
                    weekStartsOn = weekStartsOn
                )
            )
            if (result.isSuccess) {
                _uiState.value = WorkspaceSettingsUiState.Content(result.getOrThrow())
            } else {
                _uiState.value = current.copy(isSaving = false)
                onError(result.exceptionOrNull()?.message ?: "Ayarlar kaydedilemedi.")
            }
        }
    }
}