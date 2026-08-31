package com.localkarar.app.workspaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.OrderDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrdersViewModel(
    private val repository: WorkspaceRepository
) : ViewModel() {

    // Raw orders loaded from backend (provider filter applied server-side)
    private val _allOrders = MutableStateFlow<List<OrderDto>>(emptyList())

    // Derived: status is a client-side deep-link filter (Web parity)
    private val _orders = MutableStateFlow<List<OrderDto>>(emptyList())
    val orders: StateFlow<List<OrderDto>> = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncedAt = MutableStateFlow<String?>(null)
    val lastSyncedAt: StateFlow<String?> = _lastSyncedAt.asStateFlow()

    private val _integrationConnected = MutableStateFlow(true)
    val integrationConnected: StateFlow<Boolean> = _integrationConnected.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Backend filter: provider is sent on the canonical list request
    private val _selectedProvider = MutableStateFlow<String?>(null)
    val selectedProvider: StateFlow<String?> = _selectedProvider.asStateFlow()

    // Client-side deep-link filter: status is NOT sent to backend (Web parity)
    private val _selectedStatus = MutableStateFlow<String?>(null)
    val selectedStatus: StateFlow<String?> = _selectedStatus.asStateFlow()

    fun loadOrders(workspaceId: String) {
        if (workspaceId.isBlank()) return
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            // Canonical Web contract: only workspaceId + provider + limit go to backend
            repository.getOrders(
                workspaceId = workspaceId,
                provider = _selectedProvider.value
            )
                .onSuccess { resp ->
                    _allOrders.value = resp.orders
                    _lastSyncedAt.value = resp.lastSyncedAt
                    _integrationConnected.value = resp.integrationConnected
                    _isLoading.value = false
                    applyClientSideFilters()
                }
                .onFailure { err ->
                    _error.value = err.message ?: "Siparişler yüklenemedi."
                    _isLoading.value = false
                }
        }
    }

    // Apply status as a client-side deep-link filter (matching Web Orders.jsx behavior)
    private fun applyClientSideFilters() {
        val status = _selectedStatus.value
        _orders.value = if (status.isNullOrBlank() || status == "TÜMÜ") {
            _allOrders.value
        } else {
            _allOrders.value.filter { it.status.equals(status, ignoreCase = true) }
        }
    }

    fun syncNow(workspaceId: String) {
        if (workspaceId.isBlank() || _isSyncing.value) return
        _isSyncing.value = true
        _error.value = null
        viewModelScope.launch {
            repository.syncOrders(workspaceId)
                .onSuccess { syncResp ->
                    _lastSyncedAt.value = syncResp.lastSyncedAt
                    _isSyncing.value = false
                    loadOrders(workspaceId)
                }
                .onFailure { err ->
                    _error.value = err.message ?: "Eşitleme başarısız oldu."
                    _isSyncing.value = false
                }
        }
    }

    // provider -> backend (re-fetches)
    fun setProviderFilter(workspaceId: String, provider: String?) {
        _selectedProvider.value = provider
        loadOrders(workspaceId)
    }

    // status -> client-side only (no re-fetch)
    fun setStatusFilter(status: String?) {
        _selectedStatus.value = status
        applyClientSideFilters()
    }
}
