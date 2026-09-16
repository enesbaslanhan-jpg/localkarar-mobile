package com.localkarar.app.auth

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable

/*
 * SOSYAL GIRIS — Google / Apple (16.09.2026).
 *
 * Platform tarafi yalnizca saglayicidan bir KIMLIK BELIRTECI (ID token) alir;
 * hesap acma/baglama karari sunucuda (`POST /auth/social`, auth.ts). Boylece
 * web, Android ve iOS ayni sunucu kuralina duser: dogrulanmis e-postayla
 * mevcut hesaba baglan, yoksa onayla yeni hesap ac.
 *
 * Istemci kimlikleri GIZLI DEGIL (belirtecin `aud` alaninda acik dururlar);
 * sunucu bunlarin tumunu kabul eder (GOOGLE_CLIENT_IDS / APPLE_CLIENT_IDS).
 *
 * Android'de Google belirteci `serverClientId` = WEB istemcisi ile istenir;
 * Google, Android istemci kimligini paket adi + SHA-1'den kendisi eslestirir.
 * iOS'ta Google icin iOS istemcisi kullanilir (SDK yok, ASWebAuthenticationSession
 * + PKCE). Apple yalnizca iOS'ta yerel; Android'de Apple dugmesi cizilmez.
 */
object SosyalGirisKimlikleri {
    const val GOOGLE_WEB_CLIENT_ID = "501996851998-hq8st05ps1vomm4uvlfotd02kk9ejh1j.apps.googleusercontent.com"
    const val GOOGLE_IOS_CLIENT_ID = "501996851998-mflmcqteo1kq1l5dr63iisvfb2a4mel6.apps.googleusercontent.com"
    const val APPLE_BUNDLE_ID = "com.localkarar.app"
}

/** Saglayicidan alinan kimlik; sunucuya oldugu gibi gider. */
data class SosyalKimlik(
    val provider: String,
    val idToken: String,
    /** Apple: iptal icin sunucu refresh token'a cevirir. */
    val authorizationCode: String? = null,
    /** Apple adi belirtece koymaz; yalniz ILK yetkilendirmede gelir. */
    val name: String? = null
)

sealed class SosyalGirisSonucu {
    data class Basarili(val kimlik: SosyalKimlik) : SosyalGirisSonucu()
    /** Kullanici pencereyi kapatti — hata gosterilmez. */
    data object Iptal : SosyalGirisSonucu()
    data class Hata(val mesaj: String) : SosyalGirisSonucu()
}

interface SosyalGirisSaglayici {
    val googleVar: Boolean
    val appleVar: Boolean
    suspend fun google(): SosyalGirisSonucu
    suspend fun apple(): SosyalGirisSonucu
}

/** Platform koprusu: Android Credential Manager, iOS AuthenticationServices. */
@Composable
expect fun rememberSosyalGiris(): SosyalGirisSaglayici

/** POST /auth/social govdesi (auth.ts socialSchema). */
@Serializable
data class SocialLoginRequest(
    val provider: String,
    val idToken: String,
    val authorizationCode: String? = null,
    val name: String? = null,
    val acceptedLegal: Boolean? = null,
    val appleClientId: String? = null
)

/** Sunucu 409 CONSENT_REQUIRED dondu: yeni hesap; yasal onay alinip yeniden gonderilmeli. */
class OnayGerekli : Exception("CONSENT_REQUIRED")
