package com.localkarar.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.localkarar.app.network.dto.*
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkMetricCard
import com.localkarar.app.ui.theme.*

@Composable
fun DashboardHeader(user: DashboardUserDto, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = LkSpacing.Space4),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(LkPrimary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.name.take(1).uppercase(),
                style = LkTypography.getSectionTitle(),
                color = LkOnPrimary
            )
        }
        Spacer(modifier = Modifier.width(LkSpacing.Space3))
        Column {
            Text(text = "Hoş Geldiniz,", style = LkTypography.getMetadata(), color = LkTextSecondary)
            Text(text = user.name, style = LkTypography.getSectionTitle(), color = LkTextPrimary)
        }
    }
}

@Composable
fun TodayInsightWidget(
    task: UpcomingTaskDto?,
    resumeItem: ResumeItemDto?,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LkSurfacePanel, LkShapes.MD)
            .padding(LkSpacing.PadPanel)
    ) {
        Text(text = "BUGÜN DİKKAT ET", style = LkTypography.getMetadata(), color = LkPrimary)
        Spacer(modifier = Modifier.height(LkSpacing.Space2))
        
        if (task != null) {
            Text(text = "Bekleyen Görev", style = LkTypography.getSectionTitle(), color = LkTextPrimary)
            Spacer(modifier = Modifier.height(LkSpacing.Space1))
            Text(text = task.title, style = LkTypography.getBody(), color = LkTextSecondary)
            Spacer(modifier = Modifier.height(LkSpacing.Space4))
            LkButton(text = "Görevi Görüntüle", onClick = onActionClick, modifier = Modifier.fillMaxWidth())
        } else if (resumeItem != null) {
            Text(text = "Eğitime Devam Et", style = LkTypography.getSectionTitle(), color = LkTextPrimary)
            Spacer(modifier = Modifier.height(LkSpacing.Space1))
            Text(text = resumeItem.courseTitle, style = LkTypography.getBody(), color = LkTextSecondary)
            Spacer(modifier = Modifier.height(LkSpacing.Space4))
            LkButton(text = "Devam Et", onClick = onActionClick, modifier = Modifier.fillMaxWidth())
        } else {
            Text(text = "Şu anda bekleyen bir görevin görünmüyor.", style = LkTypography.getBody(), color = LkTextSecondary)
            Spacer(modifier = Modifier.height(LkSpacing.Space4))
            LkButton(text = "Eğitimlere Göz At", variant = LkButtonVariant.SECONDARY, onClick = onActionClick, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun ResumeWidget(item: ResumeItemDto, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = "Yarım Bıraktıklarınız", style = LkTypography.getSectionTitle(), color = LkTextPrimary)
        Spacer(modifier = Modifier.height(LkSpacing.Space3))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LkSurfacePanel, LkShapes.MD)
                .clickable { onClick() }
                .padding(LkSpacing.Space4),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Kurs", style = LkTypography.getMetadata(), color = LkTextSecondary)
                Text(text = item.courseTitle, style = LkTypography.getBodyStrong(), color = LkTextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(LkSpacing.Space1))
                Text(text = "% Tamamlandı", style = LkTypography.getBodySmall(), color = LkSuccess)
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = LkTextSecondary)
        }
    }
}

@Composable
fun LearningProgressWidget(stats: DashboardStatsDto, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = "Öğrenme İlerlemesi", style = LkTypography.getSectionTitle(), color = LkTextPrimary)
        Spacer(modifier = Modifier.height(LkSpacing.Space3))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
            LkMetricCard(
                label = "Aktif Eğitim",
                value = "",
                modifier = Modifier.weight(1f)
            )
            LkMetricCard(
                label = "Tamamlanan",
                value = "",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun RecommendationsWidget(recommendations: List<RecommendationDto>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = "Sana Uygun", style = LkTypography.getSectionTitle(), color = LkTextPrimary)
        Spacer(modifier = Modifier.height(LkSpacing.Space3))
        
        if (recommendations.isEmpty()) {
            Text(text = "Henüz bir öneri bulunmuyor.", style = LkTypography.getBody(), color = LkTextSecondary)
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3),
            ) {
                items(recommendations) { rec ->
                    Column(
                        modifier = Modifier
                            .width(240.dp)
                            .background(LkSurfacePanel, LkShapes.MD)
                            .clickable { /* navigate to detail */ }
                            .padding(LkSpacing.Space4)
                    ) {
                        Text(
                            text = if (rec.type == "ko" || rec.type == "knowledge_object") "İÇERİK" else rec.type.uppercase(),
                            style = LkTypography.getMetadata(),
                            color = LkPrimary
                        )
                        Spacer(modifier = Modifier.height(LkSpacing.Space1))
                        Text(
                            text = rec.title,
                            style = LkTypography.getBodyStrong(),
                            color = LkTextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecentActivityWidget(activities: List<RecentActivityDto>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = "Son İşlemler", style = LkTypography.getSectionTitle(), color = LkTextPrimary)
        Spacer(modifier = Modifier.height(LkSpacing.Space3))
        
        if (activities.isEmpty()) {
            Text(text = "Yakın zamanda etkinlik bulunmuyor.", style = LkTypography.getBody(), color = LkTextSecondary)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                activities.take(3).forEach { activity ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LkSurfacePanel, LkShapes.MD)
                            .padding(LkSpacing.Space4),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activity.title,
                                style = LkTypography.getBodyStrong(),
                                color = LkTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(LkSpacing.Space1))
                            Text(
                                text = formatActivityType(activity.eventType),
                                style = LkTypography.getBodySmall(),
                                color = LkTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatActivityType(type: String): String {
    return when (type) {
        "enrollment_progress" -> "İlerleme"
        "course_completed" -> "Kurs Tamamlandı"
        "ko_completed" -> "Ders Tamamlandı"
        "mentor_chat" -> "AI Mentor Sohbeti"
        "flashcard_review" -> "Kart Tekrarı"
        else -> "Etkinlik"
    }
}

