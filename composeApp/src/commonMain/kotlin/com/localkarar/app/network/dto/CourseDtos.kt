package com.localkarar.app.network.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CoursesListResponse(
    val courses: List<CourseDto>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
)

@Serializable
data class CourseDto(
    val id: Int,
    val slug: String? = null,
    val title: String,
    val description: String? = null,
    val category: String? = null,
    val level: String? = null,
    val lessonCount: Int? = null,
    val estimatedMinutes: Int? = null,
    val sourceType: String? = null,
    val sortOrder: Int? = null,
    val enrollment: EnrollmentDto? = null,
    val createdAt: String? = null
)

@Serializable
data class CourseDetailResponse(
    val course: CourseDetailDto
)

@Serializable
data class CourseDetailDto(
    val id: Int,
    val slug: String? = null,
    val title: String,
    val description: String? = null,
    val category: String? = null,
    val level: String? = null,
    val estimatedMinutes: Int? = null,
    val archived: Boolean? = null,
    val lessonCount: Int? = null,
    val lessons: List<LessonSummaryDto> = emptyList(),
    val enrollment: EnrollmentDto? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class EnrollmentDto(
    val id: Int,
    val status: String,
    val progress: Int
)

@Serializable
data class EnrollmentsListResponse(
    val enrollments: List<DashboardEnrollmentDto>
)

@Serializable
data class LessonSummaryDto(
    val id: Int,
    val title: String,
    val order: Int,
    val estimatedMinutes: Int? = null,
    val knowledgeObjectId: Int? = null,
    val knowledgeObjectCode: String? = null,
    val progress: LessonProgressDto? = null,
    val isLocked: Boolean
)

@Serializable
data class LessonProgressDto(
    val status: String,
    val overallPercent: Int,
    val readingPercent: Int,
    val flashcardPercent: Int,
    val videoPercent: Int,
    val quizPercent: Int,
    val taskPercent: Int,
    val lastViewedAt: String? = null
)

@Serializable
data class LessonDetailResponse(
    val lesson: LessonDetailDto
)

@Serializable
data class LessonDetailDto(
    val id: Int,
    val courseId: Int,
    val title: String,
    val order: Int,
    val estimatedMinutes: Int? = null,
    val content: String? = null,
    val knowledgeObject: KnowledgeObjectDto? = null,
    val progress: LessonProgressDto? = null,
    val prevLesson: LessonPointerDto? = null,
    val nextLesson: LessonPointerDto? = null,
    val embeddedPracticeBlocks: List<EmbeddedPracticeBlockDto> = emptyList(),
    /** Yalniz KANONIK derste dolu; digerlerinde null. */
    val canonicalSections: CanonicalSectionsDto? = null
)

@Serializable
data class EmbeddedPracticeBlockDto(
    val id: String,
    val type: String,
    val title: String,
    val description: String? = null,
    val targetId: String? = null,
    val targetCode: String? = null,
    val order: Int? = null
)

@Serializable
data class KnowledgeObjectDto(
    val id: Int,
    val code: String,
    val title: String,
    val content: String? = null,
    val status: String? = null,
    val hasFlashcards: Boolean = false,
    val hasVideo: Boolean = false,
    val metadata: KnowledgeObjectMetadataDto? = null,
    val sources: List<KnowledgeObjectSourceWrapperDto> = emptyList()
)

@Serializable
data class KnowledgeObjectMetadataDto(
    val summary: String? = null,
    val description: String? = null,
    val level: String? = null,
    val examples: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val checklist: List<String> = emptyList(),
    val formulas: List<String> = emptyList(),
    val learningOutcomes: List<String> = emptyList()
)

@Serializable
data class KnowledgeObjectSourceWrapperDto(
    val id: String,
    val source: KnowledgeObjectSourceDto
)

@Serializable
data class KnowledgeObjectSourceDto(
    val id: String,
    val title: String,
    /**
     * ⚠️ Sunucu bu alani GONDERMIYOR (source nesnesi:
     * id, title, url, authorityLevel, lastChecked, createdAt).
     * Alan duruyor cunku arayuz onu null gecip atliyor; kaldirmak
     * LessonReaderScreen'deki yayinci satirini da kaldirmak demek.
     */
    val publisher: String? = null,
    val authorityLevel: String? = null,
    val url: String? = null,
    /**
     * Kaynagin son dogrulanma tarihi (ISO). Sunucu gonderiyor.
     *
     * ⚠️ HENUZ CIZILMIYOR. Bu tarihi gosteren tek yuzey Bilgi Nesnesi
     * detayiydi ve o urunden kaldirildi (03.09.2026, urun sahibi karari).
     * Alan burada duruyor cunku DTO sunucunun gercek sozlesmesini
     * yansitiyor; ders ekranindaki kaynak listesine eklenmesi ayri bir
     * karar.
     */
    val lastChecked: String? = null
)

@Serializable
data class LessonPointerDto(
    val id: Int,
    val title: String
)

@Serializable
data class PracticalCardListResponse(
    val data: List<PracticalCardSummaryDto>
)

@Serializable
data class PracticalCardSummaryDto(
    val id: String,
    val code: String,
    val title: String,
    val type: String? = null,
    val shortDescription: String? = null,
    val category: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class PracticalCardDetailResponse(
    val data: PracticalCardDetailDto
)

@Serializable
data class PracticalCardDetailDto(
    val id: String,
    val code: String,
    val title: String,
    val type: String? = null,
    val shortDescription: String? = null,
    val category: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val content: JsonElement? = null,
    val sources: List<PracticalCardSourceDto> = emptyList()
)

@Serializable
data class PracticalCardSourceDto(
    val id: Int,
    val title: String,
    val code: String
)

@Serializable
data class ReadingCompleteRequest(
    val lessonId: Int,
    val courseId: Int
)

@Serializable
data class LessonViewRequest(
    val lessonId: Int
)

@Serializable
data class EnrollRequest(
    val courseId: Int
)

/* ------------------------------------------------------------------ *
 * KANONIK DERS BOLUMLERI
 *
 * Yayimdaki 38 dersin TAMAMI kanonik (`CANON-` kodlu) ve her birinde
 * "Pratik Bilgi Kartlari" ile bir entegrasyon bolumu var (olculdu
 * 03.09.2026). Webde bu bolumler yapisal kartlara ve DUGMELERE
 * ceviriliyor; mobilde ham markdown olarak basiliyordu, yani kullanici
 * `[ Hesaplamalar > Gercek Birim Maliyet Hesaplayicisini Ac ]` satirini
 * DUZ METIN olarak goruyordu.
 *
 * Ayristirma SUNUCUDA yapiliyor (src/services/canonical-lesson.ts);
 * boylece 600 satirlik bulanik eslestiriciyi Kotlin'de ikinci kez
 * yazmak -- ve sessizce yanlis hesaplamaya baglama riski -- ortadan
 * kalkiyor.
 * ------------------------------------------------------------------ */

/** Ders govdesinde gecen, kataloga COZULMUS hesaplama referansi. */
@Serializable
data class CanonicalCalculationRefDto(
    val label: String = "",
    /** Katalog kimligi ("unit-cost"). `CalculationCatalog` ile cozulur. */
    val calculationId: String = "",
    val title: String? = null,
    val hasSimple: Boolean = false,
    val hasDetailed: Boolean = false
)

/** Formul karti — gercek LaTeX tasiyor, `LkMath` ile ciziliyor. */
@Serializable
data class CanonicalFormulaCardDto(
    val title: String = "",
    val description: String = "",
    val formulas: List<String> = emptyList(),
    val example: CanonicalFormulaExampleDto? = null,
    val interpretation: String = "",
    /** Kartin basligindan cozulen hesaplama; yoksa null -> DUGME BASILMAZ. */
    val calculationId: String? = null,
    val decisionToolCode: String? = null,
    val decisionToolTitle: String? = null
)

@Serializable
data class CanonicalFormulaExampleDto(
    val intro: String = "",
    val formulas: List<String> = emptyList()
)

/** Hata / Dogru karti. */
@Serializable
data class CanonicalMistakeCardDto(
    val title: String? = null,
    val wrong: String? = null,
    val correct: String? = null
)

/** Karar araci entegrasyonu (dersin 3. bolumu). */
@Serializable
data class CanonicalDecisionDto(
    val toolCode: String? = null,
    val toolTitle: String? = null,
    val context: String = "",
    val bullets: List<String> = emptyList(),
    val result: String = ""
)

@Serializable
data class CanonicalExtraDecisionDto(
    val code: String = "",
    val title: String = ""
)

@Serializable
data class CanonicalSectionsDto(
    /** 3./4./5. bolumler ve satir ici referanslar CIKARILMIS markdown. */
    val body: String = "",
    val decision: CanonicalDecisionDto? = null,
    val extraDecisions: List<CanonicalExtraDecisionDto> = emptyList(),
    val calculations: List<CanonicalCalculationRefDto> = emptyList(),
    val formulaCards: List<CanonicalFormulaCardDto> = emptyList(),
    val mistakeCards: List<CanonicalMistakeCardDto> = emptyList()
)
