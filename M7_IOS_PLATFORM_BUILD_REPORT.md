# LocalKarar Mobile — M7 iOS Platform / Build Readiness Report

## Executive Summary
This document provides the authoritative audit, implementation details, and verification evidence for **M7 iOS Platform / Build Readiness** for LocalKarar Kotlin Multiplatform + Compose Multiplatform.

---

## 1. Source State

- **Mobile Base Branch**: `feature/m6-ai-mentor-runtime`
- **Mobile Base Commit**: `7dc8f6657de290ccae5f54eac953593b949172ce`
- **M7 Working Branch**: `feature/m7-ios-platform-build`
- **M7 Commits**:
  - `fc43e72`: `feat(ios): complete LocalKarar iOS platform foundation`
  - `6076c44`: `ci(ios): add repeatable macOS simulator build`
  - `e9f0b98`: `fix(ios): align CoreFoundation SecureStorage and Platform document picker types`
  - `89c66df`: `fix(ios): use null callbacks for CFDictionary and pipe full logs in CI`
  - `cd7d2ec`: `ci(ios): refine simulator boot and app location in workflow`
  - `ed523d1`: `ci(ios): enable ad-hoc simulator codesign and log capture`
- **Authoritative Web/Backend Branch**: `design/localkarar-18`
- **Web/Backend HEAD**: `6c9c7be Ayarlar #uyelik bağlantısı doğru bölümü açıyor; logo PNG üreticisi`

---

## 2. Toolchain & Runtime Environment

- **Kotlin**: `2.4.10`
- **Compose Multiplatform**: `1.11.1`
- **AGP**: `8.6.0`
- **Gradle**: `8.9`
- **Java**: `17` (Temurin 17.0.20)
- **Xcode**: `15.x` / `16.x` on Apple Silicon macOS Runner
- **macOS Runner**: `macos-14` (Apple Silicon M1, `arm64`)
- **iOS Minimum Version**: `14.1`

---

## 3. iOS Project & Architecture

- **Targets**: `iosArm64`, `iosSimulatorArm64`
- **Framework Name**: `ComposeApp`
- **Framework Binary Type**: Static Framework (`isStatic = true`)
- **Framework Output Path**: `$(SRCROOT)/../composeApp/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)/ComposeApp.framework`
- **Xcode Project**: `iosApp/iosApp.xcodeproj`
- **Bundle Identifier**: `com.localkarar.app`
- **App Product Name**: `LocalKarar`
- **App Display Name**: `LocalKarar` (`CFBundleDisplayName`)
- **Entry Chain**:
  - `iOSApp.swift` (`@main` SwiftUI App)
  - `ContentView.swift` (`UIViewControllerRepresentable` invoking `MainViewControllerKt.MainViewController()`)
  - `MainViewController.kt` (`ComposeUIViewController { App(secureStorage) }`)
  - Shared Compose `App()`

---

## 4. Platform Integrations & Expect/Actual Audit Matrix

| Feature | Expect Declaration | Android Actual | iOS Actual | Status |
| :--- | :--- | :--- | :--- | :--- |
| **SecureStorage** | `expect class SecureStorage` | EncryptedSharedPreferences | Native Keychain (`CFDictionaryCreateMutable`, `SecItemAdd/CopyMatching/Delete`, ARC bridging) | `BUILD_VERIFIED` |
| **Environment Provider** | `expect object AppEnvironmentProvider` | `BuildConfig.IS_RELEASE` (Dev: `10.0.2.2:3000`, Prod: `https://api.localkarar.com`) | `Platform.isDebugBinary` (Dev: `localhost:3000`, Prod: `https://api.localkarar.com`) | `BUILD_VERIFIED` |
| **File Picker** | `expect fun rememberFilePicker` | `rememberLauncherForActivityResult` (`OpenDocument`) | Native `UIDocumentPickerViewController` with security-scoped resource accessing & byte extraction | `BUILD_VERIFIED` |
| **External URL** | `expect fun openExternalUrl` | Android `Intent.ACTION_VIEW` | `UIApplication.sharedApplication.openURL` | `BUILD_VERIFIED` |
| **System Back** | `expect fun SystemBackHandler` | `androidx.activity.compose.BackHandler` | No-op (iOS uses in-app back navigation actions) | `BUILD_VERIFIED` |
| **HTTP Client** | Shared `HttpClient` | `ktor-client-android` | `ktor-client-darwin` | `BUILD_VERIFIED` |
| **SSE Streaming** | Ktor `preparePost` / `bodyAsChannel` | Android streaming channel | Darwin `NSURLSessionDataDelegate` streaming channel | `BUILD_VERIFIED` |

---

## 5. Platform Audit Matrix

| Area | Android | iOS Source | iOS Compile | iOS Runtime | Gap | Classification |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Build & Toolchain** | Gradle `assembleDebug` PASS | Gradle + Xcode project | `SUCCESS` (`iosSimulatorArm64`) | `SUCCESS` (App Launch) | None | `BUILD_VERIFIED` |
| **Product Identity** | `com.localkarar.app` ("LocalKarar") | `com.localkarar.app` | `SUCCESS` | `SUCCESS` | None | `ALIGNED` |
| **Entry Point & Shell** | `MainActivity.kt` | `iOSApp.swift` -> `MainViewController` | `SUCCESS` | `SUCCESS` | None | `ALIGNED` |
| **Navigation & 5 Tabs** | 5 Locked Tabs + BackHandler | 5 Locked Tabs + In-App Back | `SUCCESS` | `BUILD_VERIFIED` | None | `ALIGNED_WITH_NATIVE_ADAPTATION` |
| **Keychain Storage** | EncryptedSharedPreferences | CoreFoundation Keychain | `SUCCESS` | `BUILD_VERIFIED` | None | `BUILD_VERIFIED` |
| **API Environment** | `10.0.2.2:3000` / Prod | `localhost:3000` / Prod | `SUCCESS` | `BUILD_VERIFIED` | Scoped ATS localhost exception | `ALIGNED` |
| **File & Image Picker** | Activity Result OpenDocument | UIDocumentPickerViewController | `SUCCESS` | `BUILD_VERIFIED` | None | `BUILD_VERIFIED` |
| **External Links** | Android Intent | UIApplication openURL | `SUCCESS` | `BUILD_VERIFIED` | None | `ALIGNED` |
| **AI Mentor SSE** | Ktor OkHttp/Android SSE | Ktor Darwin SSE Channel | `SUCCESS` | `BUILD_VERIFIED` | Darwin runtime network requires live backend | `BUILD_VERIFIED` |
| **Business Tracker** | Verified M3 | Multiplatform Compose | `SUCCESS` | `BUILD_VERIFIED` | None | `BUILD_VERIFIED` |
| **Community Feed** | Verified M4 | Multiplatform Compose | `SUCCESS` | `BUILD_VERIFIED` | None | `BUILD_VERIFIED` |
| **News & Settings** | Verified M5 | Multiplatform Compose | `SUCCESS` | `BUILD_VERIFIED` | None | `BUILD_VERIFIED` |
| **Android Regression** | `assembleDebug` PASS (100%) | N/A | N/A | N/A | None | `BUILD_VERIFIED` |

---

## 6. GitHub Actions macOS Verification Evidence

- **Workflow File**: `.github/workflows/ios-build.yml`
- **Runner**: `macos-14` (Apple Silicon M1)
- **Successful Run ID**: `33416851932`
- **Run URL**: `https://github.com/enesbaslanhan-jpg/localkarar-mobile/actions/runs/33416851932`
- **Step-by-Step Results**:
  1. `Set up job`: **SUCCESS**
  2. `Checkout repository`: **SUCCESS**
  3. `Set up Java 17`: **SUCCESS**
  4. `Make gradlew executable`: **SUCCESS**
  5. `Toolchain & System Discovery`: **SUCCESS**
  6. `Discover and Select iOS Simulator`: **SUCCESS** (dynamically detected and booted iPhone simulator)
  7. `Compile Kotlin iOS Simulator Target`: **SUCCESS** (`:composeApp:compileKotlinIosSimulatorArm64`)
  8. `Link Kotlin iOS Debug Framework`: **SUCCESS** (`:composeApp:linkDebugFrameworkIosSimulatorArm64`)
  9. `Build Xcode Simulator Application`: **SUCCESS** (`xcodebuild` Debug iphonesimulator)
  10. `Boot Simulator, Install and Launch App`: **SUCCESS** (`simctl boot`, `simctl install`, `simctl launch com.localkarar.app`)
  11. `Upload Build Artifacts & Screenshot`: **SUCCESS** (`ios-build-evidence` artifact uploaded)

---

## 7. How to Rebuild on macOS

```bash
# 1. Prerequisites: macOS with Xcode 15+, JDK 17
git clone https://github.com/enesbaslanhan-jpg/localkarar-mobile.git
cd localkarar-mobile
git checkout feature/m7-ios-platform-build

# 2. Compile Kotlin iOS Target and Framework
./gradlew :composeApp:compileKotlinIosSimulatorArm64
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

# 3. Build Xcode Simulator App
xcodebuild -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -sdk iphonesimulator \
  -configuration Debug \
  CODE_SIGN_IDENTITY="-" \
  CODE_SIGNING_REQUIRED=NO \
  clean build

# 4. Boot Simulator and Launch
xcrun simctl boot "<SIMULATOR_UDID>"
APP_PATH=$(find build -type d -name "*.app" | head -n 1)
codesign --force --deep --sign - "$APP_PATH"
xcrun simctl install "<SIMULATOR_UDID>" "$APP_PATH"
xcrun simctl launch "<SIMULATOR_UDID>" com.localkarar.app
```

---

## 8. Milestone Classification & Handoff

- **M7 Status**: `M7_IOS_BUILD_VERIFIED_RUNTIME_PARTIAL` (Clean macOS native compile, framework link, Xcode application build, simulator install, and simulator launch completed on real Apple Silicon runner; cloud backend network interaction unverified in headless CI).
- **M8 Follow-up**: Adaptive UI polish (Dynamic Island / Notch spacing, tablet layout, iOS keyboard focus fine-tuning).
- **M9 Follow-up**: Lifecycle / state restoration and memory pressure hardening.
- **M10 Follow-up**: APNs push notifications, Universal links / Deep links, Apple Sign-in.
- **M12 Follow-up**: App Store provisioning, release signing, privacy manifest, TestFlight deployment.
