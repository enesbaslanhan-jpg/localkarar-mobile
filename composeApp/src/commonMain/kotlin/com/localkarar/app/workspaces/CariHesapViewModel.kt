package com.localkarar.app.workspaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.core.AppMessages
import com.localkarar.app.core.SharedFile
import com.localkarar.app.network.dto.CariHareketDto
import com.localkarar.app.network.dto.CariHesapDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/*
 * CARİ HESAP — "Ahmet'e ne kadar borcum var?"
 *
 * 🔴 Bu sorunun cevabı üründe hiçbir yerde yoktu; kişi kartı
 * ad/telefon/şehir gösteriyordu. Bakiye SUNUCUDA hesaplanıyor: aynı
 * karar web ve mobilde ayrı ayrı yazılsa biri "kapanan kayıt sayılır
 * mı" sorusunu farklı yanıtlayınca iki ekran birbirini tutmazdı.
 */
sealed class CariHesapUiState {
    object Loading : CariHesapUiState()
    data class Content(
        val hesap: CariHesapDto,
        /** Ekstre indiriliyor; düğme kilitli. */
        val ekstreHazirlaniyor: Boolean = false
    ) : CariHesapUiState()
    data class Error(val message: String) : CariHesapUiState()
}

class CariHesapViewModel(
    private val workspaceId: String,
    private val contactId: String,
    private val repository: WorkspaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CariHesapUiState>(CariHesapUiState.Loading)
    val uiState: StateFlow<CariHesapUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = CariHesapUiState.Loading
        viewModelScope.launch {
            val sonuc = repository.getCariHesap(workspaceId, contactId)
            _uiState.value = if (sonuc.isSuccess) {
                CariHesapUiState.Content(sonuc.getOrThrow())
            } else {
                CariHesapUiState.Error(
                    sonuc.exceptionOrNull()?.message ?: "Cari hesap yüklenemedi."
                )
            }
        }
    }

    /*
     * EKSTRE PAYLAŞIMI.
     *
     * ⚠️ Webde "indir", telefonda PAYLAŞ. İndirilenler klasörü telefonda
     * aynı şey değil: kullanıcı ekstreyi muhasebecisine göndermek ya da
     * Dosyalar'a kaydetmek istiyor; sistem paylaşım sayfası ikisini de
     * veriyor. `RecordDetailViewModel.pdfPaylas` ile aynı desen.
     */
    fun ekstrePaylas(paylas: (SharedFile) -> Unit) {
        val mevcut = _uiState.value
        if (mevcut !is CariHesapUiState.Content || mevcut.ekstreHazirlaniyor) return
        _uiState.value = mevcut.copy(ekstreHazirlaniyor = true)
        viewModelScope.launch {
            repository.cariEkstrePdf(workspaceId, contactId)
                .onSuccess { veri ->
                    _uiState.value = mevcut.copy(ekstreHazirlaniyor = false)
                    if (veri.isEmpty()) {
                        AppMessages.hata("Ekstre oluşturulamadı.")
                        return@onSuccess
                    }
                    /*
                     * 🔴 DOSYA ADI KULLANICIYA GORUNUYOR.
                     *
                     * Once "cari-ekstre-<uuid>.pdf" idi; emulatorde
                     * paylasim sayfasinda aynen boyle goruldu
                     * (10.09.2026). Muhasebeciye giden dosyanin adi
                     * makine kimligi olmamali.
                     *
                     * ⚠️ Ad ASCII'ye indirgeniyor: bazi hedef
                     * uygulamalar Turkce karakterli dosya adini bozuk
                     * gosteriyor. Ad okunamaz hale gelirse kimlik
                     * yedege dusuyor.
                     */
                    paylas(
                        SharedFile(
                            name = "${dosyaAdiSlug(mevcut.hesap.contact.name, contactId)}-ekstre.pdf",
                            mimeType = "application/pdf",
                            bytes = veri
                        )
                    )
                }
                .onFailure { hata ->
                    _uiState.value = mevcut.copy(ekstreHazirlaniyor = false)
                    AppMessages.hata(hata.message ?: "Ekstre indirilemedi.")
                }
        }
    }
}

/**
 * Dosya adi icin ASCII sadelestirme.
 *
 * Sunucu ayni isi `safeFileSlug` ile yapiyor (workspace-exports.ts);
 * burada tekrarlanmasinin sebebi `getBytes`in yanit basliklarini
 * disari vermemesi -- sunucunun urettigi adi okuyamiyoruz.
 */
internal fun dosyaAdiSlug(ad: String, yedek: String): String {
    val harita = mapOf(
        'ç' to "c", 'Ç' to "C", 'ğ' to "g", 'Ğ' to "G", 'ı' to "i", 'İ' to "I",
        'ö' to "o", 'Ö' to "O", 'ş' to "s", 'Ş' to "S", 'ü' to "u", 'Ü' to "U"
    )
    val sade = ad
        .map { harita[it] ?: it.toString() }
        .joinToString("")
        .map { if (it.isLetterOrDigit() && it.code < 128) it else '-' }
        .joinToString("")
        .trim('-')
        .replace(Regex("-+"), "-")
        .take(40)
    return if (sade.isBlank()) yedek else sade
}

/** Kapanmış hareket: bakiyeye girmiyor ama ekstrede duruyor. */
fun CariHareketDto.kapandiMi(): Boolean = status == "completed" || status == "cancelled"
