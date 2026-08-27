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
    val embeddedPracticeBlocks: List<EmbeddedPracticeBlockDto> = emptyList()
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
    val publisher: String? = null,
    val authorityLevel: String? = null,
    val url: String? = null
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
