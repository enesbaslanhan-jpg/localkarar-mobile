package com.localkarar.app.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.core.AppMessages
import com.localkarar.app.network.dto.KurulumProfiliDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ISLETME KURULUMU (onboarding).
 *
 * 🔴 MOBILDE HIC YOKTU. Web `/app/onboarding` sunuyor; mobil
 * `UserDto.onboardingCompleted` alanini okuyor ama DOLDURACAK EKRANI
 * yoktu. Mobilden kaydolan kullanici isletme profilini hic kuramiyor,
 * mentor ve hesaplamalar da o profile dayandigi icin yarim bir
 * deneyime dusuyordu.
 *
 * ⚠️ WEBDEKI DORT ADIM UC ADIMA INDI. Web: isletme / kanallar /
 * hedefler / OZET. Ozet adimi telefonda yalniz bir kaydirma daha
 * demek -- girilen degerler zaten formda gorunuyor. Alanlarin hicbiri
 * atilmadi; yalnizca dorduncu "geri bak" adimi kaldirildi.
 */
sealed class KurulumUiState {
    object Yukleniyor : KurulumUiState()
    data class Icerik(
        val profil: KurulumProfiliDto,
        val kaydediliyor: Boolean = false
    ) : KurulumUiState()
    data class Hata(val mesaj: String) : KurulumUiState()
}

class OnboardingViewModel(
    private val repository: OnboardingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<KurulumUiState>(KurulumUiState.Yukleniyor)
    val uiState: StateFlow<KurulumUiState> = _uiState.asStateFlow()

    init {
        yukle()
    }

    fun yukle() {
        _uiState.value = KurulumUiState.Yukleniyor
        viewModelScope.launch {
            repository.profil()
                .onSuccess { _uiState.value = KurulumUiState.Icerik(it) }
                /*
                 * Profil YOKSA da ekran aciliyor: ilk kurulumda zaten
                 * bos olacak. Hata ekrani gostermek, kullaniciyi hic
                 * baslamadan durdurmak olurdu.
                 */
                .onFailure { _uiState.value = KurulumUiState.Icerik(KurulumProfiliDto()) }
        }
    }

    fun guncelle(donustur: (KurulumProfiliDto) -> KurulumProfiliDto) {
        val mevcut = _uiState.value as? KurulumUiState.Icerik ?: return
        _uiState.value = mevcut.copy(profil = donustur(mevcut.profil))
    }

    /**
     * Profili kaydeder ve kurulumu tamamlar.
     *
     * ⚠️ IKI ISTEK, SIRAYLA. Profil yazilmadan "tamamlandi" demek,
     * kullaniciyi bir daha hic sorulmayacak bos bir profille birakirdi.
     * Profil yazimi duserse tamamlama HIC gonderilmiyor.
     */
    fun kaydetVeTamamla(onBitti: () -> Unit) {
        val mevcut = _uiState.value as? KurulumUiState.Icerik ?: return
        if (mevcut.kaydediliyor) return
        _uiState.value = mevcut.copy(kaydediliyor = true)

        viewModelScope.launch {
            repository.profilKaydet(mevcut.profil)
                /*
                 * ⚠️ SUNUCUNUN METNI GOSTERILMIYOR, KENDI METNIMIZ.
                 *
                 * Olculdu (08.09.2026, emulator): kurulum kaydedilemeyince
                 * ekranda "Validation failed" yaziyordu -- Turkce bir
                 * uygulamada ham, Ingilizce bir sunucu dizesi. Bu ucta
                 * sunucunun soyledigi hicbir sey kullanicinin
                 * yapabilecegi bir seye karsilik gelmiyor (alan bazli
                 * ayrinti zaten arayuze bagli degil), o yuzden metin
                 * burada yaziliyor.
                 */
                .onFailure {
                    _uiState.value = mevcut.copy(kaydediliyor = false)
                    AppMessages.hata("İşletme bilgilerin kaydedilemedi. Lütfen tekrar dene.")
                }
                .onSuccess {
                    repository.kurulumuTamamla()
                        .onSuccess {
                            /*
                             * TUR BAYRAGI (madde 4). Mobilde rehberli tur
                             * YOK; ama kurulumu telefonda bitiren kullanici
                             * webde bir daha turla karsilasmasin diye
                             * bayrak yaziliyor.
                             *
                             * ⚠️ Sonucu BEKLENMIYOR sayilmiyor ama hatasi
                             * da kullaniciya soylenmiyor: kurulum bitti,
                             * bir kolayligin yazilamamasi akisi durdurmaz.
                             */
                            repository.turuTamamla()
                            _uiState.value = mevcut.copy(kaydediliyor = false)
                            AppMessages.bilgi("İşletme bilgilerin kaydedildi.")
                            onBitti()
                        }
                        .onFailure {
                            /*
                             * Profil YAZILDI ama tamamlama duştu. Kullaniciya
                             * "kaydedilemedi" demek yanlis olurdu -- verisi
                             * duruyor. Yalnizca kurulumun kapanmadigi
                             * soyleniyor; sunucunun en olasi gerekcesi de
                             * (ad ya da sektor bos) kullanicinin
                             * duzeltebilecegi bir sey oldugu icin
                             * yaziliyor.
                             */
                            _uiState.value = mevcut.copy(kaydediliyor = false)
                            AppMessages.hata(
                                "Bilgilerin kaydedildi ama kurulum kapatılamadı. " +
                                    "İşletme adı ya da sektörden en az birini doldurman gerekiyor."
                            )
                        }
                }
        }
    }
}
