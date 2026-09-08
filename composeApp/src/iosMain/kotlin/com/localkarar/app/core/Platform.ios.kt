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
    var activeDelegate by remember { mutableStateOf<DocumentPickerDelegate?>(null) }

    return {
        try {
            val delegate = DocumentPickerDelegate(
                onResult = onFilePicked,
                onDismiss = { activeDelegate = null }
            )
            activeDelegate = delegate

            val picker = UIDocumentPickerViewController(
                documentTypes = listOf("public.item", "public.content", "public.data"),
                inMode = UIDocumentPickerMode.UIDocumentPickerModeImport
            ).apply {
                this.delegate = delegate
                this.allowsMultipleSelection = false
            }

            val window = UIApplication.sharedApplication.keyWindow
                ?: UIApplication.sharedApplication.windows.firstOrNull { (it as? UIWindow)?.isKeyWindow() == true } as? UIWindow
                ?: UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow

            var topVc = window?.rootViewController
            while (topVc?.presentedViewController != null) {
                topVc = topVc.presentedViewController
            }

            if (topVc != null) {
                topVc.presentViewController(picker, animated = true, completion = null)
            } else {
                AppLog.w("Platform", "No rootViewController found to present document picker")
                onFilePicked(null)
                activeDelegate = null
            }
        } catch (e: Exception) {
            AppLog.e("Platform", "rememberFilePicker error", e)
            onFilePicked(null)
            activeDelegate = null
        }
    }
}
/**
 * iOS'ta kamera yolu HENUZ YOK.
 *
 * `UIImagePickerController` sarmalayicisi yazilmadi; `null` donerek
 * arayuze "bu platformda bu dugmeyi cizme" deniyor. Yanlis calisan bir
 * dugme yerine olmayan bir dugme.
 */
@Composable
actual fun rememberCameraCapture(onPhotoTaken: (PickedFile?) -> Unit): (() -> Unit)? = null

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
