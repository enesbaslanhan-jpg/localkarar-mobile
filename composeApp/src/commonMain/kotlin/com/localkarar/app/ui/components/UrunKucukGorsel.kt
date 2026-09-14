package com.localkarar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.LkLineSoft
import com.localkarar.app.ui.theme.LkShapes
import com.localkarar.app.ui.theme.LkSurfaceCanvas
import com.localkarar.app.ui.theme.LkTextMuted

/*
 * URUN KUCUK GORSELI — siparis satiri ve urun kartinda ayni kutu.
 *
 * 🔴 NEDEN AYRI BILESEN: web'de urun gorseli 25.08.2026'da vardi; mobil
 * Urunler ekrani 28.08'de "web ile birebir" diye yazildi ama DTO'daki
 * imageUrl ekranda hic cizilmedi (15.09.2026'da fark edildi). Tek bilesen
 * olsun ki bir sonraki ekran gorseli yine unutmasin.
 *
 * Gorsel yoksa ya da yuklenemezse notr kutu + paket simgesi. Yer tutucu
 * ya da sahte gorsel URETILMEZ (web Products.jsx ile ayni ilke).
 */
@Composable
fun UrunKucukGorsel(url: String?, boyut: Dp = 44.dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(boyut)
            .clip(LkShapes.SM)
            .background(LkSurfaceCanvas, LkShapes.SM)
            .border(1.dp, LkLineSoft, LkShapes.SM),
        contentAlignment = Alignment.Center
    ) {
        LkRemoteImage(
            url = url,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            yedek = {
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = LkTextMuted,
                    modifier = Modifier.size(boyut * 0.45f)
                )
            }
        )
    }
}
