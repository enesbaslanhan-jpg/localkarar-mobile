package com.localkarar.app.community

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.CommunityNotificationDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommunityNotificationsViewModel(
    private val repository: CommunityRepository
) : ViewModel() {

    sealed interface NotificationsUiState {
        data object Loading : NotificationsUiState
        data class Content(
            val unread: Int,
            val items: List<CommunityNotificationDto>
        ) : NotificationsUiState
        data class Error(val message: String) : NotificationsUiState
    }

    private val _uiState = MutableStateFlow<NotificationsUiState>(NotificationsUiState.Loading)
    val uiState: StateFlow<NotificationsUiState> = _uiState

    var unreadCount by mutableStateOf(0)
        private set

    var notice by mutableStateOf<String?>(null)
        private set

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = NotificationsUiState.Loading
            repository.getNotifications().onSuccess { res ->
                unreadCount = res.unread
                _uiState.value = NotificationsUiState.Content(
                    unread = res.unread,
                    items = res.items
                )
            }.onFailure { e ->
                _uiState.value = NotificationsUiState.Error(e.message ?: "Bildirimler yüklenemedi")
            }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            repository.markNotificationsRead().onSuccess {
                unreadCount = 0
                val current = _uiState.value as? NotificationsUiState.Content
                if (current != null) {
                    _uiState.value = current.copy(
                        unread = 0,
                        items = current.items.map { it.copy(readAt = it.readAt ?: "now") }
                    )
                }
                notice = "Tüm bildirimler okundu işaretlendi"
            }.onFailure { e ->
                notice = e.message ?: "Bildirimler güncellenemedi"
            }
        }
    }

    fun clearNotice() {
        notice = null
    }
}
