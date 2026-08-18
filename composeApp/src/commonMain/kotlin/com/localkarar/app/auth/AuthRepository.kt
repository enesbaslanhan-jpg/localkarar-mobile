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

    suspend fun restoreSession() {
        val token = secureStorage.readToken()
        if (token.isNullOrBlank()) {
            _sessionState.value = SessionState.Unauthenticated
            return
        }

        try {
            // Verify token using /auth/me
            val response = httpClient.get("/auth/me")
            if (response.status == HttpStatusCode.OK) {
                val user = response.body<UserDto>()
                _sessionState.value = SessionState.Authenticated(user)
            } else {
                logout()
            }
        } catch (e: Exception) {
            // If offline, might want to assume unauthenticated for now or keep authenticated.
            // According to spec: If valid -> Authenticated, if invalid/expired -> Unauthenticated.
            // Network errors might cause this to fail, but for now we clear token on any exception to be safe as requested.
            logout()
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
