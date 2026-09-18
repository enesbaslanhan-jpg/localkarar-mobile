package com.localkarar.app.core

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Bellekteki gorsel baytlarini (JPEG/PNG) ekranda cizilebilir bitmap'e cevirir.
 *
 * Paylasim yazarken secilen fotografin ONIZLEMESI icin (19.09.2026): sunucu
 * URL'ini beklemek gerekmez; secildigi an gorunur. Cozulemezse null.
 */
expect fun gorseliCoz(bytes: ByteArray): ImageBitmap?
