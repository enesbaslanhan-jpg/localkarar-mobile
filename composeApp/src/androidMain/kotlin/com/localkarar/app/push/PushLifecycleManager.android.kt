package com.localkarar.app.push

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.localkarar.app.BuildConfig
import io.ktor.client.HttpClient
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

actual class PushLifecycleManager actual constructor(httpClient: HttpClient) {
    private val registrationService = DeviceRegistrationService(httpClient)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val registrationMutex = Mutex()
    @Volatile private var activeUserId: Int? = null
    @Volatile private var appContext: Context? = null

    init {
        PushRuntime.attach(this)
    }

    @Composable
    actual fun BindAuthenticatedSession(userId: Int) {
        val context = LocalContext.current.applicationContext
        val preferences = PushPreferences(context)
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }

        LaunchedEffect(userId) {
            bindContext(context)
            onAuthenticated(userId)

            // Ask once, only after the authenticated shell has had time to settle.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED &&
                !preferences.permissionPrompted
            ) {
                delay(1_200L)
                preferences.permissionPrompted = true
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun bindContext(context: Context) {
        appContext = context.applicationContext
    }

    private fun onAuthenticated(userId: Int) {
        val context = appContext ?: return
        val preferences = PushPreferences(context)
        if (preferences.activeUserId != userId) {
            preferences.clearUserScopedState()
        }
        preferences.activeUserId = userId
        activeUserId = userId

        preferences.pushToken?.let { registerIfNeeded(userId, it) }
        val messaging = firebaseMessagingOrNull(context) ?: return
        messaging.token.addOnCompleteListener { task ->
            val token = if (task.isSuccessful) task.result?.takeIf { it.isNotBlank() } else null
            if (token != null) onTokenRefreshed(token)
        }
    }

    internal fun onTokenRefreshed(token: String) {
        val context = appContext ?: return
        val preferences = PushPreferences(context)
        if (preferences.pushToken != token) {
            preferences.pushToken = token
            preferences.registeredFingerprint = null
        }
        activeUserId?.let { registerIfNeeded(it, token) }
    }

    private fun registerIfNeeded(userId: Int, token: String) {
        val context = appContext ?: return
        scope.launch {
            registrationMutex.withLock {
                if (activeUserId != userId) return@withLock
                val preferences = PushPreferences(context)
                val fingerprint = preferences.fingerprint(userId, token)
                if (preferences.registeredFingerprint == fingerprint) return@withLock

                registrationService.registerDevice(
                    installationId = preferences.installationId,
                    pushToken = token,
                    appVersion = BuildConfig.VERSION_NAME,
                    locale = Locale.getDefault().toLanguageTag()
                ).onSuccess {
                    if (activeUserId == userId) preferences.registeredFingerprint = fingerprint
                }
            }
        }
    }

    actual suspend fun unregisterBeforeLogout() {
        val context = appContext ?: return
        val preferences = PushPreferences(context)
        registrationService.unregisterDevice(preferences.installationId)
    }

    actual fun clearSession() {
        activeUserId = null
        appContext?.let { PushPreferences(it).clearUserScopedState() }
    }

    private fun firebaseMessagingOrNull(context: Context): FirebaseMessaging? {
        if (!BuildConfig.GOOGLE_SERVICES_CONFIGURED) return null
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) null else FirebaseMessaging.getInstance()
        } catch (_: IllegalStateException) {
            null
        }
    }
}

internal object PushRuntime {
    @Volatile private var manager: PushLifecycleManager? = null

    fun attach(manager: PushLifecycleManager) {
        this.manager = manager
    }

    fun onNewToken(token: String) {
        manager?.onTokenRefreshed(token)
    }
}
