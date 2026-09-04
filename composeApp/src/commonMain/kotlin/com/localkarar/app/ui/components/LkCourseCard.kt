package com.localkarar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.LkLineSoft
import com.localkarar.app.ui.theme.LkPrimaryDeep
import com.localkarar.app.ui.theme.LkShapes
import com.localkarar.app.ui.theme.LkSurfaceSunken
import com.localkarar.app.ui.theme.LkTextPrimary
import com.localkarar.app.ui.theme.LkTextSecondary
import com.localkarar.app.ui.theme.LkTypography
import com.localkarar.app.ui.theme.LkSpacing
import androidx.compose.ui.graphics.Color

@Composable
fun LkCourseCard(
    title: String,
    lessonCount: Int,
    estimatedMinutes: Int?,
    progress: Int?,
    level: String?,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        if (showDivider) {
            Divider(color = LkLineSoft, thickness = 1.dp)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = LkSpacing.Space3),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cover
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(68.dp)
                    .clip(LkShapes.MD)
                    // Mixing Warm (assume roughly #F59E0B) with SurfaceRaised for the background
                    // For now using a deep primary tint as fallback
                    .background(Color(0xFF2B3A41)), 
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    text = "LK",
                    style = LkTypography.getSectionTitle().copy(color = LkPrimaryDeep),
                    modifier = Modifier.padding(LkSpacing.Space3)
                )
            }
            
            Spacer(modifier = Modifier.width(LkSpacing.Space4))
            
            // Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(LkSpacing.Space1)
            ) {
                Text(
                    text = title,
                    style = LkTypography.getCardTitle(),
                    color = LkTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                val durationText = if (estimatedMinutes != null) "   dk" else ""
                Text(
                    text = " ders",
                    style = LkTypography.getMetadata(),
                    color = LkTextSecondary
                )
                
                val badgeText = if (progress != null) "% tamamlandı" else (level ?: "Yeni")
                Box(
                    modifier = Modifier
                        .background(LkSurfaceSunken, LkShapes.FULL)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = LkTypography.getMicro(),
                        color = LkTextSecondary
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = LkTextSecondary
            )
        }
    }
}

