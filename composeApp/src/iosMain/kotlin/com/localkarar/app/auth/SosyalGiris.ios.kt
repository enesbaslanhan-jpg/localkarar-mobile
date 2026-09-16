@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package com.localkarar.app.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.localkarar.app.core.AppLog
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import platform.AuthenticationServices.ASAuthorization
import platform.AuthenticationServices.ASAuthorizationAppleIDCredential
import platform.AuthenticationServices.ASAuthorizationAppleIDProvider
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationControllerDelegateProtocol
import platform.AuthenticationServices.ASAuthorizationControllerPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASAuthorizationErrorCanceled
import platform.AuthenticationServices.ASAuthorizationScopeEmail
import platform.AuthenticationServices.ASAuthorizationScopeFullName
import platform.AuthenticationServices.ASPresentationAnchor
import platform.AuthenticationServices.ASWebAuthenticationPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASWebAuthenticationSession
import platform.AuthenticationServices.ASWebAuthenticationSessionErrorCodeCanceledLogin
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLComponents
import platform.Foundation.NSURLQueryItem
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.create
import platform.Security.SecRandomCopyBytes
import platform.Security.kSecRandomDefault
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import kotlin.coroutines.resume

/*
 * iOS — Apple ve Google ile giris (16.09.2026). Ucuncu taraf SDK YOK:
 *
 *  - Apple: AuthenticationServices (ASAuthorizationAppleIDProvider). Belirtec
 *    (identityToken) + authorizationCode + yalniz ilk seferde ad.
 *  - Google: GoogleSignIn SDK yerine ASWebAuthenticationSession + OAuth PKCE.
 *    iOS istemcisinin sirri yoktur; kod PKCE ile degistirilir ve donen
 *    id_token sunucuya gider. Yonlendirme adresi Google'in iOS istemcisi
 *    icin zorunlu kildigi ters-DNS semasi: com.googleusercontent.apps.<id>:/oauthredirect
 *    (Info.plist'te CFBundleURLSchemes olarak kayitli olmali).
 *
 * Kotlin/Native delegate nesneleri NSObject'ten turetilir; controller
 * cagri boyunca referansta tutulur (yoksa ARC erken serbest birakir).
 */

private fun anaPencere(): UIWindow? {
    val pencereler = UIApplication.sharedApplication.windows
    @Suppress("UNCHECKED_CAST")
    return (pencereler as List<UIWindow>).firstOrNull { it.isKeyWindow() } ?: (pencereler as List<UIWindow>).firstOrNull()
}

private fun NSData.utf8(): String? = NSString.create(this, NSUTF8StringEncoding) as String?

private fun rastgeleUrlGuvenli(bayt: Int): String {
    val dizi = ByteArray(bayt)
    dizi.usePinned { SecRandomCopyBytes(kSecRandomDefault, bayt.toULong(), it.addressOf(0)) }
    return base64Url(dizi)
}

private fun base64Url(dizi: ByteArray): String {
    val data = dizi.usePinned { NSData.create(bytes = it.addressOf(0), length = dizi.size.toULong()) }
    return data.base64EncodedStringWithOptions(0u).replace('+', '-').replace('/', '_').trimEnd('=')
}

private fun sha256Base64Url(metin: String): String {
    val giris = metin.encodeToByteArray()
    val cikti = ByteArray(CC_SHA256_DIGEST_LENGTH)
    giris.usePinned { g -> cikti.usePinned { c -> CC_SHA256(g.addressOf(0), giris.size.toUInt(), c.addressOf(0).reinterpret()) } }
    return base64Url(cikti)
}

private class AppleDelegate(
    private val tamamla: (SosyalGirisSonucu) -> Unit
) : NSObject(), ASAuthorizationControllerDelegateProtocol, ASAuthorizationControllerPresentationContextProvidingProtocol {

    override fun authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization: ASAuthorization) {
        val kimlik = didCompleteWithAuthorization.credential as? ASAuthorizationAppleIDCredential
        val belirtec = kimlik?.identityToken?.utf8()
        if (kimlik == null || belirtec.isNullOrBlank()) {
            tamamla(SosyalGirisSonucu.Hata("Apple kimliği alınamadı."))
            return
        }
        val ad = listOfNotNull(kimlik.fullName?.givenName, kimlik.fullName?.familyName)
            .joinToString(" ").trim().ifBlank { null }
        tamamla(
            SosyalGirisSonucu.Basarili(
                SosyalKimlik(
                    provider = "apple",
                    idToken = belirtec,
                    authorizationCode = kimlik.authorizationCode?.utf8(),
                    name = ad
                )
            )
        )
    }

    override fun authorizationController(controller: ASAuthorizationController, didCompleteWithError: NSError) {
        if (didCompleteWithError.code == ASAuthorizationErrorCanceled) tamamla(SosyalGirisSonucu.Iptal)
        else {
            AppLog.e("SosyalGiris", "Apple giris hatasi ${didCompleteWithError.code}")
            tamamla(SosyalGirisSonucu.Hata("Apple ile giriş tamamlanamadı."))
        }
    }

    override fun presentationAnchorForAuthorizationController(controller: ASAuthorizationController): ASPresentationAnchor =
        anaPencere() ?: UIWindow()
}

private class WebOturumSunucusu : NSObject(), ASWebAuthenticationPresentationContextProvidingProtocol {
    override fun presentationAnchorForWebAuthenticationSession(session: ASWebAuthenticationSession): ASPresentationAnchor =
        anaPencere() ?: UIWindow()
}

@Serializable
private data class GoogleTokenYaniti(val id_token: String? = null)

private class IosSosyalGiris : SosyalGirisSaglayici {
    override val googleVar: Boolean = true
    override val appleVar: Boolean = true

    /* Delegate referansi cagri suresince tutulur; yerel degisken olsa ARC toplardi. */
    private var appleDelegate: AppleDelegate? = null
    private var appleController: ASAuthorizationController? = null
    private var webOturumu: ASWebAuthenticationSession? = null
    private var webSunucu: WebOturumSunucusu? = null

    override suspend fun apple(): SosyalGirisSonucu = suspendCancellableCoroutine { devam ->
        val istek = ASAuthorizationAppleIDProvider().createRequest().apply {
            requestedScopes = listOf(ASAuthorizationScopeFullName, ASAuthorizationScopeEmail)
        }
        val delegate = AppleDelegate { sonuc ->
            appleDelegate = null; appleController = null
            if (devam.isActive) devam.resume(sonuc)
        }
        val controller = ASAuthorizationController(authorizationRequests = listOf(istek))
        controller.delegate = delegate
        controller.presentationContextProvider = delegate
        appleDelegate = delegate
        appleController = controller
        controller.performRequests()
    }

    override suspend fun google(): SosyalGirisSonucu {
        val istemci = SosyalGirisKimlikleri.GOOGLE_IOS_CLIENT_ID
        val sema = "com.googleusercontent.apps." + istemci.removeSuffix(".apps.googleusercontent.com")
        val yonlendirme = "$sema:/oauthredirect"
        val dogrulayici = rastgeleUrlGuvenli(32)
        val durum = rastgeleUrlGuvenli(16)
        val bilesenler = NSURLComponents(string = "https://accounts.google.com/o/oauth2/v2/auth")!!
        bilesenler.queryItems = listOf(
            NSURLQueryItem(name = "client_id", value = istemci),
            NSURLQueryItem(name = "redirect_uri", value = yonlendirme),
            NSURLQueryItem(name = "response_type", value = "code"),
            NSURLQueryItem(name = "scope", value = "openid email profile"),
            NSURLQueryItem(name = "code_challenge", value = sha256Base64Url(dogrulayici)),
            NSURLQueryItem(name = "code_challenge_method", value = "S256"),
            NSURLQueryItem(name = "state", value = durum),
            NSURLQueryItem(name = "prompt", value = "select_account")
        )
        val url = bilesenler.URL ?: return SosyalGirisSonucu.Hata("Google adresi oluşturulamadı.")

        val kod = suspendCancellableCoroutine<Pair<String?, SosyalGirisSonucu?>> { devam ->
            val sunucu = WebOturumSunucusu()
            val oturum = ASWebAuthenticationSession(uRL = url, callbackURLScheme = sema) { geriDonus: NSURL?, hata: NSError? ->
                webOturumu = null; webSunucu = null
                if (!devam.isActive) return@ASWebAuthenticationSession
                if (hata != null) {
                    devam.resume(null to (if (hata.code == ASWebAuthenticationSessionErrorCodeCanceledLogin) SosyalGirisSonucu.Iptal else SosyalGirisSonucu.Hata("Google ile giriş tamamlanamadı.")))
                    return@ASWebAuthenticationSession
                }
                val parcalar = geriDonus?.let { NSURLComponents(uRL = it, resolvingAgainstBaseURL = false) }
                @Suppress("UNCHECKED_CAST")
                val ogeler = (parcalar?.queryItems as? List<NSURLQueryItem>).orEmpty()
                val gelenDurum = ogeler.firstOrNull { it.name == "state" }?.value
                val gelenKod = ogeler.firstOrNull { it.name == "code" }?.value
                if (gelenDurum != durum || gelenKod.isNullOrBlank()) devam.resume(null to SosyalGirisSonucu.Hata("Google yanıtı doğrulanamadı."))
                else devam.resume(gelenKod to null)
            }
            oturum.presentationContextProvider = sunucu
            oturum.prefersEphemeralWebBrowserSession = false
            webOturumu = oturum; webSunucu = sunucu
            if (!oturum.start()) devam.resume(null to SosyalGirisSonucu.Hata("Giriş penceresi açılamadı."))
        }
        kod.second?.let { return it }
        val yetkiKodu = kod.first ?: return SosyalGirisSonucu.Hata("Google yanıtı alınamadı.")

        /* PKCE degisimi: sir yok, code_verifier yeter. Yalniz id_token kullanilir. */
        return try {
            val http = HttpClient(Darwin) { install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) } }
            val yanit: GoogleTokenYaniti = http.submitForm(
                url = "https://oauth2.googleapis.com/token",
                formParameters = Parameters.build {
                    append("client_id", istemci)
                    append("code", yetkiKodu)
                    append("code_verifier", dogrulayici)
                    append("grant_type", "authorization_code")
                    append("redirect_uri", yonlendirme)
                }
            ).body()
            http.close()
            val idToken = yanit.id_token
            if (idToken.isNullOrBlank()) SosyalGirisSonucu.Hata("Google kimliği alınamadı.")
            else SosyalGirisSonucu.Basarili(SosyalKimlik(provider = "google", idToken = idToken))
        } catch (e: Exception) {
            AppLog.e("SosyalGiris", "Google belirtec degisimi basarisiz", e)
            SosyalGirisSonucu.Hata("Google ile giriş tamamlanamadı.")
        }
    }
}

@Composable
actual fun rememberSosyalGiris(): SosyalGirisSaglayici = remember { IosSosyalGiris() }
