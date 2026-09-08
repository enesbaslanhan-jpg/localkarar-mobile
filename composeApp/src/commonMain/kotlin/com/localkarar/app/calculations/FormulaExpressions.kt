package com.localkarar.app.calculations

/*
 * FORMUL METINLERI — mockup "Hesap 2 / Formül detayı" ekraninin ust blogu.
 *
 * ⚠️ HICBIRI UYDURULMADI. Her satir sunucunun `src/services/formulas.ts`
 * dosyasindaki `calculate` govdesinden birebir cevrildi; degisken adlari
 * ayni dosyadaki `inputs[...]` adlarinin Turkce etiketleri.
 *
 * 🔴 NEDEN LISTEDE OLMAYAN FORMUL ICIN BLOK CIZILMIYOR:
 * Karsiligi dogrulanmamis bir formulu ekrana basmak, kullaniciya YANLIS
 * bir hesap mantigi gostermek olurdu. Blok yalnizca burada karsiligi olan
 * formulde ciziliyor; digerlerinde girdi/sonuc bolumleri tek basina kaliyor.
 *
 * Sunucudaki formul degisirse bu metin sessizce eskiir. `fiyat_mimarisi`
 * icin `PriceArchitectureTest` koruma sagliyor; digerleri metin oldugu
 * icin testle pinlenemiyor — formul degistiginde buraya da bakilir.
 */

/** Bir formulun okunur matematigi: satirlar + altindaki tek cumlelik not. */
data class FormulaExpression(
    val satirlar: List<String>,
    val not: String? = null
)

val FORMULA_EXPRESSIONS: Map<String, FormulaExpression> = mapOf(
    "fiyat_mimarisi" to FormulaExpression(
        satirlar = listOf(
            "birim maliyet = doğrudan + operasyon + sabit gider payı + iade riski",
            "toplam oran   = komisyon + ödeme kesintisi + hedef marj",
            "satış fiyatı  = birim maliyet ÷ (1 − toplam oran)",
            "birim katkı   = satış − birim maliyet − komisyon tutarı − ödeme kesintisi"
        ),
        not = "Marj satış üzerinden hesaplanır, maliyet üzerinden değil. %32 marj, " +
            "maliyetin %32 fazlası demek değildir — maliyeti 0,68'e bölmek demektir."
    ),
    "kar_hesabi" to FormulaExpression(
        satirlar = listOf(
            "kâr       = satış − gider",
            "kâr marjı = kâr ÷ satış × 100"
        ),
        not = "Marj satış üzerinden ölçülür; satış sıfırsa marj sıfır kabul edilir."
    ),
    "basabas_noktasi" to FormulaExpression(
        satirlar = listOf(
            "katkı payı     = birim fiyat − birim değişken gider",
            "başabaş adet   = ⌈ sabit gider ÷ katkı payı ⌉",
            "başabaş geliri = başabaş adet × birim fiyat"
        ),
        not = "Adet yukarı yuvarlanır: yarım ürün satılamaz, eksik adet başabaşı tutmaz."
    ),
    "nakit_pozisyonu" to FormulaExpression(
        satirlar = listOf(
            "net pozisyon = nakit − kısa vadeli borç",
            "nakit oranı  = nakit ÷ kısa vadeli borç"
        )
    ),
    "isletme_sermayesi" to FormulaExpression(
        satirlar = listOf(
            "işletme sermayesi = dönen varlıklar − kısa vadeli borçlar"
        ),
        not = "Pozitif sonuç kısa vadeli yükümlülüklerin karşılanabildiğini gösterir."
    ),
    "roi" to FormulaExpression(
        satirlar = listOf(
            "net kâr = getiri − yatırım",
            "ROI     = net kâr ÷ yatırım × 100"
        ),
        not = "Basit ROI: vergi, enflasyon ve paranın zaman değeri hesaba girmez."
    ),
    "stok_devir" to FormulaExpression(
        satirlar = listOf(
            "devir hızı        = yıllık satışlar (maliyet) ÷ ortalama stok",
            "stokta kalma günü = 365 ÷ devir hızı"
        )
    ),
    "cac" to FormulaExpression(
        satirlar = listOf(
            "CAC = (pazarlama gideri + satış ekip gideri) ÷ yeni müşteri sayısı"
        )
    ),
    "ltv" to FormulaExpression(
        satirlar = listOf(
            "LTV = ortalama satış × yıllık sıklık × ilişki süresi"
        )
    ),
    "ltv_cac" to FormulaExpression(
        satirlar = listOf(
            "LTV/CAC = müşteri yaşam boyu değeri ÷ müşteri edinme maliyeti"
        ),
        not = "3:1 sağlıklı kabul edilir; 1:1 altı sürdürülemez."
    ),
    "indirim_kar" to FormulaExpression(
        satirlar = listOf(
            "indirimli fiyat = normal fiyat × (1 − indirim oranı)",
            "normal kâr      = (normal fiyat − birim maliyet) × beklenen adet",
            "kampanya kârı   = (indirimli fiyat − birim maliyet) × beklenen adet",
            "kâr farkı       = kampanya kârı − normal kâr"
        ),
        not = "Talep artışı varsayılmaz: adet iki senaryoda da aynı tutulur."
    ),
    "kredi_maliyeti" to FormulaExpression(
        satirlar = listOf(
            "aylık faiz    = yıllık faiz ÷ 12",
            "taksit        = tutar × aylık faiz × (1 + aylık faiz)^vade ÷ ((1 + aylık faiz)^vade − 1)",
            "toplam ödeme  = taksit × vade",
            "toplam faiz   = toplam ödeme − kredi tutarı"
        ),
        not = "Sabit faiz varsayılır; dosya masrafı ve sigorta dahil değildir."
    ),
    "ihracat_maliyet" to FormulaExpression(
        satirlar = listOf(
            "toplam maliyet   = üretim + lojistik + gümrük + diğer",
            "birim maliyet ₺  = toplam maliyet ÷ ürün adedi",
            "birim maliyet \$ = birim maliyet ₺ ÷ döviz kuru"
        )
    ),
    "kdv_ekleme" to FormulaExpression(
        satirlar = listOf(
            "KDV tutarı = KDV hariç tutar × KDV oranı",
            "KDV dahil  = KDV hariç tutar × (1 + KDV oranı)"
        ),
        not = "Sonuç vergi beyannamesi yerine geçmez; oranı güncel mevzuata göre girin."
    ),
    "kasa_kapanis" to FormulaExpression(
        satirlar = listOf(
            "toplam giriş  = açılış kasası + nakit satış + tahsilat + diğer giriş",
            "toplam çıkış  = gider ödemeleri + tedarikçi ödemeleri + bankaya yatırılan",
            "beklenen kasa = toplam giriş − toplam çıkış"
        )
    ),
    "nakit_dayanim" to FormulaExpression(
        satirlar = listOf(
            "aylık nakit açığı = maks(0 ; aylık çıkış − aylık giriş)",
            "dayanma süresi    = mevcut nakit ÷ aylık nakit açığı"
        ),
        not = "Açık sıfırsa nakit tükenmiyor demektir; süre hesaplanmaz."
    ),
    "birim_maliyet" to FormulaExpression(
        satirlar = listOf(
            "toplam maliyet = hammadde + işçilik + genel gider + ambalaj/kargo + fire/iade",
            "birim maliyet  = toplam maliyet ÷ üretilen adet"
        ),
        not = "Fire ayrı bir kalem olarak yazılmadıkça maliyetin içinde saklanır."
    ),
    "vade_farki" to FormulaExpression(
        satirlar = listOf(
            "vadeli toplam     = peşin fiyat × (1 + aylık vade oranı)^vade",
            "vade farkı        = vadeli toplam − peşin fiyat",
            "aylık eşit ödeme  = vadeli toplam ÷ vade"
        ),
        not = "Oran bileşik uygulanır; sözleşmedeki masraf ve vergiler ayrıca eklenir."
    ),
    "pazaryeri_siparis_kari" to FormulaExpression(
        satirlar = listOf(
            "komisyon tutarı = satış fiyatı × komisyon oranı",
            "toplam maliyet  = ürün + komisyon + kargo + ambalaj + reklam + iade riski",
            "sipariş katkısı = satış fiyatı − toplam maliyet",
            "sipariş marjı   = sipariş katkısı ÷ satış fiyatı × 100"
        )
    )
)
