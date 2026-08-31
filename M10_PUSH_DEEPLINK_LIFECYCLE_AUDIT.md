# LocalKarar Mobile — M10.0 Push Notifications, Deep Links & App Lifecycle Audit

- **Authoritative Backend**: [`https://github.com/enesbaslanhan-jpg/local_akademi/tree/design/localkarar-18`](https://github.com/enesbaslanhan-jpg/local_akademi/tree/design/localkarar-18)
- **Web/Backend HEAD**: [`c7a53a8`](https://github.com/enesbaslanhan-jpg/local_akademi/commit/c7a53a8afff7daaa1227fe02d4933783dca7330e)
- **Mobile Branch**: `feature/m9-lifecycle-state`
- **Mobile Base Commit**: [`4351342`](https://github.com/enesbaslanhan-jpg/localkarar-mobile/commit/4351342856ae13a7b30b02867e2337bfa0f6cd7f)
- **Audit Classification**: `M10_REQUIRES_BACKEND_CONTRACT`

---

## 1. Remote GitHub Backend Inspection Summary

Inspected all files and routes directly on `https://github.com/enesbaslanhan-jpg/local_akademi` on branch `design/localkarar-18`:

1. **Push & Device Tokens (`NOT_PRESENT`)**:
   - `prisma/schema.prisma`: No `DeviceToken`, `PushSubscription`, or `UserDevice` model.
   - `src/services/` & `src/routes/`: No `/devices`, `/push/register`, or `/push/unregister` endpoints.
   - Provider libraries: No Firebase Admin SDK, FCM v1, APNs direct HTTP/2, Expo Push, or OneSignal in `package.json`.
2. **In-App Notification Records**:
   - `CommunityNotification`: In-app notification table with `userId`, `actorId`, `type` (`follow`, `message`, `reply`, `like`, `quote`, `thread_invite`), `postId`, `threadId`, `readAt`.
   - `BusinessNotification`: In-app notification table with `workspaceId`, `recordId`, `recipientId`, `scheduledAt`, `channel: 'in_app'`.
   - `AccountNotification`: In-app notification table with `userId`, `type` (`trial_ending`, `payment_succeeded`, `payment_failed`, `membership_cancelled`).
3. **Web Routing Contract (`frontend/src/router/index.jsx`)**:
   - Explicit SPA routing mapped under `/app/*`.

---

## 2. Canonical Web ↔ Mobile Route Mapping Matrix

| Web URL (GitHub Router) | Mobile Destination | Auth Required | Parameter Types |
| :--- | :--- | :--- | :--- |
| `/app/community/gonderi/:postId` | `Destination.CommunityPost(postId)` | `true` | `postId: String` |
| `/app/community/sohbetler` | `Destination.CommunityThreadDetail(threadId)` | `true` | `threadId: String` |
| `/app/community/topluluk` | `Destination.Community` | `true` | *none* |
| `/app/community/kisiler` | `Destination.CommunityProfile(userId)` | `true` | `userId: Int` |
| `/app/profil` | `Destination.Profile` | `true` | *none* |
| `/app/profil/:userId` | `Destination.CommunityProfile(userId)` | `true` | `userId: Int` |
| `/app/bildirimler` | `Destination.CommunityNotifications` | `true` | *none* |
| `/app/mentor` | `Destination.AiMentor` | `true` | *none* |
| `/app/mentor/conversations/:id` | `Destination.Conversation(conversationId)` | `true` | `conversationId: Int` |
| `/app/community` (News mode) | `Destination.News` | `false` | *none* |
| `/app/news/:id` | `Destination.NewsDetail(articleId)` | `false` | `articleId: String` |
| `/app/courses` | `Destination.Courses` | `true` | *none* |
| `/app/courses/:courseId/learn/:lessonId?` | `Destination.LessonReader(courseId, lessonId)` | `true` | `courseId: Int, lessonId: Int` |
| `/app/decision-checks` | `Destination.DecisionTools(initialFilter)` | `true` | `initialFilter: String` |
| `/app/decision-checks/:code` | `Destination.DecisionSession(sessionId)` | `true` | `sessionId: String` |
| `/app/finance/models/:modelCode` | `Destination.FinancialModelDetail(code)` | `true` | `code: String` |
| `/app/workspaces` | `Destination.Workspaces` | `true` | *none* |
| `/app/workspaces/:workspaceId/overview` | `Destination.WorkspaceHome(workspaceId)` | `true` | `workspaceId: String` |
| `/app/workspaces/:workspaceId/tracker` | `Destination.Records(workspaceId)` | `true` | `workspaceId: String` |
| `/app/workspaces/:workspaceId/orders` | `Destination.Orders(workspaceId)` | `true` | `workspaceId: String` |
| `/app/workspaces/:workspaceId/products` | `Destination.Products(workspaceId)` | `true` | `workspaceId: String` |
| `/app/workspaces/:workspaceId/calendar` | `Destination.Calendar(workspaceId)` | `true` | `workspaceId: String` |
| `/app/workspaces/:workspaceId/documents` | `Destination.Documents(workspaceId)` | `true` | `workspaceId: String` |
| `/app/workspaces/:workspaceId/notifications` | `Destination.Notifications(workspaceId)` | `true` | `workspaceId: String` |
| `/app/workspaces/:workspaceId/team` | `Destination.Team(workspaceId)` | `true` | `workspaceId: String` |
| `/app/workspaces/:workspaceId/contacts` | `Destination.Contacts(workspaceId)` | `true` | `workspaceId: String` |
| `/app/workspaces/:workspaceId/settings` | `Destination.WorkspaceSettings(workspaceId)` | `true` | `workspaceId: String` |
| `/app/workspaces/:workspaceId/activity` | `Destination.Activity(workspaceId)` | `true` | `workspaceId: String` |
| `/app/settings` | `Destination.Settings` | `true` | *none* |

---

## 3. Implementation Requirements for M10

1. **Backend (Remote Repo PR)**:
   - Add `DeviceToken` table in Prisma schema.
   - Add `POST /devices` and `DELETE /devices/:token` endpoints.
2. **Mobile Common**:
   - Add `DeepLinkParser` with support for both `https://localkarar.com/app/*` and `localkarar://*`.
   - Add `pendingDestination` storage in `NavController` with session-gating handoff.
3. **Mobile Android**:
   - Add `POST_NOTIFICATIONS` runtime permission handling.
   - Add Intent filters to `AndroidManifest.xml` for URL schemes and App Links.
4. **Mobile iOS**:
   - Add `.onOpenURL` handling in `iOSApp.swift`.
   - Add `UNUserNotificationCenterDelegate` stub in Xcode project.
