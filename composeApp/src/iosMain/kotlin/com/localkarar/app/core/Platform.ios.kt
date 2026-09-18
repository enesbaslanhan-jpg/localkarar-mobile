package com.localkarar.app.core

import androidx.compose.runtime.*
import kotlinx.cinterop.*
import platform.Foundation.*
import platform.UIKit.*
import platform.darwin.NSObject
import platform.posix.memcpy

actual fun openExternalUrl(url: String) {
    try {
        val trimmed = url.trim()
        if (trimmed.isBlank()) return
        val nsUrl = NSURL.URLWithString(trimmed) ?: return
        val app = UIApplication.sharedApplication
        if (app.canOpenURL(nsUrl)) {
            app.openURL(nsUrl, options = emptyMap<Any?, Any?>(), completionHandler = null)
        }
    } catch (e: Exception) {
        AppLog.e("Platform", "openExternalUrl failed", e)
    }
}

/** En ustteki sunulmus denetleyici; sunum her zaman buradan yapilir. */
private fun enUstDenetleyici(): UIViewController? {
    val pencere = UIApplication.sharedApplication.keyWindow
        ?: UIApplication.sharedApplication.windows.firstOrNull { (it as? UIWindow)?.isKeyWindow() == true } as? UIWindow
        ?: UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow
    var ust = pencere?.rootViewController
    while (ust?.presentedViewController != null) ust = ust.presentedViewController
    return ust
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.baytlar(): ByteArray {
    val uzunluk = length.toInt()
    val dizi = ByteArray(uzunluk)
    if (uzunluk > 0) dizi.usePinned { memcpy(it.addressOf(0), bytes, length) }
    return dizi
}

/*
 * FOTOGRAF SECICI / KAMERA DELEGESI — UIImagePickerController (18.09.2026).
 *
 * Kamera ve galeri ayni siniftan sunulur; fark yalniz `sourceType`. Secilen
 * gorsel JPEG (%85) olarak baytlara cevrilir; OCR ve sunucu yolu Android ile
 * ayni (PickedFile). Delegate referansi cagri boyunca disarida tutulur, yoksa
 * ARC erken serbest birakir ve geri cagrim hic gelmez.
 */
private class GorselSeciciDelegesi(
    private val onResult: (PickedFile?) -> Unit,
    private val onDismiss: () -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    @OptIn(ExperimentalForeignApi::class)
    override fun imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo: Map<Any?, *>) {
        picker.dismissViewControllerAnimated(true, completion = null)
        val gorsel = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        val veri = gorsel?.let { UIImageJPEGRepresentation(it, 0.85) }
        if (veri == null) {
            onResult(null)
        } else {
            val ad = "fotograf-" + NSDate().timeIntervalSince1970.toLong().toString() + ".jpg"
            onResult(PickedFile(name = ad, bytes = veri.baytlar()))
        }
        onDismiss()
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
        onResult(null)
        onDismiss()
    }
}

private fun gorselSeciciSun(
    kaynak: UIImagePickerControllerSourceType,
    delege: GorselSeciciDelegesi
): Boolean {
    val ust = enUstDenetleyici() ?: return false
    val secici = UIImagePickerController().apply {
        sourceType = kaynak
        delegate = delege
        allowsEditing = false
    }
    ust.presentViewController(secici, animated = true, completion = null)
    return true
}

private class DocumentPickerDelegate(
    private val onResult: (PickedFile?) -> Unit,
    private val onDismiss: () -> Unit
) : NSObject(), UIDocumentPickerDelegateProtocol {

    @OptIn(ExperimentalForeignApi::class)
    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        if (url == null) {
            onResult(null)
            onDismiss()
            return
        }

        val isSecurityScoped = url.startAccessingSecurityScopedResource()
        try {
            val data = NSData.dataWithContentsOfURL(url)
            val fileName = url.lastPathComponent ?: "dosya"
            if (data != null) {
                val length = data.length.toInt()
                val bytes = ByteArray(length)
                if (length > 0) {
                    bytes.usePinned { pinned ->
                        memcpy(pinned.addressOf(0), data.bytes, data.length)
                    }
                }
                onResult(PickedFile(name = fileName, bytes = bytes))
            } else {
                onResult(null)
            }
        } catch (e: Exception) {
            AppLog.e("Platform", "Error reading picked file", e)
            onResult(null)
        } finally {
            if (isSecurityScoped) {
                url.stopAccessingSecurityScopedResource()
            }
            onDismiss()
        }
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onResult(null)
        onDismiss()
    }
}

@Composable
actual fun rememberFilePicker(onFilePicked: (PickedFile?) -> Unit): () -> Unit {
    var aktifBelgeDelegesi by remember { mutableStateOf<DocumentPickerDelegate?>(null) }
    var aktifGorselDelegesi by remember { mutableStateOf<GorselSeciciDelegesi?>(null) }

    fun dosyaSeciciAc() {
        val delegate = DocumentPickerDelegate(
            onResult = onFilePicked,
            onDismiss = { aktifBelgeDelegesi = null }
        )
        aktifBelgeDelegesi = delegate
        val picker = UIDocumentPickerViewController(
            documentTypes = listOf("public.item", "public.content", "public.data"),
            inMode = UIDocumentPickerMode.UIDocumentPickerModeImport
        ).apply {
            this.delegate = delegate
            this.allowsMultipleSelection = false
        }
        val ust = enUstDenetleyici()
        if (ust != null) {
            ust.presentViewController(picker, animated = true, completion = null)
        } else {
            AppLog.w("Platform", "No rootViewController found to present document picker")
            onFilePicked(null)
            aktifBelgeDelegesi = null
        }
    }

    fun galeriAc() {
        val delege = GorselSeciciDelegesi(onResult = onFilePicked, onDismiss = { aktifGorselDelegesi = null })
        aktifGorselDelegesi = delege
        if (!gorselSeciciSun(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary, delege)) {
            onFilePicked(null); aktifGorselDelegesi = null
        }
    }

    return {
        try {
            /*
             * "Dosya veya fotograf sec" iOS'ta ONCE bir secim sayfasi acar:
             * Dosyalar uygulamasi fotograf kutuphanesini gostermez, o yuzden
             * galeri ayri bir yol (TestFlight 109, 18.09.2026).
             */
            val ust = enUstDenetleyici()
            if (ust == null) {
                onFilePicked(null)
            } else {
                val sayfa = UIAlertController.alertControllerWithTitle(
                    title = null,
                    message = null,
                    preferredStyle = UIAlertControllerStyle.UIAlertControllerStyleActionSheet
                )
                sayfa.addAction(UIAlertAction.actionWithTitle("Fotoğraflardan seç", style = UIAlertActionStyle.UIAlertActionStyleDefault) { galeriAc() })
                sayfa.addAction(UIAlertAction.actionWithTitle("Dosyalardan seç", style = UIAlertActionStyle.UIAlertActionStyleDefault) { dosyaSeciciAc() })
                sayfa.addAction(UIAlertAction.actionWithTitle("Vazgeç", style = UIAlertActionStyle.UIAlertActionStyleCancel) { onFilePicked(null) })
                /* iPad: popover kaynagi verilmezse cokuyor. */
                sayfa.popoverPresentationController?.sourceView = ust.view
                ust.presentViewController(sayfa, animated = true, completion = null)
            }
        } catch (e: Exception) {
            AppLog.e("Platform", "rememberFilePicker error", e)
            onFilePicked(null)
        }
    }
}

/**
 * KAMERA — UIImagePickerController(camera). Info.plist'te NSCameraUsageDescription
 * var; sistem ilk kullanimda izin sorar. Kamerasi olmayan cihazda (simulator)
 * `null` doner ve dugme cizilmez.
 */
@Composable
actual fun rememberCameraCapture(onPhotoTaken: (PickedFile?) -> Unit): (() -> Unit)? {
    if (!UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)) return null
    var aktifDelege by remember { mutableStateOf<GorselSeciciDelegesi?>(null) }
    return {
        try {
            val delege = GorselSeciciDelegesi(onResult = onPhotoTaken, onDismiss = { aktifDelege = null })
            aktifDelege = delege
            if (!gorselSeciciSun(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera, delege)) {
                onPhotoTaken(null); aktifDelege = null
            }
        } catch (e: Exception) {
            AppLog.e("Platform", "rememberCameraCapture error", e)
            onPhotoTaken(null)
        }
    }
}

/**
 * iOS PAYLASIM SAYFASI — `UIActivityViewController`.
 *
 * Android'in `ACTION_SEND` seciciyle ayni is: kullaniciya "Mesajlar,
 * Mail, Dosyalara Kaydet, WhatsApp..." listesini acar. iOS'ta "indir"
 * kavrami zaten yok; belgeyi bir yere gondermenin TEK dogru yolu bu.
 *
 * ⚠️ Kamera aksine burada `null` DONMUYOR: bu API her iOS surumunde var
 * ve sarmalanmasi kucuk. Dolayisiyla disa aktarma dugmesi iOS'ta da
 * ciziliyor.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberFileSharer(): ((SharedFile) -> Unit)? {
    return { dosya ->
        try {
            /*
             * Gecici dizine yaziliyor; iOS bu dizini kendisi temizliyor,
             * bu yuzden Android'deki gibi elle sureye bakan bir temizlik
             * gerekmiyor.
             */
            val klasor = NSTemporaryDirectory()
            val yol = klasor + dosya.name

            val veri = dosya.bytes.usePinned { sabit ->
                NSData.create(
                    bytes = sabit.addressOf(0),
                    length = dosya.bytes.size.toULong()
                )
            }
            veri.writeToFile(yol, atomically = true)

            val url = NSURL.fileURLWithPath(yol)
            val denetleyici = UIActivityViewController(
                activityItems = listOf(url),
                applicationActivities = null
            )

            /*
             * En ustteki denetleyiciden sunuluyor — `rememberFilePicker`
             * ile ayni desen. Kok denetleyiciden sunmak, uzerinde acik
             * bir sayfa varken "already presenting" hatasi verirdi.
             */
            var ustVc = UIApplication.sharedApplication.keyWindow?.rootViewController
            while (ustVc?.presentedViewController != null) {
                ustVc = ustVc.presentedViewController
            }

            if (ustVc == null) {
                AppLog.w("Platform", "paylasim icin rootViewController yok")
                AppMessages.hata("Dosya paylaşılamadı.")
            } else {
                /*
                 * iPad'de `popoverPresentationController` bir kaynak
                 * gostermeden sunulursa uygulama COKUYOR. Kaynak olarak
                 * sunan gorunumun ortasi veriliyor.
                 */
                denetleyici.popoverPresentationController?.sourceView = ustVc.view
                ustVc.presentViewController(denetleyici, animated = true, completion = null)
            }
        } catch (e: Exception) {
            AppLog.e("Platform", "dosya paylasilamadi", e)
            AppMessages.hata("Dosya paylaşılamadı.")
        }
    }
}

/**
 * ⚠️ BU MAKINEDE DERLENMEDI. Kotlin/Native'in Apple hedefleri macOS
 * istiyor; gelistirme Windows uzerinde yurudu. Kod `rememberFileSharer`
 * ile ayni deseni izliyor ama DENENMEDI.
 */
@Composable
actual fun rememberTextSharer(): ((baslik: String, metin: String) -> Unit)? {
    return { _, metin ->
        try {
            val denetleyici = UIActivityViewController(
                activityItems = listOf(metin),
                applicationActivities = null
            )
            var ustVc = UIApplication.sharedApplication.keyWindow?.rootViewController
            while (ustVc?.presentedViewController != null) {
                ustVc = ustVc.presentedViewController
            }
            if (ustVc == null) {
                AppMessages.hata("Paylaşılamadı.")
            } else {
                denetleyici.popoverPresentationController?.sourceView = ustVc.view
                ustVc.presentViewController(denetleyici, animated = true, completion = null)
            }
        } catch (e: Exception) {
            AppLog.e("Platform", "metin paylasilamadi", e)
            AppMessages.hata("Paylaşılamadı.")
        }
    }
}
