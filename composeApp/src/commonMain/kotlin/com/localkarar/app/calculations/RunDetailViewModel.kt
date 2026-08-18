package com.localkarar.app.calculations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.FinancialModelRunDetailDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RunDetailUiState {
    object Loading : RunDetailUiState()
    data class Content(val run: FinancialModelRunDetailDto) : RunDetailUiState()
    data class Error(val message: String) : RunDetailUiState()
}

class RunDetailViewModel(
    private val workspaceId: String,
    private val runId: String,
    private val repository: CalculationsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RunDetailUiState>(RunDetailUiState.Loading)
    val uiState: StateFlow<RunDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = RunDetailUiState.Loading
        viewModelScope.launch {
            val result = repository.getRunDetail(workspaceId, runId)
            if (result.isSuccess) {
                _uiState.value = RunDetailUiState.Content(result.getOrThrow())
            } else {
                _uiState.value = RunDetailUiState.Error(
                    result.exceptionOrNull()?.message ?: "Çalışma detayı yüklenemedi."
                )
            }
        }
    }
}