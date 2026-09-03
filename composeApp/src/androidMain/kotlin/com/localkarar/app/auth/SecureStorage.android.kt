package com.localkarar.app.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.localkarar.app.core.AppLog

private const val DEPO_ADI = "secure_prefs"

/**
 * Token deposu — Android Keystore ile sifreli.
 *
 * 🔴 KURULUM NEDEN try/catch ICINDE:
 *
 * `EncryptedSharedPreferences.create` firlatabiliyor. En bilinen sebep cihaz
 * yedeginden geri yukleme: sifreli dosya geri geliyor ama onu acacak Keystore
 * anahtari GELMIYOR (anahtar cihaza bagli, disari cikmaz). Sonuc
 * `AEADBadTagException` / `InvalidProtocolBufferException`.
 *
 * Bu sinif `MainActivity.onCreate` icinde kuruluyor ve firlatma dogrudan
 * ACILISTA COKMEYE donusuyordu -- her acilista ayni cokme, kullanicinin cikis
 * yolu yok, uygulamayi silmekten baska care kalmiyordu.
 *
 * Artik bozuk depo TEMIZLENIP yeniden kuruluyor. Bedeli: kullanici bir kez
 * yeniden giris yapiyor. Alternatifi kullanilamaz bir uygulama.
 *
 * NOT: `data_extraction_rules.xml` ve `backup_rules.xml` bu depoyu yedekten
 * DISLIYOR, yani asil sebep de ortadan kalkti. Buradaki koruma ikinci hat.
 */
actual class SecureStorage(context: Context) {

    private val sharedPreferences: SharedPreferences = guvenliDepoAc(context)

    private fun guvenliDepoAc(context: Context): SharedPreferences {
        fun kur(): SharedPreferences {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            return EncryptedSharedPreferences.create(
                context,
                DEPO_ADI,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }

        return try {
            kur()
        } catch (ilkHata: Exception) {
            AppLog.e("SecureStorage", "Sifreli depo acilamadi, sifirlaniyor", ilkHata)
            // Bozuk dosyayi birak, sifirdan kur.
            context.deleteSharedPreferences(DEPO_ADI)
            try {
                kur()
            } catch (ikinciHata: Exception) {
                // Ikinci deneme de dustuyse cihazda Keystore gercekten
                // calismiyor demektir. Cokmek yerine sifresiz yedege dusuluyor:
                // oturum saklanmaya devam ediyor ama SIFRESIZ.
                //
                // ⚠️ Bu bilincli bir odun: alternatif, uygulamanin hic
                // acilmamasi. Token'in omru zaten 8 saat ve cihaz sahibi
                // disindaki birinin uygulama ic depolamasina erismesi root
                // gerektiriyor.
                AppLog.e("SecureStorage", "Sifreli depo ikinci denemede de acilamadi", ikinciHata)
                context.getSharedPreferences("${DEPO_ADI}_fallback", Context.MODE_PRIVATE)
            }
        }
    }

    actual fun saveToken(token: String) {
        sharedPreferences.edit().putString("auth_token", token).apply()
    }

    actual fun readToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }

    actual fun clearToken() {
        sharedPreferences.edit().remove("auth_token").apply()
    }

    actual fun saveRefreshToken(refreshToken: String) {
        sharedPreferences.edit().putString("refresh_token", refreshToken).apply()
    }

    actual fun readRefreshToken(): String? {
        return sharedPreferences.getString("refresh_token", null)
    }

    actual fun clearRefreshToken() {
        sharedPreferences.edit().remove("refresh_token").apply()
    }

    actual fun clearAll() {
        sharedPreferences.edit().remove("auth_token").remove("refresh_token").apply()
    }
}
