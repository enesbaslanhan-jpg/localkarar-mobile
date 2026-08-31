package com.localkarar.app.push

import com.localkarar.app.navigation.Destination
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PushPayloadParserTest {
    @Test
    fun communityPostMapsToDeepLinkDestination() {
        val result = PushPayloadParser.parse(
            mapOf("target" to "community_post", "postId" to "post_1001")
        )
        val pushTarget = assertIs<PushPayloadResult.Success>(result).target
        assertEquals(Destination.CommunityPost("post_1001"), pushTarget.toDeepLinkTarget().toDestination())
    }

    @Test
    fun communityThreadUsesNativeThreadDestination() {
        val result = PushPayloadParser.parse(
            mapOf("target" to "community_thread", "threadId" to "thr_505")
        )
        val pushTarget = assertIs<PushPayloadResult.Success>(result).target
        assertEquals(Destination.CommunityThreadDetail("thr_505"), pushTarget.toDeepLinkTarget().toDestination())
    }

    @Test
    fun workspaceRecordUsesBothExplicitIds() {
        val result = PushPayloadParser.parse(
            mapOf(
                "target" to "workspace_record",
                "workspaceId" to "ws_alpha",
                "recordId" to "rec_99"
            )
        )
        val pushTarget = assertIs<PushPayloadResult.Success>(result).target
        assertEquals(Destination.RecordDetail("ws_alpha", "rec_99"), pushTarget.toDeepLinkTarget().toDestination())
    }

    @Test
    fun accountAlwaysRoutesToSettings() {
        val result = PushPayloadParser.parse(mapOf("target" to "account"))
        val pushTarget = assertIs<PushPayloadResult.Success>(result).target
        assertEquals(Destination.Settings, pushTarget.toDeepLinkTarget().toDestination())
    }

    @Test
    fun missingRequiredIdIsMalformed() {
        assertIs<PushPayloadResult.Malformed>(
            PushPayloadParser.parse(mapOf("target" to "community_thread"))
        )
    }

    @Test
    fun unknownTargetIsUnsupported() {
        assertIs<PushPayloadResult.Unsupported>(
            PushPayloadParser.parse(mapOf("target" to "marketing"))
        )
    }

    @Test
    fun oversizedOrControlCharacterIdsAreRejected() {
        assertIs<PushPayloadResult.Malformed>(
            PushPayloadParser.parse(
                mapOf("target" to "community_post", "postId" to "x".repeat(129))
            )
        )
        assertIs<PushPayloadResult.Malformed>(
            PushPayloadParser.parse(
                mapOf("target" to "community_post", "postId" to "post\n1")
            )
        )
    }
}

