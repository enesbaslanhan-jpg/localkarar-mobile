package com.localkarar.app.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

actual class SecureStorage(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    actual fun saveToken(token: String) {
        sharedPreferences.edit().putString("auth_token", token).apply()
    }

    actual fun readToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }

    actual fun clearToken() {
        sharedPreferences.edit().remove("auth_token").apply()
    }
}
