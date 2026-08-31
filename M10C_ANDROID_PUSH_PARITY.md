# LocalKarar — M10C Android FCM / Notification Lifecycle Parity

## Final status

`FUNCTIONAL_REAL_FCM_CONFIG_PENDING`

The Android implementation, config-absent fallback, unit/framework tests, install, launch, App Link routing, and native push Intent routing are functional. End-to-end provider delivery is intentionally **not** claimed because this repository does not contain a real `composeApp/google-services.json`, and no Firebase project/device token/provider delivery receipt was available.

## Verified source state

| Item | Verified value |
| --- | --- |
| Mobile repository | `https://github.com/enesbaslanhan-jpg/localkarar-mobile` |
| M10A branch | `origin/feature/m10a-deep-links` |
| Actual M10A commit | `ae79633be3a1829c7c259c2f71d3dab9a47fc43c` |
| Brief-supplied M10A commit | `ae796339178ad3ecadabf8696ab3f6a27bb4510b` — not present on origin; corrected to the actual branch head above |
| M10B backend branch | `origin/feature/m10b-push-backend` |
| M10B backend commit | `f1f84a4fd95dc6b1ed0d1ad018e272da90f26f08` |
| Authoritative backend | `origin/design/localkarar-18` at `55561d58cca4d2dd9259e1f10b713e87cfd9a04a` |
| M10C branch | `feature/m10c-android-fcm` |

## Implemented contract

- `PushTarget` and the pure `PushPayloadParser` live in `commonMain`. The parser accepts the locked backend `Map<String, String>` schema and maps through `PushTarget` to the existing M10A `DeepLinkTarget` layer.
- Supported targets are `community_post`, `community_thread`, `workspace_record`, and `account`. Native-only targets use explicit IDs; account alerts route to `Destination.Settings`.
- `DeviceRegistrationRequest` contains only `pushToken`, `platform`, `appVersion`, and `locale`. Client-controlled `userId` is absent; backend ownership remains JWT-derived.
- Android installation identity is a random UUID persisted in `SharedPreferences` for the app installation lifetime.
- Registration dedup uses a SHA-256 fingerprint of `userId + installationId + pushToken`. User-scoped fingerprint/active-user state is cleared on explicit logout, unrecoverable session expiry, and account switch. The device token and installation ID remain device-scoped.
- No singleton stores a raw JWT. Registration and deletion use the existing authenticated Ktor client and `SecureStorage` bearer flow.
- Explicit logout attempts bounded best-effort `DELETE /devices/:installationId` before token removal, then bounded server logout, and always completes local logout. Session expiry performs local push-session cleanup without relying on a now-invalid JWT.
- `FirebaseMessagingService` is a platform bridge: token callbacks feed the lifecycle manager; data messages are parsed by the common parser and displayed through the appropriate Android notification channel.
- Notification channels are `lk_messages`, `lk_business`, and `lk_account`.
- Android 13+ notification permission is requested once, after the authenticated shell has stabilized. The prompt-attempt flag is persisted so denial/recomposition does not create repeated prompts.
- Foreground/data-message notifications use an explicit immutable `PendingIntent`. Background FCM notification taps, cold starts, and warm starts are read by `MainActivity`; internal native-only fields use app-namespaced extras. All valid targets enter `PendingDeepLinkStore`, preserving M10A auth gating.
- Firebase uses the official Android BoM main module. The Google Services plugin is applied only when the real file exists at `composeApp/google-services.json`.
- Config-absent builds set `BuildConfig.GOOGLE_SERVICES_CONFIGURED=false`. Runtime code checks both that flag and `FirebaseApp.getApps(context)` before any `FirebaseMessaging.getInstance()` call.
- The iOS actual implementation is a no-op for this Android milestone; no M10D/APNs work was started.

## Verification evidence

| Check | Result |
| --- | --- |
| Config file location scan | No `google-services.json` present; expected location confirmed as `composeApp/google-services.json` |
| Generated config-less BuildConfig | Debug and release generated `GOOGLE_SERVICES_CONFIGURED = false`; `VERSION_NAME = "1.0"` |
| Common/JVM tests | 17 tests passed in debug and 17 in release; includes 7 push parser tests and request-body ownership test |
| Android debug/release Kotlin compile | Passed |
| iOS simulator Kotlin compile | Passed on Windows host |
| iOS link/Xcode build | Not executable on Windows; Gradle link task was host-skipped. `ios-build.yml` now includes the M10C branch for macOS compile/link/Xcode regression |
| Clean Android build | `clean :composeApp:assembleDebug :composeApp:compileDebugAndroidTestKotlin` passed |
| Android framework tests | 2/2 passed on Pixel_8 Android 15 AVD using `AndroidJUnitRunner` |
| APK install | Streamed install succeeded on `emulator-5554` |
| App launch | Cold launch succeeded (`Status: ok`) with no `FATAL EXCEPTION` |
| M10A App Link regression | `https://localkarar.com/app/community/gonderi/post_1001` resolved to `MainActivity`, cold launch `Status: ok` |
| Terminated native target regression | Explicit namespaced `community_thread/thr_505` extras resolved to `MainActivity`, cold launch `Status: ok` |
| Notification channels | All three channel IDs observed in Android `dumpsys notification` |
| Config-less Firebase behavior | `FirebaseInitProvider` reported initialization unsuccessful as expected; app remained running and no `FirebaseMessaging.getInstance()` path was entered |
| Real FCM provider delivery | **Not tested / not claimed** — real Android Firebase config and provider credentials are absent |

## Remaining gate for COMPLETE_END_TO_END_VERIFIED

Provide the real Firebase Android app config at `composeApp/google-services.json`, use a Firebase-capable signed build/device, authenticate a real user, verify `PUT /devices/:installationId`, deliver each locked payload through the authoritative backend/FCM HTTP v1 chain, validate foreground/background/terminated tap routing, and capture provider/backend/device evidence. Until that chain is proven, the truthful status remains `FUNCTIONAL_REAL_FCM_CONFIG_PENDING`.

