package com.localkarar.app.ui.screens.courses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.localkarar.app.courses.CourseDetailUiState
import com.localkarar.app.courses.CourseDetailViewModel
import com.localkarar.app.network.dto.CourseDetailDto
import com.localkarar.app.network.dto.LessonSummaryDto
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkPageLayout

import com.localkarar.app.ui.theme.*

@Composable
fun CourseDetailScreen(
    viewModel: CourseDetailViewModel,
    onNavigateToLesson: (Int, Int) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LkPageLayout(title = "Eğitim Detayı", onBack = onBack) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is CourseDetailUiState.Loading -> LkLoadingState()
                is CourseDetailUiState.Error -> LkErrorState(
                    message = state.message,
                    onRetry = { viewModel.loadCourseDetail() }
                )
                is CourseDetailUiState.Content -> {
                    CourseDetailContent(
                        course = state.course,
                        onLessonClick = { lessonId -> onNavigateToLesson(state.course.id, lessonId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CourseDetailContent(
    course: CourseDetailDto,
    onLessonClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(LkSpacing.PadPanel),
        verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
    ) {
        item {
            Text(text = course.title, style = LkTypography.getSectionTitle(), color = LkTextPrimary)
            Spacer(modifier = Modifier.height(LkSpacing.Space2))
            if (course.description != null) {
                Text(text = course.description, style = LkTypography.getBody(), color = LkTextSecondary)
            }
            Spacer(modifier = Modifier.height(LkSpacing.Space6))
            
            if (course.enrollment != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(LkSurfacePanel)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(course.enrollment.progress / 100f)
                            .height(8.dp)
                            .background(LkPrimary)
                    )
                }
                Spacer(modifier = Modifier.height(LkSpacing.Space2))
                Text(
                    text = "Tamamlama: %",
                    style = LkTypography.getMetadata(),
                    color = LkTextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(LkSpacing.Space6))
            Text(text = "Dersler", style = LkTypography.getCardTitle(), color = LkTextPrimary)
            Spacer(modifier = Modifier.height(LkSpacing.Space2))
        }

        items(course.lessons) { lesson ->
            LessonListItem(
                lesson = lesson,
                onClick = { onLessonClick(lesson.id) }
            )
        }
    }
}

@Composable
fun LessonListItem(lesson: LessonSummaryDto, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LkSurfacePanel, LkShapes.SM)
            .clickable(onClick = onClick)
            .padding(LkSpacing.Space4),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isCompleted = lesson.progress?.status == "completed"
        
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(if (isCompleted) LkSuccess.copy(alpha = 0.1f) else LkSurfaceSunken, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = if (isCompleted) LkSuccess else LkTextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(LkSpacing.Space4))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = ". ",
                style = LkTypography.getBodyStrong(),
                color = LkTextPrimary
            )
            if (lesson.progress != null && lesson.progress.status != "not_started") {
                Spacer(modifier = Modifier.height(LkSpacing.Space1))
                Text(
                    text = "İlerleme: %",
                    style = LkTypography.getMetadata(),
                    color = LkTextSecondary
                )
            }
        }
    }
}

