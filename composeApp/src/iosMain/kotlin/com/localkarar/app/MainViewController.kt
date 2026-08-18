package com.localkarar.app

import androidx.compose.ui.window.ComposeUIViewController

import com.localkarar.app.auth.SecureStorage

fun MainViewController() = ComposeUIViewController { 
    val secureStorage = SecureStorage()
    App(secureStorage = secureStorage) 
}
