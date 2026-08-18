package com.localkarar.app.workspaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.BusinessNotificationDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class NotificationsUiState {
    object Loading : NotificationsUiState()
    data class Content(
        val notifications: List<BusinessNotificationDto> = emptyList(),
        val unreadCount: Int = 0
    ) : NotificationsUiState()
    data class Error(val message: String) : NotificationsUiState()
}

class NotificationsViewModel(
    private val workspaceId: String,
    private val repository: WorkspaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationsUiState>(NotificationsUiState.Loading)
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = NotificationsUiState.Loading
        viewModelScope.launch {
            val result = repository.getNotifications(workspaceId)
            if (result.isSuccess) {
                _uiState.value = NotificationsUiState.Content(
                    notifications = result.getOrThrow().notifications,
                    unreadCount = result.getOrThrow().unreadCount
                )
            } else {
                _uiState.value = NotificationsUiState.Error(
                    result.exceptionOrNull()?.message ?: "Bildirimler yüklenemedi."
                )
            }
        }
    }

    fun markRead(notificationId: String) {
        viewModelScope.launch {
            repository.markNotificationRead(workspaceId, notificationId)
            val current = _uiState.value
            if (current is NotificationsUiState.Content) {
                val updated = current.notifications.map {
                    if (it.id == notificationId && it.readAt == null) {
                        it.copy(readAt = "now")
                    } else it
                }
                _uiState.value = current.copy(
                    notifications = updated,
                    unreadCount = updated.count { it.readAt == null }
                )
            }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead(workspaceId)
            load()
        }
    }
}