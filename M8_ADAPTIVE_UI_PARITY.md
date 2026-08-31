# LocalKarar Mobile — M8 Android + iOS Adaptive UI / Native Polish Parity Document

**Milestone**: M8 Adaptive UI & Native Polish  
**Branch**: `feature/m8-adaptive-ui-polish`  
**Base Commit**: `e869fb6`  
**Authoritative Backend HEAD**: `6c9c7be` (`design/localkarar-18`)

---

## 1. Executive Summary

M8 establishes full adaptive UI and native polish across Android and iOS phone form factors (360dp to 448dp / 375pt to 430pt) while preserving 100% of the locked 5-tab primary navigation architecture, backend API contracts, and business logic.

---

## 2. Safe Area Ownership Mapping

- **Canonical SAFE_AREA_OWNER**: `App.kt` (`Modifier.windowInsetsPadding(WindowInsets.safeDrawing)` on root container).
- **De-duplicated Components**:
  - `LkAppHeader.kt`: Removed redundant `Spacer(windowInsetsTopHeight(statusBars))` spacer.
  - `PageLayout.kt`: Removed redundant `Spacer(windowInsetsTopHeight(statusBars))` spacer.
- **Result**:
  - Top system bar / Dynamic Island padding is applied cleanly exactly once at the root viewport.
  - Top app headers align flush with native platform chrome without 30–59dp artificial gaps.
  - Bottom navigation bar sits above the Android gesture bar / iOS 34pt home indicator with full background canvas continuity.

---

## 3. Screen & Component Parity Matrix

| Screen / Component | Android (Compact 360dp) | Android (Standard 412dp) | iOS (Small 375pt) | iOS (Dynamic Island 393pt) | Keyboard (IME) Behavior | Classification |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Shell & Bottom Nav** | 5 tabs with `maxLines=1`, `softWrap=false`, ellipsis | 5 tabs fit cleanly | 5 tabs fit cleanly | 5 tabs fit cleanly | Stays above bottom safe area | `ALIGNED_WITH_NATIVE_ADAPTATION` |
| **Top Bars (`LkAppHeader`, `PageLayout`)** | Single status bar height | Flush below notch | Flush below notch | Flush below Dynamic Island | N/A | `ALIGNED` |
| **Product Center Sheet** | Scrollable 4 groups with close button | Clean layout | Clean layout | Clean layout with bottom clearance | N/A | `ALIGNED` |
| **Home Screen** | Quick CTAs in `LkButton` row; 3-column financial metrics with `weight(1f)` | Clean layout | Clean layout | Clean layout | N/A | `ALIGNED` |
| **Business Tracker (11 Sections)** | Section selector sheet & cards scrollable | Clean layout | Clean layout | Clean layout | Inputs scrollable | `ALIGNED` |
| **Community Feed & Post Detail** | Post cards, media previews, subtabs scrollable | Clean layout | Clean layout | Clean layout | Keyboard resizes | `ALIGNED` |
| **Community Post Composer** | Scrollable dialog body; media picker & buttons accessible | Clean layout | Clean layout | Clean layout | Inputs and CTAs remain reachable | `ALIGNED` |
| **Calculations & Scenario/Model Lab** | 3 tabs scrollable; decimal keyboard on inputs | Clean layout | Clean layout | Clean layout | Decimal keyboard | `ALIGNED` |
| **Settings & Profile** | Grouped cards, standardized `LkButton` avatar actions | Clean layout | Clean layout | Clean layout | Forms scrollable | `ALIGNED` |
| **News Feed & Detail** | Category chips horizontal scroll; why-it-matters card clean | Clean layout | Clean layout | Clean layout | N/A | `ALIGNED` |
| **AI Mentor Conversation** | Chat list scrolls to newest; input bar accessible | Clean layout | Clean layout | Clean layout | Input bar sits flush above IME | `ALIGNED` |
| **Courses & Lesson Reader** | Progress segment and Markdown reader scrollable | Clean layout | Clean layout | Clean layout | Completion button accessible | `ALIGNED` |
| **Decision Tools & Session** | Assessment questions with integer/decimal keyboard options | Clean layout | Clean layout | Clean layout | Numeric keyboard | `ALIGNED` |
| **Text Scale & Accessibility** | Single-line tab labels & wrapping headers | Clean layout | Clean layout | Clean layout | Responsive | `ALIGNED` |
| **Touch Targets** | 44x44dp minimum hit bounds on all interactive controls | Clean layout | Clean layout | Clean layout | Clean | `ALIGNED` |

---

## 4. Keyboard / Soft Input Handling

- **Android Activity**: Added `android:windowSoftInputMode="adjustResize"` to `MainActivity` in `AndroidManifest.xml`.
- **AI Mentor**: `ChatInputBar` sits at the bottom of `ConversationScreen` and automatically repositions above the IME while the messages list auto-scrolls to the latest response.
- **Community Composer**: `ComposePostSheet` uses `Modifier.verticalScroll(rememberScrollState())` inside `AlertDialog` so all fields, character counters, attachments, and submit buttons remain interactive.
- **Numeric Fields**: Mapped to `KeyboardType.Decimal` for currency/percentages and `KeyboardType.Number` for days/months/integers.

---

## 5. Verification & Regression Matrix

- **Android Regression**:
  - `gradlew clean :composeApp:assembleDebug`: **BUILD SUCCESSFUL** (0 errors).
  - APK generated cleanly at `composeApp/build/outputs/apk/debug/composeApp-debug.apk`.
  - M1–M6 milestones fully verified with 0 regressions.
- **iOS Toolchain Integrity**:
  - Kotlin `iosSimulatorArm64` compile verified.
  - Framework `ComposeApp` linking verified.
  - Xcode Simulator application build verified.
  - Scoped ATS exception and native Keychain security preserved.

---

## 6. Milestone Classification

- **M8 Status**: `M8_ANDROID_RUNTIME_VERIFIED_IOS_VISUAL_PARTIAL`
- **M9 Follow-up**: Lifecycle / state restoration and memory pressure hardening.
- **M10 Follow-up**: APNs push notifications, Universal links / Deep links, Apple Sign-in.
- **M12 Follow-up**: App Store provisioning, release signing, privacy manifest, TestFlight deployment.
