package com.localkarar.app.network.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class FormulaDto(
    val id: String,
    val name: String,
    val inputs: List<FormulaInputDto> = emptyList(),
    val warning: String? = null,
    val category: String? = null,
    val description: String? = null
)

@Serializable
data class FormulaInputDto(
    val name: String,
    val label: String,
    val unit: String? = null,
    val min: Double? = null,
    val max: Double? = null
)

@Serializable
data class FormulaCalculateRequestDto(
    val inputs: Map<String, Double>
)

@Serializable
data class FormulaCalculateResponseDto(
    val formulaId: String,
    val result: Map<String, JsonElement> = emptyMap(),
    val assumptions: List<JsonElement> = emptyList(),
    val warnings: List<String> = emptyList()
)

@Serializable
data class FormulaCalculationDto(
    val id: Int,
    val formulaId: String,
    val formulaName: String,
    val inputs: Map<String, JsonElement> = emptyMap(),
    val result: Map<String, JsonElement> = emptyMap(),
    val createdAt: String
)

@Serializable
data class FinancialModelListResponseDto(
    val models: List<FinancialModelDto> = emptyList(),
    val total: Int = 0
)

@Serializable
data class FinancialModelDto(
    val code: String,
    val name: String,
    val category: String = "",
    val purpose: String = "",
    val description: String = "",
    val engineVersion: String? = null,
    val policyVersion: String? = null,
    val level: String? = null,
    val formula: String? = null,
    val inputs: List<FinancialModelInputDto> = emptyList(),
    val outputs: List<FinancialModelOutputDto> = emptyList(),
    val interpretationRules: List<String> = emptyList(),
    val warningRules: List<String> = emptyList(),
    val limitations: List<String> = emptyList(),
    val sources: List<FinancialSourceDto> = emptyList(),
    val courseCode: String? = null,
    val requirementCount: Int = 0,
    val versions: List<FinancialModelVersionDto> = emptyList()
)

@Serializable
data class FinancialModelInputDto(
    val key: String,
    val label: String,
    val type: String = "number",
    val unit: String = "",
    val required: Boolean = true,
    val min: Double? = null,
    val max: Double? = null,
    val description: String = "",
    val sourceRequired: Boolean? = null
)

@Serializable
data class FinancialModelOutputDto(
    val key: String,
    val label: String,
    val unit: String = "",
    val description: String = ""
)

@Serializable
data class FinancialSourceDto(
    val title: String = "",
    val url: String = "",
    val authority: String = "",
    val usage: String = ""
)

@Serializable
data class FinancialModelVersionDto(
    val id: String = "",
    val version: String = "",
    val changeSummary: String = "",
    val createdAt: String? = null
)

@Serializable
data class ModelAssumptionDto(
    val key: String,
    val value: JsonElement,
    val unit: String? = null,
    val sourceType: String = "user",
    val sourceReference: String? = null,
    val effectiveDate: String? = null,
    val confidence: Double? = null,
    val userVerified: Boolean? = null
)

@Serializable
data class ModelRunRequestDto(
    val inputs: Map<String, JsonElement>,
    val assumptions: List<ModelAssumptionDto> = emptyList(),
    val scenarioName: String = "base",
    /*
     * Model bir BELGEDEN acildiysa o belgenin kimligi.
     *
     * ⚠️ Gonderildiginde sunucu EK BIR KAPI aciyor: belgeden gelen en
     * az bir alanin `userVerified` olmasi sart, yoksa 422 ("OCR belge
     * verileri modelde kullanilmadan once kullanici tarafindan
     * dogrulanmalidir"). Bu yuzden bos birakilamaz degil -- yalnizca
     * gercekten belgeden acildiysa gonderiliyor.
     */
    val sourceDocumentId: String? = null
)

@Serializable
data class ValidationCheckDto(
    val code: String = "",
    val label: String = "",
    val passed: Boolean = false,
    val severity: String = "info",
    val detail: String = ""
)

@Serializable
data class ConfidenceComponentDto(
    val key: String = "",
    val label: String = "",
    val score: Double = 0.0,
    val reason: String = ""
)

@Serializable
data class ModelConfidenceDto(
    val score: Double = 0.0,
    val label: String = "low",
    val disclaimer: String = "",
    val components: List<ConfidenceComponentDto> = emptyList()
)

@Serializable
data class CalculationStepDto(
    val key: String = "",
    val label: String = "",
    val formula: String = "",
    val inputs: Map<String, JsonElement> = emptyMap(),
    val result: JsonElement? = null,
    val rounding: String = ""
)

@Serializable
data class FinancialModelRunResponseDto(
    val id: String? = null,
    val scenarioName: String? = null,
    val createdAt: String? = null,
    val model: FinancialModelDto? = null,
    val outputs: Map<String, JsonElement> = emptyMap(),
    val checks: List<ValidationCheckDto> = emptyList(),
    val warnings: List<String> = emptyList(),
    val confidence: ModelConfidenceDto? = null,
    val trace: List<CalculationStepDto> = emptyList(),
    val ethics: List<ValidationCheckDto> = emptyList(),
    val normalizedInputs: Map<String, JsonElement> = emptyMap()
)

@Serializable
data class FinancialModelSummaryDto(
    val code: String? = null,
    val name: String? = null,
    val category: String? = null
)

@Serializable
data class FinancialModelRunListItemDto(
    val id: String,
    val scenarioName: String? = null,
    val createdAt: String? = null,
    val status: String? = null,
    val model: FinancialModelSummaryDto? = null,
    val inputs: Map<String, JsonElement> = emptyMap(),
    val outputs: Map<String, JsonElement> = emptyMap()
)

@Serializable
data class FinancialModelRunListResponseDto(
    val runs: List<FinancialModelRunListItemDto> = emptyList(),
    val total: Int = 0
)

@Serializable
data class FinancialModelRunDetailDto(
    val id: String,
    val scenarioName: String? = null,
    val createdAt: String? = null,
    val status: String? = null,
    val model: FinancialModelDto? = null,
    val inputs: Map<String, JsonElement> = emptyMap(),
    val normalizedInputs: Map<String, JsonElement> = emptyMap(),
    val outputs: Map<String, JsonElement> = emptyMap(),
    val checks: List<ValidationCheckDto> = emptyList(),
    val warnings: List<String> = emptyList(),
    val confidence: ModelConfidenceDto? = null,
    val calculationTrace: List<CalculationStepDto> = emptyList()
)
/**
 * Pazaryeri hesaplama ipucu — `/marketplace/calculation-hints`.
 *
 * Son 90 gunun gercek siparis kalemlerinden ortalama satis fiyati ve
 * komisyon orani. `available=false` ise baglanti yok ya da ornek yok;
 * o zaman arayuz hicbir sey cizmiyor.
 */
@Serializable
data class HesaplamaIpucuDto(
    val available: Boolean = false,
    /** Saglayici adi — TRENDYOL, HEPSIBURADA... */
    val source: String? = null,
    val currency: String = "TRY",
    /** Kac siparis kaleminden hesaplandi. */
    val sampleSize: Int = 0,
    val avgUnitPrice: Double? = null,
    /*
     * Komisyon orani her baglantida gelmiyor. `null` ise o alan ELLE
     * birakiliyor -- sifir yazmak, komisyonu yok saymak olurdu.
     */
    val avgCommissionPercent: Double? = null,
    val note: String? = null
)

/**
 * Karar gunlugu girdisi — `POST /workspaces/{ws}/decision-journal`.
 *
 * 🔴 Mobilde HIC YOKTU. Webde model calistiktan sonra "Kararı kaydet"
 * var (`FinancialModelWorkspace.jsx`): hangi model calismasina dayanarak
 * ne karar verildi ve ne bekleniyor. Sonradan gercek sonuc yazilarak
 * kapatiliyor.
 *
 * ⚠️ `modelRunId` ZORUNLU: karar gunlugu bir model CALISMASINA bagli.
 * Calistirilmamis bir modelden karar kaydedilemiyor -- ve dogrusu bu:
 * dayanaksiz bir "karar" kaydi izlenebilir olmazdi.
 */
@Serializable
data class KararGunluguIstegiDto(
    val modelRunId: String,
    val decision: String,
    val expectedOutcome: String
)

/**
 * Karar gunlugu kaydi — hem yazma yanitinda hem LISTEDE kullaniliyor.
 *
 * 🔴 Alanlarin cogu YOKTU: mobil yalnizca karar YAZIYORDU, hicbir
 * yerde okumuyordu. Sunucuda okuma ucu bile yoktu (09.09.2026'da
 * eklendi). Gerceklesen sonuc, sapma ve cikarilan ders yaziliyor ama
 * kullanici bir daha goremiyordu.
 *
 * ⚠️ `variance` SUNUCUDA HESAPLANMIYOR, kullanici yaziyor. Beklenen ve
 * gerceklesen serbest metin; farkini sayiyla ifade etmek mumkun degil.
 */
@Serializable
data class KararGunluguDto(
    val id: String? = null,
    val decision: String? = null,
    val expectedOutcome: String? = null,
    val actualOutcome: String? = null,
    val variance: String? = null,
    val lessonLearned: String? = null,
    val createdAt: String? = null,
    val reviewedAt: String? = null,
    val modelRun: KararModelCalismasiDto? = null
)

/** Karara dayanak olan model calismasinin ozeti. */
@Serializable
data class KararModelCalismasiDto(
    val id: String? = null,
    val scenarioName: String? = null,
    val createdAt: String? = null,
    val model: FinancialModelSummaryDto? = null
)

/**
 * `GET /workspaces/{ws}/decision-journal`.
 *
 * `ozet` SUNUCUDAN geliyor ve SUZGECTEN BAGIMSIZ: "8 karardan 3'u
 * degerlendirildi" cumlesi, suzgec uygulandiginda da dogru kalsin.
 */
@Serializable
data class KararGunluguListesiDto(
    val entries: List<KararGunluguDto> = emptyList(),
    val ozet: KararGunluguOzetiDto? = null
)

@Serializable
data class KararGunluguOzetiDto(
    val toplam: Int = 0,
    val degerlendirilen: Int = 0,
    val bekleyen: Int = 0
)
