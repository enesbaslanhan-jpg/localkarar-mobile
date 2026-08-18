package com.localkarar.app.workspaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.BusinessRecordDto
import com.localkarar.app.network.dto.RecordUpdateDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RecordDetailUiState {
    object Loading : RecordDetailUiState()
    data class Content(
        val record: BusinessRecordDto,
        val isActing: Boolean = false
    ) : RecordDetailUiState()
    data class Error(val message: String) : RecordDetailUiState()
}

class RecordDetailViewModel(
    private val workspaceId: String,
    private val recordId: String,
    private val repository: WorkspaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecordDetailUiState>(RecordDetailUiState.Loading)
    val uiState: StateFlow<RecordDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = RecordDetailUiState.Loading
        viewModelScope.launch {
            val result = repository.getRecord(workspaceId, recordId)
            if (result.isSuccess) {
                _uiState.value = RecordDetailUiState.Content(result.getOrThrow())
            } else {
                _uiState.value = RecordDetailUiState.Error(
                    result.exceptionOrNull()?.message ?: "Kayıt yüklenemedi."
                )
            }
        }
    }

    fun setStatus(status: String, onError: (String) -> Unit) {
        val current = _uiState.value
        if (current !is RecordDetailUiState.Content || current.isActing) return
        _uiState.value = current.copy(isActing = true)
        viewModelScope.launch {
            val result = repository.updateRecord(
                workspaceId,
                recordId,
                RecordUpdateDto(status = status)
            )
            if (result.isSuccess) {
                _uiState.value = RecordDetailUiState.Content(result.getOrThrow())
            } else {
                _uiState.value = current.copy(isActing = false)
                onError(result.exceptionOrNull()?.message ?: "Güncelleme yapılamadı.")
            }
        }
    }

    fun defer(dueAt: String, reason: String, onError: (String) -> Unit) {
        val current = _uiState.value
        if (current !is RecordDetailUiState.Content || current.isActing) return
        _uiState.value = current.copy(isActing = true)
        viewModelScope.launch {
            val result = repository.deferRecord(workspaceId, recordId, dueAt, reason)
            if (result.isSuccess) {
                _uiState.value = RecordDetailUiState.Content(result.getOrThrow())
            } else {
                _uiState.value = current.copy(isActing = false)
                onError(result.exceptionOrNull()?.message ?: "Ertelenemedi.")
            }
        }
    }

    fun delete(onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteRecord(workspaceId, recordId)
            onDone(result.isSuccess)
        }
    }
}