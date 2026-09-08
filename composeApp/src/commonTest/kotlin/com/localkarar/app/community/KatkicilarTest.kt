package com.localkarar.app.community

import androidx.compose.ui.graphics.Color
import com.localkarar.app.network.dto.CommunityAuthorDto
import com.localkarar.app.network.dto.CommunityPostDto
import com.localkarar.app.ui.screens.community.bahsetmeliMetin
import com.localkarar.app.ui.screens.community.etiketMetni
import com.localkarar.app.ui.screens.community.katkicilariCikar
import com.localkarar.app.ui.screens.community.metneEtiketEkle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/*
 * Katki listesi ve etiketleme kurallari WEBDEN birebir aliniyor
 * (`CommunityPage.jsx`). Bu testler o kurallarin sessizce
 * ayrilmamasini bekliyor.
 */
private fun gonderi(id: String, yazarId: Int?, yazarAdi: String?, metin: String = "x") =
    CommunityPostDto(
        id = id,
        summary = metin,
        author = yazarAdi?.let { CommunityAuthorDto(id = yazarId ?: 0, name = it) }
    )

class KatkicilarTest {

    @Test
    fun coktanAzaSiralanipDortleSinirlaniyor() {
        val posts = listOf(
            gonderi("1", 1, "Ada Yılmaz"),
            gonderi("2", 1, "Ada Yılmaz"),
            gonderi("3", 1, "Ada Yılmaz"),
            gonderi("4", 2, "Bora Demir"),
            gonderi("5", 2, "Bora Demir"),
            gonderi("6", 3, "Cem Ak"),
            gonderi("7", 4, "Derya Su"),
            gonderi("8", 5, "Efe Kaya")
        )
        val katkicilar = katkicilariCikar(posts)
        assertEquals(4, katkicilar.size, "web de ilk dortle siniriyor")
        assertEquals("Ada Yılmaz", katkicilar[0].ad)
        assertEquals(3, katkicilar[0].sayi)
        assertEquals("Bora Demir", katkicilar[1].ad)
        assertEquals(2, katkicilar[1].sayi)
    }

    /*
     * Yazari olmayan gonderi DUSURULMUYOR: webde de adsiz yazar
     * varsayilan bir adla sayiliyor. Dusurmek katki sayilarini
     * sessizce kuculturdu.
     */
    @Test
    fun yazarsizGonderiVarsayilanAdlaSayiliyor() {
        val katkicilar = katkicilariCikar(listOf(gonderi("1", null, null), gonderi("2", null, null)))
        assertEquals(1, katkicilar.size)
        assertEquals(2, katkicilar[0].sayi)
        assertEquals(null, katkicilar[0].id, "kimlik uydurulmuyor")
    }

    @Test
    fun bosAkistaKatkiciYok() {
        assertTrue(katkicilariCikar(emptyList()).isEmpty())
    }

    @Test
    fun etiketBosluklariAltCizgiyeCeviriyor() {
        assertEquals("@Ada_Yılmaz", etiketMetni("Ada Yılmaz"))
        assertEquals("@Ada_Yılmaz", etiketMetni("  Ada   Yılmaz  "), "fazla bosluklar tek alt cizgi")
        assertEquals("@Ada", etiketMetni("Ada"))
    }

    @Test
    fun etiketMetninSonunaBoslukKorunarakEkleniyor() {
        assertEquals("@Ada_Yılmaz ", metneEtiketEkle("", "Ada Yılmaz"))
        assertEquals("Merhaba @Ada_Yılmaz ", metneEtiketEkle("Merhaba", "Ada Yılmaz"))
        assertEquals(
            "Merhaba @Ada_Yılmaz ",
            metneEtiketEkle("Merhaba ", "Ada Yılmaz"),
            "zaten boslukla bitiyorsa ikinci bosluk eklenmiyor"
        )
    }

    @Test
    fun etiketGosterilirkenAltCizgilerBoslugaDonuyor() {
        val sonuc = bahsetmeliMetin("Merhaba @Ada_Yılmaz, bak", Color.Red)
        assertEquals("Merhaba @Ada Yılmaz, bak", sonuc.text)
        assertEquals(1, sonuc.spanStyles.size, "yalniz etiket vurgulaniyor")
    }

    /*
     * E-posta adresi etiket DEGIL: `@` ancak satir basinda ya da
     * bosluktan sonra etiket baslatiyor.
     */
    @Test
    fun epostaAdresiEtiketSayilmiyor() {
        val sonuc = bahsetmeliMetin("bana ada@ornek.com adresinden yaz", Color.Red)
        assertEquals("bana ada@ornek.com adresinden yaz", sonuc.text)
        assertTrue(sonuc.spanStyles.isEmpty())
    }

    @Test
    fun yalnizAtIsaretiVurgulanmiyor() {
        val sonuc = bahsetmeliMetin("fiyat @ 50 TL", Color.Red)
        assertEquals("fiyat @ 50 TL", sonuc.text)
        assertTrue(sonuc.spanStyles.isEmpty())
    }
}
