package com.localkarar.app.ui.components

import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.LkElevation
import com.localkarar.app.ui.theme.LkLineStrong
import com.localkarar.app.ui.theme.LkSpacing
import com.localkarar.app.ui.theme.LkSurfaceElevated
import com.localkarar.app.ui.theme.lkElevation
import kotlin.math.roundToInt

/*
 * UC KADEMELI SURUKLENEBILIR CEKMECE — planin en riskli parcasi.
 *
 * 🔴 MATERIAL3 `ModalBottomSheet` BUNU VEREMEZ.
 *
 * Iki sebeple: (1) iki kademesi var (PartiallyExpanded / Expanded),
 * (2) karartma ile acilir ve disina dokununca kapanir. Istenen davranis
 * Apple Maps'inki: cekmece HEP ekranda, arkasindaki icerik gorunur kalir,
 * uc kademe arasinda surukleyerek gezilir. Hazir bilesen yok, yaziliyor.
 *
 * 🔴 JEST CAKISMASI — KLASIK TUZAK.
 *
 * Cekmecenin govdesi kaydirilabilir bir liste. Surukleme cekmecenin
 * TAMAMINDA baslasaydi, listeyi kaydirmak isteyen parmak cekmeceyi
 * suruklerdi. Cozum: surukleme YALNIZ tutamak ve baslikta baslar
 * (`LkSheetHandle`). Mockup'ta da bu sekilde prova edildi.
 *
 * Kademeler ekran yuksekligine gore hesaplaniyor, sabit dp degil; kucuk
 * ve buyuk telefonlarda ayni orani korumak icin.
 */

enum class LkSheetDetent {
    /** Tepeden bakis — yalniz baslik ve ilk satir gorunur. */
    Peek,

    /** Yari acik. */
    Half,

    /** Neredeyse tam ekran; ustte baglam icin bir serit kalir. */
    Full
}

@Composable
fun rememberLkSheetState(
    initial: LkSheetDetent = LkSheetDetent.Peek
): AnchoredDraggableState<LkSheetDetent> {
    val d = LocalDensity.current
    return remember {
        AnchoredDraggableState(
            initialValue = initial,
            positionalThreshold = { toplam: Float -> toplam * 0.4f },
            velocityThreshold = { with(d) { 120.dp.toPx() } },
            snapAnimationSpec = spring(dampingRatio = 0.82f),
            decayAnimationSpec = androidx.compose.animation.core.exponentialDecay()
        )
    }
}

/**
 * Cekmece.
 *
 * Cagiran `Box` icine, arkadaki icerigin UZERINE koyar.
 *
 * @param handle tutamak ve baslik — surukleme YALNIZ burada baslar.
 * @param body kaydirilabilir govde; jesti cekmeceyle cakismaz.
 */
@Composable
fun LkSheet(
    state: AnchoredDraggableState<LkSheetDetent>,
    modifier: Modifier = Modifier,
    handle: @Composable ColumnScope.() -> Unit,
    body: @Composable ColumnScope.() -> Unit
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val h = constraints.maxHeight.toFloat()
        val d = LocalDensity.current

        /*
         * Kademe konumlari — cekmecenin UST kenarinin y'si.
         *
         * ⚠️ `h` EKRAN YUKSEKLIGI DEGIL, kabin yuksekligi. Kap alt dock'un
         * ustunde bitiyor; bu yuzden 150dp'lik bir tepe kademesi ekranda
         * beklenenden YUKARIDA duruyordu ve arkadaki bolumleri ortuyordu.
         *
         * 108dp: tutamak + baslik satiri gorunur, altindaki icerik degil.
         * Cekmecenin orada oldugu anlasilir ama sayfayi yemez.
         */
        val anchors = remember(h) {
            DraggableAnchors {
                LkSheetDetent.Peek at h - with(d) { 108.dp.toPx() }
                LkSheetDetent.Half at h * 0.46f
                LkSheetDetent.Full at with(d) { 78.dp.toPx() }
            }
        }
        state.updateAnchors(anchors)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, state.requireOffset().roundToInt()) }
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .lkElevation(LkElevation.OVERLAY, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(LkSurfaceElevated)
        ) {
            /* Surukleme kapisi: yalniz bu bolge. */
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .anchoredDraggable(state, Orientation.Vertical)
            ) {
                LkSheetHandle()
                handle()
            }
            Column(Modifier.fillMaxWidth(), content = body)
        }
    }
}

/** Tutamak — cekmecenin surukleneceginin tek gorsel isareti. */
@Composable
fun LkSheetHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = LkSpacing.Space3, bottom = LkSpacing.Space1),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(width = 40.dp, height = 5.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(LkLineStrong)
        )
    }
}
