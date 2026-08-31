package com.localkarar.app.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DestinationCodecTest {

    @Test
    fun testAllDestinationsRoundTrip() {
        val testCases: List<Destination> = listOf(
            Destination.Login,
            Destination.Home,
            Destination.Courses,
            Destination.CourseDetail(courseId = 42),
            Destination.LessonReader(courseId = 42, lessonId = 7),
            Destination.DecisionTools(initialFilter = "financial"),
            Destination.DecisionTools(initialFilter = "all"),
            Destination.DecisionSession(sessionId = "sess_abc_123"),
            Destination.AiMentor,
            Destination.Conversation(conversationId = 99),
            Destination.News,
            Destination.NewsDetail(articleId = "art_777"),
            Destination.Calculations,
            Destination.FormulaDetail(formulaId = "form_kdv"),
            Destination.FinancialModelDetail(code = "dcf"),
            Destination.ModelRuns(workspaceId = "ws_alpha", modelCode = "dcf"),
            Destination.ModelRuns(workspaceId = "ws_alpha", modelCode = null),
            Destination.RunDetail(workspaceId = "ws_alpha", runId = "run_999"),
            Destination.Workspaces,
            Destination.WorkspaceHome(workspaceId = "ws_alpha"),
            Destination.Records(workspaceId = "ws_alpha"),
            Destination.RecordDetail(workspaceId = "ws_alpha", recordId = "rec_55"),
            Destination.RecordEdit(workspaceId = "ws_alpha", recordId = "rec_55"),
            Destination.RecordEdit(workspaceId = "ws_alpha", recordId = null),
            Destination.Orders(workspaceId = "ws_alpha"),
            Destination.Products(workspaceId = "ws_alpha"),
            Destination.Documents(workspaceId = "ws_alpha"),
            Destination.Notifications(workspaceId = "ws_alpha"),
            Destination.Calendar(workspaceId = "ws_alpha"),
            Destination.Team(workspaceId = "ws_alpha"),
            Destination.Contacts(workspaceId = "ws_alpha"),
            Destination.Activity(workspaceId = "ws_alpha"),
            Destination.WorkspaceSettings(workspaceId = "ws_alpha"),
            Destination.Community,
            Destination.CommunityPost(postId = "post_1001"),
            Destination.CommunityProfile(userId = 303),
            Destination.CommunityFollowers(userId = 303, mode = "followers"),
            Destination.CommunityFollowers(userId = 303, mode = "following"),
            Destination.CommunityThreadDetail(threadId = "thr_909"),
            Destination.CommunityNotifications,
            Destination.Settings,
            Destination.Profile,
            Destination.PasswordChange,
            Destination.EmailChange,
            Destination.LegalConsents,
            Destination.DeleteAccount
        )

        for (original in testCases) {
            val encoded = DestinationCodec.encode(original)
            val decoded = DestinationCodec.decode(encoded)
            assertEquals(original, decoded, "Failed round-trip for $original (encoded: '$encoded')")
        }
    }

    @Test
    fun testMalformedAndUnknownRoutesFallbackSafely() {
        val invalidInputs = listOf(
            "",
            "unknown_route",
            "course_detail",
            "course_detail:not_a_number",
            "lesson_reader:42",
            "lesson_reader:42:not_a_number",
            "conversation:invalid",
            "community_profile:not_int",
            "community_followers:303",
            "run_detail:only_one_param",
            "record_detail:only_one_param"
        )

        for (invalid in invalidInputs) {
            val decoded = DestinationCodec.decode(invalid)
            assertEquals(Destination.Home, decoded, "Expected Destination.Home fallback for '$invalid', got $decoded")
        }
    }
}
