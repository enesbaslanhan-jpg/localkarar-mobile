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
}
