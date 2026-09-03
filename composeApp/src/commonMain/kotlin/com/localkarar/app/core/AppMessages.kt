package com.localkarar.app.core

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Kisa sureli kullanici mesajlari icin tek kanal.
 *
 * NEDEN VAR:
 *
 * `Snackbar` kelimesi tum depoda SIFIR kez geciyordu. Basarisiz bir YAZMA
 * isleminin (gonderi olusturma, kayit duzenleme, urun ayari kaydetme) standart
 * bir yuzeyi yoktu: ya tum ekrani hata ekrani kapliyor -- ki kullanicinin
 * doldurdugu formu goturuyor -- ya da ekrana ozel bir metin gosteriliyordu.
 *
 * Liste ve detay YUKLEME hatalari tam ekran kalmali (o dogru davranis);
 * burasi yalnizca "islem yapildi/yapilamadi" turu geri bildirim icin.
 *
 * Derin baglanti hatalari da buraya akiyor: onceki halinde gecersiz bir
 * baglanti yalnizca konsola yaziliyordu, kullanici uygulamanin neden ana
 * ekrana actigini anlamiyordu.
 */
object AppMessages {

    private val _akis = MutableSharedFlow<AppMessage>(
        replay = 0,
        extraBufferCapacity = 8,
        // Ust uste yigilan mesajlarda EN ESKIYI at: kullaniciya onemli olan
        // en son olan. Askiya alma (SUSPEND) burada yanlis olurdu -- mesaj
        // gondermek arayuz is parcacigini bekletemez.
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val akis: SharedFlow<AppMessage> = _akis.asSharedFlow()

    fun bilgi(metin: String) {
        _akis.tryEmit(AppMessage(metin, AppMessageTuru.BILGI))
    }

    fun hata(metin: String) {
        _akis.tryEmit(AppMessage(metin, AppMessageTuru.HATA))
    }
}

data class AppMessage(
    val metin: String,
    val tur: AppMessageTuru
)

enum class AppMessageTuru { BILGI, HATA }
