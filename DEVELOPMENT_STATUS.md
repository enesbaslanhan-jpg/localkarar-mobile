# Development Status

## Stabilization & Navigation Baseline
The `feature/m3-business-tracker-v2` branch establishes the **Native Primary Navigation (V1)**, **M2 Production Auth**, and **M3 Business Tracker V2 (Orders + Products + 11-Section Parity)** for LocalKarar Compose Multiplatform.

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
| **M2 - Demo Auth Removal** | **COMPLETE** | Hardcoded tokens, demo student bypass and fake users removed |
| **M2 - Native Register & Password Reset** | **FUNCTIONALLY_IMPLEMENTED** | Validations, anti-enumeration reset request, Turkish error mapping |
| **M2 - Cross-Platform Environments** | **COMPLETE** | Android debug (10.0.2.2), iOS debug (localhost:3000), Production (api.localkarar.com) |
| **M3 - İşletme Takibi 11 Bölüm Paritesi** | **FUNCTIONALLY_IMPLEMENTED** | 11 bölümün tamamı eksiksiz bağlandı ve gerçeğe uygun modellendi |
| **M3 - Siparişler (Orders)** | **FUNCTIONALLY_IMPLEMENTED** | Sipariş kartları, metrikler, filtreler, arama, durum güncelleme ve oluşturma |
| **M3 - Ürünler (Products)** | **FUNCTIONALLY_IMPLEMENTED** | Katalog, fiyat/maliyet/marj hesaplama, kritik stok uyarıları, düzenleme/ekleme |
| **Hesaplamalar Unified Catalog** | **FUNCTIONALLY_IMPLEMENTED** | 34 hesaplama, formül & detaylı model akışları |

## Active Navigation Architecture (V1)
- `Destination.Home` → **Ana Sayfa** (Primary Tab 1)
- `Destination.WorkspaceHome` / `Destination.Workspaces` → **İşletme Takibi** (Primary Tab 2)
- `Destination.Community` → **Topluluk** (Primary Tab 3)
- `Destination.Calculations` → **Hesaplamalar** (Primary Tab 4)
- `Destination.Settings` → **Ayarlar** (Primary Tab 5)
