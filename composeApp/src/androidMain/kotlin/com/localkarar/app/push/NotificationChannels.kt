package com.localkarar.app.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val MESSAGES = "lk_messages"
    const val BUSINESS = "lk_business"
    const val ACCOUNT = "lk_account"

    fun create(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannels(
            listOf(
                NotificationChannel(MESSAGES, "Mesajlar ve topluluk", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(BUSINESS, "İşletme hatırlatmaları", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(ACCOUNT, "Hesap bildirimleri", NotificationManager.IMPORTANCE_HIGH)
            )
        )
    }

    fun forTarget(target: PushTarget): String = when (target) {
        is PushTarget.WorkspaceRecord -> BUSINESS
        PushTarget.Account -> ACCOUNT
        is PushTarget.CommunityPost,
        is PushTarget.CommunityThread -> MESSAGES
    }
}

