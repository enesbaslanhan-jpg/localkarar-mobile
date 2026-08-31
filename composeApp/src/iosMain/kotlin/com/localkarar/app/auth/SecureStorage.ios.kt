package com.localkarar.app.auth

import kotlinx.cinterop.*
import platform.CoreFoundation.*
import platform.Foundation.*
import platform.Security.*

@OptIn(ExperimentalForeignApi::class)
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
        deleteKeychainItem(itemAccount)
        val data = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return
        val query = NSMutableDictionary().apply {
            setObject(kSecClassGenericPassword, forKey = kSecClass)
            setObject(service, forKey = kSecAttrService)
            setObject(itemAccount, forKey = kSecAttrAccount)
            setObject(data, forKey = kSecValueData)
        }
        val cfQuery = CFBridgingRetain(query) as CFDictionaryRef?
        try {
            SecItemAdd(cfQuery, null)
        } finally {
            if (cfQuery != null) CFRelease(cfQuery)
        }
    }

    private fun readKeychainItem(itemAccount: String): String? {
        val query = NSMutableDictionary().apply {
            setObject(kSecClassGenericPassword, forKey = kSecClass)
            setObject(service, forKey = kSecAttrService)
            setObject(itemAccount, forKey = kSecAttrAccount)
            setObject(kCFBooleanTrue, forKey = kSecReturnData)
            setObject(kSecMatchLimitOne, forKey = kSecMatchLimit)
        }
        val cfQuery = CFBridgingRetain(query) as CFDictionaryRef? ?: return null
        var result: String? = null
        try {
            memScoped {
                val resultPtr = alloc<CFTypeRefVar>()
                val status = SecItemCopyMatching(cfQuery, resultPtr.ptr)
                if (status == errSecSuccess) {
                    val cfData = resultPtr.value
                    if (cfData != null) {
                        val nsData = cfData as? NSData
                        if (nsData != null) {
                            result = NSString.create(data = nsData, encoding = NSUTF8StringEncoding) as? String
                        }
                    }
                }
            }
        } finally {
            CFRelease(cfQuery)
        }
        return result
    }

    private fun deleteKeychainItem(itemAccount: String) {
        val query = NSMutableDictionary().apply {
            setObject(kSecClassGenericPassword, forKey = kSecClass)
            setObject(service, forKey = kSecAttrService)
            setObject(itemAccount, forKey = kSecAttrAccount)
        }
        val cfQuery = CFBridgingRetain(query) as CFDictionaryRef? ?: return
        try {
            SecItemDelete(cfQuery)
        } finally {
            CFRelease(cfQuery)
        }
    }
}
