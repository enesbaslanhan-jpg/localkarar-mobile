package com.localkarar.app.calculations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.BusinessRecordDto
import com.localkarar.app.network.dto.FormulaCalculationDto
import com.localkarar.app.network.dto.TrackerSummaryDto
import com.localkarar.app.workspaces.WorkspaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CalculationsUiState {
    object Loading : CalculationsUiState()
    data class Content(
        val catalog: List<CalculationItem> = emptyList(),
        val history: List<FormulaCalculationDto> = emptyList(),
        val trackerSummary: TrackerSummaryDto? = null,
        val openRecords: List<BusinessRecordDto> = emptyList(),
        val isRefreshing: Boolean = false
    ) : CalculationsUiState()
    data class Error(val message: String) : CalculationsUiState()
}

class CalculationsViewModel(
    private val repository: CalculationsRepository,
    private val workspaceRepository: WorkspaceRepository? = null,
    private val activeWorkspaceId: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<CalculationsUiState>(CalculationsUiState.Loading)
    val uiState: StateFlow<CalculationsUiState> = _uiState.asStateFlow()

    private val _categoryFilter = MutableStateFlow("all")
    val categoryFilter: StateFlow<String> = _categoryFilter.asStateFlow()

    init {
        load()
    }

    fun updateCategoryFilter(category: String) {
        _categoryFilter.value = category
    }

    fun load() {
        _uiState.value = CalculationsUiState.Loading
        viewModelScope.launch {
            val formulasResult = repository.getFormulas()
            val modelsResult = repository.getModels()
            val historyResult = repository.getFormulaHistory()

            val formulas = formulasResult.getOrNull()
            val models = modelsResult.getOrNull()?.models

            if (formulas != null && models != null) {
                val catalog = buildCalculationCatalog(formulas, models)
                val history = historyResult.getOrNull() ?: emptyList()

                // Load tracker data for Finansal Görünüm
                var trackerSummary: TrackerSummaryDto? = null
                var openRecords: List<BusinessRecordDto> = emptyList()

                if (workspaceRepository != null && activeWorkspaceId != null) {
                    trackerSummary = workspaceRepository.getTrackerSummary(activeWorkspaceId).getOrNull()
                    val recordsResult = workspaceRepository.getRecords(activeWorkspaceId)
                    openRecords = recordsResult.getOrNull()?.records
                        ?.filter { it.status != "completed" && it.status != "cancelled" }
                        ?.sortedBy { it.dueAt ?: "9999-12-31" }
                        ?: emptyList()
                }

                _uiState.value = CalculationsUiState.Content(
                    catalog = catalog,
                    history = history,
                    trackerSummary = trackerSummary,
                    openRecords = openRecords
                )
            } else {
                val message = "Hesaplamalar yüklenemedi. Lütfen tekrar deneyin."
                _uiState.value = CalculationsUiState.Error(message)
            }
        }
    }

    fun refresh() {
        val current = _uiState.value
        if (current is CalculationsUiState.Content) {
            _uiState.value = current.copy(isRefreshing = true)
        }
        viewModelScope.launch {
            val formulasResult = repository.getFormulas()
            val modelsResult = repository.getModels()
            val historyResult = repository.getFormulaHistory()

            val formulas = formulasResult.getOrNull()
            val models = modelsResult.getOrNull()?.models

            if (formulas != null && models != null) {
                val catalog = buildCalculationCatalog(formulas, models)
                val history = historyResult.getOrNull() ?: emptyList()

                var trackerSummary: TrackerSummaryDto? = null
                var openRecords: List<BusinessRecordDto> = emptyList()

                if (workspaceRepository != null && activeWorkspaceId != null) {
                    trackerSummary = workspaceRepository.getTrackerSummary(activeWorkspaceId).getOrNull()
                    val recordsResult = workspaceRepository.getRecords(activeWorkspaceId)
                    openRecords = recordsResult.getOrNull()?.records
                        ?.filter { it.status != "completed" && it.status != "cancelled" }
                        ?.sortedBy { it.dueAt ?: "9999-12-31" }
                        ?: emptyList()
                }

                _uiState.value = CalculationsUiState.Content(
                    catalog = catalog,
                    history = history,
                    trackerSummary = trackerSummary,
                    openRecords = openRecords
                )
            } else if (current is CalculationsUiState.Content) {
                _uiState.value = current.copy(isRefreshing = false)
            }
        }
    }
}