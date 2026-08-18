package com.localkarar.app.workspaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.BusinessRecordDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RecordsUiState {
    object Loading : RecordsUiState()
    data class Content(
        val records: List<BusinessRecordDto> = emptyList(),
        val isRefreshing: Boolean = false
    ) : RecordsUiState()
    data class Error(val message: String) : RecordsUiState()
}

class RecordsViewModel(
    private val workspaceId: String,
    private val repository: WorkspaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecordsUiState>(RecordsUiState.Loading)
    val uiState: StateFlow<RecordsUiState> = _uiState.asStateFlow()

    private var currentStatus: String? = null
    private var currentType: String? = null

    init {
        load()
    }

    fun setFilter(status: String?, type: String?) {
        currentStatus = status
        currentType = type
        load()
    }

    fun load() {
        _uiState.value = RecordsUiState.Loading
        viewModelScope.launch {
            val result = repository.getRecords(
                workspaceId = workspaceId,
                status = currentStatus,
                type = currentType,
                limit = 200
            )
            if (result.isSuccess) {
                _uiState.value = RecordsUiState.Content(result.getOrThrow().records)
            } else {
                _uiState.value = RecordsUiState.Error(
                    result.exceptionOrNull()?.message ?: "Kayıtlar yüklenemedi."
                )
            }
        }
    }

    fun refresh() {
        load()
    }
}