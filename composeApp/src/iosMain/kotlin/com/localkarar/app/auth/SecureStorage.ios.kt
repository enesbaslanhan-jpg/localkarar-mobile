package com.localkarar.app.auth

import kotlinx.cinterop.*
import platform.CoreFoundation.*
import platform.Foundation.*
import platform.Security.*

actual class SecureStorage {
    private val account = "auth_token"
    private val service = "com.localkarar.app"

    actual fun saveToken(token: String) {
        saveKeychainItem(account, token)
    }

    actual fun readToken(): String? {
        return readKeychainItem(account)
    }

    actual fun clearToken() {
        deleteKeychainItem(account)
    }

    actual fun saveRefreshToken(refreshToken: String) {
        saveKeychainItem("refresh_token", refreshToken)
    }

    actual fun readRefreshToken(): String? {
        return readKeychainItem("refresh_token")
    }

    actual fun clearRefreshToken() {
        deleteKeychainItem("refresh_token")
    }

    actual fun clearAll() {
        deleteKeychainItem(account)
        deleteKeychainItem("refresh_token")
    }

    private fun saveKeychainItem(itemAccount: String, value: String) {
        val data = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding)
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to itemAccount,
            kSecValueData to data
        )
        SecItemDelete(query as CFDictionaryRef)
        SecItemAdd(query as CFDictionaryRef, null)
    }

    private fun readKeychainItem(itemAccount: String): String? {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to itemAccount,
            kSecReturnData to true,
            kSecMatchLimit to kSecMatchLimitOne
        )
        var result: String? = null
        memScoped {
            val resultPtr = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query as CFDictionaryRef, resultPtr.ptr)
            if (status == errSecSuccess) {
                val data = resultPtr.value as? NSData
                if (data != null) {
                    val nsString = NSString.create(data = data, encoding = NSUTF8StringEncoding)
                    result = nsString as String?
                }
            }
        }
        return result
    }

    private fun deleteKeychainItem(itemAccount: String) {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to itemAccount
        )
        SecItemDelete(query as CFDictionaryRef)
    }
}
