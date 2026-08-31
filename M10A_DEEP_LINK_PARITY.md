# LocalKarar Mobile — M10A Deep Link Route Parity & Security Document

- **Mobile Baseline**: `feature/m9-lifecycle-state` @ [`4351342`](https://github.com/enesbaslanhan-jpg/localkarar-mobile/commit/4351342856ae13a7b30b02867e2337bfa0f6cd7f)
- **Web/Backend Base**: `design/localkarar-18` @ [`c7a53a8`](https://github.com/enesbaslanhan-jpg/local_akademi/commit/c7a53a8afff7daaa1227fe02d4933783dca7330e)
- **Canonical External Host**: `localkarar.com` (Scheme: `https`, Prefix: `https://localkarar.com/app/...`)
- **Status**: Complete & Verified (Android Runtime + Unit Test Suite + Codec Round-Trip)

---

## 1. Route Parity Matrix

| Canonical Web URL (`https://localkarar.com/app/...`) | Category | Semantic Target | Destination Mapping | Auth Boundary | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `/app/community` | `WEB_ROUTE_ALIGNED` | `NewsRoot` | `Destination.News` | `AUTH_GATED` | FIXED |
| `/app/community/topluluk` | `WEB_ROUTE_ALIGNED` | `CommunityFeedRoot` | `Destination.Community("feed")` | `AUTH_GATED` | FIXED |
| `/app/community/kisiler` | `WEB_ROUTE_ALIGNED` | `CommunityPeopleRoot` | `Destination.Community("people")` | `AUTH_GATED` | FIXED |
| `/app/community/sohbetler` | `WEB_ROUTE_ALIGNED` | `CommunityThreadsRoot` | `Destination.Community("threads")` | `AUTH_GATED` | FIXED |
| `/app/community/gonderi/:postId` | `WEB_ROUTE_ALIGNED` | `CommunityPost(postId)` | `Destination.CommunityPost(postId)` | `AUTH_GATED` | FIXED |
| `/app/profil` | `WEB_ROUTE_ALIGNED` | `SelfProfile` | `Destination.Profile` | `AUTH_GATED` | FIXED |
| `/app/profil/:userId` | `WEB_ROUTE_ALIGNED` | `UserProfile(userId)` | `Destination.CommunityProfile(userId)` | `AUTH_GATED` | FIXED |
| `/app/bildirimler` | `WEB_ROUTE_ALIGNED` | `NotificationsRoot` | `Destination.CommunityNotifications` | `AUTH_GATED` | FIXED |
| `/app/mentor` | `WEB_ROUTE_ALIGNED` | `MentorRoot` | `Destination.AiMentor` | `AUTH_GATED` | FIXED |
| `/app/courses` | `WEB_ROUTE_ALIGNED` | `CoursesRoot` | `Destination.Courses` | `AUTH_GATED` | FIXED |
| `/app/courses/:courseId/learn` | `WEB_ROUTE_ALIGNED` | `CourseDetail(courseId)` | `Destination.CourseDetail(courseId)` | `AUTH_GATED` | FIXED |
| `/app/courses/:courseId/learn/:lessonId` | `WEB_ROUTE_ALIGNED` | `CourseLesson(courseId, lessonId)` | `Destination.LessonReader(courseId, lessonId)` | `AUTH_GATED` | FIXED |
| `/app/decision-checks` | `WEB_ROUTE_ALIGNED` | `DecisionToolsRoot` | `Destination.DecisionTools("all")` | `AUTH_GATED` | FIXED |
| `/app/decision-checks/:code` | `WEB_ROUTE_ALIGNED` | `DecisionTool(code)` | `Destination.DecisionTool(code)` | `AUTH_GATED` | FIXED |
| `/app/finance/models/:modelCode` | `WEB_ROUTE_ALIGNED` | `FinancialModel(code)` | `Destination.FinancialModelDetail(code)` | `AUTH_GATED` | FIXED |
| `/app/workspaces` | `WEB_ROUTE_ALIGNED` | `WorkspacesRoot` | `Destination.Workspaces` | `AUTH_GATED` | FIXED |
| `/app/workspaces/:workspaceId/overview` | `WEB_ROUTE_ALIGNED` | `WorkspaceHome(wsId)` | `Destination.WorkspaceHome(wsId)` | `AUTH_GATED` | FIXED |
| `/app/workspaces/:workspaceId/tracker` | `WEB_ROUTE_ALIGNED` | `WorkspaceRecords(wsId)` | `Destination.Records(wsId)` | `AUTH_GATED` | FIXED |
| `/app/workspaces/:workspaceId/orders` | `WEB_ROUTE_ALIGNED` | `WorkspaceOrders(wsId)` | `Destination.Orders(wsId)` | `AUTH_GATED` | FIXED |
| `/app/workspaces/:workspaceId/products` | `WEB_ROUTE_ALIGNED` | `WorkspaceProducts(wsId)` | `Destination.Products(wsId)` | `AUTH_GATED` | FIXED |
| `/app/workspaces/:workspaceId/calendar` | `WEB_ROUTE_ALIGNED` | `WorkspaceCalendar(wsId)` | `Destination.Calendar(wsId)` | `AUTH_GATED` | FIXED |
| `/app/workspaces/:workspaceId/documents` | `WEB_ROUTE_ALIGNED` | `WorkspaceDocuments(wsId)` | `Destination.Documents(wsId)` | `AUTH_GATED` | FIXED |
| `/app/workspaces/:workspaceId/notifications` | `WEB_ROUTE_ALIGNED` | `WorkspaceNotifications(wsId)` | `Destination.Notifications(wsId)` | `AUTH_GATED` | FIXED |
| `/app/workspaces/:workspaceId/team` | `WEB_ROUTE_ALIGNED` | `WorkspaceTeam(wsId)` | `Destination.Team(wsId)` | `AUTH_GATED` | FIXED |
| `/app/workspaces/:workspaceId/contacts` | `WEB_ROUTE_ALIGNED` | `WorkspaceContacts(wsId)` | `Destination.Contacts(wsId)` | `AUTH_GATED` | FIXED |
| `/app/workspaces/:workspaceId/settings` | `WEB_ROUTE_ALIGNED` | `WorkspaceSettings(wsId)` | `Destination.WorkspaceSettings(wsId)` | `AUTH_GATED` | FIXED |
| `/app/workspaces/:workspaceId/activity` | `WEB_ROUTE_ALIGNED` | `WorkspaceActivity(wsId)` | `Destination.Activity(wsId)` | `AUTH_GATED` | FIXED |
| `/app/settings` | `WEB_ROUTE_ALIGNED` | `SettingsRoot` | `Destination.Settings` | `AUTH_GATED` | FIXED |

---

## 2. Native-Only Targets (`MOBILE_NATIVE_TARGET`)

| Target | Description | Native Destination |
| :--- | :--- | :--- |
| `CommunityThreadNative(threadId)` | Direct chat thread detail | `Destination.CommunityThreadDetail(threadId)` |
| `ConversationNative(conversationId)` | AI mentor conversation session | `Destination.Conversation(conversationId)` |
| `WorkspaceRecordNative(workspaceId, recordId)` | Individual tracker record detail | `Destination.RecordDetail(workspaceId, recordId)` |
| `NewsArticleNative(articleId)` | Full-screen news article reader | `Destination.NewsDetail(articleId)` |

---

## 3. Decision Tool vs Session Bug Fix

- **Historical Bug**: `/app/decision-checks/:code` previously passed the tool template string identifier directly into `Destination.DecisionSession(sessionId)`.
- **Resolution**:
  1. Added `Destination.DecisionTool(val code: String)`.
  2. Implemented `DecisionToolViewModel` and `DecisionToolScreen`.
  3. Deep link navigates to `Destination.DecisionTool(code)`.
  4. Tool screen requests session start/resumption via `DecisionRepository.startSession(code)`.
  5. Once active session UUID is obtained, safely transitions to `Destination.DecisionSession(sessionId)`.

---

## 4. Community Root Tab Routing

- Deep links `/app/community/topluluk`, `/app/community/kisiler`, and `/app/community/sohbetler` map directly to `Destination.Community("feed")`, `Destination.Community("people")`, and `Destination.Community("threads")`.
- `CommunityFeedScreen` dynamically selects the corresponding `CommunityInternalTab` on composition without fabricating mock entity IDs.

---

## 5. Security & Isolation

- **Host & Scheme Validation**: Only `https://localkarar.com/app/...` is accepted. Any other host (`evil.com`), scheme (`http://`, `localkarar://`, `javascript:`), or malformed URI is rejected safely.
- **DestinationCodec Isolation**: `DestinationCodec` is strictly used for internal navigation state persistence and is never exposed directly to external URLs.
- **Pending Target Single-Use & Cleanup**: `PendingDeepLinkStore` stores at most one in-memory target, consumed once upon authentication, and cleared on logout or account switch to prevent cross-account routing leaks.

---

## 6. External Dependency Classification

| Asset | Status | Notes |
| :--- | :--- | :--- |
| Android App Links Manifest | `SOURCE_READY` | `autoVerify="true"` configured for `localkarar.com` |
| Digital Asset Links (`assetlinks.json`) | `EXTERNAL_ASSOCIATION_PENDING` | Requires production web server deployment and release SHA-256 |
| iOS Universal Links Bridge | `SOURCE_READY` | `onOpenURL` and `onContinueUserActivity` hooked into `DeepLinkDispatcher` |
| Apple App Site Association (`apple-app-site-association`) | `EXTERNAL_ASSOCIATION_PENDING` | Requires Apple Developer Team ID and server-side `.well-known` hosting |
