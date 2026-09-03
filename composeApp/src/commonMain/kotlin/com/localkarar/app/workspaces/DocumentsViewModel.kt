package com.localkarar.app.workspaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.core.AppMessages
import com.localkarar.app.network.dto.WorkspaceDocumentDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DocumentsUiState {
    object Loading : DocumentsUiState()
    data class Content(
        val documents: List<WorkspaceDocumentDto> = emptyList()
    ) : DocumentsUiState()
    data class Error(val message: String) : DocumentsUiState()
}

class DocumentsViewModel(
    private val workspaceId: String,
    private val repository: WorkspaceRepository,
    private val uploadRepository: DocumentUploadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DocumentsUiState>(DocumentsUiState.Loading)
    val uiState: StateFlow<DocumentsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = DocumentsUiState.Loading
        viewModelScope.launch {
            val result = repository.getDocuments(workspaceId)
            if (result.isSuccess) {
                _uiState.value = DocumentsUiState.Content(result.getOrThrow().documents)
            } else {
                _uiState.value = DocumentsUiState.Error(
                    result.exceptionOrNull()?.message ?: "Belgeler yüklenemedi."
                )
            }
        }
    }

    fun delete(documentId: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteDocument(workspaceId, documentId)
            onDone(result.isSuccess)
            if (result.isSuccess) load()
        }
    }

    // ------------------------------------------------------------------
    // BELGE YUKLEME
    //
    // 🔴 Mobilde HIC YOKTU: ekran acikca "Belge yukleme su an icin web
    // suruminde kullanilabilir" diyordu. Oysa telefonla fatura cekip ya da
    // e-posta ekinden secip yuklemek, bu ozelligin EN ANLAMLI oldugu yer.
    //
    // XML de destekleniyor ve bu kritik: UBL-TR e-fatura ayristiricisi o
    // yoldan besleniyor.
    // ------------------------------------------------------------------

    private val _yukleniyor = MutableStateFlow(false)
    val yukleniyor: StateFlow<Boolean> = _yukleniyor.asStateFlow()

    fun belgeYukle(dosyaAdi: String, icerik: ByteArray, kategori: String? = null) {
        if (_yukleniyor.value) return
        _yukleniyor.value = true
        viewModelScope.launch {
            /*
             * TEK CAGRI ICINDE IKI ADIM: yukle + calisma alanina bagla.
             * Ikincisi atlanirsa belge kullanicinin kisisel listesine girer
             * ama BU ekranin listesinde gorunmez -- "yuklendi" deyip ortada
             * hicbir sey olmamasi gibi bir sonuc.
             */
            uploadRepository.yukleVeBagla(workspaceId, dosyaAdi, icerik, kategori)
                .onSuccess {
                    AppMessages.bilgi("Belge yüklendi.")
                    _yukleniyor.value = false
                    load()
                }
                .onFailure { hata ->
                    AppMessages.hata(hata.message ?: "Belge yüklenemedi.")
                    _yukleniyor.value = false
                }
        }
    }
}
