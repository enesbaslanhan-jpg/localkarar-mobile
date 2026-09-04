package com.localkarar.app.ui.theme

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext

/**
 * Android karsiligi — `Settings.Global.ANIMATOR_DURATION_SCALE`.
 *
 * Kullanici Gelistirici Secenekleri ya da Erisilebilirlik altindan animasyon
 * olcegini 0'a cektiginde bu deger 0 olur. Android'de webdeki
 * `prefers-reduced-motion`un dogrudan karsiligi budur; `TRANSITION_ANIMATION_SCALE`
 * ve `WINDOW_ANIMATION_SCALE` pencere gecisleri icindir, uygulama ici
 * animasyonlari kapsamaz.
 *
 * ⚠️ Ayar okunamazsa (kisitli profil, ozel ROM) HAREKET ACIK kabul edilir:
 * varsayilan davranisi bozmamak icin. Hata sessizce yutuluyor cunku bu bir
 * tercih sorgusu, kritik bir islem degil.
 */
@Composable
@ReadOnlyComposable
actual fun isReducedMotionEnabled(): Boolean {
    val context = LocalContext.current
    return try {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    } catch (e: Exception) {
        false
    }
}
