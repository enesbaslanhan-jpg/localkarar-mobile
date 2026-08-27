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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)
            ) {
                if (course.level != null) {
                    Box(
                        modifier = Modifier
                            .background(LkPrimary.copy(alpha = 0.1f), LkShapes.SM)
                            .padding(horizontal = LkSpacing.Space2, vertical = 2.dp)
                    ) {
                        Text(
                            text = course.level,
                            style = LkTypography.getMetadata(),
                            color = LkPrimary
                        )
                    }
                }
                Text(
                    text = "${course.lessonCount ?: course.lessons.size} ders",
                    style = LkTypography.getMetadata(),
                    color = LkTextSecondary
                )
            }
            
            if (course.archived == true) {
                Spacer(modifier = Modifier.height(LkSpacing.Space4))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LkWarning.copy(alpha = 0.1f), LkShapes.SM)
                        .padding(LkSpacing.Space3)
                ) {
                    Text(
                        text = "Bu kurs artık aktif katalogda yer almıyor. İçeriğe ve ilerlemene erişmeye devam edebilirsin.",
                        style = LkTypography.getBody(),
                        color = LkWarning
                    )
                }
            }

            Spacer(modifier = Modifier.height(LkSpacing.Space6))
            
            val totalLessons = course.lessons.size
            val doneLessons = course.lessons.count { it.progress?.status == "completed" }
            val coursePercent = if (totalLessons > 0) ((doneLessons.toFloat() / totalLessons) * 100).toInt() else 0

            Text(text = "İlerleme: %$coursePercent", style = LkTypography.getBodyStrong(), color = LkTextPrimary)
            Spacer(modifier = Modifier.height(LkSpacing.Space2))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(LkSurfacePanel)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(coursePercent / 100f)
                        .height(8.dp)
                        .background(LkPrimary)
                )
            }
            Spacer(modifier = Modifier.height(LkSpacing.Space1))
            Text(
                text = "$doneLessons / $totalLessons ders tamamlandı",
                style = LkTypography.getMetadata(),
                color = LkTextSecondary
            )
            
            Spacer(modifier = Modifier.height(LkSpacing.Space6))
            Text(text = "Dersler", style = LkTypography.getCardTitle(), color = LkTextPrimary)
            Spacer(modifier = Modifier.height(LkSpacing.Space2))
        }

        items(course.lessons) { lesson ->
            LessonListItem(
                lesson = lesson,
                onClick = { if (!lesson.isLocked) onLessonClick(lesson.id) }
            )
        }
    }
}

@Composable
fun LessonListItem(lesson: LessonSummaryDto, onClick: () -> Unit) {
    val isCompleted = lesson.progress?.status == "completed"
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LkSurfacePanel, LkShapes.SM)
            .clickable(enabled = !lesson.isLocked, onClick = onClick)
            .padding(LkSpacing.Space4)
            .alpha(if (lesson.isLocked) 0.5f else 1f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${lesson.order}",
            style = LkTypography.getBodyStrong(),
            color = LkTextSecondary,
            modifier = Modifier.width(24.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = lesson.title,
                style = LkTypography.getBodyStrong(),
                color = LkTextPrimary
            )
            val minutes = lesson.estimatedMinutes?.let { "$it dk" } ?: ""
            val progressText = if (lesson.progress?.overallPercent != null && lesson.progress.overallPercent > 0) " · %${lesson.progress.overallPercent}" else ""
            if (minutes.isNotEmpty() || progressText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(LkSpacing.Space1))
                Text(
                    text = "$minutes$progressText",
                    style = LkTypography.getMetadata(),
                    color = LkTextSecondary
                )
            }
        }
        
        Spacer(modifier = Modifier.width(LkSpacing.Space4))
        
        if (lesson.isLocked) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Kilitli",
                tint = LkTextSecondary,
                modifier = Modifier.size(16.dp)
            )
        } else if (isCompleted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Tamamlandı",
                tint = LkSuccess,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

