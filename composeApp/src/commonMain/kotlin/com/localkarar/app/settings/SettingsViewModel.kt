package com.localkarar.app.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.auth.ConsentItemDto
import com.localkarar.app.auth.LegalDocumentDto
import com.localkarar.app.auth.MissingConsentDto
import com.localkarar.app.auth.UserDto
import kotlinx.coroutines.launch

fun roleLabel(role: String?): String {
    return when (role?.lowercase()) {
        "admin" -> "Yönetici"
        "content_editor" -> "İçerik Editörü"
        "subject_expert" -> "Konu Uzmanı"
        "learner", "student", "member" -> "Üye"
        else -> role?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } ?: "Üye"
    }
}

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

    // Display Name editing
    var editName by mutableStateOf("")
        private set
    var nameLoading by mutableStateOf(false)
        private set

    // Password change
    var passwordCurrent by mutableStateOf("")
        private set
    var passwordNew by mutableStateOf("")
        private set
    var passwordConfirm by mutableStateOf("")
        private set
    var passwordLoading by mutableStateOf(false)
        private set

    // Email change
    var emailNew by mutableStateOf("")
        private set
    var emailCurrentPassword by mutableStateOf("")
        private set
    var emailLoading by mutableStateOf(false)
        private set

    // Account delete
    var deletePassword by mutableStateOf("")
        private set
    var deleteConfirmation by mutableStateOf("")
        private set
    var deleteLoading by mutableStateOf(false)
        private set

    // Avatar upload/delete
    var avatarLoading by mutableStateOf(false)
        private set

    // Logout all
    var logoutAllLoading by mutableStateOf(false)
        private set

    // Legal & Consents
    var legalDocuments by mutableStateOf<List<LegalDocumentDto>>(emptyList())
        private set
    var acceptedConsents by mutableStateOf<List<ConsentItemDto>>(emptyList())
        private set
    var missingConsents by mutableStateOf<List<MissingConsentDto>>(emptyList())
        private set
    var consentsLoading by mutableStateOf(false)
        private set

    fun refresh(onNewSession: ((String, UserDto) -> Unit)? = null) {
        loading = true
        viewModelScope.launch {
            repository.getMe().onSuccess { userDto ->
                user = userDto
                editName = userDto.name
                onNewSession?.invoke("", userDto)
            }.onFailure { e ->
                setNotice(e.message ?: "Profil yüklenemedi", isError = true)
            }
            loading = false
        }
    }

    fun onEditNameChange(value: String) {
        editName = value
    }

    fun updateDisplayName(onUserUpdated: (UserDto) -> Unit) {
        val trimmed = editName.trim()
        if (trimmed.length < 2) {
            setNotice("İsim en az 2 karakter olmalıdır.", isError = true)
            return
        }
        nameLoading = true
        viewModelScope.launch {
            repository.updateProfile(trimmed).onSuccess { updated ->
                user = user?.copy(name = updated.name) ?: UserDto(
                    id = updated.id,
                    email = "",
                    name = updated.name,
                    role = "learner"
                )
                user?.let { onUserUpdated(it) }
                setNotice("Profil ismi güncellendi.")
            }.onFailure { e ->
                setNotice(e.message ?: "Profil güncellenemedi.", isError = true)
            }
            nameLoading = false
        }
    }

    /*
     * MOCKUP "AYAR 2" ALANLARI.
     *
     * 🔴 EKRAN YALNIZ ADI DUZENLIYORDU. Sunucu uc alan daha kabul ediyor
     * (`auth.ts` `profilSemasi`: bio, location, websiteUrl) ve `UserDto`
     * ucunu de tasiyor; mobilde girilecek yer yoktu.
     *
     * ⚠️ "Kullanıcı adı" mockup'ta var ama SUNUCUDA YOK — kullanicilarin
     * `username` alani bulunmuyor. Uydurma bir alan koymak, kaydedilmeyen
     * bir kutu gostermek olurdu.
     */
    var editBio by mutableStateOf("")
    var editLocation by mutableStateOf("")
    var editWebsite by mutableStateOf("")
    var coverLoading by mutableStateOf(false)

    /** Alanlari acik olan kullanicinin mevcut degerleriyle doldurur. */
    fun profilAlanlariniHazirla() {
        val u = user ?: return
        editName = u.name
        editBio = u.bio.orEmpty()
        editLocation = u.location.orEmpty()
        editWebsite = u.websiteUrl.orEmpty()
    }

    fun onEditBioChange(value: String) { editBio = value }
    fun onEditLocationChange(value: String) { editLocation = value }
    fun onEditWebsiteChange(value: String) { editWebsite = value }

    fun saveProfile(onUserUpdated: (UserDto) -> Unit) {
        val ad = editName.trim()
        if (ad.length < 2) {
            setNotice("İsim en az 2 karakter olmalıdır.", isError = true)
            return
        }
        val site = editWebsite.trim()
        if (site.isNotBlank() && !site.startsWith("http://") && !site.startsWith("https://")) {
            setNotice("Site adresi http:// veya https:// ile başlamalı.", isError = true)
            return
        }
        nameLoading = true
        viewModelScope.launch {
            repository.updateProfile(
                name = ad,
                bio = editBio.trim(),
                location = editLocation.trim(),
                websiteUrl = site
            ).onSuccess { updated ->
                user = user?.copy(
                    name = updated.name,
                    bio = updated.bio,
                    location = updated.location,
                    websiteUrl = updated.websiteUrl,
                    avatarUrl = updated.avatarUrl ?: user?.avatarUrl,
                    coverUrl = updated.coverUrl ?: user?.coverUrl
                )
                user?.let { onUserUpdated(it) }
                setNotice("Profil güncellendi.")
            }.onFailure { e ->
                setNotice(e.message ?: "Profil güncellenemedi.", isError = true)
            }
            nameLoading = false
        }
    }

    fun uploadCover(name: String, bytes: ByteArray, onNewSession: (String, UserDto) -> Unit) {
        coverLoading = true
        viewModelScope.launch {
            repository.uploadCover(name, bytes).onSuccess {
                setNotice("Kapak fotoğrafı yüklendi.")
                refresh(onNewSession = onNewSession)
            }.onFailure { e ->
                setNotice(e.message ?: "Kapak yüklenemedi.", isError = true)
            }
            coverLoading = false
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

    fun changePassword(onNewSession: ((String, UserDto) -> Unit)? = null) {
        if (passwordCurrent.isBlank()) {
            setNotice("Mevcut şifrenizi giriniz.", isError = true)
            return
        }
        if (passwordNew.length < 10) {
            setNotice("Yeni şifre en az 10 karakter olmalıdır.", isError = true)
            return
        }
        if (passwordNew != passwordConfirm) {
            setNotice("Şifreler eşleşmiyor.", isError = true)
            return
        }
        passwordLoading = true
        viewModelScope.launch {
            repository.changePassword(passwordCurrent, passwordNew).onSuccess { session ->
                passwordCurrent = ""
                passwordNew = ""
                passwordConfirm = ""
                setNotice("Şifreniz başarıyla güncellendi.")
                onNewSession?.invoke(session.token, session.user)
            }.onFailure { e ->
                setNotice(e.message ?: "Şifre değiştirilemedi.", isError = true)
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
        val trimmedEmail = emailNew.trim().lowercase()
        if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            setNotice("Geçerli bir e-posta adresi giriniz.", isError = true)
            return
        }
        if (emailCurrentPassword.isBlank()) {
            setNotice("Mevcut şifrenizi giriniz.", isError = true)
            return
        }
        emailLoading = true
        viewModelScope.launch {
            repository.changeEmail(trimmedEmail, emailCurrentPassword).onSuccess { session ->
                emailNew = ""
                emailCurrentPassword = ""
                setNotice("E-posta adresiniz başarıyla güncellendi.")
                user = session.user
                onNewSession(session.token, session.user)
            }.onFailure { e ->
                setNotice(e.message ?: "E-posta değiştirilemedi.", isError = true)
            }
            emailLoading = false
        }
    }

    fun logoutAll(onNewSession: ((String, UserDto) -> Unit)? = null) {
        logoutAllLoading = true
        viewModelScope.launch {
            repository.logoutAll().onSuccess { session ->
                setNotice("Diğer tüm cihazlardaki oturumlar kapatıldı.")
                user = session.user
                onNewSession?.invoke(session.token, session.user)
            }.onFailure { e ->
                setNotice(e.message ?: "Oturumlar kapatılamadı.", isError = true)
            }
            logoutAllLoading = false
        }
    }

    fun onDeletePasswordChange(value: String) {
        deletePassword = value
    }

    fun onDeleteConfirmationChange(value: String) {
        deleteConfirmation = value
    }

    fun deleteAccount(onDeleted: () -> Unit) {
        if (deletePassword.isBlank()) {
            setNotice("Mevcut şifrenizi giriniz.", isError = true)
            return
        }
        if (deleteConfirmation.trim() != "HESABIMI SİL") {
            setNotice("Lütfen 'HESABIMI SİL' yazarak onaylayın.", isError = true)
            return
        }
        deleteLoading = true
        viewModelScope.launch {
            repository.deleteAccount(deletePassword).onSuccess {
                onDeleted()
            }.onFailure { e ->
                setNotice(e.message ?: "Hesap silinemedi.", isError = true)
                deleteLoading = false
            }
        }
    }

    fun uploadAvatar(name: String, bytes: ByteArray, onNewSession: (String, UserDto) -> Unit) {
        avatarLoading = true
        viewModelScope.launch {
            repository.uploadAvatar(name, bytes).onSuccess {
                setNotice("Profil fotoğrafı başarıyla yüklendi.")
                refresh(onNewSession = onNewSession)
            }.onFailure { e ->
                setNotice(e.message ?: "Fotoğraf yüklenemedi.", isError = true)
                avatarLoading = false
            }
        }
    }

    fun removeAvatar(onNewSession: ((String, UserDto) -> Unit)? = null) {
        avatarLoading = true
        viewModelScope.launch {
            repository.deleteAvatar().onSuccess {
                setNotice("Profil fotoğrafı kaldırıldı.")
                refresh(onNewSession = onNewSession)
            }.onFailure { e ->
                setNotice(e.message ?: "Fotoğraf kaldırılamadı.", isError = true)
                avatarLoading = false
            }
        }
    }

    fun loadConsents() {
        consentsLoading = true
        viewModelScope.launch {
            repository.getLegalDocuments().onSuccess { docs ->
                legalDocuments = docs
            }
            repository.getConsents().onSuccess { consentsDto ->
                acceptedConsents = consentsDto.accepted
                missingConsents = consentsDto.missing
            }.onFailure { e ->
                setNotice(e.message ?: "Yasal onay bilgileri yüklenemedi.", isError = true)
            }
            consentsLoading = false
        }
    }

    fun acceptConsents() {
        consentsLoading = true
        viewModelScope.launch {
            repository.acceptConsents().onSuccess {
                setNotice("Güncel metinler onaylandı.")
                loadConsents()
            }.onFailure { e ->
                setNotice(e.message ?: "Onay işlemi tamamlanamadı.", isError = true)
                consentsLoading = false
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