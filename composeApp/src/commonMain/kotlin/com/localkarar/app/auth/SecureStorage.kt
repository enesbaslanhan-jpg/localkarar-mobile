package com.localkarar.app.auth

expect class SecureStorage {
    fun saveToken(token: String)
    fun readToken(): String?
    fun clearToken()
}
