package com.localkarar.app.courses

import com.localkarar.app.network.dto.CourseDto
import com.localkarar.app.network.dto.DashboardEnrollmentDto
import com.localkarar.app.home.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class CoursesStateData(
    val courses: List<CourseDto> = emptyList(),
    val totalPages: Int = 1,
    val total: Int = 0,
    val enrollments: List<DashboardEnrollmentDto> = emptyList(),
    val categories: List<String> = emptyList(),
    // Current Filters
    val search: String = "",
    val category: String = "",
    val level: String = "",
    val page: Int = 1
)

sealed class CoursesUiState {
    object Loading : CoursesUiState()
    data class Content(val data: CoursesStateData) : CoursesUiState()
    data class Error(val message: String) : CoursesUiState()
}

class CoursesViewModel(
    private val repository: CourseRepository,
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CoursesUiState>(CoursesUiState.Loading)
    val uiState: StateFlow<CoursesUiState> = _uiState.asStateFlow()

    private var currentData = CoursesStateData()
    
    enum class ActiveView {
        CATALOG, ENROLLMENTS
    }
    private val _activeView = MutableStateFlow(ActiveView.CATALOG)
    val activeView: StateFlow<ActiveView> = _activeView.asStateFlow()

    init {
        loadAll()
    }

    fun setActiveView(view: ActiveView) {
        _activeView.value = view
    }

    fun setPage(page: Int) {
        currentData = currentData.copy(page = page)
        loadCoursesOnly()
    }

    fun setCategory(category: String) {
        currentData = currentData.copy(category = category, page = 1)
        loadCoursesOnly()
    }

    fun setSearch(search: String) {
        currentData = currentData.copy(search = search, page = 1)
        loadCoursesOnly()
    }

    fun setLevel(level: String) {
        currentData = currentData.copy(level = level, page = 1)
        loadCoursesOnly()
    }
    
    fun resetFilters() {
        currentData = currentData.copy(page = 1, category = "", search = "", level = "")
        loadCoursesOnly()
    }

    private fun loadAll() {
        _uiState.value = CoursesUiState.Loading
        viewModelScope.launch {
            try {
                coroutineScope {
                    val coursesDeferred = async { 
                        repository.getCourses(
                            page = currentData.page, 
                            pageSize = 6,
                            search = currentData.search,
                            category = currentData.category,
                            level = currentData.level
                        ) 
                    }
                    val categoriesDeferred = async { repository.getCategories() }
                    val enrollmentsDeferred = async { repository.getMyEnrollments() }
                    val dashboardDeferred = async { dashboardRepository.getDashboard() }

                    val coursesResult = coursesDeferred.await()
                    val categoriesResult = categoriesDeferred.await()
                    val enrollmentsResult = enrollmentsDeferred.await()
                    val dashboardResult = dashboardDeferred.await()

                    val courses = coursesResult.getOrNull()?.courses ?: emptyList()
                    val categories = categoriesResult.getOrNull() ?: emptyList()

                    currentData = currentData.copy(
                        courses = courses,
                        totalPages = coursesResult.getOrNull()?.totalPages ?: 1,
                        total = coursesResult.getOrNull()?.total ?: 0,
                        categories = categories,
                        enrollments = enrollmentsResult.getOrNull()?.enrollments ?: emptyList(),
                    )
                    _uiState.value = CoursesUiState.Content(currentData)
                }
            } catch (e: Exception) {
                _uiState.value = CoursesUiState.Error(e.message ?: "Failed to load data")
            }
        }
    }

    private fun loadCoursesOnly() {
        _uiState.value = CoursesUiState.Loading
        viewModelScope.launch {
            val result = repository.getCourses(
                page = currentData.page, 
                pageSize = 6,
                search = currentData.search,
                category = currentData.category,
                level = currentData.level
            )
            result.onSuccess { response ->
                currentData = currentData.copy(
                    courses = response.courses,
                    totalPages = response.totalPages,
                    total = response.total
                )
                _uiState.value = CoursesUiState.Content(currentData)
            }.onFailure { e ->
                _uiState.value = CoursesUiState.Error(e.message ?: "Failed to filter courses")
            }
        }
    }
}
