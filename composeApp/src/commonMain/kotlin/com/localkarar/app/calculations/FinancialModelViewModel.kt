package com.localkarar.app.calculations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.FinancialModelDto
import com.localkarar.app.network.dto.FinancialModelRunResponseDto
import com.localkarar.app.network.dto.ModelRunRequestDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement

sealed class FinancialModelUiState {
    object Loading : FinancialModelUiState()
    data class Content(
        val model: FinancialModelDto,
        val runResult: FinancialModelRunResponseDto? = null,
        val isRunning: Boolean = false
    ) : FinancialModelUiState()
    data class Error(val message: String) : FinancialModelUiState()
}

class FinancialModelViewModel(
    private val code: String,
    private val workspaceId: String?,
    private val repository: CalculationsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FinancialModelUiState>(FinancialModelUiState.Loading)
    val uiState: StateFlow<FinancialModelUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = FinancialModelUiState.Loading
        viewModelScope.launch {
            val result = repository.getModel(code)
            if (result.isSuccess) {
                _uiState.value = FinancialModelUiState.Content(result.getOrThrow())
            } else {
                _uiState.value = FinancialModelUiState.Error("Model yüklenemedi.")
            }
        }
    }

    fun run(inputs: Map<String, JsonElement>, scenarioName: String, onError: (String) -> Unit) {
        val current = _uiState.value
        if (current !is FinancialModelUiState.Content || current.isRunning) return
        val workspace = workspaceId
        if (workspace == null) {
            onError("Finansal model çalıştırmak için önce bir işletme seçin.")
            return
        }
        _uiState.value = current.copy(isRunning = true)
        viewModelScope.launch {
            val request = ModelRunRequestDto(
                inputs = inputs,
                assumptions = emptyList(),
                scenarioName = scenarioName
            )
            val result = repository.runModel(workspace, code, request)
            if (result.isSuccess) {
                _uiState.value = current.copy(runResult = result.getOrThrow(), isRunning = false)
            } else {
                _uiState.value = current.copy(isRunning = false)
                onError("Model çalıştırılamadı.")
            }
        }
    }
}