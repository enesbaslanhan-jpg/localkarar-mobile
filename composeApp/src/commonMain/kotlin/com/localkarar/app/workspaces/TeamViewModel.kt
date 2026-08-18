package com.localkarar.app.workspaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.BusinessInvitationDto
import com.localkarar.app.network.dto.WorkspaceMemberDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TeamUiState {
    object Loading : TeamUiState()
    data class Content(
        val members: List<WorkspaceMemberDto> = emptyList(),
        val invitations: List<BusinessInvitationDto> = emptyList(),
        val isInviting: Boolean = false
    ) : TeamUiState()
    data class Error(val message: String) : TeamUiState()
}

class TeamViewModel(
    private val workspaceId: String,
    private val repository: WorkspaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TeamUiState>(TeamUiState.Loading)
    val uiState: StateFlow<TeamUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = TeamUiState.Loading
        viewModelScope.launch {
            val membersResult = repository.getMembers(workspaceId)
            val invitationsResult = repository.getInvitations(workspaceId)
            val members = membersResult.getOrNull()
            if (members != null) {
                _uiState.value = TeamUiState.Content(
                    members = members,
                    invitations = invitationsResult.getOrNull() ?: emptyList()
                )
            } else {
                _uiState.value = TeamUiState.Error(
                    membersResult.exceptionOrNull()?.message ?: "Ekip yüklenemedi."
                )
            }
        }
    }

    fun invite(email: String, role: String, onError: (String) -> Unit) {
        val current = _uiState.value
        if (current !is TeamUiState.Content || current.isInviting) return
        _uiState.value = current.copy(isInviting = true)
        viewModelScope.launch {
            val result = repository.inviteMember(workspaceId, email.trim(), role)
            if (result.isSuccess) {
                _uiState.value = current.copy(isInviting = false)
                load()
            } else {
                _uiState.value = current.copy(isInviting = false)
                onError(result.exceptionOrNull()?.message ?: "Davet gönderilemedi.")
            }
        }
    }

    fun changeRole(memberId: String, role: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.updateMemberRole(workspaceId, memberId, role)
            if (result.isSuccess) {
                load()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Rol güncellenemedi.")
            }
        }
    }

    fun removeMember(memberId: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.removeMember(workspaceId, memberId)
            if (result.isSuccess) {
                load()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Üye kaldırılamadı.")
            }
        }
    }

    fun cancelInvitation(invitationId: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.cancelInvitation(workspaceId, invitationId)
            if (result.isSuccess) {
                load()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Davet iptal edilemedi.")
            }
        }
    }
}