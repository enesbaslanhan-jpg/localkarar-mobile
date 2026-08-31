# LocalKarar Mobile — M9.0 Lifecycle & State Restoration Audit

**Milestone**: M9.0 Lifecycle / State Restoration Audit  
**Mobile Base Branch**: `feature/m8-adaptive-ui-polish`  
**Mobile Base Commit**: `22ac259`  
**Web/Backend HEAD**: `c7a53a8` (`design/localkarar-18`)  
**Status**: `M9_READY_FOR_IMPLEMENTATION`

---

## 1. Executive Summary

This audit evaluates the lifecycle, state restoration, process recreation, and duplicate request resilience of LocalKarar Mobile across Android and iOS phone form factors. The application preserves 100% of its locked 5-tab navigation, backend contracts, and business logic.

Key findings:
1. **ViewModel Ownership**: ViewModels are currently instantiated with Composable `remember { ... }` rather than a lifecycle-backed `ViewModelStoreOwner`. As a result, Android configuration changes (rotation, dark mode) and process recreation re-instantiate all ViewModels from scratch.
2. **Navigation State**: `NavController` holds its backstack in an in-memory `MutableStateFlow` initialized to `Destination.Home`. Configuration changes and process death reset the user back to the Home screen, discarding nested detail destinations.
3. **Form & Draft Persistence**: Form fields across Login, Business Tracker, Profile, and Community Composer rely on `remember { mutableStateOf("") }` (zero `rememberSaveable` usage), causing user input loss on recreation.
4. **Duplicate Request & Mutation Guards**: `CommunityViewModel.submitPost` and certain mutation handlers lack single-flight submission locks, risking duplicate records on rapid user tapping or recreation mid-flight.
5. **AI Mentor Stream Lifecycle**: SSE progressive streaming is safely scoped to `viewModelScope` with cancellation in `awaitClose`, and backend persists completed responses; however, screen recreation mid-stream disconnects the client and reloads from server via `loadConversation`.
6. **Multiplatform Lifecycle Readiness**: `lifecycle-viewmodel-compose:2.8.0` and `lifecycle-runtime-compose:2.8.0` are already declared in `commonMain.dependencies`, allowing a zero-external-dependency upgrade to lifecycle-retained ViewModels across both Android and iOS.

---

## 2. Current Architecture vs. Target M9 Architecture

| Component | Current Implementation (M8) | Target M9 Architecture | Compatibility |
| :--- | :--- | :--- | :--- |
| **App State Owner** | `App.kt` (`remember` holders) | `App.kt` + `AuthRepository` single source of truth | `COMMON` |
| **Session Owner** | `AuthRepository` (`SessionState` + Ktor Auth bearer plugin) | `AuthRepository` + `SecureStorage` (Keychain / EncryptedPrefs) | `COMMON` |
| **Navigation Owner** | `NavController` (`remember { NavController(Destination.Home) }`) | `rememberNavController` / saveable backStack state | `COMMON` |
| **ViewModel Ownership** | `remember { SomeViewModel(...) }` (Composable lifetime) | `viewModel { SomeViewModel(...) }` (retained across config changes) | `COMMON` (JetBrains Lifecycle 2.8.0) |
| **Repository Ownership** | Stateless HTTP wrappers created at root `App.kt` | Root singletons passed via DI/composition | `COMMON` |
| **Coroutine Ownership** | `viewModelScope` in ViewModels, `rememberCoroutineScope()` in UI | `viewModelScope` for domain jobs, lifecycle-bounded UI scopes | `COMMON` |

---

## 3. Comprehensive ViewModel Inventory (35 ViewModels)

| # | ViewModel | Feature | Creation Site | Current Lifetime | State Type | Repo Deps | Recreation Behavior | Duplicate Risk | Classification |
| :---: | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | `AuthViewModel` | Auth / Root | `App.kt:64` | `remember` (App) | `StateFlow` | `AuthRepository` | Re-runs `restoreSession` | Low (idempotent /auth/me) | `SAFE` |
| 2 | `HomeViewModel` | Home | `App.kt:72` | `remember` (App) | `StateFlow` | `Dashboard`, `Workspace`, `Decision` | Re-runs `loadDashboard` | Low (GET) | `SAFE` |
| 3 | `CommunityViewModel` | Community | `AppShell.kt:276` | `remember` (Shell) | `StateFlow` + `mutableStateOf` | `CommunityRepository` | Re-runs `refreshFeed`, loses draft | **HIGH (no submit lock)** | `RISKY` |
| 4 | `SocialViewModel` | Community | `AppShell.kt:277` | `remember` (Shell) | `StateFlow` | `CommunityRepository` | Re-runs `loadMembers` | Low (GET) | `SAFE` |
| 5 | `ThreadsViewModel` | Community | `AppShell.kt:278` | `remember` (Shell) | `StateFlow` + `mutableStateOf` | `CommunityRepository` | Re-runs `loadThreads` | Medium (messages reset) | `SAFE` |
| 6 | `CommunityNotificationsViewModel` | Community | `AppShell.kt:279` | `remember` (Shell) | `StateFlow` | `CommunityRepository` | Re-runs `loadNotifications` | Low (GET) | `SAFE` |
| 7 | `NewsViewModel` | News | `AppShell.kt:280` | `remember` (Shell) | `StateFlow` | `NewsRepository` | Re-runs `refresh`, `articleById` can be null | Medium (NewsDetail lookup) | `RISKY` |
| 8 | `SettingsViewModel` | Settings | `AppShell.kt:281` | `remember` (Shell) | `StateFlow` | `SettingsRepository` | Re-runs `loadSettings` | Low (GET) | `SAFE` |
| 9 | `CoursesViewModel` | Courses | `AppShell.kt:298` | `remember` (Route) | `StateFlow` | `Course`, `Dashboard` | Recreated on tab switch/recreation | Low (GET) | `SAFE` |
| 10 | `CourseDetailViewModel` | Courses | `AppShell.kt:306` | `remember(courseId)` | `StateFlow` | `CourseRepository` | Recreated on recreation | Low (GET) | `SAFE` |
| 11 | `LessonReaderViewModel` | Courses | `AppShell.kt:314` | `remember(courseId, lessonId)` | `StateFlow` | `CourseRepository` | Recreated on recreation | Low (GET) | `SAFE` |
| 12 | `DecisionToolsViewModel` | Decision | `AppShell.kt:324` | `remember` (Route) | `StateFlow` | `DecisionRepository` | Re-evaluates filter | Low (GET) | `SAFE` |
| 13 | `DecisionSessionViewModel` | Decision | `AppShell.kt:339` | `remember(sessionId)` | `StateFlow` | `DecisionRepository` | Re-fetches session from server | Low (answers persisted) | `SAFE` |
| 14 | `MentorViewModel` | AI Mentor | `AppShell.kt:346` | `remember` (Route) | `StateFlow` | `MentorRepository` | Re-fetches conversation list | Low (GET) | `SAFE` |
| 15 | `MemoryViewModel` | AI Mentor | `AppShell.kt:347` | `remember` (Route) | `StateFlow` | `MentorRepository` | Re-fetches memories | Low (GET) | `SAFE` |
| 16 | `ConversationViewModel` | AI Mentor | `AppShell.kt:356` | `remember(conversationId)` | `StateFlow` + `mutableStateOf` | `MentorRepository` | Re-fetches conversation, cancels SSE | Medium (stream reconnect) | `SAFE` |
| 17 | `CalculationsViewModel` | Calculations | `AppShell.kt:366` | `remember(activeWorkspaceId)` | `StateFlow` | `Calculations`, `Workspace` | Re-fetches calculation catalog | Low (GET) | `SAFE` |
| 18 | `FormulaCalculatorViewModel` | Calculations | `AppShell.kt:387` | `remember(formula.id)` | `StateFlow` | `CalculationsRepository` | Recreated, loses uncalculated inputs | Low (in-memory) | `RISKY` |
| 19 | `FinancialModelViewModel` | Calculations | `AppShell.kt:393` | `remember(code)` | `StateFlow` | `CalculationsRepository` | Re-fetches model metadata | Low (GET) | `SAFE` |
| 20 | `ModelRunsViewModel` | Calculations | `AppShell.kt:407` | `remember(workspaceId, modelCode)` | `StateFlow` | `CalculationsRepository` | Re-fetches runs | Low (GET) | `SAFE` |
| 21 | `RunDetailViewModel` | Calculations | `AppShell.kt:417` | `remember(workspaceId, runId)` | `StateFlow` | `CalculationsRepository` | Re-fetches run detail | Low (GET) | `SAFE` |
| 22 | `WorkspacesViewModel` | Business Tracker | `AppShell.kt:423` | `remember` (Route) | `StateFlow` | `WorkspaceRepository`, `Store` | Re-fetches workspaces | Low (GET) | `SAFE` |
| 23 | `WorkspaceHomeViewModel` | Business Tracker | `AppShell.kt:432` | `remember(workspaceId)` | `StateFlow` | `WorkspaceRepository` | Re-fetches workspace dashboard | Low (GET) | `SAFE` |
| 24 | `RecordsViewModel` | Business Tracker | `AppShell.kt:452` | `remember(workspaceId)` | `StateFlow` | `WorkspaceRepository` | Re-fetches records | Low (GET) | `SAFE` |
| 25 | `RecordDetailViewModel` | Business Tracker | `AppShell.kt:460` | `remember(workspaceId, recordId)` | `StateFlow` | `WorkspaceRepository` | Re-fetches record | Low (GET) | `SAFE` |
| 26 | `RecordEditViewModel` | Business Tracker | `AppShell.kt:468` | `remember(workspaceId, recordId)` | `StateFlow` | `WorkspaceRepository` | Re-fetches record, loses draft form | Medium (mid-save duplicate) | `RISKY` |
| 27 | `OrdersViewModel` | Business Tracker | `AppShell.kt:476` | `remember(workspaceId)` | `StateFlow` | `WorkspaceRepository` | Re-fetches orders via LaunchedEffect | Low (GET) | `SAFE` |
| 28 | `ProductsViewModel` | Business Tracker | `AppShell.kt:484` | `remember(workspaceId)` | `StateFlow` | `WorkspaceRepository` | Re-fetches products via LaunchedEffect | Low (GET) | `SAFE` |
| 29 | `DocumentsViewModel` | Business Tracker | `AppShell.kt:492` | `remember(workspaceId)` | `StateFlow` | `WorkspaceRepository` | Re-fetches documents | Low (GET) | `SAFE` |
| 30 | `NotificationsViewModel` | Business Tracker | `AppShell.kt:500` | `remember(workspaceId)` | `StateFlow` | `WorkspaceRepository` | Re-fetches notifications | Low (GET) | `SAFE` |
| 31 | `CalendarViewModel` | Business Tracker | `AppShell.kt:508` | `remember(workspaceId)` | `StateFlow` | `WorkspaceRepository` | Re-fetches calendar | Low (GET) | `SAFE` |
| 32 | `TeamViewModel` | Business Tracker | `AppShell.kt:516` | `remember(workspaceId)` | `StateFlow` | `WorkspaceRepository` | Re-fetches team members | Low (GET) | `SAFE` |
| 33 | `ContactsViewModel` | Business Tracker | `AppShell.kt:524` | `remember(workspaceId)` | `StateFlow` | `WorkspaceRepository` | Re-fetches contacts | Low (GET) | `SAFE` |
| 34 | `ActivityViewModel` | Business Tracker | `AppShell.kt:532` | `remember(workspaceId)` | `StateFlow` | `WorkspaceRepository` | Re-fetches audit activity | Low (GET) | `SAFE` |
| 35 | `WorkspaceSettingsViewModel` | Business Tracker | `AppShell.kt:540` | `remember(workspaceId)` | `StateFlow` | `WorkspaceRepository` | Re-fetches settings | Low (GET) | `SAFE` |

---

## 4. State Restoration Strategy Matrix

| Domain / State | Classification | Storage Mechanism | Restoration Strategy | Sensitive? |
| :--- | :--- | :--- | :--- | :--- |
| **Auth Access Token** | `PERSISTENT` | `SecureStorage` (Keychain / EncryptedSharedPreferences) | Direct read on cold start | Yes (Encrypted) |
| **Auth Refresh Token** | `PERSISTENT` | `SecureStorage` (Keychain / EncryptedSharedPreferences) | Used for Ktor Bearer Token Refresh | Yes (Encrypted) |
| **Current User Profile** | `PERSISTENT` | Backend (`GET /auth/me`) | Refetched on startup / 401 refresh | No |
| **Navigation BackStack** | `RESTORABLE` | `rememberSaveable` / Navigation State Bundle | Rehydrated from serialized route identifiers | No |
| **Selected Tab** | `RESTORABLE` | `rememberSaveable` (Root Nav) | Restores exact active tab | No |
| **Community Feed Posts** | `PERSISTENT_SERVER` | Backend (`GET /community/feed`) | Refetched from server on start/refresh | No |
| **Community Post Draft** | `RESTORABLE_UI` | `rememberSaveable` | Restored in text field if composing | No |
| **AI Mentor Messages** | `PERSISTENT_SERVER` | Backend (`GET /mentor/conversations/{id}`) | Refetched on open/recreation | No |
| **AI Mentor Active Stream** | `TRANSIENT` | Runtime Flow / SSE channel | Cancelled on recreation; refetched from server | No |
| **Decision Session Answers**| `PERSISTENT_SERVER` | Backend (`PATCH /decision/sessions/{id}/answer`) | Continuously persisted; reloaded from backend | No |
| **Form Inputs (Business)** | `RESTORABLE_UI` | `rememberSaveable` | Kept across config change / process restore | No |
| **Passwords / Sensitive** | `DO_NOT_RESTORE` | Memory only (Cleared on recreation/logout) | NEVER persisted or serialized | **CRITICAL** |
| **Active Dialogs / Sheets** | `TRANSIENT` | `rememberSaveable` (boolean flag only) | Reopened if appropriate or default closed | No |

---

## 5. Navigation Serializability Audit

All 28 `Destination` types in `Destination.kt` map to lightweight identifiers (`Int` / `String`), with exactly one exception:
- **`FormulaDetail`**: Currently embeds full DTO `data class FormulaDetail(val formula: FormulaDto, val historicalCalculation: FormulaCalculationDto? = null)`.
- **Recommendation**: Decouple `FormulaDetail` to `data class FormulaDetail(val formulaId: String, val historicalCalculationId: String? = null)` and fetch formula metadata from `CalculationsRepository` if missing, enabling 100% serializable route navigation.

---

## 6. Duplicate Request & Mutation Protection (P0 Audit)

1. **`CommunityViewModel.submitPost()`**:
   - Issue: Lacks an `isSubmitting` guard. Rapid double-tapping before the network completes sends multiple POST requests.
   - Fix: Add `var isSubmitting by mutableStateOf(false)` and early return if already submitting.
2. **`RecordEditViewModel.save()`**:
   - Issue: If the screen recreates while saving, `isSaving` resets to `false` in the newly instantiated ViewModel.
   - Fix: Retain ViewModel across configuration changes via `viewModel { ... }`.
3. **`ThreadsViewModel.sendMessage()`**:
   - Guarded with `if (isSendingMessage) return` (Safe).
4. **`DecisionSessionViewModel.completeSession()`**:
   - Guarded with `_uiState.value = currentState.copy(isSubmitting = true)` (Safe).
5. **`ConversationViewModel.sendMessage()`**:
   - Guarded with `if (isStreaming) return` (Safe).

---

## 7. AI Mentor SSE Lifecycle & Process Resilience

1. **Streaming Flow**: Uses `preparePost.execute` in `MentorRepository.readSseStream()`, reading UTF-8 lines from `bodyAsChannel()`.
2. **Lifecycle Cancellation**: The flow is wrapped in `callbackFlow` with `awaitClose { job.cancel() }`. When the ViewModel or Composable is disposed, the HTTP connection is terminated cleanly.
3. **Backend Truth**: When the client disconnects or process dies mid-generation, the server completes generation in the background. When the mobile app reopens `Destination.Conversation(id)`, `loadConversation(id)` retrieves the full completed message history from the backend.
4. **No Duplicate Auto-Generation**: Reopening an existing conversation loads messages via `GET /mentor/conversations/{id}` without auto-firing a new SSE stream.

---

## 8. Memory & Leak Safety

- **Android (`Platform.android.kt`)**: `AppContextHolder.appContext` stores `applicationContext` (not an `Activity`). Zero Activity leaks detected.
- **iOS (`Platform.ios.kt`)**: `DocumentPickerDelegate` is retained as a local state object during file selection and dismissed via `onDismiss { activeDelegate = null }`. Zero dangling delegate leaks detected.

---

## 9. Required M9 Implementation Scope

1. **Architecture**:
   - Migrate from raw `remember { SomeViewModel(...) }` to `viewModel { SomeViewModel(...) }` (using already available `androidx.lifecycle.viewmodel.compose`).
2. **Navigation**:
   - Implement saveable navigation backstack persistence using `rememberSaveable` for `NavController`.
   - Decouple `Destination.FormulaDetail` to reference `formulaId` instead of embedding full `FormulaDto`.
3. **Forms & Drafts**:
   - Convert non-sensitive form fields (Record Title, Amount, Description, Community Post Draft, Email Change input) to `rememberSaveable`.
   - Ensure passwords, delete confirmations, and tokens remain memory-only.
4. **Mutation Safety**:
   - Add `isSubmitting` mutex to `CommunityViewModel.submitPost` and `createThread`.
5. **Lifecycle Collection**:
   - Utilize `collectAsStateWithLifecycle()` on active data flows where appropriate.
