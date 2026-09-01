package com.localkarar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.LkLineStrong
import com.localkarar.app.ui.theme.LkPrimary
import com.localkarar.app.ui.theme.LkShapes
import com.localkarar.app.ui.theme.LkSpacing
import com.localkarar.app.ui.theme.LkSurfaceCanvas
import com.localkarar.app.ui.theme.LkSurfacePanel
import com.localkarar.app.ui.theme.LkSurfaceRaised
import com.localkarar.app.ui.theme.LkTextPrimary
import com.localkarar.app.ui.theme.LkTextSecondary
import com.localkarar.app.ui.theme.LkTypography

@Composable
fun LkSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = LkTypography.getSectionTitle(),
            color = LkTextPrimary
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(LkSpacing.Space1))
            Text(
                text = subtitle,
                style = LkTypography.getMetadata(),
                color = LkTextSecondary
            )
        }
    }
}

@Composable
fun LkInfoPanel(
    title: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LkSurfacePanel, LkShapes.MD)
            .border(1.dp, LkLineStrong, LkShapes.MD)
            .padding(LkSpacing.PadPanel)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = LkTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(LkSpacing.Space2))
            }
            Text(
                text = title,
                style = LkTypography.getBodyStrong(),
                color = LkTextPrimary
            )
        }
        Spacer(modifier = Modifier.height(LkSpacing.Space3))
        content()
    }
}

@Composable
fun LkResultRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = LkTextPrimary
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = LkTypography.getBodySmall(),
            color = LkTextSecondary,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(LkSpacing.Space4))
        Text(
            text = value,
            style = LkTypography.getBodyStrong(),
            color = valueColor
        )
    }
}

@Composable
fun LkChip(
    text: String,
    background: Color = LkSurfaceRaised,
    contentColor: Color = LkTextSecondary,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val clickModifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier
    Text(
        text = text,
        style = LkTypography.getMicro(),
        color = contentColor,
        modifier = clickModifier
            .background(background, CircleShape)
            .padding(horizontal = LkSpacing.Space3, vertical = LkSpacing.Space1)
    )
}

@Composable
fun LkChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = LkTypography.getMicro(),
        color = if (selected) LkSurfaceCanvas else LkTextSecondary,
        modifier = modifier
            .background(
                if (selected) LkPrimary else LkSurfaceRaised,
                CircleShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = LkSpacing.Space3, vertical = LkSpacing.Space1)
    )
}