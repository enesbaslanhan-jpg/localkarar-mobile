package com.localkarar.app.core

import androidx.compose.runtime.Composable
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun openExternalUrl(url: String) {
    try {
        val nsUrl = NSURL.URLWithString(url) ?: return
        UIApplication.sharedApplication.openURL(nsUrl)
    } catch (e: Exception) {
        println("openExternalUrl failed: ${e.message}")
    }
}

@Composable
actual fun rememberFilePicker(onFilePicked: (PickedFile?) -> Unit): () -> Unit {
    return {
        println("File picking is not available on iOS yet.")
        onFilePicked(null)
    }
}