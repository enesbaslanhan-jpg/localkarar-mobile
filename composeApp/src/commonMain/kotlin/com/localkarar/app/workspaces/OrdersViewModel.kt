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

    private val _selectedProvider = MutableStateFlow<String?>(null)
    val selectedProvider: StateFlow<String?> = _selectedProvider.asStateFlow()

    private val _selectedStatus = MutableStateFlow<String?>(null)
    val selectedStatus: StateFlow<String?> = _selectedStatus.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun loadOrders(workspaceId: String) {
        if (workspaceId.isBlank()) return
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            repository.getOrders(
                workspaceId = workspaceId,
                provider = _selectedProvider.value,
                status = _selectedStatus.value,
                query = _searchQuery.value
            )
                .onSuccess { resp ->
                    _orders.value = resp.orders
                    _lastSyncedAt.value = resp.lastSyncedAt
                    _integrationConnected.value = resp.integrationConnected
                    _isLoading.value = false
                }
                .onFailure { err ->
                    _error.value = err.message ?: "Siparişler yüklenemedi."
                    _isLoading.value = false
                }
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

    fun setProviderFilter(workspaceId: String, provider: String?) {
        _selectedProvider.value = provider
        loadOrders(workspaceId)
    }

    fun setStatusFilter(workspaceId: String, status: String?) {
        _selectedStatus.value = status
        loadOrders(workspaceId)
    }

    fun setSearchQuery(workspaceId: String, query: String) {
        _searchQuery.value = query
        loadOrders(workspaceId)
    }
}
