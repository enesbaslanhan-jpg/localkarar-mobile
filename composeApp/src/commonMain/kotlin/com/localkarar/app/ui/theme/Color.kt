package com.localkarar.app.ui.theme

import androidx.compose.ui.graphics.Color

// ──────────────────────────────────────────────────────────────────
// DARK theme palette  (currently the primary parity target)
// Source: LOCALKARAR_DESIGN_SYSTEM.md §1 Colors
// ──────────────────────────────────────────────────────────────────

// Brand — Dark
val LkPrimary        = Color(0xFF94CEED)
val LkPrimaryHover   = Color(0xFFB4DDF2)
val LkPrimaryDeep    = Color(0xFFC0E8FF)
val LkPrimarySoft    = Color(0xFF47758A)
val LkOnPrimary      = Color(0xFF001E2B)

// Surfaces — Dark
val LkSurfaceCanvas  = Color(0xFF121619)
val LkSurfaceSunken  = Color(0xFF0C1013)
val LkSurfacePanel   = Color(0xFF1D2429)
val LkSurfaceRaised  = Color(0xFF293137)

// Fixed Surfaces (theme-independent)
val LkSurfaceSignature  = Color(0xFF173F4E)
val LkSurfaceSignature2 = Color(0xFF23515A)
val LkOnSignature       = Color(0xFFF4FAFC)
val LkOnSignatureDim    = Color(0xFFB7C6C3)
val LkSurfaceStage      = Color(0xFF152126)
val LkOnStage           = Color(0xFFFFFFFF)

// Text — Dark
val LkTextPrimary   = Color(0xFFF1F4F5)
val LkTextSecondary = Color(0xFFD2D9DD)
val LkTextMuted     = Color(0xFFBBC5CA)

// Lines & Borders — Dark
val LkLineSoft      = Color(0x1AEEF1F3)   // rgba(238,241,243, 0.10)
val LkLineStrong    = Color(0x2DEEF1F3)   // rgba(238,241,243, 0.18)

// Status
val LkSuccess = Color(0xFF72D3AD)
val LkWarning = Color(0xFFF6BB79)
val LkDanger  = Color(0xFFFFB4AB)

// ──────────────────────────────────────────────────────────────────
// LIGHT theme palette
// Source: LOCALKARAR_DESIGN_SYSTEM.md §1 Colors (Light column)
// Not the current parity priority but architecture is correct.
// ──────────────────────────────────────────────────────────────────

val LkLightPrimary        = Color(0xFF0D556F)
val LkLightPrimaryHover   = Color(0xFF306D88)
val LkLightPrimaryDeep    = Color(0xFF173F4E)
val LkLightPrimarySoft    = Color(0xFF94CEED)
val LkLightOnPrimary      = Color(0xFFFFFFFF)

val LkLightSurfaceCanvas  = Color(0xFFE1E2E5)
val LkLightSurfaceSunken  = Color(0xFFD9DADC)
val LkLightSurfacePanel   = Color(0xFFEDEEF0)
val LkLightSurfaceRaised  = Color(0xFFF8F9FC)

val LkLightTextPrimary    = Color(0xFF191C1E)
val LkLightTextSecondary  = Color(0xFF40484D)
val LkLightTextMuted      = Color(0xFF515B60)

val LkLightLineSoft       = Color(0x1C183E45)   // rgba(24,62,69, 0.11)
val LkLightLineStrong     = Color(0x33183E45)   // rgba(24,62,69, 0.20)

val LkLightSuccess = Color(0xFF23735A)
val LkLightWarning = Color(0xFF8A5D24)
val LkLightDanger  = Color(0xFFBA1A1A)
