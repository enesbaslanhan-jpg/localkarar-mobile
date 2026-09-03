package com.localkarar.app.network

import com.localkarar.app.network.dto.CanonicalSectionsDto
import com.localkarar.app.ui.screens.courses.hesaplamaHedefi
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * KANONIK DERS BOLUMLERI — SOZLESME TESTI.
 *
 * Govde UYDURMA DEGIL: calisan sunucudan
 * `GET /courses/439/lessons/1254` ile alinan gercek yanittan kopyalandi
 * (03.09.2026, ders CANON-COURSE-001).
 *
 * ⚠️ NEDEN KATI KIP (`ignoreUnknownKeys = false`)?
 *
 * Bu projede ayni hata iki kez cikti: DTO sunucunun gonderdigi alani
 * TANIMAMASINA ragmen hata VERMIYORDU, cunku uretim istemcisi bilinmeyen
 * anahtarlari yok sayiyor. Pazaryeri ekraninda tum veri uydurma cikti;
 * ogrenme yolunda alan adi ve konu baglantilari sessizce dustu.
 *
 * Burada katı kip, sunucunun gonderdigi ama DTO'nun tanimadigi HER alani
 * hataya cevirir. Uretim yolu hosgorulu kalir; hatayi test yakalar.
 */
class CanonicalSectionsContractTest {

    private val katiJson = Json { ignoreUnknownKeys = false; isLenient = false }

    private val gercekYanit = """
    {
      "body": "# Gerçek Birim Maliyet Hesaplama Pusulası\n\n## Pratik Karar: \"...\"\n\nGövde metni.",
      "decision": {
        "toolCode": "DC-PROFIT-001",
        "toolTitle": "Ürünüm Gerçekten Kârlı mı?",
        "context": "Ürününüzün ham maliyet tuzağına düşmesini engellemek için ...",
        "bullets": [],
        "result": "Sonuç: gerekçeli karar fişi"
      },
      "extraDecisions": [],
      "calculations": [
        {
          "label": "Gerçek Birim Maliyet Hesaplayıcısını Aç",
          "calculationId": "unit-cost",
          "title": "Gerçek Birim Maliyet",
          "hasSimple": true,
          "hasDetailed": false
        }
      ],
      "formulaCards": [
        {
          "title": "Gerçek Birim Maliyet Formülü",
          "description": "Bir ürünün gerçek ekonomik yükünü görmek için şu formülü uygulayın:",
          "formulas": ["\\text{Gerçek Birim Maliyet} = \\text{Birim Malzeme} + \\text{Birim İşçilik}"],
          "example": null,
          "interpretation": "",
          "calculationId": "unit-cost",
          "decisionToolCode": null,
          "decisionToolTitle": null
        }
      ],
      "mistakeCards": [
        {
          "title": "Sabit Personel Maliyeti Dağıtımı",
          "wrong": "İşçiyi zaten aylık sabit maaşla çalıştırıyorum...",
          "correct": "Çalışanın zamanı kısıtlı bir kaynaktır..."
        }
      ]
    }
    """.trimIndent()

    @Test
    fun gercek_yanit_kati_kipte_ayristirilir() {
        val s = katiJson.decodeFromString<CanonicalSectionsDto>(gercekYanit)
        assertEquals("DC-PROFIT-001", s.decision?.toolCode)
        assertEquals("Ürünüm Gerçekten Kârlı mı?", s.decision?.toolTitle)
        assertEquals(1, s.calculations.size)
        assertEquals(1, s.formulaCards.size)
        assertEquals(1, s.mistakeCards.size)
    }

    @Test
    fun hesaplama_kimligi_mobil_kataloga_cozulur() {
        val s = katiJson.decodeFromString<CanonicalSectionsDto>(gercekYanit)
        /*
         * Sunucu KATALOG KIMLIGI gonderiyor ("unit-cost"); mobil bunu kendi
         * `CalculationCatalog` tablosundan hedef ekrana cozuyor. Iki katalog
         * ayrisirsa dugme sessizce kaybolur -- bu test onu yakalar.
         */
        val hedef = hesaplamaHedefi(s.calculations[0].calculationId)
        assertNotNull(hedef, "unit-cost mobil katalogda bulunamadi")
        assertEquals("birim_maliyet", hedef.first)
        assertEquals(false, hedef.second, "hizli hesaplama bekleniyordu")
    }

    @Test
    fun formul_karti_gercek_latex_tasiyor() {
        val s = katiJson.decodeFromString<CanonicalSectionsDto>(gercekYanit)
        val formul = s.formulaCards[0].formulas.first()
        // Ham `$$` sarmalayicisi sunucuda soyuluyor; icerde gercek komut kaliyor.
        assertTrue(formul.contains("\\text"), "LaTeX komutu bekleniyordu: $formul")
        assertTrue(!formul.contains("$$"), "\$\$ sarmalayicisi sizmis: $formul")
    }

    @Test
    fun katalogda_olmayan_kimlik_icin_hedef_uretilmez() {
        /*
         * "Sahte route uretme" kurali: cozulemeyen bir kimlik icin dugme
         * BASILMAMALI. Yanlis hesaplamayi acmak, hic acmamaktan kotudur.
         */
        assertNull(hesaplamaHedefi("boyle-bir-hesaplama-yok"))
        assertNull(hesaplamaHedefi(null))
        assertNull(hesaplamaHedefi(""))
    }

    @Test
    fun detayli_analiz_modeli_dogru_hedefe_gider() {
        // `cash-conversion-cycle` katalogda yalniz modelCode tasiyor.
        val hedef = hesaplamaHedefi("cash-conversion-cycle")
        assertNotNull(hedef)
        assertEquals("CASH_CONVERSION_CYCLE", hedef.first)
        assertEquals(true, hedef.second, "detayli analiz bekleniyordu")
    }

    @Test
    fun bos_bolum_govdesi_cokmez() {
        val s = katiJson.decodeFromString<CanonicalSectionsDto>("""{"body":""}""")
        assertNull(s.decision)
        assertEquals(emptyList(), s.calculations)
        assertEquals(emptyList(), s.formulaCards)
        assertEquals(emptyList(), s.mistakeCards)
    }
}
