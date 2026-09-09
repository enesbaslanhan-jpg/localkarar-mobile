package com.localkarar.app.decision

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.calculations.CalculationsRepository
import com.localkarar.app.network.dto.TrackerAnaliziDto
import com.localkarar.app.workspaces.WorkspaceRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class KararRaporuUiState {
    object Loading : KararRaporuUiState()
    data class Content(
        val satirlar: List<KararSatiri>,
        val ozet: KararRaporuOzeti,
        /**
         * Yalniz sahip/yonetici icin dolu; diger rollerde `null` ve
         * panel HIC cizilmiyor. Erisemeyecegi bir seyi hatirlatan
         * uyari kullaniciya bir sey kazandirmaz.
         */
        val analiz: TrackerAnaliziDto? = null
    ) : KararRaporuUiState()
    data class Error(val message: String) : KararRaporuUiState()
}

class KararRaporuViewModel(
    private val workspaceId: String,
    private val workspaceRepository: WorkspaceRepository,
    private val calculationsRepository: CalculationsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<KararRaporuUiState>(KararRaporuUiState.Loading)
    val uiState: StateFlow<KararRaporuUiState> = _uiState.asStateFlow()

    private val _suzgec = MutableStateFlow(KararSuzgeci.HEPSI)
    val suzgec: StateFlow<KararSuzgeci> = _suzgec.asStateFlow()

    init {
        load()
    }

    fun suzgecSec(yeni: KararSuzgeci) {
        _suzgec.value = yeni
    }

    fun load() {
        _uiState.value = KararRaporuUiState.Loading
        viewModelScope.launch {
            /* Uc istek PARALEL: sirayla beklenselerdi en yavas uc
               otekileri de geciktirirdi. */
            val takipIstegi = async { workspaceRepository.getRecords(workspaceId, kararKaynakli = true) }
            val gunlukIstegi = async { calculationsRepository.kararGunlugu(workspaceId) }
            val analizIstegi = async { workspaceRepository.getTrackerAnalysis(workspaceId) }

            val takipSonucu = takipIstegi.await()
            if (takipSonucu.isFailure) {
                /*
                 * ⚠️ YALNIZ takip ucunun hatasi ekrani dusuruyor.
                 *
                 * Karar gunlugu ayri bir serviste (finansal modeller) ve
                 * o modul hic kullanilmamis olabilir; analiz ise cogu
                 * rolde bilerek 403 donuyor. Ikisinin hatasi raporu
                 * bosaltmamali.
                 */
                _uiState.value = KararRaporuUiState.Error(
                    takipSonucu.exceptionOrNull()?.message ?: "Karar raporu yüklenemedi."
                )
                return@launch
            }

            val satirlar = kararSatirlari(
                takipler = takipSonucu.getOrThrow().records,
                gunluk = gunlukIstegi.await().getOrNull()?.entries.orEmpty()
            )
            _uiState.value = KararRaporuUiState.Content(
                satirlar = satirlar,
                ozet = kararOzeti(satirlar),
                analiz = analizIstegi.await().getOrNull()
            )
        }
    }
}
