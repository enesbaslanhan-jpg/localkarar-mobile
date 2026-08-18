package com.localkarar.app.community

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.CommunityPostDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommunityViewModel(
    private val repository: CommunityRepository
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Content(
            val posts: List<CommunityPostDto> = emptyList(),
            val loading: Boolean = false
        ) : UiState
        data class Error(val message: String) : UiState
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    var selectedType: String? = null
        private set
    var notice by mutableStateOf<String?>(null)
        private set
    var titleInput by mutableStateOf("")
        private set
    var summaryInput by mutableStateOf("")
        private set
    var composing by mutableStateOf(false)
        private set

    private var nextCursor: String? = null

    val tabs = listOf(
        null to "Tümü",
        "official" to "Resmi",
        "user" to "Topluluk"
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.getFeed(selectedType, null).onSuccess { feed ->
                nextCursor = feed.nextCursor
                _uiState.value = UiState.Content(posts = feed.posts)
            }.onFailure { e ->
                _uiState.value = UiState.Error(e.message ?: "Gönderiler yüklenemedi")
            }
        }
    }

    fun selectType(type: String?) {
        if (selectedType == type) return
        selectedType = type
        refresh()
    }

    fun loadMore() {
        val cursor = nextCursor ?: return
        val current = _uiState.value as? UiState.Content ?: return
        viewModelScope.launch {
            repository.getFeed(selectedType, cursor).onSuccess { feed ->
                nextCursor = feed.nextCursor
                _uiState.value = current.copy(posts = current.posts + feed.posts)
            }
        }
    }

    fun startCompose() {
        titleInput = ""
        summaryInput = ""
        composing = true
    }

    fun onTitleChange(value: String) {
        titleInput = value
    }

    fun onSummaryChange(value: String) {
        summaryInput = value
    }

    fun dismissCompose() {
        composing = false
        notice = null
    }

    fun submitPost() {
        val title = titleInput.trim()
        val summary = summaryInput.trim()
        if (title.length < 5) {
            notice = "Başlık en az 5 karakter olmalı"
            return
        }
        if (summary.length < 20) {
            notice = "İçerik en az 20 karakter olmalı"
            return
        }
        viewModelScope.launch {
            repository.createPost(title, summary).onSuccess {
                composing = false
                notice = "Gönderin yayınlandı"
                refresh()
            }.onFailure { e ->
                notice = e.message ?: "Gönderi oluşturulamadı"
            }
        }
    }

    fun reportPost(postId: String, reason: String, details: String?) {
        viewModelScope.launch {
            repository.reportPost(postId, reason, details).onSuccess {
                notice = "Şikayet iletildi"
            }.onFailure { e ->
                notice = e.message ?: "Şikayet gönderilemedi"
            }
        }
    }

    fun clearNotice() {
        notice = null
    }
}