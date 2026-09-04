package com.localkarar.app.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.localkarar.app.core.AppPreferences
import com.localkarar.app.core.PrefKeys

/**
 * Tema secimi.
 *
 * Webdeki `ThemeContext` ile AYNI uc durum: kullanici acik ya da koyu secebilir,
 * secmediyse SISTEM tercihi gecerlidir.
 */
enum class ThemeMode {
    SYSTEM, LIGHT, DARK;

    companion object {
        fun fromStored(raw: String?): ThemeMode = when (raw) {
            "light" -> LIGHT
            "dark" -> DARK
            else -> SYSTEM
        }
    }

    fun toStored(): String = when (this) {
        LIGHT -> "light"
        DARK -> "dark"
        SYSTEM -> "system"
    }
}

/**
 * Tema durumunu tutar ve secimi kalici olarak saklar.
 *
 * ⚠️ Secim CIKISTA SILINMEZ: `AppPreferences` kullaniliyor, `SecureStorage`
 * degil. Webde de tema `localStorage`'da ve oturumdan bagimsiz -- kullanici
 * cikip girdiginde temasi degismemeli.
 */
class ThemeController(private val prefs: AppPreferences) {

    var mode: ThemeMode by mutableStateOf(
        ThemeMode.fromStored(prefs.getString(PrefKeys.THEME_MODE))
    )
        private set

    /** Secimi uygular ve kalici olarak saklar. */
    fun select(yeni: ThemeMode) {
        mode = yeni
        prefs.putString(PrefKeys.THEME_MODE, yeni.toStored())
    }

    /** Acik <-> koyu arasinda gecis. SISTEM'deyken yururlukteki halin tersine gecer. */
    fun toggle(sistemKoyuMu: Boolean) {
        val suAnKoyu = when (mode) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> sistemKoyuMu
        }
        select(if (suAnKoyu) ThemeMode.LIGHT else ThemeMode.DARK)
    }
}

/**
 * Denetleyiciye her yerden erisim.
 *
 * Varsayilan `null`: saglanmadan kullanilirsa sessizce yanlis davranmak yerine
 * cagiran tarafta acikca ele alinir.
 */
val LocalThemeController = staticCompositionLocalOf<ThemeController?> { null }
