package com.localkarar.app.auth

expect class SecureStorage {
    fun saveToken(token: String)
    fun readToken(): String?
    fun clearToken()

    fun saveRefreshToken(refreshToken: String)
    fun readRefreshToken(): String?
    fun clearRefreshToken()
    fun clearAll()
}
