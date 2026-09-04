package com.localkarar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.*

@Composable
fun LkAppHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    onOpenProductCenter: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LkSurfaceCanvas)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = LkSpacing.Space4),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Geri",
                        tint = LkPrimary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = if (onBack == null) LkSpacing.Space2 else 0.dp)
            ) {
                Text(
                    text = title,
                    style = LkTypography.getSectionTitle(),
                    color = LkTextPrimary,
                    maxLines = 1
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = LkTypography.getMicro(),
                        color = LkTextSecondary,
                        maxLines = 1
                    )
                }
            }

            // Custom actions
            actions()

            // Product center launcher
            if (onOpenProductCenter != null) {
                Row(
                    modifier = Modifier
                        .clip(LkShapes.FULL)
                        .background(LkSurfacePanel)
                        .border(1.dp, LkLineStrong, LkShapes.FULL)
                        .clickable(onClick = onOpenProductCenter)
                        .padding(horizontal = LkSpacing.Space3, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Apps,
                        contentDescription = "Ürün Merkezi",
                        tint = LkPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Ürünler",
                        style = LkTypography.getMicro(),
                        color = LkPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        Divider(color = LkLineSoft, thickness = 1.dp)
    }
}
