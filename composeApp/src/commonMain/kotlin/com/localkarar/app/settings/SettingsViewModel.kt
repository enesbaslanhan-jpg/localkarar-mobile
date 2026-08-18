package com.localkarar.app.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.auth.UserDto
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    var user by mutableStateOf<UserDto?>(null)
        private set
    var loading by mutableStateOf(false)
        private set
    var notice by mutableStateOf<String?>(null)
        private set
    var noticeIsError by mutableStateOf(false)
        private set

    var passwordCurrent by mutableStateOf("")
        private set
    var passwordNew by mutableStateOf("")
        private set
    var passwordConfirm by mutableStateOf("")
        private set
    var passwordLoading by mutableStateOf(false)
        private set

    var emailNew by mutableStateOf("")
        private set
    var emailCurrentPassword by mutableStateOf("")
        private set
    var emailLoading by mutableStateOf(false)
        private set

    var deletePassword by mutableStateOf("")
        private set
    var deleteConfirmation by mutableStateOf("")
        private set
    var deleteLoading by mutableStateOf(false)
        private set

    var avatarLoading by mutableStateOf(false)
        private set

    fun refresh(onNewSession: ((String, UserDto) -> Unit)? = null) {
        loading = true
        viewModelScope.launch {
            repository.getMe().onSuccess {
                user = it
                onNewSession?.invoke("", it)
            }.onFailure { e ->
                setNotice(e.message ?: "Profil yüklenemedi", isError = true)
            }
            loading = false
        }
    }

    fun onPasswordCurrentChange(value: String) {
        passwordCurrent = value
    }

    fun onPasswordNewChange(value: String) {
        passwordNew = value
    }

    fun onPasswordConfirmChange(value: String) {
        passwordConfirm = value
    }

    fun changePassword() {
        if (passwordNew.length < 8) {
            setNotice("Yeni şifre en az 8 karakter olmalı", isError = true)
            return
        }
        if (passwordNew != passwordConfirm) {
            setNotice("Şifreler eşleşmiyor", isError = true)
            return
        }
        passwordLoading = true
        viewModelScope.launch {
            repository.changePassword(passwordCurrent, passwordNew).onSuccess {
                passwordCurrent = ""
                passwordNew = ""
                passwordConfirm = ""
                setNotice("Şifre güncellendi")
            }.onFailure { e ->
                setNotice(e.message ?: "Şifre değiştirilemedi", isError = true)
            }
            passwordLoading = false
        }
    }

    fun onEmailNewChange(value: String) {
        emailNew = value
    }

    fun onEmailCurrentPasswordChange(value: String) {
        emailCurrentPassword = value
    }

    fun changeEmail(onNewSession: (String, UserDto) -> Unit) {
        if (!emailNew.contains("@")) {
            setNotice("Geçerli bir e-posta girin", isError = true)
            return
        }
        emailLoading = true
        viewModelScope.launch {
            repository.changeEmail(emailNew.trim().lowercase(), emailCurrentPassword).onSuccess { response ->
                emailNew = ""
                emailCurrentPassword = ""
                setNotice("E-posta güncellendi")
                onNewSession(response.token, response.user)
            }.onFailure { e ->
                setNotice(e.message ?: "E-posta değiştirilemedi", isError = true)
            }
            emailLoading = false
        }
    }

    fun onDeletePasswordChange(value: String) {
        deletePassword = value
    }

    fun onDeleteConfirmationChange(value: String) {
        deleteConfirmation = value
    }

    fun deleteAccount(onDeleted: () -> Unit) {
        if (deleteConfirmation != "HESABIMI SİL") {
            setNotice("Onay metnini doğru yazın", isError = true)
            return
        }
        deleteLoading = true
        viewModelScope.launch {
            repository.deleteAccount(deletePassword).onSuccess {
                onDeleted()
            }.onFailure { e ->
                setNotice(e.message ?: "Hesap silinemedi", isError = true)
                deleteLoading = false
            }
        }
    }

    fun uploadAvatar(name: String, bytes: ByteArray, onNewSession: (String, UserDto) -> Unit) {
        avatarLoading = true
        viewModelScope.launch {
            repository.uploadAvatar(name, bytes).onSuccess {
                setNotice("Profil fotoğrafı güncellendi")
                refresh(onNewSession = onNewSession)
            }.onFailure { e ->
                setNotice(e.message ?: "Fotoğraf yüklenemedi", isError = true)
                avatarLoading = false
            }
        }
    }

    fun removeAvatar() {
        avatarLoading = true
        viewModelScope.launch {
            repository.deleteAvatar().onSuccess {
                setNotice("Profil fotoğrafı kaldırıldı")
                refresh()
            }.onFailure { e ->
                setNotice(e.message ?: "Fotoğraf kaldırılamadı", isError = true)
                avatarLoading = false
            }
        }
    }

    fun setNotice(message: String, isError: Boolean = false) {
        notice = message
        noticeIsError = isError
    }

    fun clearNotice() {
        notice = null
    }
}