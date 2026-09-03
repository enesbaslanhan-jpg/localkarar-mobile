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
 * yerde ekran goruntusu alamaz hale gelir. `DisposableEffect` bunu garanti
 * ediyor.
 */
@Composable
actual fun SecureScreen(enabled: Boolean) {
    val context = LocalContext.current
    DisposableEffect(enabled) {
        val activity = context as? Activity
        if (enabled && activity != null) {
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
