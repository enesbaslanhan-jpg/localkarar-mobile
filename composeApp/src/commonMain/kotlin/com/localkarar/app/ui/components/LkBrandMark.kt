package com.localkarar.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.isReducedMotionEnabled

/**
 * Marka isareti.
 *
 * ⚠️ TEMAYLA DONMEZ. Renkleri bilerek sabit; acik ve koyu temada ayni
 * gorunur (devir belgesi §3.5).
 *
 * PUSULA HAREKETI (`hareketli = true`): igne −28°'den gelip 6°'yi asarak
 * yerine oturuyor, 700ms. Giris akisinda TEK SEFERLIK — surekli donen bir
 * kadran dekoratif gurultu olurdu, bu yuzden `key` verilmedikce tekrar
 * tetiklenmiyor.
 *
 * 🔴 CSS'TE IKI KEZ YAKALANAN TUZAK: ayni animasyon adi yeniden
 * tetiklenmiyordu. Compose'daki karsiligi `LaunchedEffect` anahtari —
 * `Unit` birakilirsa her yeniden bilesimde degil, HIC yeniden calismaz;
 * bilerek boyle, cunku bu hareket giriste bir kez olmali.
 */
@Composable
fun LkBrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    hareketli: Boolean = false
) {
    val kisitli = isReducedMotionEnabled()
    val calisir = hareketli && !kisitli

    val aci = remember { Animatable(if (calisir) -28f else 0f) }
    val olcek = remember { Animatable(if (calisir) 0.88f else 1f) }
    val saydamlik = remember { Animatable(if (calisir) 0f else 1f) }

    LaunchedEffect(calisir) {
        if (!calisir) {
            aci.snapTo(0f)
            olcek.snapTo(1f)
            saydamlik.snapTo(1f)
            return@LaunchedEffect
        }
        saydamlik.animateTo(1f, tween(220))
        /* Once 6°'yi asiyor, sonra yerine oturuyor: yayin taskin ucu. */
        aci.animateTo(6f, tween(460))
        aci.animateTo(0f, tween(240))
    }

    LaunchedEffect(calisir) {
        if (calisir) olcek.animateTo(1f, tween(520))
    }

    /*
     * 🔴 ISARET PNG OLARAK CIZILIYORDU VE ZEMINI SEFFAF DEGILDI: koyu bir
     * kare tasiyordu. Duz zeminde belli olmuyordu ama marka gradyaninin
     * (karsilama, acilis) uzerinde YAPISTIRILMIS BIR KUTU gibi
     * goruluyordu.
     *
     * Artik kodla ciziliyor — uygulama simgesindeki vektorle AYNI
     * geometri (`ic_launcher_foreground.xml`): agzi sagda acik halka,
     * altin baklava, merkez nokta. Renkler sabit; tema ile donmez (§3.5).
     */
    Canvas(
        modifier = modifier
            .size(size)
            .alpha(saydamlik.value)
            .scale(olcek.value)
            .rotate(aci.value)
    ) {
        val k = this.size.minDimension / 108f
        val kalinlik = 7f * k
        val yaricap = 24f * k
        val merkez = Offset(this.size.width / 2f, this.size.height / 2f)

        drawArc(
            color = Color.White,
            startAngle = 35f,
            sweepAngle = 290f,
            useCenter = false,
            topLeft = Offset(merkez.x - yaricap, merkez.y - yaricap),
            size = Size(yaricap * 2, yaricap * 2),
            style = Stroke(width = kalinlik, cap = StrokeCap.Round)
        )

        val baklava = Path().apply {
            moveTo(merkez.x, merkez.y - 16f * k)
            lineTo(merkez.x + 10f * k, merkez.y)
            lineTo(merkez.x, merkez.y + 16f * k)
            lineTo(merkez.x - 10f * k, merkez.y)
            close()
        }
        drawPath(baklava, color = Color(0xFFE3A857))

        drawCircle(color = Color.White, radius = 4.5f * k, center = merkez)
    }
}
