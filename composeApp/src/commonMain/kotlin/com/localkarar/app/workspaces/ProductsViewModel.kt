package com.localkarar.app.workspaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.ProductDto
import com.localkarar.app.network.dto.UpdateProductSettingsRequestDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductsViewModel(
    private val repository: WorkspaceRepository
) : ViewModel() {

    private val _products = MutableStateFlow<List<ProductDto>>(emptyList())
    val products: StateFlow<List<ProductDto>> = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _lastSyncedAt = MutableStateFlow<String?>(null)
    val lastSyncedAt: StateFlow<String?> = _lastSyncedAt.asStateFlow()

    private val _integrationConnected = MutableStateFlow(true)
    val integrationConnected: StateFlow<Boolean> = _integrationConnected.asStateFlow()

    // Filters
    private val _selectedProvider = MutableStateFlow<String?>(null)
    val selectedProvider: StateFlow<String?> = _selectedProvider.asStateFlow()

    private val _selectedOnSale = MutableStateFlow<Boolean?>(null)
    val selectedOnSale: StateFlow<Boolean?> = _selectedOnSale.asStateFlow()

    private val _selectedStockFilter = MutableStateFlow<String?>(null)
    val selectedStockFilter: StateFlow<String?> = _selectedStockFilter.asStateFlow()

    private val _selectedWindow = MutableStateFlow("30d")
    val selectedWindow: StateFlow<String> = _selectedWindow.asStateFlow()

    private val _selectedSortBy = MutableStateFlow("default")
    val selectedSortBy: StateFlow<String> = _selectedSortBy.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun loadProducts(workspaceId: String) {
        if (workspaceId.isBlank()) return
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            repository.getProducts(
                workspaceId = workspaceId,
                provider = _selectedProvider.value,
                onSale = _selectedOnSale.value,
                stockFilter = _selectedStockFilter.value,
                window = _selectedWindow.value,
                sortBy = _selectedSortBy.value,
                query = _searchQuery.value
            )
                .onSuccess { resp ->
                    _products.value = resp.products
                    _lastSyncedAt.value = resp.lastSyncedAt
                    _integrationConnected.value = resp.integrationConnected
                    _isLoading.value = false
                }
                .onFailure { err ->
                    _error.value = err.message ?: "Ürünler yüklenemedi."
                    _isLoading.value = false
                }
        }
    }

    fun setProviderFilter(workspaceId: String, provider: String?) {
        _selectedProvider.value = provider
        loadProducts(workspaceId)
    }

    fun setOnSaleFilter(workspaceId: String, onSale: Boolean?) {
        _selectedOnSale.value = onSale
        loadProducts(workspaceId)
    }

    fun setStockFilter(workspaceId: String, stockFilter: String?) {
        _selectedStockFilter.value = stockFilter
        loadProducts(workspaceId)
    }

    fun setPerformanceWindow(workspaceId: String, window: String) {
        _selectedWindow.value = window
        loadProducts(workspaceId)
    }

    fun setSortBy(workspaceId: String, sortBy: String) {
        _selectedSortBy.value = sortBy
        loadProducts(workspaceId)
    }

    fun setSearchQuery(workspaceId: String, query: String) {
        _searchQuery.value = query
        loadProducts(workspaceId)
    }

    fun saveLocalSettings(
        workspaceId: String,
        productId: String,
        internalNote: String?,
        tags: List<String>? = null,
        lowStockThresholdOverride: Int?,
        isFavorite: Boolean?,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.updateProductSettings(
                workspaceId,
                productId,
                UpdateProductSettingsRequestDto(
                    workspaceId = workspaceId,
                    internalNote = internalNote,
                    tags = tags,
                    lowStockThresholdOverride = lowStockThresholdOverride,
                    isFavorite = isFavorite
                )
            ).onSuccess {
                loadProducts(workspaceId)
                onComplete()
            }
        }
    }
}
