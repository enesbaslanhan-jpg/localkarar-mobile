package com.localkarar.app.network.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class DashboardResponse(
    val user: DashboardUserDto,
    val stats: DashboardStatsDto,
    val enrollments: List<DashboardEnrollmentDto>? = null,
    val currentLearningPath: JsonElement? = null,
    val resumeItem: ResumeItemDto? = null,
    val recommendations: List<RecommendationDto>? = null,
    val upcomingTasks: List<UpcomingTaskDto>? = null,
    val recentActivity: List<RecentActivityDto>? = null,
    val quizHistory: List<JsonElement>? = null,
    val recentCompletedKO: JsonElement? = null,
    val recentQuizResult: JsonElement? = null,
    val recentMentorSession: JsonElement? = null,
    val recentCourseActivity: JsonElement? = null,
    val demoMode: Boolean = false
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
