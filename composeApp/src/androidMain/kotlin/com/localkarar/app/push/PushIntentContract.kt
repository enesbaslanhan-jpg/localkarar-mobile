package com.localkarar.app.push

import android.content.Intent

object PushIntentContract {
    private const val EXTRA_TARGET = "com.localkarar.app.push.TARGET"
    private const val EXTRA_POST_ID = "com.localkarar.app.push.POST_ID"
    private const val EXTRA_THREAD_ID = "com.localkarar.app.push.THREAD_ID"
    private const val EXTRA_WORKSPACE_ID = "com.localkarar.app.push.WORKSPACE_ID"
    private const val EXTRA_RECORD_ID = "com.localkarar.app.push.RECORD_ID"

    fun put(intent: Intent, target: PushTarget): Intent = intent.apply {
        when (target) {
            is PushTarget.CommunityPost -> {
                putExtra(EXTRA_TARGET, "community_post")
                putExtra(EXTRA_POST_ID, target.postId)
            }
            is PushTarget.CommunityThread -> {
                putExtra(EXTRA_TARGET, "community_thread")
                putExtra(EXTRA_THREAD_ID, target.threadId)
            }
            is PushTarget.WorkspaceRecord -> {
                putExtra(EXTRA_TARGET, "workspace_record")
                putExtra(EXTRA_WORKSPACE_ID, target.workspaceId)
                putExtra(EXTRA_RECORD_ID, target.recordId)
            }
            PushTarget.Account -> putExtra(EXTRA_TARGET, "account")
        }
    }

    fun read(intent: Intent?): Map<String, String>? {
        if (intent == null) return null
        val internalTarget = intent.getStringExtra(EXTRA_TARGET)
        if (internalTarget != null) {
            return buildMap {
                put("target", internalTarget)
                intent.getStringExtra(EXTRA_POST_ID)?.let { put("postId", it) }
                intent.getStringExtra(EXTRA_THREAD_ID)?.let { put("threadId", it) }
                intent.getStringExtra(EXTRA_WORKSPACE_ID)?.let { put("workspaceId", it) }
                intent.getStringExtra(EXTRA_RECORD_ID)?.let { put("recordId", it) }
            }
        }

        // FCM's system notification path copies data payload keys directly to the launch Intent.
        val target = intent.getStringExtra("target") ?: return null
        return buildMap {
            put("target", target)
            intent.getStringExtra("postId")?.let { put("postId", it) }
            intent.getStringExtra("threadId")?.let { put("threadId", it) }
            intent.getStringExtra("workspaceId")?.let { put("workspaceId", it) }
            intent.getStringExtra("recordId")?.let { put("recordId", it) }
        }
    }
}

