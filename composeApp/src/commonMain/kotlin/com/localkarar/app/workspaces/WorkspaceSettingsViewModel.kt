package com.localkarar.app.workspaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.InboxSenderDto
import com.localkarar.app.network.dto.InboxStatusDto
import com.localkarar.app.network.dto.UpdateSettingsRequestDto
import com.localkarar.app.network.dto.UpdateWorkspaceRequestDto
import com.localkarar.app.network.dto.WorkspaceDetailDto
import com.localkarar.app.network.dto.WorkspaceSettingsDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class WorkspaceSettingsUiState {
    object Loading : WorkspaceSettingsUiState()
    data class Content(
        val settings: WorkspaceSettingsDto,
        /**
         * Isletmenin kendisi.
         *
         * Ayarlar ekrani mockup'ta ilk satirda ISLETME ADINI gosteriyor ve
         * en altta silinecekleri sayiyor; ikisi de `settings` ucundan
         * gelmiyor. Detay istegi bu yuzden ayarla birlikte yapiliyor.
         * `null` kalabilir — detay gelmezse ekran ayarlarla calismaya
         * devam eder, yalniz ad satiri ve tehlikeli alan gizlenir.
         */
        val workspace: WorkspaceDetailDto? = null,
        /**
         * e-Fatura gelen kutusu durumu.
         *
         * `null` = ya sunucu 403 dondu (yetkisiz uye) ya da istek
         * basarisiz oldu. Ikisinde de bolum GIZLENIYOR: kullanamayacagi
         * bir kanali gostermek, ekrani doldurmaktan baska ise yaramaz.
         */
        val inbox: InboxStatusDto? = null,
        val inboxSenders: List<InboxSenderDto> = emptyList(),
        val isSaving: Boolean = false,
        val isInboxBusy: Boolean = false,
        val isDeleting: Boolean = false
    ) : WorkspaceSettingsUiState()
    data class Error(val message: String) : WorkspaceSettingsUiState()
}

class WorkspaceSettingsViewModel(
    private val workspaceId: String,
    private val repository: WorkspaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WorkspaceSettingsUiState>(WorkspaceSettingsUiState.Loading)
    val uiState: StateFlow<WorkspaceSettingsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = WorkspaceSettingsUiState.Loading
        viewModelScope.launch {
            val result = repository.getSettings(workspaceId)
            if (result.isSuccess) {
                val detay = repository.getWorkspace(workspaceId).getOrNull()
                val kutu = repository.getInbox(workspaceId).getOrNull()
                val gonderenler =
                    if (kutu == null) emptyList()
                    else repository.getInboxSenders(workspaceId)
                        .getOrNull()?.gonderenler.orEmpty()
                _uiState.value = WorkspaceSettingsUiState.Content(
                    settings = result.getOrThrow(),
                    workspace = detay,
                    inbox = kutu,
                    inboxSenders = gonderenler
                )
            } else {
                _uiState.value = WorkspaceSettingsUiState.Error(
                    result.exceptionOrNull()?.message ?: "Ayarlar yüklenemedi."
                )
            }
        }
    }

    fun save(
        defaultCurrency: String,
        timezone: String,
        locale: String,
        weekStartsOn: Int,
        onError: (String) -> Unit
    ) {
        val current = _uiState.value
        if (current !is WorkspaceSettingsUiState.Content || current.isSaving) return
        _uiState.value = current.copy(isSaving = true)
        viewModelScope.launch {
            val result = repository.updateSettings(
                workspaceId,
                UpdateSettingsRequestDto(
                    defaultCurrency = defaultCurrency,
                    timezone = timezone,
                    locale = locale,
                    weekStartsOn = weekStartsOn
                )
            )
            if (result.isSuccess) {
                _uiState.value = current.copy(settings = result.getOrThrow(), isSaving = false)
            } else {
                _uiState.value = current.copy(isSaving = false)
                onError(result.exceptionOrNull()?.message ?: "Ayarlar kaydedilemedi.")
            }
        }
    }

    /**
     * ISLETME PROFILI — webdeki ilk kartin tamami.
     *
     * 🔴 MOBILDE YALNIZ AD DEGISTIRILEBILIYORDU. Webde ayni ekranda
     * unvan, vergi no, sektor, sehir, isletme asamasi, calisan sayisi,
     * satis kanallari, hedef, zorluklar ve dort finansal alan var
     * (`Settings.jsx` "İşletme Profili"). DTO bunlarin hepsini zaten
     * tasiyordu; gonderen kimse yoktu. Onemi kozmetik degil: mentor
     * onerileri ve isletme takibi bu alanlara gore kisisellestiriliyor.
     *
     * ⚠️ Bos metin ve `null` FARKLI: `null` "dokunma" demek. Sunucu
     * `!== undefined` bakiyor, biz de gondermedigimiz alani hic
     * yazmiyoruz.
     */
    fun saveProfile(
        body: UpdateWorkspaceRequestDto,
        onError: (String) -> Unit,
        onSaved: () -> Unit = {}
    ) {
        val current = _uiState.value
        if (current !is WorkspaceSettingsUiState.Content || current.isSaving) return
        val ad = body.name?.trim()
        if (ad != null && ad.length < 2) {
            onError("İşletme adı en az 2 karakter olmalı.")
            return
        }
        val vergi = body.taxNumber?.trim()
        if (!vergi.isNullOrEmpty() && vergi.length !in listOf(10, 11)) {
            /* Sunucu da reddediyor; burada engellemek kullaniciya
               hatayi ALANIN YANINDA gostermeyi mumkun kiliyor. */
            onError("Vergi numarası 10 (VKN) ya da 11 (TCKN) haneli olmalı.")
            return
        }
        _uiState.value = current.copy(isSaving = true)
        viewModelScope.launch {
            val result = repository.updateWorkspace(workspaceId, body)
            if (result.isSuccess) {
                _uiState.value = current.copy(workspace = result.getOrThrow(), isSaving = false)
                onSaved()
            } else {
                _uiState.value = current.copy(isSaving = false)
                onError(result.exceptionOrNull()?.message ?: "İşletme bilgileri kaydedilemedi.")
            }
        }
    }

    /** Gelen kutusu adresi acar ya da YENILER (sunucu her cagrida yeni uretir). */
    fun inboxAcVeyaYenile(onError: (String) -> Unit) {
        val current = _uiState.value
        if (current !is WorkspaceSettingsUiState.Content || current.isInboxBusy) return
        _uiState.value = current.copy(isInboxBusy = true)
        viewModelScope.launch {
            val result = repository.enableInbox(workspaceId)
            if (result.isSuccess) {
                /* Yanit `kanalHazir` tasimayabiliyor; onceki degeri
                   koruyoruz ki "hazir degil" uyarisi kaybolmasin. */
                val yeni = result.getOrThrow()
                _uiState.value = current.copy(
                    inbox = yeni.copy(kanalHazir = yeni.kanalHazir || current.inbox?.kanalHazir == true),
                    isInboxBusy = false
                )
            } else {
                _uiState.value = current.copy(isInboxBusy = false)
                onError(result.exceptionOrNull()?.message ?: "Adres oluşturulamadı.")
            }
        }
    }

    fun inboxKapat(onError: (String) -> Unit) {
        val current = _uiState.value
        if (current !is WorkspaceSettingsUiState.Content || current.isInboxBusy) return
        _uiState.value = current.copy(isInboxBusy = true)
        viewModelScope.launch {
            val result = repository.disableInbox(workspaceId)
            if (result.isSuccess) {
                _uiState.value = current.copy(
                    inbox = current.inbox?.copy(acik = false, adres = null),
                    isInboxBusy = false
                )
            } else {
                _uiState.value = current.copy(isInboxBusy = false)
                onError(result.exceptionOrNull()?.message ?: "Gelen kutusu kapatılamadı.")
            }
        }
    }

    fun gonderenEkle(email: String, etiket: String?, onError: (String) -> Unit) {
        val current = _uiState.value
        if (current !is WorkspaceSettingsUiState.Content || current.isInboxBusy) return
        val temiz = email.trim()
        if (!temiz.contains("@") || temiz.length < 5) {
            onError("Geçerli bir e-posta adresi girin.")
            return
        }
        _uiState.value = current.copy(isInboxBusy = true)
        viewModelScope.launch {
            val result = repository.addInboxSender(workspaceId, temiz, etiket)
            if (result.isSuccess) {
                val liste = repository.getInboxSenders(workspaceId)
                    .getOrNull()?.gonderenler.orEmpty()
                _uiState.value = current.copy(inboxSenders = liste, isInboxBusy = false)
            } else {
                _uiState.value = current.copy(isInboxBusy = false)
                onError(result.exceptionOrNull()?.message ?: "Gönderen eklenemedi.")
            }
        }
    }

    fun gonderenCikar(senderId: String, onError: (String) -> Unit) {
        val current = _uiState.value
        if (current !is WorkspaceSettingsUiState.Content || current.isInboxBusy) return
        _uiState.value = current.copy(isInboxBusy = true)
        viewModelScope.launch {
            val result = repository.removeInboxSender(workspaceId, senderId)
            if (result.isSuccess) {
                _uiState.value = current.copy(
                    inboxSenders = current.inboxSenders.filterNot { it.id == senderId },
                    isInboxBusy = false
                )
            } else {
                _uiState.value = current.copy(isInboxBusy = false)
                onError(result.exceptionOrNull()?.message ?: "Gönderen çıkarılamadı.")
            }
        }
    }

    /**
     * Isletme adini degistirir.
     *
     * 🔴 EKRANDA HICBIR SEY DUZENLENEMIYORDU: ad, saat dilimi ve dil
     * salt okunur satirlardi, para birimi disinda dokunulacak bir sey
     * yoktu. Mockup'ta isletme adi satiri duzenlenebilir; sunucu da
     * `PUT /workspaces/:id` ile adi kabul ediyor.
     */
    fun rename(yeniAd: String, onError: (String) -> Unit) {
        val current = _uiState.value
        if (current !is WorkspaceSettingsUiState.Content || current.isSaving) return
        val temiz = yeniAd.trim()
        if (temiz.length < 2) {
            onError("İşletme adı en az 2 karakter olmalı.")
            return
        }
        _uiState.value = current.copy(isSaving = true)
        viewModelScope.launch {
            val result = repository.updateWorkspace(
                workspaceId,
                UpdateWorkspaceRequestDto(name = temiz)
            )
            if (result.isSuccess) {
                _uiState.value = current.copy(workspace = result.getOrThrow(), isSaving = false)
            } else {
                _uiState.value = current.copy(isSaving = false)
                onError(result.exceptionOrNull()?.message ?: "İşletme adı güncellenemedi.")
            }
        }
    }

    /**
     * Isletmeyi siler.
     *
     * Yikici islem ekranin EN ALTINDA ve tek basina duruyor; onayi arayuz
     * aliyor, burasi yalniz cagriyi yapiyor. Basarisiz olursa durum eski
     * haline donuyor — "silindi" deyip silmemis olmak en kotu sonuc.
     */
    fun delete(onDeleted: () -> Unit, onError: (String) -> Unit) {
        val current = _uiState.value
        if (current !is WorkspaceSettingsUiState.Content || current.isDeleting) return
        _uiState.value = current.copy(isDeleting = true)
        viewModelScope.launch {
            val result = repository.deleteWorkspace(workspaceId)
            if (result.isSuccess) {
                onDeleted()
            } else {
                _uiState.value = current.copy(isDeleting = false)
                onError(result.exceptionOrNull()?.message ?: "İşletme silinemedi.")
            }
        }
    }
}