package com.localkarar.app.workspaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.CreateProductRequestDto
import com.localkarar.app.network.dto.ProductDto
import com.localkarar.app.network.dto.UpdateProductRequestDto
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

    private val _selectedCategory = MutableStateFlow("Tümü")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun loadProducts(workspaceId: String) {
        if (workspaceId.isBlank()) return
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            repository.getProducts(workspaceId, _selectedCategory.value, _searchQuery.value)
                .onSuccess { resp ->
                    _products.value = resp.products
                    _isLoading.value = false
                }
                .onFailure { err ->
                    _error.value = err.message ?: "Ürünler yüklenemedi."
                    _isLoading.value = false
                }
        }
    }

    fun setCategoryFilter(workspaceId: String, category: String) {
        _selectedCategory.value = category
        loadProducts(workspaceId)
    }

    fun setSearchQuery(workspaceId: String, query: String) {
        _searchQuery.value = query
        loadProducts(workspaceId)
    }

    fun createProduct(workspaceId: String, request: CreateProductRequestDto, onComplete: () -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.createProduct(workspaceId, request)
                .onSuccess {
                    loadProducts(workspaceId)
                    onComplete()
                }
                .onFailure { err ->
                    _error.value = err.message ?: "Ürün eklenemedi."
                    _isLoading.value = false
                }
        }
    }

    fun updateProduct(workspaceId: String, productId: String, request: UpdateProductRequestDto, onComplete: () -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.updateProduct(workspaceId, productId, request)
                .onSuccess {
                    loadProducts(workspaceId)
                    onComplete()
                }
                .onFailure { err ->
                    _error.value = err.message ?: "Ürün güncellenemedi."
                    _isLoading.value = false
                }
        }
    }

    fun deleteProduct(workspaceId: String, productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(workspaceId, productId)
                .onSuccess { loadProducts(workspaceId) }
        }
    }
}
