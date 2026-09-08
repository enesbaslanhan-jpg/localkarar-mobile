package com.localkarar.app.workspaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Ekip davetini kabul etme durumu.
 *
 * 🔴 BU AKIS MOBILDE HIC YOKTU. Ekip ekrani davet GONDEREBILIYOR ama
 * gelen daveti kabul etmenin bir yolu yoktu; e-postadaki baglantiyi
 * telefonunda acan kisi tarayiciya dusuyordu.
 */
sealed class InvitationUiState {
    /** Kullanici "Daveti kabul et" demeden istek ATILMIYOR — bkz. ViewModel. */
    object Hazir : InvitationUiState()
    object Gonderiliyor : InvitationUiState()
    data class Basarili(val workspaceId: String?) : InvitationUiState()
    data class Hata(val mesaj: String) : InvitationUiState()
}

class InvitationAcceptViewModel(
    private val token: String,
    private val repository: WorkspaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<InvitationUiState>(InvitationUiState.Hazir)
    val uiState: StateFlow<InvitationUiState> = _uiState.asStateFlow()

    /*
     * 🔴 ACILIR ACILMAZ KABUL EDILMIYOR.
     *
     * Baglantiya dokunmak "kabul ediyorum" demek degil: davet bir
     * calisma alanina UYE olmak, yani o isletmenin kayitlarina,
     * kisilerine ve belgelerine erisim kazanmak. Kullanici neye
     * katildigini gorup ONAYLAMALI. Web de ayni sekilde bir ekran
     * gosteriyor (`InvitationPage.jsx`), sessizce kabul etmiyor.
     *
     * ⚠️ Tek atislik: `Gonderiliyor` iken ikinci cagri yok sayiliyor.
     * Iki istek giderse ikincisi sunucudan "zaten uyesiniz" hatasi alir
     * ve kullanici basarili bir islemin ardindan hata gorurdu.
     */
    fun kabulEt() {
        if (_uiState.value is InvitationUiState.Gonderiliyor) return
        _uiState.value = InvitationUiState.Gonderiliyor
        viewModelScope.launch {
            repository.acceptInvitation(token)
                .onSuccess { yanit ->
                    _uiState.value = InvitationUiState.Basarili(yanit.workspaceId)
                }
                .onFailure { hata ->
                    _uiState.value = InvitationUiState.Hata(
                        hata.message ?: "Davet kabul edilemedi."
                    )
                }
        }
    }
}
