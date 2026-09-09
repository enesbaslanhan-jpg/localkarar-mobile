package com.localkarar.app.decision

import com.localkarar.app.network.dto.BusinessRecordDto
import com.localkarar.app.network.dto.KararGunluguDto
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/*
 * KARAR RAPORU — hedeflenen ile gerceklesenin yan yana konmasi.
 *
 * 🔴 NEDEN VAR: veri IKI YERDE yaziliyor ama hicbir yerde okunmuyordu.
 *
 *   1. Karar araci takipleri: karar bir goreve baglaniyor, beklenen ve
 *      gerceklesen sonuc kaydin `metadata`sina yaziliyordu. Yalniz
 *      karar sonuc ekraninda gorunuyordu; oradan cikinca kayboluyordu.
 *   2. Finansal model karar gunlugu: sunucuda OKUMA UCU BILE YOKTU.
 *
 * ⚠️ Iki kaynak TEK LISTEDE. Kullanici icin ikisi de "verdigim karar";
 * hangi tablodan geldigi onun sorunu degil. Ayri iki rapor yapmak ayni
 * soruyu iki yerde sordurmak olurdu. Satirin kaynagi yine de
 * isaretleniyor, cunku birine gorev acilabiliyor otekine acilamiyor.
 */

enum class KararKaynagi { KARAR_ARACI, FINANSAL_MODEL }

/** Uzerinde calisma bitmis sayilan kayit durumlari. */
private val BITMIS_DURUMLAR = setOf("completed", "cancelled")

/**
 * Iki farkli kaynagin tek satir bicimi.
 *
 * @param kayitId yalniz karar araci takiplerinde dolu; finansal model
 *   kararinin acilacak bir gorevi yok, o yuzden `null`. Arayuz butonu
 *   buna gore ciziyor -- tiklaninca hicbir sey yapmayan bir dugme
 *   koymaktansa dugmeyi hic koymamak.
 */
data class KararSatiri(
    val id: String,
    val kaynak: KararKaynagi,
    val baslik: String,
    val altBaslik: String?,
    val beklenen: String?,
    val gerceklesen: String?,
    val sapma: String?,
    val ders: String?,
    val tarih: String?,
    val degerlendirmeTarihi: String?,
    val kayitId: String?,
    /** Gorev tamamlandi ya da iptal edildi mi. Gunluk kayitlarinda `true`. */
    val gorevBitti: Boolean = true,
    val gecikti: Boolean = false
) {
    /**
     * Gerceklesen sonuc YAZILMAMIS.
     *
     * Rapor bunu kullaniyor: ozet sayilari ve suzgec "sonucu yazildi mi"
     * sorusunu soruyor, gorevin bitip bitmedigini degil.
     */
    val sonucBekliyor: Boolean get() = gerceklesen.isNullOrBlank()

    /**
     * 🔴 SONUCU YAZILMASI GEREKEN durum — `sonucBekliyor` ile AYNI SEY DEGIL.
     *
     * Emulator gezintisinde yakalandi (09.09.2026): ana sayfa daha
     * BASLAMAMIS bir goreve "Sonucu bekliyor" yaziyordu. Yanlis, cunku
     * is bitmedi ki sonucu yazilsin -- kullaniciya yapacak bir sey
     * varmis gibi gorunuyordu.
     *
     * Ayrim: rapor "sonuc yazildi mi" diye sorar (bitmemis gorevler de
     * sayilir, cunku o karar hala takipte); ana sayfa ise "simdi benden
     * bir sey bekleniyor mu" diye sorar ve cevap yalnizca gorev bitmis
     * ama sonuc yazilmamissa evettir.
     */
    val sonucuYazilmali: Boolean get() = gorevBitti && sonucBekliyor
}

data class KararRaporuOzeti(
    val toplam: Int,
    val degerlendirilen: Int,
    val bekleyen: Int
)

enum class KararSuzgeci { HEPSI, BEKLEYEN, DEGERLENDIRILEN }

private fun JsonObject?.metin(anahtar: String): String? =
    this?.get(anahtar)?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }

/** Karar araci takibi tasiyan bir kaydi satira cevirir. */
fun takipSatiri(kayit: BusinessRecordDto): KararSatiri {
    val takip = kayit.metadata["decisionFollowUp"] as? JsonObject
    return KararSatiri(
        id = "takip-${kayit.id}",
        kaynak = KararKaynagi.KARAR_ARACI,
        /* Karar basligi yoksa UYDURULMUYOR: gorevin kendi basligi
           yaziliyor, o da her zaman var. */
        baslik = takip.metin("decisionTitle") ?: kayit.title,
        altBaslik = kayit.title.takeIf { takip.metin("decisionTitle") != null },
        beklenen = takip.metin("expectedOutcome"),
        gerceklesen = takip.metin("actualOutcome"),
        /* ⚠️ Karar araci tarafinda sapma alani HIC YOK; uydurulmuyor. */
        sapma = null,
        ders = takip.metin("lessonLearned"),
        tarih = kayit.createdAt,
        degerlendirmeTarihi = takip.metin("reviewedAt"),
        kayitId = kayit.id,
        gorevBitti = kayit.status in BITMIS_DURUMLAR,
        /* Gecikme SUNUCUDA hesaplaniyor; burada tekrar edilmiyor. */
        gecikti = kayit.overdue
    )
}

/** Finansal model karar gunlugu kaydini satira cevirir. */
fun gunlukSatiri(kayit: KararGunluguDto): KararSatiri {
    return KararSatiri(
        id = "gunluk-${kayit.id ?: kayit.createdAt ?: ""}",
        kaynak = KararKaynagi.FINANSAL_MODEL,
        baslik = kayit.decision?.takeIf { it.isNotBlank() } ?: "Karar",
        altBaslik = kayit.modelRun?.model?.name,
        beklenen = kayit.expectedOutcome?.takeIf { it.isNotBlank() },
        gerceklesen = kayit.actualOutcome?.takeIf { it.isNotBlank() },
        sapma = kayit.variance?.takeIf { it.isNotBlank() },
        ders = kayit.lessonLearned?.takeIf { it.isNotBlank() },
        tarih = kayit.createdAt,
        degerlendirmeTarihi = kayit.reviewedAt,
        kayitId = null
    )
}

/**
 * Iki kaynagi birlestirip yeniden eskiye siralar.
 *
 * ⚠️ Tarih ISO 8601 metni olarak KARSILASTIRILIYOR, cozulmuyor: ayni
 * bicimdeki ISO damgalarinda metin sirasi zaman sirasiyla ayni ve
 * ortak kodda tarih cozucu yok. Tarihi olmayan kayit sona dusuyor.
 */
fun kararSatirlari(
    takipler: List<BusinessRecordDto>,
    gunluk: List<KararGunluguDto>
): List<KararSatiri> =
    (takipler.map(::takipSatiri) + gunluk.map(::gunlukSatiri))
        .sortedByDescending { it.tarih ?: "" }

/**
 * Ozet SUZGECTEN ONCE hesaplanir.
 *
 * Suzgec uygulandiginda toplam da duseydi "8 karardan 3'u
 * degerlendirildi" cumlesi yanlis cikardi.
 */
fun kararOzeti(satirlar: List<KararSatiri>): KararRaporuOzeti {
    val degerlendirilen = satirlar.count { !it.sonucBekliyor }
    return KararRaporuOzeti(
        toplam = satirlar.size,
        degerlendirilen = degerlendirilen,
        bekleyen = satirlar.size - degerlendirilen
    )
}

fun kararSuz(satirlar: List<KararSatiri>, suzgec: KararSuzgeci): List<KararSatiri> = when (suzgec) {
    KararSuzgeci.HEPSI -> satirlar
    KararSuzgeci.BEKLEYEN -> satirlar.filter { it.sonucBekliyor }
    KararSuzgeci.DEGERLENDIRILEN -> satirlar.filter { !it.sonucBekliyor }
}

/**
 * ANA SAYFADAKI "KARARLARDAN GELEN GOREVLER" BOLUMU.
 *
 * Sonucu yazilmis VE bitmis kararlar ana sayfada yer kaplamiyor;
 * onlarin yeri rapor. Sonucu bekleyenler basa aliniyor: karar
 * takibinin butun anlami o adimda -- karari verip gorevi bitirip
 * sonucu hic yazmazsan geriye donup ogrenecek bir sey kalmiyor.
 */
fun anaSayfaKararGorevleri(
    takipler: List<BusinessRecordDto>,
    enFazla: Int = 5
): List<KararSatiri> =
    takipler.map(::takipSatiri)
        /* Bitmis VE sonucu yazilmis karar ana sayfada yer kaplamiyor. */
        .filterNot { it.gorevBitti && !it.sonucBekliyor }
        /* Sonucu yazilmayi bekleyenler basa. */
        .sortedBy { if (it.sonucuYazilmali) 0 else 1 }
        .take(enFazla)
