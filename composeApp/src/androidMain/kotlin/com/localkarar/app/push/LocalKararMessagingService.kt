package com.localkarar.app.push

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.localkarar.app.MainActivity
import com.localkarar.app.R

class LocalKararMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        PushPreferences(applicationContext).pushToken = token
        PushRuntime.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val result = PushPayloadParser.parse(message.data)
        val target = (result as? PushPayloadResult.Success)?.target ?: return
        NotificationChannels.create(applicationContext)

        val launchIntent = PushIntentContract.put(
            Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            target
        )
        val requestCode = target.hashCode() and Int.MAX_VALUE
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            requestCode,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            applicationContext,
            NotificationChannels.forTarget(target)
        )
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(message.notification?.title ?: "LocalKarar")
            .setContentText(message.notification?.body ?: "Yeni bir bildiriminiz var")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        getSystemService(NotificationManager::class.java).notify(requestCode, notification)
    }
}

