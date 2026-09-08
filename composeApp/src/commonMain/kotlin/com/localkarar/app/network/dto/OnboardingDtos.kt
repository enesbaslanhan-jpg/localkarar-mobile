package com.localkarar.app.network.dto

import kotlinx.serialization.Serializable

/**
 * KURULUM (onboarding) ve DEGERLENDIRME (assessment) DTO'lari.
 *
 * 🔴 IKI AKIS DA MOBILDE HIC YOKTU. Web `/app/onboarding` ve
 * `/app/assessment` rotalarini sunuyor; mobil `UserDto.onboardingCompleted`
 * alanini OKUYOR ama dolduracak ekrani yoktu. Yani mobilden kaydolan
 * kullanici isletme profilini hic kuramiyordu.
 */

/** `GET /onboarding/status` */
@Serializable
data class KurulumDurumuDto(
    val onboardingCompleted: Boolean = false,
    /*
     * ⚠️ Turdan AYRI bayrak. Anket bittiginde tur daha baslamamis olur;
     * ikisini tek alana baglamak birini gormeyi imkansiz kilardi
     * (sunucunun kendi notu).
     */
    val tourCompleted: Boolean = false,
    val profileComplete: Boolean = false,
    val hasProfile: Boolean = false
)

/** `GET|PUT /onboarding/profile` */
@Serializable
data class KurulumProfiliDto(
    val name: String? = null,
    val sector: String? = null,
    val city: String? = null,
    val currency: String? = null,
    val monthlySales: Double? = null,
    val monthlyExpenses: Double? = null,
    val cashBalance: Double? = null,
    val debtBalance: Double? = null,
    /** startup | growth | mature */
    val businessStage: String? = null,
    val employeeCount: Int? = null,
    val salesChannels: List<String> = emptyList(),
    val primaryGoal: String? = null,
    val weeklyLearningMinutes: Int? = null,
    val challenges: List<String> = emptyList()
)

/**
 * `POST /onboarding/complete`
 *
 * ⚠️ Sunucu `onboardingCompleted: true` sabitini bekliyor
 * (`z.literal(true)`); baska bir deger gondermek 422 doner.
 *
 * 🔴 ALANIN VARSAYILAN DEGERI OLMAMALI.
 *
 * Olculdu (08.09.2026, calisan sunucu): alan `= true` varsayilaniyla
 * tanimliyken kurulum HIC kapanmiyordu -- profil yaziliyor, hemen
 * ardindan gelen `complete` 422 donuyor ve kullanici ekranda kaliyordu.
 *
 * Sebep: istemcinin `Json` yapilandirmasinda `encodeDefaults`
 * kapali (varsayilan). kotlinx, degeri varsayilanina esit olan alani
 * govdeye HIC yazmiyor; sunucuya bos bir `{}` gidiyor ve zod
 * "Required" diyor. Varsayilani kaldirmak alanin her zaman
 * yazilmasini garanti ediyor.
 */
@Serializable
data class KurulumTamamlaDto(
    val onboardingCompleted: Boolean
)

// ---------------------------------------------------------------------
// DEGERLENDIRME
// ---------------------------------------------------------------------

@Serializable
data class DegerlendirmeSorusuDto(
    val id: String,
    /** Alan anahtari — sorular alana gore gruplaniyor. */
    val domain: String? = null,
    val domainLabel: String? = null,
    val step: Int = 0,
    val title: String? = null,
    val subtitle: String? = null,
    /** Sunucuda hepsi "single". */
    val type: String? = null,
    val options: List<DegerlendirmeSecenegiDto> = emptyList()
)

@Serializable
data class DegerlendirmeSecenegiDto(
    /*
     * ⚠️ DEGER SAYI DEGIL, DIZGE. Sunucu cevaplari `"0".."4"` kalibiyla
     * dogruluyor (`z.string().regex(/^[0-4]$/)`); sayi gondermek 422
     * doner.
     */
    val value: String,
    val label: String? = null
)

@Serializable
data class DegerlendirmeSorulariDto(
    val questions: List<DegerlendirmeSorusuDto> = emptyList(),
    val totalSteps: Int = 0
)

/**
 * `POST /assessment/submit`
 *
 * ⚠️ SUNUCU EKSIK CEVAP KABUL ETMIYOR: butun sorular cevaplanmadan
 * gonderim 422 doner ("Eksik cevaplar: ..."). Arayuz de bu yuzden
 * son adima kadar gondermiyor.
 */
@Serializable
data class DegerlendirmeGonderDto(
    val answers: Map<String, String>,
    val version: Int = 1
)

@Serializable
data class DegerlendirmeSonucuDto(
    val id: String? = null,
    val version: Int = 1,
    val scores: Map<String, Double> = emptyMap(),
    val priorityDomains: List<String> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val createdAt: String? = null
)
