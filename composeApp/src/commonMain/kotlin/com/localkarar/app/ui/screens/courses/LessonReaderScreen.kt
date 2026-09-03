package com.localkarar.app.ui.screens.courses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.localkarar.app.courses.LessonReaderUiState
import com.localkarar.app.courses.LessonReaderViewModel
import com.localkarar.app.network.dto.CourseDetailDto
import com.localkarar.app.network.dto.LessonDetailDto
import com.localkarar.app.network.dto.KnowledgeObjectMetadataDto
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
    onOpenFormula: (String) -> Unit,
    onOpenModel: (String) -> Unit,
    onOpenDecisionTool: (String) -> Unit,
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
                        course = state.course,
                        lesson = state.lesson,
                        onNavigateToLesson = onNavigateToLesson,
                        onOpenFormula = onOpenFormula,
                        onOpenModel = onOpenModel,
                        onOpenDecisionTool = onOpenDecisionTool,
                        onComplete = { viewModel.markLessonComplete { onBack() } }
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonContent(
    course: CourseDetailDto,
    lesson: LessonDetailDto,
    onNavigateToLesson: (Int, Int) -> Unit,
    onOpenFormula: (String) -> Unit,
    onOpenModel: (String) -> Unit,
    onOpenDecisionTool: (String) -> Unit,
    onComplete: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(LkSpacing.PadPanel),
        verticalArrangement = Arrangement.spacedBy(LkSpacing.Space6)
    ) {
        item {
            val lessons = course.lessons
            val totalLessons = lessons.size
            val doneLessons = lessons.count { it.progress?.status == "completed" }
            val coursePercent = if (totalLessons > 0) ((doneLessons.toFloat() / totalLessons) * 100).toInt() else 0
            val lessonIndex = lessons.indexOfFirst { it.id == lesson.id }
            
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = course.title,
                    style = LkTypography.getMetadata(),
                    color = LkTextSecondary
                )
                Text(
                    text = "İlerleme %$coursePercent",
                    style = LkTypography.getMetadata(),
                    color = LkTextSecondary
                )
            }
            Spacer(modifier = Modifier.height(LkSpacing.Space3))
            
            // Progress Segments
            Row(
                modifier = Modifier.fillMaxWidth().height(4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                lessons.forEach { l ->
                    val isDone = l.progress?.status == "completed"
                    val isActive = l.id == lesson.id
                    val color = when {
                        isActive -> LkPrimary
                        isDone -> LkSuccess
                        else -> LkSurfaceSunken
                    }
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(color))
                }
            }
            Spacer(modifier = Modifier.height(LkSpacing.Space6))
            
            // Lesson Title
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Ders ${(lessonIndex + 1)}/$totalLessons",
                    style = LkTypography.getMetadata(),
                    color = LkTextSecondary
                )
                Text(
                    text = "${lesson.estimatedMinutes ?: 0} dakika",
                    style = LkTypography.getMetadata(),
                    color = LkTextSecondary
                )
            }
            Spacer(modifier = Modifier.height(LkSpacing.Space2))
            Text(text = lesson.title, style = LkTypography.getPageTitle(), color = LkTextPrimary)
            
            Spacer(modifier = Modifier.height(LkSpacing.Space6))
            Divider(color = LkLineSoft, thickness = 1.dp)
            Spacer(modifier = Modifier.height(LkSpacing.Space6))
            
            val kanonikBolumler = lesson.canonicalSections
            val ko = lesson.knowledgeObject
            if (ko != null) {
                val metadata = ko.metadata
                
                // Short Summary
                if (metadata != null && (metadata.summary != null || metadata.description != null)) {
                    val text = metadata.summary ?: metadata.description
                    if (!text.isNullOrEmpty()) {
                        Text(
                            text = text,
                            style = LkTypography.getBodyStrong(),
                            color = LkTextPrimary
                        )
                        Spacer(modifier = Modifier.height(LkSpacing.Space6))
                    }
                }
                
                /*
                 * GOVDE.
                 *
                 * Kanonik derste sunucu, 3./4./5. bolumleri ve satir ici
                 * `[ Hesaplamalar > ... ]` referanslarini govdeden CIKARIP
                 * yapisal olarak gonderiyor. Ham govdeyi basmak, ayni
                 * icerigi ikinci kez gostermek ve o referanslari kullaniciya
                 * DUZ METIN olarak sizdirmak demekti -- mobilde tam bu
                 * oluyordu.
                 */
                val govde = kanonikBolumler?.body ?: ko.content
                if (!govde.isNullOrEmpty()) {
                    MarkdownViewer(content = removeDuplicateH1(govde, lesson.title))
                    Spacer(modifier = Modifier.height(LkSpacing.Space6))
                }
                
                // Examples
                if (metadata?.examples?.isNotEmpty() == true) {
                    Text(text = "Örnekler", style = LkTypography.getCardTitle(), color = LkTextPrimary)
                    Spacer(modifier = Modifier.height(LkSpacing.Space2))
                    metadata.examples.forEach { example ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(LkSurfacePanel, LkShapes.SM)
                                .padding(LkSpacing.Space4)
                        ) {
                            Text(text = example, style = LkTypography.getBody(), color = LkTextPrimary)
                        }
                        Spacer(modifier = Modifier.height(LkSpacing.Space2))
                    }
                    Spacer(modifier = Modifier.height(LkSpacing.Space4))
                }
                
                // Steps
                if (metadata?.steps?.isNotEmpty() == true) {
                    Text(text = "Adımlar", style = LkTypography.getCardTitle(), color = LkTextPrimary)
                    Spacer(modifier = Modifier.height(LkSpacing.Space2))
                    metadata.steps.forEachIndexed { index, step ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = LkSpacing.Space2)) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(LkPrimary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "${index + 1}", style = LkTypography.getMetadata(), color = LkPrimary)
                            }
                            Spacer(modifier = Modifier.width(LkSpacing.Space3))
                            Text(text = step, style = LkTypography.getBody(), color = LkTextPrimary, modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(LkSpacing.Space4))
                }
                
                // Checklist
                if (metadata?.checklist?.isNotEmpty() == true) {
                    Text(text = "Kontrol Listesi", style = LkTypography.getCardTitle(), color = LkTextPrimary)
                    Spacer(modifier = Modifier.height(LkSpacing.Space2))
                    metadata.checklist.forEach { item ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = LkSpacing.Space2)) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(LkPrimary, CircleShape)
                                    .align(Alignment.CenterVertically)
                            )
                            Spacer(modifier = Modifier.width(LkSpacing.Space3))
                            Text(text = item, style = LkTypography.getBody(), color = LkTextPrimary, modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(LkSpacing.Space4))
                }
                
                // Formulas
                if (metadata?.formulas?.isNotEmpty() == true) {
                    Text(text = "Formüller / Metrikler", style = LkTypography.getCardTitle(), color = LkTextPrimary)
                    Spacer(modifier = Modifier.height(LkSpacing.Space2))
                    metadata.formulas.forEach { formula ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(LkWarning.copy(alpha = 0.05f), LkShapes.SM)
                                .padding(LkSpacing.Space4)
                        ) {
                            Text(text = formula, style = LkTypography.getBody(), color = LkWarning)
                        }
                        Spacer(modifier = Modifier.height(LkSpacing.Space2))
                    }
                    Spacer(modifier = Modifier.height(LkSpacing.Space4))
                }
                
                // Sources
                if (ko.sources.isNotEmpty()) {
                    Divider(color = LkLineSoft, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(LkSpacing.Space4))
                    Text(text = "Kaynaklar", style = LkTypography.getCardTitle(), color = LkTextSecondary)
                    Spacer(modifier = Modifier.height(LkSpacing.Space3))
                    ko.sources.forEach { sourceWrapper ->
                        val source = sourceWrapper.source
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = LkSpacing.Space2)) {
                            Text(text = source.title, style = LkTypography.getBodyStrong(), color = LkTextPrimary)
                            if (source.publisher != null) {
                                Text(text = source.publisher, style = LkTypography.getMetadata(), color = LkTextSecondary)
                            }
                            if (source.url != null) {
                                Text(
                                    text = "Kaynağa Git",
                                    style = LkTypography.getMetadata().copy(textDecoration = TextDecoration.Underline),
                                    color = LkPrimary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(LkSpacing.Space4))
                }
            }
            
            /*
             * KANONIK BOLUMLER — karar araci karti, hesaplama baglantilari
             * ve pratik bilgi kartlari. Yayimdaki 38 dersin tamami kanonik.
             */
            if (kanonikBolumler != null) {
                CanonicalSections(
                    sections = kanonikBolumler,
                    onOpenFormula = onOpenFormula,
                    onOpenModel = onOpenModel,
                    onOpenDecisionTool = onOpenDecisionTool
                )
            }

            /*
             * ESKI PRATIK BLOKLAR — yalniz KANONIK OLMAYAN derste.
             *
             * Web de ayni kosulu kullaniyor (CoursePlayerPage.jsx:474,
             * `!isCanonical &&`): kanonik derste ayni icerik yukaridaki
             * yapisal kartlarda zaten var, ikinci kez basmak duplikasyon.
             *
             * Uygulamada bu dal bugun HIC calismiyor: yayimdaki 38 dersin
             * hepsi kanonik (olculdu 03.09.2026). Yine de duruyor, cunku
             * kanonik olmayan bir ders yayimlanirsa bos ekran kalmasin.
             */
            if (kanonikBolumler == null && lesson.embeddedPracticeBlocks.isNotEmpty()) {
                Divider(color = LkLineSoft, thickness = 1.dp)
                Spacer(modifier = Modifier.height(LkSpacing.Space4))
                Text(text = "Pratik ve Uygulama", style = LkTypography.getCardTitle(), color = LkTextPrimary)
                Spacer(modifier = Modifier.height(LkSpacing.Space3))
                lesson.embeddedPracticeBlocks.forEach { block ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LkSurfacePanel, LkShapes.SM)
                            .padding(LkSpacing.Space4)
                    ) {
                        Column {
                            val typeLabel = when (block.type) {
                                "practical-card" -> "Pratik Kartı"
                                "decision-tool" -> "Karar Aracı"
                                "financial-model" -> "Finansal Model"
                                else -> "Uygulama"
                            }
                            Text(text = typeLabel, style = LkTypography.getMetadata(), color = LkPrimary)
                            Spacer(modifier = Modifier.height(LkSpacing.Space1))
                            Text(text = block.title, style = LkTypography.getBodyStrong(), color = LkTextPrimary)
                            if (block.description != null) {
                                Spacer(modifier = Modifier.height(LkSpacing.Space1))
                                Text(text = block.description, style = LkTypography.getBody(), color = LkTextSecondary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(LkSpacing.Space3))
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(LkSpacing.Space8))
            Divider(color = LkLineSoft, thickness = 1.dp)
            Spacer(modifier = Modifier.height(LkSpacing.Space8))

            val isDone = lesson.progress?.status == "completed"

            LkButton(
                text = if (isDone) "Ders Tamamlandı" else "Dersi Tamamla",
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth(),
                variant = if (isDone) LkButtonVariant.SECONDARY else LkButtonVariant.PRIMARY
            )

            Spacer(modifier = Modifier.height(LkSpacing.Space6))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (lesson.prevLesson != null) {
                    LkButton(
                        text = "Önceki", 
                        variant = LkButtonVariant.SECONDARY,
                        onClick = { onNavigateToLesson(lesson.courseId, lesson.prevLesson.id) }
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                
                if (lesson.nextLesson != null) {
                    LkButton(
                        text = "Sonraki", 
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


