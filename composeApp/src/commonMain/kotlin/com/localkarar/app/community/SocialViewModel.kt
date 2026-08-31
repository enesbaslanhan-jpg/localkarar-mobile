package com.localkarar.app.community

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SocialViewModel(
    private val repository: CommunityRepository
) : ViewModel() {

    // ==========================================
    // PEOPLE STATE
    // ==========================================

    sealed interface PeopleUiState {
        data object Loading : PeopleUiState
        data class Content(val people: List<PersonDto>) : PeopleUiState
        data class Error(val message: String) : PeopleUiState
    }

    private val _peopleState = MutableStateFlow<PeopleUiState>(PeopleUiState.Loading)
    val peopleState: StateFlow<PeopleUiState> = _peopleState

    var followingIds by mutableStateOf<Set<Int>>(emptySet())
        private set
    var blockedIds by mutableStateOf<Set<Int>>(emptySet())
        private set
    var searchQuery by mutableStateOf("")
        private set

    private var searchJob: Job? = null

    // ==========================================
    // OWN PROFILE STATE
    // ==========================================

    sealed interface OwnProfileUiState {
        data object Loading : OwnProfileUiState
        data class Content(
            val summary: OwnSummaryDto,
            val posts: List<CommunityPostDto> = emptyList(),
            val likes: List<CommunityPostDto> = emptyList(),
            val bookmarks: List<CommunityPostDto> = emptyList()
        ) : OwnProfileUiState
        data class Error(val message: String) : OwnProfileUiState
    }

    private val _ownProfileState = MutableStateFlow<OwnProfileUiState>(OwnProfileUiState.Loading)
    val ownProfileState: StateFlow<OwnProfileUiState> = _ownProfileState

    var ownProfileTab by mutableStateOf("posts") // "posts", "likes", "bookmarks"

    // ==========================================
    // OTHER PROFILE STATE
    // ==========================================

    sealed interface OtherProfileUiState {
        data object Idle : OtherProfileUiState
        data object Loading : OtherProfileUiState
        data class Content(
            val profile: OtherProfileDto,
            val sayilar: OtherProfileSayilarDto,
            val takipEdiyorum: Boolean,
            val posts: List<CommunityPostDto> = emptyList(),
            val mediaPosts: List<CommunityPostDto> = emptyList()
        ) : OtherProfileUiState
        data class Error(val message: String) : OtherProfileUiState
    }

    private val _otherProfileState = MutableStateFlow<OtherProfileUiState>(OtherProfileUiState.Idle)
    val otherProfileState: StateFlow<OtherProfileUiState> = _otherProfileState

    var otherProfileTab by mutableStateOf("posts") // "posts", "media"

    // ==========================================
    // FOLLOWERS / FOLLOWING LIST STATE
    // ==========================================

    sealed interface FollowListUiState {
        data object Loading : FollowListUiState
        data class Content(val title: String, val people: List<PersonDto>) : FollowListUiState
        data class Error(val message: String) : FollowListUiState
    }

    private val _followListState = MutableStateFlow<FollowListUiState>(FollowListUiState.Loading)
    val followListState: StateFlow<FollowListUiState> = _followListState

    var notice by mutableStateOf<String?>(null)
        private set

    init {
        loadPeople("")
    }

    // ==========================================
    // PEOPLE METHODS
    // ==========================================

    fun onSearchQueryChange(query: String) {
        searchQuery = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            loadPeople(query)
        }
    }

    fun loadPeople(q: String) {
        viewModelScope.launch {
            _peopleState.value = PeopleUiState.Loading
            repository.getPeople(q).onSuccess { res ->
                followingIds = res.followingIds.toSet()
                blockedIds = res.blockedIds.toSet()
                _peopleState.value = PeopleUiState.Content(res.people)
            }.onFailure { e ->
                _peopleState.value = PeopleUiState.Error(e.message ?: "Kişiler yüklenemedi")
            }
        }
    }

    fun toggleFollow(personId: Int) {
        val isFollowing = followingIds.contains(personId)
        val isBlocked = blockedIds.contains(personId)
        if (isBlocked) {
            notice = "Engellenmiş kullanıcı takip edilemez"
            return
        }

        // Optimistic
        followingIds = if (isFollowing) followingIds - personId else followingIds + personId

        viewModelScope.launch {
            val result = if (isFollowing) repository.unfollow(personId) else repository.follow(personId)
            result.onSuccess { res ->
                followingIds = if (res) followingIds + personId else followingIds - personId
                // If on other profile, update that state too
                val other = _otherProfileState.value as? OtherProfileUiState.Content
                if (other != null && other.profile.id == personId) {
                    val countChange = if (res) 1 else -1
                    _otherProfileState.value = other.copy(
                        takipEdiyorum = res,
                        sayilar = other.sayilar.copy(
                            takipci = maxOf(0, other.sayilar.takipci + countChange)
                        )
                    )
                }
            }.onFailure {
                // Rollback
                followingIds = if (isFollowing) followingIds + personId else followingIds - personId
                notice = "Takip işlemi başarısız"
            }
        }
    }

    fun toggleBlock(personId: Int) {
        val isBlocked = blockedIds.contains(personId)

        // Optimistic
        blockedIds = if (isBlocked) blockedIds - personId else blockedIds + personId
        if (!isBlocked) {
            followingIds = followingIds - personId
        }

        viewModelScope.launch {
            val result = if (isBlocked) repository.unblock(personId) else repository.block(personId)
            result.onSuccess { res ->
                blockedIds = if (res) blockedIds + personId else blockedIds - personId
                if (res) followingIds = followingIds - personId
                notice = if (res) "Kullanıcı engellendi" else "Engel kaldırıldı"
            }.onFailure {
                // Rollback
                blockedIds = if (isBlocked) blockedIds + personId else blockedIds - personId
                notice = "Engelleme işlemi başarısız"
            }
        }
    }

    fun reportUser(personId: Int, reason: String, details: String?) {
        viewModelScope.launch {
            repository.reportUser(personId, reason, details).onSuccess {
                notice = "Kullanıcı şikayeti iletildi"
            }.onFailure { e ->
                notice = e.message ?: "Şikayet gönderilemedi"
            }
        }
    }

    // ==========================================
    // OWN PROFILE METHODS
    // ==========================================

    fun loadOwnProfile() {
        viewModelScope.launch {
            _ownProfileState.value = OwnProfileUiState.Loading
            repository.getOwnSummary().onSuccess { summary ->
                val posts = repository.getOwnList("posts").getOrDefault(emptyList())
                val likes = repository.getOwnList("likes").getOrDefault(emptyList())
                val bookmarks = repository.getOwnList("bookmarks").getOrDefault(emptyList())

                _ownProfileState.value = OwnProfileUiState.Content(
                    summary = summary,
                    posts = posts,
                    likes = likes,
                    bookmarks = bookmarks
                )
            }.onFailure { e ->
                _ownProfileState.value = OwnProfileUiState.Error(e.message ?: "Profil yüklenemedi")
            }
        }
    }

    // ==========================================
    // OTHER PROFILE METHODS
    // ==========================================

    fun loadOtherProfile(userId: Int) {
        viewModelScope.launch {
            _otherProfileState.value = OtherProfileUiState.Loading
            repository.getOtherProfile(userId).onSuccess { res ->
                val posts = repository.getOtherUserPosts(userId).getOrDefault(emptyList())
                val mediaPosts = repository.getOtherUserPosts(userId, tur = "media").getOrDefault(emptyList())

                _otherProfileState.value = OtherProfileUiState.Content(
                    profile = res.profil,
                    sayilar = res.sayilar,
                    takipEdiyorum = res.takipEdiyorum,
                    posts = posts,
                    mediaPosts = mediaPosts
                )
            }.onFailure { e ->
                _otherProfileState.value = OtherProfileUiState.Error(e.message ?: "Profil yüklenemedi")
            }
        }
    }

    // ==========================================
    // FOLLOWERS / FOLLOWING LIST METHODS
    // ==========================================

    fun loadFollowList(userId: Int, mode: String) {
        viewModelScope.launch {
            _followListState.value = FollowListUiState.Loading
            val isFollowers = mode == "followers"
            val title = if (isFollowers) "Takipçiler" else "Takip Edilenler"
            val result = if (isFollowers) {
                repository.getProfileFollowers(userId)
            } else {
                repository.getProfileFollowing(userId)
            }

            result.onSuccess { people ->
                _followListState.value = FollowListUiState.Content(title = title, people = people)
            }.onFailure { e ->
                _followListState.value = FollowListUiState.Error(e.message ?: "Liste yüklenemedi")
            }
        }
    }

    fun clearNotice() {
        notice = null
    }
}
