package com.localkarar.app.network

/**
 * API environment configuration for Android, iOS and Production.
 *
 * Semantic states:
 *  - DEBUG_ANDROID → http://10.0.2.2:3000 (Android emulator host loopback)
 *  - DEBUG_IOS     → http://localhost:3000 (iOS simulator host loopback)
 *  - PRODUCTION    → https://api.localkarar.com (Real HTTPS backend)
 */
const val PRODUCTION_API_URL = "https://api.localkarar.com"
const val ANDROID_DEV_API_URL = "http://10.0.2.2:3000"
const val IOS_DEV_API_URL = "http://localhost:3000"
const val DEVELOPMENT_API_URL = ANDROID_DEV_API_URL

object ApiConfig {
    val baseUrl: String get() = AppEnvironmentProvider.baseUrl
}
