package com.localkarar.app.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Destek talebi.
 *
 * Sunucu alanlari zod ile dogruluyor (support.ts): ad 2-100, e-posta gecerli,
 * konu 3-150, mesaj 20-5000. Ayni kurallar burada da uygulaniyor ki kullanici
 * "Gonder"e basip 422 beklemek yerine hatayi ANINDA gorsun.
 *
 * ⚠️ Istemci dogrulamasi sunucununkinin YERINE GECMEZ, yalniz onunla ayni
 * seyi soyler. Sunucu 422 donerse o mesaj yine gosteriliyor.
 */
class SupportViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    private val _gonderiliyor = MutableStateFlow(false)
    val gonderiliyor: StateFlow<Boolean> = _gonderiliyor.asStateFlow()

    private val _gonderildi = MutableStateFlow(false)
    val gonderildi: StateFlow<Boolean> = _gonderildi.asStateFlow()

    private val _hata = MutableStateFlow<String?>(null)
    val hata: StateFlow<String?> = _hata.asStateFlow()

    fun alanHatasi(ad: String, eposta: String, konu: String, mesaj: String): String? = when {
        ad.trim().length < 2 -> "Adınızı yazın (en az 2 karakter)."
        !eposta.trim().contains("@") || eposta.trim().length < 5 ->
            "Geçerli bir e-posta adresi yazın."
        konu.trim().length < 3 -> "Konu en az 3 karakter olmalı."
        mesaj.trim().length < 20 -> "Mesaj en az 20 karakter olmalı."
        mesaj.trim().length > 5000 -> "Mesaj en fazla 5000 karakter olabilir."
        else -> null
    }

    fun gonder(ad: String, eposta: String, konu: String, mesaj: String) {
        if (_gonderiliyor.value) return

        val alanHatasi = alanHatasi(ad, eposta, konu, mesaj)
        if (alanHatasi != null) {
            _hata.value = alanHatasi
            return
        }

        _gonderiliyor.value = true
        _hata.value = null
        viewModelScope.launch {
            repository.destekTalebiGonder(ad, eposta, konu, mesaj)
                .onSuccess {
                    _gonderildi.value = true
                    _gonderiliyor.value = false
                }
                .onFailure { e ->
                    _hata.value = e.message ?: "Talebiniz gönderilemedi."
                    _gonderiliyor.value = false
                }
        }
    }

    fun sifirla() {
        _gonderildi.value = false
        _hata.value = null
    }
}
