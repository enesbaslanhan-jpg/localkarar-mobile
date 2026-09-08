package com.localkarar.app.ui.screens.community

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.localkarar.app.network.dto.CommunityPostDto

/**
 * Akista en cok paylasan kisiler.
 *
 * ⚠️ SUNUCU UCU YOK, webde de yok. Liste YUKLENMIS akistan turetiliyor
 * (`CommunityPage.jsx:298`): yazara gore say, coktan aza sirala, ilk
 * dordu al. Yani "toplulugun en cok katki vereni" degil, GORULEN
 * akista en cok paylasani -- uydurma bir itibar puani hesaplanmiyor.
 *
 * ⚠️ TEK YERDE: hem "Katkı sağlayanlar" karti hem paylasim yazarken
 * acilan etiket listesi bunu kullaniyor. Webde de ikisi ayni `contributors`
 * dizisinden besleniyor; iki ayri hesap birbirinden sessizce ayrilirdi.
 */
internal data class Katkici(
    val id: Int?,
    val ad: String,
    val avatarUrl: String?,
    val sayi: Int
)

internal const val KATKICI_SAYISI = 4

internal fun katkicilariCikar(
    posts: List<CommunityPostDto>,
    enFazla: Int = KATKICI_SAYISI
): List<Katkici> =
    posts.groupBy { it.author?.name?.takeIf { ad -> ad.isNotBlank() } ?: "Topluluk üyesi" }
        .map { (ad, gonderiler) ->
            Katkici(
                id = gonderiler.firstNotNullOfOrNull { it.author?.id },
                ad = ad,
                avatarUrl = gonderiler.firstNotNullOfOrNull { it.author?.avatarUrl },
                sayi = gonderiler.size
            )
        }
        .sortedByDescending { it.sayi }
        .take(enFazla)

/**
 * Bir adi metne eklenecek etikete cevirir.
 *
 * ⚠️ BOSLUKLAR ALT CIZGIYE DONUYOR ve bu webin kurali
 * (`CommunityPage.jsx:588`). Gonderi govdesinde etiket
 * `@Ad_Soyad` olarak duruyor, ekranda gosterilirken alt cizgiler
 * bosluga geri ceviriliyor. Mobilde farkli bir bicim kullanmak, ayni
 * gonderiyi webde bozuk gosterirdi.
 */
internal fun etiketMetni(ad: String): String =
    "@" + ad.trim().split(Regex("\\s+")).joinToString("_")

/**
 * Etiketi yazilmakta olan metnin sonuna ekler.
 *
 * ⚠️ Kural webden birebir (`CommunityPage.jsx:589`): metin bosluk ile
 * bitmiyorsa once bir bosluk, sonra etiket, sonra bir bosluk daha --
 * boylece kullanici yazmaya devam edince etiket bitisik kalmiyor.
 */
internal fun metneEtiketEkle(mevcut: String, ad: String): String {
    val ayrac = if (mevcut.isNotEmpty() && !mevcut.endsWith(" ")) " " else ""
    return "$mevcut$ayrac${etiketMetni(ad)} "
}

/**
 * Gonderi metnindeki etiketleri gorunur kilar.
 *
 * 🔴 Etiket seciciyi eklemek TEK BASINA yarim is olurdu: gonderi
 * govdesinde etiket `@Ad_Soyad` olarak duruyor ve mobil bunu oldugu
 * gibi, alt cizgileriyle basiyordu. Web ayni metni vurgulu ve alt
 * cizgileri BOSLUGA cevirerek gosteriyor (`CommunityPage.jsx:887`) --
 * ayni gonderi iki platformda iki turlu okunuyordu.
 *
 * ⚠️ Yalnizca GORUNUM degisiyor; sunucuya giden ve sunucudan gelen
 * metne dokunulmuyor.
 *
 * ⚠️ Etiket bir BAGLANTI degil. Metindeki ad bir kullaniciya
 * baglanmiyor (sunucu bahsetme varligi tutmuyor); dokunulabilir yapmak
 * hangi kisiye gidecegini TAHMIN etmek olurdu.
 */
internal fun bahsetmeliMetin(metin: String, vurguRengi: Color): AnnotatedString =
    buildAnnotatedString {
        var i = 0
        while (i < metin.length) {
            val harf = metin[i]
            /* E-posta adresindeki `@` etiket sayilmasin: etiket ancak
               satir basinda ya da bosluktan sonra baslayabilir. */
            val etiketBaslayabilir = i == 0 || metin[i - 1].isWhitespace()
            if (harf == '@' && etiketBaslayabilir) {
                var j = i + 1
                while (j < metin.length && (metin[j].isLetterOrDigit() || metin[j] == '_')) j++
                if (j > i + 1) {
                    withStyle(SpanStyle(color = vurguRengi, fontWeight = FontWeight.SemiBold)) {
                        append(metin.substring(i, j).replace('_', ' '))
                    }
                    i = j
                    continue
                }
            }
            append(harf)
            i++
        }
    }
