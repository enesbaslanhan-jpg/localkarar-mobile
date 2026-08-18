# LocalKarar Mobile

This is the Kotlin Multiplatform (KMP) foundation project for the LocalKarar mobile client, targeting Android and iOS.

## Purpose

This mobile application acts as the dedicated client for the existing LocalKarar Web product. It is NOT a standalone service. Both Web and Mobile interfaces share the same backend, API, user accounts, and business data.

## Technology Stack

- **Framework:** Kotlin Multiplatform
- **UI Toolkit:** Compose Multiplatform
- **Language:** Kotlin
- **Build System:** Gradle Kotlin DSL

## Project Structure

The project is structured following modern Compose Multiplatform guidelines:

- `composeApp/src/commonMain` - Contains all shared code (UI, navigation, domain, models) that executes on both Android and iOS.
- `composeApp/src/androidMain` - Contains Android-specific integrations (e.g., system permissions, biometrics).
- `composeApp/src/iosMain` - Contains iOS-specific integrations.
- `iosApp/` - An Xcode project that embeds and executes the shared Compose framework on iOS.

## Environment Configuration Concept

Environment URLs and basic configurations are defined in `composeApp/src/commonMain/kotlin/com/localkarar/app/core/Config.kt`.

**Security Rule:** No API keys (e.g., OpenAI, Gemini, DB credentials) or secrets are stored in this mobile application. They will remain securely managed on the LocalKarar Backend.

## How to Run

### Android
1. Open the project in Android Studio.
2. Select the `composeApp` run configuration.
3. Select an Android device/emulator.
4. Click Run (Shift+F10) or execute `./gradlew :composeApp:installDebug`.

### iOS
1. Ensure Xcode and CocoaPods (if later required) are installed.
2. Open `iosApp/iosApp.xcodeproj` in Xcode.
3. Select an iOS Simulator.
4. Click Run (Cmd+R). Note that the first build takes time as Gradle compiles the Kotlin `ComposeApp.framework`.

## Next Steps
This project currently provides the foundation. The next major phase is **Task 2 — LocalKarar Web/App Contract & Mobile Design Integration Preparation**.