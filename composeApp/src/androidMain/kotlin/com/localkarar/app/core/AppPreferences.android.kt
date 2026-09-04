package com.localkarar.app.core

import android.content.Context
import android.content.SharedPreferences

private const val DEPO_ADI = "app_prefs"

/**
 * Tercih deposu — duz `SharedPreferences`.
 *
 * Sifreleme YOK ve gerekmiyor: burada gizli bir sey saklanmiyor (su an yalniz
 * tema secimi). `SecureStorage`tan ayri olmasinin sebebi o dosyadaki notta.
 */
actual class AppPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(DEPO_ADI, Context.MODE_PRIVATE)

    actual fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    actual fun getString(key: String): String? = prefs.getString(key, null)

    actual fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }
}
