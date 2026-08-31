# LocalKarar Mobile — M10.2 Final Push & Deep Link Contract Lock

> **FINAL PROPOSED CONTRACT — AWAITING PRODUCT DOMAIN CONFIRMATION**
> 
> - **Mobile Base**: `feature/m9-lifecycle-state` @ [`4351342`](https://github.com/enesbaslanhan-jpg/localkarar-mobile/commit/4351342856ae13a7b30b02867e2337bfa0f6cd7f)
> - **Web/Backend HEAD**: `design/localkarar-18` @ [`c7a53a8`](https://github.com/enesbaslanhan-jpg/local_akademi/commit/c7a53a8afff7daaa1227fe02d4933783dca7330e)
> - **Status**: Locked contract for M10 implementation phasing (M10A → M10B → M10C → M10D).

---

## 1. Corrected Auth Boundary & Web Route Classification

All routes in the web client under `/app/*` (`frontend/src/router/index.jsx`) are nested within `<Route element={<ProtectedRoute />}>`. Consequently, **all application deep links require authentication before navigation**.

### Route Classification Matrix

| Web URL (`frontend/src/router/index.jsx`) | Category | Web Component | Semantic Target | Mobile Destination | Auth Boundary |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `/app/community/gonderi/:postId` | `EXISTING_WEB_ROUTE` | `CommunityPostPage` | `CommunityPost(postId)` | `Destination.CommunityPost(postId)` | `AUTH_REQUIRED` |
| `/app/community/sohbetler` | `EXISTING_WEB_ROUTE` | `CommunitySocialPage (threads)` | `CommunityThreadsRoot` | `Destination.Community` *(Threads tab)* | `AUTH_REQUIRED` |
| `/app/community/kisiler` | `EXISTING_WEB_ROUTE` | `CommunitySocialPage (people)` | `CommunityPeopleRoot` | `Destination.Community` *(Social tab)* | `AUTH_REQUIRED` |
| `/app/community/topluluk` | `EXISTING_WEB_ROUTE` | `CommunityPage (feed)` | `CommunityFeedRoot` | `Destination.Community` *(Feed tab)* | `AUTH_REQUIRED` |
| `/app/community` | `EXISTING_WEB_ROUTE` | `NewsPage` | `NewsRoot` | `Destination.News` | `AUTH_REQUIRED` |
| `/app/profil` | `EXISTING_WEB_ROUTE` | `ProfilePage (self)` | `SelfProfile` | `Destination.Profile` | `AUTH_REQUIRED` |
| `/app/profil/:userId` | `EXISTING_WEB_ROUTE` | `ProfilePage (other)` | `UserProfile(userId)` | `Destination.CommunityProfile(userId)` | `AUTH_REQUIRED` |
| `/app/bildirimler` | `EXISTING_WEB_ROUTE` | `NotificationsPage` | `NotificationsRoot` | `Destination.CommunityNotifications` | `AUTH_REQUIRED` |
| `/app/mentor` | `EXISTING_WEB_ROUTE` | `MentorPage` | `MentorRoot` | `Destination.AiMentor` | `AUTH_REQUIRED` |
| *[Native DM / Notification only]* | `MOBILE_NATIVE_TARGET`| N/A *(Web has no direct DM route)* | `CommunityThread(threadId)` | `Destination.CommunityThreadDetail(threadId)` | `AUTH_REQUIRED` |
| *[Native Conversation only]* | `MOBILE_NATIVE_TARGET`| N/A *(Web has no conversation URL)* | `Conversation(conversationId)` | `Destination.Conversation(conversationId)` | `AUTH_REQUIRED` |
| *[Native News Article only]* | `MOBILE_NATIVE_TARGET`| N/A *(Web renders in-modal)* | `NewsArticle(articleId)` | `Destination.NewsDetail(articleId)` | `AUTH_REQUIRED` |
| `/app/courses` | `EXISTING_WEB_ROUTE` | `CoursesPage` | `CoursesRoot` | `Destination.Courses` | `AUTH_REQUIRED` |
| `/app/courses/:courseId/learn/:lessonId?` | `EXISTING_WEB_ROUTE` | `CoursePlayerPage` | `CourseLesson(cId, lId?)` | `Destination.LessonReader` (if lId) / `CourseDetail` (if no lId) | `AUTH_REQUIRED` |
| `/app/decision-checks` | `EXISTING_WEB_ROUTE` | `DecisionCheckList` | `DecisionToolsRoot` | `Destination.DecisionTools("all")` | `AUTH_REQUIRED` |
| `/app/decision-checks/:code` | `EXISTING_WEB_ROUTE` | `DecisionCheckSession` | `DecisionTool(code)` | `Destination.DecisionTool(code)` *(New)* | `AUTH_REQUIRED` |
| `/app/finance/models/:modelCode` | `EXISTING_WEB_ROUTE` | `FinancialModelWorkspace` | `FinancialModel(code)` | `Destination.FinancialModelDetail(code)` | `AUTH_REQUIRED` |
| `/app/workspaces` | `EXISTING_WEB_ROUTE` | `WorkspaceList` | `WorkspacesRoot` | `Destination.Workspaces` | `AUTH_REQUIRED` |
| `/app/workspaces/:workspaceId/overview` | `EXISTING_WEB_ROUTE` | `WorkspaceOverview` | `WorkspaceHome(wsId)` | `Destination.WorkspaceHome(wsId)` | `AUTH_REQUIRED` |
| `/app/workspaces/:workspaceId/tracker` | `EXISTING_WEB_ROUTE` | `WorkspaceTracker` | `WorkspaceRecords(wsId)` | `Destination.Records(wsId)` | `AUTH_REQUIRED` |
| *[Native Record Detail only]* | `MOBILE_NATIVE_TARGET`| N/A *(Web renders in-modal)* | `WorkspaceRecord(wsId, rId)` | `Destination.RecordDetail(wsId, rId)` | `AUTH_REQUIRED` |
| `/app/workspaces/:workspaceId/orders` | `EXISTING_WEB_ROUTE` | `WorkspaceOrders` | `WorkspaceOrders(wsId)` | `Destination.Orders(wsId)` | `AUTH_REQUIRED` |
| `/app/workspaces/:workspaceId/products` | `EXISTING_WEB_ROUTE` | `WorkspaceProducts` | `WorkspaceProducts(wsId)` | `Destination.Products(wsId)` | `AUTH_REQUIRED` |
| `/app/workspaces/:workspaceId/calendar` | `EXISTING_WEB_ROUTE` | `WorkspaceCalendar` | `WorkspaceCalendar(wsId)` | `Destination.Calendar(wsId)` | `AUTH_REQUIRED` |
| `/app/workspaces/:workspaceId/documents` | `EXISTING_WEB_ROUTE` | `WorkspaceDocuments` | `WorkspaceDocuments(wsId)` | `Destination.Documents(wsId)` | `AUTH_REQUIRED` |
| `/app/workspaces/:workspaceId/notifications` | `EXISTING_WEB_ROUTE` | `WorkspaceNotifications` | `WorkspaceNotifs(wsId)` | `Destination.Notifications(wsId)` | `AUTH_REQUIRED` |
| `/app/workspaces/:workspaceId/team` | `EXISTING_WEB_ROUTE` | `WorkspaceTeam` | `WorkspaceTeam(wsId)` | `Destination.Team(wsId)` | `AUTH_REQUIRED` |
| `/app/workspaces/:workspaceId/contacts` | `EXISTING_WEB_ROUTE` | `WorkspaceContacts` | `WorkspaceContacts(wsId)` | `Destination.Contacts(wsId)` | `AUTH_REQUIRED` |
| `/app/workspaces/:workspaceId/settings` | `EXISTING_WEB_ROUTE` | `WorkspaceSettings` | `WorkspaceSettings(wsId)` | `Destination.WorkspaceSettings(wsId)` | `AUTH_REQUIRED` |
| `/app/workspaces/:workspaceId/activity` | `EXISTING_WEB_ROUTE` | `WorkspaceActivity` | `WorkspaceActivity(wsId)` | `Destination.Activity(wsId)` | `AUTH_REQUIRED` |
| `/app/settings` | `EXISTING_WEB_ROUTE` | `SettingsPage` | `SettingsRoot` | `Destination.Settings` | `AUTH_REQUIRED` |

---

## 2. Decision Tool vs Session Resolution

- **Problem**: Previously, `code` from `/app/decision-checks/:code` was incorrectly passed directly into `Destination.DecisionSession(sessionId = code)`.
- **Locked Solution**:
  - `Destination.DecisionTool(val code: String)`: Represents opening a decision tool by its template code (e.g. `"finansman-karari"`). It fetches the tool metadata and starts/resumes a session.
  - `Destination.DecisionSession(val sessionId: String)`: Strictly reserved for active session UUID instances.

---

## 3. Course Optional Lesson Parameter Handling

- `/app/courses/:courseId/learn/:lessonId?`:
  - If `:lessonId` is present and valid integer: maps to `Destination.LessonReader(courseId, lessonId)`.
  - If `:lessonId` is omitted: maps to `Destination.CourseDetail(courseId)` (Mobile course overview where user can select or resume the latest lesson).
  - Parser **never** fabricates default lesson IDs.

---

## 4. Locked Backend Push & Device Contract

### A. Prisma Schema (`PushInstallation`)
```prisma
model PushInstallation {
  id             String   @id @default(uuid())
  installationId String   @unique
  userId         Int
  platform       String   // "android" | "ios"
  pushToken      String   @unique
  appVersion     String?
  locale         String?  @default("tr")
  enabled        Boolean  @default(true)
  lastSeenAt     DateTime @default(now())
  createdAt      DateTime @default(now())
  updatedAt      DateTime @updatedAt

  user User @relation(fields: [userId], references: [id], onDelete: Cascade)

  @@index([userId])
}
```

### B. Device Registration & Logout Lifecycle
1. **Installation ID**: Random UUID generated on initial mobile app install and stored persistently in app storage (`Settings` / `SharedPreferences` / `NSUserDefaults`).
2. **Registration / Update (`PUT /devices/:installationId`)**:
   - Auth: `Bearer <jwt>` (authenticated `request.user.id` is authoritative; body `userId` is ignored).
   - Transactional upsert: If `pushToken` existed on a previous installation row, that old association is pruned to respect `@unique`. The current `installationId` is bound to `userId` with `enabled = true`.
3. **Logout Unregistration (`DELETE /devices/:installationId`)**:
   - Auth: `Bearer <jwt>`.
   - Physical deletion: Deletes `PushInstallation` where `installationId = :installationId` and `userId = request.user.id`.
   - Result: Immediate revocation. No push notifications for the logged-out user can reach this installation.
4. **Account Switch**:
   - User A logs out → row deleted.
   - User B logs in → `PUT /devices/:installationId` registers installation under User B.
   - Zero cross-account push leakage.

---

## 5. Unified Push Provider: FCM HTTP v1

- **Backend**: Direct REST calls to Google FCM HTTP v1 API (`https://fcm.googleapis.com/v1/projects/{projectId}/messages:send`).
- **Android**: Google Play Services FCM token.
- **iOS**: Firebase Messaging SDK integrated with Apple APNs. Maps APNs device token to FCM token automatically.
- **Credentials**:
  - Backend: Firebase Service Account JSON via environment variable.
  - Android: `google-services.json`.
  - iOS: `GoogleService-Info.plist` + Apple Developer APNs Authentication Key (`.p8`).

---

## 6. Semantic Push Data Payload Contract

Avoids ambiguous generic keys (`entityId`, `secondaryId`) by using explicit typed schema:

```json
// Community Post Mention / Reply
{
  "notification": { "title": "LocalKarar", "body": "Gönderinize yeni bir yanıt geldi" },
  "data": { "target": "community_post", "postId": "post_1001" }
}

// Community Direct Message / Thread
{
  "notification": { "title": "Ahmet Yılmaz", "body": "Yeni mesaj gönderdi" },
  "data": { "target": "community_thread", "threadId": "thr_505" }
}

// Business Task Due Alert
{
  "notification": { "title": "Vade Hatırlatması", "body": "KDV ödemesi için son 1 gün" },
  "data": { "target": "workspace_record", "workspaceId": "ws_alpha", "recordId": "rec_99" }
}

// Account / Billing
{
  "notification": { "title": "Üyelik Durumu", "body": "Deneme süreniz 3 gün içinde bitiyor" },
  "data": { "target": "account" }
}
```

---

## 7. Initial Push vs In-App Event Set

| Event | Channel | Reason |
| :--- | :--- | :--- |
| **Direct Message / Thread Mention** | `PUSH + IN_APP` | High priority personal communication |
| **Post Reply / User Mention** | `PUSH + IN_APP` | Direct engagement on user-generated content |
| **Business Task Due Reminder** | `PUSH + IN_APP` | Critical financial/operational deadline |
| **Account / Billing Status** | `PUSH + IN_APP` | Subscription continuity & security alerts |
| **Post Likes / Bookmark Actions** | `IN_APP_ONLY` | Prevents high-volume notification spam |
| **Broadcast News Publication** | `IN_APP_ONLY` | High frequency, non-urgent information |
| **Marketing Campaigns** | `NON_SCOPE` | Excluded from product milestone M10 |

---

## 8. Deep Link Parser & Auth Gating Flow

```
External URL (https://<domain>/app/...)
       │
       ▼
DeepLinkParser.parse(url)
       ├── Invalid / Unknown / Malformed ──► Fallback to Destination.Home
       └── Valid DeepLinkTarget
                 │
                 ▼
       PendingDeepLinkStore.set(target)
                 │
                 ▼
       App Session Restoration (/auth/me)
                 ├── Authenticated ──► Dispatch Destination to NavController & consume pending target
                 └── Unauthenticated ──► Navigate to Destination.Login (Target preserved until login)
```

---

## 9. Implementation Phasing

1. **M10A — Deep Links & Route Parity (Mobile)**:
   - Implement `DeepLinkParser`, `DeepLinkTarget`, `PendingDeepLinkStore`.
   - Add `Destination.DecisionTool(code)`.
   - Add Android `Intent` / iOS `.onOpenURL` routing handlers.
   - Comprehensive unit test suite for deep link parsing and malformed inputs.
   - *Requires zero external credentials.*
2. **M10B — Backend Push & Device API (Backend)**:
   - Add `PushInstallation` Prisma model.
   - Implement `PUT /devices/:installationId` and `DELETE /devices/:installationId`.
   - Implement `PushService` using FCM HTTP v1.
3. **M10C — Android FCM & Permissions (Android)**:
   - Add `POST_NOTIFICATIONS` runtime permission handling.
   - Implement `FirebaseMessagingService` for token registration and payload routing.
4. **M10D — iOS APNs / Firebase Integration (iOS)**:
   - Add Firebase Messaging iOS bridge and `UNUserNotificationCenterDelegate`.
   - Configure Universal Links domain verification.
