package com.localkarar.app.core

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

actual fun gorseliCoz(bytes: ByteArray): ImageBitmap? = try {
    /* Onizleme icin tam cozunurluk gereksiz; buyuk fotograflar 4x kucultulur. */
    val secenek = BitmapFactory.Options().apply { inSampleSize = if (bytes.size > 2_000_000) 4 else if (bytes.size > 600_000) 2 else 1 }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, secenek)?.asImageBitmap()
} catch (e: Throwable) {
    null
}
