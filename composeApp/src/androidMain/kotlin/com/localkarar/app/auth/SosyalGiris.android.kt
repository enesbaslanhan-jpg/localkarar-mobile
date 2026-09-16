package com.localkarar.app.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.localkarar.app.core.AppLog

/*
 * ANDROID — Google ile giris, Credential Manager uzerinden (16.09.2026).
 *
 * `serverClientId` WEB istemcisidir: Google, Android uygulamasini paket adi +
 * imza SHA-1'inden tanir (Cloud Console'daki iki Android istemcisi), belirtecin
 * `aud`ina ise web istemcisini yazar; sunucu o kimligi kabul ediyor.
 *
 * Iki adim: once cihazda oturumu acik Google hesaplariyla "yetkili" istek
 * (filterByAuthorizedAccounts=true → tek dokunus), hesap yoksa acik hesap
 * secici (GetSignInWithGoogleOption). Kullanici kapatirsa Iptal; hata
 * mesajlari sunucuya degil ekrana gider, belirtec loglanmaz.
 *
 * Apple ile giris Android'de YOK: Apple'in Android SDK'si yok, web akisi
 * App Store kurali disinda ve kullanici beklentisi dusuk. Dugme cizilmez.
 */
private class AndroidSosyalGiris(private val context: Context) : SosyalGirisSaglayici {
    override val googleVar: Boolean = true
    override val appleVar: Boolean = false

    private fun Context.etkinlik(): Activity? {
        var c: Context? = this
        while (c is ContextWrapper) {
            if (c is Activity) return c
            c = c.baseContext
        }
        return null
    }

    override suspend fun google(): SosyalGirisSonucu {
        val activity = context.etkinlik() ?: return SosyalGirisSonucu.Hata("Giriş penceresi açılamadı.")
        val yonetici = CredentialManager.create(activity)
        val yetkili = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setServerClientId(SosyalGirisKimlikleri.GOOGLE_WEB_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(true)
                    .setAutoSelectEnabled(true)
                    .build()
            ).build()
        val acik = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetSignInWithGoogleOption.Builder(SosyalGirisKimlikleri.GOOGLE_WEB_CLIENT_ID).build()
            ).build()

        return try {
            val sonuc = try {
                yonetici.getCredential(activity, yetkili)
            } catch (e: NoCredentialException) {
                yonetici.getCredential(activity, acik)
            }
            val kimlik = sonuc.credential
            if (kimlik is CustomCredential && kimlik.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val google = GoogleIdTokenCredential.createFrom(kimlik.data)
                SosyalGirisSonucu.Basarili(
                    SosyalKimlik(provider = "google", idToken = google.idToken, name = google.displayName)
                )
            } else {
                SosyalGirisSonucu.Hata("Google kimliği alınamadı.")
            }
        } catch (e: GetCredentialCancellationException) {
            SosyalGirisSonucu.Iptal
        } catch (e: NoCredentialException) {
            SosyalGirisSonucu.Hata("Bu cihazda Google hesabı bulunamadı. Ayarlar'dan bir Google hesabı ekleyin.")
        } catch (e: GetCredentialException) {
            AppLog.e("SosyalGiris", "Google Credential Manager hatasi: ${e.type}", e)
            SosyalGirisSonucu.Hata("Google ile giriş tamamlanamadı. Tekrar deneyin.")
        } catch (e: Exception) {
            AppLog.e("SosyalGiris", "Google giris hatasi", e)
            SosyalGirisSonucu.Hata("Google ile giriş tamamlanamadı.")
        }
    }

    override suspend fun apple(): SosyalGirisSonucu =
        SosyalGirisSonucu.Hata("Apple ile giriş bu cihazda desteklenmiyor.")
}

@Composable
actual fun rememberSosyalGiris(): SosyalGirisSaglayici {
    val context = LocalContext.current
    return remember(context) { AndroidSosyalGiris(context) }
}
