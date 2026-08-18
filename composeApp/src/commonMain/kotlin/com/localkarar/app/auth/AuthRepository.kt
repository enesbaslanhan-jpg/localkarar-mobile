package com.localkarar.app.auth

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.localkarar.app.network.ApiError

sealed class SessionState {
    object CheckingSession : SessionState()
    object Unauthenticated : SessionState()
    data class Authenticated(val user: UserDto) : SessionState()
}

class AuthRepository(
    private val httpClient: HttpClient,
    private val secureStorage: SecureStorage
) {
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.CheckingSession)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    /**
     * Restores session on app start.
     *
     * Semantics:
     *  - No token stored → Unauthenticated (never had a session)
     *  - Token present + GET /auth/me returns 200 → Authenticated
     *  - Token present + 401/403 (invalid/expired token) → clear token → Unauthenticated
     *  - Token present + network error / timeout / backend unreachable → keep token →
     *    Unauthenticated with preserved token so retry on next launch is possible.
     *    The user can log in again; the token is NOT wiped just because the network is down.
     *
     * This prevents offline startup from silently destroying valid sessions.
     */
    suspend fun restoreSession() {
        val token = secureStorage.readToken()
        if (token.isNullOrBlank()) {
            _sessionState.value = SessionState.Unauthenticated
            return
        }

        try {
            val response = httpClient.get("/auth/me")
            when (response.status) {
                HttpStatusCode.OK -> {
                    val user = response.body<UserDto>()
                    _sessionState.value = SessionState.Authenticated(user)
                }
                HttpStatusCode.Unauthorized,
                HttpStatusCode.Forbidden -> {
                    // Explicit auth failure — token is invalid/expired. Wipe it.
                    secureStorage.clearToken()
                    _sessionState.value = SessionState.Unauthenticated
                }
                else -> {
                    // Unexpected server error — do NOT wipe token; treat as recoverable.
                    _sessionState.value = SessionState.Unauthenticated
                }
            }
        } catch (e: ApiError.Unauthorized) {
            // Thrown by HttpResponseValidator on 401 — explicit auth rejection.
            secureStorage.clearToken()
            _sessionState.value = SessionState.Unauthenticated
        } catch (e: ApiError.NetworkUnavailable) {
            // Offline — DO NOT erase the token. User can retry next launch.
            _sessionState.value = SessionState.Unauthenticated
        } catch (e: ApiError.Timeout) {
            // Backend unreachable — same as offline. Preserve token.
            _sessionState.value = SessionState.Unauthenticated
        } catch (e: Exception) {
            // Any other unexpected error (serialization, etc.) — preserve token,
            // but mark as unauthenticated so the user can attempt login again.
            _sessionState.value = SessionState.Unauthenticated
        }
    }

    suspend fun login(request: LoginRequest): Result<UserDto> {
        return try {
            val response = httpClient.post("/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            val loginResponse = response.body<LoginResponse>()
            secureStorage.saveToken(loginResponse.token)
            _sessionState.value = SessionState.Authenticated(loginResponse.user)
            Result.success(loginResponse.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        secureStorage.clearToken()
        _sessionState.value = SessionState.Unauthenticated
    }
}
