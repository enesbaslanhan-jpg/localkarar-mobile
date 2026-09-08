package com.localkarar.app.onboarding

import com.localkarar.app.network.ApiConfig
import com.localkarar.app.network.SafeApiClient
import com.localkarar.app.network.dto.*
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * KURULUM ve DEGERLENDIRME uclari.
 *
 * 🔴 Mobilde HIC KULLANILMIYORDU. Web `/app/onboarding` ve
 * `/app/assessment` sunuyor; mobilde iki akisin da karsiligi yoktu.
 */
class OnboardingRepository(private val api: SafeApiClient) {

    private val base = ApiConfig.baseUrl

    suspend fun durum(): Result<KurulumDurumuDto> =
        api.get("$base/onboarding/status")

    suspend fun profil(): Result<KurulumProfiliDto> =
        api.get("$base/onboarding/profile")

    /**
     * 🔴 GOVDE ELLE KURULUYOR — DTO'yu oldugu gibi gondermek 422 DONUYOR.
     *
     * Olculdu (08.09.2026, calisan sunucuya istek): mobil bos alanlari
     * `null` olarak gonderiyordu ve sunucu bunlari reddediyordu --
     * "Expected string, received null" (`city`, `currency`...).
     *
     * Sebep sunucunun semasindaki INCE AYRIM: alanlarin cogu
     * `.optional()` ama `.nullable()` DEGIL (`onboarding.ts`). Yani
     * "gondermeyebilirsin" serbest, "null gonder" degil. kotlinx ise
     * varsayilan olarak null alanlari da yaziyor.
     *
     * ⚠️ SADECE NULL'LARI ATMAK DA YANLIS OLURDU. Sunucu DORT alani
     * acikca `.nullable()` isaretlemis: `businessStage`,
     * `employeeCount`, `primaryGoal`, `weeklyLearningMinutes`. Bunlarda
     * null "temizle" demek; atlanirsa kullanici sectigi asamayi geri
     * ALAMAZDI. Web de tam bu dordunde null gonderiyor
     * (`OnboardingPage.jsx`).
     *
     * ⚠️ Istemcinin ortak `Json`una `explicitNulls = false` koymak
     * cozum DEGIL: o zaman uygulamanin HICBIR yerinde bir alan
     * temizlenemezdi (ornegin kayittaki kisiyi kaldirmak).
     */
    suspend fun profilKaydet(profil: KurulumProfiliDto): Result<KurulumProfiliDto> {
        val alanlar = mutableMapOf<String, JsonElement>()

        /* Null ise HIC gonderilmiyor: sunucu null kabul etmiyor. */
        profil.name?.let { alanlar["name"] = JsonPrimitive(it) }
        profil.sector?.let { alanlar["sector"] = JsonPrimitive(it) }
        profil.city?.let { alanlar["city"] = JsonPrimitive(it) }
        profil.currency?.let { alanlar["currency"] = JsonPrimitive(it) }
        profil.monthlySales?.let { alanlar["monthlySales"] = JsonPrimitive(it) }
        profil.monthlyExpenses?.let { alanlar["monthlyExpenses"] = JsonPrimitive(it) }
        profil.cashBalance?.let { alanlar["cashBalance"] = JsonPrimitive(it) }
        profil.debtBalance?.let { alanlar["debtBalance"] = JsonPrimitive(it) }

        /* Listeler her zaman gonderiliyor: bos liste "hicbiri" demek ve
           kullanicinin secimini kaldirmasi boyle iletiliyor. */
        alanlar["salesChannels"] = JsonArray(profil.salesChannels.map { JsonPrimitive(it) })
        alanlar["challenges"] = JsonArray(profil.challenges.map { JsonPrimitive(it) })

        /* Sunucunun `.nullable()` dedigi dort alan: null = TEMIZLE. */
        alanlar["businessStage"] = profil.businessStage?.let { JsonPrimitive(it) } ?: JsonNull
        alanlar["employeeCount"] = profil.employeeCount?.let { JsonPrimitive(it) } ?: JsonNull
        alanlar["primaryGoal"] = profil.primaryGoal?.let { JsonPrimitive(it) } ?: JsonNull
        alanlar["weeklyLearningMinutes"] =
            profil.weeklyLearningMinutes?.let { JsonPrimitive(it) } ?: JsonNull

        return api.putJson("$base/onboarding/profile", JsonObject(alanlar))
    }

    /** ⚠️ Sunucu `onboardingCompleted: true` sabitini bekliyor. */
    suspend fun kurulumuTamamla(): Result<Unit> =
        api.post("$base/onboarding/complete", KurulumTamamlaDto(onboardingCompleted = true))

    /*
     * URUN TURU (madde 4).
     *
     * ⚠️ Mobilde REHBERLI TUR YOK ve yapilmayacak: webdeki tur ekrandan
     * ekrana gezdiren bir kaplama; mobilde karsiligi bambaska bir
     * tasarim isi olurdu ve dock'lu bir kabukta ayni sekilde
     * calismazdi. Ama TAMAMLANDI bayragi yaziliyor: kurulumu mobilden
     * yapan kullanici webde bir daha tur karsisina cikmasin.
     */
    suspend fun turuTamamla(): Result<Unit> =
        api.post("$base/onboarding/tour/complete")

    suspend fun degerlendirmeSorulari(): Result<DegerlendirmeSorulariDto> =
        api.get("$base/assessment/questions")

    suspend fun degerlendirmeGonder(cevaplar: Map<String, String>): Result<DegerlendirmeSonucuDto> =
        api.post("$base/assessment/submit", DegerlendirmeGonderDto(cevaplar))

    suspend fun degerlendirmeSonucu(): Result<DegerlendirmeSonucuDto> =
        api.get("$base/assessment/results")
}
