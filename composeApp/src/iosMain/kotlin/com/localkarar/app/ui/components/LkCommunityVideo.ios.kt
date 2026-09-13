package com.localkarar.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitViewController
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVKit.AVPlayerViewController
import platform.Foundation.NSURL

@Composable
actual fun LkPlatformVideoSurface(
    url: String,
    playing: Boolean,
    muted: Boolean,
    modifier: Modifier
) {
    val player = remember(url) {
        val nsUrl = NSURL.URLWithString(url)
        AVPlayer(playerItem = nsUrl?.let { AVPlayerItem(uRL = it) })
    }
    LaunchedEffect(player, playing, muted) {
        player.muted = muted
        if (playing) player.play() else player.pause()
    }
    DisposableEffect(player) {
        onDispose { player.pause(); player.replaceCurrentItemWithPlayerItem(null) }
    }
    UIKitViewController(
        factory = {
            AVPlayerViewController().apply {
                this.player = player
                showsPlaybackControls = false
            }
        },
        modifier = modifier,
        update = { it.player = player }
    )
}
