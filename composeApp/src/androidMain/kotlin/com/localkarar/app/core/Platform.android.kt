package com.localkarar.app.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

object AppContextHolder {
    var appContext: Context? = null
}

actual fun openExternalUrl(url: String) {
    val ctx = AppContextHolder.appContext ?: return
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(intent)
    } catch (e: Exception) {
        AppLog.e("Platform", "openExternalUrl failed", e)
    }
}

@Composable
actual fun rememberFilePicker(onFilePicked: (PickedFile?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            onFilePicked(null)
            return@rememberLauncherForActivityResult
        }
        var displayName = "dosya"
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    displayName = cursor.getString(nameIndex) ?: "dosya"
                }
            }
        } catch (e: Exception) {
            displayName = "dosya"
        }
        val bytes = try {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (e: Exception) {
            null
        }
        if (bytes == null) {
            onFilePicked(null)
        } else {
            onFilePicked(PickedFile(name = displayName, bytes = bytes))
        }
    }
    return {
        launcher.launch(arrayOf("*/*"))
    }
}
@Composable
actual fun rememberCameraCapture(onPhotoTaken: (PickedFile?) -> Unit): (() -> Unit)? {
    val context = LocalContext.current

    /*
     * Cekilen fotograf ONCE onbellege yaziliyor, sonra okunup yukleniyor.
     *
     * Sistem kamerasi bize goruntuyu bellekte vermiyor; yalnizca
     * yazabilecegi bir adres istiyor. `TakePicturePreview` kucuk resim
     * donduruyor (belge okunmaz), bu yuzden FileProvider adresi
     * kullaniliyor.
     */
    var hedef by remember { mutableStateOf<Uri?>(null) }
    var hedefDosya by remember { mutableStateOf<File?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { basarili ->
        val dosya = hedefDosya
        if (!basarili || dosya == null) {
            onPhotoTaken(null)
            return@rememberLauncherForActivityResult
        }
        val bytes = try {
            dosya.readBytes()
        } catch (e: Exception) {
            AppLog.e("Platform", "kamera dosyasi okunamadi", e)
            null
        }
        /* Onbellek dosyasi hemen siliniyor: belge yukleniyor, cihazda
           ikinci bir kopya birakmaya gerek yok. */
        try {
            dosya.delete()
        } catch (_: Exception) {
        }
        onPhotoTaken(bytes?.takeIf { it.isNotEmpty() }?.let { PickedFile(dosya.name, it) })
    }

    return {
        try {
            val klasor = File(context.cacheDir, "belge-cekim").apply { mkdirs() }
            val dosya = File(klasor, "belge-${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                dosya
            )
            hedefDosya = dosya
            hedef = uri
            launcher.launch(uri)
        } catch (e: Exception) {
            AppLog.e("Platform", "kamera acilamadi", e)
            onPhotoTaken(null)
        }
    }
}

@Composable
actual fun rememberFileSharer(): ((SharedFile) -> Unit)? {
    val context = LocalContext.current
    return { dosya ->
        try {
            /*
             * Gecici dosya onbellekte, KENDI klasorunde.
             *
             * `file_paths.xml` yalnizca `belge-cekim` ve `paylasim`
             * klasorlerini aciyor; uygulamanin geri kalan dosyalari
             * (oturum anahtari dahil) bu saglayicidan okunamiyor.
             */
            val klasor = File(context.cacheDir, "paylasim").apply { mkdirs() }

            /*
             * ⚠️ ESKI PAYLASIMLAR TEMIZLENIYOR.
             *
             * Dosyayi paylasimdan HEMEN SONRA silemiyoruz: alici uygulama
             * (e-posta, WhatsApp, Dosyalar) okumayi kendi zamaninda
             * yapiyor ve sildigimiz an bos dosya aliyor. Bunun yerine bir
             * sonraki paylasimda bir saatten eski artiklar siliniyor —
             * boylece onbellek suresiz buyumuyor.
             */
            val esik = System.currentTimeMillis() - 60 * 60 * 1000
            klasor.listFiles()?.forEach { eski ->
                if (eski.lastModified() < esik) runCatching { eski.delete() }
            }

            val hedef = File(klasor, dosya.name)
            hedef.writeBytes(dosya.bytes)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                hedef
            )

            val niyet = Intent(Intent.ACTION_SEND).apply {
                type = dosya.mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                /* Okuma izni ALICIYA veriliyor; olmadan alici uygulama
                   adresi gorur ama dosyayi acamaz. */
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            /*
             * `createChooser` + NEW_TASK: paylasim sayfasi Activity
             * baglami disindan da acilabiliyor. Dogrudan `startActivity`
             * cagirmak, kullanicinin varsayilan uygulamasi yoksa
             * ActivityNotFoundException atardi.
             */
            val secici = Intent.createChooser(niyet, "Paylaş").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(secici)
        } catch (e: Exception) {
            AppLog.e("Platform", "dosya paylasilamadi", e)
            AppMessages.hata("Dosya paylaşılamadı.")
        }
    }
}

@Composable
actual fun rememberTextSharer(): ((baslik: String, metin: String) -> Unit)? {
    val context = LocalContext.current
    return { baslik, metin ->
        try {
            val niyet = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, baslik)
                putExtra(Intent.EXTRA_TEXT, metin)
            }
            context.startActivity(
                Intent.createChooser(niyet, "Paylaş").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        } catch (e: Exception) {
            AppLog.e("Platform", "metin paylasilamadi", e)
            AppMessages.hata("Paylaşılamadı.")
        }
    }
}
