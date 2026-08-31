package com.localkarar.app.navigation.deeplink

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PendingDeepLinkStore {
    private var pendingTarget: DeepLinkTarget? = null
    private var lastConsumedTime: Long = 0L
    private var lastTarget: DeepLinkTarget? = null

    private val _pendingFlow = MutableStateFlow<DeepLinkTarget?>(null)
    val pendingFlow: StateFlow<DeepLinkTarget?> = _pendingFlow.asStateFlow()

    fun set(target: DeepLinkTarget) {
        // Prevent duplicate delivery within 1.5 seconds if identical
        val now = currentTimeMillis()
        if (target == lastTarget && (now - lastConsumedTime) < 1500L) {
            return
        }
        pendingTarget = target
        _pendingFlow.value = target
    }

    fun peek(): DeepLinkTarget? = pendingTarget

    fun consume(): DeepLinkTarget? {
        val target = pendingTarget
        if (target != null) {
            lastTarget = target
            lastConsumedTime = currentTimeMillis()
            pendingTarget = null
            _pendingFlow.value = null
        }
        return target
    }

    fun clear() {
        pendingTarget = null
        lastTarget = null
        lastConsumedTime = 0L
        _pendingFlow.value = null
    }

    private fun currentTimeMillis(): Long {
        return try {
            kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        } catch (_: Exception) {
            0L
        }
    }
}
