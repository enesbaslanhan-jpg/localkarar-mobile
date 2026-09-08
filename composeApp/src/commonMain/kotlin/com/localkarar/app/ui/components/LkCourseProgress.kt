package com.localkarar.app.ui.components

import androidx.compose.runtime.Composable
import com.localkarar.app.ui.theme.*

/** Mobil föy §24: yüzde dolgunun içinde, ders sayısı sağda. */
@Composable
fun LkCourseProgress(progress: Float, detail: String? = null) {
    LkProgressPill(
        oran = progress, sagDeger = detail,
        dolguRengi = LkPrimaryFill, dolguUstuRengi = LkOnPrimary,
        yolRengi = LkSurfaceRaised, yolUstuRengi = LkTextSecondary
    )
}
