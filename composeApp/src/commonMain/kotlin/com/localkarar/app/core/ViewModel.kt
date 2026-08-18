package com.localkarar.app.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

/**
 * Creates a ViewModel scoped to the platform ViewModelStore when available
 * (Android), falling back to composition-scoped remember on targets without a
 * ViewModelStoreOwner (iOS).
 */
@Composable
inline fun <reified T : ViewModel> lkViewModel(
    key: String,
    noinline create: () -> T
): T {
    val owner = LocalViewModelStoreOwner.current
    return if (owner != null) {
        viewModel(
            key = key,
            factory = viewModelFactory {
                initializer { create() }
            }
        )
    } else {
        remember(key) { create() }
    }
}