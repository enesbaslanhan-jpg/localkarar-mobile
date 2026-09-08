package com.localkarar.app.workspaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.core.AppMessages
import com.localkarar.app.core.SharedFile
import com.localkarar.app.network.dto.BusinessRecordDto
import com.localkarar.app.network.dto.TrackerSummaryDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Kayit listesinin yon suzgeci.
 *
 * 🔴 `BELIRSIZ` MOBILDE HIC YOKTU. Sunucu `direction=neutral` kabul
 * ediyor, ozet ucu bunlari ayrica sayiyor (`counts.awaitingDirection`) ve
 * web Ana Sayfa'da gosteriyor; mobilde tutari olan ama borc mu alacak mi
 * belli olmayan kayitlara ulasmanin HICBIR yolu yoktu.
 *
 * ⚠️ `BELIRSIZ` sunucunun `neutral`'i ile birebir ayni degil: sunucunun
 * ozet sayaci "neutral VE tutari olan" diyor (`business-tracker.ts:329`),
 * ham `direction=neutral` suzgeci ise tutarsiz sevkiyat kayitlarini da
 * getiriyor. Sayacla listenin uyusmasi icin tutar kosulu burada da var.
 */
enum class KayitYonu(val sunucuDegeri: String?, val etiket: String) {
    TUMU(null, "Tümü"),
    TAHSILAT("receivable", "Tahsilat"),
    ODEME("payable", "Ödeme"),
    BELIRSIZ("neutral", "Yönü belirsiz")
}

sealed class RecordsUiState {
    object Loading : RecordsUiState()
    data class Content(
        val records: List<BusinessRecordDto> = emptyList(),
        val summary: TrackerSummaryDto? = null,
        val isRefreshing: Boolean = false
    ) : RecordsUiState()
    data class Error(val message: String) : RecordsUiState()
}

class RecordsViewModel(
    private val workspaceId: String,
    private val repository: WorkspaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecordsUiState>(RecordsUiState.Loading)
    val uiState: StateFlow<RecordsUiState> = _uiState.asStateFlow()

    private var currentStatus: String? = null
    private var currentType: String? = null
    private var currentDirection: KayitYonu = KayitYonu.TUMU

    init {
        load()
    }

    fun setFilter(status: String?, type: String?) {
        currentStatus = status
        currentType = type
        load()
    }

    fun setDirection(yon: KayitYonu) {
        currentDirection = yon
        load()
    }

    fun load() {
        _uiState.value = RecordsUiState.Loading
        viewModelScope.launch {
            val result = repository.getRecords(
                workspaceId = workspaceId,
                status = currentStatus,
                type = currentType,
                direction = currentDirection.sunucuDegeri,
                limit = 100
            )
            if (result.isSuccess) {
                /*
                 * Ozet AYRI bir istek ve basarisiz olmasi listeyi
                 * dusurmuyor: sayaclar olmadan da liste okunur, listesiz
                 * sayac ise ise yaramaz.
                 */
                val ozet = repository.getTrackerSummary(workspaceId).getOrNull()
                val kayitlar = result.getOrThrow().records.let { liste ->
                    if (currentDirection == KayitYonu.BELIRSIZ) {
                        liste.filter { it.amount != null }
                    } else liste
                }
                _uiState.value = RecordsUiState.Content(kayitlar, ozet)
            } else {
                _uiState.value = RecordsUiState.Error(
                    result.exceptionOrNull()?.message ?: "Kayıtlar yüklenemedi."
                )
            }
        }
    }

    fun refresh() {
        load()
    }

    // ------------------------------------------------------------------
    // DISA AKTARMA
    //
    // 🔴 Mobilde HIC YOKTU. Webde ekranin ustunde CSV / Excel / PDF
    // dugmeleri var (`Tracker.jsx`).
    //
    // ⚠️ EKRANDAKI SUZGECLER GONDERILIYOR. Suzgecsiz tam liste indirmek,
    // kullanicinin baktigi seyden BASKA bir dosya uretmek olurdu --
    // "geciken tahsilatlar"i suzup indirdiginde 400 satirlik tam liste
    // gelmesi gibi.
    // ------------------------------------------------------------------

    private val _disaAktariliyor = MutableStateFlow<String?>(null)
    val disaAktariliyor: StateFlow<String?> = _disaAktariliyor.asStateFlow()

    /**
     * @param format csv | xlsx | pdf
     * @param paylas inen dosyayi sistem paylasim sayfasina veren islev.
     */
    fun disaAktar(format: String, paylas: (SharedFile) -> Unit) {
        if (_disaAktariliyor.value != null) return
        _disaAktariliyor.value = format
        viewModelScope.launch {
            repository.exportRecords(
                workspaceId = workspaceId,
                format = format,
                status = currentStatus,
                type = currentType,
                direction = currentDirection.sunucuDegeri
            )
                .onSuccess { veri ->
                    _disaAktariliyor.value = null
                    if (veri.isEmpty()) {
                        /* Bos dosya paylasmak, kullaniciya calistigini
                           soyleyip elini bos birakmak olurdu. */
                        AppMessages.hata("Dışa aktarılacak kayıt bulunamadı.")
                        return@onSuccess
                    }
                    paylas(
                        SharedFile(
                            name = "kayitlar.$format",
                            mimeType = disaAktarmaTuru(format),
                            bytes = veri
                        )
                    )
                }
                .onFailure { hata ->
                    _disaAktariliyor.value = null
                    AppMessages.hata(hata.message ?: "Dışa aktarma tamamlanamadı.")
                }
        }
    }
}

/** Uc bicimin MIME turu — alici uygulama dosyayi buna gore aciyor. */
internal fun disaAktarmaTuru(format: String): String = when (format) {
    "csv" -> "text/csv"
    "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    "pdf" -> "application/pdf"
    /* Bilinmeyen bicim: sistem secsin. Yanlis bir tur yazmak, dosyayi
       acamayan bir uygulamaya yonlendirirdi. */
    else -> "application/octet-stream"
}
