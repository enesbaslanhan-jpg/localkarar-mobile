package com.localkarar.app.core

import platform.Foundation.NSUserDefaults

/**
 * Tercih deposu — `NSUserDefaults`.
 *
 * Android'deki `SharedPreferences` karsiligi. Keychain KULLANILMIYOR: burada
 * gizli bir sey saklanmiyor ve Keychain oturumdan bagimsiz kalicilik icin
 * gereksiz agir.
 */
actual class AppPreferences {

    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    actual fun getString(key: String): String? = defaults.stringForKey(key)

    actual fun remove(key: String) {
        defaults.removeObjectForKey(key)
    }
}
