package com.localkarar.app.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.calculations.CalculationsRepository
import com.localkarar.app.network.ApiConfig
import com.localkarar.app.network.SafeApiClient
import com.localkarar.app.network.dto.FormulaDto
import com.localkarar.app.network.dto.GenelAramaDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * GENEL ARAMA — mobilde ilk kez.
 *
 * 🔴 Webin ust cubugunda kisi / paylasim / kurs / karar araci /
 * hesaplama / haber gruplarini tek kutuda arayan bir alan var
 * (`Header.jsx`). Mobilde her ekranin KENDI aramasi vardi ama tek bir
 * genel arama YOKTU: kullanici bir kursu ararken once Kurslar'a,
 * bir kisiyi ararken once Toplulugu acmak zorundaydi.
 *
 * ⚠️ EN AZ IKI HARF ve 250 ms bekleme -- webin kurallarinin ayni.
 * Her tusa basista istek atmak hem sunucuyu hem hiz sinirini
 * (dakikada 60) gereksiz yorardi.
 */
class GlobalSearchViewModel(
    private val api: SafeApiClient,
    private val calculationsRepository: CalculationsRepository
) : ViewModel() {

    data class Sonuclar(
        val sunucu: GenelAramaDto = GenelAramaDto(),
        val hesaplamalar: List<FormulaDto> = emptyList()
    ) {
        val bosMu: Boolean
            get() = sunucu.people.isEmpty() && sunucu.posts.isEmpty() &&
                sunucu.courses.isEmpty() && sunucu.decisionChecks.isEmpty() &&
                hesaplamalar.isEmpty() && sunucu.news.isEmpty()
    }

    sealed class Durum {
        /** Henuz iki harf yazilmadi. */
        data object Bekliyor : Durum()
        data object Araniyor : Durum()
        data class Icerik(val sonuclar: Sonuclar) : Durum()
        data class Hata(val mesaj: String) : Durum()
    }

    private val _durum = MutableStateFlow<Durum>(Durum.Bekliyor)
    val durum: StateFlow<Durum> = _durum.asStateFlow()

    var terim: String = ""
        private set

    private var aramaIsi: Job? = null

    /*
     * Hesaplama katalogu BIR KEZ cekiliyor ve bellekte suzuluyor.
     *
     * ⚠️ Web bu grubu YEREL bir katalog dosyasindan suzuyor
     * (`data/calculationCatalog`). Mobilde ayni listeyi ikinci kez
     * elle yazmak, iki kopyanin sessizce ayrilmasi demekti; onun
     * yerine sunucunun kendi formul listesi suzuluyor. Kullanici
     * acisindan sonuc ayni: terimle eslesen hesaplama araclari.
     */
    private var formuller: List<FormulaDto>? = null

    fun terimDegisti(yeni: String) {
        terim = yeni
        aramaIsi?.cancel()

        if (yeni.trim().length < 2) {
            _durum.value = Durum.Bekliyor
            return
        }

        aramaIsi = viewModelScope.launch {
            delay(250)
            _durum.value = Durum.Araniyor
            ara(yeni.trim())
        }
    }

    fun tekrarDene() {
        terimDegisti(terim)
    }

    private suspend fun ara(sorgu: String) {
        if (formuller == null) {
            calculationsRepository.getFormulas().onSuccess { formuller = it }
        }

        val kodlanmis = sorgu.encodeURLQueryComponent()
        api.get<GenelAramaDto>("${ApiConfig.baseUrl}/api/v2/search?q=$kodlanmis")
            .onSuccess { sonuc ->
                _durum.value = Durum.Icerik(
                    Sonuclar(
                        sunucu = sonuc,
                        hesaplamalar = hesaplamaEslesmeleri(sorgu)
                    )
                )
            }
            .onFailure {
                /* ⚠️ Sunucunun metni degil kendi metnimiz: bu ucta
                   sunucunun soyleyecegi hicbir sey kullanicinin
                   yapabilecegi bir seye karsilik gelmiyor. */
                _durum.value = Durum.Hata("Arama şu anda yapılamıyor. Lütfen tekrar dene.")
            }
    }

    /** Webdeki gibi en fazla bes hesaplama. */
    private fun hesaplamaEslesmeleri(sorgu: String): List<FormulaDto> {
        val kucuk = sorgu.trKucuk()
        return formuller.orEmpty()
            .filter { it.name.trKucuk().contains(kucuk) }
            .take(5)
    }
}

/*
 * ⚠️ Turkce kucuk harf: `lowercase()` yerel-bagimsiz ve "İ" harfini
 * "i̇" (noktali i + birlesik nokta) yapiyor; "İhracat" araniyorken
 * "ihracat" ile eslesmiyordu.
 */
private fun String.trKucuk(): String = replace('I', 'ı').replace('İ', 'i').lowercase()

/*
 * Sorgu dizesine giren terim KACISLI yaziliyor. Bosluk, `&` ya da `#`
 * iceren bir arama aksi halde adresi bozardi.
 */
private fun String.encodeURLQueryComponent(): String = buildString {
    for (bayt in this@encodeURLQueryComponent.encodeToByteArray()) {
        val deger = bayt.toInt() and 0xFF
        val harf = deger.toChar()
        if (harf.isLetterOrDigit() && deger < 128 || harf in "-_.~") {
            append(harf)
        } else {
            append('%')
            append(deger.toString(16).uppercase().padStart(2, '0'))
        }
    }
}
