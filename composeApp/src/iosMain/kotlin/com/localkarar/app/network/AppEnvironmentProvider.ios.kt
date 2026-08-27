package com.localkarar.app.network

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

/**
 * iOS environment provider.
 *
 * Uses kotlin.native.Platform.isDebugBinary to select:
 *   - Debug (Simulator / Local) → http://localhost:3000
 *   - Release (Production)      → https://api.localkarar.com
 */
actual object AppEnvironmentProvider {
    @OptIn(ExperimentalNativeApi::class)
    actual val baseUrl: String
        get() = if (Platform.isDebugBinary) IOS_DEV_API_URL else PRODUCTION_API_URL
}
