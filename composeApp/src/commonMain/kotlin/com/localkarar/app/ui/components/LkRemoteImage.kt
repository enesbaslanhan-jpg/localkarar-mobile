package com.localkarar.app.ui.components

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import coil3.compose.SubcomposeAsyncImageContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import com.localkarar.app.network.ApiConfig

/*
 * UZAK GORSEL — Coil 3.
 *
 * 🔴 SUNUCU GORELI YOL DONDURUYOR.
 *
 * `avatarUrl` ve `coverUrl` mutlak adres degil, `/auth/avatar/<dosya>`
 * seklinde GORELI yol donuyor (`src/services/auth.ts:174`). Web bunu
 * kendi kokune gore cozuyor; mobilde otomatik cozulmez ve Coil'e oldugu
 * gibi verilirse istek hicbir yere gitmez.
 *
 * `mutlakGorselUrl` taban adresi basa ekliyor. Zaten mutlak gelen
 * adresler oldugu gibi birakiliyor — sunucu ileride tam URL donmeye
 * baslarsa bu kod kirilmaz.
 *
 * ⚠️ Bu uclar KIMLIK DOGRULAMASI ISTEMIYOR (`/auth/avatar/:storedName`
 * herkese acik bir GET). Bu yuzden Coil'e ozel bir token gecirilmiyor;
 * istese de gerekmezdi. Uc bir gun korunmaya alinirsa burada Ktor
 * istemcisinin paylasilmasi gerekir.
 */
fun mutlakGorselUrl(yol: String?): String? {
    val temiz = yol?.trim().orEmpty()
    if (temiz.isEmpty()) return null
    if (temiz.startsWith("http://") || temiz.startsWith("https://")) return temiz
    val taban = ApiConfig.baseUrl.trimEnd('/')
    return if (temiz.startsWith("/")) taban + temiz else "$taban/$temiz"
}

/**
 * Uzak gorsel; yuklenirken ve hata durumunda `yedek` cizilir.
 *
 * `yedek` ZORUNLU. Gorsel gelmediginde bos bir kutu birakmak yerine bas
 * harf ya da desen gosteriliyor — ag yavassa ya da dosya silinmisse ekran
 * delik gorunmez.
 */
@Composable
fun LkRemoteImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    yedek: @Composable () -> Unit
) {
    val tam = mutlakGorselUrl(url)
    if (tam == null) {
        Box(modifier) { yedek() }
        return
    }
    SubcomposeAsyncImage(
        model = tam,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    ) {
        /*
         * ⚠️ Coil 3'te `painter.state` bir `StateFlow`, dogrudan `State`
         * DEGIL. `when (painter.state)` yazilirsa derleyici "her zaman
         * false" uyarisi verir ve hicbir dal calismaz.
         */
        val durum by painter.state.collectAsState()
        when (durum) {
            is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
            /* Yukleniyor ve hata AYNI yedegi gosteriyor: kullanicinin
               gordugu sey ikisinde de "gorsel yok", ayirmak bilgi vermiyor. */
            else -> Box(Modifier.fillMaxSize()) { yedek() }
        }
    }
}
