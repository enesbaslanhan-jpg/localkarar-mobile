package com.localkarar.app.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.localkarar.app.network.ApiError

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    val sessionState: StateFlow<SessionState> = authRepository.sessionState

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _registerError = MutableStateFlow<String?>(null)
    val registerError: StateFlow<String?> = _registerError.asStateFlow()

    private val _resetError = MutableStateFlow<String?>(null)
    val resetError: StateFlow<String?> = _resetError.asStateFlow()

    private val _resetSuccess = MutableStateFlow(false)
    val resetSuccess: StateFlow<Boolean> = _resetSuccess.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.restoreSession()
        }
    }

    fun clearErrors() {
        _loginError.value = null
        _registerError.value = null
        _resetError.value = null
    }

    fun login(email: String, password: String) {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        if (trimmedEmail.isBlank() || trimmedPassword.isBlank()) {
            _loginError.value = "E-posta ve şifre boş bırakılamaz."
            return
        }

        _isLoading.value = true
        _loginError.value = null

        viewModelScope.launch {
            val request = LoginRequest(trimmedEmail, trimmedPassword)
            val result = authRepository.login(request)
            
            if (result.isFailure) {
                val exception = result.exceptionOrNull()
                _loginError.value = when (exception) {
                    is ApiError -> exception.message
                    else -> exception?.message ?: "Giriş yapılamadı. Bilgilerinizi kontrol edin."
                }
            }
            _isLoading.value = false
        }
    }

    fun register(name: String, email: String, password: String, legalAccepted: Boolean) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()

        if (trimmedName.length < 2) {
            _registerError.value = "Lütfen adınızı ve soyadınızı girin."
            return
        }
        if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            _registerError.value = "Geçerli bir e-posta adresi girin."
            return
        }
        if (password.length < 8) {
            _registerError.value = "Şifre en az 8 karakter olmalıdır."
            return
        }
        if (!legalAccepted) {
            _registerError.value = "Devam etmek için kullanım koşullarını onaylamalısınız."
            return
        }

        _isLoading.value = true
        _registerError.value = null

        viewModelScope.launch {
            val request = RegisterRequest(name = trimmedName, email = trimmedEmail, password = password)
            val result = authRepository.register(request)

            if (result.isFailure) {
                val exception = result.exceptionOrNull()
                _registerError.value = when (exception) {
                    is ApiError -> exception.message
                    else -> exception?.message ?: "Kayıt işlemi başarısız oldu."
                }
            }
            _isLoading.value = false
        }
    }

    fun requestPasswordReset(email: String) {
        val trimmedEmail = email.trim()
        if (!trimmedEmail.contains("@")) {
            _resetError.value = "Lütfen geçerli bir e-posta adresi girin."
            return
        }

        _isLoading.value = true
        _resetError.value = null
        _resetSuccess.value = false

        viewModelScope.launch {
            val result = authRepository.requestPasswordReset(trimmedEmail)
            if (result.isSuccess) {
                _resetSuccess.value = true
            } else {
                _resetError.value = result.exceptionOrNull()?.message ?: "İstek iletilemedi. Lütfen tekrar deneyin."
            }
            _isLoading.value = false
        }
    }

    fun confirmPasswordReset(token: String, newPassword: String) {
        if (token.isBlank()) {
            _resetError.value = "Geçersiz sıfırlama kodu."
            return
        }
        if (newPassword.length < 8) {
            _resetError.value = "Yeni şifre en az 8 karakter olmalıdır."
            return
        }

        _isLoading.value = true
        _resetError.value = null

        viewModelScope.launch {
            val result = authRepository.confirmPasswordReset(token, newPassword)
            if (result.isFailure) {
                _resetError.value = result.exceptionOrNull()?.message ?: "Şifre sıfırlanamadı. Bağlantı süresi dolmuş olabilir."
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
