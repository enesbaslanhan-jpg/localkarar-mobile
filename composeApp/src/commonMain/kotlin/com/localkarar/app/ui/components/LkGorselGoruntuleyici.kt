package com.localkarar.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.localkarar.app.ui.theme.LkShapes

/*
 * TAM EKRAN GORSEL GORUNTULEYICI (urun sahibi, 19.09.2026: "uzerine tiklayinca
 * buyutmuyor"). Siyah zemin, gorsel sigdirilir, iki parmakla yaklastirilir ve
 * kaydirilir; kapatmak icin sag ustteki X ya da bos alana dokunma.
 * Kaynak ya bellekteki bitmap (paylasim onizlemesi) ya uzak adres (akis).
 */
@Composable
fun LkGorselGoruntuleyici(
    bitmap: ImageBitmap? = null,
    url: String? = null,
    aciklama: String? = null,
    onKapat: () -> Unit
) {
    var olcek by remember { mutableStateOf(1f) }
    var kaydirma by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    Dialog(onDismissRequest = onKapat, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(onClick = onKapat)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        olcek = (olcek * zoom).coerceIn(1f, 5f)
                        kaydirma = if (olcek > 1f) kaydirma + pan else androidx.compose.ui.geometry.Offset.Zero
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            val donusum = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = olcek; scaleY = olcek
                    translationX = kaydirma.x; translationY = kaydirma.y
                }
            if (bitmap != null) {
                Image(bitmap = bitmap, contentDescription = aciklama, contentScale = ContentScale.Fit, modifier = donusum)
            } else if (!url.isNullOrBlank()) {
                LkRemoteImage(url = url, contentDescription = aciklama, contentScale = ContentScale.Fit, modifier = donusum) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LkLoadingSpinner(size = 28.dp) }
                }
            }
            Icon(
                Icons.Outlined.Close,
                contentDescription = "Kapat",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 52.dp, end = 16.dp)
                    .size(36.dp)
                    .clip(LkShapes.FULL)
                    .background(Color.White.copy(alpha = 0.18f))
                    .clickable(onClick = onKapat)
                    .padding(6.dp)
            )
        }
    }
}
