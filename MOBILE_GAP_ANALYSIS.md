# LocalKarar Mobile Gap Analysis

This document identifies potential gaps, missing features, and necessary backend adjustments required to support a production-grade mobile application for LocalKarar, based on the audit of the current web/backend implementation.

## 1. Authentication & Session Management
- **Current State:** The backend uses standard JWT authentication with an 8-hour expiry (`/auth/login`).
- **Gap:** Mobile applications typically require long-lived sessions to prevent users from being logged out daily. There is currently no `Refresh Token` implementation in the `auth.ts` service.
- **Recommendation:** Implement a Refresh Token mechanism or extend the JWT expiry for mobile clients (e.g., via a specific mobile login flow) to ensure a seamless mobile experience.

## 2. Push Notifications
- **Current State:** The backend handles "News" and "Workspaces" but lacks an infrastructure for mobile push notifications.
- **Gap:** There are no endpoints to register device tokens (e.g., FCM / APNs tokens) and no background jobs set up to push targeted alerts to devices.
- **Recommendation:** Create a `DeviceToken` model in Prisma and add endpoints for the mobile app to register/unregister for push notifications. Update the `business-reminder-worker` and `news/worker` to trigger pushes.

## 3. Offline Capabilities & Sync
- **Current State:** The web application is primarily an SPA that depends on always-on connectivity.
- **Gap:** Mobile users may expect certain features (like Course progression or reading Knowledge Objects) to work offline. Currently, there are no Delta Sync endpoints (returning only what changed since the last fetch) to efficiently synchronize data.
- **Recommendation:** Phase 1 of the mobile app will be strictly online-only. If offline support is desired later, a sync layer must be added to the backend.

## 4. App Version Enforcement (Force Update)
- **Current State:** The backend `/health` endpoint exposes a version, but it's not explicitly structured for mobile force-update logic.
- **Gap:** We need a way to block outdated mobile app versions from accessing the API if breaking changes are introduced.
- **Recommendation:** Add a mobile configuration endpoint (e.g., `/api/v1/mobile/config`) that returns the minimum supported app version, allowing the app to trigger a "Force Update" screen.

## 5. Streaming Endpoints
- **Current State:** AI Mentor uses `/mentor/conversations/:id/messages/stream` (SSE - Server-Sent Events).
- **Gap:** KMP networking libraries (like Ktor) need careful configuration to handle SSE efficiently on both Android and iOS without dropping connections or leaking memory.
- **Recommendation:** Ensure the mobile networking layer is explicitly built to handle the SSE standard robustly.

## 6. Deep Linking
- **Current State:** Frontend uses standard browser routing (React Router).
- **Gap:** Mobile needs explicit Deep Link mapping (e.g., linking directly into a `DecisionCheck` or `FinancialModelRun` from an email).
- **Recommendation:** Align the Android `intent-filter` and iOS `Universal Links` structure with the existing web paths, and handle them gracefully in the mobile navigation graph.

## 7. Dashboard Engine
### Missing
True business-status/financial summary data (Ciro, Nakit, vs) for Home.
Server-generated business insight engine.

### Existing
Learning progress metrics (Active courses, completed, progress).
Upcoming tasks / resume item.

- **Gap:** There is no explicit 'Bugün' (Today's Insight) backend engine, and there are no direct financial metrics returned by the root dashboard.
- **Recommendation:** If a financial summary widget or AI insight widget is required on the mobile home screen, a dedicated backend endpoint must be developed, or the existing `/dashboard` endpoint needs to aggregate those business metrics.

## 8. Dashboard `resumeItem` Deep Linking
- **Current State:** The `/dashboard` endpoint returns a `resumeItem` which represents the last active course. It only contains the `courseId`.
- **Gap:** It does not contain the exact `lessonId` the user was viewing. Therefore, a mobile "Devam Et" action can only deep link to the `CourseDetailScreen`, not directly to the `LessonReaderScreen`. The `CourseDetailScreen` will then fetch the course details and route the user to their last viewed lesson, adding an extra network roundtrip.
- **Recommendation:** Update the `/dashboard` `resumeItem` response to include `lastViewedLessonId` from the `LessonProgress` table, so the mobile client can route the user directly to the lesson.
