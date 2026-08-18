package com.localkarar.app.ui.screens.courses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.courses.CoursesUiState
import com.localkarar.app.courses.CoursesViewModel
import com.localkarar.app.network.dto.CourseDto
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.components.LkCourseCard
import com.localkarar.app.ui.theme.*

@Composable
fun CoursesScreen(
    viewModel: CoursesViewModel,
    onNavigateToCourseDetail: (Int) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LkPageLayout(title = "Katalog") {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                when (val state = uiState) {
                    is CoursesUiState.Loading -> LkLoadingState()
                    is CoursesUiState.Error -> LkErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadCourses() }
                    )
                    is CoursesUiState.Content -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = LkSpacing.Space6)
                        ) {
                            items(state.courses) { course ->
                                LkCourseCard(
                                    title = course.title,
                                    lessonCount = course.lessonCount ?: 0,
                                    estimatedMinutes = course.estimatedMinutes,
                                    progress = course.enrollment?.progress,
                                    level = course.level,
                                    onClick = { onNavigateToCourseDetail(course.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
