package com.localkarar.app.push

import android.content.Context
import java.security.MessageDigest
import java.util.UUID

internal class PushPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val installationId: String
        get() {
            val existing = preferences.getString(KEY_INSTALLATION_ID, null)
            if (!existing.isNullOrBlank()) return existing
            return UUID.randomUUID().toString().also {
                preferences.edit().putString(KEY_INSTALLATION_ID, it).commit()
            }
        }

    var pushToken: String?
        get() = preferences.getString(KEY_PUSH_TOKEN, null)
        set(value) { preferences.edit().putString(KEY_PUSH_TOKEN, value).apply() }

    var activeUserId: Int?
        get() = if (preferences.contains(KEY_ACTIVE_USER_ID)) {
            preferences.getInt(KEY_ACTIVE_USER_ID, -1).takeIf { it > 0 }
        } else null
        set(value) {
            val editor = preferences.edit()
            if (value == null) editor.remove(KEY_ACTIVE_USER_ID) else editor.putInt(KEY_ACTIVE_USER_ID, value)
            editor.apply()
        }

    var registeredFingerprint: String?
        get() = preferences.getString(KEY_REGISTERED_FINGERPRINT, null)
        set(value) {
            val editor = preferences.edit()
            if (value == null) editor.remove(KEY_REGISTERED_FINGERPRINT) else editor.putString(KEY_REGISTERED_FINGERPRINT, value)
            editor.apply()
        }

    var permissionPrompted: Boolean
        get() = preferences.getBoolean(KEY_PERMISSION_PROMPTED, false)
        set(value) { preferences.edit().putBoolean(KEY_PERMISSION_PROMPTED, value).apply() }

    fun clearUserScopedState() {
        preferences.edit()
            .remove(KEY_ACTIVE_USER_ID)
            .remove(KEY_REGISTERED_FINGERPRINT)
            .apply()
    }

    fun fingerprint(userId: Int, token: String): String {
        val source = "$userId|$installationId|$token"
        return MessageDigest.getInstance("SHA-256")
            .digest(source.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    private companion object {
        const val PREFS_NAME = "lk_push_lifecycle"
        const val KEY_INSTALLATION_ID = "installation_id"
        const val KEY_PUSH_TOKEN = "push_token"
        const val KEY_ACTIVE_USER_ID = "active_user_id"
        const val KEY_REGISTERED_FINGERPRINT = "registered_fingerprint"
        const val KEY_PERMISSION_PROMPTED = "notification_permission_prompted"
    }
}

