package com.localkarar.app.network

/**
 * Platform-specific environment provider.
 * Resolved per platform via expect/actual.
 */
expect object AppEnvironmentProvider {
    val baseUrl: String
}
