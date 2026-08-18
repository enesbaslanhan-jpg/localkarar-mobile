package com.localkarar.app.ui.screens.courses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.courses.LessonReaderUiState
import com.localkarar.app.courses.LessonReaderViewModel
import com.localkarar.app.network.dto.LessonDetailDto
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.components.MarkdownViewer
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.theme.*

@Composable
fun LessonReaderScreen(
    viewModel: LessonReaderViewModel,
    onNavigateToLesson: (Int, Int) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LkPageLayout(title = "Ders", onBack = onBack) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is LessonReaderUiState.Loading -> LkLoadingState()
                is LessonReaderUiState.Error -> LkErrorState(
                    message = state.message,
                    onRetry = { viewModel.loadLesson() }
                )
                is LessonReaderUiState.Content -> {
                    LessonContent(
                        lesson = state.lesson,
                        onNavigateToLesson = onNavigateToLesson,
                        onComplete = { viewModel.markLessonComplete { onBack() } }
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonContent(
    lesson: LessonDetailDto,
    onNavigateToLesson: (Int, Int) -> Unit,
    onComplete: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(LkSpacing.PadPanel),
        verticalArrangement = Arrangement.spacedBy(LkSpacing.Space6)
    ) {
        item {
            Text(text = lesson.title, style = LkTypography.getPageTitle(), color = LkTextPrimary)
            Spacer(modifier = Modifier.height(LkSpacing.Space4))
            
val ko = lesson.knowledgeObject
            if (ko != null && !ko.content.isNullOrEmpty()) {
                val cleanedContent = removeDuplicateH1(ko.content, lesson.title)
                MarkdownViewer(content = cleanedContent)
}
        }
        
        item {
            Spacer(modifier = Modifier.height(LkSpacing.Space8))
            Divider(color = LkLineSoft, thickness = 1.dp)
            Spacer(modifier = Modifier.height(LkSpacing.Space8))

            LkButton(
                text = "Dersi Tamamla",
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(LkSpacing.Space6))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (lesson.prevLesson != null) {
                    LkButton(
                        text = "Önceki Ders", 
                        variant = LkButtonVariant.SECONDARY,
                        onClick = { onNavigateToLesson(lesson.courseId, lesson.prevLesson.id) }
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                
                if (lesson.nextLesson != null) {
                    LkButton(
                        text = "Sonraki Ders", 
                        variant = LkButtonVariant.SECONDARY,
                        onClick = { onNavigateToLesson(lesson.courseId, lesson.nextLesson.id) }
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
            }
        }
    }
}


private fun removeDuplicateH1(content: String, title: String): String {
    val lines = content.lines()
    for (i in lines.indices) {
        val line = lines[i].trim()
        if (line.isEmpty()) continue
        if (line.startsWith("# ") && !line.startsWith("## ")) {
            val h1Text = line.removePrefix("#").trim()
            if (h1Text.equals(title.trim(), ignoreCase = true)) {
                val newLines = lines.toMutableList()
                newLines.removeAt(i)
                return newLines.joinToString("\n")
            }
            break
        } else {
            break
        }
    }
    return content
}


