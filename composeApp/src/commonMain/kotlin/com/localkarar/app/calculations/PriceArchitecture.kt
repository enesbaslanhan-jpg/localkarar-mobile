package com.localkarar.app.calculations

import kotlin.math.round

/*
 * FIYAT MIMARISI — SUNUCUDAKI FORMULUN BIREBIR KOPYASI.
 *
 * Kaynak: `src/services/formulas.ts` -> `calculatePriceArchitecture`
 * (formul id: `fiyat_mimarisi`).
 *
 * ⚠️ NEDEN KOPYA VAR?
 * Hesaplamalar ekranindaki "Fiyatlandirma Sihirbazi" CANLI bir kaydirici:
 * kullanici marji surukledikce sonuc aninda degismeli. Sunucuya
 * (`POST /formulas/fiyat_mimarisi/calculate`) her kaydirma adiminda istek
 * atmak hem ag trafigi hem gecikme demekti. Formul 12 satir saf aritmetik
 * oldugu icin tasinabilir.
 *
 * 🔴 SAPMA RISKI VE ONLEMI:
 * Sunucudaki formul degisirse bu kopya sessizce yanlis sonuc uretir.
 * `PriceArchitectureTest` sunucunun KENDI fonksiyonundan uretilmis bes
 * senaryoyu pinliyor; formul degisirse test kirilir.
 *
 * ⚠️ Kullanici KAYDETMEK istediginde bu kopya DEGIL sunucu ucu kullanilir
 * (`CalculationsRepository.calculateFormula`). Bu kopya yalniz onizleme icin.
 */

/** `calculatePriceArchitecture` ciktisi. Alan adlari sunucudaki ile ayni. */
data class PriceArchitectureResult(
    val gercekBirimMaliyet: Double,
    val onerilenKdvHaricFiyat: Double,
    val komisyonTutari: Double,
    val odemeKesintisi: Double,
    val birimKatki: Double,
    val gerceklesenMarj: Double
)

/** Sunucudaki `round2` ile ayni: iki basamaga yuvarlar. */
private fun round2(v: Double): Double = round(v * 100) / 100

/**
 * Girdiler sunucudaki ile ayni adlarda ve ayni birimlerde:
 * tutarlar TRY, oranlar YUZDE (0-99).
 *
 * @return Toplam oran %100'e ulasirsa `null` -- sunucu burada hata firlatiyor
 *   ("Komisyon, odeme kesintisi ve hedef marj toplami %100'den kucuk
 *   olmalidir"). Onizlemede istisna firlatmak kaydirici surukleneken
 *   uygulamayi cokertirdi; cagiran taraf `null` durumunu gosterir.
 */
fun calculatePriceArchitecture(
    dogrudanMaliyet: Double,
    operasyonMaliyeti: Double,
    sabitGiderPayi: Double,
    iadeRiskPayi: Double,
    komisyonOrani: Double,
    odemeOrani: Double,
    hedefMarj: Double
): PriceArchitectureResult? {
    val komisyon = komisyonOrani / 100
    val odeme = odemeOrani / 100
    val marj = hedefMarj / 100
    val toplamOran = komisyon + odeme + marj

    if (toplamOran >= 1) return null

    val birimMaliyet = dogrudanMaliyet + operasyonMaliyeti + sabitGiderPayi + iadeRiskPayi
    val netSatisFiyati = birimMaliyet / (1 - toplamOran)
    val komisyonTutari = netSatisFiyati * komisyon
    val odemeKesintisi = netSatisFiyati * odeme
    val katki = netSatisFiyati - birimMaliyet - komisyonTutari - odemeKesintisi

    return PriceArchitectureResult(
        gercekBirimMaliyet = round2(birimMaliyet),
        onerilenKdvHaricFiyat = round2(netSatisFiyati),
        komisyonTutari = round2(komisyonTutari),
        odemeKesintisi = round2(odemeKesintisi),
        birimKatki = round2(katki),
        gerceklesenMarj = round2(if (netSatisFiyati > 0) (katki / netSatisFiyati) * 100 else 0.0)
    )
}
