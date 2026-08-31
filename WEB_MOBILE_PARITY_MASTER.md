# Web <-> Mobile Parity Master Matrix

This document maps all LocalKarar Web routes to their corresponding Mobile destinations and tracks their parity status under the locked **Native Primary Navigation (V1)** architecture.

*Do NOT label ALIGNED before actual visual/runtime comparison.*

---

## 0. Native Primary Navigation Architecture (V1)

**LOCKED PRIMARY BOTTOM NAVIGATION:**
1. **Ana Sayfa** (`Destination.Home`)
2. **İşletme Takibi** (`Destination.WorkspaceHome` / `Destination.Workspaces`)
3. **Topluluk** (`Destination.Community`)
4. **Hesaplamalar** (`Destination.Calculations`)
5. **Ayarlar** (`Destination.Settings`)

**GLOBAL PRODUCT CENTER (LAUNCHER MODAL):**
Accessible from app header / launcher button with 4 semantic groups:
- **KARAR VER:** Karar Araçları (`Destination.DecisionTools`), Hesaplamalar (`Destination.Calculations`)
- **ÖĞREN:** Kurslar (`Destination.Courses`), AI Mentor (`Destination.AiMentor`)
- **TAKİP ET:** Haberler (`Destination.News`), İşletme Takibi (`Destination.WorkspaceHome` / `Destination.Workspaces`)
- **SOSYAL:** Topluluk (`Destination.Community`), Profil (`Destination.Profile`)

---

## 1. Ana Sayfa (Dashboard)
**WEB**
- **Route:** `/app/dashboard`
- **Component:** `Dashboard.jsx`
- **Major Sections:** Bugünkü durum, business status, Tahsilat, Ödeme, Net görünüm, Sıradaki işler, Kaldığın yer, Son kararlar

**MOBILE**
- **Destination:** `Destination.Home` (Primary Bottom Tab #1)
- **Screen:** `HomeScreen`
- **Major Sections:** Header with quick action buttons (Hesapla, Mentor, Karar Ver), StatusPanel, TasksPanel, ResumePanel, DecisionsPanel.
- **State:** `FUNCTIONALLY_IMPLEMENTED`

---

## 2. İşletme Takibi (Business Tracking & Workspace 11 Sections)
**WEB**
- **Route:** `/app/workspaces/:workspaceId/*`
- **Component:** `WorkspaceLayout.jsx` and subcomponents
- **11 Sections:** Genel Bakış, Kayıtlar, Siparişler, Ürünler, Belgeler, Bildirimler, Takvim, Ekip, Kişiler, Aktiviteler, Ayarlar

**MOBILE**
- **Destination:** `Destination.WorkspaceHome` / `Destination.Workspaces` (Primary Bottom Tab #2)
- **Selector:** Native `WorkspaceSectionSheet` modal grouping 11 sections into 5 domains:
  - *GENEL:* Genel Bakış, Kayıtlar
  - *TİCARET:* Siparişler, Ürünler (Commerce placeholders ready)
  - *OPERASYON:* Belgeler, Takvim, Bildirimler
  - *İNSANLAR:* Ekip, Kişiler
  - *YÖNETİM:* Aktiviteler, İşletme Ayarları
- **State:** `FUNCTIONALLY_IMPLEMENTED`

---

## 3. Topluluk (Community)
**WEB**
- **Route:** `/app/community/topluluk`
- **Component:** `CommunityPage.jsx`
- **Major Sections:** Akış, Gönderi Detayı, Profil, Kişiler, Sohbetler

**MOBILE**
- **Destination:** `Destination.Community`, `Destination.CommunityPost` (Primary Bottom Tab #3)
- **Screen:** `CommunityFeedScreen`, `CommunityPostDetailScreen`
- **Internal Sub-tabs:** Akış (Feed with All/Official/User filters), Kişiler (Members), Sohbetler (Chats), Profil (User profile)
- **State:** `FUNCTIONALLY_IMPLEMENTED`

---

## 4. Hesaplamalar (Calculations)
**WEB**
- **Route:** `/app/calculations`, `/app/finance/models/:modelCode`
- **Component:** `ToolsPage.jsx`, `FinancialModelWorkspace.jsx`

**MOBILE**
- **Destination:** `Destination.Calculations`, `Destination.FinancialModelDetail`, `Destination.FormulaDetail`, `Destination.ModelRuns`, `Destination.RunDetail` (Primary Bottom Tab #4)
- **Screen:** `CalculationsScreen`, `FormulaDetailScreen`, `FinancialModelScreen`, `ModelRunsScreen`, `RunDetailScreen`
- **Sections:** Katalog (34 unified calculations), Finansal Görünüm, Geçmiş (history with reopen), Quick formula flow, Detailed model flow (7 tabs)
- **State:** `FUNCTIONALLY_IMPLEMENTED`

---

## 5. Ayarlar (Settings)
**WEB**
- **Route:** `/app/settings`
- **Component:** `SettingsPage.jsx`

**MOBILE**
- **Destination:** `Destination.Settings`, `Destination.Profile`, `Destination.PasswordChange`, `Destination.EmailChange`, `Destination.DeleteAccount` (Primary Bottom Tab #5)
- **Screen:** `SettingsScreen`, `ProfileScreen`, `PasswordChangeScreen`, etc.
- **Sections:** Profil ve İşletme, Güvenlik ve Gizlilik, Uygulama ve Tercihler, Hesap Silme, Çıkış Yap
- **State:** `FUNCTIONALLY_IMPLEMENTED`

---

## 6. Secondary Modules (Global Product Center & Contextual Access)

| Module | Primary Destination | Header Launcher | Home Card / Shortcut |
| :--- | :--- | :--- | :--- |
| **Kurslar** | `Destination.Courses` | ÖĞREN → Kurslar | ResumePanel ("Kaldığın yer") |
| **Karar Araçları** | `Destination.DecisionTools` | KARAR VER → Karar Araçları | Quick Action ("Karar Ver") + DecisionsPanel |
| **AI Mentor** | `Destination.AiMentor` | ÖĞREN → AI Mentor | Quick Action ("Mentor") |
| **Haberler** | `Destination.News` | TAKİP ET → Haberler | Contextual Links |
