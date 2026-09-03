package com.localkarar.app.network

/**
 * API environment configuration for Android, iOS and Production.
 *
 * Semantic states:
 *  - DEBUG_ANDROID -> http://10.0.2.2:3000 (Android emulator host loopback)
 *  - DEBUG_IOS     -> http://localhost:3000 (iOS simulator host loopback)
 *  - PRODUCTION    -> https://localkarar.com (Real HTTPS backend)
 *
 * PRODUCTION ADRESI NEDEN KOK ADRES:
 *
 * Burasi bir zamanlar `https://api.localkarar.com` diyordu ve boyle bir host
 * HIC VAR OLMADI. Sunucudaki ters vekil (deploy/Caddyfile) yalniz iki blok
 * taniyor: `localkarar.com` ve ona kalici yonlenen `www.localkarar.com`.
 * Yani bu satirla derlenen her release build, DNS cozumlemesinde takilip
 * TEK BIR istegi bile tamamlayamazdi.
 *
 * Alt alan adi acmak yerine kok adres secildi: sunucuda degisiklik, yeni DNS
 * kaydi ve sertifika kapsami gerektirmiyor. Backend rotalari zaten kokte
 * (`/auth`, `/workspaces`, `/marketplace/...` -- `/api` oneki YOK) ve web
 * SPA'sinin yedek yoluyla cakismiyor; ikisi ayni Fastify ornegi tarafindan
 * sunuluyor.
 *
 * DIKKAT: bilinmeyen bir API OLMAYAN yol SPA yedegine dusup 200 + HTML
 * donuyor. Yanlis yazilmis bir yol burada 404 olarak GORUNMEZ. HttpClient'taki
 * content-type bekcisi bu durumu yakaliyor -- kaldirilmamali.
 */
const val PRODUCTION_API_URL = "https://localkarar.com"
const val ANDROID_DEV_API_URL = "http://10.0.2.2:3000"
const val IOS_DEV_API_URL = "http://localhost:3000"
const val DEVELOPMENT_API_URL = ANDROID_DEV_API_URL

object ApiConfig {
    val baseUrl: String get() = AppEnvironmentProvider.baseUrl
}
