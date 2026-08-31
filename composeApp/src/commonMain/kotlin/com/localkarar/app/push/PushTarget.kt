package com.localkarar.app.push

import com.localkarar.app.navigation.deeplink.DeepLinkTarget

sealed interface PushTarget {
    data class CommunityPost(val postId: String) : PushTarget
    data class CommunityThread(val threadId: String) : PushTarget
    data class WorkspaceRecord(val workspaceId: String, val recordId: String) : PushTarget
    data object Account : PushTarget

    fun toDeepLinkTarget(): DeepLinkTarget = when (this) {
        is CommunityPost -> DeepLinkTarget.CommunityPost(postId)
        is CommunityThread -> DeepLinkTarget.CommunityThreadNative(threadId)
        is WorkspaceRecord -> DeepLinkTarget.WorkspaceRecordNative(workspaceId, recordId)
        Account -> DeepLinkTarget.SettingsRoot
    }
}

sealed interface PushPayloadResult {
    data class Success(val target: PushTarget) : PushPayloadResult
    data object Unsupported : PushPayloadResult
    data class Malformed(val reason: String) : PushPayloadResult
}

