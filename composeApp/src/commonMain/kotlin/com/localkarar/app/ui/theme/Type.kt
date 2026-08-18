package com.localkarar.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import localkarar_mobile.composeapp.generated.resources.Res
import localkarar_mobile.composeapp.generated.resources.manrope_regular
import localkarar_mobile.composeapp.generated.resources.manrope_medium
import localkarar_mobile.composeapp.generated.resources.manrope_semibold
import localkarar_mobile.composeapp.generated.resources.manrope_bold
import org.jetbrains.compose.resources.Font

/**
 * Manrope font family with explicit per-weight resource references.
 *
 * Source: Manrope variable font (SIL Open Font License 1.1).
 * The OFL.txt license is preserved in the composeResources/font/ directory.
 *
 * Weight mapping (Web design system → Font resource):
 *   W400 Regular   → manrope_regular.ttf
 *   W500 Medium    → manrope_medium.ttf
 *   W600 SemiBold  → manrope_semibold.ttf
 *   W700 Bold      → manrope_bold.ttf
 *
 * Note: All four TTF files are sourced from the same Manrope variable font file.
 * Compose uses the FontWeight hint to select the correct variable axis value at runtime.
 * Turkish glyphs (ş, ç, ğ, ı, İ, Ö, Ü, etc.) are fully covered by Manrope Latin.
 */
@Composable
fun getManropeFontFamily(): FontFamily {
    return FontFamily(
        Font(Res.font.manrope_regular, weight = FontWeight.W400),
        Font(Res.font.manrope_medium,  weight = FontWeight.W500),
        Font(Res.font.manrope_semibold, weight = FontWeight.W600),
        Font(Res.font.manrope_bold,    weight = FontWeight.W700),
    )
}

/**
 * LocalKarar Typography Scale
 *
 * Mapped from the official Web design system tokens.
 * Reference: LOCALKARAR_DESIGN_SYSTEM.md §2 Typography
 *
 * Web px values converted to sp 1:1 (standard mobile sp = web px at 1x density).
 */
object LkTypography {

    /** PageTitle — 24sp / W700 / lh 1.25 / ls -0.02em */
    @Composable
    fun getPageTitle() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W700,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.48).sp,
        color = LkTextPrimary
    )

    /** Display — 28sp / W700 / lh 1.25 / ls -0.035em */
    @Composable
    fun getDisplay() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W700,
        fontSize = 28.sp,
        lineHeight = 35.sp,
        letterSpacing = (-0.98).sp,
        color = LkTextPrimary
    )

    /** Metric — 23sp / W700 / lh 1.25 */
    @Composable
    fun getMetric() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W700,
        fontSize = 23.sp,
        lineHeight = 29.sp,
        color = LkTextPrimary
    )

    /** SectionTitle — 16sp / W700 / lh 1.25 */
    @Composable
    fun getSectionTitle() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W700,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        color = LkTextPrimary
    )

    /** CardTitle / DomainTitle — 15sp / W600 / lh 1.25 */
    @Composable
    fun getCardTitle() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W600,
        fontSize = 15.sp,
        lineHeight = 19.sp,
        color = LkTextPrimary
    )

    /** Body — 13sp / W400 / lh 1.5 */
    @Composable
    fun getBody() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W400,
        fontSize = 13.sp,
        lineHeight = 19.5.sp,
        color = LkTextPrimary
    )

    /** BodyStrong — 13sp / W500 / lh 1.5 */
    @Composable
    fun getBodyStrong() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W500,
        fontSize = 13.sp,
        lineHeight = 19.5.sp,
        color = LkTextPrimary
    )

    /** BodySmall — 12sp / W400 / lh 1.5 */
    @Composable
    fun getBodySmall() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W400,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        color = LkTextSecondary
    )

    /** Meta / Metadata — 11sp / W400 / lh 1.5 */
    @Composable
    fun getMetadata() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W400,
        fontSize = 11.sp,
        lineHeight = 16.5.sp,
        color = LkTextSecondary
    )

    /** Micro — 10sp / W500 / lh 1.5 */
    @Composable
    fun getMicro() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W500,
        fontSize = 10.sp,
        lineHeight = 15.sp,
        color = LkTextMuted
    )
}
