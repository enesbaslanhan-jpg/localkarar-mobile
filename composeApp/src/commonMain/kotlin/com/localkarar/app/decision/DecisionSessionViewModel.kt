package com.localkarar.app.decision

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.DecisionCheckSessionDto
import com.localkarar.app.network.dto.DecisionResultDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement

sealed class DecisionSessionUiState {
    object Loading : DecisionSessionUiState()
    data class Content(
        val session: DecisionCheckSessionDto,
        val result: DecisionResultDto? = null,
        val isSubmitting: Boolean = false,
        val errors: Map<String, String> = emptyMap()
    ) : DecisionSessionUiState()
    data class Error(val message: String) : DecisionSessionUiState()
}

class DecisionSessionViewModel(
    private var sessionId: String,
    private val repository: DecisionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DecisionSessionUiState>(DecisionSessionUiState.Loading)
    val uiState: StateFlow<DecisionSessionUiState> = _uiState.asStateFlow()

    init {
        loadSession()
    }

    fun loadSession() {
        _uiState.value = DecisionSessionUiState.Loading
        viewModelScope.launch {
            val sessionResult = repository.getSession(sessionId)
            if (sessionResult.isSuccess) {
                val session = sessionResult.getOrThrow()
                if (session.status == "completed") {
                    val resultResult = repository.getResult(sessionId)
                    if (resultResult.isSuccess) {
                        _uiState.value = DecisionSessionUiState.Content(session, resultResult.getOrThrow())
                    } else {
                        // Show session even if result fetch failed somehow
                        _uiState.value = DecisionSessionUiState.Content(session)
                    }
                } else {
                    _uiState.value = DecisionSessionUiState.Content(session)
                }
            } else {
                _uiState.value = DecisionSessionUiState.Error(
                    sessionResult.exceptionOrNull()?.message ?: "Oturum yüklenemedi."
                )
            }
        }
    }

    fun updateAnswer(questionCode: String, value: JsonElement?, isUnknown: Boolean = false) {
        val currentState = _uiState.value
        if (currentState is DecisionSessionUiState.Content) {
            // Optimistically update locally
            val updatedAnswers = currentState.session.answers.toMutableList()
            val index = updatedAnswers.indexOfFirst { it.questionCode == questionCode }
            
            val newAnswer = com.localkarar.app.network.dto.DecisionAnswerDto(
                questionCode = questionCode,
                valueJson = value,
                isUnknown = isUnknown
            )

            if (index != -1) {
                updatedAnswers[index] = newAnswer
            } else {
                updatedAnswers.add(newAnswer)
            }

            // Clear any error for this field
            val updatedErrors = currentState.errors.toMutableMap()
            updatedErrors.remove(questionCode)

            val updatedSession = currentState.session.copy(answers = updatedAnswers)
            _uiState.value = currentState.copy(session = updatedSession, errors = updatedErrors)

            // Send to server
            viewModelScope.launch {
                val res = repository.updateAnswer(sessionId, questionCode, value, isUnknown)
                if (res.isFailure) {
                    // Revert on failure could be implemented here
                }
            }
        }
    }

    fun completeSession(onError: (String) -> Unit) {
        val currentState = _uiState.value
        if (currentState is DecisionSessionUiState.Content) {
            
            // Local Validation
            val nextErrors = mutableMapOf<String, String>()
            for (field in currentState.session.definition) {
                val answer = currentState.session.answers.find { it.questionCode == field.code }
                val isUnknown = answer?.isUnknown == true
                if (!isUnknown) {
                    val valueDouble = answer?.valueJson?.toString()?.toDoubleOrNull()
                    if (valueDouble == null || !valueDouble.isFinite() || (field.min != null && valueDouble < field.min) || (field.max != null && valueDouble > field.max)) {
                        val range = if (field.max == null) {
                            if (field.min == null) "Geçerli" else "${field.min} veya üzeri"
                        } else {
                            if (field.min == null) "${field.max} veya altı" else "${field.min}-${field.max} arası"
                        }
                        nextErrors[field.code] = "$range geçerli bir değer girin."
                    }
                }
            }

            if (nextErrors.isNotEmpty()) {
                _uiState.value = currentState.copy(errors = nextErrors)
                onError("Lütfen formdaki hataları düzeltin.")
                return
            }

            _uiState.value = currentState.copy(isSubmitting = true, errors = emptyMap())
            viewModelScope.launch {
                val result = repository.completeSession(sessionId)
                if (result.isSuccess) {
                    // Refresh completely to get the result from backend
                    loadSession()
                } else {
                    _uiState.value = (uiState.value as DecisionSessionUiState.Content).copy(isSubmitting = false)
                    onError(result.exceptionOrNull()?.message ?: "Hesaplama tamamlanamadı. Lütfen girişleri kontrol edin.")
                }
            }
        }
    }

    fun restartSession(toolCode: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val newSessionId = repository.startSession(toolCode).getOrThrow().sessionId
                // Update sessionId and load new session
                this@DecisionSessionViewModel.sessionId = newSessionId
                loadSession()
            } catch (e: Exception) {
                onError("Yeni hesaplama başlatılamadı.")
            }
        }
    }
}
