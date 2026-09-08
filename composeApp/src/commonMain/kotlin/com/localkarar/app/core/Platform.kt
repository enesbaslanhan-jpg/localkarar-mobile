package com.localkarar.app.core

import androidx.compose.runtime.Composable

data class PickedFile(
    val name: String,
    val bytes: ByteArray
)

expect fun openExternalUrl(url: String)

@Composable
expect fun rememberFilePicker(onFilePicked: (PickedFile?) -> Unit): () -> Unit
/**
 * KAMERAYLA BELGE CEKIMI.
 *
 * `null` donuyorsa platformda kamera yolu YOK ve arayuz dugmeyi hic
 * cizmiyor. Calismayan bir "Fotoğraf çek" dugmesi koymak, kullaniciyi
 * bos yere dokunduran bir yalan olurdu.
 *
 * ⚠️ Android tarafinda CAMERA IZNI BILEREK ISTENMIYOR: cekim, sistemin
 * kamera uygulamasina bir dosya adresi verilerek yaptiriliyor
 * (`ActivityResultContracts.TakePicture`). Manifest'te `CAMERA` izni
 * BILDIRILMEDIGI surece sistem calisma zamani izni istemez; bildirilseydi
 * kullaniciya gereksiz bir izin sorusu sorulurdu.
 */
@Composable
expect fun rememberCameraCapture(onPhotoTaken: (PickedFile?) -> Unit): (() -> Unit)?

/**
 * Paylasilacak dosya — bellekte tutulan icerik.
 *
 * `bytes` bilerek hazir: sunucudan inen belge zaten bellege aliniyor ve
 * ikinci bir disk yolu acmak, silinmesi unutulacak bir kopya birakirdi.
 * Platform tarafi gecici dosyayi kendisi yazip kendisi siliyor.
 */
data class SharedFile(
    val name: String,
    /** Ornek: "application/pdf", "text/csv". */
    val mimeType: String,
    val bytes: ByteArray
)

/**
 * SISTEM PAYLASIM SAYFASI.
 *
 * 🔴 WEBDEKI KARSILIGI "İNDİR" DEGIL "PAYLAS".
 *
 * Web tarafi kaydi CSV/Excel/PDF olarak indiriyor (`exports.downloadRecords`,
 * `<a download>`). Telefonda "indirilenler klasoru" ayni sey degil: kullanici
 * belgeyi muhasebecisine gondermek, WhatsApp'a atmak ya da Dosyalar'a
 * kaydetmek istiyor. Sistem paylasim sayfasi bu ucunu birden veriyor;
 * "kaydet" onun icindeki secenelerden yalnizca biri.
 *
 * `null` donuyorsa platformda paylasim yolu YOK ve arayuz dugmeyi hic
 * cizmiyor — `rememberCameraCapture` ile ayni kural: calismayan bir dugme
 * koymak yerine dugmeyi koymuyoruz.
 */
@Composable
expect fun rememberFileSharer(): ((SharedFile) -> Unit)?

/**
 * METIN PAYLASIMI — kisa bir ozeti sistem paylasim sayfasina verir.
 *
 * ⚠️ `rememberFileSharer` ILE AYNI SEY DEGIL, bilerek ayri.
 *
 * Karar makbuzu bir DOSYA degil, birkac satirlik bir ozet. Dosya olarak
 * paylasilsaydi WhatsApp'a bir .txt eki olarak duserdi ve alici onu acmak
 * zorunda kalirdi; metin olarak paylasilinca mesajin KENDISI oluyor.
 *
 * Webdeki karsiligi "Yazdır" (`window.print()`). Telefonda yazdirma dogal
 * eylem degil; paylasim sayfasi hem gondermeyi hem kaydetmeyi hem de
 * (yazici tanimliysa) yazdirmayi tek yerde veriyor.
 */
@Composable
expect fun rememberTextSharer(): ((baslik: String, metin: String) -> Unit)?
