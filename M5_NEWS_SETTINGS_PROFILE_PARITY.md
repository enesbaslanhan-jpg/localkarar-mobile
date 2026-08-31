# LOCALKARAR MOBILE — M5 NEWS / SETTINGS / ACCOUNT PROFILE PARITY REPORT

**Status:** `M5_COMPLETE_RUNTIME_VERIFIED`  
**Milestone:** M5 — News / Settings / Account Profile Parity & Hardening  
**Target Platform:** Kotlin Multiplatform (Compose Multiplatform Android & iOS)  
**Authoritative Backend:** `https://github.com/enesbaslanhan-jpg/local_akademi` (`design/localkarar-18`, commit `f3f1711a8aa35abed345816cf9492c6ba46b0aa6`)  
**Mobile Branch:** `feature/m5-news-settings-profile`  
**Verification Device:** Pixel 8 API 35 Emulator with live Fastify PostgreSQL backend (Port 3000)

---

## 1. Executive Summary

Milestone M5 successfully delivers complete functional and visual parity for **News (Haberler)**, **Account Settings (Ayarlar)**, **Account Profile Management (Profil Bilgileri)**, **Security (Şifre & E-posta Değiştirme, Diğer Cihazlardan Çık)**, **Legal Consents (Yasal Bilgiler ve Onaylar)**, and **Account Deletion (Hesabımı Sil)**.

Primary navigation remains locked to the 5 standard tabs (`[Ana Sayfa, İşletme Takibi, Topluluk, Hesaplamalar, Ayarlar]`), preserving Haberler as a secondary module accessible via the Product Center (`Ürün Merkezi`). The Account Profile remains strictly decoupled from the M4 Community Social Profile.

---

## 2. Parity & Contract Matrix

| Feature / Screen | Web Reference Contract | Mobile Endpoint / Action | Status | Notes |
|---|---|---|---|---|
| **News Feed** | `GET /api/news` | `GET /api/news` (via `NewsRepository`) | `PARITY_VERIFIED` | Category chips with icons, importance badges, published date, source attribution |
| **News Detail** | `GET /api/news/:id` | `GET /api/news/:id` (via `NewsRepository`) | `PARITY_VERIFIED` | "Neden Önemli?" card, tags, and validated "Kaynakta Aç" external link action |
| **Settings Hub** | `/settings` | `SettingsScreen.kt` | `PARITY_VERIFIED` | Grouped cards (`HESAP`, `İŞLETME`, `GİZLİLİK VE YASAL`, `OTURUM`, `HESAP İŞLEMLERİ`), user avatar initials badge, localized role badge |
| **Role Localization** | `learner`/`student`/`member` | Turkish mapped (`Üye`, `Yönetici`, `İçerik Editörü`) | `PARITY_VERIFIED` | Localized role pills rendered across Settings and Profile |
| **Profile Bilgileri** | `PATCH /auth/profile` (`{ "name": "..." }`) | `PATCH /auth/profile` (`ProfileScreen.kt`) | `PARITY_VERIFIED` | Inline name editing with instant save & local session update, avatar photo upload/remove |
| **E-posta Değiştir** | `PUT /auth/email` (`{ "newEmail", "currentPassword" }`) | `PUT /auth/email` (`EmailChangeScreen.kt`) | `PARITY_VERIFIED` | Password confirmation, new token and user state rotation |
| **Şifre Değiştir** | `PUT /auth/password` (`{ "currentPassword", "newPassword" }`) | `PUT /auth/password` (`PasswordChangeScreen.kt`) | `PARITY_VERIFIED` | Strict 10-character validation, password masking, session rotation |
| **Yasal Onaylar** | `GET /auth/consents`, `POST /auth/consents`, `GET /auth/legal-documents` | `GET /auth/consents`, `POST /auth/consents`, `GET /auth/legal-documents` (`LegalConsentsScreen.kt`) | `PARITY_VERIFIED` | View dynamic legal documents (Kullanım Koşulları, KVKK, Çerez Politikası), consent status indicators, accept action |
| **Diğer Oturumlar** | `POST /auth/logout-all` (`{}`) | `POST /auth/logout-all` (`SettingsScreen.kt`) | `PARITY_VERIFIED` | Confirmation dialog, server session revocation, token rotation, green success banner |
| **Hesabımı Sil** | `DELETE /auth/account` (`{ "password", "confirmation": "HESABIMI SİL" }`) | `DELETE /auth/account` (`DeleteAccountScreen.kt`) | `PARITY_VERIFIED` | Red warning card, password confirmation, exact `"HESABIMI SİL"` match, logout on deletion |
| **Auth Hardening** | Refresh token storage & token version tracking | `SecureStorage` (EncryptedSharedPreferences on Android, Keychain on iOS) | `PARITY_VERIFIED` | Token and refresh token persisted securely across sessions |

---

## 3. Key Bug Fixes & Architecture Hardening

1. **Settings Repository P0 Base URL Fix:**
   - Fixed endpoint prefix in `SettingsRepository.kt` where endpoints were previously calling `/api/auth/...` (which returned `404 Not Found`). Updated to relative canonical `/auth/...` endpoints (`/auth/profile`, `/auth/password`, `/auth/email`, `/auth/consents`, `/auth/logout-all`, `/auth/account`).
2. **Fastify JSON Body Compatibility:**
   - Fastify enforces that `Content-Type: application/json` requests must contain a valid JSON payload string. Fixed `logoutAll` and `acceptConsents` to send `{}` (`setBody("{}")`), resolving HTTP 400 Bad Request errors.
3. **Multiplatform SecureStorage Refresh Token Support:**
   - Extended `SecureStorage` interface with `saveRefreshToken()`, `getRefreshToken()`, and `clearTokens()`.
   - Android: Implemented with `EncryptedSharedPreferences`.
   - iOS: Implemented with Apple Keychain API.
4. **Session Synchronization:**
   - Created `applyNewSession` and `updateUser` methods in `AuthRepository` so that password change, email change, profile update, and logout-all seamlessly synchronize in-memory user and token states.

---

## 4. Android Runtime QA Verification Evidence

All test cases were executed on a Pixel 8 API 35 emulator connected to the live Fastify backend server:

- `m5_qa_04_settings_root.png`: Settings hub displaying "Demo Student", localized role pill `Üye`, user email, and structured navigation cards.
- `m5_qa_05_profile_screen.png`: Profile details screen with avatar initials, email, and localized role.
- `m5_qa_06_edit_name.png` & `m5_qa_13_profile_saved.png`: Inline editing of display name and successful execution of `PATCH /auth/profile`.
- `m5_qa_16_email_change_screen.png`: Email change screen with new email and current password inputs.
- `m5_qa_22_password_screen.png`: Password change screen enforcing the 10-character minimum length constraint.
- `m5_qa_23_consents_screen.png`: Legal consents screen loading live documents (`Kullanım Koşulları`, `Gizlilik ve KVKK`, `Çerez Politikası`) from `/auth/legal-documents` and `/auth/consents`.
- `m5_qa_24_logout_all_dialog.png` & `m5_qa_26_logout_all_success.png`: "Diğer Cihazlardaki Oturumları Kapat" confirmation dialog and subsequent green success banner.
- `m5_qa_30_product_center_sheet.png`: Product Center launcher displaying secondary modules including Haberler.
- `m5_qa_31_news_feed.png`: News feed loading live items from `GET /api/news` with category filter chips, importance badges (`Yüksek Öncelik`, `Önemli`), and formatted publication dates.
- `m5_qa_32_news_detail.png` & `m5_qa_33_news_detail_bottom.png`: News detail screen with article header, summary, "Neden Önemli?" card, tags, and "Kaynakta Aç" external browser launcher.
- `m5_qa_34_back_to_news_feed.png` & `m5_qa_35_back_to_community.png`: Clean top app bar back navigation traversing from News Detail -> News Feed -> Community.
- `m5_qa_36_delete_account.png`: Account deletion screen with irreversible action warning and `"HESABIMI SİL"` confirmation validation.
- `m5_qa_38_logged_out.png`: Clean session teardown upon selecting "Çıkış Yap".
- `m5_qa_55_home_loaded.png` & `m5_qa_56_settings_student.png`: Clean re-authentication and session restoration.

---

## 5. Milestone Conclusion

M5 News, Settings, and Account Profile parity implementation is complete, strictly compliant with authoritative contracts, and runtime-verified on Android with live backend services.

**Final Status:** `M5_COMPLETE_RUNTIME_VERIFIED`
