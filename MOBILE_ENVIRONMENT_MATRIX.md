# Mobile Environment Matrix (M2)

This document specifies the network and environment resolution strategy for the LocalKarar Compose Multiplatform client across Android and iOS platforms.

---

## 1. Environment Configurations

| Environment State | Platform / Target | Base API URL | Cleartext Permitted | Resolution Mechanism |
| :--- | :--- | :--- | :--- | :--- |
| **`DEBUG_ANDROID`** | Android Emulator | `http://10.0.2.2:3000` | YES (scoped to `10.0.2.2`) | `BuildConfig.IS_RELEASE == false` |
| **`DEBUG_IOS`** | iOS Simulator / Local Dev | `http://localhost:3000` | YES (local debug binary) | `Platform.isDebugBinary == true` |
| **`PRODUCTION`** | Android Release APK / AAB | `https://localkarar.com` | NO (Enforced by OS Network Security) | `BuildConfig.IS_RELEASE == true` |
| **`PRODUCTION`** | iOS App Store / Release | `https://localkarar.com` | NO (Enforced by ATS) | `Platform.isDebugBinary == false` |

---

## 2. Platform Implementation Details

### Android
- **File:** `AppEnvironmentProvider.android.kt`
- **Mechanism:** Inspects `com.localkarar.app.BuildConfig.IS_RELEASE`.
- **Security:** `network_security_config.xml` permits cleartext HTTP strictly for domain `10.0.2.2` during debug builds. Release builds strictly enforce HTTPS to `localkarar.com`.

### iOS
- **File:** `AppEnvironmentProvider.ios.kt`
- **Mechanism:** Inspects `kotlin.native.Platform.isDebugBinary`.
- **Bundle Identifier:** `com.localkarar.app` (configured in `iosApp/Configuration/Config.xcconfig`).
- **Product Name:** `LocalKarar` (configured in `iosApp/Configuration/Config.xcconfig`).

---

## 3. Host Mapping Reference

- **Host Machine (Local Backend):** `http://localhost:3000`
- **Android Emulator Loopback:** `http://10.0.2.2:3000`
- **iOS Simulator Loopback:** `http://localhost:3000`
- **Physical Device over LAN (Optional):** Replace baseUrl with development machine's local IP (e.g. `http://192.168.x.x:3000`).
- **Production Server:** `https://localkarar.com`
