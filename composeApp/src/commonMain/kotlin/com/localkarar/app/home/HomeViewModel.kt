package com.localkarar.app.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.DashboardResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Content(val data: DashboardResponse) : HomeUiState()
    data class Error(val message: String, val isAuthError: Boolean = false) : HomeUiState()
}

class HomeViewModel(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard(isRefresh: Boolean = false) {
        if (isRefresh) {
            _isRefreshing.value = true
        } else {
            _uiState.value = HomeUiState.Loading
        }

        viewModelScope.launch {
            val result = repository.getDashboard()
            
            _isRefreshing.value = false
            
            result.onSuccess { data ->
                _uiState.value = HomeUiState.Content(data)
            }.onFailure { exception ->
                val errorMsg = exception.message ?: "Bilinmeyen bir hata oluştu"
                val isAuthError = exception is com.localkarar.app.network.ApiError.Unauthorized || errorMsg == "UNAUTHORIZED"
                
                // If it's a refresh failure but we already have content, we might not want to destroy the view.
                // For simplicity here, we'll set the error state.
                _uiState.value = HomeUiState.Error(
                    if (isAuthError) "Oturum süreniz doldu." else "Bağlantı hatası veya sunucuya ulaşılamıyor.",
                    isAuthError
                )
            }
        }
    }
}
