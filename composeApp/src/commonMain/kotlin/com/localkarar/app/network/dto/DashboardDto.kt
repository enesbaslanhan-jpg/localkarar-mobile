package com.localkarar.app.network.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class DashboardResponse(
    val user: DashboardUserDto,
    val stats: DashboardStatsDto,
    val enrollments: List<DashboardEnrollmentDto>? = null,
    val resumeItem: ResumeItemDto? = null,
    val recommendations: List<RecommendationDto>? = null,
    val upcomingTasks: List<UpcomingTaskDto>? = null,
    val recentActivity: List<RecentActivityDto>? = null,
    val quizHistory: List<QuizAttemptDto>? = null,
    val recentCompletedKO: JsonElement? = null,
    val recentQuizResult: JsonElement? = null,
    val recentMentorSession: RecentMentorSessionDto? = null,
    val recentCourseActivity: RecentCourseActivityDto? = null,
    val demoMode: Boolean = false
)

/**
 * Quiz denemesi gecmisi.
 *
 * 🔴 `feedback` ZORUNLUYDU VE SUNUCU ONU HIC GONDERMIYOR.
 *
 * Sunucu yalniz su bes alani doner (src/services/learnerDashboard.ts:239):
 * `{ id, koId, score, passed, createdAt }`.
 *
 * Sonucu: quiz gecmisi OLAN her kullanicida `/dashboard` yaniti
 * deserialization'da patliyor ve Ana Sayfa'nin TAMAMI "Bir Hata Oluştu --
 * Bağlantı hatası veya sunucuya ulaşılamıyor" ekranina dusuyordu. Sunucu
 * 200 donuyordu; hata agda degil BURADAYDI. Emulatorde yakalandi
 * (03.09.2026), logcat: "Field 'feedback' is required ... $.quizHistory[0]".
 *
 * Quiz/flashcard urun karariyla kapali; ama gecmis kayitlar veritabaninda
 * duruyor ve pano onlari donmeye devam ediyor. DTO bu yuzden tolere etmeli.
 *
 * ⚠️ Bu, pazaryeri DTO'larinda bulunan hatanin AYNISI: sunucunun
 * gondermedigi bir alani zorunlu istemek. Alan eklerken sunucunun GERCEK
 * yanitina bakilmali, tahmin edilmemeli.
 */
@Serializable
data class QuizAttemptDto(
    val id: String,
    val koId: Int,
    val score: Int,
    val passed: Boolean,
    val createdAt: String,
    /** Sunucu gondermiyor; ileride eklenirse diye duruyor. */
    val feedback: String? = null,
    /** Sunucu gondermiyor. */
    val quizId: String? = null
)

@Serializable
data class RecentMentorSessionDto(
    val id: String,
    val eventType: String,
    val createdAt: String
)

@Serializable
data class RecentCourseActivityDto(
    val id: String,
    val eventType: String,
    val courseTitle: String? = null,
    val createdAt: String
)

@Serializable
data class DashboardUserDto(
    val name: String,
    val email: String,
    val role: String
)

@Serializable
data class DashboardStatsDto(
    val completedCourses: Int? = null,
    val activeCourses: Int? = null,
    val notStartedCourses: Int? = null,
    val totalEnrollments: Int? = null,
    val avgProgress: Int? = null,
    val weeklyProgress: Int? = null,
    val weeklyEnrolled: Int? = null,
    val completedKOs: Int? = null,
    val inProgressKOs: Int? = null
)

@Serializable
data class DashboardEnrollmentDto(
    val id: Int,
    val courseId: Int,
    val courseTitle: String,
    val courseCategory: String? = null,
    val courseLevel: String? = null,
    /**
     * Kursun toplam ders sayisi.
     *
     * 🔴 BU ALAN EKSIKTI ve `ignoreUnknownKeys` yuzunden sessizce dusuyordu.
     * Webde iki yerde gorunur: Kurslar sayfasinin ust kartinda ("N ders")
     * ve Kayitlarim listesinde (EnrollmentsPage.jsx:77, CoursesPage.jsx:341).
     * Mobilde ders sayisi hic gosterilemiyordu.
     *
     * Sunucu `/enrollments/my` yanitinda gonderiyor; 0 gelebilir, o zaman
     * web de yazmiyor.
     */
    val courseLessonCount: Int = 0,
    val progress: Int,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class ResumeItemDto(
    val id: Int,
    val courseId: Int,
    val courseTitle: String,
    val progress: Int,
    val status: String,
    val updatedAt: String
)

@Serializable
data class RecommendationDto(
    val id: Int,
    val code: String? = null,
    val title: String,
    val type: String,
    val categoryName: String? = null,
    val createdAt: String
)

@Serializable
data class UpcomingTaskDto(
    val id: String,
    val taskId: String,
    val title: String,
    val status: String,
    val progressPercent: Int,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class RecentActivityDto(
    val id: String,
    val eventType: String,
    val title: String,
    val detail: JsonElement? = null,
    val createdAt: String
)
