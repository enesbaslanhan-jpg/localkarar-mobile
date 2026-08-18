package com.localkarar.app.network

/**
 * API environment configuration.
 *
 * Environment is resolved at build time on Android via BuildConfig.IS_RELEASE.
 * On other platforms it defaults to Development.
 *
 * Production API URL:
 *   Must be set to the real backend before shipping. Currently documented as a
 *   required configuration point. Debug builds always use the local emulator URL.
 *
 * SECURITY RULE:
 *   Debug cleartext http://10.0.2.2:3000 is ONLY permitted in debug builds.
 *   The Android network_security_config.xml enforces this at the OS level.
 *   Release builds must use HTTPS.
 */
object ApiConfig {
    /**
     * Returns the base URL for the current build environment.
     *
     * Android: resolved from BuildConfig.IS_RELEASE (set in build.gradle.kts).
     * Other platforms: always Development until platform-specific overrides are added.
     *
     * To configure the production URL: set PRODUCTION_API_URL below.
     * Do NOT set it to a localhost/emulator address.
     */
    val baseUrl: String get() = AppEnvironmentProvider.baseUrl
}

// Production URL placeholder — must be replaced with the real backend before release.
// Leaving this as a clearly identifiable constant makes it impossible to miss in CI/CD review.
const val PRODUCTION_API_URL = "https://api.localkarar.com"

// Development URL — Android emulator loopback to Windows host.
const val DEVELOPMENT_API_URL = "http://10.0.2.2:3000"
