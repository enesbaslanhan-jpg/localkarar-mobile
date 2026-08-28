package com.localkarar.app.community

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.CommunityThreadDto
import com.localkarar.app.network.dto.PersonDto
import com.localkarar.app.network.dto.ThreadMessageDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ThreadsViewModel(
    private val repository: CommunityRepository
) : ViewModel() {

    // ==========================================
    // THREADS LIST STATE
    // ==========================================

    sealed interface ThreadsUiState {
        data object Loading : ThreadsUiState
        data class Content(
            val threads: List<CommunityThreadDto> = emptyList(),
            val invitations: List<CommunityThreadDto> = emptyList()
        ) : ThreadsUiState
        data class Error(val message: String) : ThreadsUiState
    }

    private val _threadsState = MutableStateFlow<ThreadsUiState>(ThreadsUiState.Loading)
    val threadsState: StateFlow<ThreadsUiState> = _threadsState

    // ==========================================
    // MESSAGES STATE (ACTIVE THREAD)
    // ==========================================

    sealed interface MessagesUiState {
        data object Idle : MessagesUiState
        data object Loading : MessagesUiState
        data class Content(
            val threadId: String,
            val messages: List<ThreadMessageDto> = emptyList()
        ) : MessagesUiState
        data class Error(val message: String) : MessagesUiState
    }

    private val _messagesState = MutableStateFlow<MessagesUiState>(MessagesUiState.Idle)
    val messagesState: StateFlow<MessagesUiState> = _messagesState

    var messageInput by mutableStateOf("")
        private set
    var isSendingMessage by mutableStateOf(false)
        private set

    // Create thread modal state
    var showCreateThreadSheet by mutableStateOf(false)
        private set
    var newThreadName by mutableStateOf("")
        private set
    var selectedMemberIds by mutableStateOf<Set<Int>>(emptySet())
        private set
    var availablePeople by mutableStateOf<List<PersonDto>>(emptyList())
        private set
    var isLoadingPeople by mutableStateOf(false)
        private set

    var notice by mutableStateOf<String?>(null)
        private set

    init {
        loadThreads()
    }

    // ==========================================
    // THREADS METHODS
    // ==========================================

    fun loadThreads() {
        viewModelScope.launch {
            _threadsState.value = ThreadsUiState.Loading
            repository.getThreads().onSuccess { allThreads ->
                val joined = allThreads.filter { it.durumum == "joined" }
                val invited = allThreads.filter { it.durumum != "joined" }
                _threadsState.value = ThreadsUiState.Content(threads = joined, invitations = invited)
            }.onFailure { e ->
                _threadsState.value = ThreadsUiState.Error(e.message ?: "Sohbetler yüklenemedi")
            }
        }
    }

    fun handleInvitation(threadId: String, accept: Boolean) {
        val karar = if (accept) "accept" else "decline"
        viewModelScope.launch {
            repository.inviteDecision(threadId, karar).onSuccess {
                notice = if (accept) "Davet kabul edildi" else "Davet reddedildi"
                loadThreads()
            }.onFailure { e ->
                notice = e.message ?: "Davet işlemi başarısız"
            }
        }
    }

    // ==========================================
    // MESSAGES METHODS
    // ==========================================

    fun loadMessages(threadId: String) {
        viewModelScope.launch {
            _messagesState.value = MessagesUiState.Loading
            repository.getMessages(threadId).onSuccess { msgs ->
                _messagesState.value = MessagesUiState.Content(threadId = threadId, messages = msgs)
            }.onFailure { e ->
                _messagesState.value = MessagesUiState.Error(e.message ?: "Mesajlar yüklenemedi")
            }
        }
    }

    fun onMessageInputChange(value: String) {
        messageInput = value
    }

    fun sendMessage(threadId: String) {
        val text = messageInput.trim()
        if (text.isBlank()) return

        isSendingMessage = true
        viewModelScope.launch {
            repository.sendMessage(threadId, text).onSuccess { sentMsg ->
                messageInput = ""
                isSendingMessage = false
                val current = _messagesState.value as? MessagesUiState.Content
                if (current != null && current.threadId == threadId) {
                    _messagesState.value = current.copy(messages = current.messages + sentMsg)
                }
            }.onFailure { e ->
                isSendingMessage = false
                notice = e.message ?: "Mesaj gönderilemedi"
            }
        }
    }

    // ==========================================
    // CREATE THREAD MODAL METHODS
    // ==========================================

    fun openCreateThreadSheet() {
        newThreadName = ""
        selectedMemberIds = emptySet()
        showCreateThreadSheet = true
        loadAvailablePeople()
    }

    fun dismissCreateThreadSheet() {
        showCreateThreadSheet = false
    }

    fun onNewThreadNameChange(name: String) {
        newThreadName = name
    }

    fun toggleMemberSelection(userId: Int) {
        selectedMemberIds = if (selectedMemberIds.contains(userId)) {
            selectedMemberIds - userId
        } else {
            selectedMemberIds + userId
        }
    }

    fun createThread(onCreated: (String) -> Unit) {
        val memberIds = selectedMemberIds.toList()
        if (memberIds.isEmpty()) {
            notice = "En az bir kişi seçmelisiniz"
            return
        }

        val name = if (newThreadName.isNotBlank()) newThreadName.trim() else null

        viewModelScope.launch {
            repository.createThread(memberIds = memberIds, name = name).onSuccess { thread ->
                showCreateThreadSheet = false
                notice = "Sohbet başlatıldı"
                loadThreads()
                onCreated(thread.id)
            }.onFailure { e ->
                notice = e.message ?: "Sohbet oluşturulamadı"
            }
        }
    }

    private fun loadAvailablePeople() {
        isLoadingPeople = true
        viewModelScope.launch {
            repository.getPeople("").onSuccess { res ->
                availablePeople = res.people
                isLoadingPeople = false
            }.onFailure {
                isLoadingPeople = false
            }
        }
    }

    fun clearNotice() {
        notice = null
    }
}
