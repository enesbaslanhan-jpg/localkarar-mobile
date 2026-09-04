package com.localkarar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
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
import com.localkarar.app.ui.theme.LkPrimaryFill
import com.localkarar.app.ui.theme.LkOnPrimary
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
    // §11 Chip = control-sm (32dp); §19 dokunma hedefi 44dp.
    // Onceden ~24dp idi: hem sozlesme disi hem erisilebilirlik ihlali.
    val tiklanabilir = if (onClick != null) {
        modifier.heightIn(min = 44.dp).clickable(onClick = onClick)
    } else {
        modifier
    }
    Box(
        modifier = tiklanabilir
            .padding(vertical = if (onClick != null) 6.dp else 0.dp)
            .height(32.dp)
            .background(background, CircleShape)
            .padding(horizontal = LkSpacing.Space3),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = LkTypography.getLabel(), color = contentColor)
    }
}

@Composable
fun LkChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Secili zemin `primaryFill` (brand-500): §8.1 secili/primary yuzeyler
    // icin solid brand-500 istiyor ve beyaz yaziyla her iki modda AA gecer.
    Box(
        modifier = modifier
            .heightIn(min = 44.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
            .height(32.dp)
            .background(if (selected) LkPrimaryFill else LkSurfaceRaised, CircleShape)
            .padding(horizontal = LkSpacing.Space3),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = LkTypography.getLabel(),
            color = if (selected) LkOnPrimary else LkTextSecondary
        )
    }
}