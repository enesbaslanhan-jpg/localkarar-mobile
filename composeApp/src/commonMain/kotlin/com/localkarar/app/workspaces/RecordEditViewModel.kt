package com.localkarar.app.workspaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.BusinessContactDto
import com.localkarar.app.network.dto.BusinessRecordDto
import com.localkarar.app.network.dto.RecordInputDto
import com.localkarar.app.network.dto.RecordUpdateDto
import com.localkarar.app.network.dto.WorkspaceMemberDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*

sealed class RecordEditUiState {
    object Loading : RecordEditUiState()
    data class Content(
        val record: BusinessRecordDto? = null,
        val contacts: List<BusinessContactDto> = emptyList(),
        val members: List<WorkspaceMemberDto> = emptyList(),
        val isSaving: Boolean = false
    ) : RecordEditUiState()
    data class Error(val message: String) : RecordEditUiState()
}

class RecordEditViewModel(
    private val workspaceId: String,
    private val recordId: String?,
    private val repository: WorkspaceRepository
) : ViewModel() {
    val financeAccounts = MutableStateFlow<List<JsonObject>>(emptyList())

    private val _uiState = MutableStateFlow<RecordEditUiState>(RecordEditUiState.Loading)
    val uiState: StateFlow<RecordEditUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = RecordEditUiState.Loading
        viewModelScope.launch {
            repository.finance(workspaceId, "accounts").onSuccess { result ->
                financeAccounts.value = result["accounts"]?.jsonArray?.map { it.jsonObject } ?: emptyList()
            }
            val contactsResult = repository.getContacts(workspaceId)
            val membersResult = repository.getMembers(workspaceId)
            val contacts = contactsResult.getOrNull() ?: emptyList()
            val members = membersResult.getOrNull() ?: emptyList()

            if (recordId == null) {
                _uiState.value = RecordEditUiState.Content(contacts = contacts, members = members)
                return@launch
            }
            val recordResult = repository.getRecord(workspaceId, recordId)
            if (recordResult.isSuccess) {
                _uiState.value = RecordEditUiState.Content(
                    record = recordResult.getOrThrow(),
                    contacts = contacts,
                    members = members
                )
            } else {
                _uiState.value = RecordEditUiState.Error(
                    recordResult.exceptionOrNull()?.message ?: "Kayıt yüklenemedi."
                )
            }
        }
    }

    fun save(
        input: RecordInputDto,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val current = _uiState.value
        if (current !is RecordEditUiState.Content || current.isSaving) return
        _uiState.value = current.copy(isSaving = true)
        viewModelScope.launch {
            val result = if (recordId == null) {
                repository.createRecord(workspaceId, input)
            } else {
                val update = input.toUpdateDto()
                repository.updateRecordForm(workspaceId, recordId, if (current.record?.loanId != null) update.copy(amount = null, currency = null, direction = null, recurrenceRule = null) else update)
            }
            if (result.isSuccess) {
                _uiState.value = current.copy(isSaving = false)
                onSuccess()
            } else {
                _uiState.value = current.copy(isSaving = false)
                onError(result.exceptionOrNull()?.message ?: "Kayıt kaydedilemedi.")
            }
        }
    }
}

private fun RecordInputDto.toUpdateDto(): RecordUpdateDto {
    return RecordUpdateDto(
        accountId = accountId, category = category, settlementAt = settlementAt,
        type = type,
        title = title,
        description = description,
        direction = direction,
        amount = amount,
        currency = currency,
        priority = priority,
        dueAt = dueAt,
        contactId = contactId,
        assignedToId = assignedToId,
        recurrenceRule = recurrenceRule
    )
}
