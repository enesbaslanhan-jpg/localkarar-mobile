# Development Status

## Stabilization & Navigation Baseline
The `feature/m1-mobile-shell-v2` branch establishes the **Native Primary Navigation (V1)** locked architecture and **M2 Production + Auth Foundation** for LocalKarar Compose Multiplatform.

## Current Status of Feature Integration

| Feature / Phase | Status | Notes |
| :--- | :--- | :--- |
| **M0 - Safety Baseline** | COMPLETE | Working tree clean, baseline build passed, dedicated feature branch created |
| **M0.1 - Mobile Inventory Audit** | COMPLETE | Documented in `MOBILE_SHELL_V2_AUDIT.md` |
| **M1 - Native Primary Bottom Navigation** | **FUNCTIONALLY_IMPLEMENTED** | Locked 5 tabs: Ana Sayfa, İşletme Takibi, Topluluk, Hesaplamalar, Ayarlar |
| **M1 - Global Product Center** | **FUNCTIONALLY_IMPLEMENTED** | Top bar launcher modal with 4 semantic groups (KARAR VER, ÖĞREN, TAKİP ET, SOSYAL) |
| **M1 - Workspace Section Selector** | **FUNCTIONALLY_IMPLEMENTED** | Native modal sheet supporting all 11 Web sections grouped into 5 domains |
| **M1 - Topluluk Sub-Navigation** | **FUNCTIONALLY_IMPLEMENTED** | Internal sub-tabs: Akış (Feed), Kişiler, Sohbetler, Profil |
| **M1 - Ayarlar Hub** | **FUNCTIONALLY_IMPLEMENTED** | First-class settings destination with categorized profile, security, app & account flows |
| **M2 - Demo Auth Removal** | **COMPLETE** | Hardcoded tokens, student@localakademi.com demo bypass and fake users removed |
| **M2 - Native Register Flow** | **FUNCTIONALLY_IMPLEMENTED** | Name, email, password validation, legal consent, Turkish error mapping |
| **M2 - Password Reset (Forgot / Confirm)** | **FUNCTIONALLY_IMPLEMENTED** | Anti-enumeration email request + token confirmation screens |
| **M2 - Platform Environment Resolution** | **COMPLETE** | Android debug (10.0.2.2), iOS debug (localhost:3000), Production (api.localkarar.com) |
| **M2 - iOS Identity & Bundle Setup** | **COMPLETE** | `com.localkarar.app`, App Name `LocalKarar` in `Config.xcconfig` |
| **M2 - Raw Error UX Safety** | **COMPLETE** | Intercepts HTTP/JSON error responses and translates to safe Turkish messages |
| **Secondary Module Regression** | **FUNCTIONALLY_IMPLEMENTED** | Courses, Decision Tools, AI Mentor, News fully accessible via Product Center & Home |
| **Hesaplamalar Unified Catalog** | **FUNCTIONALLY_IMPLEMENTED** | Unified catalog, 34 items, 7 categories, formula quick & detailed model flows |
| **Ticaret Module (Orders & Products)** | **PLACEHOLDER_READY** | Architecture and route parity preserved for upcoming Commerce Package |

## Active Navigation Architecture (V1)
- `Destination.Home` → **Ana Sayfa** (Primary Tab 1)
- `Destination.WorkspaceHome` / `Destination.Workspaces` → **İşletme Takibi** (Primary Tab 2)
- `Destination.Community` → **Topluluk** (Primary Tab 3)
- `Destination.Calculations` → **Hesaplamalar** (Primary Tab 4)
- `Destination.Settings` → **Ayarlar** (Primary Tab 5)
