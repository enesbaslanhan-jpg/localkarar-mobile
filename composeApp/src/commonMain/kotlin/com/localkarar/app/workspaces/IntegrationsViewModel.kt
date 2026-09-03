package com.localkarar.app.workspaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.core.AppMessages
import com.localkarar.app.core.openExternalUrl
import com.localkarar.app.network.dto.HepsiburadaConnectRequestDto
import com.localkarar.app.network.dto.IntegrationConnectionDto
import com.localkarar.app.network.dto.MarketplaceEntryDto
import com.localkarar.app.network.dto.N11ConnectRequestDto
import com.localkarar.app.network.dto.ShopifyConnectRequestDto
import com.localkarar.app.network.dto.TrendyolConnectRequestDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class IntegrationsUiState {
    object Loading : IntegrationsUiState()
    data class Content(
        val katalog: List<MarketplaceEntryDto>,
        val baglantilar: List<IntegrationConnectionDto>
    ) : IntegrationsUiState()

    data class Error(val mesaj: String, val hata: Throwable?) : IntegrationsUiState()
}

/**
 * Pazaryeri entegrasyonlari.
 *
 * Bu ekran YOKTU ve yoklugu, uydurma veri arizasinin gorunmez kalmasinin asil
 * sebebiydi: kullanici mobilden hicbir pazaryeri baglayamadigi icin gercek
 * siparis/urun verisi hicbir zaman gelmiyordu.
 */
class IntegrationsViewModel(
    private val repository: WorkspaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<IntegrationsUiState>(IntegrationsUiState.Loading)
    val uiState: StateFlow<IntegrationsUiState> = _uiState.asStateFlow()

    private val _islemDevamEdiyor = MutableStateFlow(false)
    val islemDevamEdiyor: StateFlow<Boolean> = _islemDevamEdiyor.asStateFlow()

    fun yukle(workspaceId: String) {
        if (workspaceId.isBlank()) return
        _uiState.value = IntegrationsUiState.Loading
        viewModelScope.launch {
            repository.getWorkspaceIntegrations(workspaceId)
                .onSuccess { yanit ->
                    _uiState.value = IntegrationsUiState.Content(
                        katalog = yanit.marketplaces,
                        baglantilar = yanit.connections
                    )
                }
                .onFailure { hata ->
                    _uiState.value = IntegrationsUiState.Error(
                        mesaj = hata.message ?: "Entegrasyonlar yüklenemedi.",
                        hata = hata
                    )
                }
        }
    }

    /**
     * Baglanma sonucu.
     *
     * Sunucu kimlik bilgilerini ONCE DOGRULUYOR ve gecersizse veritabanina
     * YAZMIYOR; dolayisiyla buradaki hata gercek bir dogrulama sonucu ve
     * kullaniciya OLDUGU GIBI gosteriliyor. Genel bir "bir hata olustu"
     * mesajina cevirmek, kullanicinin hangi alani duzeltmesi gerektigini
     * gizlerdi.
     */
    private fun sonucuIsle(workspaceId: String, sonuc: Result<Unit>, basariMesaji: String) {
        sonuc
            .onSuccess {
                AppMessages.bilgi(basariMesaji)
                yukle(workspaceId)
            }
            .onFailure { hata ->
                AppMessages.hata(hata.message ?: "Bağlantı kurulamadı.")
            }
        _islemDevamEdiyor.value = false
    }

    fun trendyolBagla(workspaceId: String, merchantId: String, apiKey: String, apiSecret: String) {
        if (_islemDevamEdiyor.value) return
        _islemDevamEdiyor.value = true
        viewModelScope.launch {
            sonucuIsle(
                workspaceId,
                repository.connectTrendyol(
                    TrendyolConnectRequestDto(
                        workspaceId = workspaceId,
                        merchantId = merchantId.trim(),
                        apiKey = apiKey.trim(),
                        apiSecret = apiSecret.trim()
                    )
                ),
                "Trendyol bağlandı."
            )
        }
    }

    fun hepsiburadaBagla(workspaceId: String, merchantId: String, username: String, password: String) {
        if (_islemDevamEdiyor.value) return
        _islemDevamEdiyor.value = true
        viewModelScope.launch {
            sonucuIsle(
                workspaceId,
                repository.connectHepsiburada(
                    HepsiburadaConnectRequestDto(
                        workspaceId = workspaceId,
                        merchantId = merchantId.trim(),
                        username = username.trim(),
                        password = password.trim()
                    )
                ),
                "Hepsiburada bağlandı."
            )
        }
    }

    fun n11Bagla(workspaceId: String, storeName: String, appKey: String, appSecret: String) {
        if (_islemDevamEdiyor.value) return
        _islemDevamEdiyor.value = true
        viewModelScope.launch {
            sonucuIsle(
                workspaceId,
                repository.connectN11(
                    N11ConnectRequestDto(
                        workspaceId = workspaceId,
                        storeName = storeName.trim(),
                        appKey = appKey.trim(),
                        appSecret = appSecret.trim()
                    )
                ),
                "N11 bağlandı."
            )
        }
    }

    /**
     * Shopify OAuth: sunucudan gelen yetkilendirme adresi HARICI TARAYICIDA
     * aciliyor.
     *
     * Uygulama ici WebView bilincli olarak KULLANILMIYOR: kullanicidan Shopify
     * hesabinin parolasi isteniyor ve uygulamanin gosterdigi bir goruntuleyicide
     * parola toplamak hem guvenlik hem magaza incelemesi acisindan yanlis.
     */
    fun shopifyBagla(workspaceId: String, shopDomain: String) {
        if (_islemDevamEdiyor.value) return
        _islemDevamEdiyor.value = true
        viewModelScope.launch {
            repository.connectShopify(
                ShopifyConnectRequestDto(workspaceId = workspaceId, shopDomain = shopDomain.trim())
            )
                .onSuccess { yanit ->
                    val adres = yanit.authorizationUrl
                    if (adres.isNullOrBlank()) {
                        AppMessages.hata("Shopify yetkilendirme adresi alınamadı.")
                    } else {
                        openExternalUrl(adres)
                        AppMessages.bilgi("Shopify izni tarayıcıda açıldı. İşlem bitince buraya dönün.")
                    }
                }
                .onFailure { hata ->
                    AppMessages.hata(hata.message ?: "Shopify bağlantısı başlatılamadı.")
                }
            _islemDevamEdiyor.value = false
        }
    }

    fun baglantiyiKes(workspaceId: String, provider: String) {
        if (_islemDevamEdiyor.value) return
        _islemDevamEdiyor.value = true
        viewModelScope.launch {
            sonucuIsle(
                workspaceId,
                repository.disconnectIntegration(workspaceId, provider),
                "Bağlantı kesildi."
            )
        }
    }

    fun simdiEsitle(workspaceId: String, provider: String) {
        if (_islemDevamEdiyor.value) return
        _islemDevamEdiyor.value = true
        viewModelScope.launch {
            repository.syncOrders(workspaceId, provider)
                .onSuccess {
                    // Sunucu esitlemeyi BASLATIYOR, bitirmiyor. "Su kadar kayit
                    // geldi" denemez; o bilgi bu yanitta YOK.
                    AppMessages.bilgi("Eşitleme başlatıldı. Tamamlanınca kayıtlar görünecek.")
                    yukle(workspaceId)
                }
                .onFailure { hata ->
                    AppMessages.hata(hata.message ?: "Eşitleme başlatılamadı.")
                }
            _islemDevamEdiyor.value = false
        }
    }
}
