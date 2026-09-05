package com.localkarar.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.LkTypography
import com.localkarar.app.ui.theme.LkTypography.numeric
import com.localkarar.app.ui.theme.isReducedMotionEnabled

/*
 * ILERLEME HAPI — referans kitlerin deseni.
 *
 * Duz bir cizgi degil: koyu bir hap, YUZDE DOLGUNUN ICINDE, karsilastirma
 * degeri yolun sag ucunda. Yuzdeyi cubugun disina yazmak bir satir daha
 * yer kapliyor ve gozun iki yere bakmasini gerektiriyor.
 *
 * 🔴 BU BILESEN BIR HEDEFE GORE ILERLEME GOSTERMEZ.
 *
 * Mockup'ta "Eylul hedefinin %57'si" yaziyordu. Sunucuda BOYLE BIR ALAN
 * YOK -- ne `TrackerSummaryDto`'da ne baska bir uctan geliyor. O rakam
 * mockup'i doldurmak icin uydurulmustu. Uydurma bir finansal hedefi
 * ekrana basmak, kullanicinin gercek sandigi bir sey gostermek olurdu.
 *
 * Bu yuzden bilesen NOTR: kendisine verilen orani cizer, "hedef" demez.
 * Cagiran taraf orani neyin neye orani oldugunu ETIKETTE soylemek
 * zorundadir.
 */
@Composable
fun LkProgressPill(
    /** 0f..1f. Disarida hesaplanir; bilesen yorum yapmaz. */
    oran: Float,
    modifier: Modifier = Modifier,
    /** Yolun sag ucunda duran karsilastirma degeri (orn. toplam tutar). */
    sagDeger: String? = null,
    dolguRengi: Color,
    dolguUstuRengi: Color,
    yolRengi: Color,
    yolUstuRengi: Color
) {
    val kisitli = isReducedMotionEnabled()
    val hedef = oran.coerceIn(0f, 1f)
    val genislik by animateFloatAsState(
        targetValue = hedef,
        animationSpec = if (kisitli) tween(0) else tween(900),
        label = "progress"
    )
    val yuzde = (hedef * 100).toInt()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(30.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(yolRengi)
    ) {
        /* Dolgu ve icindeki yuzde. */
        Box(
            Modifier
                .fillMaxWidth(genislik.coerceAtLeast(0.001f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(999.dp))
                .background(dolguRengi)
        )

        Row(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            /*
             * Yuzde dolgunun ICINDE duruyor. Dolgu cok darsa disina taser ve
             * yol rengi uzerinde okunur -- bu yuzden dar durumda yol ustu
             * rengine geciliyor.
             */
            Text(
                text = "%$yuzde",
                style = LkTypography.getMetadata().numeric(),
                color = if (genislik > 0.18f) dolguUstuRengi else yolUstuRengi,
                modifier = Modifier.padding(start = 13.dp)
            )
            if (sagDeger != null) {
                Box(Modifier.weight(1f))
                Text(
                    text = sagDeger,
                    style = LkTypography.getMetadata().numeric(),
                    color = yolUstuRengi,
                    modifier = Modifier.padding(end = 13.dp)
                )
            }
        }
    }
}
