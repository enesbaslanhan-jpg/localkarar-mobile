package com.localkarar.app.workspaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.core.AppMessages
import com.localkarar.app.core.SharedFile
import com.localkarar.app.network.dto.ModelOnerileriDto
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

    // ------------------------------------------------------------------
    // BELGE ONERISI — KABUL / RET
    //
    // 🔴 Sunucu yuklenen belgeyi okuyup "bundan su kaydi acayim mi" diye
    // soruyordu; mobilde bu soruya CEVAP VERILEMIYORDU. Belge yukleniyor,
    // "Analiz edildi" rozeti ciziliyor, oneri hicbir yerde gorunmuyordu.
    // Belge yuklemenin asil faydasi tam burada.
    //
    // ⚠️ Islenen oneri ID'si tutuluyor: iki dugme de o satirda devre disi
    // kaliyor. Olmadan kullanici "Kayıt oluştur"a iki kez dokunabilir ve
    // ikinci istek 409 ile doner -- sunucu korunur ama kullanici sebebini
    // anlamayan bir hata gorurdu.
    // ------------------------------------------------------------------

    private val _islenenOneri = MutableStateFlow<String?>(null)
    val islenenOneri: StateFlow<String?> = _islenenOneri.asStateFlow()

    fun oneriyiKabulEt(suggestionId: String) = oneriIsle(suggestionId, kabul = true)

    fun oneriyiReddet(suggestionId: String) = oneriIsle(suggestionId, kabul = false)

    private fun oneriIsle(suggestionId: String, kabul: Boolean) {
        if (_islenenOneri.value != null) return
        _islenenOneri.value = suggestionId
        viewModelScope.launch {
            val sonuc = if (kabul) {
                repository.acceptDocumentSuggestion(workspaceId, suggestionId)
            } else {
                repository.rejectDocumentSuggestion(workspaceId, suggestionId)
            }
            sonuc
                .onSuccess {
                    AppMessages.bilgi(
                        if (kabul) "Kayıt oluşturuldu." else "Öneri kaldırıldı."
                    )
                    _islenenOneri.value = null
                    /* Liste yenileniyor: kabul edilen oneri sunucuda
                       `accepted` oluyor ve bir daha cizilmemeli. */
                    load()
                }
                .onFailure { hata ->
                    AppMessages.hata(hata.message ?: "İşlem tamamlanamadı.")
                    _islenenOneri.value = null
                }
        }
    }

    // ------------------------------------------------------------------
    // BELGEDEN FINANSAL MODEL ONERISI
    //
    // 🔴 Mobilde HIC YOKTU. "Bu belgeyle hangi hesabi yapabilirim"
    // sorusunun cevabi webde vardi, mobilde yoktu.
    //
    // ⚠️ ISTEK KENDILIGINDEN ATILMIYOR: kullanici o belge icin
    // "Hesaplama öner" derse atiliyor. Her belge icin otomatik sormak,
    // liste her acildiginda N istek demekti ve cogunu kimse okumayacakti.
    // ------------------------------------------------------------------

    private val _modelOnerileri = MutableStateFlow<Map<String, ModelOnerileriDto>>(emptyMap())
    val modelOnerileri: StateFlow<Map<String, ModelOnerileriDto>> = _modelOnerileri.asStateFlow()

    private val _modelAraniyor = MutableStateFlow<String?>(null)
    val modelAraniyor: StateFlow<String?> = _modelAraniyor.asStateFlow()

    fun modelOnerisiAra(documentId: String) {
        if (_modelAraniyor.value != null) return
        _modelAraniyor.value = documentId
        viewModelScope.launch {
            repository.getDocumentModelSuggestions(workspaceId, documentId)
                .onSuccess { yanit ->
                    _modelAraniyor.value = null
                    _modelOnerileri.value = _modelOnerileri.value + (documentId to yanit)
                    /*
                     * Bos sonuc SESSIZ GECILMIYOR: kullanici dugmeye
                     * bastiginda bir sey olmali.
                     *
                     * ⚠️ Sunucunun kendi uyarisi VARSA bildirim
                     * gonderilmiyor -- o metin zaten belgenin altinda
                     * kalici olarak yaziliyor. Ikisini birden gostermek
                     * ayni cumleyi iki kez soylemek olurdu (emulatorde
                     * gorulen davranis, 07.09.2026).
                     */
                    if (yanit.models.isEmpty() && yanit.warning.isNullOrBlank()) {
                        AppMessages.bilgi("Bu belgeden hesaplamaya uygun alan çıkarılamadı.")
                    }
                }
                .onFailure { hata ->
                    _modelAraniyor.value = null
                    AppMessages.hata(hata.message ?: "Hesaplama önerisi alınamadı.")
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
    // ------------------------------------------------------------------
    // BELGENIN KENDISINI GERI ALMA
    //
    // 🔴 Bu yol YOKTU: kullanici faturasinin fotografini yukluyor, geri
    // alamiyordu. Belge detayi yalniz OCR metnini donuyordu; veri iceri
    // girip cikamiyordu. Bu bir guven sorunu ve KVKK'nin veri
    // tasinabilirligi beklentisiyle de celisiyordu.
    //
    // ⚠️ Webde "indir", telefonda PAYLAS: kullanici belgeyi
    // muhasebecisine gondermek ya da Dosyalar'a kaydetmek istiyor;
    // sistem paylasim sayfasi ikisini de veriyor.
    // ------------------------------------------------------------------

    private val _indirilenBelgeId = MutableStateFlow<String?>(null)
    val indirilenBelgeId: StateFlow<String?> = _indirilenBelgeId.asStateFlow()

    fun belgePaylas(belge: WorkspaceDocumentDto, paylas: (SharedFile) -> Unit) {
        if (_indirilenBelgeId.value != null) return
        _indirilenBelgeId.value = belge.id
        viewModelScope.launch {
            repository.belgeIndir(workspaceId, belge.id)
                .onSuccess { veri ->
                    _indirilenBelgeId.value = null
                    if (veri.isEmpty()) {
                        AppMessages.hata("Belge indirilemedi.")
                        return@onSuccess
                    }
                    /* Kullanicinin verdigi ad korunuyor; sunucunun
                       urettigi uuid adi kullaniciya "9c2a....pdf" olarak
                       dusurdu. */
                    paylas(
                        SharedFile(
                            name = belge.originalName,
                            mimeType = belge.mimeType ?: "application/octet-stream",
                            bytes = veri
                        )
                    )
                }
                .onFailure { hata ->
                    _indirilenBelgeId.value = null
                    AppMessages.hata(hata.message ?: "Belge indirilemedi.")
                }
        }
    }
}
