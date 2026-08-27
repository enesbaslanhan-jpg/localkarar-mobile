package com.localkarar.app.navigation

import com.localkarar.app.network.dto.FormulaCalculationDto
import com.localkarar.app.network.dto.FormulaDto

sealed interface Destination {
    object Login : Destination
    object Home : Destination
    object Courses : Destination
    data class DecisionTools(val initialFilter: String = "all") : Destination
    object AiMentor : Destination
    object Calculations : Destination
    object News : Destination
    object Community : Destination
    object Workspaces : Destination
    object Settings : Destination

    data class CourseDetail(val courseId: Int) : Destination
    data class LessonReader(val courseId: Int, val lessonId: Int) : Destination
    data class DecisionSession(val sessionId: String) : Destination

    data class FormulaDetail(
    val formula: FormulaDto,
    val historicalCalculation: FormulaCalculationDto? = null
) : Destination
    data class FinancialModelDetail(val code: String) : Destination
    data class ModelRuns(val workspaceId: String, val modelCode: String? = null) : Destination
    data class RunDetail(val workspaceId: String, val runId: String) : Destination

    data class WorkspaceHome(val workspaceId: String) : Destination
    data class Records(val workspaceId: String) : Destination
    data class RecordDetail(val workspaceId: String, val recordId: String) : Destination
    data class RecordEdit(val workspaceId: String, val recordId: String?) : Destination
    data class Orders(val workspaceId: String) : Destination
    data class Products(val workspaceId: String) : Destination
    data class Documents(val workspaceId: String) : Destination
    data class Notifications(val workspaceId: String) : Destination
    data class Calendar(val workspaceId: String) : Destination
    data class Team(val workspaceId: String) : Destination
    data class Contacts(val workspaceId: String) : Destination
    data class Activity(val workspaceId: String) : Destination
    data class WorkspaceSettings(val workspaceId: String) : Destination

    data class Conversation(val conversationId: Int) : Destination
    data class NewsDetail(val articleId: String) : Destination
    data class CommunityPost(val postId: String) : Destination

    object Profile : Destination
    object PasswordChange : Destination
    object EmailChange : Destination
    object DeleteAccount : Destination
}