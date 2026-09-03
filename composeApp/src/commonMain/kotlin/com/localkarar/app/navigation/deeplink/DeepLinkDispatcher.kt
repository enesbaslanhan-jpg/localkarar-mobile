package com.localkarar.app.navigation.deeplink

import com.localkarar.app.core.AppLog
import com.localkarar.app.core.AppMessages

/**
 * Gelen derin baglantiyi ayristirip bekleyen hedef olarak parkeder.
 *
 * 🔴 GECERSIZ BAGLANTI ARTIK SESSIZ DEGIL.
 *
 * Onceki halinde `Unsupported` ve `Malformed` yalnizca konsola yaziliyor ve
 * `false` donuluyordu; donus degeri ise HER IKI cagri yerinde de atiliyordu
 * (MainActivity.handleIntent ve ContentView.swift). Sonuc: bozuk bir
 * `localkarar.com/app/...` baglantisi uygulamayi sessizce ana ekrana aciyor,
 * kullanici tikladigi seyin neden acilmadigini anlamiyordu.
 *
 * Ayristiricinin KENDISI saglam (kati sema/host/segment dogrulamasi;
 * DeepLinkParserTest 20 dusmanca girdiyi reddediyor) -- degisen yalnizca
 * sonucun kullaniciya bildirilmesi.
 */
object DeepLinkDispatcher {

    fun submit(rawUrl: String?): Boolean {
        if (rawUrl.isNullOrBlank()) return false

        return when (val result = DeepLinkParser.parse(rawUrl)) {
            is DeepLinkResult.Success -> {
                PendingDeepLinkStore.set(result.target)
                true
            }
            is DeepLinkResult.Unsupported -> {
                AppLog.w("DeepLink", "Unsupported deep link: $rawUrl")
                // "Desteklenmiyor" = adres gecerli ama mobilde karsiligi yok
                // (ornegin yalniz webde bulunan bir sayfa). Kullanicinin
                // yapabilecegi sey tarayicida acmak; mesaj onu soyluyor.
                AppMessages.bilgi("Bu bağlantı uygulamada açılamıyor. Tarayıcıdan deneyebilirsiniz.")
                false
            }
            is DeepLinkResult.Malformed -> {
                AppLog.w("DeepLink", "Malformed deep link: $rawUrl (${result.reason})")
                AppMessages.hata("Bağlantı geçersiz görünüyor.")
                false
            }
        }
    }
}
