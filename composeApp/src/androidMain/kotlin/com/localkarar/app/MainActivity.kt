package com.localkarar.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import com.localkarar.app.auth.SecureStorage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val secureStorage = SecureStorage(applicationContext)
        
        setContent {
            App(secureStorage = secureStorage)
        }
    }
}
