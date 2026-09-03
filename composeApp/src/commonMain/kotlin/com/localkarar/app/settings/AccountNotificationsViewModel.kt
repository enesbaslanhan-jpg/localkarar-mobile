package com.localkarar.app.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.core.AppMessages
import com.localkarar.app.network.ApiConfig
import com.localkarar.app.network.SafeApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

/**
 * HESAP BILDIRIMLERI.
 *
 * 🔴 MOBILDE HIC YOKTU. Webde `/app/bildirimler` canli.
 *
 * Mobilde iki ayri bildirim ekrani vardi (calisma alani ve topluluk) ama
 * HESAP bildirimleri yoktu -- uyelik uyarilari, sifre degisikligi bildirimleri
 * ve odeme sonuclari o kanaldan geliyor.
 *
 * ⚠️ `POST /account/notifications/read` uyelik kapisinin MUAF listesinde:
 * suresi dolmus kullanici, "sureniz doldu" diyen bildirimi kapatabilmeli.
 * Mobilde bu uc bagli olmadigi icin o uyari kapatilamiyordu.
 */
@Serializable
data class AccountNotificationDto(
    val id: String,
    val type: String? = null,
    val title: String,
    val body: String? = null,
    /** Uygulama ici hedef; derin baglanti yolu olarak gelebiliyor. */
    val linkTo: String? = null,
    val readAt: String? = null,
    val createdAt: String? = null
)

@Serializable
data class AccountNotificationListDto(
    val unread: Int = 0,
    val items: List<AccountNotificationDto> = emptyList()
)

class AccountNotificationsRepository(private val api: SafeApiClient) {

    private val base = ApiConfig.baseUrl

    suspend fun listele(): Result<AccountNotificationListDto> =
        api.get("$base/account/notifications")

    /** Tumunu okundu isaretler. Sunucu `{ okundu: <sayi> }` donuyor. */
    suspend fun tumunuOkunduIsaretle(): Result<Map<String, Int>> =
        api.post("$base/account/notifications/read", emptyMap<String, String>())
}

sealed class AccountNotificationsUiState {
    object Loading : AccountNotificationsUiState()
    data class Content(
        val okunmamis: Int,
        val bildirimler: List<AccountNotificationDto>
    ) : AccountNotificationsUiState()

    data class Error(val mesaj: String, val hata: Throwable?) : AccountNotificationsUiState()
}

class AccountNotificationsViewModel(
    private val repository: AccountNotificationsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AccountNotificationsUiState>(
        AccountNotificationsUiState.Loading
    )
    val uiState: StateFlow<AccountNotificationsUiState> = _uiState.asStateFlow()

    fun yukle() {
        _uiState.value = AccountNotificationsUiState.Loading
        viewModelScope.launch {
            repository.listele()
                .onSuccess { yanit ->
                    _uiState.value = AccountNotificationsUiState.Content(
                        okunmamis = yanit.unread,
                        bildirimler = yanit.items
                    )
                }
                .onFailure { hata ->
                    _uiState.value = AccountNotificationsUiState.Error(
                        mesaj = hata.message ?: "Bildirimler yüklenemedi.",
                        hata = hata
                    )
                }
        }
    }

    fun tumunuOkunduIsaretle() {
        viewModelScope.launch {
            repository.tumunuOkunduIsaretle()
                .onSuccess { yukle() }
                .onFailure { hata ->
                    AppMessages.hata(hata.message ?: "Bildirimler işaretlenemedi.")
                }
        }
    }
}
