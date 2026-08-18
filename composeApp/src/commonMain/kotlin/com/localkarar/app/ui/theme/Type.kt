package com.localkarar.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import localkarar_mobile.composeapp.generated.resources.Res
import localkarar_mobile.composeapp.generated.resources.manrope
import org.jetbrains.compose.resources.Font

@Composable
fun getManropeFontFamily(): FontFamily {
    return FontFamily(
        Font(Res.font.manrope, weight = FontWeight.W400),
        Font(Res.font.manrope, weight = FontWeight.W500),
        Font(Res.font.manrope, weight = FontWeight.W600),
        Font(Res.font.manrope, weight = FontWeight.W700),
        Font(Res.font.manrope, weight = FontWeight.W800)
    )
}

object LkTypography {
    @Composable
    fun getPageTitle() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W700,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.48).sp, // -0.02em of 24
        color = LkTextPrimary
    )
    
    @Composable
    fun getDisplay() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W800,
        fontSize = 28.sp,
        lineHeight = 35.sp,
        letterSpacing = (-0.98).sp, // -0.035em of 28
        color = LkTextPrimary
    )
    
    @Composable
    fun getMetric() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W700,
        fontSize = 23.sp,
        lineHeight = 29.sp,
        color = LkTextPrimary
    )
    
    @Composable
    fun getSectionTitle() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W700,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        color = LkTextPrimary
    )
    
    @Composable
    fun getCardTitle() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W700,
        fontSize = 15.sp,
        lineHeight = 19.sp,
        color = LkTextPrimary
    )
    
    @Composable
    fun getBody() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W400,
        fontSize = 13.sp,
        lineHeight = 19.5.sp, // 1.5x
        color = LkTextPrimary
    )

    @Composable
    fun getBodyStrong() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W500,
        fontSize = 13.sp,
        lineHeight = 19.5.sp,
        color = LkTextPrimary
    )
    
    @Composable
    fun getBodySmall() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W400,
        fontSize = 12.sp,
        lineHeight = 18.sp, // 1.5x
        color = LkTextSecondary
    )
    
    @Composable
    fun getMetadata() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W400,
        fontSize = 11.sp,
        lineHeight = 16.5.sp, // 1.5x
        color = LkTextSecondary
    )
    
    @Composable
    fun getMicro() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W500,
        fontSize = 10.sp,
        lineHeight = 15.sp, // 1.5x
        color = LkTextMuted
    )
}
