package com.localkarar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.localkarar.app.ui.theme.LkShapes

@Composable
expect fun LkPlatformVideoSurface(
    url: String,
    playing: Boolean,
    muted: Boolean,
    modifier: Modifier = Modifier
)

/**
 * Akista veri harcamayan kontrollu video: kendiliginden baslamaz. Ilk
 * dokunus sessiz oynatir, ikinci dokunus sesi acar. Buyutme ayni oynatma
 * durumunu tam ekran yuzeye tasir.
 */
@Composable
fun LkCommunityVideo(
    url: String,
    posterUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    allowFullscreen: Boolean = true
) {
    val fullUrl = mutlakGorselUrl(url) ?: return
    var started by remember(fullUrl) { mutableStateOf(false) }
    var playing by remember(fullUrl) { mutableStateOf(false) }
    var muted by remember(fullUrl) { mutableStateOf(true) }
    var fullscreen by remember(fullUrl) { mutableStateOf(false) }

    fun primaryAction() {
        when {
            !started -> { started = true; playing = true; muted = true }
            muted -> muted = false
            else -> playing = !playing
        }
    }

    @Composable
    fun VideoBox(boxModifier: Modifier, fullscreenMode: Boolean) {
        Box(
            modifier = boxModifier
                .background(Color.Black)
                .clickable { primaryAction() },
            contentAlignment = Alignment.Center
        ) {
            if (started) {
                LkPlatformVideoSurface(
                    url = fullUrl,
                    playing = playing,
                    muted = muted,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LkRemoteImage(
                    url = posterUrl,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize()
                ) { Box(Modifier.fillMaxSize().background(Color(0xFF101719))) }
                Box(
                    Modifier.size(52.dp).clip(CircleShape).background(Color(0xB3182225)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Videoyu oynat", tint = Color.White)
                }
            }

            if (started) {
                Icon(
                    if (muted) Icons.Outlined.VolumeOff else Icons.Outlined.VolumeUp,
                    contentDescription = if (muted) "Sesi aç" else "Sesi kapat",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(44.dp)
                        .background(Color(0x9910181B), CircleShape)
                )
            }
            if (allowFullscreen && !fullscreenMode) {
                IconButton(
                    onClick = { fullscreen = true },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Outlined.Fullscreen, contentDescription = "Tam ekran", tint = Color.White)
                }
            }
            if (fullscreenMode) {
                IconButton(
                    onClick = { fullscreen = false },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Outlined.Close, contentDescription = "Tam ekranı kapat", tint = Color.White)
                }
            }
        }
    }

    VideoBox(modifier.fillMaxWidth().clip(LkShapes.Card), false)

    if (fullscreen) {
        Dialog(
            onDismissRequest = { fullscreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            VideoBox(Modifier.fillMaxSize(), true)
        }
    }
}
