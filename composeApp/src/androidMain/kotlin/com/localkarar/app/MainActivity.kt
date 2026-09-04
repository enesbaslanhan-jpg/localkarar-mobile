package com.localkarar.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.localkarar.app.auth.SecureStorage
import com.localkarar.app.core.AppPreferences
import com.localkarar.app.core.AppContextHolder
import com.localkarar.app.navigation.deeplink.DeepLinkDispatcher

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppContextHolder.appContext = applicationContext

        // Handle cold-start intent
        handleIntent(intent)

        val secureStorage = SecureStorage(applicationContext)
        val appPreferences = AppPreferences(applicationContext)

        setContent {
            App(secureStorage = secureStorage, appPreferences = appPreferences)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Handle warm-start intent
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data
        if (uri != null) {
            DeepLinkDispatcher.submit(uri.toString())
        }
    }
}
