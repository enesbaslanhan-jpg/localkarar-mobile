package com.localkarar.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/*
 * RENK SISTEMI — KAYNAK: `DESIGN.md` (LocalKarar Design System v2).
 *
 * Dokuman kendini "tek kaynak (source of truth)" ilan ediyor; degerler oradan
 * geliyor, uygulanmis CSS'ten degil.
 *
 * 🔴 KOD ILE DOKUMAN AYRISIYOR — DOKUMAN KAZANIR.
 * `frontend/src/styles/theme-modes.css` `--brand-500: #0D556F` diyor;
 * `DESIGN.md` §1.1 ise brand-500 = #306D88. Onaylanan mobil prototip de
 * (`balanced_home_preview.html`) #306D88 / #7BA2B3 kullaniyor. Iki bagimsiz
 * kaynak dokumani dogruluyor, sapan taraf CSS.
 *
 * 🔴 YUZEY KADEMELERI §2.1'IN KENDI DEGERLERINDEN GENISLETILDI.
 *
 * §2.1 "Iki komsu yuzeyin degerleri birbirinden ayirt edilebilir olmalidir"
 * diyor, ama dokumanda verilen degerler bunu SAGLAMIYORDU. Olculdu
 * (WCAG bagil parlaklik orani):
 *
 *   acik  canvas→panel 1.057   panel→raised 1.055   canvas→raised 1.114
 *   koyu  canvas→panel 1.051   panel→raised 1.047   canvas→raised 1.100
 *
 * Kenarlik olmadan bir yuzey sinirinin algilanmasi icin kabaca 1.2 gerekiyor;
 * 1.05 cogu ekranda gorunmuyor. Ekranlar tek bir gri yikama gibi okunuyordu.
 *
 * Yeni merdivende kartin zeminden ayrimi her iki temada ~1.24. Metin
 * kontrastlari §2.2 esiklerinin uzerinde kaldi (acik 13.7:1, koyu 14.6:1;
 * muted metin kart uzerinde 3.95:1, §2.2 en az 3:1 istiyor).
 *
 * ⚠️ BU BIR DESIGN.md DEGISIKLIGIDIR. §2.1 tablosundaki degerler bu
 * degerlerle guncellenmeli; yoksa web ve mobil yuzeyleri ayrisir.
 *
 * ⚠️ Tokenlar @Composable getter: yalniz composable icinde kullanilabilir.
 * Composable disi bir baglamda DERLEME HATASI verir -- sessizce yanlis renk
 * donmez.
 */

// ──────────────────────────────────────────────────────────────────
// §1.1 BRAND PALETTE — DEGISTIRILMEZ
// ──────────────────────────────────────────────────────────────────

object LkBrand {
    val B50  = Color(0xFFD8E3E8)
    val B100 = Color(0xFFC5D6DE)
    val B200 = Color(0xFFA0BCC8)
    val B300 = Color(0xFF7BA2B3)
    val B400 = Color(0xFF55879D)
    val B500 = Color(0xFF306D88)   // primary
    val B600 = Color(0xFF25556A)
    val B700 = Color(0xFF1B3D4C)
    val B800 = Color(0xFF10242D)
    // B900/B950 bilerek YOK: §1.3 zemin/yuzey olarak kullanilmalarini
    // yasakliyor ve baska kullanimlari da yok.
}

@Immutable
data class LkColorScheme(
    /** Vurgu — metin, ikon, secili durum. Moda gore kontrast korunur. */
    val primary: Color,
    val primaryHover: Color,
    val primaryDeep: Color,
    val primarySoft: Color,
    /** §8.1: Primary CTA dolgusu HER IKI MODDA solid brand-500. */
    val primaryFill: Color,
    val onPrimary: Color,
    val surfaceCanvas: Color,
    val surfaceSunken: Color,
    val surfacePanel: Color,
    val surfaceRaised: Color,
    val surfaceHighlight: Color,
    val surfaceElevated: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    /** §2.3 divider — en zayif ayirici. */
    val lineSoft: Color,
    /** §2.3 border-default. */
    val lineStrong: Color,
    /** §2.3 border-strong. */
    val lineBold: Color,
    val success: Color,
    val warning: Color,
    val danger: Color
)

/** §1.2 + §2.1 + §8 — koyu mod. */
val LkDarkColors = LkColorScheme(
    // §8.1: koyu modda metin/link vurgusu brand-300; CTA dolgusu ayri token.
    primary          = LkBrand.B300,
    primaryHover     = LkBrand.B200,
    primaryDeep      = LkBrand.B400,
    primarySoft      = Color(0x26306D88),   // brand-500 %15 tonal zemin
    primaryFill      = LkBrand.B500,
    onPrimary        = Color(0xFFFFFFFF),
    surfaceCanvas    = Color(0xFF14181C),   // background
    surfaceSunken    = Color(0xFF0B0E11),   // surface-0
    surfacePanel     = Color(0xFF1C2126),   // surface-1
    surfaceRaised    = Color(0xFF242A30),   // surface-2
    surfaceHighlight = Color(0xFF2C333A),   // surface-3
    surfaceElevated  = Color(0xFF343C44),   // surface-elevated
    textPrimary      = Color(0xFFE4E9ED),
    textSecondary    = Color(0xFF9AA6AE),
    textMuted        = Color(0xFF7C8790),
    lineSoft         = Color(0x0FFFFFFF),   // divider rgba(255,255,255,.06)
    lineStrong       = Color(0x1AFFFFFF),   // default rgba(255,255,255,.10)
    lineBold         = Color(0x29FFFFFF),   // strong  rgba(255,255,255,.16)
    success          = Color(0xFF4ADE80),
    warning          = Color(0xFFFBBF24),
    danger           = Color(0xFFF87171)
)

/** §1.2 + §2.1 + §9 — acik mod. */
val LkLightColors = LkColorScheme(
    primary          = LkBrand.B500,
    primaryHover     = LkBrand.B600,
    primaryDeep      = LkBrand.B700,
    primarySoft      = LkBrand.B50,
    primaryFill      = LkBrand.B500,
    onPrimary        = Color(0xFFFFFFFF),
    surfaceCanvas    = Color(0xFFE1E7EB),
    surfaceSunken    = Color(0xFFD6DDE2),
    surfacePanel     = Color(0xFFF0F3F6),
    surfaceRaised    = Color(0xFFFFFFFF),
    surfaceHighlight = Color(0xFFFFFFFF),
    surfaceElevated  = Color(0xFFFFFFFF),
    textPrimary      = Color(0xFF1A1C1E),
    textSecondary    = Color(0xFF3F484A),
    textMuted        = Color(0xFF6B7575),
    lineSoft         = Color(0x141A1C1E),   // divider rgba(26,28,30,.08)
    lineStrong       = Color(0x241A1C1E),   // default rgba(26,28,30,.14)
    lineBold         = Color(0x3D1A1C1E),   // strong  rgba(26,28,30,.24)
    success          = Color(0xFF15803D),
    warning          = Color(0xFFB45309),
    danger           = Color(0xFFB91C1C)
)

/**
 * Yururlukteki palet.
 *
 * `staticCompositionLocalOf`: tema nadiren degisir; degistiginde tum agacin
 * yeniden cizilmesi zaten istenen davranis.
 */
val LocalLkColors = staticCompositionLocalOf { LkDarkColors }

// ──────────────────────────────────────────────────────────────────
// TOKENLAR
// ──────────────────────────────────────────────────────────────────

val LkPrimary: Color          @Composable @ReadOnlyComposable get() = LocalLkColors.current.primary
val LkPrimaryHover: Color     @Composable @ReadOnlyComposable get() = LocalLkColors.current.primaryHover
val LkPrimaryDeep: Color      @Composable @ReadOnlyComposable get() = LocalLkColors.current.primaryDeep
val LkPrimarySoft: Color      @Composable @ReadOnlyComposable get() = LocalLkColors.current.primarySoft
val LkPrimaryFill: Color      @Composable @ReadOnlyComposable get() = LocalLkColors.current.primaryFill
val LkOnPrimary: Color        @Composable @ReadOnlyComposable get() = LocalLkColors.current.onPrimary

val LkSurfaceCanvas: Color    @Composable @ReadOnlyComposable get() = LocalLkColors.current.surfaceCanvas
val LkSurfaceSunken: Color    @Composable @ReadOnlyComposable get() = LocalLkColors.current.surfaceSunken
val LkSurfacePanel: Color     @Composable @ReadOnlyComposable get() = LocalLkColors.current.surfacePanel
val LkSurfaceRaised: Color    @Composable @ReadOnlyComposable get() = LocalLkColors.current.surfaceRaised
val LkSurfaceHighlight: Color @Composable @ReadOnlyComposable get() = LocalLkColors.current.surfaceHighlight
val LkSurfaceElevated: Color  @Composable @ReadOnlyComposable get() = LocalLkColors.current.surfaceElevated

val LkTextPrimary: Color      @Composable @ReadOnlyComposable get() = LocalLkColors.current.textPrimary
val LkTextSecondary: Color    @Composable @ReadOnlyComposable get() = LocalLkColors.current.textSecondary
val LkTextMuted: Color        @Composable @ReadOnlyComposable get() = LocalLkColors.current.textMuted

val LkLineSoft: Color         @Composable @ReadOnlyComposable get() = LocalLkColors.current.lineSoft
val LkLineStrong: Color       @Composable @ReadOnlyComposable get() = LocalLkColors.current.lineStrong
val LkLineBold: Color         @Composable @ReadOnlyComposable get() = LocalLkColors.current.lineBold

val LkSuccess: Color          @Composable @ReadOnlyComposable get() = LocalLkColors.current.success
val LkWarning: Color          @Composable @ReadOnlyComposable get() = LocalLkColors.current.warning
val LkDanger: Color           @Composable @ReadOnlyComposable get() = LocalLkColors.current.danger

// ──────────────────────────────────────────────────────────────────
// TEMADAN BAGIMSIZ YUZEYLER
//
// Imza panelleri her iki temada da ayni koyu zeminde durur; uzerlerindeki
// metin rengi de sabit. Tema duyarli yapilmalari acik temada okunaksiz bir
// kontrast uretirdi. §3.2 bunlar icin `shadow-dark` istisnasini taniyor.
// ──────────────────────────────────────────────────────────────────

val LkSurfaceSignature  = LkBrand.B700
val LkSurfaceSignature2 = Color(0xFF23515A)
val LkOnSignature       = Color(0xFFF4FAFC)
val LkOnSignatureDim    = Color(0xFFB7C6C3)
val LkSurfaceStage      = Color(0xFF152126)
val LkOnStage           = Color(0xFFFFFFFF)
