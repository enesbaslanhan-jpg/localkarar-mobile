package com.localkarar.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

/**
 * LocalKarar dark color scheme — primary parity target.
 * Source: LOCALKARAR_DESIGN_SYSTEM.md §1 Colors (Dark column)
 */
private val LkDarkColorScheme = darkColors(
    primary         = LkPrimary,
    primaryVariant  = LkPrimaryHover,
    secondary       = LkPrimary,
    background      = LkSurfaceCanvas,
    surface         = LkSurfacePanel,
    error           = LkDanger,
    onPrimary       = LkOnPrimary,
    onSecondary     = LkOnPrimary,
    onBackground    = LkTextPrimary,
    onSurface       = LkTextPrimary,
    onError         = LkTextPrimary
)

/**
 * LocalKarar light color scheme — foundation ready, not yet polished.
 * Source: LOCALKARAR_DESIGN_SYSTEM.md §1 Colors (Light column)
 */
private val LkLightColorScheme = lightColors(
    primary         = LkLightPrimary,
    primaryVariant  = LkLightPrimaryHover,
    secondary       = LkLightPrimary,
    background      = LkLightSurfaceCanvas,
    surface         = LkLightSurfacePanel,
    error           = LkLightDanger,
    onPrimary       = LkLightOnPrimary,
    onSecondary     = LkLightOnPrimary,
    onBackground    = LkLightTextPrimary,
    onSurface       = LkLightTextPrimary,
    onError         = LkLightTextPrimary
)

/**
 * LocalKarar theme.
 *
 * This is the SINGLE authoritative theme provider for all LocalKarar Mobile UI.
 * It must NOT be wrapped in an additional MaterialTheme() anywhere in the app —
 * doing so would silently reset typography and shapes to Material defaults.
 *
 * Architecture:
 *   LocalKararTheme wraps MaterialTheme with:
 *   - LkDarkColorScheme / LkLightColorScheme  (exact Web design tokens)
 *   - LkTypography mapped to all Material text slots
 *   - LkShapes mapped to small/medium/large Material shape slots
 *
 * @param darkTheme Defaults to system preference. Dark mode is the current parity target.
 */
@Composable
fun LocalKararTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors    = if (darkTheme) LkDarkColorScheme else LkLightColorScheme,
        typography = androidx.compose.material.Typography(
            h1        = LkTypography.getPageTitle(),
            h2        = LkTypography.getSectionTitle(),
            h3        = LkTypography.getCardTitle(),
            h4        = LkTypography.getCardTitle(),
            h5        = LkTypography.getCardTitle(),
            h6        = LkTypography.getCardTitle(),
            subtitle1 = LkTypography.getBodyStrong(),
            subtitle2 = LkTypography.getBodyStrong(),
            body1     = LkTypography.getBody(),
            body2     = LkTypography.getBodySmall(),
            button    = LkTypography.getBodyStrong(),
            caption   = LkTypography.getMetadata(),
            overline  = LkTypography.getMicro()
        ),
        shapes = androidx.compose.material.Shapes(
            small  = LkShapes.SM,   // 8dp — buttons, inputs
            medium = LkShapes.MD,   // 12dp — panels, cards
            large  = LkShapes.LG    // 14dp — signature panels
        ),
        content = content
    )
}
