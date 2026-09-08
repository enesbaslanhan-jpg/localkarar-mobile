package com.localkarar.app.decision

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.core.AppMessages
import com.localkarar.app.network.dto.BusinessRecordDto
import com.localkarar.app.network.dto.RecordInputDto
import com.localkarar.app.network.dto.RecordUpdateDto
import com.localkarar.app.workspaces.WorkspaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * KARAR TAKIBI — "bu karar tuttu mu?"
 *
 * 🔴 MOBILDE HIC YOKTU. Webde karar sonucunun altinda bir bolum var
 * (`DecisionFollowUp.jsx`): karari isletmede bir GOREVE baglıyor, vakti
 * gelince gercek sonucu ve cikarilan dersi kaydettiriyor. Karar
 * araclarinin ogrenme dongusu bu; mobilde karar veriliyor ama geri
 * donup bakilamiyordu.
 *
 * ⚠️ SUNUCUDA OZEL BIR UC YOK ve gerekmiyor. Web de normal bir kayit
 * aciyor (`type: "task"`) ve karar bilgisini `metadata`ya yaziyor.
 * Mobil AYNI sekli kullaniyor -- baska turlu yazilsaydi webde acilan
 * takip mobilde gorunmezdi.
 *
 * `metadata.decisionSessionId` -> hangi karara ait
 * `metadata.decisionFollowUp.decisionTitle` / `.expectedOutcome`
 * `metadata.decisionFollowUp.actualOutcome` / `.lessonLearned` / `.reviewedAt`
 */
class DecisionFollowUpViewModel(
    private val workspaceId: String?,
    private val sessionId: String,
    private val repository: WorkspaceRepository
) : ViewModel() {

    private val _takipler = MutableStateFlow<List<BusinessRecordDto>>(emptyList())
    val takipler: StateFlow<List<BusinessRecordDto>> = _takipler.asStateFlow()

    private val _islemde = MutableStateFlow(false)
    val islemde: StateFlow<Boolean> = _islemde.asStateFlow()

    init {
        yukle()
    }

    fun yukle() {
        val ws = workspaceId ?: return
        viewModelScope.launch {
            repository.getRecords(ws, limit = 100)
                .onSuccess { yanit ->
                    /*
                     * Suzgec ISTEMCIDE: sunucu `decisionSessionId`e gore
                     * suzmuyor, web de ayni sekilde cekip kendisi
                     * suzuyor (`DecisionFollowUp.jsx`).
                     */
                    _takipler.value = yanit.records.filter { kayit ->
                        kayit.metadata["decisionSessionId"]?.jsonPrimitive?.contentOrNull == sessionId
                    }
                }
                /* Sessiz: takip listesi gelmezse karar sonucu yine okunur.
                   Bos liste gostermek, hata kutusu gostermekten iyi. */
                .onFailure { }
        }
    }

    /**
     * Karari bir goreve baglar.
     *
     * @param beklenenSonuc kullanicinin "ne olmasini bekliyorum" cevabi.
     * @param vadeIso son tarih (ISO 8601).
     */
    /*
     * ⚠️ `kararBasligi` PARAMETRE, kurucu alani DEGIL.
     *
     * Kurucuda tutuluyordu ve AppShell orada basligi bilmiyor (oturum
     * henuz yuklenmemis olabilir); sonuc olarak `decisionTitle`
     * veritabanina BOS yaziliyordu -- olculdu (08.09.2026). Baslik
     * ekranda, oturum yuklendikten sonra belli oluyor; oradan geliyor.
     */
    fun takipOlustur(
        kararBasligi: String,
        baslik: String,
        beklenenSonuc: String,
        vadeIso: String,
        onBitti: () -> Unit
    ) {
        val ws = workspaceId ?: return
        if (_islemde.value) return
        _islemde.value = true
        viewModelScope.launch {
            val ustVeri = mapOf(
                "decisionSessionId" to JsonPrimitive(sessionId),
                "decisionFollowUp" to JsonObject(
                    mapOf(
                        "decisionTitle" to JsonPrimitive(kararBasligi),
                        "expectedOutcome" to JsonPrimitive(beklenenSonuc.trim())
                    )
                )
            )
            repository.createRecord(
                ws,
                RecordInputDto(
                    type = "task",
                    title = baslik.trim(),
                    /* Gorev; para hareketi degil. */
                    direction = "neutral",
                    dueAt = vadeIso,
                    metadata = ustVeri
                )
            )
                .onSuccess {
                    _islemde.value = false
                    AppMessages.bilgi("Karar takibi oluşturuldu.")
                    yukle()
                    onBitti()
                }
                .onFailure { hata ->
                    _islemde.value = false
                    AppMessages.hata(hata.message ?: "Takip oluşturulamadı.")
                }
        }
    }

    /**
     * Takibi kapatir: gercek sonuc ve cikarilan ders yazilir.
     *
     * ⚠️ MEVCUT `metadata` KORUNUYOR. Ustune yazmak
     * `decisionSessionId`i ve `expectedOutcome`u silerdi; o zaman kayit
     * karara bagli olmaktan cikar ve "ne bekliyordum" sorusu cevapsiz
     * kalirdi.
     */
    fun sonucuKaydet(
        kayit: BusinessRecordDto,
        gercekSonuc: String,
        cikarilanDers: String,
        onBitti: () -> Unit
    ) {
        val ws = workspaceId ?: return
        if (_islemde.value) return
        _islemde.value = true
        viewModelScope.launch {
            val eskiTakip = (kayit.metadata["decisionFollowUp"] as? JsonObject)?.toMutableMap()
                ?: mutableMapOf()
            eskiTakip["actualOutcome"] = JsonPrimitive(gercekSonuc.trim())
            eskiTakip["lessonLearned"] = JsonPrimitive(cikarilanDers.trim())

            val yeniUstVeri = kayit.metadata.toMutableMap()
            yeniUstVeri["decisionFollowUp"] = JsonObject(eskiTakip)

            repository.updateRecord(
                ws,
                kayit.id,
                RecordUpdateDto(status = "completed", metadata = yeniUstVeri)
            )
                .onSuccess {
                    _islemde.value = false
                    AppMessages.bilgi("Sonuç kaydedildi.")
                    yukle()
                    onBitti()
                }
                .onFailure { hata ->
                    _islemde.value = false
                    AppMessages.hata(hata.message ?: "Sonuç kaydedilemedi.")
                }
        }
    }
}
