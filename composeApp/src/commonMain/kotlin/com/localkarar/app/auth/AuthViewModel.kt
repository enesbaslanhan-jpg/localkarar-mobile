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

    /**
     * Kayit.
     *
     * @param onBasarili yalnizca SUNUCU kaydi kabul ettiginde cagrilir.
     *   Karsilama ekranini tetikliyor; dogrulama hatasinda ya da 4xx/5xx
     *   yanitinda cagrilmamali, yoksa kaydolamamis kullaniciya "hos
     *   geldin" denirdi.
     */
    fun register(
        name: String,
        email: String,
        password: String,
        legalAccepted: Boolean,
        onBasarili: () -> Unit = {}
    ) {
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
        if (password.length < 10) {
            _registerError.value = "Şifre en az 10 karakter olmalıdır."
            return
        }
        if (!legalAccepted) {
            _registerError.value = "Devam etmek için kullanım koşullarını onaylamalısınız."
            return
        }

        _isLoading.value = true
        _registerError.value = null

        viewModelScope.launch {
            val request = RegisterRequest(
                name = trimmedName,
                email = trimmedEmail,
                password = password,
                acceptedLegal = legalAccepted
            )
            val result = authRepository.register(request)

            if (result.isFailure) {
                val exception = result.exceptionOrNull()
                _registerError.value = when (exception) {
                    is ApiError -> exception.message
                    else -> exception?.message ?: "Kayıt işlemi başarısız oldu."
                }
            } else {
                onBasarili()
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

    /*
     * E-POSTA DOGRULAMA.
     *
     * 🔴 UCLAR VARDI, EKRAN YOKTU. `AuthRepository.requestEmailVerification`
     * ve `confirmEmailVerification` yaziliydi ama commonMain'de HICBIR YER
     * cagirmiyordu; kullanici mobilde adresini hicbir zaman dogrulayamiyordu.
     *
     * Sunucu BAGLANTI degil 6 HANELI KOD gonderiyor (`auth.ts`: "Bağlantı
     * yerine kod: mobil istemcide derin bağlantı kurmaya gerek kalmıyor").
     * Mockup'taki "bağlantıya dokun" metni bu yuzden koda cevrildi —
     * bilgi mimarisi sunucudan, gorsel dil mockup'tan.
     */
    private val _verifyError = MutableStateFlow<String?>(null)
    val verifyError: StateFlow<String?> = _verifyError.asStateFlow()

    private val _verifySent = MutableStateFlow(false)
    val verifySent: StateFlow<Boolean> = _verifySent.asStateFlow()

    private val _verifyDone = MutableStateFlow(false)
    val verifyDone: StateFlow<Boolean> = _verifyDone.asStateFlow()

    fun sendEmailVerification() {
        _isLoading.value = true
        _verifyError.value = null
        viewModelScope.launch {
            val result = authRepository.requestEmailVerification()
            if (result.isSuccess) {
                _verifySent.value = true
            } else {
                _verifyError.value = result.exceptionOrNull()?.message
                    ?: "Doğrulama kodu gönderilemedi."
            }
            _isLoading.value = false
        }
    }

    fun confirmEmailVerification(code: String) {
        val temiz = code.trim()
        if (temiz.length != 6 || temiz.any { !it.isDigit() }) {
            _verifyError.value = "Kod 6 haneli olmalı."
            return
        }
        _isLoading.value = true
        _verifyError.value = null
        viewModelScope.launch {
            val result = authRepository.confirmEmailVerification(temiz)
            if (result.isSuccess) {
                _verifyDone.value = true
            } else {
                _verifyError.value = result.exceptionOrNull()?.message ?: "Kod doğrulanamadı."
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
