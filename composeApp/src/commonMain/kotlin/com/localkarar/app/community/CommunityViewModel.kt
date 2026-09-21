package com.localkarar.app.community

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.CommunityMediaDto
import com.localkarar.app.network.dto.CommunityPostDto
import com.localkarar.app.network.dto.QuotedPostDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

class CommunityViewModel(
    private val repository: CommunityRepository
) : ViewModel() {

    sealed interface FeedUiState {
        data object Loading : FeedUiState
        data class Content(
            val posts: List<CommunityPostDto> = emptyList(),
            val loadingMore: Boolean = false
        ) : FeedUiState
        data class Error(val message: String) : FeedUiState
    }

    sealed interface DetailUiState {
        data object Idle : DetailUiState
        data object Loading : DetailUiState
        data class Content(
            val post: CommunityPostDto,
            val parent: CommunityPostDto? = null
        ) : DetailUiState
        data class Error(val message: String) : DetailUiState
    }

    private val _feedState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val feedState: StateFlow<FeedUiState> = _feedState

    private val _detailState = MutableStateFlow<DetailUiState>(DetailUiState.Idle)
    val detailState: StateFlow<DetailUiState> = _detailState

    var selectedType: String? = null
        private set
    var notice by mutableStateOf<String?>(null)
        private set
    var composing by mutableStateOf(false)
        private set

    // Compose state
    var metinInput by mutableStateOf("")
        private set
    var attachedMedia by mutableStateOf<CommunityMediaDto?>(null)
    /** Secilen gorselin bellekteki onizlemesi; sunucu URL'i beklenmez (19.09.2026). */
    var attachedPreview by mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null)
    var attachedIsVideo by mutableStateOf(false)
        private set
    var isUploadingMedia by mutableStateOf(false)
        private set
    var mediaUploadProgress by mutableStateOf(0f)
        private set
    private var mediaUploadJob: Job? = null
    var replyTargetPost by mutableStateOf<CommunityPostDto?>(null)
        private set
    var quoteTargetPost by mutableStateOf<CommunityPostDto?>(null)
        private set
    var isSubmittingPost by mutableStateOf(false)
        private set

    private var nextCursor: String? = null

    val tabs = listOf(
        null to "Tümü",
        "official" to "Resmi",
        "user" to "Topluluk"
    )

    init {
        refreshFeed()
    }

    // ==========================================
    // FEED
    // ==========================================

    fun refreshFeed() {
        viewModelScope.launch {
            _feedState.value = FeedUiState.Loading
            repository.getFeed(selectedType, null).onSuccess { feed ->
                nextCursor = feed.nextCursor
                _feedState.value = FeedUiState.Content(posts = feed.posts)
            }.onFailure { e ->
                _feedState.value = FeedUiState.Error(e.message ?: "Gönderiler yüklenemedi")
            }
        }
    }

    /** Bekleyen video varken akisi beyazlatmadan yalniz veriyi tazeler. */
    fun refreshProcessingMedia() {
        val current = _feedState.value as? FeedUiState.Content
        if (current != null && current.posts.any { it.media?.status == "processing" }) {
            viewModelScope.launch {
                repository.getFeed(selectedType, null).onSuccess { feed ->
                    nextCursor = feed.nextCursor
                    _feedState.value = current.copy(posts = feed.posts)
                }
            }
        }
        val detail = _detailState.value as? DetailUiState.Content
        if (detail?.post?.media?.status == "processing") {
            viewModelScope.launch {
                repository.getPost(detail.post.id).onSuccess { fresh ->
                    _detailState.value = DetailUiState.Content(fresh.post, fresh.parent)
                }
            }
        }
    }

    fun selectType(type: String?) {
        if (selectedType == type) return
        selectedType = type
        refreshFeed()
    }

    fun loadMore() {
        val cursor = nextCursor ?: return
        val current = _feedState.value as? FeedUiState.Content ?: return
        if (current.loadingMore) return

        viewModelScope.launch {
            _feedState.value = current.copy(loadingMore = true)
            repository.getFeed(selectedType, cursor).onSuccess { feed ->
                nextCursor = feed.nextCursor
                _feedState.value = FeedUiState.Content(
                    posts = current.posts + feed.posts,
                    loadingMore = false
                )
            }.onFailure {
                _feedState.value = current.copy(loadingMore = false)
            }
        }
    }

    // ==========================================
    // POST DETAIL
    // ==========================================

    fun loadPostDetail(postId: String) {
        viewModelScope.launch {
            _detailState.value = DetailUiState.Loading
            repository.getPost(postId).onSuccess { detail ->
                _detailState.value = DetailUiState.Content(
                    post = detail.post,
                    parent = detail.parent
                )
            }.onFailure { e ->
                _detailState.value = DetailUiState.Error(e.message ?: "Gönderi yüklenemedi")
            }
        }
    }

    // ==========================================
    // INTERACTIONS: LIKE / BOOKMARK / DELETE
    // ==========================================

    fun toggleLike(postId: String, currentBegendim: Boolean) {
        // Optimistic update on Feed
        updatePostInFeed(postId) { post ->
            val newBegendim = !currentBegendim
            val newCount = if (newBegendim) post.begeniSayisi + 1 else maxOf(0, post.begeniSayisi - 1)
            post.copy(begendim = newBegendim, begeniSayisi = newCount)
        }
        updatePostInDetail(postId) { post ->
            val newBegendim = !currentBegendim
            val newCount = if (newBegendim) post.begeniSayisi + 1 else maxOf(0, post.begeniSayisi - 1)
            post.copy(begendim = newBegendim, begeniSayisi = newCount)
        }

        viewModelScope.launch {
            val result = if (currentBegendim) repository.unlikePost(postId) else repository.likePost(postId)
            result.onSuccess { res ->
                updatePostInFeed(postId) { it.copy(begendim = res.aktif, begeniSayisi = res.sayi) }
                updatePostInDetail(postId) { it.copy(begendim = res.aktif, begeniSayisi = res.sayi) }
            }.onFailure {
                // Rollback
                updatePostInFeed(postId) { it.copy(begendim = currentBegendim) }
                updatePostInDetail(postId) { it.copy(begendim = currentBegendim) }
                notice = "Beğeni güncellenemedi"
            }
        }
    }

    fun toggleBookmark(postId: String, currentKaydettim: Boolean) {
        // Optimistic update
        updatePostInFeed(postId) { post ->
            post.copy(kaydettim = !currentKaydettim)
        }
        updatePostInDetail(postId) { post ->
            post.copy(kaydettim = !currentKaydettim)
        }

        viewModelScope.launch {
            val result = if (currentKaydettim) repository.unbookmarkPost(postId) else repository.bookmarkPost(postId)
            result.onSuccess { res ->
                updatePostInFeed(postId) { it.copy(kaydettim = res.aktif) }
                updatePostInDetail(postId) { it.copy(kaydettim = res.aktif) }
            }.onFailure {
                updatePostInFeed(postId) { it.copy(kaydettim = currentKaydettim) }
                updatePostInDetail(postId) { it.copy(kaydettim = currentKaydettim) }
                notice = "Kaydetme güncellenemedi"
            }
        }
    }

    fun deletePost(postId: String, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deletePost(postId).onSuccess {
                notice = "Gönderi kaldırıldı"
                // Remove from feed
                val current = _feedState.value as? FeedUiState.Content
                if (current != null) {
                    _feedState.value = current.copy(posts = current.posts.filter { it.id != postId })
                }
                onDeleted()
            }.onFailure { e ->
                notice = e.message ?: "Gönderi kaldırılamadı"
            }
        }
    }

    fun reportPost(postId: String, reason: String, details: String?) {
        viewModelScope.launch {
            repository.reportPost(postId, reason, details).onSuccess {
                notice = "Şikayetiniz iletildi"
            }.onFailure { e ->
                notice = e.message ?: "Şikayet gönderilemedi"
            }
        }
    }

    // ==========================================
    // COMPOSE / REPLY / QUOTE
    // ==========================================

    fun startCompose(replyTo: CommunityPostDto? = null, quoteOf: CommunityPostDto? = null) {
        metinInput = ""
        attachedMedia = null
        replyTargetPost = replyTo
        quoteTargetPost = quoteOf
        composing = true
    }

    fun onMetinChange(value: String) {
        if (value.length <= 500) {
            metinInput = value
        }
    }

    fun onMediaSelected(fileName: String, bytes: ByteArray, mimeType: String) {
        mediaUploadJob?.cancel()
        attachedPreview = if (mimeType.startsWith("image/")) com.localkarar.app.core.gorseliCoz(bytes) else null
        attachedIsVideo = mimeType.startsWith("video/")
        mediaUploadJob = viewModelScope.launch {
            isUploadingMedia = true
            mediaUploadProgress = 0f
            repository.uploadMedia(fileName, bytes, mimeType) { progress ->
                mediaUploadProgress = progress
            }.onSuccess { res ->
                attachedMedia = res.media
            }.onFailure { e ->
                isUploadingMedia = false
                notice = e.message ?: "Görsel yüklenemedi"
            }
            isUploadingMedia = false
            mediaUploadProgress = 0f
            mediaUploadJob = null
        }
    }

    fun cancelMediaUpload() {
        mediaUploadJob?.cancel()
        mediaUploadJob = null
        isUploadingMedia = false
        mediaUploadProgress = 0f
        notice = "Yükleme iptal edildi"
    }

    fun removeAttachedMedia() {
        val mediaId = attachedMedia?.id
        attachedMedia = null
        attachedPreview = null
        attachedIsVideo = false
        if (mediaId != null) {
            viewModelScope.launch {
                repository.discardMedia(mediaId)
            }
        }
    }

    fun dismissCompose() {
        if (isUploadingMedia) cancelMediaUpload()
        if (attachedMedia != null) {
            removeAttachedMedia()
        }
        composing = false
        replyTargetPost = null
        quoteTargetPost = null
        notice = null
    }

    fun submitPost(onSuccess: (String?) -> Unit = {}) {
        if (isSubmittingPost) return

        val metin = metinInput.trim()
        val media = attachedMedia
        val parentId = replyTargetPost?.id
        val quotedPostId = quoteTargetPost?.id

        if (metin.isBlank() && media == null && quotedPostId == null) {
            notice = "Bir şeyler yazın veya bir görsel ekleyin"
            return
        }

        isSubmittingPost = true
        viewModelScope.launch {
            try {
                repository.createPost(
                    metin = metin,
                    mediaId = media?.id,
                    parentId = parentId,
                    quotedPostId = quotedPostId
                ).onSuccess { res ->
                    isSubmittingPost = false
                    composing = false
                    metinInput = ""
                    attachedMedia = null
                    replyTargetPost = null
                    quoteTargetPost = null
                    notice = "Paylaşımın yayımlandı."
                    refreshFeed()
                    if (parentId != null) {
                        loadPostDetail(parentId)
                    }
                    onSuccess(res.post?.id)
                }.onFailure { e ->
                    isSubmittingPost = false
                    notice = e.message ?: "Paylaşım oluşturulamadı"
                }
            } catch (e: Exception) {
                isSubmittingPost = false
                notice = e.message ?: "Paylaşım oluşturulamadı"
            }
        }
    }

    fun clearNotice() {
        notice = null
    }

    // ==========================================
    // HELPERS
    // ==========================================

    /**
     * Engellenen yazarin gonderilerini akistan ve acik detaydan HEMEN cikarir.
     *
     * 🔴 Canli (21.09.2026, App Store inceleme videosu cekilirken): kullanici
     * profilde "Engelle"ye basti, akisa dondu, gonderi hala duruyordu. Sunucu
     * akisi zaten filtreliyor (community.ts, authorId notIn) ama uygulama
     * akisi yeniden cekmiyordu; yeni istek atilana kadar eski liste kaliyordu.
     * Yeniden yukleme yerine yerel silme: aninda, beyaz ekran yok.
     */
    fun yazariGizle(authorId: Int) {
        (_feedState.value as? FeedUiState.Content)?.let { current ->
            _feedState.value = current.copy(
                posts = current.posts.filterNot { (it.authorId ?: it.author?.id) == authorId }
            )
        }
        (_detailState.value as? DetailUiState.Content)?.let { current ->
            if ((current.post.authorId ?: current.post.author?.id) == authorId) {
                _detailState.value = DetailUiState.Error("Bu kullanıcıyı engellediniz.")
            }
        }
    }

    private fun updatePostInFeed(postId: String, transform: (CommunityPostDto) -> CommunityPostDto) {
        val current = _feedState.value as? FeedUiState.Content ?: return
        _feedState.value = current.copy(
            posts = current.posts.map { if (it.id == postId) transform(it) else it }
        )
    }

    private fun updatePostInDetail(postId: String, transform: (CommunityPostDto) -> CommunityPostDto) {
        val current = _detailState.value as? DetailUiState.Content ?: return
        if (current.post.id == postId) {
            _detailState.value = current.copy(post = transform(current.post))
        } else {
            // Check replies recursively
            _detailState.value = current.copy(
                post = current.post.copy(replies = updateRepliesTree(current.post.replies, postId, transform))
            )
        }
    }

    private fun updateRepliesTree(
        replies: List<CommunityPostDto>,
        postId: String,
        transform: (CommunityPostDto) -> CommunityPostDto
    ): List<CommunityPostDto> {
        return replies.map { reply ->
            if (reply.id == postId) {
                transform(reply)
            } else {
                reply.copy(replies = updateRepliesTree(reply.replies, postId, transform))
            }
        }
    }
}
