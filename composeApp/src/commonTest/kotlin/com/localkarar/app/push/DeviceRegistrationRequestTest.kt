package com.localkarar.app.push

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeviceRegistrationRequestTest {
    @Test
    fun requestBodyContainsNoClientOwnedUserId() {
        val json = Json.encodeToString(
            DeviceRegistrationRequest(
                pushToken = "valid-fcm-token",
                platform = "android",
                appVersion = "1.0",
                locale = "tr-TR"
            )
        )

        assertFalse(json.contains("userId"))
        assertTrue(json.contains("\"platform\":\"android\""))
    }
}

