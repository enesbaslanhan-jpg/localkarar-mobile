package com.localkarar.app.workspaces

/**
 * CSV SUTUNLARINI KAYIT ALANLARINA ESLESTIRME.
 *
 * 🔴 SUNUCU BU ISI YAPMIYOR. `/records/import` ucu `columnMapping`i
 * ISTEMCIDEN bekliyor (`business-tracker.ts` -> `importRequestSchema`).
 * Web bunu tarayicida yapiyor (`ImportDialog.jsx`); mobil de ayni isi
 * yapmak zorunda, yoksa hicbir sutun eslesmez ve her satir duser.
 *
 * ⚠️ TAKMA ADLAR WEBDEN BIREBIR KOPYALANDI. Iki ürün ayni dosyayi ayni
 * sekilde okumali: kullanici webde ice aktardigi dosyayi telefonda da
 * aktarabilmeli. Listeler ayrisirsa ayni dosya iki üründe farkli
 * sonuc verir ve sebebi hicbir yerde gorunmez.
 */
private val ALAN_TAKMA_ADLARI: Map<String, List<String>> = mapOf(
    "type" to listOf("type", "tür", "tur", "kategori"),
    "title" to listOf("title", "başlık", "baslik", "name", "isim", "konu"),
    "description" to listOf("description", "açıklama", "aciklama", "detay", "not"),
    "direction" to listOf("direction", "yön", "yon", "tip"),
    "amount" to listOf("amount", "tutar", "miktar", "price", "fiyat"),
    "currency" to listOf("currency", "para birimi", "parabirimi", "birim"),
    "priority" to listOf("priority", "öncelik", "oncelik"),
    "dueAt" to listOf("dueat", "due_at", "vade", "son tarih", "tarih", "date"),
    "contactId" to listOf("contactid", "contact_id", "cari", "müşteri", "musteri", "tedarikçi", "tedarikci"),
    "assignedToId" to listOf("assignedtoid", "assigned_to_id", "sorumlu", "assignee"),
    "recurrenceRule" to listOf("recurrencerule", "recurrence_rule", "tekrarlama", "tekrar")
)

/**
 * CSV metninin BASLIK satirindaki sutun adlarini cikarir.
 *
 * ⚠️ Tam bir CSV cozumleyici DEGIL ve olmasi da gerekmiyor: yalnizca
 * ilk satirin sutun ADLARI lazim. Satirlarin kendisini sunucu
 * cozumluyor (`parseCsv`), yani veri dogrulugu istemciye bagli degil.
 *
 * Tirnak icindeki ayraclar korunuyor -- "Tutar, KDV dahil" gibi bir
 * baslik iki sutuna bolunmesin diye.
 */
fun csvBasliklariniOku(icerik: String?): List<String> {
    if (icerik.isNullOrBlank()) return emptyList()

    val ilkSatir = icerik.lineSequence()
        .firstOrNull { it.isNotBlank() }
        ?: return emptyList()

    val sutunlar = mutableListOf<String>()
    val gecerli = StringBuilder()
    var tirnakta = false

    for (karakter in ilkSatir) {
        when {
            karakter == '"' -> tirnakta = !tirnakta
            karakter == ',' && !tirnakta -> {
                sutunlar.add(gecerli.toString().trim())
                gecerli.clear()
            }
            else -> gecerli.append(karakter)
        }
    }
    sutunlar.add(gecerli.toString().trim())

    /* BOM ilk baslikta kalirsa hicbir takma ad eslesmez. */
    return sutunlar
        .mapIndexed { i, s -> if (i == 0) s.removePrefix("﻿") else s }
        .filter { it.isNotBlank() }
}

/**
 * Sutun adlarindan `columnMapping` uretir.
 *
 * Donen sozluk: hedefAlan -> dosyadaki sutun basligi. Eslesmeyen alan
 * SOZLUKTE YER ALMIYOR; bos dize gondermek sunucuda o alani "bos hucre"
 * olarak isletirdi.
 */
fun iceAktarmaEslestir(sutunlar: List<String>): Map<String, String> {
    if (sutunlar.isEmpty()) return emptyMap()

    /* Karsilastirma kucuk harfle; sutun adinin KENDISI korunuyor cunku
       sunucu satirlari orijinal basliklarla okuyor. */
    val kucuk = sutunlar.map { it.lowercase() }

    val eslesme = mutableMapOf<String, String>()
    for ((alan, takmaAdlar) in ALAN_TAKMA_ADLARI) {
        for (takmaAd in takmaAdlar) {
            val i = kucuk.indexOf(takmaAd)
            if (i != -1) {
                eslesme[alan] = sutunlar[i]
                break
            }
        }
    }
    return eslesme
}
