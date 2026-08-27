# Mobile Auth Parity Matrix (M2)

This document audits and maps the Web/Backend authentication architecture against the LocalKarar Native Mobile implementation (Compose Multiplatform).

---

## 1. Auth Flow Parity Matrix

| Flow | Web Flow / Component | Backend Endpoint | Mobile Implementation | Status |
| :--- | :--- | :--- | :--- | :--- |
| **Login** | `AuthPage.jsx` (mode="login") | `POST /auth/login` | `LoginScreen.kt` + `AuthRepository.login()` | **ALIGNED** |
| **Register** | `AuthPage.jsx` (mode="register") + Terms consent | `POST /auth/register` | `RegisterScreen.kt` + `AuthRepository.register()` | **ALIGNED** |
| **Forgot Password (Request)** | `PasswordResetPage.jsx` (mode="request") | `POST /auth/password-reset/request` | `ForgotPasswordScreen.kt` + `AuthRepository.requestPasswordReset()` | **ALIGNED** |
| **Reset Password (Confirm)** | `PasswordResetPage.jsx` (mode="confirm") | `POST /auth/password-reset/confirm` | `ResetPasswordScreen.kt` + `AuthRepository.confirmPasswordReset()` | **ALIGNED** |
| **Session Restore** | `AuthContext.jsx` (`/auth/me`) | `GET /auth/me` | `AuthRepository.restoreSession()` via `SecureStorage` | **ALIGNED** |
| **Email Verification** | `SettingsPage.jsx` / verification code modal | `POST /auth/email/verify-request`<br>`POST /auth/email/verify-confirm` | `AuthRepository.requestEmailVerification()`<br>`AuthRepository.confirmEmailVerification()` | **ALIGNED** |
| **Password Change (In-App)** | `SettingsPage.jsx` (Şifre Değiştir) | `PUT /auth/password` | `PasswordChangeScreen.kt` + `SettingsRepository` | **ALIGNED** |
| **Email Change (In-App)** | `SettingsPage.jsx` (E-posta Değiştir) | `PUT /auth/email` | `EmailChangeScreen.kt` + `SettingsRepository` | **ALIGNED** |
| **Logout (Single Device)** | Client token purge + state reset | Client-side purge | `AuthViewModel.logout()` (clears `SecureStorage` + resets navigation) | **ALIGNED** |
| **Logout All Devices** | `SettingsPage.jsx` (Tüm Oturumları Kapat) | `POST /auth/logout-all` | `SettingsRepository.logoutAll()` + session refresh | **ALIGNED** |
| **Delete Account** | `SettingsPage.jsx` (Hesabı Sil) | `DELETE /api/users/me` | `DeleteAccountScreen.kt` + `SettingsRepository` | **ALIGNED** |

---

## 2. Security & Anti-Enumeration Rules

1. **Anti-Enumeration on Reset Password:**
   - The backend always returns `200 OK { success: true }` regardless of whether the email exists.
   - The mobile UI displays generic confirmation: `"Eğer adres sistemde kayıtlıysa sıfırlama bağlantısı gönderildi."`
2. **Token Security & Offline Preservation:**
   - On `401 Unauthorized` or explicit token invalidation, the token is purged from `SecureStorage`.
   - On network errors / timeouts, the token is **preserved** to prevent offline startup from destroying active sessions.
3. **No Raw Error Leakage:**
   - Raw Ktor exception traces, stack traces, and unparsed JSON bodies are intercepted in `HttpClient.kt` and mapped to user-friendly Turkish messages.
