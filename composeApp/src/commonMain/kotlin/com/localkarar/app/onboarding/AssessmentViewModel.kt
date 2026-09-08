package com.localkarar.app.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.core.AppMessages
import com.localkarar.app.network.dto.DegerlendirmeSonucuDto
import com.localkarar.app.network.dto.DegerlendirmeSorusuDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ISLETME OZ DEGERLENDIRMESI (assessment).
 *
 * 🔴 MOBILDE HIC YOKTU. Web `/app/assessment` sunuyor: alan alan
 * sorular, puanlar ve oncelikli alanlar.
 *
 * ⚠️ SUNUCU EKSIK CEVAP KABUL ETMIYOR (`Eksik cevaplar: ...`), bu
 * yuzden gonderim ancak hepsi cevaplandiginda aciliyor. Yarim gonderip
 * kullaniciya makine listesi gostermek yerine dugme kapali duruyor.
 */
sealed class DegerlendirmeUiState {
    object Yukleniyor : DegerlendirmeUiState()
    data class Sorular(
        val sorular: List<DegerlendirmeSorusuDto>,
        val cevaplar: Map<String, String> = emptyMap(),
        val gonderiliyor: Boolean = false
    ) : DegerlendirmeUiState()
    data class Sonuc(val sonuc: DegerlendirmeSonucuDto) : DegerlendirmeUiState()
    data class Hata(val mesaj: String) : DegerlendirmeUiState()
}

class AssessmentViewModel(
    private val repository: OnboardingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DegerlendirmeUiState>(DegerlendirmeUiState.Yukleniyor)
    val uiState: StateFlow<DegerlendirmeUiState> = _uiState.asStateFlow()

    init {
        yukle()
    }

    /**
     * Once VARSA onceki sonuc gosteriliyor.
     *
     * Kullanici degerlendirmeyi daha once yaptiysa karsisina bos bir
     * anket degil, kendi puanlari cikmali; yeniden yapmak istiyorsa
     * `yenidenBasla()` var.
     */
    fun yukle() {
        _uiState.value = DegerlendirmeUiState.Yukleniyor
        viewModelScope.launch {
            val onceki = repository.degerlendirmeSonucu().getOrNull()
            if (onceki != null && onceki.scores.isNotEmpty()) {
                _uiState.value = DegerlendirmeUiState.Sonuc(onceki)
                return@launch
            }
            sorulariYukle()
        }
    }

    fun yenidenBasla() {
        _uiState.value = DegerlendirmeUiState.Yukleniyor
        viewModelScope.launch { sorulariYukle() }
    }

    private suspend fun sorulariYukle() {
        repository.degerlendirmeSorulari()
            .onSuccess { yanit ->
                _uiState.value = DegerlendirmeUiState.Sorular(yanit.questions)
            }
            .onFailure { hata ->
                _uiState.value = DegerlendirmeUiState.Hata(
                    hata.message ?: "Değerlendirme soruları yüklenemedi."
                )
            }
    }

    fun cevapla(soruId: String, deger: String) {
        val mevcut = _uiState.value as? DegerlendirmeUiState.Sorular ?: return
        _uiState.value = mevcut.copy(cevaplar = mevcut.cevaplar + (soruId to deger))
    }

    fun gonder() {
        val mevcut = _uiState.value as? DegerlendirmeUiState.Sorular ?: return
        if (mevcut.gonderiliyor) return
        /* Kapi burada da: sunucu eksik cevabi zaten reddediyor, ama
           kullaniciya makine mesaji gostermenin anlami yok. */
        if (mevcut.cevaplar.size < mevcut.sorular.size) return

        _uiState.value = mevcut.copy(gonderiliyor = true)
        viewModelScope.launch {
            repository.degerlendirmeGonder(mevcut.cevaplar)
                .onSuccess { _uiState.value = DegerlendirmeUiState.Sonuc(it) }
                .onFailure { hata ->
                    _uiState.value = mevcut.copy(gonderiliyor = false)
                    AppMessages.hata(hata.message ?: "Değerlendirme gönderilemedi.")
                }
        }
    }
}
