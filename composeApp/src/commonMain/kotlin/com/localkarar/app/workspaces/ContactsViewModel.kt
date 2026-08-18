package com.localkarar.app.workspaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.BusinessContactDto
import com.localkarar.app.network.dto.ContactInputDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ContactsUiState {
    object Loading : ContactsUiState()
    data class Content(
        val contacts: List<BusinessContactDto> = emptyList(),
        val isSaving: Boolean = false
    ) : ContactsUiState()
    data class Error(val message: String) : ContactsUiState()
}

class ContactsViewModel(
    private val workspaceId: String,
    private val repository: WorkspaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ContactsUiState>(ContactsUiState.Loading)
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = ContactsUiState.Loading
        viewModelScope.launch {
            val result = repository.getContacts(workspaceId)
            if (result.isSuccess) {
                _uiState.value = ContactsUiState.Content(result.getOrThrow())
            } else {
                _uiState.value = ContactsUiState.Error(
                    result.exceptionOrNull()?.message ?: "Kişiler yüklenemedi."
                )
            }
        }
    }

    fun save(
        contactId: String?,
        input: ContactInputDto,
        onError: (String) -> Unit
    ) {
        val current = _uiState.value
        if (current !is ContactsUiState.Content || current.isSaving) return
        _uiState.value = current.copy(isSaving = true)
        viewModelScope.launch {
            val result = if (contactId == null) {
                repository.createContact(workspaceId, input)
            } else {
                repository.updateContact(workspaceId, contactId, input)
            }
            if (result.isSuccess) {
                _uiState.value = current.copy(isSaving = false)
                load()
            } else {
                _uiState.value = current.copy(isSaving = false)
                onError(result.exceptionOrNull()?.message ?: "Kişi kaydedilemedi.")
            }
        }
    }

    fun delete(contactId: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteContact(workspaceId, contactId)
            if (result.isSuccess) {
                load()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Kişi silinemedi.")
            }
        }
    }
}