package com.localkarar.app.workspaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.CreateOrderRequestDto
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

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedStatus = MutableStateFlow<String?>(null)
    val selectedStatus: StateFlow<String?> = _selectedStatus.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun loadOrders(workspaceId: String) {
        if (workspaceId.isBlank()) return
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            repository.getOrders(workspaceId, _selectedStatus.value, _searchQuery.value)
                .onSuccess { resp ->
                    _orders.value = resp.orders
                    _isLoading.value = false
                }
                .onFailure { err ->
                    _error.value = err.message ?: "Siparişler yüklenemedi."
                    _isLoading.value = false
                }
        }
    }

    fun setStatusFilter(workspaceId: String, status: String?) {
        _selectedStatus.value = status
        loadOrders(workspaceId)
    }

    fun setSearchQuery(workspaceId: String, query: String) {
        _searchQuery.value = query
        loadOrders(workspaceId)
    }

    fun createOrder(workspaceId: String, request: CreateOrderRequestDto, onComplete: () -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.createOrder(workspaceId, request)
                .onSuccess {
                    loadOrders(workspaceId)
                    onComplete()
                }
                .onFailure { err ->
                    _error.value = err.message ?: "Sipariş oluşturulamadı."
                    _isLoading.value = false
                }
        }
    }

    fun updateStatus(workspaceId: String, orderId: String, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(workspaceId, orderId, status)
                .onSuccess { loadOrders(workspaceId) }
        }
    }

    fun deleteOrder(workspaceId: String, orderId: String) {
        viewModelScope.launch {
            repository.deleteOrder(workspaceId, orderId)
                .onSuccess { loadOrders(workspaceId) }
        }
    }
}
