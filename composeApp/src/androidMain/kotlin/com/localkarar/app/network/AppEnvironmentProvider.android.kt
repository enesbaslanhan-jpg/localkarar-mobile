package com.localkarar.app.network

import com.localkarar.app.BuildConfig

/**
 * Android-specific environment provider.
 *
 * Uses BuildConfig.IS_RELEASE (set in build.gradle.kts per buildType) to select:
 *   - Debug   -> http://10.0.2.2:3000  (Android emulator loopback to host machine)
 *   - Release -> https://localkarar.com (production HTTPS, cleartext NOT permitted)
 */
actual object AppEnvironmentProvider {
    actual val baseUrl: String
        get() = if (BuildConfig.IS_RELEASE) PRODUCTION_API_URL else ANDROID_DEV_API_URL

    actual val isRelease: Boolean
        get() = BuildConfig.IS_RELEASE
}
