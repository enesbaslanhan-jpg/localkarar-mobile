package com.localkarar.app.ui

import com.localkarar.app.ui.components.MathNode
import com.localkarar.app.ui.components.MetinParcasi
import com.localkarar.app.ui.components.matematikAyir
import com.localkarar.app.ui.components.parseLatex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * LaTeX AYRISTIRICI TESTLERI.
 *
 * 🔴 BURADAKI FORMULLER UYDURULMADI. Hepsi yayimdaki canonical icerikten
 * alindi (02.09.2026 olcumu: 337 KO'nun 39'unda matematik var).
 *
 * Onceden bu ifadeler kullaniciya HAM METIN olarak gorunuyordu; webde ise
 * KaTeX ile ciziliyorlardi. Testler o farkin geri gelmemesi icin.
 *
 * Olculen komut dagilimi:
 *   \text 505  \times 78  \frac 75  \% 33  \approx 19
 *   \left 12   \right 12  \div 7    \sum 1  \ge 1
 */
class LkMathTest {

    private fun duzMetin(dugumler: List<MathNode>): String =
        dugumler.joinToString("") { dugum ->
            when (dugum) {
                is MathNode.Text -> dugum.value
                is MathNode.Fraction ->
                    "(" + duzMetin(dugum.numerator) + ")/(" + duzMetin(dugum.denominator) + ")"
            }
        }

    @Test
    fun textKomutuIcerigiAcikCikarir() {
        // Icerikteki en sik komut, 505 kullanim.
        val dugumler = parseLatex("""\text{Net Tahsilat} = \text{Brüt Satış}""")
        assertEquals("Net Tahsilat = Brüt Satış", duzMetin(dugumler))
    }

    @Test
    fun sembollerUnicodeKarsiliginaCevrilir() {
        assertEquals("2 × 3", duzMetin(parseLatex("""2 \times 3""")))
        assertEquals("6 ÷ 2", duzMetin(parseLatex("""6 \div 2""")))
        assertEquals("≈ 100", duzMetin(parseLatex("""\approx 100""")))
        assertEquals("x ≥ 5", duzMetin(parseLatex("""x \ge 5""")))
        assertEquals("∑", duzMetin(parseLatex("""\sum""")))
    }

    @Test
    fun kacisliYuzdeIsaretiCozulur() {
        // Icerikte 33 kez geciyor: `$\%27.4$`
        assertEquals("%27.4", duzMetin(parseLatex("""\%27.4""")))
    }

    @Test
    fun kesirPayVePaydayaAyrilir() {
        val dugumler = parseLatex("""\frac{a}{b}""")
        assertEquals(1, dugumler.size)
        val kesir = dugumler[0]
        assertTrue(kesir is MathNode.Fraction)
        assertEquals("a", duzMetin(kesir.numerator))
        assertEquals("b", duzMetin(kesir.denominator))
    }

    @Test
    fun icIceSusluParantezlerDogruSayilir() {
        /*
         * 🔴 EN KOLAY YAPILAN HATA: ilk `}` karakterinde durmak.
         *
         * Gercek formullerde pay `\text{...}` iceriyor, yani kesirin payi
         * kendi suslu parantezini tasiyor. Derinlik sayilmazsa pay yarim
         * kesilir ve formul SESSIZCE yanlis gorunur.
         */
        val dugumler = parseLatex("""\frac{\text{Hedeflenen Net Gelir}}{\text{Toplam Maliyet}}""")
        val kesir = dugumler.single()
        assertTrue(kesir is MathNode.Fraction)
        assertEquals("Hedeflenen Net Gelir", duzMetin(kesir.numerator))
        assertEquals("Toplam Maliyet", duzMetin(kesir.denominator))
    }

    @Test
    fun gercekIcerikFormuluTamAyristirilir() {
        // Turda ekranda HAM gorunen formulun ta kendisi.
        val ham = """\text{Minimum Satış Fiyatı} = \frac{\text{Hedeflenen Net Gelir} + """ +
            """\text{Sabit Kargo Bedeli}}{1 - \text{Platform Komisyon Oranı} - """ +
            """\text{Hedef Ülke Vergi Oranı}}"""

        val sonuc = duzMetin(parseLatex(ham))

        assertEquals(
            "Minimum Satış Fiyatı = (Hedeflenen Net Gelir + Sabit Kargo Bedeli)/" +
                "(1 - Platform Komisyon Oranı - Hedef Ülke Vergi Oranı)",
            sonuc
        )
    }

    @Test
    fun leftRightYalnizcaBoyutlandirmaIsaretidir() {
        // `\left(` ve `\right)` parantezi KENDISI uretmiyor; parantez zaten
        // bir sonraki karakter olarak geliyor. Ikisini de yazmak `((` verirdi.
        assertEquals("(a + b)", duzMetin(parseLatex("""\left( a + b \right)""")))
    }

    @Test
    fun bilinmeyenKomutSESSIZCE_ATILMAZ() {
        /*
         * Icerige yeni bir komut girerse ekranda `\sqrt` olarak gorunur.
         * Yanlis ama GORUNUR. Sessizce yutmak formulun eksik bir degeri
         * gostermesine yol acardi -- finansal icerikte bu kabul edilemez.
         */
        val sonuc = duzMetin(parseLatex("""\sqrt{16}"""))
        assertTrue(sonuc.contains("\\sqrt"), "bilinmeyen komut kaybolmamali: $sonuc")
    }

    @Test
    fun blokVeSatirIciMatematikAyrilir() {
        // Kotlin ham dizgesinde `$a` sablon degiskeni sayilir; `${'$'}` ile
        // gercek dolar isareti yaziliyor.
        val D = "$"
        val parcalar = matematikAyir("Sonuç: ${D}${D}a = b${D}${D} ve ayrıca ${D}x = 1${D} olur.")

        val matematikler = parcalar.filterIsInstance<MetinParcasi.Matematik>()
        assertEquals(2, matematikler.size)
        assertTrue(matematikler[0].blok, "\$\$ blok olmali")
        assertEquals("a = b", matematikler[0].latex)
        assertTrue(!matematikler[1].blok, "tek \$ satir ici olmali")
        assertEquals("x = 1", matematikler[1].latex)
    }

    @Test
    fun matematiksizMetinTEK_PARCA_kalir() {
        // Icerigin cogunda `$` yok; o durumda eski cizim yolu korunmali.
        val parcalar = matematikAyir("Bu paragrafta formül yok.")
        assertEquals(1, parcalar.size)
        assertTrue(parcalar[0] is MetinParcasi.Duz)
    }

    @Test
    fun kapanmamisSinirlayiciMetinOlarakKalir() {
        /*
         * Bozuk bir `$` yuzunden geri kalan TUM icerigi formul sanmak,
         * tek bir yazim hatasinin sayfayi goturmesi demekti.
         */
        val parcalar = matematikAyir("Fiyat 100$ civarında ve devam eden metin.")
        assertTrue(parcalar.all { it is MetinParcasi.Duz }, "kapanmamis \$ formul sayilmamali")
    }

    @Test
    fun kacisliDolarIsaretiMetindir() {
        val parcalar = matematikAyir("""Tutar \${'$'}100 olarak yazıldı.""")
        assertTrue(parcalar.all { it is MetinParcasi.Duz })
    }
}
