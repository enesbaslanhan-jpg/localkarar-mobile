package com.localkarar.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import com.localkarar.app.auth.SecureStorage
import com.localkarar.app.core.AppContextHolder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppContextHolder.appContext = applicationContext
        
        val secureStorage = SecureStorage(applicationContext)
        
        setContent {
            App(secureStorage = secureStorage)
        }
    }
}
