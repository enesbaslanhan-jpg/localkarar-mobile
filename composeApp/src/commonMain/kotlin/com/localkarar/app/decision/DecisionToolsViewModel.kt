package com.localkarar.app.decision

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.DecisionCheckListDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DecisionToolsUiState {
    object Loading : DecisionToolsUiState()
    data class Content(val tools: List<DecisionCheckListDto>) : DecisionToolsUiState()
    data class Error(val message: String) : DecisionToolsUiState()
}

class DecisionToolsViewModel(private val repository: DecisionRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<DecisionToolsUiState>(DecisionToolsUiState.Loading)
    val uiState: StateFlow<DecisionToolsUiState> = _uiState.asStateFlow()

    init {
        loadTools()
    }

    fun loadTools() {
        _uiState.value = DecisionToolsUiState.Loading
        viewModelScope.launch {
            val historyResult = repository.getSessionHistory()
            val toolsResult = repository.getDecisionChecks()

            if (toolsResult.isSuccess) {
                val tools = toolsResult.getOrThrow().toMutableList()
                val history = historyResult.getOrNull() ?: emptyList()

                // Merge history into tools
                for (i in tools.indices) {
                    val historyMatch = history.firstOrNull { it.decisionCheckCode == tools[i].code }
                    if (historyMatch != null) {
                        tools[i] = tools[i].copy(
                            status = historyMatch.status,
                            sessionId = historyMatch.id
                        )
                    }
                }
                _uiState.value = DecisionToolsUiState.Content(tools)
            } else {
                _uiState.value = DecisionToolsUiState.Error(
                    toolsResult.exceptionOrNull()?.message ?: "Araçlar yüklenemedi."
                )
            }
        }
    }

    fun startSession(code: String, onSessionStarted: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.startSession(code)
            if (result.isSuccess) {
                onSessionStarted(result.getOrThrow().sessionId)
            } else {
                onError(result.exceptionOrNull()?.message ?: "Oturum başlatılamadı.")
            }
        }
    }
}
