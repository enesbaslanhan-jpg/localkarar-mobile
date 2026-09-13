package com.localkarar.app.core

import android.app.Activity
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

/**
 * Android karsiligi: FLAG_SECURE.
 *
 * Bayrak pencere seviyesinde oldugu icin ekran birakilirken MUTLAKA
 * temizlenmeli; aksi halde koruma tum uygulamaya yayilir ve kullanici hicbir
 * yerde ekran goruntusu alamaz hale gelir.
 *
 * 🔴 SAYAC, TEK BAYRAK DEGIL (14.09.2026).
 *
 * Onceki hal her ekranda dogrudan set/clear yapiyordu. Compose iki ekran
 * arasinda gecerken YENI ekrani once kurar, ESKI ekrani sonra dagitir.
 * Kayit → Giris gibi iki korumali ekran art arda gelince sira su oluyordu:
 * Giris bayragi KOYAR, ardindan Kayit'in dagitimi bayragi KALDIRIR --
 * giris ekrani korumasiz kalir. Tersi de mumkun: dagitim gecikirse bayrak
 * ana sayfaya sizar ve kullanici hicbir yerde ekran goruntusu alamaz
 * (emulatorde bir kez goruldu, 13.09.2026; birebir tekrarlanamadi).
 *
 * Sayac sirayi umursamaz: korumali ekran sayisi 0'dan 1'e cikinca bayrak
 * konur, 1'den 0'a inince kaldirilir. Aradaki set/clear cakismalari
 * ortadan kalkar.
 */
private var korumaliEkranSayisi = 0

private fun bayragiUygula(activity: Activity) {
    if (korumaliEkranSayisi > 0) {
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    } else {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}

@Composable
actual fun SecureScreen(enabled: Boolean) {
    val context = LocalContext.current
    DisposableEffect(enabled) {
        val activity = context as? Activity
        if (enabled && activity != null) {
            korumaliEkranSayisi += 1
            bayragiUygula(activity)
        }
        onDispose {
            if (enabled && activity != null) {
                korumaliEkranSayisi = (korumaliEkranSayisi - 1).coerceAtLeast(0)
                bayragiUygula(activity)
            }
        }
    }
}
