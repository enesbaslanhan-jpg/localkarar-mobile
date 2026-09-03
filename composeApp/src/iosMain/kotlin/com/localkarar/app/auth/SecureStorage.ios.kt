package com.localkarar.app.auth

import kotlinx.cinterop.*
import platform.CoreFoundation.*
import platform.Foundation.*
import platform.Security.*
import com.localkarar.app.core.AppLog

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
        val dict = CFDictionaryCreateMutable(
            kCFAllocatorDefault,
            0,
            kCFCopyStringDictionaryKeyCallBacks.ptr,
            kCFTypeDictionaryValueCallBacks.ptr
        ) ?: return

        val serviceCf = CFBridgingRetain(service as NSString)
        val accountCf = CFBridgingRetain(itemAccount as NSString)
        val dataCf = CFBridgingRetain(data)

        try {
            CFDictionarySetValue(dict, kSecClass, kSecClassGenericPassword)
            CFDictionarySetValue(dict, kSecAttrService, serviceCf)
            CFDictionarySetValue(dict, kSecAttrAccount, accountCf)
            CFDictionarySetValue(dict, kSecValueData, dataCf)

            /*
             * 🔴 kSecAttrAccessible HIC SET EDILMIYORDU.
             *
             * Varsayilan `kSecAttrAccessibleWhenUnlocked`tir ve "ThisDeviceOnly"
             * olmayan her deger, ogeyi iCloud/iTunes YEDEGINE sokar; yani
             * kullanicinin baska bir cihazina geri yuklenir. Bir erisim tokeni
             * ve bir yenileme tokeni icin bu istenmez -- yenileme tokeni 30 gun
             * gecerli ve tek basina hesaba tam erisim veriyor.
             *
             * AfterFirstUnlock secildi, WhenUnlocked DEGIL: uygulama arka planda
             * uyandiginda (bildirim, derin baglanti) cihaz kilitliyse
             * WhenUnlocked ile token okunamaz ve istek sebepsiz duserdi.
             *
             * Yedeklemeyi kapatan kisim ThisDeviceOnly eki.
             */
            CFDictionarySetValue(
                dict,
                kSecAttrAccessible,
                kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
            )

            val status = SecItemAdd(dict, null)
            if (status != errSecSuccess) {
                // Donus degeri atiliyordu: basarisiz kayit tamamen sessizdi ve
                // kullanici bir sonraki aciliste sebepsiz giris ekraninda
                // buluyordu kendini.
                AppLog.e("SecureStorage", "Keychain kaydi basarisiz (OSStatus=$status)")
            }
        } finally {
            if (serviceCf != null) CFRelease(serviceCf)
            if (accountCf != null) CFRelease(accountCf)
            if (dataCf != null) CFRelease(dataCf)
            CFRelease(dict)
        }
    }

    private fun readKeychainItem(itemAccount: String): String? {
        val dict = CFDictionaryCreateMutable(
            kCFAllocatorDefault,
            0,
            kCFCopyStringDictionaryKeyCallBacks.ptr,
            kCFTypeDictionaryValueCallBacks.ptr
        ) ?: return null

        val serviceCf = CFBridgingRetain(service as NSString)
        val accountCf = CFBridgingRetain(itemAccount as NSString)
        var result: String? = null

        try {
            CFDictionarySetValue(dict, kSecClass, kSecClassGenericPassword)
            CFDictionarySetValue(dict, kSecAttrService, serviceCf)
            CFDictionarySetValue(dict, kSecAttrAccount, accountCf)
            CFDictionarySetValue(dict, kSecReturnData, kCFBooleanTrue)
            CFDictionarySetValue(dict, kSecMatchLimit, kSecMatchLimitOne)

            memScoped {
                val resultPtr = alloc<CFTypeRefVar>()
                val status = SecItemCopyMatching(dict, resultPtr.ptr)
                if (status == errSecSuccess) {
                    val cfData = resultPtr.value
                    if (cfData != null) {
                        val nsData = CFBridgingRelease(cfData) as? NSData
                        if (nsData != null) {
                            result = NSString.create(data = nsData, encoding = NSUTF8StringEncoding) as? String
                        }
                    }
                }
            }
        } finally {
            if (serviceCf != null) CFRelease(serviceCf)
            if (accountCf != null) CFRelease(accountCf)
            CFRelease(dict)
        }
        return result
    }

    private fun deleteKeychainItem(itemAccount: String) {
        val dict = CFDictionaryCreateMutable(
            kCFAllocatorDefault,
            0,
            kCFCopyStringDictionaryKeyCallBacks.ptr,
            kCFTypeDictionaryValueCallBacks.ptr
        ) ?: return

        val serviceCf = CFBridgingRetain(service as NSString)
        val accountCf = CFBridgingRetain(itemAccount as NSString)

        try {
            CFDictionarySetValue(dict, kSecClass, kSecClassGenericPassword)
            CFDictionarySetValue(dict, kSecAttrService, serviceCf)
            CFDictionarySetValue(dict, kSecAttrAccount, accountCf)
            SecItemDelete(dict)
        } finally {
            if (serviceCf != null) CFRelease(serviceCf)
            if (accountCf != null) CFRelease(accountCf)
            CFRelease(dict)
        }
    }
}
