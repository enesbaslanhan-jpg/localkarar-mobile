package com.localkarar.app.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.NewsArticleDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NewsViewModel(
    private val repository: NewsRepository
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Content(
            val articles: List<NewsArticleDto> = emptyList(),
            val loading: Boolean = false,
            val loadingMore: Boolean = false
        ) : UiState
        data class Error(val message: String) : UiState
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    var selectedCategory: String? = null
        private set

    private var nextCursor: String? = null
    private var hasMore = false

    val categories = listOf(
        null to "Tümü",
        "FINANS" to "Finans",
        "MEVZUAT" to "Mevzuat",
        "VERGI" to "Vergi",
        "IS_DUNYASI" to "İş Dünyası",
        "DIJITALLESME" to "Dijitalleşme",
        "DESTEK" to "Destek",
        "GENEL_EKONOMI" to "Genel Ekonomi"
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.getFeed(selectedCategory, null).onSuccess { feed ->
                nextCursor = feed.nextCursor
                hasMore = feed.nextCursor != null
                _uiState.value = UiState.Content(articles = feed.items)
            }.onFailure { e ->
                _uiState.value = UiState.Error(e.message ?: "Haberler yüklenemedi")
            }
        }
    }

    fun selectCategory(category: String?) {
        if (selectedCategory == category) return
        selectedCategory = category
        refresh()
    }

    fun loadMore() {
        val cursor = nextCursor ?: return
        if (!hasMore) return
        val current = _uiState.value as? UiState.Content ?: return
        if (current.loadingMore) return
        viewModelScope.launch {
            _uiState.value = current.copy(loadingMore = true)
            repository.getFeed(selectedCategory, cursor).onSuccess { feed ->
                nextCursor = feed.nextCursor
                hasMore = feed.nextCursor != null
                _uiState.value = current.copy(
                    articles = current.articles + feed.items,
                    loadingMore = false
                )
            }.onFailure {
                _uiState.value = current.copy(loadingMore = false)
            }
        }
    }

    fun articleById(id: String): NewsArticleDto? {
        return (_uiState.value as? UiState.Content)?.articles?.firstOrNull { it.id == id }
    }

    fun canLoadMore(): Boolean = hasMore
}