package com.localkarar.app.ui.screens.courses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.localkarar.app.courses.CoursesUiState
import com.localkarar.app.courses.CoursesViewModel
import com.localkarar.app.courses.CoursesStateData
import com.localkarar.app.network.dto.CourseDto
import com.localkarar.app.network.dto.DashboardEnrollmentDto
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
    val activeView by viewModel.activeView.collectAsState()
    var showFilterBar by remember { mutableStateOf(false) }

    val title = if (activeView == CoursesViewModel.ActiveView.ENROLLMENTS) "Kayıtlarım" else "Kurslar"
    
    LkPageLayout(
        title = title
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space2),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (activeView == CoursesViewModel.ActiveView.CATALOG) {
                    TextButton(onClick = { showFilterBar = !showFilterBar }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtrele", tint = LkPrimary)
                        Spacer(Modifier.width(4.dp))
                        Text("Filtrele", style = LkTypography.getBodyStrong(), color = LkPrimary)
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                
                TextButton(onClick = {
                    if (activeView == CoursesViewModel.ActiveView.ENROLLMENTS) {
                        viewModel.setActiveView(CoursesViewModel.ActiveView.CATALOG)
                    } else {
                        viewModel.setActiveView(CoursesViewModel.ActiveView.ENROLLMENTS)
                    }
                }) {
                    Text(if (activeView == CoursesViewModel.ActiveView.ENROLLMENTS) "Tüm kurslar" else "Kayıtlarım", style = LkTypography.getBodyStrong(), color = LkPrimary)
                }
            }
            when (val state = uiState) {
                is CoursesUiState.Loading -> LkLoadingState()
                is CoursesUiState.Error -> LkErrorState(
                    message = state.message,
                    onRetry = { viewModel.resetFilters() }
                )
                is CoursesUiState.Content -> {
                    if (activeView == CoursesViewModel.ActiveView.ENROLLMENTS) {
                        EnrollmentsView(state.data, onNavigateToCourseDetail)
                    } else {
                        CatalogView(state.data, viewModel, showFilterBar, onNavigateToCourseDetail)
                    }
                }
            }
        }
    }
}

@Composable
fun CatalogView(
    data: CoursesStateData,
    viewModel: CoursesViewModel,
    showFilterBar: Boolean,
    onNavigateToCourseDetail: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = LkSpacing.Space6)
    ) {
        if (showFilterBar) {
            item {
                FilterBar(data, viewModel)
                Spacer(modifier = Modifier.height(LkSpacing.Space4))
            }
        }
        
        // Active Path Hero
        item {
            ActivePathHero(data, onNavigateToCourseDetail)
            Spacer(modifier = Modifier.height(LkSpacing.Space6))
        }

        // Removed Category Chips from here (moved to FilterBar)

        // Course List
        if (data.courses.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(LkSpacing.Space6), contentAlignment = Alignment.Center) {
                    Text("Kurs bulunamadı", style = LkTypography.getBody(), color = LkTextSecondary)
                }
            }
        } else {
            items(data.courses) { course ->
                Box(modifier = Modifier.padding(horizontal = LkSpacing.Space4)) {
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

@Composable
fun EnrollmentsView(
    data: CoursesStateData,
    onNavigateToCourseDetail: (Int) -> Unit
) {
    if (data.enrollments.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Henüz kursa kayıt olmadın", style = LkTypography.getBody(), color = LkTextSecondary)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = LkSpacing.Space6, start = LkSpacing.Space4, end = LkSpacing.Space4)
        ) {
            items(data.enrollments) { enrollment ->
                EnrollmentCard(enrollment, onNavigateToCourseDetail)
                Spacer(modifier = Modifier.height(LkSpacing.Space4))
            }
        }
    }
}

/**
 * Kurslar sayfasinin ust karti.
 *
 * Aktif kurs varsa onu, yoksa katalogu tanitir.
 *
 * ⚠️ OGRENME YOLU BURADAN KALDIRILDI (03.09.2026, urun sahibi karari).
 * Yol, kutuphane Bilgi Nesnelerinin uzerine kuruluydu ve o icerik
 * urunden cikarildi; webde de sayfasi silindi. Urunun ogrenme yuzeyi
 * 38 kanonik kurs.
 */
@Composable
fun ActivePathHero(
    data: CoursesStateData,
    onNavigateToCourseDetail: (Int) -> Unit
) {
    val activeCourse = data.enrollments.firstOrNull { it.status == "in_progress" }

    Surface(
        color = LkSurfacePanel,
        shape = LkShapes.MD,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = LkSpacing.Space4)
            .border(1.dp, LkLineSoft, LkShapes.MD)
    ) {
        Column(modifier = Modifier.padding(LkSpacing.Space5)) {
            val label = if (activeCourse != null) "AKTİF ÖĞRENME" else "KURS KATALOĞU"
            val title = activeCourse?.courseTitle ?: "İşletmeni güçlendiren uygulamalı kurslar"
            val progress = activeCourse?.progress ?: 0

            Text(label, style = LkTypography.getMicro().copy(fontWeight = FontWeight.Bold), color = LkTextSecondary)
            Spacer(modifier = Modifier.height(LkSpacing.Space2))
            Text(title, style = LkTypography.getSectionTitle(), color = LkTextPrimary)

            /* Ders sayisi. Web de 0 iken yazmiyor (CoursesPage.jsx:341). */
            if (activeCourse != null && activeCourse.courseLessonCount > 0) {
                Spacer(modifier = Modifier.height(LkSpacing.Space1))
                Text(
                    "${activeCourse.courseLessonCount} ders",
                    style = LkTypography.getMetadata(),
                    color = LkTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(LkSpacing.Space4))

            if (activeCourse != null) {
                LinearProgressIndicator(
                    progress = progress / 100f,
                    color = LkPrimary,
                    backgroundColor = LkSurfaceSunken,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(LkShapes.FULL)
                )
                Spacer(modifier = Modifier.height(LkSpacing.Space2))
                Text("%$progress tamamlandı", style = LkTypography.getMetadata(), color = LkTextSecondary)
            }

            Spacer(modifier = Modifier.height(LkSpacing.Space4))

            Button(
                onClick = { activeCourse?.let { onNavigateToCourseDetail(it.courseId) } },
                colors = ButtonDefaults.buttonColors(backgroundColor = LkSurfaceSunken),
                shape = LkShapes.FULL,
                elevation = ButtonDefaults.elevation(0.dp)
            ) {
                Text(
                    if (activeCourse != null) "Derse devam et" else "Kursları keşfet",
                    style = LkTypography.getBodyStrong(),
                    color = LkTextPrimary
                )
                Spacer(modifier = Modifier.width(LkSpacing.Space2))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = LkTextPrimary)
            }
        }
    }
}

@Composable
fun FilterBar(data: CoursesStateData, viewModel: CoursesViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LkSurfacePanel)
            .padding(LkSpacing.Space4),
        verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
    ) {
        OutlinedTextField(
            value = data.search,
            onValueChange = { viewModel.setSearch(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Kurs ara...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(backgroundColor = LkSurfaceCanvas)
        )
        
        val scrollState = rememberScrollState()
        Text("Kategoriler", style = LkTypography.getBodyStrong(), color = LkTextSecondary)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)
        ) {
            CategoryChip(text = "Tümü", isSelected = data.category.isEmpty(), onClick = { viewModel.setCategory("") })
            data.categories.forEach { cat ->
                CategoryChip(text = cat, isSelected = data.category == cat, onClick = { viewModel.setCategory(cat) })
            }
        }
        
        Text("Seviye", style = LkTypography.getBodyStrong(), color = LkTextSecondary)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)
        ) {
            val levels = listOf("" to "Tümü", "beginner" to "Başlangıç", "intermediate" to "Orta", "advanced" to "İleri")
            levels.forEach { (value, label) ->
                CategoryChip(text = label, isSelected = data.level == value, onClick = { viewModel.setLevel(value) })
            }
        }
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { viewModel.resetFilters() }) {
                Text("Temizle", style = LkTypography.getBodyStrong(), color = LkTextSecondary)
            }
        }
    }
}

@Composable
fun CategoryChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(LkShapes.FULL)
            .background(if (isSelected) LkPrimary else LkSurfacePanel)
            .border(1.dp, if (isSelected) LkPrimary else LkLineSoft, LkShapes.FULL)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = LkTypography.getBodyStrong(),
            color = if (isSelected) LkSurfaceCanvas else LkTextPrimary
        )
    }
}

@Composable
fun EnrollmentCard(
    enrollment: DashboardEnrollmentDto,
    onNavigateToCourseDetail: (Int) -> Unit
) {
    Surface(
        color = LkSurfacePanel,
        shape = LkShapes.MD,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, LkLineSoft, LkShapes.MD)
            .clickable { onNavigateToCourseDetail(enrollment.courseId) }
    ) {
        Column(modifier = Modifier.padding(LkSpacing.Space5)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    enrollment.courseTitle,
                    style = LkTypography.getCardTitle(),
                    color = LkTextPrimary,
                    modifier = Modifier.weight(1f)
                )
                
                val statusText = when(enrollment.status) {
                    "completed" -> "Tamamlandı"
                    "in_progress" -> "Devam ediyor"
                    else -> "Başlanmadı"
                }
                
                Box(modifier = Modifier.background(LkSurfaceSunken, LkShapes.FULL).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(statusText, style = LkTypography.getMicro(), color = LkTextSecondary)
                }
            }
            
            Spacer(modifier = Modifier.height(LkSpacing.Space3))
            
            Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)) {
                if (enrollment.courseCategory != null) {
                    Box(modifier = Modifier.background(LkSurfaceSunken, LkShapes.SM).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(enrollment.courseCategory, style = LkTypography.getMicro(), color = LkPrimary)
                    }
                }
                if (enrollment.courseLevel != null) {
                    Box(modifier = Modifier.background(LkSurfaceSunken, LkShapes.SM).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(enrollment.courseLevel, style = LkTypography.getMicro(), color = LkTextSecondary)
                    }
                }
                /* Ders sayisi. Web de 0 iken YAZMIYOR (EnrollmentsPage.jsx:77);
                   "0 ders" bilgi degil, gurultu. */
                if (enrollment.courseLessonCount > 0) {
                    Box(modifier = Modifier.background(LkSurfaceSunken, LkShapes.SM).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(
                            "${enrollment.courseLessonCount} ders",
                            style = LkTypography.getMicro(),
                            color = LkTextSecondary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(LkSpacing.Space4))
            
            LinearProgressIndicator(
                progress = enrollment.progress / 100f,
                color = LkPrimary,
                backgroundColor = LkSurfaceSunken,
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(LkShapes.FULL)
            )
        }
    }
}
