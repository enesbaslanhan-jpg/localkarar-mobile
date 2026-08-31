package com.localkarar.app.auth

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.localkarar.app.network.ApiError
import kotlinx.coroutines.withTimeoutOrNull

sealed class SessionState {
    object CheckingSession : SessionState()
    object Unauthenticated : SessionState()
    data class Authenticated(val user: UserDto) : SessionState()
}

class AuthRepository(
    private val httpClient: HttpClient,
    private val secureStorage: SecureStorage,
    private val beforeExplicitLogout: suspend () -> Unit = {},
    private val onSessionCleared: () -> Unit = {}
) {
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.CheckingSession)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    /**
     * Restores session on app startup.
     *
     * Rules:
     *  - No token stored → Unauthenticated (no fake demo users or mock tokens)
     *  - Token present + GET /auth/me returns 200 → Authenticated
     *  - Token present + 401/403 (invalid/expired) → clear token → Unauthenticated
     *  - Token present + network error / timeout → keep token → Unauthenticated (can retry next time)
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
                    expireSession()
                }
                else -> {
                    // Recoverable server error, keep token in secure storage
                    _sessionState.value = SessionState.Unauthenticated
                }
            }
        } catch (e: ApiError.Unauthorized) {
            expireSession()
        } catch (e: ApiError.NetworkUnavailable) {
            // Offline: Preserve token so user doesn't lose credentials when offline
            _sessionState.value = SessionState.Unauthenticated
        } catch (e: ApiError.Timeout) {
            _sessionState.value = SessionState.Unauthenticated
        } catch (e: Exception) {
            _sessionState.value = SessionState.Unauthenticated
        }
    }

    suspend fun login(request: LoginRequest): Result<UserDto> {
        return try {
            val response = httpClient.post("/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status == HttpStatusCode.Unauthorized) {
                return Result.failure(Exception("E-posta adresi veya şifre hatalı."))
            }
            if (response.status == HttpStatusCode.UnprocessableEntity) {
                return Result.failure(Exception("Geçersiz giriş bilgileri."))
            }

            val loginResponse = response.body<LoginResponse>()
            secureStorage.saveToken(loginResponse.token)
            loginResponse.refreshToken?.let { secureStorage.saveRefreshToken(it) }
            _sessionState.value = SessionState.Authenticated(loginResponse.user)
            Result.success(loginResponse.user)
        } catch (e: ApiError.Unauthorized) {
            Result.failure(Exception("E-posta adresi veya şifre hatalı."))
        } catch (e: ApiError.NetworkUnavailable) {
            Result.failure(Exception("İnternet bağlantısı kurulamadı. Lütfen bağlantınızı kontrol edin."))
        } catch (e: ApiError.Timeout) {
            Result.failure(Exception("Sunucu yanıt vermedi. Lütfen daha sonra tekrar deneyin."))
        } catch (e: Exception) {
            val msg = e.message ?: "Giriş yapılırken bir hata oluştu."
            Result.failure(Exception(if (msg.contains("401")) "E-posta adresi veya şifre hatalı." else msg))
        }
    }

    suspend fun register(request: RegisterRequest): Result<UserDto> {
        return try {
            val response = httpClient.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status == HttpStatusCode.BadRequest) {
                return Result.failure(Exception("Bu e-posta adresi zaten kullanımda."))
            }
            if (response.status == HttpStatusCode.Forbidden) {
                return Result.failure(Exception("Kayıtlar şu an kapalı veya davetiyelidir."))
            }
            if (response.status == HttpStatusCode.UnprocessableEntity) {
                return Result.failure(Exception("Kayıt bilgileri geçersiz. Lütfen alanları kontrol edin."))
            }

            val registerResponse = response.body<LoginResponse>()
            secureStorage.saveToken(registerResponse.token)
            registerResponse.refreshToken?.let { secureStorage.saveRefreshToken(it) }
            _sessionState.value = SessionState.Authenticated(registerResponse.user)
            Result.success(registerResponse.user)
        } catch (e: ApiError.NetworkUnavailable) {
            Result.failure(Exception("İnternet bağlantısı kurulamadı. Lütfen bağlantınızı kontrol edin."))
        } catch (e: ApiError.Timeout) {
            Result.failure(Exception("Sunucu yanıt vermedi. Lütfen daha sonra tekrar deneyin."))
        } catch (e: Exception) {
            val msg = e.message ?: "Kayıt işlemi sırasında bir hata oluştu."
            Result.failure(Exception(if (msg.contains("already in use") || msg.contains("400")) "Bu e-posta adresi zaten kullanımda." else msg))
        }
    }

    suspend fun requestPasswordReset(email: String): Result<Boolean> {
        return try {
            val response = httpClient.post("/auth/password-reset/request") {
                contentType(ContentType.Application.Json)
                setBody(PasswordResetRequest(email = email.trim().lowercase()))
            }
            Result.success(response.status.isSuccess())
        } catch (e: ApiError.NetworkUnavailable) {
            Result.failure(Exception("İnternet bağlantısı kurulamadı."))
        } catch (e: Exception) {
            Result.success(true) // Anti-enumeration: always report success on client
        }
    }

    suspend fun confirmPasswordReset(token: String, newPassword: String): Result<UserDto> {
        return try {
            val response = httpClient.post("/auth/password-reset/confirm") {
                contentType(ContentType.Application.Json)
                setBody(PasswordResetConfirmRequest(token = token.trim(), newPassword = newPassword))
            }

            if (response.status == HttpStatusCode.BadRequest) {
                return Result.failure(Exception("Sıfırlama bağlantısı geçersiz ya da süresi dolmuş."))
            }

            val authResponse = response.body<LoginResponse>()
            secureStorage.saveToken(authResponse.token)
            authResponse.refreshToken?.let { secureStorage.saveRefreshToken(it) }
            _sessionState.value = SessionState.Authenticated(authResponse.user)
            Result.success(authResponse.user)
        } catch (e: ApiError.NetworkUnavailable) {
            Result.failure(Exception("İnternet bağlantısı kurulamadı."))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Şifre sıfırlama işlemi başarısız oldu."))
        }
    }

    suspend fun requestEmailVerification(): Result<Boolean> {
        return try {
            val response = httpClient.post("/auth/email/verify-request")
            Result.success(response.status.isSuccess())
        } catch (e: Exception) {
            Result.failure(Exception("Doğrulama kodu gönderilemedi."))
        }
    }

    suspend fun confirmEmailVerification(code: String): Result<Boolean> {
        return try {
            val response = httpClient.post("/auth/email/verify-confirm") {
                contentType(ContentType.Application.Json)
                setBody(EmailVerifyConfirmRequest(code = code.trim()))
            }
            if (response.status.isSuccess()) {
                // Refresh user
                val meResponse = httpClient.get("/auth/me")
                if (meResponse.status == HttpStatusCode.OK) {
                    _sessionState.value = SessionState.Authenticated(meResponse.body())
                }
                Result.success(true)
            } else {
                Result.failure(Exception("Geçersiz doğrulama kodu."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "E-posta doğrulama başarısız oldu."))
        }
    }

    suspend fun serverLogout() {
        val refreshToken = secureStorage.readRefreshToken()
        try {
            // Device ownership must be revoked while the authenticated Ktor client
            // can still load the JWT. Failure or timeout never blocks local logout.
            try {
                withTimeoutOrNull(3_000L) { beforeExplicitLogout() }
            } catch (_: Exception) {
                // Best effort: local logout continues below.
            }
            try {
                withTimeoutOrNull(3_000L) {
                    if (!refreshToken.isNullOrBlank()) {
                        httpClient.post("/auth/logout") {
                            contentType(ContentType.Application.Json)
                            setBody(RefreshTokenRequest(refreshToken))
                        }
                    }
                }
            } catch (_: Exception) {
                // Best effort: local logout continues below.
            }
        } finally {
            logout()
        }
    }

    fun logout() {
        secureStorage.clearAll()
        onSessionCleared()
        _sessionState.value = SessionState.Unauthenticated
    }

    fun expireSession() = logout()

    fun applyNewSession(token: String, user: UserDto, refreshToken: String? = null) {
        if (token.isNotBlank()) {
            secureStorage.saveToken(token)
        }
        if (!refreshToken.isNullOrBlank()) {
            secureStorage.saveRefreshToken(refreshToken)
        }
        _sessionState.value = SessionState.Authenticated(user)
    }

    fun updateUser(user: UserDto) {
        _sessionState.value = SessionState.Authenticated(user)
    }
}
