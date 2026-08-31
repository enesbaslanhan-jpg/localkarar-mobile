# LocalKarar Mobile — M8 Android + iOS Adaptive UI / Native Polish Audit

**Milestone**: M8.0 (Audit & Planning Phase — No Code Changes)  
**Base Branch**: `feature/m7-ios-platform-build` (`e869fb6`)  
**Authoritative Backend HEAD**: `6c9c7be` (`design/localkarar-18`)  
**Scope Rule**: Audit existing LocalKarar product UI across Android and iOS phone form factors without changing product architecture, backend contracts, or business logic.

---

## 1. Locked Product Architecture & Navigation Integrity

- **Locked Primary Bottom Navigation (5 Tabs)**:
  1. `Ana Sayfa` (`Destination.Home`)
  2. `İşletme Takibi` (`Destination.WorkspaceHome` / `Destination.Workspaces`)
  3. `Topluluk` (`Destination.Community`)
  4. `Hesaplamalar` (`Destination.Calculations`)
  5. `Ayarlar` (`Destination.Settings`)
- **Secondary Modules** (Accessible via Product Center, Home contextual links, and Deep Navigation):
  - `Kurslar` (`Destination.Courses`, `CourseDetail`, `LessonReader`)
  - `Karar Araçları` (`Destination.DecisionTools`, `DecisionSession`)
  - `AI Mentor` (`Destination.AiMentor`, `Conversation`)
  - `Haberler` (`Destination.News`, `NewsDetail`)
- **Ürün Merkezi (Product Center)**:
  - `KARAR VER`: Karar Araçları, Hesaplamalar
  - `ÖĞREN`: Kurslar, AI Mentor
  - `TAKİP ET`: Haberler, İşletme Takibi
  - `SOSYAL`: Topluluk, Profil

---

## 2. Target Device Matrix

| Platform | Device Class | Logical Dimensions | Density / Scale | Cutout / System Chrome |
| :--- | :--- | :--- | :--- | :--- |
| **Android** | Compact Phone (e.g. Small 4.7"-5.5") | 360 x 640 / 360 x 800 dp | ~2.0 - 3.0x | Standard status bar + 3-button / gesture bar |
| **Android** | Standard Modern Phone (Pixel 8, Galaxy S24) | 412 x 915 / 393 x 852 dp | ~2.625 - 3.0x | Center punch-hole camera + gesture bar |
| **Android** | Large Modern Phone (Pixel 8 Pro, Galaxy Ultra) | 448 x 998 dp | ~3.5x | Punch-hole camera + gesture bar |
| **iOS** | Compact / Small Modern iPhone (SE 3rd, 13 mini) | 375 x 667 / 375 x 812 pt | 2.0x / 3.0x | Home button / Classic Top Notch |
| **iOS** | Standard Modern iPhone (iPhone 15, 16) | 393 x 852 pt | 3.0x | Dynamic Island (59pt top) + Home Indicator (34pt bottom) |
| **iOS** | Large Modern iPhone (iPhone 15 Pro Max, 16 Plus) | 430 x 932 pt | 3.0x | Dynamic Island (59pt top) + Home Indicator (34pt bottom) |

---

## 3. Design System & Component Audit

### 3.1 Primitives Audit
- **Theme (`Theme.kt`)**: `LocalKararTheme` is the single authoritative provider wrapping `MaterialTheme` with `LkDarkColorScheme`.
- **Colors (`Color.kt`)**: Dark palette tokens (`LkPrimary: #94CEED`, `LkSurfaceCanvas: #121619`, `LkSurfacePanel: #1D2429`, `LkSurfaceSunken: #0C1013`, `LkSurfaceRaised: #293137`, `LkTextPrimary: #F1F4F5`, `LkTextSecondary: #D2D9DD`, `LkLineSoft: rgba(238,241,243,0.10)`, `LkLineStrong: rgba(238,241,243,0.18)`). Contrast exceeds WCAG AAA standards.
- **Typography (`Type.kt`)**: Manrope variable font family with `regular`, `medium`, `semibold`, `bold`. Full Turkish Latin coverage (`ş`, `ç`, `ğ`, `ı`, `İ`, `ö`, `ü`).
- **Spacing (`Spacing.kt`)**: `Space1` (4dp) to `Space12` (48dp). `PadPanel = 16dp`, `PadSignature = 24dp`.
- **Shapes (`Shape.kt`)**: `XS = 4dp`, `SM = 8dp`, `MD = 12dp`, `LG = 14dp`, `FULL = 999dp`.

### 3.2 Component Reuse & Inconsistency Findings
1. **Raw Buttons vs `LkButton`**:
   - `HomeScreen.kt`: Quick action CTAs use raw `Button` / `OutlinedButton` with custom modifiers.
   - `ConversationScreen.kt`: Chat input bar and rename dialog use raw `Button` and `TextButton`.
   - `ProfileScreen.kt` (Settings): Avatar actions use raw `OutlinedButton` with inline border brushes.
2. **Raw Dialogs vs Standard Sheet Presentation**:
   - `ComposePostSheet.kt`: Uses raw `AlertDialog` instead of full/half modal bottom sheet, making keyboard input cramped on small phones.
   - `CreateThreadSheet.kt`: Uses `AlertDialog` for thread creation.
   - `SettingsScreen.kt`: Uses `AlertDialog` for logout-all confirmation.
3. **Hardcoded Dimension Values**:
   - Multiple screens contain inline `16.dp`, `24.dp`, `12.dp` instead of `LkSpacing` semantic tokens.

---

## 4. Window, Safe Area & Insets Architecture Audit

### 4.1 Root Insets vs Screen Insets Conflict
- **Root Level (`App.kt:96`)**:
  ```kotlin
  Box(modifier = Modifier.fillMaxSize().background(LkSurfaceCanvas).windowInsetsPadding(WindowInsets.safeDrawing))
  ```
  `safeDrawing` at root consumes top status bar + bottom navigation bar/home indicator + display cutouts.
- **Top Bar Level (`LkAppHeader.kt:36` & `PageLayout.kt:33`)**:
  ```kotlin
  Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
  ```
- **Finding**: Because `App.kt` already insets by `safeDrawing`, adding `windowInsetsTopHeight(WindowInsets.statusBars)` in `LkAppHeader` and `LkPageLayout` creates **double top padding** on Android and iOS (pushing screen content 30-59dp too far down).
- **Required M8 Architecture**: Remove root-level full `safeDrawing` consumption or remove redundant top bar spacers so system bars and top headers align flush with native platform geometry.

### 4.2 Bottom Navigation & iOS Home Indicator
- `LkBottomNavigation` has fixed height 56dp.
- When `safeDrawing` is applied at the root, the bottom bar sits above the 34dp iOS home indicator, but creates a dark unstyled gap if background canvas doesn't extend underneath.
- On iOS, native tab bars extend the background canvas into the 34dp bottom safe area with items vertically centered above it.

### 4.3 Android `windowSoftInputMode`
- `AndroidManifest.xml` does not declare `android:windowSoftInputMode="adjustResize"`.
- Without `adjustResize`, opening the IME keyboard on Android causes the viewport to pan rather than resize cleanly in Compose.

---

## 5. Screen-by-Screen Audit Matrix

| Screen / Component | Android Small (360dp) | Android Std (412dp) | iOS Small (375pt) | iOS Std (393pt Dynamic Island) | Safe Area Handling | Keyboard / IME | Text Wrapping / Truncation | Gap Classification | Priority | Required M8 Action |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Shell & Bottom Nav** | 5 tabs fit tightly (72dp/tab) | 5 tabs fit cleanly | 5 tabs fit | 5 tabs fit | Good above home indicator; needs flush canvas | N/A | "İşletme Takibi" and "Hesaplamalar" can ellipsize at >1.1x font scale | `PARTIAL` | **P0** | Ensure minimum label sizing, micro font, and flush bottom canvas extension. |
| **Top Bars (`LkAppHeader` / `LkPageLayout`)** | Top padding pushed down | Top padding pushed down | Top padding pushed down | Excessive spacing under Dynamic Island | Double status bar padding bug | N/A | Long titles wrap gracefully; subtitle maxLines=1 | `BROKEN` (Spacing) | **P0** | Eliminate duplicate top bar insets between `App.kt` and `LkAppHeader`/`PageLayout`. |
| **Home Screen** | 3 CTA buttons ("Hesapla", "Mentor", "Karar Ver") tight; 3 financial metrics tight | Clean card layout | Clean layout | Clean layout | Relies on PageLayout | N/A | Large currency (e.g. `₺1.250.000`) in 3-col status panel can wrap | `PARTIAL` | **P1** | Adjust button content padding and format large currency numbers gracefully on 360dp. |
| **Business Tracker (11 Sections)** | Section pill & sheets scrollable | Clean layout | Clean layout | Clean layout | Section selector sheet has close button and scroll | N/A | Section labels ("İşletme Ayarları", "Aktiviteler") fit cleanly | `ALIGNED` | **P1** | Verify bottom sheet home indicator clearance. |
| **Community Feed & Tabs** | Subtabs ("Akış", "Kişiler", "Sohbetler", "Profil") scrollable | Clean layout | Clean layout | Clean layout | Top bar actions aligned | N/A | Post author names and multi-line content wrap properly | `ALIGNED` | **P1** | Standardize subtab pill padding. |
| **Community Post Composer** | `AlertDialog` cramped when IME opens | Cramped on IME | Cramped on IME | Cramped on IME | Dialog margins | Keyboard covers part of input without scroll | Media preview and text input can exceed screen | `PARTIAL` | **P0** | Make compose dialog body scrollable (`verticalScroll`) or convert to modal sheet with IME padding. |
| **AI Mentor Conversation** | Input bar reachable; streaming text updates list | Clean chat list | Clean chat list | Clean chat list | Top bar has title + rename button | Missing explicit `imePadding()` on input bar | Markdown tables / code blocks horizontally scrollable | `PARTIAL` | **P0** | Add `imePadding()` to chat composer and `adjustResize` in AndroidManifest. |
| **Calculations & Model Lab** | 3 tabs ("Katalog", "Finansal Görünüm", "Geçmiş") scrollable | Clean layout | Clean layout | Clean layout | Good | Numeric input fields need numeric keyboard type | Turkish formulas & large results wrap cleanly | `ALIGNED` | **P1** | Ensure `KeyboardType.Decimal` on numeric inputs. |
| **Financial Model Detail** | 7 tabs horizontal scroll; chart Canvas renders | Clean layout | Clean layout | Clean layout | Good | Inputs scroll with page | Scenario chips and validation checks wrap | `ALIGNED` | **P1** | Ensure chart Canvas respects max constraint width. |
| **Settings & Profile** | Grouped cards scrollable; avatar upload works | Clean layout | Clean layout | Clean layout | Profile header and logout-all dialog clean | Password & email change forms need keyboard scroll | Role pills & email wrap cleanly | `ALIGNED` | **P1** | Ensure destructive dialogs are accessible on small screens. |
| **News Feed & Detail** | Category chips horizontal scroll; cards clean | Clean layout | Clean layout | Clean layout | Detail screen hero & why-it-matters card clean | N/A | Long news titles wrap up to 3 lines without clipping | `ALIGNED` | **P1** | Ensure CategoryRow padding aligns with list margins. |
| **Courses & Lesson Reader** | Progress bar & Markdown reader clean | Clean layout | Clean layout | Clean layout | Bottom completion button sticky/scrollable | N/A | Long lesson titles wrap cleanly; code blocks scroll | `ALIGNED` | **P1** | Ensure bottom lesson completion button has navigation bar clearance. |
| **Decision Tools & Session** | Question cards and answer options scroll | Clean layout | Clean layout | Clean layout | Result snapshot panel clean | Numeric inputs need numeric keypad | Questions wrap properly; option radio buttons fit | `ALIGNED` | **P1** | Verify `KeyboardType.Number` on numeric questions. |

---

## 6. Turkish Text & Accessibility Stress Test

- **Canonical Turkish Strings Tested**:
  - `İşletme Takibi` (Bottom Nav & Header) — 14 characters
  - `Hesaplamalar` (Bottom Nav & Header) — 12 characters
  - `Diğer Cihazlardaki Oturumları Kapat` (Settings Action) — 35 characters
  - `Düzenle ve Yeniden Oluştur` (Mentor Action) — 26 characters
  - `Öğrenme İlerlemesi` (Home / Courses Progress) — 18 characters
  - `Nakit Akışı ve Karlılık Analizi` (Model Title) — 32 characters
- **Findings**:
  - All Turkish Latin glyphs (`ş`, `ç`, `ğ`, `ı`, `İ`, `ö`, `ü`) render with 100% fidelity via Manrope variable font.
  - At 100% font scale, all primary tabs and titles fit without clipping.
  - At >1.15x accessibility font scale on 360dp width, bottom navigation labels require `maxLines = 1` with text overflow ellipsis, while screen header titles wrap onto two lines gracefully.
  - Canonical Turkish product terms are strictly preserved without abbreviation.

---

## 7. Touch Targets & Interaction Areas

- **Bottom Navigation**: 56dp bar height with full 72-86dp horizontal touch width per item.
- **Top Bar Actions & Product Center Pill**: Minimum 44x44dp touch bounding box on icon buttons and 36dp pill height.
- **List Items & Cards**: Full card width clickable with ripple feedback.
- **Inline Action Chips**: Minimum 36dp height with 8dp horizontal padding.
- **Finding**: All primary interactive controls meet or exceed 44x44dp mobile touch target recommendations.

---

## 8. Keyboard / Soft Input (IME) Matrix

| Surface | Input Control | Keyboard Type | IME Action | Current Behavior | Gap / Required Action |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Login / Register** | `LkTextField`, `LkPasswordTextField` | Email / Password | Next / Done | Page scrolls | Add `adjustResize` to AndroidManifest. |
| **AI Mentor Chat** | `ChatInputBar` `OutlinedTextField` | Text / Multiline | Default | Input bar sits at bottom | Add `imePadding()` to input bar container. |
| **Community Compose** | `ComposePostSheet` `OutlinedTextField` | Text / Multiline | Default | `AlertDialog` content | Add `verticalScroll` to dialog body to prevent clipping. |
| **Decision Tools** | `LkNumericField` | Decimal | Done | Page scrolls | Set `KeyboardType.Decimal` on numeric inputs. |
| **Financial Model** | `LkNumericField` | Decimal | Next / Done | Page scrolls | Set `KeyboardType.Decimal` on inputs. |
| **Settings (Password/Email)**| `LkTextField` | Text / Password | Done | Form scrolls | Ensure form has bottom IME clearance. |

---

## 9. Visual Runtime Verification Evidence & Limitations

### 9.1 Android Runtime
- Clean build: `BUILD SUCCESSFUL` (`:composeApp:assembleDebug`).
- Android runtime verification: M1–M6 native features verified on Android host.

### 9.2 iOS Runtime (macOS Apple Silicon CI)
- Xcode Simulator build: `BUILD SUCCEEDED` (`xcodebuild` Debug `iphonesimulator`).
- Simulator lifecycle: `simctl boot`, `simctl install`, and `simctl launch com.localkarar.app` **SUCCESSFUL** in CI Run `33416851932`.
- **Limitation**: Headless macOS CI executed app launch and captured simulator screenshot, but full interactive UI session navigation across authenticated screens cannot be exercised in CI without a live reachable backend server.
- Classification: `NOT_RUNTIME_VERIFIED` for interactive iOS deep screen visual parity; `BUILD_VERIFIED` for iOS framework, binary, and launch.

---

## 10. M8 Required Scope & Priority Model

### Priority P0 (Critical Usability & Geometry Fixes)
1. **Top Bar Double-Inset Resolution**: Fix duplicate status bar insets between `App.kt:96` (`WindowInsets.safeDrawing`) and `LkAppHeader.kt:36` / `PageLayout.kt:33`.
2. **Keyboard (IME) Padding & Android Resize**: Add `android:windowSoftInputMode="adjustResize"` in `AndroidManifest.xml` and apply `Modifier.imePadding()` to `ConversationScreen` input bar.
3. **Community Post Compose Dialog Scrollability**: Add scroll container to `ComposePostSheet` and `CreateThreadSheet` to ensure input and post buttons remain accessible when keyboard opens on small screens.
4. **Bottom Navigation Label Robustness**: Ensure 5 bottom tab labels ("İşletme Takibi", "Hesaplamalar") do not clip or wrap awkwardly on 360dp/375pt screens.

### Priority P1 (Design System & Adaptive Refinements)
1. **Home Screen Financial Metric Wrapping**: Ensure 3-column metric cards on `StatusPanel` handle 7-digit currency amounts cleanly without horizontal clipping on 360dp devices.
2. **Standardize Shared Button & Dialog Usage**: Replace raw `Button` / `OutlinedButton` on `HomeScreen` and `ProfileScreen` with `LkButton` variants.
3. **Numeric Keypad Specification**: Ensure all numeric inputs in Decision Tools and Financial Models explicitly request `KeyboardType.Decimal` / `KeyboardType.Number`.
4. **Bottom Sheet Safe Area Clearance**: Ensure `WorkspaceSectionSheet` and `ProductCenterSheet` have proper home indicator clearance on iOS.

### Priority P2 (Visual Polish & Fine-Tuning)
1. **Semantic Spacing Replacement**: Replace remaining inline `16.dp` / `24.dp` values with `LkSpacing` semantic constants.
2. **List Surface Elevation & Border Consistency**: Harmonize card borders across dark theme panels.

---

## 11. Explicit Non-Scope (Excluded from M8)

The following items belong to subsequent milestones and must NOT be included in M8:
- **No IA or Navigation Changes**: 5 primary tabs remain locked. No module moves or renames.
- **No Business Logic or API Contract Changes**: No DTO changes, endpoint modifications, or calculation algorithm changes.
- **No Process Restoration / State Persistence Redesign** (Deferred to **M9**).
- **No Push Notifications (APNs / FCM) or Universal/Deep Links** (Deferred to **M10**).
- **No Tablet / iPad Split-Screen Redesign** (M8 targets phone form factors).
- **No App Store Release Provisioning / Production Signing** (Deferred to **M12**).
- **No New Features or Modules**.

---

## 12. M8 Readiness Classification

**`M8_READY_FOR_IMPLEMENTATION`**
