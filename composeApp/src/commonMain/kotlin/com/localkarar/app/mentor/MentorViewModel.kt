package com.localkarar.app.mentor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.ConversationListItemDto
import com.localkarar.app.network.dto.MemoryDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MentorViewModel(
    private val repository: MentorRepository
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Content(
            val conversations: List<ConversationListItemDto>,
            val loading: Boolean = false
        ) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state

    var showMemorySheet by mutableStateOf(false)
        private set

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val current = _state.value
            if (current is UiState.Content) {
                _state.value = current.copy(loading = true)
            } else {
                _state.value = UiState.Loading
            }
            repository.listConversations().onSuccess { conversations ->
                _state.value = UiState.Content(conversations = conversations)
            }.onFailure { e ->
                if (current is UiState.Content) {
                    _state.value = current.copy(loading = false)
                } else {
                    _state.value = UiState.Error(e.message ?: "Sohbetler yüklenemedi")
                }
            }
        }
    }

    fun onCreateNew(onCreated: (Int) -> Unit) {
        viewModelScope.launch {
            repository.createConversation("Yeni Sohbet").onSuccess { conversation ->
                onCreated(conversation.id)
            }.onFailure { e ->
                _state.value = UiState.Error(e.message ?: "Sohbet oluşturulamadı")
            }
        }
    }

    fun onArchive(id: Int) {
        viewModelScope.launch {
            repository.archiveConversation(id)
            refresh()
        }
    }

    fun onDelete(id: Int) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            refresh()
        }
    }
}

class MemoryViewModel(
    private val repository: MentorRepository
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Content(val memories: List<MemoryDto>, val loading: Boolean = false) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state

    var input by mutableStateOf("")
        private set
    var selectedType by mutableStateOf("fact")
        private set
    var notice by mutableStateOf<String?>(null)
        private set

    val memoryTypes = listOf("fact", "preference", "goal", "profile", "problem", "decision")

    init {
        refresh()
    }

    fun onInputChange(value: String) {
        input = value
    }

    fun onTypeChange(value: String) {
        selectedType = value
    }

    fun refresh() {
        viewModelScope.launch {
            val current = _state.value
            if (current is UiState.Content) {
                _state.value = current.copy(loading = true)
            } else {
                _state.value = UiState.Loading
            }
            repository.listMemories().onSuccess { memories ->
                _state.value = UiState.Content(memories = memories)
            }.onFailure { e ->
                if (current is UiState.Content) {
                    _state.value = current.copy(loading = false)
                } else {
                    _state.value = UiState.Error(e.message ?: "Hatıralar yüklenemedi")
                }
            }
        }
    }

    fun addMemory() {
        val value = input.trim()
        if (value.isEmpty()) {
            notice = "Hatıra içeriği boş olamaz"
            return
        }
        viewModelScope.launch {
            repository.createMemory(selectedType, value).onSuccess {
                input = ""
                notice = "Hatıra kaydedildi"
                refresh()
            }.onFailure { e ->
                notice = e.message ?: "Hatıra kaydedilemedi"
            }
        }
    }

    fun deleteMemory(id: Int) {
        viewModelScope.launch {
            repository.deleteMemory(id)
            refresh()
        }
    }

    fun clearNotice() {
        notice = null
    }
}