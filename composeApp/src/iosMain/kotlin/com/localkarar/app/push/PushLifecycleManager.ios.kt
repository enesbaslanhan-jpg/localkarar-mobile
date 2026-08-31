package com.localkarar.app.push

import androidx.compose.runtime.Composable
import io.ktor.client.HttpClient

actual class PushLifecycleManager actual constructor(
    @Suppress("UNUSED_PARAMETER") httpClient: HttpClient
) {
    @Composable
    actual fun BindAuthenticatedSession(userId: Int) = Unit

    actual suspend fun unregisterBeforeLogout() = Unit

    actual fun clearSession() = Unit
}

