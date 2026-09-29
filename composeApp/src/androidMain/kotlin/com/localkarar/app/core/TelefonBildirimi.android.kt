package com.localkarar.app.core

/*
 * Android'de telefon bildirimi HENÜZ YOK: Firebase Cloud Messaging ayrı bir
 * Firebase projesi ve `google-services.json` ister. O gelene dek `desteklenir`
 * false ve hiçbir izin sorusu çıkmaz; iOS ile aynı ekran kodu çalışır.
 */
actual object TelefonBildirimi {
    actual val desteklenir: Boolean = false
    actual fun durum(sonuc: (BildirimIzni) -> Unit) = sonuc(BildirimIzni.DESTEKLENMIYOR)
    actual fun izinIste(sonuc: (Boolean) -> Unit) = sonuc(false)
    actual fun sessizKayit() = Unit
}
