# Product

<!-- impeccable:product-schema 1 -->

## Platform

adaptive

## Users

LocalKarar is used by investors, tradespeople, small and medium-sized businesses, and businesspeople. They use it while monitoring a real business, evaluating financial choices, learning, and making day-to-day operating decisions.

## Product Purpose

LocalKarar helps people understand the state of a business and make decisions using their own numbers instead of guesswork. The native application is the Android and iOS client of the existing LocalKarar web product, not a separate service. Success means users can complete the same core work on mobile with the same account and business data.

## Positioning

LocalKarar brings business tracking, decision tools, financial calculations and models, learning, AI-supported guidance, news, and a professional community into one connected decision-support product. It is not an accounting, legal, tax, or investment-advisory replacement; the final decision remains with the user.

## Operating Context

Users may check urgent business status, record income or expenses, review cash flow and profitability, evaluate a commercial decision, upload a document, follow orders and products, continue a course, consult the AI Mentor, or participate in the community. Mobile use includes short, interrupted sessions and one-handed phone use as well as longer analytical work.

## Capabilities and Constraints

- The web product is the source of truth for user-facing product scope, terminology, accounts, permissions, backend contracts, and business data.
- Android and iOS have equal priority. Shared product structure and brand must coexist with platform-native navigation, controls, gestures, feedback, safe areas, and accessibility behavior.
- The implementation is Kotlin Multiplatform with Compose Multiplatform shared UI, an Android application, and an iOS application shell.
- The current primary navigation contains five destinations: Ana Sayfa, İşletme Takibi, Topluluk, Hesaplamalar, and Ayarlar. Secondary modules are available through Ürün Merkezi and contextual entry points.
- Light and dark appearances are required.
- Turkish is the canonical product language. Layouts must tolerate long Turkish labels and user-configured text scaling.
- Administrative web surfaces are not currently represented in the native destination map; whether they belong in the mobile product remains an open decision.

## Brand Commitments

- Product name: LocalKarar.
- The canonical product promise is clear, evidence-aware decision support for business users.
- Existing web product content and functionality are authoritative.
- The compass and C/K monogram used by the web product is the incumbent brand mark. Existing native iconography and initials must not silently replace it without an explicit brand decision.

## Evidence on Hand

- Web application: `C:\Users\bugrz\Documents\Codex\2026-07-19\new-chat\outputs\LocalAkademi_fixed`
- Native application: `C:\Users\bugrz\Desktop\localkarar mobil app\LocalKarar-Mobile`
- Native runtime screenshots: `C:\Users\bugrz\Desktop\localkarar mobil app\screenshots`
- Existing brand exports: `C:\Users\bugrz\Desktop\localkarar mobil app\social_media_assets`
- No testimonials, customer logos, performance claims, investment-return claims, or invented financial results may be introduced without verified evidence.

## Product Principles

1. Preserve web capability and business truth while translating interaction to each native platform.
2. Put the user's current business state and next useful decision ahead of feature promotion.
3. Make complex financial work understandable without hiding necessary detail.
4. Keep users in control: recommendations explain their basis and never imply professional advice or guaranteed outcomes.
5. Design for quick mobile checks and deeper analytical sessions without splitting the product into separate experiences.

## Accessibility & Inclusion

Support platform text scaling, screen readers, high contrast, light and dark appearances, reduced motion, safe areas, keyboard/IME visibility, and minimum native touch targets. Information must never rely on color alone. Android and iOS accessibility behavior must be verified independently.
