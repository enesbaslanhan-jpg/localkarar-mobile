package com.localkarar.app.calculations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.FinancialModelDto
import com.localkarar.app.network.dto.FinancialModelRunResponseDto
import com.localkarar.app.network.dto.HesaplamaIpucuDto
import com.localkarar.app.network.dto.KararGunluguIstegiDto
import com.localkarar.app.network.dto.ModelAssumptionDto
import com.localkarar.app.network.dto.ModelRunRequestDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull

sealed class FinancialModelUiState {
    object Loading : FinancialModelUiState()
    data class Content(
        val model: FinancialModelDto,
        val runResult: FinancialModelRunResponseDto? = null,
        val isRunning: Boolean = false
    ) : FinancialModelUiState()
    data class Error(val message: String) : FinancialModelUiState()
}

class FinancialModelViewModel(
    private val code: String,
    private val workspaceId: String?,
    private val repository: CalculationsRepository,
    /**
     * Model bir BELGEDEN acildiysa o belgenin kimligi (madde 18).
     * Girdiler belgeden on doldurulup kaynak "belge" isaretleniyor.
     */
    private val sourceDocumentId: String? = null,
    private val workspaceRepository: com.localkarar.app.workspaces.WorkspaceRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<FinancialModelUiState>(FinancialModelUiState.Loading)
    val uiState: StateFlow<FinancialModelUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = FinancialModelUiState.Loading
        viewModelScope.launch {
            val result = repository.getModel(code)
            if (result.isSuccess) {
                _uiState.value = FinancialModelUiState.Content(result.getOrThrow())
            } else {
                _uiState.value = FinancialModelUiState.Error("Model yüklenemedi.")
            }
        }
    }

    // ------------------------------------------------------------------
    // GIRDI KAYNAK KUNYESI
    //
    // 🔴 Mobil `assumptions` alanini HEP BOS gonderiyordu. Sunucu her
    // girdinin nereden geldigini saklıyor (`ModelAssumption`) ve web bunu
    // girdi basina soruyor: kaynak turu, referans, gecerlilik tarihi,
    // "doğruladım" kutusu.
    //
    // ⚠️ MOBILDE GIRDI BASINA DORT ALANLIK BIR KUNYE FORMU YOK, bilerek.
    // Telefonda her girdi icin dort ek alan doldurtmak yapilabilir bir is
    // degil. Kunye YALNIZ degeri kullanici KOYMADIYSA soruluyor: belgeden
    // ya da pazaryerinden geldiyse nereden geldigi yaziliyor ve
    // "doğruladım" isaretlemesi isteniyor. Kendi yazdigi sayi icin
    // kullaniciya "bu nereden geldi" diye sormak anlamsizdir.
    // ------------------------------------------------------------------

    /** Girdi anahtari -> o degerin kaynagi. Kullanicinin yazdiklari listede YOK. */
    private val _kaynaklar = MutableStateFlow<Map<String, GirdiKaynagi>>(emptyMap())
    val kaynaklar: StateFlow<Map<String, GirdiKaynagi>> = _kaynaklar.asStateFlow()

    private val _ipucu = MutableStateFlow<HesaplamaIpucuDto?>(null)
    val ipucu: StateFlow<HesaplamaIpucuDto?> = _ipucu.asStateFlow()

    /**
     * BELGEDEN ON DOLDURMA (madde 18).
     *
     * 🔴 Mobilde HIC YOKTU. Belgeler ekranindaki "hesaplama öner"
     * modeli aciyordu ama belgenin OKUNAN DEGERLERINI tasimiyordu;
     * kullanici faturadaki rakamlari elle yeniden yaziyordu.
     *
     * ⚠️ Degerler DOGRUDAN GECERLI SAYILMIYOR: kaynak "belge" olarak
     * isaretleniyor ve `dogrulandi = false` basliyor. Sunucu da ayni
     * kurali tutuyor -- dogrulanmamis belge verisiyle model
     * calistirmayi reddediyor. OCR/ayristirma yanilabilir; okunan bir
     * rakami kullaniciya sormadan hesaba sokmak yanlis olurdu.
     *
     *  alan -> deger; ekran bunlari girdi kutularina yaziyor.
     */
    suspend fun belgedenOnDoldur(): Map<String, Double> {
        val ws = workspaceId ?: return emptyMap()
        val belge = sourceDocumentId ?: return emptyMap()
        val depo = workspaceRepository ?: return emptyMap()

        val yanit = depo.getDocumentModelSuggestions(ws, belge).getOrNull() ?: return emptyMap()
        val model = yanit.models.firstOrNull { it.code == code } ?: return emptyMap()
        val degerler = model.mappedInputs
            .mapNotNull { (anahtar, deger) ->
                (deger as? kotlinx.serialization.json.JsonPrimitive)
                    ?.contentOrNull?.toDoubleOrNull()?.let { anahtar to it }
            }
            .toMap()
        if (degerler.isNotEmpty()) {
            belgeKaynagiIsaretle(degerler.keys, yanit.documentName ?: "belge")
        }
        return degerler
    }

    fun ipucuYukle() {
        val ws = workspaceId ?: return
        viewModelScope.launch {
            repository.getCalculationHints(ws)
                .onSuccess { yanit -> _ipucu.value = if (yanit.available) yanit else null }
                /* Sessiz: ipucu bir kolaylik, gelmezse ekran yine calisir. */
                .onFailure { _ipucu.value = null }
        }
    }

    /**
     * Pazaryeri ipucundan doldurulacak degerler.
     *
     * ⚠️ YALNIZ `PRODUCT_PROFITABILITY` icin — web de oyle. Ortalama
     * satis fiyatini baska bir modelin baska bir alanina yazmak, farkli
     * bir buyuklugu ayni sayiyla doldurmak olurdu.
     *
     * ⚠️ Kanal kesintisi ancak komisyon ORANI biliniyorsa hesaplaniyor;
     * bilinmiyorsa o alan ELLE birakiliyor, sifir yazilmiyor.
     */
    fun ipucuDegerleri(): Map<String, Double> {
        val y = _ipucu.value ?: return emptyMap()
        if (code != "PRODUCT_PROFITABILITY") return emptyMap()
        val fiyat = y.avgUnitPrice ?: return emptyMap()
        val degerler = mutableMapOf("netPrice" to fiyat)
        y.avgCommissionPercent?.let { oran ->
            degerler["channelCost"] = kotlin.math.round(fiyat * oran) / 100.0
        }
        return degerler
    }

    /** Ipucundan doldurulan alanlari kaynagiyla isaretler. */
    fun ipucuKaynagiIsaretle(alanlar: Set<String>) {
        val y = _ipucu.value ?: return
        val referans = "Pazaryeri: ${y.source ?: "bağlı mağaza"} · ${y.sampleSize} sipariş kalemi"
        _kaynaklar.value = _kaynaklar.value + alanlar.associateWith {
            /*
             * ⚠️ `market_data` — 'marketplace' DEGIL.
             *
             * Sunucunun enum'u alti deger kabul ediyor ve 'marketplace'
             * onlarin arasinda YOK. Web tam bu hatayi yapiyordu ve
             * "Bu değerlerle doldur" sonrasi model calistirilamiyordu
             * (olculdu 08.09.2026, 422); web tarafi da duzeltildi.
             */
            GirdiKaynagi(tur = "market_data", referans = referans)
        }
    }

    /** Belgeden on doldurulan alanlari isaretler — kullanici DOGRULAMALI. */
    fun belgeKaynagiIsaretle(alanlar: Set<String>, belgeAdi: String) {
        _kaynaklar.value = _kaynaklar.value + alanlar.associateWith {
            GirdiKaynagi(tur = "document", referans = "Belge: $belgeAdi", dogrulandi = false)
        }
    }

    fun dogrulamayiDegistir(alan: String, dogrulandi: Boolean) {
        val mevcut = _kaynaklar.value[alan] ?: return
        _kaynaklar.value = _kaynaklar.value + (alan to mevcut.copy(dogrulandi = dogrulandi))
    }

    // ------------------------------------------------------------------
    // KARAR GUNLUGU
    //
    // 🔴 Mobilde HIC YOKTU. Model calisiyor, sonuc goruluyor, ama "bu
    // sonuca dayanarak ne karar verdim" hicbir yere yazilamiyordu.
    //
    // ⚠️ YALNIZ CALISTIRILMIS bir modelden kaydedilebiliyor: sunucu
    // `modelRunId` istiyor ve dogrusu bu -- dayanaksiz bir karar kaydi
    // izlenebilir olmazdi.
    // ------------------------------------------------------------------

    private val _kararKaydediliyor = MutableStateFlow(false)
    val kararKaydediliyor: StateFlow<Boolean> = _kararKaydediliyor.asStateFlow()

    fun kararKaydet(karar: String, beklenen: String, onBitti: () -> Unit) {
        val ws = workspaceId ?: return
        val calisma = (_uiState.value as? FinancialModelUiState.Content)?.runResult?.id ?: return
        if (_kararKaydediliyor.value) return
        _kararKaydediliyor.value = true
        viewModelScope.launch {
            repository.kararKaydet(
                ws,
                KararGunluguIstegiDto(calisma, karar.trim(), beklenen.trim())
            )
                .onSuccess {
                    _kararKaydediliyor.value = false
                    com.localkarar.app.core.AppMessages.bilgi("Karar kaydedildi.")
                    onBitti()
                }
                .onFailure { hata ->
                    _kararKaydediliyor.value = false
                    com.localkarar.app.core.AppMessages.hata(hata.message ?: "Karar kaydedilemedi.")
                }
        }
    }

    fun run(inputs: Map<String, JsonElement>, scenarioName: String, onError: (String) -> Unit) {
        val current = _uiState.value
        if (current !is FinancialModelUiState.Content || current.isRunning) return
        val workspace = workspaceId
        if (workspace == null) {
            onError("Finansal model çalıştırmak için önce bir işletme seçin.")
            return
        }
        _uiState.value = current.copy(isRunning = true)
        viewModelScope.launch {
            /*
             * Her girdi icin bir kunye gonderiliyor. Kaynak
             * isaretlenmemisse `user`: degeri kullanici yazmis demektir.
             */
            val kunyeler = inputs.map { (anahtar, deger) ->
                val kaynak = _kaynaklar.value[anahtar]
                ModelAssumptionDto(
                    key = anahtar,
                    value = deger,
                    sourceType = kaynak?.tur ?: "user",
                    sourceReference = kaynak?.referans,
                    userVerified = kaynak?.dogrulandi
                )
            }
            val request = ModelRunRequestDto(
                inputs = inputs,
                assumptions = kunyeler,
                scenarioName = scenarioName,
                sourceDocumentId = sourceDocumentId
            )
            val result = repository.runModel(workspace, code, request)
            if (result.isSuccess) {
                _uiState.value = current.copy(runResult = result.getOrThrow(), isRunning = false)
            } else {
                _uiState.value = current.copy(isRunning = false)
                /*
                 * 🔴 SUNUCUNUN MESAJI YUTULUYORDU. Burada sabit
                 * "Model çalıştırılamadı." yaziliyordu; oysa sunucu hangi
                 * alanin eksik oldugunu SOYLUYOR ("Ürün Maliyeti
                 * zorunludur. Operasyon Maliyeti zorunludur."). Kullanici
                 * neyi duzeltecegini bilmeden ayni dugmeye basip
                 * duruyordu.
                 */
                onError(result.exceptionOrNull()?.message ?: "Model çalıştırılamadı.")
            }
        }
    }
}

/**
 * Bir girdinin nereden geldigi.
 *
 * `tur` sunucunun enum'undan: document | business_record | user | case |
 * approved_dataset | market_data.
 */
data class GirdiKaynagi(
    val tur: String,
    val referans: String,
    /*
     * Belgeden gelen degerler icin ZORUNLU: sunucu, dogrulanmamis belge
     * verisiyle model calistirmayi reddediyor ("OCR belge verileri
     * modelde kullanilmadan once kullanici tarafindan dogrulanmalidir").
     */
    val dogrulandi: Boolean? = null
)