package com.localkarar.app.network

enum class AppEnvironment {
    Development,
    Production
}

object ApiConfig {
    var environment: AppEnvironment = AppEnvironment.Development

    val baseUrl: String
        get() = when (environment) {
            // Use 10.0.2.2 for Android emulator to access Windows host localhost
            AppEnvironment.Development -> "http://10.0.2.2:3000"
            // Placeholder for production
            AppEnvironment.Production -> "https://api.localkarar.com"
        }
}
