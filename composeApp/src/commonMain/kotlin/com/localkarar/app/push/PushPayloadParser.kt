package com.localkarar.app.push

object PushPayloadParser {
    private const val MAX_VALUE_LENGTH = 512

    fun parse(data: Map<String, String>): PushPayloadResult {
        if (data.isEmpty()) return PushPayloadResult.Malformed("Payload is empty")
        if (data.values.any { it.length > MAX_VALUE_LENGTH }) {
            return PushPayloadResult.Malformed("Payload value exceeds safe limit")
        }

        return when (data["target"]?.trim()) {
            null, "" -> PushPayloadResult.Malformed("Target is missing")
            "community_post" -> requiredId(data, "postId")
                ?.let { PushPayloadResult.Success(PushTarget.CommunityPost(it)) }
                ?: PushPayloadResult.Malformed("postId is invalid")
            "community_thread" -> requiredId(data, "threadId")
                ?.let { PushPayloadResult.Success(PushTarget.CommunityThread(it)) }
                ?: PushPayloadResult.Malformed("threadId is invalid")
            "workspace_record" -> {
                val workspaceId = requiredId(data, "workspaceId")
                val recordId = requiredId(data, "recordId")
                if (workspaceId != null && recordId != null) {
                    PushPayloadResult.Success(PushTarget.WorkspaceRecord(workspaceId, recordId))
                } else {
                    PushPayloadResult.Malformed("workspaceId or recordId is invalid")
                }
            }
            "account" -> PushPayloadResult.Success(PushTarget.Account)
            else -> PushPayloadResult.Unsupported
        }
    }

    private fun requiredId(data: Map<String, String>, key: String): String? {
        val value = data[key]?.trim() ?: return null
        return value.takeIf {
            it.isNotEmpty() &&
                it.length <= 128 &&
                it.none(Char::isISOControl)
        }
    }
}

