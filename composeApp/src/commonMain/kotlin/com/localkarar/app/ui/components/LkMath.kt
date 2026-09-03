package com.localkarar.app.ui.components

/**
 * KUCUK BIR LaTeX AYRISTIRICISI.
 *
 * 🔴 NEDEN TAM BIR KaTeX PORTU DEGIL:
 *
 * Webde formuller `katex` + `remark-math` + `rehype-katex` ile ciziliyor;
 * mobilde hicbir karsiligi yoktu ve `$$...$$` blogu kullaniciya HAM METIN
 * olarak gorunuyordu ("$$\text{Minimum Satis Fiyati} = \frac{...}$$").
 *
 * Cozumden once icerik OLCULDU (02.09.2026, yayimdaki 337 KO'nun 39'unda
 * matematik var). Kullanilan komutlarin TAMAMI:
 *
 *     \text 505   \times 78   \frac 75   \% 33   \approx 19
 *     \left 12    \right 12   \div 7     \sum 1   \ge 1
 *
 * Ust simge, alt simge, matris, integral, kok YOK. Bunlar sembolik matematik
 * degil, kelimelerden kurulu IS FORMULLERI:
 *
 *     \text{Net Tahsilat} = \text{Brut Satis} - (\text{Komisyon} + ...)
 *
 * Bu sozluk kapali ve kucuk oldugu icin WebView (iki platformda ayri kurulum,
 * KaTeX varliklarinin paketlenmesi, acilis maliyeti) ya da tam bir matematik
 * dizgi motoru gereksiz. Asagidaki ayristirici bu dokuz komutu DOGRU cizer.
 *
 * ⚠️ BILINMEYEN KOMUT SESSIZCE ATILMAZ, oldugu gibi yazilir. Icerige yeni bir
 * komut girerse ekranda `\sqrt` olarak gorunur -- yanlis ama GORUNUR. Sessizce
 * yutmak, formulun yanlis bir degeri gostermesine yol acardi.
 */

sealed interface MathNode {
    /** Duz metin parcasi. */
    data class Text(val value: String) : MathNode

    /** `\frac{pay}{payda}` — ust uste kesir. */
    data class Fraction(val numerator: List<MathNode>, val denominator: List<MathNode>) : MathNode
}

/**
 * Tek bir LaTeX ifadesini dugumlere cevirir.
 *
 * Girdi `$` / `$$` sinirlayicilari OLMADAN verilir.
 */
fun parseLatex(input: String): List<MathNode> {
    val nodes = mutableListOf<MathNode>()
    val metin = StringBuilder()

    fun metniBosalt() {
        if (metin.isNotEmpty()) {
            nodes.add(MathNode.Text(bosluklariDuzelt(metin.toString())))
            metin.clear()
        }
    }

    var i = 0
    while (i < input.length) {
        val c = input[i]

        if (c != '\\') {
            // Suslu parantezler LaTeX gruplamasi; ciktida gorunmemeli.
            if (c != '{' && c != '}') metin.append(c)
            i++
            continue
        }

        // Ters bolu ile baslayan komut
        val komutSonu = komutSonunuBul(input, i + 1)
        val komut = input.substring(i + 1, komutSonu)

        when (komut) {
            "frac" -> {
                val pay = grupOku(input, komutSonu)
                if (pay == null) {
                    // Bozuk `\frac`: oldugu gibi yaz, sessizce yutma.
                    metin.append("\\frac")
                    i = komutSonu
                    continue
                }
                val payda = grupOku(input, pay.second)
                if (payda == null) {
                    metin.append("\\frac")
                    i = komutSonu
                    continue
                }
                metniBosalt()
                nodes.add(
                    MathNode.Fraction(
                        numerator = parseLatex(pay.first),
                        denominator = parseLatex(payda.first)
                    )
                )
                i = payda.second
            }

            // `\text{...}` icerigi duz metin olarak akiyor; ozel bir dugum
            // gerekmiyor cunku zaten italik yazmiyoruz.
            "text", "mathrm" -> {
                val grup = grupOku(input, komutSonu)
                if (grup == null) {
                    metin.append("\\$komut")
                    i = komutSonu
                } else {
                    metin.append(grup.first)
                    i = grup.second
                }
            }

            // `\left(` ve `\right)` yalnizca boyutlandirma isareti; parantezin
            // kendisi bir sonraki karakter olarak zaten geliyor.
            "left", "right" -> i = komutSonu

            "times" -> { metin.append('×'); i = komutSonu }
            "div" -> { metin.append('÷'); i = komutSonu }
            "approx" -> { metin.append('≈'); i = komutSonu }
            "ge", "geq" -> { metin.append('≥'); i = komutSonu }
            "le", "leq" -> { metin.append('≤'); i = komutSonu }
            "ne", "neq" -> { metin.append('≠'); i = komutSonu }
            "sum" -> { metin.append('∑'); i = komutSonu }
            "cdot" -> { metin.append('·'); i = komutSonu }
            "pm" -> { metin.append('±'); i = komutSonu }

            // Kacisli semboller: `\%`, `\$`, `\&`, `\_`, `\#`
            "" -> {
                if (komutSonu < input.length) {
                    metin.append(input[komutSonu])
                    i = komutSonu + 1
                } else {
                    metin.append('\\')
                    i = komutSonu
                }
            }

            // Bosluk komutlari
            "quad" -> { metin.append("  "); i = komutSonu }
            "qquad" -> { metin.append("    "); i = komutSonu }

            else -> {
                // BILINMEYEN: oldugu gibi yaz. Gorunur olmasi, sessizce
                // kaybolmasindan iyi.
                metin.append("\\").append(komut)
                i = komutSonu
            }
        }
    }

    metniBosalt()
    return nodes
}

/**
 * Bosluk duzeltmesi.
 *
 * LaTeX matematik kipinde kaynak bosluklari YOK SAYILIR ve aralar operatore
 * gore otomatik verilir. Burada boyle yapilmiyor: bu icerikte formuller
 * kelimelerden kurulu ve yazarlar operatorlerin etrafina zaten bosluk koymus
 * (`\text{Net Tahsilat} = \text{Brut Satis}`). Tum bosluklari atmak
 * "NetTahsilat=BrutSatis" verirdi.
 *
 * Bu yuzden bosluklar KORUNUYOR; yalnizca iki kaynak kusuru temizleniyor:
 *   - `\left( \text{...}` yaziminin biraktigi parantez ici bosluk
 *   - komut sonrasi olusan cift bosluk
 */
private fun bosluklariDuzelt(ham: String): String {
    val sb = StringBuilder(ham.length)
    var i = 0
    while (i < ham.length) {
        val c = ham[i]
        if (c == ' ') {
            // Ust uste bosluklari teke indir
            if (sb.isNotEmpty() && sb.last() == ' ') { i++; continue }
            // Acilis parantezinden hemen sonraki bosluk
            if (sb.isNotEmpty() && sb.last() == '(') { i++; continue }
            // Kapanis parantezinden hemen onceki bosluk
            var j = i
            while (j < ham.length && ham[j] == ' ') j++
            if (j < ham.length && ham[j] == ')') { i = j; continue }
        }
        sb.append(c)
        i++
    }
    return sb.toString()
}

/** `\` sonrasindaki komut adinin bittigi indeksi verir. */
private fun komutSonunuBul(input: String, baslangic: Int): Int {
    var j = baslangic
    while (j < input.length && input[j].isLetter()) j++
    return j
}

/**
 * `{...}` grubunu okur; bosluklari atlar.
 *
 * Ic ice suslu parantezleri SAYAR (`\frac{\text{a}}{b}` gibi durumlar icin);
 * ilk `}` karakterinde durmak yaygin ve sessiz bir hata olurdu.
 *
 * @return grup icerigi ve grup sonrasi indeks; grup yoksa null.
 */
private fun grupOku(input: String, baslangic: Int): Pair<String, Int>? {
    var j = baslangic
    while (j < input.length && input[j] == ' ') j++
    if (j >= input.length || input[j] != '{') return null

    var derinlik = 0
    val basIcerik = j + 1
    while (j < input.length) {
        when (input[j]) {
            '{' -> derinlik++
            '}' -> {
                derinlik--
                if (derinlik == 0) return input.substring(basIcerik, j) to (j + 1)
            }
        }
        j++
    }
    return null
}

/**
 * Bir metin parcasindaki matematik bolumlerini ayirir.
 *
 * `$$...$$` ve `$...$` ikisi de destekleniyor: icerikte her ikisi de
 * kullaniliyor (210 blok, ayrica satir ici ornekler).
 */
sealed interface MetinParcasi {
    data class Duz(val value: String) : MetinParcasi
    data class Matematik(val latex: String, val blok: Boolean) : MetinParcasi
}

fun matematikAyir(raw: String): List<MetinParcasi> {
    if (!raw.contains('$')) return listOf(MetinParcasi.Duz(raw))

    val parcalar = mutableListOf<MetinParcasi>()
    val tampon = StringBuilder()
    var i = 0

    fun tamponuBosalt() {
        if (tampon.isNotEmpty()) {
            parcalar.add(MetinParcasi.Duz(tampon.toString()))
            tampon.clear()
        }
    }

    while (i < raw.length) {
        // Kacisli dolar isareti metnin parcasi (`\$100` gibi)
        if (raw[i] == '\\' && i + 1 < raw.length && raw[i + 1] == '$') {
            tampon.append('$')
            i += 2
            continue
        }

        if (raw[i] == '$') {
            val blok = i + 1 < raw.length && raw[i + 1] == '$'
            val sinirlayici = if (blok) "$$" else "$"
            val basIcerik = i + sinirlayici.length
            val kapanis = raw.indexOf(sinirlayici, basIcerik)

            if (kapanis == -1) {
                // Kapanmamis sinirlayici: metin olarak birak. Ayristirmayi
                // zorlamak, geri kalan tum icerigi formul sanmaya yol acardi.
                tampon.append(raw[i])
                i++
                continue
            }

            val icerik = raw.substring(basIcerik, kapanis)
            if (icerik.isBlank()) {
                tampon.append(sinirlayici)
                i = kapanis + sinirlayici.length
                continue
            }

            tamponuBosalt()
            parcalar.add(MetinParcasi.Matematik(icerik.trim(), blok))
            i = kapanis + sinirlayici.length
            continue
        }

        tampon.append(raw[i])
        i++
    }

    tamponuBosalt()
    return parcalar
}
