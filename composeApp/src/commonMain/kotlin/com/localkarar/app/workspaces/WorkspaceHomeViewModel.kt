package com.localkarar.app.workspaces

import com.localkarar.app.network.dto.OrderDto
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.TrackerSummaryDto
import com.localkarar.app.decision.KararSatiri
import com.localkarar.app.decision.anaSayfaKararGorevleri
import com.localkarar.app.network.dto.WorkspaceDetailDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class WorkspaceHomeUiState {
    object Loading : WorkspaceHomeUiState()
    data class Content(
        val workspace: WorkspaceDetailDto,
        val summary: TrackerSummaryDto? = null,
        val summaryFailed: Boolean = false
    ) : WorkspaceHomeUiState()
    data class Error(val message: String) : WorkspaceHomeUiState()
}

class WorkspaceHomeViewModel(
    private val workspaceId: String,
    private val repository: WorkspaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WorkspaceHomeUiState>(WorkspaceHomeUiState.Loading)
    val uiState: StateFlow<WorkspaceHomeUiState> = _uiState.asStateFlow()

    /*
     * PAZARYERI SIPARISLERI — alt cekmeceyi besliyor.
     *
     * `uiState`in ICINDE DEGIL, ayri bir akista: siparis istegi ozet ve
     * isletme istegiden BAGIMSIZ basarisiz olabilir. Ayni duruma konsaydi
     * entegrasyon kapaliyken TUM ekran hata durumuna duserdi; oysa
     * isletme ozeti gayet calisiyor.
     *
     * "Bos liste" ile "yuklenemedi" ayri seyler; `ordersLoaded` bunu
     * ayiriyor ki cekmece bos durumda dogru metni gosterebilsin.
     */
    private val _orders = MutableStateFlow<List<OrderDto>>(emptyList())
    val orders: StateFlow<List<OrderDto>> = _orders.asStateFlow()

    private val _ordersLoaded = MutableStateFlow(false)
    val ordersLoaded: StateFlow<Boolean> = _ordersLoaded.asStateFlow()

    /**
     * GENEL BAKISIN BOLUM SAYILARI.
     *
     * Onay alinan foyde her bolum satiri altinda GERCEK bir sayi var
     * ("24 ürün · 3 stok azaldı"). Bu sayilar ozet ucundan gelmiyor;
     * her biri kendi ucundan cekiliyor.
     *
     * ⚠️ Hepsi `null` baslar ve istek basarisiz olursa `null` KALIR —
     * satir o zaman alt yazisiz cizilir. Sifir yazmak, sayiyi
     * bilmedigimiz halde "hic yok" demek olurdu.
     */
    data class BolumSayilari(
        val urun: Int? = null,
        val stokAzalan: Int? = null,
        val belge: Int? = null,
        val okunmamisBildirim: Int? = null
    )

    private val _sayilar = MutableStateFlow(BolumSayilari())
    val sayilar: StateFlow<BolumSayilari> = _sayilar.asStateFlow()

    /*
     * KARARDAN DOGAN GOREVLER.
     *
     * 🔴 Bu bolum YOKTU. Karar araci bir karari goreve bagliyordu ama o
     * ekrandan cikinca gorev siradan bir kayda donusuyordu: hangi
     * karardan dogdugu ve ne beklendigi bir daha hicbir yerde
     * gorunmuyordu.
     *
     * `uiState`in ICINDE DEGIL, ayri akista: bu istek ozetten bagimsiz
     * basarisiz olabilir ve ayni duruma konsaydi tum ekran hataya
     * duserdi. Bos liste ile "yuklenemedi" burada AYNI sonucu veriyor
     * (bolum cizilmiyor) -- bos bir kutu ana sayfada yer kaplamaktan
     * baska bir sey yapmaz.
     */
    private val _kararGorevleri = MutableStateFlow<List<KararSatiri>>(emptyList())
    val kararGorevleri: StateFlow<List<KararSatiri>> = _kararGorevleri.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = WorkspaceHomeUiState.Loading
        viewModelScope.launch {
            val workspaceResult = repository.getWorkspace(workspaceId)
            if (workspaceResult.isFailure) {
                _uiState.value = WorkspaceHomeUiState.Error(
                    workspaceResult.exceptionOrNull()?.message ?: "İşletme yüklenemedi."
                )
                return@launch
            }
            val workspace = workspaceResult.getOrThrow()
            val summaryResult = repository.getTrackerSummary(workspaceId)
            _uiState.value = WorkspaceHomeUiState.Content(
                workspace = workspace,
                summary = summaryResult.getOrNull(),
                summaryFailed = summaryResult.isFailure
            )
        }

        /* Siparisler AYRI coroutine: ozetin gelmesini bekletmesin. */
        viewModelScope.launch {
            repository.getOrders(workspaceId = workspaceId, provider = null)
                .onSuccess {
                    _orders.value = it.orders
                    _ordersLoaded.value = true
                }
                .onFailure {
                    /* Sessiz: entegrasyon yoksa bu BEKLENEN bir durum ve
                       ekranin geri kalanini bozmamali. */
                    _ordersLoaded.value = false
                }
        }

        /*
         * Bolum sayilari — her biri AYRI coroutine.
         *
         * Tek bir `launch` icinde sirayla beklenselerdi, belge ucu yavas
         * oldugunda urun sayisi da gecikirdi; ekran zaten gelen sayiyi
         * geldiginde yaziyor.
         */
        viewModelScope.launch {
            repository.getProducts(workspaceId = workspaceId).onSuccess { liste ->
                _sayilar.value = _sayilar.value.copy(urun = liste.total)
            }
        }
        viewModelScope.launch {
            repository.getProducts(workspaceId = workspaceId, stockFilter = "low")
                .onSuccess { liste ->
                    _sayilar.value = _sayilar.value.copy(stokAzalan = liste.total)
                }
        }
        viewModelScope.launch {
            repository.getDocuments(workspaceId).onSuccess { yanit ->
                _sayilar.value = _sayilar.value.copy(belge = yanit.total)
            }
        }
        viewModelScope.launch {
            repository.getNotifications(workspaceId).onSuccess { yanit ->
                _sayilar.value = _sayilar.value.copy(okunmamisBildirim = yanit.unreadCount)
            }
        }
        viewModelScope.launch {
            /* Suzgec SUNUCUDA: istemcide suzulseydi genel liste 100
               kayitla sinirli oldugu icin 101. siradaki karar gorevi
               sessizce kaybolurdu. */
            repository.getRecords(workspaceId, kararKaynakli = true).onSuccess { yanit ->
                _kararGorevleri.value = anaSayfaKararGorevleri(yanit.records)
            }
        }
    }
}