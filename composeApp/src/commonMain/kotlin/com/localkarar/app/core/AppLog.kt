package com.localkarar.app.core

import com.localkarar.app.network.AppEnvironmentProvider

/**
 * Release derlemesinde susan gunluk katmani.
 *
 * NEDEN VAR:
 *
 * Kod her yerde dogrudan `println` cagiriyordu ve Ktor'un Logging eklentisi
 * `LogLevel.INFO` ile kuruluydu. INFO seviyesi BASLIKLARI da yaziyor -- yani
 * `defaultRequest` icinde eklenen `Authorization: Bearer <token>` release
 * derlemesinde logcat'e dusuyordu. Ayni cihazdaki baska bir uygulama bunu
 * Android 11+ ile okuyamaz, ama `adb logcat`, cihaz yedekleri ve hata toplama
 * araclari okur; token'in 8 saatlik omru boyunca hesap tamamen ele gecirilebilir.
 *
 * Bu yuzden gunlukleme tek bir kapidan geciyor ve o kapi release'de kapali.
 * Dogrudan `println` kullanilmamali.
 */
object AppLog {

    private val enabled: Boolean get() = !AppEnvironmentProvider.isRelease

    fun d(tag: String, message: String) {
        if (enabled) println("[$tag] $message")
    }

    fun w(tag: String, message: String) {
        if (enabled) println("[$tag] UYARI: $message")
    }

    /**
     * Hata gunlugu. `throwable` mesaji da yalnizca debug'da yaziliyor:
     * sunucu hata govdeleri e-posta, calisma alani adi ve dogrulama
     * ayrintilari tasiyabiliyor.
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (enabled) {
            println("[$tag] HATA: $message")
            throwable?.let { println("[$tag] HATA: ${it::class.simpleName}: ${it.message}") }
        }
    }
}
