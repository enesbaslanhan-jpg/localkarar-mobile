package com.localkarar.app.calculations

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/*
 * SAPMA TESTI — Kotlin kopyasi sunucudaki formulle ayni sonucu vermeli.
 *
 * Beklenen degerler ELLE YAZILMADI: sunucunun kendi fonksiyonu
 * (`src/services/formulas.ts` -> `calculatePriceArchitecture`) `tsx` ile
 * calistirilip ciktisi buraya pinlendi (04.09.2026).
 *
 * Sunucudaki formul degisir de bu kopya guncellenmezse bu test kirilir --
 * kopyanin sessizce yanlis fiyat gostermesini engelleyen tek sey bu.
 *
 * ⚠️ Yeni senaryo eklerken beklenen degeri KAFADAN yazma; sunucudaki
 * fonksiyondan uret. Kafadan yazilan bir beklenen deger, iki tarafi da
 * yanlis olan bir teste donusur.
 */
class PriceArchitectureTest {

    private fun hesapla(
        maliyet: Double, operasyon: Double, sabit: Double, iade: Double,
        komisyon: Double, odeme: Double, marj: Double
    ) = calculatePriceArchitecture(maliyet, operasyon, sabit, iade, komisyon, odeme, marj)

    @Test
    fun mockupBenzeriSenaryo() {
        val s = hesapla(850.0, 0.0, 0.0, 0.0, 0.0, 0.0, 40.0)!!
        assertEquals(850.0, s.gercekBirimMaliyet)
        assertEquals(1416.67, s.onerilenKdvHaricFiyat)
        assertEquals(566.67, s.birimKatki)
        assertEquals(40.0, s.gerceklesenMarj)
    }

    @Test
    fun pazaryeriSenaryosu() {
        val s = hesapla(850.0, 45.0, 60.0, 25.0, 18.0, 2.5, 25.0)!!
        assertEquals(980.0, s.gercekBirimMaliyet)
        assertEquals(1798.17, s.onerilenKdvHaricFiyat)
        assertEquals(449.54, s.birimKatki)
        assertEquals(25.0, s.gerceklesenMarj)
    }

    @Test
    fun dusukMarjSenaryosu() {
        val s = hesapla(1200.0, 100.0, 150.0, 50.0, 12.0, 1.8, 10.0)!!
        assertEquals(1968.5, s.onerilenKdvHaricFiyat)
        assertEquals(196.85, s.birimKatki)
        assertEquals(10.0, s.gerceklesenMarj)
    }

    @Test
    fun yuksekMarjSenaryosu() {
        val s = hesapla(300.0, 20.0, 15.0, 5.0, 8.0, 2.0, 60.0)!!
        assertEquals(1133.33, s.onerilenKdvHaricFiyat)
        assertEquals(680.0, s.birimKatki)
        assertEquals(60.0, s.gerceklesenMarj)
    }

    @Test
    fun sifirMaliyetSifirFiyat() {
        val s = hesapla(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 30.0)!!
        assertEquals(0.0, s.onerilenKdvHaricFiyat)
        assertEquals(0.0, s.gerceklesenMarj)
    }

    /**
     * Sunucu bu durumda HATA FIRLATIYOR; kopya `null` donuyor.
     *
     * Fark bilincli: onizlemede istisna, kaydirici surukleneken uygulamayi
     * cokertirdi. Cagiran taraf `null`i "bu oranlarla fiyat hesaplanamaz"
     * olarak gostermeli.
     */
    @Test
    fun toplamOranYuzdeYuzeUlasirsaNull() {
        assertNull(hesapla(500.0, 0.0, 0.0, 0.0, 50.0, 10.0, 40.0))
        assertNull(hesapla(500.0, 0.0, 0.0, 0.0, 60.0, 20.0, 30.0))
    }
}
