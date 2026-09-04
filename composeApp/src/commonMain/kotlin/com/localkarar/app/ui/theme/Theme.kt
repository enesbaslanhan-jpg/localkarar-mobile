package com.localkarar.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * LocalKarar dark color scheme — primary parity target.
 * Source: LOCALKARAR_DESIGN_SYSTEM.md §1 Colors (Dark column)
 */
/**
 * Material renk semasi — KOYU.
 *
 * ⚠️ Degerler `LkDarkColors` paletinden okunuyor, token getter'larindan
 * DEGIL: tokenlar artik @Composable ve burasi ust duzey bir tanim.
 *
 * Bu sema yalniz Material bilesenlerinin varsayilanlarini besliyor;
 * ekranlarin okudugu asil kaynak `LocalLkColors`.
 */
private val LkDarkColorScheme = darkColors(
    primary         = LkDarkColors.primary,
    primaryVariant  = LkDarkColors.primaryHover,
    secondary       = LkDarkColors.primary,
    background      = LkDarkColors.surfaceCanvas,
    surface         = LkDarkColors.surfacePanel,
    error           = LkDarkColors.danger,
    onPrimary       = LkDarkColors.onPrimary,
    onSecondary     = LkDarkColors.onPrimary,
    onBackground    = LkDarkColors.textPrimary,
    onSurface       = LkDarkColors.textPrimary,
    onError         = LkDarkColors.textPrimary
)

/**
 * LocalKarar light color scheme — foundation ready, not yet polished.
 * Source: LOCALKARAR_DESIGN_SYSTEM.md §1 Colors (Light column)
 */
/** Material renk semasi — ACIK. Degerler `LkLightColors` paletinden. */
private val LkLightColorScheme = lightColors(
    primary         = LkLightColors.primary,
    primaryVariant  = LkLightColors.primaryHover,
    secondary       = LkLightColors.primary,
    background      = LkLightColors.surfaceCanvas,
    surface         = LkLightColors.surfacePanel,
    error           = LkLightColors.danger,
    onPrimary       = LkLightColors.onPrimary,
    onSecondary     = LkLightColors.onPrimary,
    onBackground    = LkLightColors.textPrimary,
    onSurface       = LkLightColors.textPrimary,
    onError         = LkLightColors.textPrimary
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
    themeController: ThemeController? = null,
    content: @Composable () -> Unit
) {
    /*
     * 🔴 PALET BURADAN SAGLANIYOR.
     *
     * Onceden yalniz `MaterialTheme.colors` degistiriliyordu; ekranlar
     * `LkTextPrimary` gibi SABITLERI okudugu icin temanin hicbir etkisi
     * yoktu ve uygulama koyu takiliydi. Artik tokenlar `LocalLkColors`
     * uzerinden okunuyor ve saglanan palet gercekten uygulanıyor.
     */
    val palet = if (darkTheme) LkDarkColors else LkLightColors

    CompositionLocalProvider(
        LocalLkColors provides palet,
        LocalThemeController provides themeController
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
}
