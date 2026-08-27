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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.restoreSession()
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginError.value = "E-posta ve şifre boş bırakılamaz."
            return
        }

        _isLoading.value = true
        _loginError.value = null

        viewModelScope.launch {
            val request = LoginRequest(email.trim(), password.trim())
            val result = authRepository.login(request)
            
            if (result.isFailure) {
                val exception = result.exceptionOrNull()
                _loginError.value = when (exception) {
                    is ApiError -> exception.message
                    else -> "Giriş yapılamadı. Bağlantınızı kontrol edin."
                }
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
