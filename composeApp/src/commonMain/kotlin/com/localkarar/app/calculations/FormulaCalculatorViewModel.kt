package com.localkarar.app.calculations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.FormulaCalculateResponseDto
import com.localkarar.app.network.dto.FormulaCalculationDto
import com.localkarar.app.network.dto.FormulaDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FormulaCalculatorUiState {
    data class Content(
        val formula: FormulaDto,
        val result: FormulaCalculateResponseDto? = null,
        val isCalculating: Boolean = false,
        val initialInputs: Map<String, String> = emptyMap()
    ) : FormulaCalculatorUiState()
    data class Error(val message: String) : FormulaCalculatorUiState()
}

class FormulaCalculatorViewModel(
    private val formula: FormulaDto,
    private val repository: CalculationsRepository,
    private val historicalCalculation: FormulaCalculationDto? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<FormulaCalculatorUiState>(
        FormulaCalculatorUiState.Content(
            formula = formula,
            result = historicalCalculation?.let { calculation ->
                FormulaCalculateResponseDto(
                    formulaId = calculation.formulaId,
                    result = calculation.result,
                    assumptions = calculation.inputs.values.map { it as kotlinx.serialization.json.JsonElement }.toList(),
                    warnings = emptyList()
                )
            },
            initialInputs = historicalCalculation?.inputs?.mapValues { 
                if (it.value is kotlinx.serialization.json.JsonPrimitive) {
                    (it.value as kotlinx.serialization.json.JsonPrimitive).content
                } else {
                    it.value.toString()
                }
            } ?: emptyMap()
        )
    )
    val uiState: StateFlow<FormulaCalculatorUiState> = _uiState.asStateFlow()

    fun calculate(inputs: Map<String, Double>, onError: (String) -> Unit) {
        val current = _uiState.value
        if (current !is FormulaCalculatorUiState.Content || current.isCalculating) return
        _uiState.value = current.copy(isCalculating = true)
        viewModelScope.launch {
            val result = repository.calculateFormula(formula.id, inputs)
            if (result.isSuccess) {
                _uiState.value = current.copy(result = result.getOrThrow(), isCalculating = false)
            } else {
                _uiState.value = current.copy(isCalculating = false)
                onError("Hesaplama yapılamadı.")
            }
        }
    }
}