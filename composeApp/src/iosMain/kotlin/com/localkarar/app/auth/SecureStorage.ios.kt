package com.localkarar.app.auth

import kotlinx.cinterop.*
import platform.CoreFoundation.*
import platform.Foundation.*
import platform.Security.*

actual class SecureStorage {
    private val account = "auth_token"
    private val service = "com.localkarar.app"

    actual fun saveToken(token: String) {
        val data = (token as NSString).dataUsingEncoding(NSUTF8StringEncoding)
        
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to account,
            kSecValueData to data
        )

        // Delete any existing token before saving a new one
        SecItemDelete(query as CFDictionaryRef)
        
        // Save the new token
        SecItemAdd(query as CFDictionaryRef, null)
    }

    actual fun readToken(): String? {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to account,
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

    actual fun clearToken() {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to account
        )
        SecItemDelete(query as CFDictionaryRef)
    }
}
