package com.localkarar.app.push

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.localkarar.app.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PushAndroidFrameworkTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun installationIdIsPersistentUuidAndFingerprintIsUserScoped() {
        context.getSharedPreferences("lk_push_lifecycle", Context.MODE_PRIVATE).edit().clear().commit()
        val first = PushPreferences(context)
        val installationId = first.installationId
        val second = PushPreferences(context)

        assertEquals(installationId, second.installationId)
        assertTrue(Regex("[0-9a-f-]{36}").matches(installationId))
        assertNotEquals(first.fingerprint(1, "token-a"), first.fingerprint(2, "token-a"))
        assertNotEquals(first.fingerprint(1, "token-a"), first.fingerprint(1, "token-b"))
    }

    @Test
    fun nativeOnlyTargetRoundTripsThroughExplicitInternalExtras() {
        val source = Intent(context, MainActivity::class.java)
        PushIntentContract.put(source, PushTarget.WorkspaceRecord("ws-1", "rec-2"))

        assertEquals(
            mapOf(
                "target" to "workspace_record",
                "workspaceId" to "ws-1",
                "recordId" to "rec-2"
            ),
            PushIntentContract.read(source)
        )
    }
}
