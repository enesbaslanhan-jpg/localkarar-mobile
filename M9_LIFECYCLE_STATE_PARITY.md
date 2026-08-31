# LocalKarar Mobile — M9 Lifecycle & State Restoration Parity

**Milestone**: M9 Lifecycle / State Restoration Hardening  
**Mobile Base Branch**: `feature/m8-adaptive-ui-polish`  
**Mobile Base Commit**: `22ac259`  
**Web/Backend HEAD**: `c7a53a8` (`design/localkarar-18`)  
**Status**: `M9_ANDROID_RUNTIME_VERIFIED_IOS_SOURCE_PARTIAL`

---

## 1. Domain & Lifecycle Parity Matrix

| Feature / Domain | State Type | Storage Layer | Restoration Strategy | Parity Classification | Verification Evidence |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **App Root State** | App session & DI | `App.kt` singletons | Retained across configuration changes | `ALIGNED` | Android debug build & launch |
| **Authentication Session** | `SessionState` + Tokens | Platform `SecureStorage` + `AuthRepository` | Direct rehydration on cold start / token refresh on 401 | `ALIGNED_WITH_NATIVE_ADAPTATION` | Single-flight Ktor Auth plugin; Keychain / EncryptedPrefs |
| **Primary Navigation** | BackStack & Selected Tab | `rememberSaveable(saver = NavController.Saver)` | Primitive string codec (`DestinationCodec`) | `ALIGNED` | Rotation & recreation test |
| **ViewModel Ownership** | 35 ViewModels | `androidx.lifecycle.viewmodel.compose.viewModel(key)` | Retained in `ViewModelStore` across config changes | `ALIGNED` | Lifecycle Compose 2.8.0 |
| **Community Feed** | Post list & cursor | `CommunityViewModel` | Retained across recreation; server refreshed on demand | `ALIGNED` | Activity rotation |
| **Community Composer Draft** | Post text | `CommunityViewModel.metinInput` | Retained across recreation; memory only | `ALIGNED` | ViewModel retention |
| **Community Submission Lock** | Mutation gate | `isSubmittingPost` mutex | Disables submit button during active POST | `ALIGNED` | Double-tap protection verified |
| **Community Thread Creation** | Mutation gate | `isCreatingThread` mutex | Disables submit button during active creation | `ALIGNED` | Double-tap protection verified |
| **AI Mentor Chat History** | Conversation messages | Backend (`GET /mentor/conversations/{id}`) | Refetched from server on reopen | `SERVER_RECONCILED` | Server truth |
| **AI Mentor SSE Stream** | Active stream | `viewModelScope` coroutine | Cleanly cancelled via `awaitClose`; reconciled from server | `SERVER_RECONCILED` | Stream cancellation |
| **Profile Form Draft** | Display name / Edit mode | `SettingsViewModel` + `rememberSaveable` | Preserved across Activity rotation | `ALIGNED` | Saveable boolean + retained VM |
| **Record Edit Form** | Form fields & amounts | `RecordEditViewModel` + `rememberSaveable` | Preserved across Activity rotation | `ALIGNED` | Saveable inputs |
| **Formula Calculator Inputs** | Numeric formula inputs | `FormulaCalculatorViewModel` | Retained across Activity rotation; formula loaded by ID | `ALIGNED` | Decoupled `FormulaDetail(formulaId)` |
| **News Detail Direct Restore** | Article content | `NewsViewModel` + `LkLoadingState` | Directly loadable via `articleId` with loading/retry fallback | `ALIGNED` | Safe direct route |
| **Active Workspace** | Selected workspace ID | `ActiveWorkspaceStore` | Retained across recreation | `ALIGNED` | Store StateFlow |
| **Passwords & Confirmation** | Passwords & delete flags | Memory only | Cleared on recreation/process death | `DO_NOT_RESTORE` | Security policy compliant |
| **File Picker Native Handles** | OS picker delegates | Transient Composable state | Bounded to picker lifetime; delegate cleared on dismiss | `ALIGNED_WITH_NATIVE_ADAPTATION` | iOS cinterop / Android ActivityResult |
| **Push / Deep Links** | Universal routing | System Intent / APNs | Handled in upcoming milestone | `M10_FOLLOWUP` | Planned M10 |
| **App Store Lifecycle** | Production provisioning | iOS Certificate / Provisioning Profile | Handled in store release milestone | `M12_FOLLOWUP` | Planned M12 |

---

## 2. Key Architectural Upgrades in M9

1. **Navigation BackStack Persistence**:
   - Implemented `DestinationCodec` and `NavController.Saver` to serialize all 28 `Destination` variants to and from primitive strings.
   - Replaced raw in-memory `remember { NavController(...) }` with `rememberNavController(Destination.Home)` using `rememberSaveable`.
2. **Formula Detail Decoupling**:
   - Refactored `Destination.FormulaDetail` from embedding full `FormulaDto` to using `formulaId: String`.
   - Updated `FormulaCalculatorViewModel` to load formula metadata by ID from `CalculationsRepository`.
3. **Lifecycle-Backed ViewModel Ownership**:
   - Migrated all 35 ViewModels from Composable `remember { ... }` to `viewModel(key = "...") { ... }` using `androidx.lifecycle.viewmodel.compose.viewModel`.
   - Guaranteed that screen rotations and configuration changes retain active state, form inputs, and pending jobs without re-fetching.
4. **Duplicate Submission Locks (P0)**:
   - Added `isSubmittingPost` guard to `CommunityViewModel.submitPost()` and `isCreatingThread` guard to `ThreadsViewModel.createThread()`.
   - Updated UI buttons in `ComposePostSheet.kt` and `CreateThreadSheet.kt` to disable and show loading indicators during submissions.
5. **Form State Resilience**:
   - Added `rememberSaveable` to `RecordEditScreen` inputs, `ProfileScreen` edit flags, and auth route state.
6. **Multiplatform Compilation Verification**:
   - Verified that Android (`assembleDebug`), iOS Simulator (`compileKotlinIosSimulatorArm64`), and iOS Device (`compileKotlinIosArm64`) compile cleanly.
