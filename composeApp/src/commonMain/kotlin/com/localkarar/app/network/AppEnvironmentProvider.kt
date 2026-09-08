package com.localkarar.app.network

/**
 * Platform-specific environment provider.
 * Resolved per platform via expect/actual.
 */
expect object AppEnvironmentProvider {
    val baseUrl: String

    /**
     * Release derlemesi mi?
     *
     * `baseUrl` zaten bu ayrimi yapiyordu ama disariya yalniz sonucu veriyordu;
     * gunlukleme de ayni ayrimi gerektirdigi icin bayragin kendisi aciliyor.
     * Iki ayri yerde "release mi" diye sormak, birinin gozden kacmasi demek.
     */
    val isRelease: Boolean

    /**
     * Uygulama surumu, "1.0.0 (1)" bicimi.
     *
     * 🔴 HAKKINDA EKRANI SURUM GOSTERMIYORDU. Mockup "Ayar 8"in ikinci
     * satiri bu; destek talebinde de ilk sorulan sey. Sabit yazilmadi —
     * `BuildConfig` uzerinden GERCEK degeri okuyor, yoksa surum artinca
     * ekran sessizce eskir.
     */
    val versionLabel: String
}
