package com.localkarar.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled

/**
 * iOS karsiligi — `UIAccessibility.isReduceMotionEnabled`.
 *
 * Ayarlar → Erisilebilirlik → Hareket → Hareketi Azalt. Apple'in kendi
 * yonergesi de bu bayrak aciksa suslemesel animasyonlarin kaldirilmasini
 * istiyor; webdeki `prefers-reduced-motion` ile ayni tercih.
 *
 * ⚠️ Bu kod HENUZ CIHAZDA CALISTIRILMADI. iOS hedefi derleniyor ama gorsel
 * dogrulama yapilmadi -- bilinen eksik.
 */
@Composable
@ReadOnlyComposable
actual fun isReducedMotionEnabled(): Boolean = UIAccessibilityIsReduceMotionEnabled()
