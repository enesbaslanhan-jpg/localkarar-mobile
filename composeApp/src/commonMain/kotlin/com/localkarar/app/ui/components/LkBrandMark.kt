package com.localkarar.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import localkarar_mobile.composeapp.generated.resources.Res
import localkarar_mobile.composeapp.generated.resources.local_karar_mark
import org.jetbrains.compose.resources.painterResource

@Composable
fun LkBrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    Image(
        painter = painterResource(Res.drawable.local_karar_mark),
        contentDescription = "LocalKarar",
        contentScale = ContentScale.Fit,
        modifier = modifier.size(size)
    )
}
