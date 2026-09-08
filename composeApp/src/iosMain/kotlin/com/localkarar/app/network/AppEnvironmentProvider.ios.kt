package com.localkarar.app.network

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

/**
 * iOS environment provider.
 *
 * Uses kotlin.native.Platform.isDebugBinary to select:
 *   - Debug (Simulator / Local) -> http://localhost:3000
 *   - Release (Production)      -> https://localkarar.com
 */
actual object AppEnvironmentProvider {
    @OptIn(ExperimentalNativeApi::class)
    actual val baseUrl: String
        get() = if (Platform.isDebugBinary) IOS_DEV_API_URL else PRODUCTION_API_URL

    @OptIn(ExperimentalNativeApi::class)
    actual val isRelease: Boolean
        get() = !Platform.isDebugBinary

    /*
     * iOS surumu `Info.plist`ten okunuyor: CFBundleShortVersionString +
     * CFBundleVersion. Android`daki VERSION_NAME / VERSION_CODE ciftinin
     * karsiligi.
     */
    actual val versionLabel: String
        get() {
            val bundle = platform.Foundation.NSBundle.mainBundle
            val ad = bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
            val kod = bundle.objectForInfoDictionaryKey("CFBundleVersion") as? String
            return listOfNotNull(ad, kod?.let { "($it)" }).joinToString(" ").ifBlank { "—" }
        }
}
