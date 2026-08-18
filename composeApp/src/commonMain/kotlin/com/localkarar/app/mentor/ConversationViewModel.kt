package com.localkarar.app.mentor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.ConversationDetailDto
import com.localkarar.app.network.dto.MentorStreamEvent
import com.localkarar.app.network.dto.MessageDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ConversationViewModel(
    private val repository: MentorRepository,
    initialConversationId: Int? = null
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Content(
            val conversation: ConversationDetailDto? = null,
            val messages: List<MessageDto> = emptyList()
        ) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state

    var input by mutableStateOf("")
        private set
    var isStreaming by mutableStateOf(false)
        private set
    var streamText by mutableStateOf("")
        private set
    var providerName by mutableStateOf<String?>(null)
        private set
    var streamError by mutableStateOf<String?>(null)
        private set

    private var streamJob: Job? = null
    private var conversationId: Int? = null

    init {
        conversationId = initialConversationId
        if (initialConversationId != null) {
            viewModelScope.launch { loadConversation(initialConversationId) }
        }
    }

    fun setConversation(id: Int) {
        if (conversationId == id) return
        conversationId = id
        viewModelScope.launch { loadConversation(id) }
    }

    private suspend fun loadConversation(id: Int) {
        repository.getConversation(id).onSuccess { response ->
            _state.value = UiState.Content(
                conversation = response.conversation,
                messages = response.messages
            )
        }.onFailure { e ->
            _state.value = UiState.Error(e.message ?: "Sohbet yüklenemedi")
        }
    }

    fun onInputChange(value: String) {
        input = value
    }

    fun sendMessage() {
        val message = input.trim()
        val id = conversationId ?: return
        if (message.isEmpty() || isStreaming) return
        viewModelScope.launch {
            isStreaming = true
            streamText = ""
            providerName = null
            streamError = null
            val optimistic = MessageDto(
                id = -(System.currentTimeMillis() / 1000).toInt(),
                role = "user",
                content = message,
                createdAt = kotlinx.datetime.Clock.System.now().toString()
            )
            val current = _state.value
            if (current is UiState.Content) {
                _state.value = current.copy(messages = current.messages + optimistic)
            }
            input = ""
            streamJob = viewModelScope.launch {
                repository.streamMessage(id, message).collect { event ->
                    when (event) {
                        is MentorStreamEvent.Delta -> streamText += event.delta
                        is MentorStreamEvent.Provider -> {
                            providerName = event.model ?: event.provider
                        }
                        is MentorStreamEvent.StreamError -> {
                            streamError = event.message ?: "Yanıt oluşturulamadı"
                        }
                        is MentorStreamEvent.Done -> {
                            refreshMessages()
                        }
                        is MentorStreamEvent.Cancelled -> {
                            refreshMessages()
                        }
                        is MentorStreamEvent.Start -> {
                            refreshMessages()
                        }
                    }
                }
                isStreaming = false
                streamText = ""
                refreshMessages()
            }
        }
    }

    fun stopStreaming() {
        streamJob?.cancel()
        streamJob = null
        isStreaming = false
        streamText = ""
        refreshMessages()
    }

    private fun refreshMessages() {
        val id = conversationId ?: return
        viewModelScope.launch {
            repository.getConversation(id).onSuccess { response ->
                _state.value = UiState.Content(
                    conversation = response.conversation,
                    messages = response.messages
                )
            }
        }
    }

    fun renameConversation(title: String) {
        val id = conversationId ?: return
        viewModelScope.launch {
            repository.renameConversation(id, title).onSuccess { conv ->
                val current = _state.value
                if (current is UiState.Content) {
                    _state.value = current.copy(conversation = conv)
                }
            }
        }
    }

    fun clearError() {
        streamError = null
    }
}