package com.localkarar.app.workspaces

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ActiveWorkspaceStore {
    private val _activeWorkspaceId = MutableStateFlow<String?>(null)
    val activeWorkspaceId: StateFlow<String?> = _activeWorkspaceId.asStateFlow()

    private val _activeWorkspaceName = MutableStateFlow<String?>(null)
    val activeWorkspaceName: StateFlow<String?> = _activeWorkspaceName.asStateFlow()

    fun setActive(workspaceId: String?, name: String? = null) {
        _activeWorkspaceId.value = workspaceId
        _activeWorkspaceName.value = name
    }
}