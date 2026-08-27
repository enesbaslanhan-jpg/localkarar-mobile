# Mobile Shell V2 — Navigation & Shell Architecture Audit

**Release Package:** M1 — Mobile Shell V2  
**Framework:** Kotlin Multiplatform + Compose Multiplatform (Android + iOS)  
**Status:** FUNCTIONALLY_IMPLEMENTED (Build Passing)  
**Date:** 2026-08-27  

---

## 1. Executive Summary

This document audits the migration of the LocalKarar native mobile application from the legacy 5-tab prototype (`Ana Sayfa, Kurslar, Karar, Mentor, Menü`) to the locked **Native Primary Navigation (V1)** architecture.

### Primary Bottom Navigation (LOCKED):
1. **Ana Sayfa** (`Destination.Home`) — Operational dashboard, daily status, quick actions.
2. **İşletme Takibi** (`Destination.WorkspaceHome` / `Destination.Workspaces`) — Business tracker, 11 grouped sections.
3. **Topluluk** (`Destination.Community`) — Community feed, members, direct chats, user profile.
4. **Hesaplamalar** (`Destination.Calculations`) — Financial calculators, formula catalog, model runs.
5. **Ayarlar** (`Destination.Settings`) — First-class settings, profile, security, account management.

---

## 2. Navigation Architecture Mapping

| Module / Feature | Legacy Route / Access | New M1 Shell Access Pathway | Navigation Target |
| :--- | :--- | :--- | :--- |
| **Ana Sayfa** | Bottom Tab #1 | **Bottom Tab #1** | `Destination.Home` |
| **İşletme Takibi** | Menu drawer item | **Bottom Tab #2** + Product Center + Home Card | `Destination.WorkspaceHome(activeId)` / `Workspaces` |
| **Topluluk** | Menu drawer item | **Bottom Tab #3** + Product Center | `Destination.Community` |
| **Hesaplamalar** | Menu drawer item | **Bottom Tab #4** + Product Center + Home Action | `Destination.Calculations` |
| **Ayarlar** | Menu drawer item | **Bottom Tab #5** + Product Center | `Destination.Settings` |
| **Kurslar** | Bottom Tab #2 (Legacy) | **Product Center** (ÖĞREN) + Home Resume Card | `Destination.Courses` |
| **Karar Araçları** | Bottom Tab #3 (Legacy) | **Product Center** (KARAR VER) + Home Decisions Card | `Destination.DecisionTools()` |
| **AI Mentor** | Bottom Tab #4 (Legacy) | **Product Center** (ÖĞREN) + Home Quick Action | `Destination.AiMentor` |
| **Haberler** | Menu drawer item | **Product Center** (TAKİP ET) | `Destination.News` |

---

## 3. Global Product Center (Launcher Modal)

Accessible anywhere via the app header / launcher icon, grouping all platform tools into 4 semantic domains:

```
┌─────────────────────────────────────────────────────────────┐
│                      Ürün Merkezi                           │
├──────────────────────────────┬──────────────────────────────┤
│ ⚖️ KARAR VER                 │ 🎓 ÖĞREN                     │
│  • Karar Araçları            │  • Kurslar                   │
│  • Hesaplamalar              │  • AI Mentor                 │
├──────────────────────────────┼──────────────────────────────┤
│ 📈 TAKİP ET                  │ 👥 SOSYAL                    │
│  • Haberler                  │  • Topluluk                  │
│  • İşletme Takibi            │  • Profil                    │
└──────────────────────────────┴──────────────────────────────┘
```

---

## 4. Workspace 11-Section Selector Parity

The native mobile shell supports all 11 Web Workspace sections via the native `WorkspaceSectionSheet`:

1. **GENEL**
   - *Genel Bakış* (`Destination.WorkspaceHome`)
   - *Kayıtlar* (`Destination.Records`)
2. **TİCARET**
   - *Siparişler* (`Destination.Orders` — Commerce placeholder)
   - *Ürünler* (`Destination.Products` — Commerce placeholder)
3. **OPERASYON**
   - *Belgeler* (`Destination.Documents`)
   - *Takvim* (`Destination.Calendar`)
   - *Bildirimler* (`Destination.Notifications`)
4. **İNSANLAR**
   - *Ekip* (`Destination.Team`)
   - *Kişiler* (`Destination.Contacts`)
5. **YÖNETİM**
   - *Aktiviteler* (`Destination.Activity`)
   - *İşletme Ayarları* (`Destination.WorkspaceSettings`)

---

## 5. Topluluk (Community) Sub-Navigation

Topluluk includes clean internal sub-navigation:
- **Akış** (Feed with All / Official / User filters & compose FAB)
- **Kişiler** (Members & ecosystem contacts)
- **Sohbetler** (Direct messages & conversations)
- **Profil** (User community profile & badges)

---

## 6. Shared Compatibility & Insets

- **Shared Core:** `commonMain` handles all typed navigation, destination hierarchy, and layout definitions.
- **System Insets:** Inset-aware layouts (`WindowInsets.statusBars`, `safeDrawing`) ensure edge-to-edge compatibility on Android 14/15 and iOS devices.
- **Stack Behavior:** Primary tab selections maintain a clean root stack, preventing stack explosion or redundant copies.
