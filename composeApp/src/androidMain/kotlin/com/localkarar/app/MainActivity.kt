package com.localkarar.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.localkarar.app.auth.SecureStorage
import com.localkarar.app.core.AppContextHolder
import com.localkarar.app.navigation.deeplink.DeepLinkDispatcher
import com.localkarar.app.navigation.deeplink.PendingDeepLinkStore
import com.localkarar.app.push.PushIntentContract
import com.localkarar.app.push.PushPayloadParser
import com.localkarar.app.push.PushPayloadResult

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppContextHolder.appContext = applicationContext

        // Handle cold-start intent
        handleIntent(intent)

        val secureStorage = SecureStorage(applicationContext)

        setContent {
            App(secureStorage = secureStorage)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Handle warm-start intent
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val pushData = PushIntentContract.read(intent)
        val pushResult = pushData?.let(PushPayloadParser::parse)
        if (pushResult is PushPayloadResult.Success) {
            PendingDeepLinkStore.set(pushResult.target.toDeepLinkTarget())
            return
        }

        val uri = intent?.data
        if (uri != null) {
            DeepLinkDispatcher.submit(uri.toString())
        }
    }
}
