package com.localkarar.app

import com.localkarar.app.core.AppPreferences

import androidx.compose.ui.window.ComposeUIViewController

import com.localkarar.app.auth.SecureStorage

fun MainViewController() = ComposeUIViewController { 
    val secureStorage = SecureStorage()
    App(secureStorage = secureStorage, appPreferences = AppPreferences()) 
}
