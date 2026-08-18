package com.localkarar.app.courses

import com.localkarar.app.network.dto.CourseDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

sealed class CoursesUiState {
    object Loading : CoursesUiState()
    data class Content(val courses: List<CourseDto>) : CoursesUiState()
    data class Error(val message: String) : CoursesUiState()
}

class CoursesViewModel(private val repository: CourseRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<CoursesUiState>(CoursesUiState.Loading)
    val uiState: StateFlow<CoursesUiState> = _uiState.asStateFlow()

    init {
        loadCourses()
    }

    fun loadCourses() {
        _uiState.value = CoursesUiState.Loading
        viewModelScope.launch {
            val result = repository.getCourses(page = 1, pageSize = 50)
            result.onSuccess { response ->
                _uiState.value = CoursesUiState.Content(response.courses)
            }.onFailure { e ->
                _uiState.value = CoursesUiState.Error(e.message ?: "Failed to load courses")
            }
        }
    }
}
