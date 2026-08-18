package com.localkarar.app.courses

import com.localkarar.app.network.dto.LessonDetailDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

sealed class LessonReaderUiState {
    object Loading : LessonReaderUiState()
    data class Content(val lesson: LessonDetailDto) : LessonReaderUiState()
    data class Error(val message: String) : LessonReaderUiState()
}

class LessonReaderViewModel(
    private val repository: CourseRepository,
    private val courseId: Int,
    private val lessonId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow<LessonReaderUiState>(LessonReaderUiState.Loading)
    val uiState: StateFlow<LessonReaderUiState> = _uiState.asStateFlow()

    init {
        loadLesson()
    }

    fun loadLesson() {
        _uiState.value = LessonReaderUiState.Loading
        viewModelScope.launch {
            val result = repository.getLessonDetail(courseId, lessonId)
            result.onSuccess { response ->
                _uiState.value = LessonReaderUiState.Content(response.lesson)
                // Mark lesson as viewed in background
                repository.markLessonViewed(lessonId)
            }.onFailure { e ->
                _uiState.value = LessonReaderUiState.Error(e.message ?: "Failed to load lesson")
            }
        }
    }

    fun markLessonComplete(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.markReadingComplete(courseId, lessonId)
            // Reload or just call back
            onComplete()
        }
    }
}
