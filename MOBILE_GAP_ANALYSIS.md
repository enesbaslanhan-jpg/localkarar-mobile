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

## 9. Hesaplamalar Catalog (Phase 12) — **RESOLVED**
- **Previous Gap:** Android displayed separate formulas/models lists, missing category filters, missing mode labels, missing quick workspace cards, Finansal Görünüm showed models instead of tracker data, no unified CalculationItem.
- **Resolution:** 
  - Created `CalculationCatalog.kt` with 34 unified `CalculationItem` entries mirroring Web's `calculationCatalog.js`
  - 7 categories (all, cash, profitability, customer, operations, growth, valuation)
  - Correct mode labels: both→"Hızlı hesap, Detaylı analiz mevcut", simple→"Hızlı hesap", detailed→"İleri analiz"
  - `CalculationsScreen.kt` rewritten with 3 tabs: Katalog, Finansal Görünüm, Geçmiş
  - Katalog: quick workspace cards, category chips, unified cards with mode badges
  - Finansal Görünüm: real tracker summary (receivable/payable/net), open records, overdue, recent calculations, empty state with CTA
  - Geçmiş: formula calculation history via `/api/formula-calculations`
  - Top actions routed: İçe aktar→Documents, Gelir/gider→Records, Fatura→Documents, Ödeme takvimi→Calendar
  - No "Model Laboratuvarı" user-facing references
  - Build PASS, runtime structure verified

## 10. Calculation Workspace / Result (Phase 14) — **RESOLVED**
- **Previous Gap:** Mobile had basic FormulaDetailScreen and FinancialModelScreen but missing Web parity features: result labels mapping, durum/status badge, sensitivity display, ethics checks, scenario comparison, sources tab, version history tab, full 7-tab layout matching Web's FinancialModelWorkspace.
- **Resolution:**
  - **FormulaDetailScreen (Hızlı Hesaplama):**
    - Added `FORMULA_RESULT_LABELS` map matching Web's `RESULT_LABELS` for all 19 formulas
    - Added `durum`/`status` badge with color coding (success/danger/neutral) matching Web's `resultTone` logic
    - Dynamic inputs with validation (required, min, max, Turkish decimal parsing)
    - Backend execution via `POST /formulas/:id/calculate`, history saved server-side
  - **FinancialModelScreen (Detaylı Analiz) - 7 tabs matching Web:**
    1. **Çalışma Alanı** - Split view with inputs rail + output rail, quick run button
    2. **Girdiler** - Full input panel with sourceRequired indicators, run button
    3. **Senaryolar** - 5 scenario selector (Baz, İyimser, Olumsuz, Stres, Özel) with comparison table
    4. **Çıktılar** - Output dashboard with confidence badge, all metrics, sensitivity display
    5. **Kontroller** - Validation checks + ethics checks + calculation trace + confidence components + warnings/limitations
    6. **Kaynaklar** - Methodology sources with authority badges (official/academic/professional)
    7. **Değişiklikler** - Version history from `model.versions` (backend Prisma)
  - Dynamic input rendering for all types: number, integer, number_array with comma-separated parsing
  - Validation: required, min/max, type checking, Turkish number formatting
  - Backend execution via `POST /workspaces/:id/financial-models/:code/runs`, history saved server-side
  - No "Model Laboratuvarı" user-facing references (scenarios under "Senaryolar" tab)
  - Build PASS, runtime structure verified

## 11. Finansal Görünüm (Phase 15) — **RESOLVED**
- **Previous Gap:** Android Finansal Görünüm tab existed but needed verification against current Web ToolsPage.jsx and backend tracker APIs.
- **Resolution:**
  - **Signature Panel:** Headline logic matching Web (empty state → "Finansal görünümünüzü işletme kayıtlarıyla kurun", overdue → count, negative net → "nakit planı gerekiyor", else "kontrollü")
  - **KPIs:** Alacak, Borç, Net (30 gün) from `trackerSummary.nextThirtyDays`
  - **Tahsilat ve ödeme defteri:** Open records (status != completed/cancelled) sorted by dueAt ascending, max 7 shown
  - **İstisnalar:** Overdue records (dueAt < today), max 3 shown with red border
  - **Son hesaplamalar:** Last 4 formula calculations from history
  - **Empty State:** "Finansal görünümünüzü işletme kayıtlarıyla kurun" + "İşletme kaydı ekle" CTA → Workspace Records
  - **Workspace Resolution:** Uses activeWorkspaceId from ActiveWorkspaceStore (shared with Dashboard/İşletme Takibi)
  - **Data Sources:** GET `/api/workspaces/:id/tracker/summary` + GET `/api/workspaces/:id/records`
  - **Build PASS**

## 12. Hesaplamalar Geçmiş (Phase 16) — **RESOLVED**
- **Previous Gap:** Mobile Geçmiş tab showed basic history but lacked Web parity: clickable reopen, formulaResultLabel mapping, 4 entries, 'durum' filter, saved inputs/result not restored on reopen.
- **Resolution:**
  - **Source:** GET `/api/formula-calculations` (user-scoped, ordered desc, limit 50)
  - **Display:** Formula name, timestamp, up to 4 result entries (excluding 'durum'), friendly labels via `formulaResultLabel` map
  - **Click Behavior:** Taps history item → finds formula in catalog by formulaId → navigates to FormulaDetail (Hızlı Hesaplama) with `historicalCalculation` payload
  - **Reopen Semantics:** FormulaCalculatorViewModel initializes with historical calculation → FormulaDetailScreen restores saved inputs into fields and displays saved result immediately (no recalculation needed)
  - **Navigation:** Uses typed `Destination.FormulaDetail(formula, historicalCalculation)` with calculationId passed via navigation, not raw JSON in route
  - **Back Navigation:** Returns to Hesaplamalar → Geçmiş view
  - **Error State:** If formula no longer in catalog, shows Turkish error "Bu hesaplama artık katalogda yok."
  - **Ordering:** Newest first (backend returns desc by createdAt)
  - **No Model Runs:** Financial model runs scoped to FinancialModelWorkspace, not in main Geçmiş tab (matches Web)
  - **Build PASS**
