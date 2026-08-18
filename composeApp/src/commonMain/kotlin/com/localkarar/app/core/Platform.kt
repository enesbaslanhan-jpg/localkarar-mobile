package com.localkarar.app.core

import androidx.compose.runtime.Composable

data class PickedFile(
    val name: String,
    val bytes: ByteArray
)

expect fun openExternalUrl(url: String)

@Composable
expect fun rememberFilePicker(onFilePicked: (PickedFile?) -> Unit): () -> Unit