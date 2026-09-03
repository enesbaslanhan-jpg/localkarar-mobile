package com.localkarar.app.network

import com.localkarar.app.network.dto.IntegrationStatusDto
import com.localkarar.app.network.dto.OrderDetailWireDto
import com.localkarar.app.network.dto.OrderListWireDto
import com.localkarar.app.network.dto.ProductDetailWireDto
import com.localkarar.app.network.dto.ProductListWireDto
import com.localkarar.app.network.dto.SyncStartedResponseDto
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * PAZARYERI SOZLESME TESTLERI.
 *
 * NEDEN VARLAR:
 *
 * Mobil DTO'lar sunucunun GONDERMEDIGI alanlari zorunlu istiyordu (OrderDto
 * `workspaceId` + `orderNumber`, ProductDto `workspaceId` + non-null `sku`).
 * Sonuc: her BASARILI sunucu yaniti MissingFieldException atiyor, repository
 * bunu "istek basarisiz" sanip uydurma veriye dusuyordu. Siparisler ve Urunler
 * ekranlari aylarca %100 uydurma veri gosterdi ve kimse fark etmedi -- cunku
 * ekran dolu gorunuyordu.
 *
 * Bu testler o sinifi hatayi bir daha sessiz birakmiyor.
 *
 * ⚠️ ASAGIDAKI JSON'LAR ELLE GUZELLESTIRILMEMELI. Her biri sunucunun gercekten
 * urettigi sekli tasiyor:
 *   - orderJson()                (src/services/integrations/marketplace-routes.ts)
 *   - productListItemFromRow()   (src/services/integrations/product-analytics.ts)
 * Sunucu sekli degisirse BURASI DA degismeli; "temiz" ornek yazmak testin
 * degerini tamamen yok eder.
 *
 * ⚠️ Bu testler JVM'de kosuyor, yani R8'siz. R8'in DTO'lari bozup bozmadigini
 * KANITLAMIYORLAR -- onun icin release derlemesiyle gercek bir tur gerekiyor.
 */
class MarketplaceContractTest {

    // Sunucunun ContentNegotiation ayariyla ayni: bilinmeyen anahtarlar
    // yok sayiliyor, EKSIK anahtarlar ise hata veriyor. Test tam da bunu olcuyor.
    private val json = Json { ignoreUnknownKeys = true }

    // ------------------------------------------------------------------
    // GET /marketplace/orders  ->  { orders, total, limit, offset }
    // ------------------------------------------------------------------
    private val siparisListesiJson = """
        {
          "orders": [
            {
              "id": "ord_01",
              "provider": "TRENDYOL",
              "externalId": "TY-778812",
              "externalOrderNumber": "778812",
              "customerDisplayName": "A. Yilmaz",
              "currency": "TRY",
              "grossAmount": 1250.5,
              "discountAmount": 0,
              "commissionAmount": 187.58,
              "shippingAmount": 44.9,
              "refundAmount": null,
              "netContribution": 1018.02,
              "status": "DELIVERED",
              "orderDate": "2026-08-27T11:04:00.000Z",
              "syncedAt": "2026-08-28T02:00:00.000Z",
              "metadata": null,
              "itemCount": 2
            }
          ],
          "total": 1,
          "limit": 100,
          "offset": 0
        }
    """.trimIndent()

    @Test
    fun siparisListesiSunucuSekliyleAyristirilir() {
        val yanit = json.decodeFromString<OrderListWireDto>(siparisListesiJson)

        assertEquals(1, yanit.total)
        assertEquals(1, yanit.orders.size)

        val siparis = yanit.orders.first()
        assertEquals("ord_01", siparis.id)
        assertEquals("TRENDYOL", siparis.provider)

        // Sunucu `externalOrderNumber` gonderiyor; arayuz `orderNumber` okuyor.
        // Bu esleme kopar ve alan zorunlu kalirsa tum liste ayristirilamaz.
        assertEquals("778812", siparis.orderNumber)
        assertEquals("A. Yilmaz", siparis.customerName)

        // commissionAmount / shippingAmount / refundAmount eslemeleri
        assertEquals(187.58, siparis.commission)
        assertEquals(44.9, siparis.shipping)
        assertNull(siparis.refund)

        assertEquals(2, siparis.itemsCount)
        // Liste ucu kalem GONDERMIYOR; bos olmali, uydurulmamali.
        assertTrue(siparis.items.isEmpty())
    }

    /**
     * Bu test ARIZANIN KENDISINI olcuyor.
     *
     * Sunucu yanitinda `workspaceId` ve `orderNumber` ADLARI HIC YOK. DTO bu
     * alanlari zorunlu isterse asagidaki cagri exception atar ve test duser.
     */
    @Test
    fun sunucununGondermedigiAlanlarZorunluDegil() {
        val enAzYanit = """
            {
              "orders": [
                { "id": "ord_02", "provider": "N11", "status": "CREATED" }
              ],
              "total": 1, "limit": 100, "offset": 0
            }
        """.trimIndent()

        val yanit = json.decodeFromString<OrderListWireDto>(enAzYanit)
        val siparis = yanit.orders.first()

        assertEquals("ord_02", siparis.id)
        assertNull(siparis.orderNumber)
        assertNull(siparis.customerName)
        assertNull(siparis.grossAmount)
    }

    // ------------------------------------------------------------------
    // GET /marketplace/orders/:orderId  ->  { order: {...} }  (SARMALAYICI)
    // ------------------------------------------------------------------
    @Test
    fun siparisDetayiSarmalayiciIcindenOkunur() {
        val detayJson = """
            {
              "order": {
                "id": "ord_01",
                "provider": "TRENDYOL",
                "externalOrderNumber": "778812",
                "currency": "TRY",
                "grossAmount": 1250.5,
                "status": "DELIVERED",
                "items": [
                  {
                    "id": "itm_01",
                    "externalProductId": "P-9",
                    "sku": "TY-LPT-99",
                    "barcode": "8681234560012",
                    "title": "Laptop Standi",
                    "quantity": 2,
                    "unitPrice": 450.0,
                    "grossAmount": 900.0,
                    "discountAmount": 0,
                    "commissionAmount": 135.0,
                    "refundAmount": null,
                    "netContribution": 765.0
                  }
                ]
              }
            }
        """.trimIndent()

        val siparis = json.decodeFromString<OrderDetailWireDto>(detayJson).order

        assertEquals("ord_01", siparis.id)
        assertEquals(1, siparis.items.size)

        val kalem = siparis.items.first()
        assertEquals("Laptop Standi", kalem.title)
        assertEquals(2, kalem.quantity)
        // Sunucu kalem toplamini `grossAmount` diye gonderiyor; arayuz
        // `totalPrice` okuyor.
        assertEquals(900.0, kalem.totalPrice)
    }

    // ------------------------------------------------------------------
    // GET /marketplace/products  ->  { products, total, threshold, windowDays }
    // ------------------------------------------------------------------
    private val urunListesiJson = """
        {
          "products": [
            {
              "id": "prd_01",
              "provider": "TRENDYOL",
              "externalId": "TY-P-1",
              "title": "Laptop Standi",
              "brand": null,
              "category": null,
              "sku": null,
              "barcode": "8681234560012",
              "salePrice": 450.0,
              "listPrice": 599.0,
              "stockQuantity": 42,
              "isActive": true,
              "imageUrl": null,
              "syncedAt": "2026-08-28T02:00:00.000Z",
              "lowStock": false,
              "performance": {
                "windowDays": 30,
                "unitsSold": 185,
                "orderCount": 142,
                "grossSales": 83250.0,
                "averageSellingPrice": 450.0,
                "returnedUnits": 4,
                "returnRate": 2.1,
                "commissionTotal": null,
                "shippingTotal": null,
                "refundTotal": null,
                "netContribution": null,
                "financialsAvailable": false
              },
              "internalNote": null,
              "tags": [],
              "lowStockThresholdOverride": null,
              "isFavorite": false
            }
          ],
          "total": 1,
          "threshold": 5,
          "windowDays": 30
        }
    """.trimIndent()

    @Test
    fun urunListesiSunucuSekliyleAyristirilir() {
        val yanit = json.decodeFromString<ProductListWireDto>(urunListesiJson)

        assertEquals(1, yanit.total)
        assertEquals(5, yanit.threshold)

        val urun = yanit.products.first()
        assertEquals("prd_01", urun.id)

        // 🔴 `sku` NULL GELEBILIR. Onceki DTO'da non-null String idi ve tek bir
        // sku'suz urun butun listeyi ayristirilamaz hale getiriyordu.
        assertNull(urun.sku)

        // `stockQuantity` -> `stock` eslemesi
        assertEquals(42, urun.stock)

        // Performans alanlari DUZ DEGIL, IC ICE geliyor.
        assertEquals(185, urun.unitsSold)
        assertEquals(142, urun.orderCount)
        assertEquals(83250.0, urun.grossSales)
        assertEquals(2.1, urun.returnRate)

        // Komisyon/kargo yoksa arayuz "veri yok" gostermeli, sifir degil.
        assertFalse(urun.performance.financialsAvailable)

        // "Indirimde mi" sunucuda alan degil; satis < liste fiyatindan turetiliyor.
        assertTrue(urun.onSale)
    }

    @Test
    fun urunDetayiPerformansiAyriAlandanOkunur() {
        val detayJson = """
            {
              "product": {
                "id": "prd_01",
                "provider": "SHOPIFY",
                "title": "Deri Kartlik",
                "salePrice": 680.0,
                "listPrice": 680.0,
                "stockQuantity": 18
              },
              "performance": {
                "windowDays": 90,
                "unitsSold": 64,
                "orderCount": 59,
                "grossSales": 43520.0,
                "returnRate": 0.5,
                "financialsAvailable": true
              },
              "capabilities": {
                "supportsProductViews": false,
                "supportsFavorites": true,
                "supportsProductAnalytics": true
              }
            }
        """.trimIndent()

        val yanit = json.decodeFromString<ProductDetailWireDto>(detayJson)

        assertEquals("prd_01", yanit.product.id)
        assertEquals(64, yanit.performance.unitsSold)
        assertEquals(90, yanit.performance.windowDays)

        // Satis fiyati liste fiyatina esitse indirim YOK.
        assertFalse(yanit.product.onSale)
    }

    // ------------------------------------------------------------------
    // GET /integrations/{provider}/status
    // ------------------------------------------------------------------
    @Test
    fun entegrasyonDurumuBaglantiyiUydurmaz() {
        val durumJson = """
            {
              "connected": false,
              "syncing": false,
              "circuitBreakerTripped": false,
              "counts": { "orders": 0, "products": 0 },
              "latestRuns": [],
              "connections": []
            }
        """.trimIndent()

        val durum = json.decodeFromString<IntegrationStatusDto>(durumJson)

        // 🔴 Onceki DTO'da `connected` varsayilani TRUE idi ve repository
        // herhangi bir hatada `connected = true` uyduruyordu.
        assertFalse(durum.connected)
        assertEquals(0, durum.counts.orders)
        // Hic esitleme kosmadiysa son esitleme zamani da YOK.
        assertNull(durum.lastSyncedAt)
    }

    @Test
    fun sonEsitlemeZamaniTamamlanmisKosumdanAlinir() {
        val durumJson = """
            {
              "connected": true,
              "syncing": false,
              "circuitBreakerTripped": false,
              "counts": { "orders": 12, "products": 4 },
              "latestRuns": [
                { "id": "run_02", "syncType": "ORDERS", "status": "RUNNING",
                  "startedAt": "2026-08-28T03:00:00.000Z", "finishedAt": null },
                { "id": "run_01", "syncType": "ORDERS", "status": "SUCCESS",
                  "startedAt": "2026-08-28T02:00:00.000Z",
                  "finishedAt": "2026-08-28T02:01:30.000Z" }
              ]
            }
        """.trimIndent()

        val durum = json.decodeFromString<IntegrationStatusDto>(durumJson)

        assertTrue(durum.connected)
        // Devam eden kosum (finishedAt = null) atlanip tamamlanan alinmali.
        assertEquals("2026-08-28T02:01:30.000Z", durum.lastSyncedAt)
    }

    // ------------------------------------------------------------------
    // POST /integrations/{provider}/sync  ->  { started, connectionId }
    // ------------------------------------------------------------------
    @Test
    fun esitlemeYanitiSadeceBaslatildiginiSoyler() {
        val yanit = json.decodeFromString<SyncStartedResponseDto>(
            """{ "started": true, "connectionId": "conn_01" }"""
        )

        assertTrue(yanit.started)
        assertEquals("conn_01", yanit.connectionId)

        // Bu yanitta "kac siparis esitlendi" bilgisi YOK ve olamaz -- sunucu
        // isi baslatip donuyor. Onceki surum burada 12 sayisini uyduruyordu.
    }
}
