package com.localkarar.app.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NavController(initialDestination: Destination) {
    private val _backStack = MutableStateFlow(listOf(initialDestination))
    val backStack: StateFlow<List<Destination>> = _backStack.asStateFlow()

    val currentDestination: Destination
        get() = _backStack.value.last()

    fun navigateTo(destination: Destination) {
        val currentStack = _backStack.value
        if (currentStack.lastOrNull() == destination) return
        
        val isPrimaryRoot = when (destination) {
            Destination.Home,
            Destination.Workspaces,
            is Destination.WorkspaceHome,
            Destination.Community,
            Destination.Calculations,
            Destination.Settings -> true
            else -> false
        }
        
        if (isPrimaryRoot) {
            _backStack.value = listOf(destination)
        } else {
            _backStack.value = currentStack + destination
        }
    }

    fun popBackStack(): Boolean {
        val currentStack = _backStack.value
        if (currentStack.size > 1) {
            _backStack.value = currentStack.dropLast(1)
            return true
        }
        return false
    }

    fun resetTo(destination: Destination) {
        _backStack.value = listOf(destination)
    }
}
