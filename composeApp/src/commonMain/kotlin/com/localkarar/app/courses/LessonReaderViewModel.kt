package com.localkarar.app.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.network.dto.CourseDetailDto
import com.localkarar.app.network.dto.LessonDetailDto
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LessonReaderUiState {
    object Loading : LessonReaderUiState()
    data class Content(val course: CourseDetailDto, val lesson: LessonDetailDto) : LessonReaderUiState()
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
            val courseDeferred = async { repository.getCourseDetail(courseId) }
            val lessonDeferred = async { repository.getLessonDetail(courseId, lessonId) }
            
            val courseResult = courseDeferred.await()
            val lessonResult = lessonDeferred.await()
            
            if (courseResult.isSuccess && lessonResult.isSuccess) {
                _uiState.value = LessonReaderUiState.Content(
                    course = courseResult.getOrThrow().course,
                    lesson = lessonResult.getOrThrow().lesson
                )
                // Mark lesson as viewed in background
                repository.markLessonViewed(lessonId)
            } else {
                val error = courseResult.exceptionOrNull() ?: lessonResult.exceptionOrNull()
                _uiState.value = LessonReaderUiState.Error(error?.message ?: "Failed to load lesson")
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
