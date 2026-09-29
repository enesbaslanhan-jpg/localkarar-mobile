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
    /** AI Mentor veri isleme bilgilendirmesi onaylandi (deger: onay tarihi, ISO). Cihaz basina. */
    const val AI_MENTOR_ONAY = "ai_mentor_onay"
    /** Telefon bildirimi izin sorusu gösterildi (bir kez; cihaz başına). */
    const val PUSH_SORULDU = "push_sorulan"
    /** İlk açılışın epoch ms'si; geri bildirim kartı bundan 3 gün sonra çıkar. */
    const val ILK_ACILIS = "ilk_acilis_ms"
    /** Geri bildirim kartı gösterildi/gönderildi/kapatıldı (bir daha çıkmaz). */
    const val GERIBILDIRIM_KARTI = "geribildirim_karti"
}

/** Kabukta saglanir; ekranlar tercih deposuna buradan ulasir. */
val LocalAppPreferences = androidx.compose.runtime.staticCompositionLocalOf<AppPreferences?> { null }
