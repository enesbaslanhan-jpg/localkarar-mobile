package com.localkarar.app.core

/**
 * Basit, SIFRELENMEMIS tercih deposu.
 *
 * ⚠️ `SecureStorage` KULLANILMIYOR ve bu bilincli. O depo iki sebeple yanlis
 * yer:
 *   1. Cikista temizleniyor (`clearAll`). Tema secimi cikistan sonra da
 *      kalmali -- webde de `localStorage`'da duruyor ve oturumdan bagimsiz.
 *   2. Sifreleme gereksiz: burada saklanan sey bir gizli bilgi degil.
 *
 * Webdeki karsiligi: `frontend/src/context/ThemeContext.jsx` ->
 * `localStorage` + `prefers-color-scheme` yedegi.
 */
expect class AppPreferences {
    fun putString(key: String, value: String)
    fun getString(key: String): String?
    fun remove(key: String)
}

/** Tercih anahtarlari tek yerde; elle yazilan dizeler kaymasin. */
object PrefKeys {
    const val THEME_MODE = "theme_mode"
}
