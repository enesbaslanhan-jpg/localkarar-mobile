package com.localkarar.app.decision

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.DecisionCheckSessionDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DecisionToolUiState {
    data object Loading : DecisionToolUiState
    data class Ready(val sessionId: String) : DecisionToolUiState
    data class Error(val message: String) : DecisionToolUiState
}

class DecisionToolViewModel(
    val code: String,
    private val repository: DecisionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DecisionToolUiState>(DecisionToolUiState.Loading)
    val uiState: StateFlow<DecisionToolUiState> = _uiState.asStateFlow()

    init {
        startOrResume()
    }

    fun startOrResume() {
        _uiState.value = DecisionToolUiState.Loading
        viewModelScope.launch {
            val result = repository.startSession(code)
            if (result.isSuccess) {
                val startDto = result.getOrThrow()
                _uiState.value = DecisionToolUiState.Ready(startDto.sessionId)
            } else {
                _uiState.value = DecisionToolUiState.Error(
                    result.exceptionOrNull()?.message ?: "Karar aracı başlatılamadı."
                )
            }
        }
    }
}
