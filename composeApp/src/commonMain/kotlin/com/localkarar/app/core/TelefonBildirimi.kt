package com.localkarar.app.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * TELEFON BİLDİRİMİ (push) — 29.09.2026.
 *
 * Bugüne dek bildirimler yalnız uygulamanın içindeki zilde görünüyordu. Bu
 * dosya iOS'ta Apple bildirim servisine (APNs) kayıt için köprü:
 *
 *   Swift AppDelegate ──token──▶ [PushKaydi] ──▶ AppShell ──▶ POST /account/devices
 *
 * Android ŞİMDİLİK YOK (FCM ayrı bir Firebase projesi ister): `desteklenir`
 * false döner, hiçbir izin sorusu çıkmaz.
 */
object PushKaydi {
    private val _kod = MutableStateFlow<String?>(null)

    /** Apple'ın bu cihaz için verdiği kod (hex). Henüz alınmadıysa null. */
    val kod: StateFlow<String?> = _kod

    /** Swift `AppDelegate` çağırır (didRegisterForRemoteNotificationsWithDeviceToken). */
    fun tokenAlindi(token: String) {
        _kod.value = token
    }

    /** Swift `AppDelegate` çağırır (didFailToRegisterForRemoteNotificationsWithError). */
    fun kayitBasarisiz(sebep: String) {
        AppLog.w("Push", "APNs kaydı başarısız: $sebep")
    }
}

enum class BildirimIzni { SORULMADI, IZINLI, REDDEDILDI, DESTEKLENMIYOR }

expect object TelefonBildirimi {
    /** Bu platformda telefon bildirimi var mı (iOS: evet, Android: henüz hayır). */
    val desteklenir: Boolean

    /** Sistem izin durumu; sonuç ANA iş parçacığında döner. */
    fun durum(sonuc: (BildirimIzni) -> Unit)

    /** Sistem izin penceresini açar; verilirse APNs'e kaydolur. Sonuç ana iş parçacığında. */
    fun izinIste(sonuc: (Boolean) -> Unit)

    /** Zaten izinliyse cihaz kodunu tazelemek için sessizce kaydolur (soru sormaz). */
    fun sessizKayit()
}
