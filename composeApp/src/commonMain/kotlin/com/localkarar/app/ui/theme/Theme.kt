package com.localkarar.app.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable

private val DarkColors = darkColors(
    primary = LkPrimary,
    primaryVariant = LkPrimaryHover,
    secondary = LkPrimary,
    background = LkSurfaceCanvas,
    surface = LkSurfacePanel,
    error = LkDanger,
    onPrimary = LkOnPrimary,
    onSecondary = LkOnPrimary,
    onBackground = LkTextPrimary,
    onSurface = LkTextPrimary,
    onError = LkTextPrimary
)

@Composable
fun LocalKararTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = DarkColors,
        typography = androidx.compose.material.Typography(
            h1 = LkTypography.getPageTitle(),
            h2 = LkTypography.getSectionTitle(),
            h3 = LkTypography.getCardTitle(),
            h4 = LkTypography.getCardTitle(),
            h5 = LkTypography.getCardTitle(),
            h6 = LkTypography.getCardTitle(),
            subtitle1 = LkTypography.getBodyStrong(),
            subtitle2 = LkTypography.getBodyStrong(),
            body1 = LkTypography.getBody(),
            body2 = LkTypography.getBodySmall(),
            button = LkTypography.getBodyStrong(),
            caption = LkTypography.getMetadata(),
            overline = LkTypography.getMicro()
        ),
        shapes = androidx.compose.material.Shapes(
            small = LkShapes.SM,
            medium = LkShapes.MD,
            large = LkShapes.LG
        ),
        content = content
    )
}
