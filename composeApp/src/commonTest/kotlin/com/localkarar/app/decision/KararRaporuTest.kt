package com.localkarar.app.decision

import com.localkarar.app.network.dto.BusinessRecordDto
import com.localkarar.app.network.dto.FinancialModelSummaryDto
import com.localkarar.app.network.dto.KararGunluguDto
import com.localkarar.app.network.dto.KararModelCalismasiDto
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/*
 * KARAR RAPORU BIRLESTIRME MANTIGI.
 *
 * 🔴 Veri iki yerde YAZILIYOR, hicbir yerde okunmuyordu: karar araci
 * takipleri kaydin metadata'sinda, finansal model kararlari ise
 * sunucuda okuma ucu bile olmayan bir tabloda.
 *
 * En kritik iddia: UYDURMA SAYI YOK. Ne sapma yuzdesi ne karar basari
 * orani -- ikisi de serbest metinden cikarilamaz.
 */

private fun kayit(
    id: String,
    baslik: String,
    durum: String = "open",
    olusturma: String? = "2026-08-01T10:00:00.000Z",
    takip: Map<String, String>? = null
) = BusinessRecordDto(
    id = id,
    workspaceId = "ws-1",
    type = "task",
    title = baslik,
    status = durum,
    createdAt = olusturma,
    metadata = takip?.let {
        mapOf("decisionFollowUp" to JsonObject(it.mapValues { (_, v) -> JsonPrimitive(v) }))
    } ?: emptyMap()
)

class KararRaporuTest {

    @Test
    fun takip_hedeflenen_ve_gerceklesen_sonucu_tasiyor() {
        val satir = takipSatiri(
            kayit(
                "rec-1", "Tedarikçiyle pazarlık",
                takip = mapOf(
                    "decisionTitle" to "Alım fiyatını düşür",
                    "expectedOutcome" to "Birim maliyet %8 düşsün",
                    "actualOutcome" to "%5 düştü"
                )
            )
        )
        assertEquals("Alım fiyatını düşür", satir.baslik)
        assertEquals("Tedarikçiyle pazarlık", satir.altBaslik)
        assertEquals("Birim maliyet %8 düşsün", satir.beklenen)
        assertEquals("%5 düştü", satir.gerceklesen)
        assertEquals("rec-1", satir.kayitId)
    }

    /* Karar basligi yoksa uydurulmuyor; gorevin kendi basligi yaziliyor. */
    @Test
    fun karar_basligi_yoksa_gorev_basligi_kullaniliyor() {
        val satir = takipSatiri(kayit("rec-2", "Vitrini yenile", takip = mapOf("expectedOutcome" to "Giriş artsın")))
        assertEquals("Vitrini yenile", satir.baslik)
        assertNull(satir.altBaslik)
    }

    /* 🔴 Karar araci tarafinda sapma alani HIC YOK. Uydurulmuyor. */
    @Test
    fun karar_aracinda_sapma_uretilmiyor() {
        val satir = takipSatiri(
            kayit("rec-3", "İş", takip = mapOf("expectedOutcome" to "A", "actualOutcome" to "B"))
        )
        assertNull(satir.sapma)
    }

    @Test
    fun iki_kaynak_tek_listede_ve_yeniden_eskiye_siralaniyor() {
        val satirlar = kararSatirlari(
            takipler = listOf(kayit("rec-1", "Eski iş", olusturma = "2026-08-01T10:00:00.000Z")),
            gunluk = listOf(
                KararGunluguDto(
                    id = "dj-1",
                    decision = "Tahsilatı sıkılaştır",
                    expectedOutcome = "Cari oran 2,2",
                    createdAt = "2026-08-09T10:00:00.000Z",
                    modelRun = KararModelCalismasiDto(model = FinancialModelSummaryDto(name = "Likidite"))
                )
            )
        )
        assertEquals(2, satirlar.size)
        assertEquals("Tahsilatı sıkılaştır", satirlar[0].baslik)
        assertEquals(KararKaynagi.FINANSAL_MODEL, satirlar[0].kaynak)
        assertEquals("Likidite", satirlar[0].altBaslik)
        /* Finansal model kararinin acilacak bir gorevi yok. */
        assertNull(satirlar[0].kayitId)
    }

    @Test
    fun ozet_suzgecten_bagimsiz_hesaplaniyor() {
        val satirlar = kararSatirlari(
            takipler = listOf(
                kayit("rec-1", "Biten", takip = mapOf("expectedOutcome" to "A", "actualOutcome" to "B")),
                kayit("rec-2", "Bekleyen", takip = mapOf("expectedOutcome" to "C"))
            ),
            gunluk = emptyList()
        )
        val ozet = kararOzeti(satirlar)
        assertEquals(2, ozet.toplam)
        assertEquals(1, ozet.degerlendirilen)
        assertEquals(1, ozet.bekleyen)

        /* Suzgec uygulandiginda toplam DUSMUYOR: "2 karardan 1'i
           degerlendirildi" cumlesi yanlisa donmesin. */
        val suzulmus = kararSuz(satirlar, KararSuzgeci.BEKLEYEN)
        assertEquals(1, suzulmus.size)
        assertEquals(2, kararOzeti(satirlar).toplam)
    }

    /*
     * ANA SAYFA: bitmis VE sonucu yazilmis kararlar yer kaplamiyor,
     * sonucu bekleyenler basa aliniyor.
     */
    @Test
    fun ana_sayfa_kapanmis_karari_gostermiyor() {
        val gorevler = anaSayfaKararGorevleri(
            listOf(
                kayit("rec-1", "Kapanmış", durum = "completed", takip = mapOf("expectedOutcome" to "A", "actualOutcome" to "B")),
                kayit("rec-2", "Açık", takip = mapOf("expectedOutcome" to "C"))
            )
        )
        assertEquals(1, gorevler.size)
        assertEquals("Açık", gorevler[0].baslik)
    }

    @Test
    fun ana_sayfa_sonucu_bekleyeni_basa_aliyor() {
        val gorevler = anaSayfaKararGorevleri(
            listOf(
                kayit("rec-1", "Açık iş", takip = mapOf("expectedOutcome" to "A")),
                /* Bitmis ama sonucu yazilmamis: karar takibinin yarim
                   kaldigi yer, en aciL olan bu. */
                kayit("rec-2", "Sonucu bekleyen", durum = "completed", takip = mapOf("expectedOutcome" to "B"))
            )
        )
        assertEquals("Sonucu bekleyen", gorevler[0].baslik)
        assertTrue(gorevler.all { it.sonucBekliyor })
    }

    @Test
    fun ana_sayfa_en_fazla_bes_satir_veriyor() {
        val cok = (1..9).map { kayit("rec-$it", "İş $it", takip = mapOf("expectedOutcome" to "A")) }
        assertEquals(5, anaSayfaKararGorevleri(cok).size)
    }

    /* Takibi olmayan kayit gelirse cokmemeli: metadata bos olabilir. */
    @Test
    fun takip_verisi_yoksa_cokmuyor() {
        val satir = takipSatiri(kayit("rec-9", "Takipsiz"))
        assertEquals("Takipsiz", satir.baslik)
        assertNull(satir.beklenen)
        assertTrue(satir.sonucBekliyor)
    }
}
