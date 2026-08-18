package com.localkarar.app.courses

import com.localkarar.app.network.dto.CourseDetailDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

sealed class CourseDetailUiState {
    object Loading : CourseDetailUiState()
    data class Content(val course: CourseDetailDto) : CourseDetailUiState()
    data class Error(val message: String) : CourseDetailUiState()
}

class CourseDetailViewModel(
    private val repository: CourseRepository,
    private val courseId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow<CourseDetailUiState>(CourseDetailUiState.Loading)
    val uiState: StateFlow<CourseDetailUiState> = _uiState.asStateFlow()

    init {
        loadCourseDetail()
    }

    fun loadCourseDetail() {
        _uiState.value = CourseDetailUiState.Loading
        viewModelScope.launch {
            val result = repository.getCourseDetail(courseId)
            result.onSuccess { response ->
                if (response.course.enrollment == null) {
                    val enrollResult = repository.enrollCourse(courseId)
                    if (enrollResult.isSuccess) {
                        val reloadedResult = repository.getCourseDetail(courseId)
                        reloadedResult.onSuccess { reloadedResponse ->
                            _uiState.value = CourseDetailUiState.Content(reloadedResponse.course)
                        }.onFailure {
                            _uiState.value = CourseDetailUiState.Content(response.course)
                        }
                    } else {
                        _uiState.value = CourseDetailUiState.Content(response.course)
                    }
                } else {
                    _uiState.value = CourseDetailUiState.Content(response.course)
                }
            }.onFailure { e ->
                _uiState.value = CourseDetailUiState.Error(e.message ?: "Failed to load course details")
            }
        }
    }
}
