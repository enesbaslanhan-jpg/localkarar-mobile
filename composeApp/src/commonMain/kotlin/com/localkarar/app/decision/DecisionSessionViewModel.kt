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
        val isSubmitting: Boolean = false
    ) : DecisionSessionUiState()
    data class Error(val message: String) : DecisionSessionUiState()
}

class DecisionSessionViewModel(
    private val sessionId: String,
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

    fun updateAnswer(questionCode: String, value: JsonElement?) {
        val currentState = _uiState.value
        if (currentState is DecisionSessionUiState.Content) {
            // Optimistically update locally
            val updatedAnswers = currentState.session.answers.toMutableList()
            val index = updatedAnswers.indexOfFirst { it.questionCode == questionCode }
            
            val newAnswer = com.localkarar.app.network.dto.DecisionAnswerDto(
                questionCode = questionCode,
                valueJson = value,
                isUnknown = false
            )

            if (index != -1) {
                updatedAnswers[index] = newAnswer
            } else {
                updatedAnswers.add(newAnswer)
            }

            val updatedSession = currentState.session.copy(answers = updatedAnswers)
            _uiState.value = currentState.copy(session = updatedSession)

            // Send to server
            viewModelScope.launch {
                val res = repository.updateAnswer(sessionId, questionCode, value)
                if (res.isFailure) {
                    // Revert on failure could be implemented here
                }
            }
        }
    }

    fun completeSession(onError: (String) -> Unit) {
        val currentState = _uiState.value
        if (currentState is DecisionSessionUiState.Content) {
            _uiState.value = currentState.copy(isSubmitting = true)
            viewModelScope.launch {
                val result = repository.completeSession(sessionId)
                if (result.isSuccess) {
                    // Refresh completely to get the result from backend
                    loadSession()
                } else {
                    _uiState.value = currentState.copy(isSubmitting = false)
                    onError(result.exceptionOrNull()?.message ?: "Hesaplama tamamlanamadı. Lütfen girişleri kontrol edin.")
                }
            }
        }
    }
}
