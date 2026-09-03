package com.localkarar.app.network.dto

import kotlinx.serialization.Serializable

/**
 * PAZARYERI ENTEGRASYON YASAM DONGUSU.
 *
 * NEDEN SONRADAN EKLENDI:
 *
 * Mobil, sunucudaki entegrasyon uclarindan yalnizca ikisini cagiriyordu:
 * `trendyol/status` ve `trendyol/sync`. Kullanici mobilden HICBIR pazaryeri
 * baglayamiyordu. Bu yuzden gercek veri hicbir zaman gelmiyor, repository de
 * uydurma veriye dusuyordu ve o uydurma veri "makul" gorundugu icin eksiklik
 * fark edilmiyordu.
 *
 * Sunucuda dort saglayici icin tam dongu ZATEN hazirdi
 * (src/services/integrations/marketplace-routes.ts).
 */

/** GET /integrations/marketplaces */
@Serializable
data class MarketplaceCatalogDto(
    val marketplaces: List<MarketplaceEntryDto> = emptyList()
)

@Serializable
data class MarketplaceEntryDto(
    val provider: String,
    val label: String,
    val enabled: Boolean = false,
    /** Amazon icin true: SP-API gelistirici onayi olmadan gercek bagdastirici yok. */
    val comingSoon: Boolean = false,
    val capabilities: MarketplaceCapabilitiesDto = MarketplaceCapabilitiesDto()
)

@Serializable
data class MarketplaceCapabilitiesDto(
    val supportsProductViews: Boolean = false,
    val supportsFavorites: Boolean = false,
    val supportsProductAnalytics: Boolean = false
)

/** GET /integrations?workspaceId=... */
@Serializable
data class WorkspaceIntegrationsDto(
    val connections: List<IntegrationConnectionDto> = emptyList(),
    val marketplaces: List<MarketplaceEntryDto> = emptyList()
)

/**
 * `publicConnectionView` ciktisi (credentials.ts).
 *
 * ⚠️ Kimlik bilgileri (apiKey/apiSecret) BU YANITTA YOK ve olmamali; sunucu
 * onlari sifreli saklayip disari hic vermiyor. Mobilde de saklanmiyorlar.
 */
@Serializable
data class IntegrationConnectionDto(
    val id: String,
    val provider: String,
    val status: String,
    val externalAccountId: String? = null,
    val displayName: String? = null,
    val lastSyncedAt: String? = null,
    val createdAt: String? = null,
    val consecutiveFailureCount: Int = 0
)

// ---------------------------------------------------------------------------
// Baglanma istekleri.
//
// Her saglayicinin kimlik modeli FARKLI ve sunucu bunlari ayri zod semalariyla
// dogruluyor. Tek bir "genel" istek nesnesi kullanmak, alan adlarini yanlis
// gonderip 422 almanin en kolay yolu olurdu.
// ---------------------------------------------------------------------------

/** POST /integrations/trendyol/connect */
@Serializable
data class TrendyolConnectRequestDto(
    val workspaceId: String,
    /** Sunucu sayisal olmasini sart kosuyor (regex ^\d+$). */
    val merchantId: String,
    val apiKey: String,
    val apiSecret: String,
    val displayName: String? = null
)

/** POST /integrations/hepsiburada/connect */
@Serializable
data class HepsiburadaConnectRequestDto(
    val workspaceId: String,
    val merchantId: String,
    val username: String,
    val password: String,
    val displayName: String? = null
)

/** POST /integrations/n11/connect */
@Serializable
data class N11ConnectRequestDto(
    val workspaceId: String,
    val storeName: String,
    val appKey: String,
    val appSecret: String,
    val displayName: String? = null
)

/**
 * POST /integrations/shopify/connect
 *
 * Shopify OAuth ile baglaniyor: sunucu bir yetkilendirme adresi donuyor ve
 * kullanicinin tarayicida onay vermesi gerekiyor.
 */
@Serializable
data class ShopifyConnectRequestDto(
    val workspaceId: String,
    val shopDomain: String
)

@Serializable
data class ShopifyConnectResponseDto(
    val authorizationUrl: String? = null
)
