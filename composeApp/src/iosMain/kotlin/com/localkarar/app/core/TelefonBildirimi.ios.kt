package com.localkarar.app.core

import platform.UIKit.UIApplication
/* `registerForRemoteNotifications` bir Objective-C KATEGORİ yöntemi: Kotlin/Native onu uzantı fonksiyonu olarak sunar ve AYRICA içe aktarılmayı ister (ilk iOS derlemesi, 30.09.2026: "Unresolved reference"). */
import platform.UIKit.registerForRemoteNotifications
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/*
 * iOS: izin `UNUserNotificationCenter` ile, kayıt `registerForRemoteNotifications`
 * ile. İzin penceresi iOS'ta UYGULAMA BAŞINA BİR KEZ çıkar; kullanıcı
 * reddederse tekrar sorulamaz, yalnız iOS Ayarları'ndan açılır (bkz. Ayarlar
 * ekranındaki "Telefon bildirimleri" satırı).
 *
 * Geri çağrılar UIKit'ten arka kuyrukta gelir; Compose durumuna dokunmadan
 * önce ana kuyruğa alınır.
 */
private fun anaKuyrukta(islem: () -> Unit) {
    dispatch_async(dispatch_get_main_queue()) { islem() }
}

private fun apnsKaydol() {
    UIApplication.sharedApplication.registerForRemoteNotifications()
}

actual object TelefonBildirimi {
    actual val desteklenir: Boolean = true

    actual fun durum(sonuc: (BildirimIzni) -> Unit) {
        UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler { ayarlar ->
            val izin = when (ayarlar?.authorizationStatus) {
                UNAuthorizationStatusAuthorized,
                UNAuthorizationStatusProvisional,
                UNAuthorizationStatusEphemeral -> BildirimIzni.IZINLI
                UNAuthorizationStatusDenied -> BildirimIzni.REDDEDILDI
                else -> BildirimIzni.SORULMADI
            }
            anaKuyrukta { sonuc(izin) }
        }
    }

    actual fun izinIste(sonuc: (Boolean) -> Unit) {
        val secenekler = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(secenekler) { verildi, _ ->
            anaKuyrukta {
                if (verildi) apnsKaydol()
                sonuc(verildi)
            }
        }
    }

    actual fun sessizKayit() {
        durum { if (it == BildirimIzni.IZINLI) apnsKaydol() }
    }
}
