package com.localkarar.app.ui.shell

import androidx.compose.runtime.Composable

@Composable
expect fun SystemBackHandler(enabled: Boolean, onBack: () -> Unit)
