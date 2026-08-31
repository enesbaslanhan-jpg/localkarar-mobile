package com.localkarar.app.navigation.deeplink

import com.localkarar.app.navigation.Destination

sealed interface DeepLinkTarget {
    // Community Root Tabs
    data object CommunityFeedRoot : DeepLinkTarget
    data object CommunityPeopleRoot : DeepLinkTarget
    data object CommunityThreadsRoot : DeepLinkTarget
    data class CommunityPost(val postId: String) : DeepLinkTarget
    data class UserProfile(val userId: Int) : DeepLinkTarget
    data object SelfProfile : DeepLinkTarget
    data object NotificationsRoot : DeepLinkTarget

    // Mentor
    data object MentorRoot : DeepLinkTarget

    // Courses
    data object CoursesRoot : DeepLinkTarget
    data class CourseDetail(val courseId: Int) : DeepLinkTarget
    data class CourseLesson(val courseId: Int, val lessonId: Int) : DeepLinkTarget

    // Decision Tools
    data object DecisionToolsRoot : DeepLinkTarget
    data class DecisionTool(val code: String) : DeepLinkTarget

    // Financial Models
    data class FinancialModel(val code: String) : DeepLinkTarget

    // Workspaces / Tracker
    data object WorkspacesRoot : DeepLinkTarget
    data class WorkspaceHome(val workspaceId: String) : DeepLinkTarget
    data class WorkspaceRecords(val workspaceId: String) : DeepLinkTarget
    data class WorkspaceOrders(val workspaceId: String) : DeepLinkTarget
    data class WorkspaceProducts(val workspaceId: String) : DeepLinkTarget
    data class WorkspaceCalendar(val workspaceId: String) : DeepLinkTarget
    data class WorkspaceDocuments(val workspaceId: String) : DeepLinkTarget
    data class WorkspaceNotifications(val workspaceId: String) : DeepLinkTarget
    data class WorkspaceTeam(val workspaceId: String) : DeepLinkTarget
    data class WorkspaceContacts(val workspaceId: String) : DeepLinkTarget
    data class WorkspaceSettings(val workspaceId: String) : DeepLinkTarget
    data class WorkspaceActivity(val workspaceId: String) : DeepLinkTarget

    // Settings
    data object SettingsRoot : DeepLinkTarget

    // News
    data object NewsRoot : DeepLinkTarget

    // Native-Only Semantic Targets (MOBILE_NATIVE_TARGET)
    data class CommunityThreadNative(val threadId: String) : DeepLinkTarget
    data class ConversationNative(val conversationId: Int) : DeepLinkTarget
    data class WorkspaceRecordNative(val workspaceId: String, val recordId: String) : DeepLinkTarget
    data class NewsArticleNative(val articleId: String) : DeepLinkTarget

    fun toDestination(): Destination = when (this) {
        is CommunityFeedRoot -> Destination.Community("feed")
        is CommunityPeopleRoot -> Destination.Community("people")
        is CommunityThreadsRoot -> Destination.Community("threads")
        is CommunityPost -> Destination.CommunityPost(postId)
        is UserProfile -> Destination.CommunityProfile(userId)
        is SelfProfile -> Destination.Profile
        is NotificationsRoot -> Destination.CommunityNotifications

        is MentorRoot -> Destination.AiMentor

        is CoursesRoot -> Destination.Courses
        is CourseDetail -> Destination.CourseDetail(courseId)
        is CourseLesson -> Destination.LessonReader(courseId, lessonId)

        is DecisionToolsRoot -> Destination.DecisionTools("all")
        is DecisionTool -> Destination.DecisionTool(code)

        is FinancialModel -> Destination.FinancialModelDetail(code)

        is WorkspacesRoot -> Destination.Workspaces
        is WorkspaceHome -> Destination.WorkspaceHome(workspaceId)
        is WorkspaceRecords -> Destination.Records(workspaceId)
        is WorkspaceOrders -> Destination.Orders(workspaceId)
        is WorkspaceProducts -> Destination.Products(workspaceId)
        is WorkspaceCalendar -> Destination.Calendar(workspaceId)
        is WorkspaceDocuments -> Destination.Documents(workspaceId)
        is WorkspaceNotifications -> Destination.Notifications(workspaceId)
        is WorkspaceTeam -> Destination.Team(workspaceId)
        is WorkspaceContacts -> Destination.Contacts(workspaceId)
        is WorkspaceSettings -> Destination.WorkspaceSettings(workspaceId)
        is WorkspaceActivity -> Destination.Activity(workspaceId)

        is SettingsRoot -> Destination.Settings
        is NewsRoot -> Destination.News

        is CommunityThreadNative -> Destination.CommunityThreadDetail(threadId)
        is ConversationNative -> Destination.Conversation(conversationId)
        is WorkspaceRecordNative -> Destination.RecordDetail(workspaceId, recordId)
        is NewsArticleNative -> Destination.NewsDetail(articleId)
    }
}
