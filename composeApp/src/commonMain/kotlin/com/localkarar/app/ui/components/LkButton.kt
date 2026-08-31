package com.localkarar.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.LkLineStrong
import com.localkarar.app.ui.theme.LkPrimary
import com.localkarar.app.ui.theme.LkShapes
import com.localkarar.app.ui.theme.LkSurfacePanel
import com.localkarar.app.ui.theme.LkSurfaceRaised
import com.localkarar.app.ui.theme.LkSurfaceSunken
import com.localkarar.app.ui.theme.LkTextPrimary
import com.localkarar.app.ui.theme.LkTextSecondary
import com.localkarar.app.ui.theme.LkTypography
import com.localkarar.app.ui.theme.LkOnPrimary

enum class LkButtonVariant {
    PRIMARY,
    SECONDARY,
    QUIET,
    GHOST,
    DANGER
}

@Composable
fun LkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: LkButtonVariant = LkButtonVariant.PRIMARY,
    enabled: Boolean = true
) {
    val backgroundColor = when (variant) {
        LkButtonVariant.PRIMARY -> LkPrimary
        LkButtonVariant.SECONDARY -> LkSurfaceRaised
        LkButtonVariant.QUIET -> LkSurfaceSunken
        LkButtonVariant.GHOST -> Color.Transparent
        LkButtonVariant.DANGER -> LkSurfaceSunken
    }

    val contentColor = when (variant) {
        LkButtonVariant.PRIMARY -> LkOnPrimary
        LkButtonVariant.SECONDARY -> LkTextPrimary
        LkButtonVariant.QUIET -> LkTextSecondary
        LkButtonVariant.GHOST -> LkPrimary
        LkButtonVariant.DANGER -> com.localkarar.app.ui.theme.LkDanger
    }

    val borderStroke = when (variant) {
        LkButtonVariant.SECONDARY -> BorderStroke(1.dp, LkLineStrong)
        LkButtonVariant.DANGER -> BorderStroke(1.dp, com.localkarar.app.ui.theme.LkDanger.copy(alpha = 0.5f))
        else -> null
    }

    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        enabled = enabled,
        shape = LkShapes.SM,
        border = borderStroke,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            disabledBackgroundColor = backgroundColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
        contentPadding = PaddingValues(horizontal = 18.dp)
    ) {
        Text(
            text = text,
            style = LkTypography.getBody().copy(fontWeight = FontWeight.W700)
        )
    }
}
