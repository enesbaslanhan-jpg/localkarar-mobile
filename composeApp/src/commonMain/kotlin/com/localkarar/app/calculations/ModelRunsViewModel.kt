package com.localkarar.app.calculations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.FinancialModelRunListItemDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ModelRunsUiState {
    object Loading : ModelRunsUiState()
    data class Content(
        val runs: List<FinancialModelRunListItemDto> = emptyList()
    ) : ModelRunsUiState()
    data class Error(val message: String) : ModelRunsUiState()
}

class ModelRunsViewModel(
    private val workspaceId: String,
    private val modelCode: String?,
    private val repository: CalculationsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ModelRunsUiState>(ModelRunsUiState.Loading)
    val uiState: StateFlow<ModelRunsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = ModelRunsUiState.Loading
        viewModelScope.launch {
            val result = repository.getModelRuns(workspaceId, modelCode)
            if (result.isSuccess) {
                _uiState.value = ModelRunsUiState.Content(result.getOrThrow().runs)
            } else {
                _uiState.value = ModelRunsUiState.Error(
                    result.exceptionOrNull()?.message ?: "Çalışma geçmişi yüklenemedi."
                )
            }
        }
    }
}