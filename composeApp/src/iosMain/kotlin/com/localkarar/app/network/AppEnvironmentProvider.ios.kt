package com.localkarar.app.network

/**
 * iOS environment provider.
 * TODO: Replace with a proper build-time mechanism (xcconfig or conditional compilation)
 * before iOS release. Currently defaults to Development.
 */
actual object AppEnvironmentProvider {
    actual val baseUrl: String
        get() = DEVELOPMENT_API_URL
}
