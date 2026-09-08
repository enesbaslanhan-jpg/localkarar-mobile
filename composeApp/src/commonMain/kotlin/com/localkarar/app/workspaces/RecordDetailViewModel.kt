package com.localkarar.app.workspaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.core.AppMessages
import com.localkarar.app.core.SharedFile
import com.localkarar.app.network.dto.BusinessRecordDto
import com.localkarar.app.network.dto.RecordUpdateDto
import com.localkarar.app.network.dto.WorkspaceDocumentDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RecordDetailUiState {
    object Loading : RecordDetailUiState()
    data class Content(
        val record: BusinessRecordDto,
        val isActing: Boolean = false,
        /**
         * Belge secici acikken calisma alanindaki belgeler.
         *
         * ⚠️ `null` = HENUZ ISTENMEDI, bos liste = ISTENDI VE BOS.
         * Ikisini ayni degerle gostermek, "belge yok" ile "daha
         * yuklenmedi"yi ayni ekrana dusururdu.
         */
        val secilebilirBelgeler: List<WorkspaceDocumentDto>? = null,
        val belgeSeciciAcik: Boolean = false,
        val belgeYukleniyor: Boolean = false
    ) : RecordDetailUiState()
    data class Error(val message: String) : RecordDetailUiState()
}

class RecordDetailViewModel(
    private val workspaceId: String,
    private val recordId: String,
    private val repository: WorkspaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecordDetailUiState>(RecordDetailUiState.Loading)
    val uiState: StateFlow<RecordDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = RecordDetailUiState.Loading
        viewModelScope.launch {
            val result = repository.getRecord(workspaceId, recordId)
            if (result.isSuccess) {
                _uiState.value = RecordDetailUiState.Content(result.getOrThrow())
            } else {
                _uiState.value = RecordDetailUiState.Error(
                    result.exceptionOrNull()?.message ?: "Kayıt yüklenemedi."
                )
            }
        }
    }

    /*
     * KAYIT–BELGE BAGLAMA (madde 11).
     *
     * 🔴 Ne webde ne mobilde kullaniciya sunulmuyordu; bag yalnizca
     * belge analizinden URETILEN kayitlarda kuruluyordu. Elle yuklenmis
     * bir sozlesmeyi mevcut bir odemeye ilistirmenin yolu yoktu.
     *
     * ⚠️ Belge listesi ANCAK secici acilinca cekiliyor: her detay
     * acilisinda calisma alaninin tum belgelerini indirmek icin sebep
     * yok.
     */
    fun belgeSeciciyiAc() {
        val mevcut = _uiState.value as? RecordDetailUiState.Content ?: return
        _uiState.value = mevcut.copy(belgeSeciciAcik = true, belgeYukleniyor = true)
        viewModelScope.launch {
            repository.getDocuments(workspaceId)
                .onSuccess { yanit ->
                    val simdiki = _uiState.value as? RecordDetailUiState.Content ?: return@onSuccess
                    _uiState.value = simdiki.copy(
                        secilebilirBelgeler = yanit.documents,
                        belgeYukleniyor = false
                    )
                }
                .onFailure {
                    val simdiki = _uiState.value as? RecordDetailUiState.Content ?: return@onFailure
                    _uiState.value = simdiki.copy(belgeSeciciAcik = false, belgeYukleniyor = false)
                    AppMessages.hata("Belgeler yüklenemedi.")
                }
        }
    }

    fun belgeSeciciyiKapat() {
        val mevcut = _uiState.value as? RecordDetailUiState.Content ?: return
        _uiState.value = mevcut.copy(belgeSeciciAcik = false)
    }

    fun belgeBagla(documentId: String) {
        val mevcut = _uiState.value as? RecordDetailUiState.Content ?: return
        if (mevcut.isActing) return
        _uiState.value = mevcut.copy(isActing = true)
        viewModelScope.launch {
            repository.attachDocumentToRecord(workspaceId, recordId, documentId)
                .onSuccess {
                    AppMessages.bilgi("Belge kayda bağlandı.")
                    /* Bag kurulduktan sonra kayit TAZE cekiliyor: detay
                       ucu bagli belgeyi ve icinden okunan alanlari
                       birlikte donduruyor. */
                    load()
                }
                .onFailure {
                    val simdiki = _uiState.value as? RecordDetailUiState.Content ?: return@onFailure
                    _uiState.value = simdiki.copy(isActing = false)
                    AppMessages.hata("Belge bağlanamadı.")
                }
        }
    }

    /**
     * ⚠️ Yalniz BAGI koparir, BELGEYI SILMEZ. Kullaniciya gosterilen
     * metin de bunu soyluyor: belgesini kaybetmekten korkmamali.
     */
    fun belgeBaginiKopar(documentId: String) {
        val mevcut = _uiState.value as? RecordDetailUiState.Content ?: return
        if (mevcut.isActing) return
        _uiState.value = mevcut.copy(isActing = true)
        viewModelScope.launch {
            repository.detachDocumentFromRecord(workspaceId, recordId, documentId)
                .onSuccess {
                    AppMessages.bilgi("Belge bağı koparıldı; belge duruyor.")
                    load()
                }
                .onFailure {
                    val simdiki = _uiState.value as? RecordDetailUiState.Content ?: return@onFailure
                    _uiState.value = simdiki.copy(isActing = false)
                    AppMessages.hata("Belge bağı koparılamadı.")
                }
        }
    }

    fun setStatus(status: String, onError: (String) -> Unit) {
        val current = _uiState.value
        if (current !is RecordDetailUiState.Content || current.isActing) return
        _uiState.value = current.copy(isActing = true)
        viewModelScope.launch {
            val result = repository.updateRecord(
                workspaceId,
                recordId,
                RecordUpdateDto(status = status)
            )
            if (result.isSuccess) {
                _uiState.value = RecordDetailUiState.Content(result.getOrThrow())
            } else {
                _uiState.value = current.copy(isActing = false)
                onError(result.exceptionOrNull()?.message ?: "Güncelleme yapılamadı.")
            }
        }
    }

    fun defer(dueAt: String, reason: String, onError: (String) -> Unit) {
        val current = _uiState.value
        if (current !is RecordDetailUiState.Content || current.isActing) return
        _uiState.value = current.copy(isActing = true)
        viewModelScope.launch {
            val result = repository.deferRecord(workspaceId, recordId, dueAt, reason)
            if (result.isSuccess) {
                _uiState.value = RecordDetailUiState.Content(result.getOrThrow())
            } else {
                _uiState.value = current.copy(isActing = false)
                onError(result.exceptionOrNull()?.message ?: "Ertelenemedi.")
            }
        }
    }

    fun delete(onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteRecord(workspaceId, recordId)
            onDone(result.isSuccess)
        }
    }

    // ------------------------------------------------------------------
    // KAYIT PDF'I
    //
    // 🔴 Mobilde HIC YOKTU. Webde kayit basina PDF indiriliyor
    // (`records/:id/export.pdf`) ve destekleniyorsa paylasim sayfasina
    // veriliyor (`utils/dosyaPaylas.js`).
    //
    // ⚠️ Telefonda paylasim webdeki "indir"in daha genis hali: kaydet,
    // gonder ya da yazdir -- ucu de o sayfanin icinde.
    // ------------------------------------------------------------------

    private val _pdfHazirlaniyor = MutableStateFlow(false)
    val pdfHazirlaniyor: StateFlow<Boolean> = _pdfHazirlaniyor.asStateFlow()

    fun pdfPaylas(paylas: (SharedFile) -> Unit) {
        if (_pdfHazirlaniyor.value) return
        _pdfHazirlaniyor.value = true
        viewModelScope.launch {
            repository.exportRecordPdf(workspaceId, recordId)
                .onSuccess { veri ->
                    _pdfHazirlaniyor.value = false
                    if (veri.isEmpty()) {
                        AppMessages.hata("Belge oluşturulamadı.")
                        return@onSuccess
                    }
                    /*
                     * Dosya adinda kayit ID'si: kullanici birden fazla
                     * kaydi arka arkaya paylasirsa dosyalar birbirini
                     * ezmesin. Baslik kullanilmiyor cunku icinde dosya
                     * adinda gecemeyecek karakterler olabilir.
                     */
                    paylas(
                        SharedFile(
                            name = "kayit-$recordId.pdf",
                            mimeType = "application/pdf",
                            bytes = veri
                        )
                    )
                }
                .onFailure { hata ->
                    _pdfHazirlaniyor.value = false
                    AppMessages.hata(hata.message ?: "Belge oluşturulamadı.")
                }
        }
    }
}
