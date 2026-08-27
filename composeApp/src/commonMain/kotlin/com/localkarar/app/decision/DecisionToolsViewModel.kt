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
    data class Content(
        val allTools: List<DecisionCheckListDto>,
        val visibleTools: List<DecisionCheckListDto>,
        val searchQuery: String,
        val statusFilter: String
    ) : DecisionToolsUiState()
    data class Error(val message: String) : DecisionToolsUiState()
}

class DecisionToolsViewModel(private val repository: DecisionRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<DecisionToolsUiState>(DecisionToolsUiState.Loading)
    val uiState: StateFlow<DecisionToolsUiState> = _uiState.asStateFlow()

    private val _allTools = MutableStateFlow<List<DecisionCheckListDto>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow("all")

    init {
        loadTools()
        
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                _allTools,
                _searchQuery,
                _statusFilter
            ) { tools, query, filter ->
                updateVisibleTools(tools, query, filter)
            }.collect { contentState ->
                if (_uiState.value !is DecisionToolsUiState.Loading && _uiState.value !is DecisionToolsUiState.Error) {
                    _uiState.value = contentState
                }
            }
        }
    }

    private fun normalizeStatus(status: String?): String {
        return when (status) {
            "completed", "complete" -> "completed"
            "in_progress", "started" -> "in_progress"
            else -> "not_started"
        }
    }

    private fun updateVisibleTools(
        tools: List<DecisionCheckListDto>,
        query: String,
        filter: String
    ): DecisionToolsUiState.Content {
        val needle = query.trim().lowercase()
        val visible = tools.filter { tool ->
            val matchesFilter = filter == "all" || normalizeStatus(tool.status) == filter
            if (!matchesFilter) return@filter false
            
            if (needle.isEmpty()) return@filter true
            
            val fields = listOfNotNull(tool.title, tool.category, tool.description)
            fields.any { it.lowercase().contains(needle) }
        }
        return DecisionToolsUiState.Content(tools, visible, query, filter)
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateStatusFilter(filter: String) {
        _statusFilter.value = filter
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
                _allTools.value = tools
                _uiState.value = updateVisibleTools(tools, _searchQuery.value, _statusFilter.value)
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
