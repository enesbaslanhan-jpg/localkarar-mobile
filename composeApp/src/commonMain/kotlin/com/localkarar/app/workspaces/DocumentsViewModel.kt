package com.localkarar.app.workspaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.WorkspaceDocumentDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DocumentsUiState {
    object Loading : DocumentsUiState()
    data class Content(
        val documents: List<WorkspaceDocumentDto> = emptyList()
    ) : DocumentsUiState()
    data class Error(val message: String) : DocumentsUiState()
}

class DocumentsViewModel(
    private val workspaceId: String,
    private val repository: WorkspaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DocumentsUiState>(DocumentsUiState.Loading)
    val uiState: StateFlow<DocumentsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = DocumentsUiState.Loading
        viewModelScope.launch {
            val result = repository.getDocuments(workspaceId)
            if (result.isSuccess) {
                _uiState.value = DocumentsUiState.Content(result.getOrThrow().documents)
            } else {
                _uiState.value = DocumentsUiState.Error(
                    result.exceptionOrNull()?.message ?: "Belgeler yüklenemedi."
                )
            }
        }
    }

    fun delete(documentId: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteDocument(workspaceId, documentId)
            onDone(result.isSuccess)
            if (result.isSuccess) load()
        }
    }
}