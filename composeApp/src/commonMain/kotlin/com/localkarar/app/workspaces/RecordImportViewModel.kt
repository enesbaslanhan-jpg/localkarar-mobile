package com.localkarar.app.workspaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.core.AppMessages
import com.localkarar.app.network.dto.RecordImportResultDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * TOPLU KAYIT ICE AKTARMA.
 *
 * 🔴 Mobilde HIC YOKTU. Webde uc adimli bir sihirbaz var
 * (`ImportDialog.jsx`): dosya sec -> sutunlari elle eslestir -> onizle
 * -> yaz.
 *
 * ⚠️ MOBILDE SUTUN ESLESTIRME IZGARASI YOK, BILEREK. Telefonda 11
 * hedef alani 11 sutunla elle eslestirmek yapilabilir bir is degil.
 * Eslestirme webdeki takma adlarla OTOMATIK yapiliyor; eslesen alanlar
 * kullaniciya YAZILIYOR, boylece dosyasinin dogru okunup okunmadigini
 * gorup vazgecebiliyor.
 *
 * ⚠️ YALNIZ CSV. Web de aslinda yalnizca CSV'de calisiyor: xlsx
 * dalinda sutun listesi bos birakilmis (`rows = []`), yani eslestirme
 * hic kurulamiyor. Desteklenmeyen bir bicimi "destekliyormus" gibi
 * gostermek yerine mobil bunu acikca soyluyor.
 */
sealed class ImportUiState {
    object DosyaBekleniyor : ImportUiState()
    object Yukleniyor : ImportUiState()
    data class Onizleme(
        val dosyaAdi: String,
        val fileId: String,
        val eslesme: Map<String, String>,
        val sonuc: RecordImportResultDto
    ) : ImportUiState()
    object Yaziliyor : ImportUiState()
    data class Bitti(val eklenen: Int) : ImportUiState()
    data class Hata(val mesaj: String) : ImportUiState()
}

class RecordImportViewModel(
    private val workspaceId: String,
    private val repository: WorkspaceRepository,
    private val uploadRepository: DocumentUploadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ImportUiState>(ImportUiState.DosyaBekleniyor)
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    fun dosyaSecildi(dosyaAdi: String, icerik: ByteArray) {
        if (!dosyaAdi.lowercase().endsWith(".csv")) {
            _uiState.value = ImportUiState.Hata(
                "Şimdilik yalnızca CSV dosyaları içe aktarılabilir."
            )
            return
        }
        _uiState.value = ImportUiState.Yukleniyor
        viewModelScope.launch {
            /*
             * Dosya once YUKLENIYOR: ice aktarma ucu dosyanin kendisini
             * degil, yuklenmis bir belgenin kimligini bekliyor
             * (`fileId`). Web de once yukluyor.
             */
            val belgeId = uploadRepository.yukleVeBagla(workspaceId, dosyaAdi, icerik)
                .getOrElse { hata ->
                    _uiState.value = ImportUiState.Hata(hata.message ?: "Dosya yüklenemedi.")
                    return@launch
                }

            /*
             * Sutun basliklari DOSYANIN KENDISINDEN okunuyor, sunucudan
             * geri istenmiyor: bayt dizisi zaten elimizde ve ikinci bir
             * istek atmak gereksiz bir gidis-donus olurdu.
             */
            val sutunlar = csvBasliklariniOku(icerik.decodeToString())
            val eslesme = iceAktarmaEslestir(sutunlar)

            if (eslesme.isEmpty()) {
                _uiState.value = ImportUiState.Hata(
                    "Dosyadaki sütun başlıkları tanınmadı. Başlık satırında " +
                        "\"Başlık\", \"Tutar\", \"Vade\" gibi adlar bulunmalı."
                )
                return@launch
            }

            repository.importRecords(workspaceId, belgeId, eslesme, previewOnly = true)
                .onSuccess { sonuc ->
                    _uiState.value = ImportUiState.Onizleme(dosyaAdi, belgeId, eslesme, sonuc)
                }
                .onFailure { hata ->
                    _uiState.value = ImportUiState.Hata(hata.message ?: "Dosya okunamadı.")
                }
        }
    }

    fun onayla() {
        val onizleme = _uiState.value as? ImportUiState.Onizleme ?: return
        _uiState.value = ImportUiState.Yaziliyor
        viewModelScope.launch {
            repository.importRecords(
                workspaceId, onizleme.fileId, onizleme.eslesme, previewOnly = false
            )
                .onSuccess { sonuc ->
                    /*
                     * Sunucunun bildirdigi sayi yaziliyor, onizlemedeki
                     * TAHMIN degil: ikisi arasinda fark cikarsa
                     * kullanicinin gormesi gereken sey gercekten yazilan
                     * satir sayisi.
                     */
                    val eklenen = if (sonuc.imported > 0) sonuc.imported else sonuc.validRows
                    _uiState.value = ImportUiState.Bitti(eklenen)
                }
                .onFailure { hata ->
                    AppMessages.hata(hata.message ?: "İçe aktarma tamamlanamadı.")
                    _uiState.value = onizleme
                }
        }
    }

    fun bastanBasla() {
        _uiState.value = ImportUiState.DosyaBekleniyor
    }
}
