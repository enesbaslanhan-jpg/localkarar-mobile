package com.localkarar.app.workspaces

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ActiveWorkspaceStore {
    private val _activeWorkspaceId = MutableStateFlow<String?>(null)
    val activeWorkspaceId: StateFlow<String?> = _activeWorkspaceId.asStateFlow()

    fun setActive(workspaceId: String?) {
        _activeWorkspaceId.value = workspaceId
    }
}