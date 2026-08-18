package com.localkarar.app.decision

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.DecisionHistorySessionDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DecisionHistoryViewModel(
    private val repository: DecisionRepository
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Content(val sessions: List<DecisionHistorySessionDto>) : UiState
        data class Error(val message: String) : UiState
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.getSessionHistory().onSuccess { sessions ->
                _uiState.value = UiState.Content(sessions)
            }.onFailure { e ->
                _uiState.value = UiState.Error(e.message ?: "Geçmiş yüklenemedi")
            }
        }
    }
}