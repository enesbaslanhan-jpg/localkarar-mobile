package com.localkarar.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import localkarar_mobile.composeapp.generated.resources.Res
import localkarar_mobile.composeapp.generated.resources.manrope
import org.jetbrains.compose.resources.Font

/**
 * Manrope — arayuzun tek metin fontu. WEB ILE AYNI AILE
 * (`frontend/src/styles/tokens.css` -> `--font-family`).
 *
 * 🔴 UYGULAMADAKI TUM YAZI TEK AGIRLIKTA CIZILIYORDU.
 *
 * Kaynak dizininde bes ayri dosya vardi -- manrope_regular / _medium /
 * _semibold / _bold / manrope -- ve BESI DE BAYT BAYT AYNIYDI (165420 bayt,
 * ayni md5). Hepsi ayni VARIABLE fontun kopyasiydi ve o fontun agirlik
 * ekseni `wght 200..800`, VARSAYILANI 200 (ExtraLight).
 *
 * Compose bir variable fontu, aksi soylenmedikce varsayilan orneginde cizer.
 * Yani `FontWeight.W700` istense de gelen sey 200 agirliktı: baslik, govde,
 * buton, hepsi ExtraLight. Ekranlarin "beyaz kagida yazilmis duz metin" gibi
 * gorunmesinin sebebi buydu -- agirlik kontrasti sifirdi.
 *
 * COZUM: tek dosya, her agirlik `FontVariation` ile eksenden ornekleniyor.
 *
 * Eksen degerleri webin kendi tokenlarindan (`tokens.css`):
 *   W400 → 400  (--font-weight-normal)
 *   W500 → 500  (--font-weight-medium)
 *   W600 → 650  (--font-weight-semibold)
 *   W700 → 720  (--font-weight-bold)
 *   W800 → 780  (--font-weight-heavy — .section-title, .brand)
 *
 * ⚠️ Webdeki `--font-weight-black: 850` BU EKSENE SIGMIYOR (max 800).
 * O deger yalniz `.brandmark` ve `.iconbox` icin kullaniliyor; mobilde
 * karsiligi yok, en yakin sinir 800 olurdu. Ihtiyac olursa 800 kullanilir.
 *
 * ⚠️ `variationSettings` iOS tarafinda DOGRULANMADI. Android'de olculdu;
 * iOS hedefi derleniyor ama cihazda gorsel kontrol yapilmadi.
 */
@Composable
fun getManropeFontFamily(): FontFamily {
    @Composable
    fun agirlik(w: FontWeight, eksen: Int) = Font(
        Res.font.manrope,
        weight = w,
        variationSettings = FontVariation.Settings(FontVariation.weight(eksen))
    )

    return FontFamily(
        agirlik(FontWeight.W400, 400),
        agirlik(FontWeight.W500, 500),
        agirlik(FontWeight.W600, 650),
        agirlik(FontWeight.W700, 720),
        agirlik(FontWeight.W800, 780),
    )
}

/**
 * TIPOGRAFI OLCEGI — `DESIGN.md` §4, MOBIL SUTUNU.
 *
 * 🔴 OLCEK SOZLESMEDEN SAPIYORDU. Onceki degerler `tokens.css`ten geliyordu
 * ve dokumanla ayrisiyordu: body 13 (doküman 14), page-title 24 (doküman
 * mobilde 20), card-title 15 (doküman 16). §4 "Deger yalnizca token'dan
 * okunur" dedigi icin dokumandaki olcek esas alindi.
 *
 * §4 zorunluluklari:
 *   - line-height: baslik 1.25, govde 1.5, uzun okuma 1.75
 *   - letter-spacing YALNIZ display ve page-title'da -0.01em
 *   - kusuratli boyut yok (20.8px gibi)
 *
 * ⚠️ FONKSIYON ADLARI DEGISMEDI. 77 tuketici dosya var; ad degisimi hepsine
 * dokunurdu. Eski ad → §4 tokeni esleme asagida her fonksiyonun ustunde.
 *
 * ⚠️ `getMicro()` — §4'te 10px KADEME YOK; en kucuk kademe caption (11px).
 * Bu yuzden caption'a esitlendi. Yeni kodda `getMetadata()` tercih edilmeli.
 */
object LkTypography {

    /** §4 `display` — 32sp / W700. Yalniz auth ve onboarding hero. */
    @Composable
    fun getDisplay() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W700,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.32).sp,
        color = LkTextPrimary
    )

    /** §4 `page-title` — mobil 20sp / W700. TUM sayfalarda ayni. */
    @Composable
    fun getPageTitle() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W700,
        fontSize = 20.sp,
        lineHeight = 25.sp,
        letterSpacing = (-0.2).sp,
        color = LkTextPrimary
    )

    /** §4 `section-title` — mobil 16sp / W600. */
    @Composable
    fun getSectionTitle() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W600,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        color = LkTextPrimary
    )

    /** §4 `card-title` — 16sp / W600. */
    @Composable
    fun getCardTitle() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W600,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        color = LkTextPrimary
    )

    /** §4 `body-lg` — 16sp / lh 1.75. Ders ve kurs okuma alani (§17). */
    @Composable
    fun getBodyLarge() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W400,
        fontSize = 16.sp,
        lineHeight = 28.sp,
        color = LkTextPrimary
    )

    /** §4 `body` — 14sp / W400 / lh 1.5. Varsayilan govde. */
    @Composable
    fun getBody() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        color = LkTextPrimary
    )

    /** §4 `body` + vurgu — 14sp / W600. Satir basligi, one cikan deger. */
    @Composable
    fun getBodyStrong() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W600,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        color = LkTextPrimary
    )

    /** §4 `body-sm` — 13sp / W400. Yardimci metin, tablo hucresi. */
    @Composable
    fun getBodySmall() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W400,
        fontSize = 13.sp,
        lineHeight = 19.sp,
        color = LkTextSecondary
    )

    /** §4 `label` — 12sp / W600. Form etiketi, sekme, chip. */
    @Composable
    fun getLabel() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W600,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        color = LkTextSecondary
    )

    /** §4 `caption` — 11sp / W500. Zaman damgasi, meta, dipnot. */
    @Composable
    fun getMetadata() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W500,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        color = LkTextMuted
    )

    /** §4'te 10px kademe yok — `caption`a esitlendi. Yeni kodda kullanma. */
    @Composable
    fun getMicro() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W500,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        color = LkTextMuted
    )

    /** §4 `metric-lg` — mobil 20sp / W700. KPI ana degeri. */
    @Composable
    fun getMetric() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W700,
        fontSize = 20.sp,
        lineHeight = 25.sp,
        color = LkTextPrimary
    )

    /**
     * Alt navigasyon etiketi — 10sp / W600.
     *
     * ⚠️ §4'TE KARSILIGI OLMAYAN TEK KADEME. Dokumandaki en kucuk boyut
     * caption (11sp); alt dock 5 sekme tasiyor ve 11sp'de "İşletme Takibi"
     * ile "Hesaplamalar" 360dp genislikte kirpiliyor (olculdu: "Ayarlaı").
     *
     * Onaylanan prototip de burada 10px kullaniyor
     * (`.dock-tab { font-size: 10px; font-weight: 600 }`).
     *
     * §0 "eksikse dokumana madde eklenir" diyor: bu kademe §4'e alt
     * navigasyon istisnasi olarak islenmeli. BASKA HICBIR YERDE
     * kullanilmaz -- gorsel olarak dock disinda 10sp yasak.
     */
    @Composable
    fun getNavLabel() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W600,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        color = LkTextMuted
    )

    /** §4 `metric-md` — mobil 16sp / W700. Kart ici metrik. */
    @Composable
    fun getMetricSmall() = TextStyle(
        fontFamily = getManropeFontFamily(),
        fontWeight = FontWeight.W700,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        color = LkTextPrimary
    )
}
