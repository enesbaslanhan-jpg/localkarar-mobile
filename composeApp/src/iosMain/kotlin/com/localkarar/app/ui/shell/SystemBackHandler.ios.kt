package com.localkarar.app.ui.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

/*
 * iOS'TA "GERI" = SOL KENARDAN KAYDIRMA.
 *
 * Android'de sistem geri tusu/jesti `BackHandler` ile geliyor. iOS'ta karsiligi
 * UIKit'in kenar jesti (`UIScreenEdgePanGestureRecognizer`); MainViewController
 * bunu Compose gorunumune baglar ve tetiklenince buradaki kaydin EN SON etkin
 * isleyicisini cagirir. Compose seviyesinde yazilan pointerInput jestleri iOS'ta
 * tetiklenmedi (TestFlight 112-115, urun sahibi dogruladi); sistem tanıyıcısı
 * Compose'un dokunus akisindan bagimsizdir.
 *
 * Kayit bir yigin: birden fazla ekran ayni anda isleyici koyabilir (kabuk +
 * alt sayfa); Android'in BackHandler'i gibi en son kompoze edilen kazanir.
 */
object KenarGeriKaydi {
    private class Kayit(val etkin: Boolean, val geri: () -> Unit)
    private val yigin = mutableListOf<Kayit>()

    fun ekle(etkin: Boolean, geri: () -> Unit): Any {
        val k = Kayit(etkin, geri); yigin.add(k); return k
    }

    fun kaldir(anahtar: Any) { yigin.remove(anahtar) }

    /** Kenar jesti tamamlandi: en son etkin isleyici kosar. Kimse yoksa false. */
    fun tetikle(): Boolean {
        val k = yigin.lastOrNull { it.etkin } ?: return false
        k.geri(); return true
    }
}

@Composable
actual fun SystemBackHandler(enabled: Boolean, onBack: () -> Unit) {
    DisposableEffect(enabled, onBack) {
        val anahtar = KenarGeriKaydi.ekle(enabled, onBack)
        onDispose { KenarGeriKaydi.kaldir(anahtar) }
    }
}
