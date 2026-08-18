package com.localkarar.app.network

import com.localkarar.app.BuildConfig

/**
 * Android-specific environment provider.
 *
 * Uses BuildConfig.IS_RELEASE (set in build.gradle.kts per buildType) to select:
 *   - Debug   → http://10.0.2.2:3000  (Android emulator loopback to host machine)
 *   - Release → https://api.localkarar.com (production HTTPS — cleartext NOT permitted)
 *
 * The release cleartext exception in network_security_config.xml covers 10.0.2.2 ONLY,
 * and only because the domain-config block is scoped to that specific domain.
 * Release builds never resolve to 10.0.2.2.
 */
actual object AppEnvironmentProvider {
    actual val baseUrl: String
        get() = if (BuildConfig.IS_RELEASE) PRODUCTION_API_URL else DEVELOPMENT_API_URL
}
