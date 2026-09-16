package com.localkarar.app.network

import com.localkarar.app.auth.LoginResponse
import com.localkarar.app.auth.SocialLoginRequest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * SOSYAL GIRIS SOZLESMESI (16.09.2026) — POST /auth/social.
 * - acceptedLegal verilmediyse govdede HIC yer almaz (sunucu 409 ile onay ister);
 *   true verilirse gider.
 * - Sunucu yaniti isNewUser / hasPassword tasir; eski yanit (alan yok) da cozumlenir.
 */
class SocialLoginContractTest {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    @Test
    fun onaysizIstekteAcceptedLegalYok() {
        val govde = json.encodeToString(SocialLoginRequest.serializer(), SocialLoginRequest(provider = "google", idToken = "jwt"))
        assertFalse(govde.contains("acceptedLegal"), govde)
        assertTrue(govde.contains("\"provider\":\"google\""))
    }

    @Test
    fun onayliIstekAcceptedLegalTrueTasir() {
        val govde = json.encodeToString(
            SocialLoginRequest.serializer(),
            SocialLoginRequest(provider = "apple", idToken = "jwt", authorizationCode = "kod", name = "Ayşe K", acceptedLegal = true, appleClientId = "com.localkarar.app")
        )
        assertTrue(govde.contains("\"acceptedLegal\":true"), govde)
        assertTrue(govde.contains("\"authorizationCode\":\"kod\""))
    }

    @Test
    fun yanitYeniVeEskiSunucudaCozumlenir() {
        val yeni = json.decodeFromString<LoginResponse>("""{"token":"t","refreshToken":"r","isNewUser":true,"user":{"id":1,"email":"a@b.c","name":"A","role":"student","hasPassword":false}}""")
        assertTrue(yeni.isNewUser); assertFalse(yeni.user.hasPassword)
        val eski = json.decodeFromString<LoginResponse>("""{"token":"t","user":{"id":1,"email":"a@b.c","name":"A","role":"student"}}""")
        assertFalse(eski.isNewUser); assertTrue(eski.user.hasPassword)
        assertEquals("A", eski.user.name)
    }
}
