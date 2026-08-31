package com.localkarar.app

import android.app.Application
import com.localkarar.app.core.AppContextHolder
import com.localkarar.app.push.NotificationChannels

class LocalKararApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContextHolder.appContext = applicationContext
        NotificationChannels.create(this)
    }
}

