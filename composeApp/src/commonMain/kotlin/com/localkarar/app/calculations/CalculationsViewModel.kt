package com.localkarar.app.calculations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.FinancialModelDto
import com.localkarar.app.network.dto.FormulaCalculationDto
import com.localkarar.app.network.dto.FormulaDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CalculationsUiState {
    object Loading : CalculationsUiState()
    data class Content(
        val formulas: List<FormulaDto> = emptyList(),
        val models: List<FinancialModelDto> = emptyList(),
        val history: List<FormulaCalculationDto> = emptyList(),
        val isRefreshing: Boolean = false
    ) : CalculationsUiState()
    data class Error(val message: String) : CalculationsUiState()
}

class CalculationsViewModel(
    private val repository: CalculationsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CalculationsUiState>(CalculationsUiState.Loading)
    val uiState: StateFlow<CalculationsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = CalculationsUiState.Loading
        viewModelScope.launch {
            val formulasResult = repository.getFormulas()
            val modelsResult = repository.getModels()
            val historyResult = repository.getFormulaHistory()

            val formulas = formulasResult.getOrNull()
            val models = modelsResult.getOrNull()?.models
            val history = historyResult.getOrNull()

            if (formulas != null && models != null) {
                _uiState.value = CalculationsUiState.Content(
                    formulas = formulas,
                    models = models,
                    history = history ?: emptyList()
                )
            } else {
                val message = formulasResult.exceptionOrNull()?.message
                    ?: modelsResult.exceptionOrNull()?.message
                    ?: "Hesaplamalar yüklenemedi. Lütfen tekrar deneyin."
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
            val history = historyResult.getOrNull()

            if (formulas != null && models != null) {
                _uiState.value = CalculationsUiState.Content(
                    formulas = formulas,
                    models = models,
                    history = history ?: emptyList()
                )
            } else if (current is CalculationsUiState.Content) {
                _uiState.value = current.copy(isRefreshing = false)
            }
        }
    }
}