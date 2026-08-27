# Web <-> Mobile Parity Master Matrix

This document maps all LocalKarar Web routes to their corresponding Android Mobile destinations and tracks their parity status.

*Do NOT label ALIGNED before actual visual/runtime comparison.*

## 1. Ana Sayfa (Dashboard)
**WEB**
- **Route:** `/app/dashboard`
- **Component:** `Dashboard.jsx`
- **Major Sections:** Bugünkü durum, business status, Tahsilat, Ödeme, Net görünüm, Sıradaki işler, Kaldığın yer, Son kararlar
- **Order:** Status Header -> Quick Metrics -> Upcoming -> Continue Learning -> Recent Decisions
- **Actions:** Continue Course, Open Decision, View All
- **Data Source:** Backend API `/api/v1/dashboard/me` (to be verified)
- **Labels:** Web terminology for financial status, learning state
- **Visual Structure:** Metric cards, status alerts, rows for decisions

**ANDROID**
- **Destination:** `Destination.Home`
- **Screen:** `HomeScreen`
- **Major Sections:** Pending deep audit (Phase 4)
- **Actions:** Pending deep audit
- **State Variations:** Loading, Success, Error
- **Data Source:** `SafeApiClient` fetching `/api/v1/dashboard/me` (to be verified)

**STATUS:** `RUNTIME_GAP` / `STRUCTURAL_GAP` (Pending Phase 4 Audit)

---

## 2. Kurslar (Courses)
**WEB**
- **Route:** `/app/courses`, `/app/courses/:courseId/learn/:lessonId?`
- **Component:** `CoursesPage.jsx`, `CoursePlayerPage.jsx`
- **Major Sections:** Active learning hero, Progress bar, Category filters, Course catalog, Competency panel
- **Actions:** Derse devam et, Filter, Start Course
- **Data Source:** Backend `/api/v1/courses`
- **Visual Structure:** Grid of course cards, hero section, side panel for competencies

**ANDROID**
- **Destination:** `Destination.Courses`, `Destination.CourseDetail`, `Destination.LessonReader`
- **Screen:** `CoursesScreen`, `CourseDetailScreen`, `LessonReaderScreen`
- **Major Sections:** Pending deep audit (Phase 5 & 6)
- **Actions:** Pending deep audit
- **Data Source:** `SafeApiClient`

**STATUS:** `STRUCTURAL_GAP` (Pending Phase 5 & 6 Audit)

---

## 3. Karar Araçları (Decision Tools)
**WEB**
- **Route:** `/app/decision-checks`, `/app/decision-checks/:code`
- **Component:** `DecisionCheckList.jsx`, `DecisionCheckSession.jsx`
- **Major Sections:** Recommended tool, full list, categories, session state, Son oturumlar, Geçmiş kararlar
- **Actions:** Devam Et, Sonucu Gör, Aracı Aç
- **Data Source:** `/api/v1/decision-checks`
- **Visual Structure:** Grid of tools, dynamic form based on schema, result panels

**ANDROID**
- **Destination:** `Destination.DecisionTools`, `Destination.DecisionSession`, `Destination.DecisionHistory`
- **Screen:** `DecisionToolsScreen`, `DecisionSessionScreen`, `DecisionHistoryScreen`
- **Major Sections:** Pending deep audit (Phase 7-11)
- **Actions:** Pending deep audit
- **Data Source:** `SafeApiClient`

**STATUS:** `STRUCTURAL_GAP` / `DATA_GAP` (Pending Phase 7-11 Audit)

---

## 4. Hesaplamalar (Calculations)
**WEB**
- **Route:** `/app/calculations`, `/app/finance/models/:modelCode`
- **Component:** `ToolsPage.jsx`, `FinancialModelWorkspace.jsx`
- **Major Sections:** İçe aktar, Hesaplama başlat, Gelir gider tahsilat, Fatura ve belgeler, Ödeme takvimi
- **Actions:** Hızlı hesap, Detaylı analiz, Open
- **Data Source:** `/api/v1/financial-models` (or similar)
- **Visual Structure:** Categories, calculator cards, dynamic workspace

**ANDROID**
- **Destination:** `Destination.Calculations`, `Destination.FinancialModelDetail`, `Destination.FormulaDetail`
- **Screen:** `CalculationsScreen`, `FormulaDetailScreen`, `FinancialModelScreen`
- **Major Sections:** 
  - Katalog (unified catalog, category chips, quick workspace cards)
  - Finansal Görünüm (tracker summary, open records, overdue, recent calculations)
  - Geçmiş (formula history with clickable reopen)
  - Hızlı Hesaplama (FormulaDetailScreen: dynamic inputs, validation, Turkish formatting, result labels, durum badge)
  - Detaylı Analiz (FinancialModelScreen: 7 tabs - Çalışma Alanı, Girdiler, Senaryolar, Çıktılar, Kontroller, Kaynaklar, Değişiklikler; sensitivity, ethics, scenario comparison)
- **Data Source:** `SafeApiClient` — GET `/api/formulas`, `/api/financial-models`, `/api/formula-calculations`, `/api/workspaces/:id/tracker/summary`, `/api/workspaces/:id/records`, POST `/api/formulas/:id/calculate`, POST `/api/workspaces/:id/financial-models/:code/runs`

**STATUS:** `FUNCTIONALLY_IMPLEMENTED_VISUAL_QA_PENDING` — Unified catalog (34 items), 7 categories, correct mode badges, real Finansal Görünüm via tracker APIs, no Model Lab, quick workspace cards routed, Geçmiş functional with clickable reopen. Quick formula flow complete with dynamic inputs, validation, result labels, durum badge. Detailed model flow complete with 7 tabs, sensitivity display, ethics checks, scenario comparison. Build PASS. Runtime visual verification pending.

---

## 5. İşletme Takibi (Business Tracking)
**WEB**
- **Route:** `/app/workspaces/:workspaceId/*`
- **Component:** `WorkspaceLayout.jsx` and subcomponents
- **Major Sections:** Genel Bakış, Kayıtlar, Belgeler, Bildirimler, Takvim, Ekip, Kişiler, Aktiviteler
- **Actions:** Kayıt ekle, filter, search, upload
- **Data Source:** `/api/v1/workspaces/...`
- **Visual Structure:** Sidebar navigation (Web) mapped to tabs/sections (Mobile)

**ANDROID**
- **Destination:** `Destination.Workspaces`, `Destination.Records`, `Destination.Documents`, `Destination.Calendar`, etc.
- **Screen:** `WorkspaceHomeScreen`, `RecordsScreen`, etc.
- **Major Sections:** Pending deep audit (Phase 17-21)
- **Data Source:** `SafeApiClient`

**STATUS:** `STRUCTURAL_GAP` (Pending Phase 17-21 Audit)

---

## 6. AI Mentor
**WEB**
- **Route:** `/app/mentor`
- **Component:** `MentorPage.jsx`
- **Major Sections:** Conversation list, message history, composer, memory panel, İşlem Önerileri
- **Actions:** SSE streaming, copy, regenerate, feedback, Hafızayı Yönet
- **Data Source:** `/api/v1/mentor` (Streaming)
- **Visual Structure:** Chat interface, side panel for memory

**ANDROID**
- **Destination:** `Destination.AiMentor`, `Destination.Conversation`
- **Screen:** `AiMentorScreen`
- **Major Sections:** Pending deep audit (Phase 22 & 23)
- **Data Source:** Ktor SSE plugin

**STATUS:** `RUNTIME_GAP` (Pending Phase 22 & 23 Audit)

---

## 7. Haberler (News)
**WEB**
- **Route:** `/app/community` (News mode)
- **Component:** `NewsPage.jsx`
- **Major Sections:** Featured article, list, topic filters, tags, official source
- **Actions:** Filter, Open external
- **Data Source:** `/api/v1/news`
- **Visual Structure:** Article cards, featured hero

**ANDROID**
- **Destination:** `Destination.News`, `Destination.NewsDetail`
- **Screen:** `NewsScreen`
- **Major Sections:** Pending deep audit (Phase 24)
- **Data Source:** `SafeApiClient`

**STATUS:** `STRUCTURAL_GAP` (Pending Phase 24 Audit)

---

## 8. Topluluk (Community)
**WEB**
- **Route:** `/app/community/topluluk`
- **Component:** `CommunityPage.jsx`
- **Major Sections:** Gönderi oluştur, feed, Gündemde, Katkı sağlayanlar
- **Actions:** Post, interact (if supported)
- **Data Source:** `/api/v1/community`
- **Visual Structure:** Feed layout

**ANDROID**
- **Destination:** `Destination.Community`, `Destination.CommunityPost`
- **Screen:** `CommunityScreen`
- **Major Sections:** Pending deep audit (Phase 25)
- **Data Source:** `SafeApiClient`

**STATUS:** `STRUCTURAL_GAP` (Pending Phase 25 Audit)

---

## 9. Ayarlar / Profil (Settings)
**WEB**
- **Route:** `/app/settings`
- **Component:** `SettingsPage.jsx`
- **Major Sections:** Profil ve işletme, Bildirimler, Erişilebilirlik, Güvenlik, Veri ve gizlilik
- **Actions:** Update profile, change photo, delete account
- **Data Source:** `/api/v1/users/me`

**ANDROID**
- **Destination:** `Destination.Settings`, `Destination.Profile`, etc.
- **Screen:** `SettingsScreen`
- **Major Sections:** Pending deep audit (Phase 26 & 27)
- **Data Source:** `SafeApiClient`

**STATUS:** `STRUCTURAL_GAP` (Pending Phase 26 & 27 Audit)

---

## 10. Global Navigation & Search
**WEB**
- **Major Sections:** Top bar with Search, Sidebar for navigation
- **Actions:** Search courses, tools, calculations

**ANDROID**
- **Major Sections:** Bottom Navigation (Home, Courses, Decision, Mentor, Menu)
- **Actions:** Tab switching, Menu sheet
- **Search:** Pending audit

**STATUS:** `STRUCTURAL_GAP` (Pending Phase 28 & 29 Audit)
