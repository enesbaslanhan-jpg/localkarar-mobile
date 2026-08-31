package com.localkarar.app.navigation

import com.localkarar.app.navigation.deeplink.DeepLinkParser
import com.localkarar.app.navigation.deeplink.DeepLinkResult
import com.localkarar.app.navigation.deeplink.DeepLinkTarget
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DeepLinkParserTest {

    @Test
    fun testAllAllowedWebRoutesParity() {
        val cases = listOf(
            "https://localkarar.com/app/community" to DeepLinkTarget.NewsRoot,
            "https://localkarar.com/app/community/topluluk" to DeepLinkTarget.CommunityFeedRoot,
            "https://localkarar.com/app/community/kisiler" to DeepLinkTarget.CommunityPeopleRoot,
            "https://localkarar.com/app/community/sohbetler" to DeepLinkTarget.CommunityThreadsRoot,
            "https://localkarar.com/app/community/gonderi/post_888" to DeepLinkTarget.CommunityPost("post_888"),
            "https://localkarar.com/app/profil" to DeepLinkTarget.SelfProfile,
            "https://localkarar.com/app/profil/42" to DeepLinkTarget.UserProfile(42),
            "https://localkarar.com/app/bildirimler" to DeepLinkTarget.NotificationsRoot,
            "https://localkarar.com/app/mentor" to DeepLinkTarget.MentorRoot,
            "https://localkarar.com/app/courses" to DeepLinkTarget.CoursesRoot,
            "https://localkarar.com/app/courses/10/learn" to DeepLinkTarget.CourseDetail(10),
            "https://localkarar.com/app/courses/10/learn/5" to DeepLinkTarget.CourseLesson(10, 5),
            "https://localkarar.com/app/decision-checks" to DeepLinkTarget.DecisionToolsRoot,
            "https://localkarar.com/app/decision-checks/finansman-karari" to DeepLinkTarget.DecisionTool("finansman-karari"),
            "https://localkarar.com/app/finance/models/dcf" to DeepLinkTarget.FinancialModel("dcf"),
            "https://localkarar.com/app/workspaces" to DeepLinkTarget.WorkspacesRoot,
            "https://localkarar.com/app/workspaces/ws_alpha/overview" to DeepLinkTarget.WorkspaceHome("ws_alpha"),
            "https://localkarar.com/app/workspaces/ws_alpha/tracker" to DeepLinkTarget.WorkspaceRecords("ws_alpha"),
            "https://localkarar.com/app/workspaces/ws_alpha/orders" to DeepLinkTarget.WorkspaceOrders("ws_alpha"),
            "https://localkarar.com/app/workspaces/ws_alpha/products" to DeepLinkTarget.WorkspaceProducts("ws_alpha"),
            "https://localkarar.com/app/workspaces/ws_alpha/calendar" to DeepLinkTarget.WorkspaceCalendar("ws_alpha"),
            "https://localkarar.com/app/workspaces/ws_alpha/documents" to DeepLinkTarget.WorkspaceDocuments("ws_alpha"),
            "https://localkarar.com/app/workspaces/ws_alpha/notifications" to DeepLinkTarget.WorkspaceNotifications("ws_alpha"),
            "https://localkarar.com/app/workspaces/ws_alpha/team" to DeepLinkTarget.WorkspaceTeam("ws_alpha"),
            "https://localkarar.com/app/workspaces/ws_alpha/contacts" to DeepLinkTarget.WorkspaceContacts("ws_alpha"),
            "https://localkarar.com/app/workspaces/ws_alpha/settings" to DeepLinkTarget.WorkspaceSettings("ws_alpha"),
            "https://localkarar.com/app/workspaces/ws_alpha/activity" to DeepLinkTarget.WorkspaceActivity("ws_alpha"),
            "https://localkarar.com/app/settings" to DeepLinkTarget.SettingsRoot
        )

        for ((url, expectedTarget) in cases) {
            val result = DeepLinkParser.parse(url)
            assertIs<DeepLinkResult.Success>(result, "Expected Success for '$url'")
            assertEquals(expectedTarget, result.target, "Target mismatch for '$url'")
            assertEquals(expectedTarget.toDestination(), result.target.toDestination())
        }
    }

    @Test
    fun testQueryStringsAndFragmentsIgnoredSafely() {
        val url = "https://localkarar.com/app/courses/12/learn/34?utm_source=push&ref=campaign#section2"
        val result = DeepLinkParser.parse(url)
        assertIs<DeepLinkResult.Success>(result)
        assertEquals(DeepLinkTarget.CourseLesson(12, 34), result.target)
        assertEquals(Destination.LessonReader(12, 34), result.target.toDestination())
    }

    @Test
    fun testNativeOnlyTargetsMapping() {
        val dmTarget = DeepLinkTarget.CommunityThreadNative("thr_99")
        assertEquals(Destination.CommunityThreadDetail("thr_99"), dmTarget.toDestination())

        val convTarget = DeepLinkTarget.ConversationNative(77)
        assertEquals(Destination.Conversation(77), convTarget.toDestination())

        val recTarget = DeepLinkTarget.WorkspaceRecordNative("ws_1", "rec_2")
        assertEquals(Destination.RecordDetail("ws_1", "rec_2"), recTarget.toDestination())

        val articleTarget = DeepLinkTarget.NewsArticleNative("art_5")
        assertEquals(Destination.NewsDetail("art_5"), articleTarget.toDestination())
    }

    @Test
    fun testDecisionToolVsSessionSeparation() {
        val result = DeepLinkParser.parse("https://localkarar.com/app/decision-checks/ihracat-karari")
        assertIs<DeepLinkResult.Success>(result)
        assertEquals(DeepLinkTarget.DecisionTool("ihracat-karari"), result.target)
        assertEquals(Destination.DecisionTool("ihracat-karari"), result.target.toDestination())
    }

    @Test
    fun testCourseOptionalLessonRoute() {
        val detailResult = DeepLinkParser.parse("https://localkarar.com/app/courses/10/learn")
        assertIs<DeepLinkResult.Success>(detailResult)
        assertEquals(DeepLinkTarget.CourseDetail(10), detailResult.target)
        assertEquals(Destination.CourseDetail(10), detailResult.target.toDestination())

        val lessonResult = DeepLinkParser.parse("https://localkarar.com/app/courses/10/learn/25")
        assertIs<DeepLinkResult.Success>(lessonResult)
        assertEquals(DeepLinkTarget.CourseLesson(10, 25), lessonResult.target)
        assertEquals(Destination.LessonReader(10, 25), lessonResult.target.toDestination())
    }

    @Test
    fun testCommunityTabsRouteToCorrectSubSurfacesWithoutFakeIds() {
        val topluluk = DeepLinkParser.parse("https://localkarar.com/app/community/topluluk")
        assertIs<DeepLinkResult.Success>(topluluk)
        assertEquals(Destination.Community("feed"), topluluk.target.toDestination())

        val kisiler = DeepLinkParser.parse("https://localkarar.com/app/community/kisiler")
        assertIs<DeepLinkResult.Success>(kisiler)
        assertEquals(Destination.Community("people"), kisiler.target.toDestination())

        val sohbetler = DeepLinkParser.parse("https://localkarar.com/app/community/sohbetler")
        assertIs<DeepLinkResult.Success>(sohbetler)
        assertEquals(Destination.Community("threads"), sohbetler.target.toDestination())
    }

    @Test
    fun testInvalidAndMalformedInputsRejectedSafely() {
        val invalidUrls = listOf(
            null,
            "",
            "   ",
            "http://localkarar.com/app/courses", // Insecure HTTP scheme
            "https://evil.com/app/courses", // Untrusted host
            "https://sub.localkarar.com/app/courses", // Subdomain mismatch
            "localkarar://app/courses", // Custom scheme
            "javascript:alert(1)",
            "file:///etc/passwd",
            "content://com.android.providers.media/external/images",
            "https://localkarar.com/", // Root path
            "https://localkarar.com/app", // Bare /app
            "https://localkarar.com/app/nonexistent_section", // Unknown route
            "https://localkarar.com/app/courses/not_a_number/learn", // Non-integer courseId
            "https://localkarar.com/app/courses/10/learn/not_a_number", // Non-integer lessonId
            "https://localkarar.com/app/profil/not_a_number", // Non-integer userId
            "https://localkarar.com/app/profil/-5", // Negative userId
            "https://localkarar.com/app/community/gonderi", // Missing postId
            "https://localkarar.com/app/community/gonderi/", // Blank postId
            "https://localkarar.com/app/workspaces/ws_alpha/unknown_tab" // Unknown workspace subtab
        )

        for (invalid in invalidUrls) {
            val result = DeepLinkParser.parse(invalid)
            assertIs<DeepLinkResult>(result)
            kotlin.test.assertTrue(
                result is DeepLinkResult.Unsupported || result is DeepLinkResult.Malformed,
                "Expected Unsupported or Malformed for '$invalid', but got $result"
            )
        }
    }
}
