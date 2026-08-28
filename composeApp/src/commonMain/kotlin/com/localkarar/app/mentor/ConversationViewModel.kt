package com.localkarar.app.mentor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.CitationDto
import com.localkarar.app.network.dto.ConversationDetailDto
import com.localkarar.app.network.dto.MentorStreamEvent
import com.localkarar.app.network.dto.MessageDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class StreamStatus {
    IDLE,
    STARTING,
    STREAMING,
    COMPLETED,
    CANCELLED,
    FAILED
}

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
    var streamStatus by mutableStateOf(StreamStatus.IDLE)
        private set
    val isStreaming: Boolean
        get() = streamStatus == StreamStatus.STARTING || streamStatus == StreamStatus.STREAMING

    var streamText by mutableStateOf("")
        private set
    var providerName by mutableStateOf<String?>(null)
        private set
    var streamError by mutableStateOf<String?>(null)
        private set
    var streamSources by mutableStateOf<List<CitationDto>>(emptyList())
        private set

    // For editing messages
    var editingMessageId by mutableStateOf<Int?>(null)
        private set
    var editingMessageText by mutableStateOf("")
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
        if (conversationId == id && _state.value is UiState.Content) return
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

    fun startEditingMessage(message: MessageDto) {
        editingMessageId = message.id
        editingMessageText = message.content
    }

    fun onEditingTextChange(value: String) {
        editingMessageText = value
    }

    fun cancelEditing() {
        editingMessageId = null
        editingMessageText = ""
    }

    fun sendMessage(knowledgeObjectCode: String? = null, contextOverride: String? = null) {
        val message = input.trim()
        val id = conversationId ?: return
        if (message.isEmpty() || isStreaming) return
        if (message.length > 8000) {
            streamError = "Mesaj en fazla 8000 karakter olabilir"
            return
        }

        viewModelScope.launch {
            streamStatus = StreamStatus.STARTING
            streamText = ""
            providerName = null
            streamError = null
            streamSources = emptyList()

            // Optimistic user message
            val optimistic = MessageDto(
                id = -(System.currentTimeMillis() % 100000).toInt(),
                role = "user",
                content = message,
                generationStatus = "completed",
                createdAt = kotlinx.datetime.Clock.System.now().toString()
            )
            val current = _state.value
            if (current is UiState.Content) {
                _state.value = current.copy(messages = current.messages + optimistic)
            }
            input = ""

            executeStreamFlow(id) {
                repository.streamMessage(id, message, knowledgeObjectCode, contextOverride)
            }
        }
    }

    fun regenerateMessage(messageId: Int) {
        val id = conversationId ?: return
        if (isStreaming) return

        viewModelScope.launch {
            streamStatus = StreamStatus.STARTING
            streamText = ""
            providerName = null
            streamError = null
            streamSources = emptyList()

            executeStreamFlow(id) {
                repository.regenerateAssistantMessage(id, messageId)
            }
        }
    }

    fun submitEditAndRegenerate() {
        val messageId = editingMessageId ?: return
        val newText = editingMessageText.trim()
        val id = conversationId ?: return
        if (newText.isEmpty() || isStreaming) return
        if (newText.length > 8000) {
            streamError = "Mesaj en fazla 8000 karakter olabilir"
            return
        }

        cancelEditing()
        viewModelScope.launch {
            streamStatus = StreamStatus.STARTING
            streamText = ""
            providerName = null
            streamError = null
            streamSources = emptyList()

            executeStreamFlow(id) {
                repository.editAndRegenerateUserMessage(id, messageId, newText)
            }
        }
    }

    private suspend fun executeStreamFlow(
        id: Int,
        flowProvider: () -> kotlinx.coroutines.flow.Flow<MentorStreamEvent>
    ) {
        streamJob?.cancel()
        streamJob = viewModelScope.launch {
            try {
                flowProvider().collect { event ->
                    when (event) {
                        is MentorStreamEvent.Start -> {
                            streamStatus = StreamStatus.STREAMING
                        }
                        is MentorStreamEvent.Provider -> {
                            providerName = event.model ?: event.provider
                        }
                        is MentorStreamEvent.Delta -> {
                            streamStatus = StreamStatus.STREAMING
                            streamText += event.delta
                        }
                        is MentorStreamEvent.Done -> {
                            streamStatus = StreamStatus.COMPLETED
                            streamSources = event.sources
                            refreshMessages(id)
                        }
                        is MentorStreamEvent.Cancelled -> {
                            streamStatus = StreamStatus.CANCELLED
                            refreshMessages(id)
                        }
                        is MentorStreamEvent.StreamError -> {
                            streamStatus = StreamStatus.FAILED
                            streamError = mapErrorMessage(event.code, event.message)
                        }
                    }
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    streamStatus = StreamStatus.FAILED
                    streamError = "Bağlantı hatası: ${e.message ?: "Sunucuya ulaşılamıyor"}"
                }
            } finally {
                if (streamStatus == StreamStatus.STREAMING || streamStatus == StreamStatus.STARTING) {
                    streamStatus = StreamStatus.COMPLETED
                }
                refreshMessages(id)
            }
        }
    }

    fun stopStreaming() {
        streamJob?.cancel()
        streamJob = null
        streamStatus = StreamStatus.CANCELLED
        conversationId?.let { refreshMessages(it) }
    }

    private fun refreshMessages(id: Int) {
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
        val clean = title.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            repository.renameConversation(id, clean).onSuccess { conv ->
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

    private fun mapErrorMessage(code: String?, message: String?): String {
        return when (code) {
            "RATE_LIMIT" -> "Kısa sürede çok fazla istek gönderdiniz. Lütfen 1 dakika bekleyin."
            "CONCURRENT_LIMIT" -> "Aynı anda çok fazla yanıt oluşturuluyor. Lütfen mevcut yanıtın tamamlanmasını bekleyin."
            "VALIDATION_ERROR" -> message ?: "Geçersiz istek gönderildi."
            "AI_PROVIDER_ERROR" -> "Yapay zeka servisi şu anda yanıt veremiyor. Lütfen biraz sonra tekrar deneyin."
            "NETWORK_ERROR" -> "Bağlantı kesildi. Lütfen internet bağlantınızı kontrol edip tekrar deneyin."
            else -> message ?: "Yanıt oluşturulurken bir sorun oluştu."
        }
    }
}