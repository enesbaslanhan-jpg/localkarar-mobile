package com.localkarar.app.push

import androidx.compose.runtime.Composable
import io.ktor.client.HttpClient

expect class PushLifecycleManager(httpClient: HttpClient) {
    @Composable
    fun BindAuthenticatedSession(userId: Int)

    suspend fun unregisterBeforeLogout()

    fun clearSession()
}

