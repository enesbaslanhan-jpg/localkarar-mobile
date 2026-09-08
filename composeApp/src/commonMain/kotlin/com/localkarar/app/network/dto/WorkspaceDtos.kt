package com.localkarar.app.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class WorkspaceSummaryDto(
    val id: String,
    val name: String,
    val legalName: String? = null,
    val sector: String? = null,
    val city: String? = null,
    val currency: String? = null,
    val status: String? = null,
    val memberCount: Int = 0,
    val role: String? = null,
    val createdAt: String? = null
)

@Serializable
data class WorkspaceListResponseDto(
    val workspaces: List<WorkspaceSummaryDto> = emptyList()
)

@Serializable
data class WorkspaceMemberDto(
    val id: String,
    val userId: Int,
    val name: String,
    val email: String,
    val role: String,
    val status: String,
    val joinedAt: String? = null
)

@Serializable
data class WorkspaceDetailDto(
    val id: String,
    val name: String,
    /**
     * 🔴 SUNUCU GONDERIYOR, MOBIL OKUMUYORDU.
     *
     * `taxNumber` isletme detayinda geliyor (workspace.ts:455) ve
     * e-Fatura yonunu (gelen mi giden mi) belirleyen alan bu. Mobilde
     * DTO alani olmadigi icin `ignoreUnknownKeys` yuzunden sessizce
     * dusuyordu; kullanici numarayi ne gorebiliyor ne girebiliyordu.
     */
    val taxNumber: String? = null,
    val legalName: String? = null,
    val sector: String? = null,
    val city: String? = null,
    val country: String? = null,
    val currency: String? = null,
    val businessStage: String? = null,
    val employeeCount: Int? = null,
    val salesChannels: List<String> = emptyList(),
    val primaryGoal: String? = null,
    val challenges: List<String> = emptyList(),
    val monthlySales: Double? = null,
    val monthlyExpenses: Double? = null,
    val cashBalance: Double? = null,
    val debtBalance: Double? = null,
    val status: String? = null,
    val memberCount: Int = 0,
    val contactCount: Int = 0,
    val members: List<WorkspaceMemberDto> = emptyList(),
    val myRole: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class CreateWorkspaceRequestDto(
    val name: String,
    val legalName: String? = null,
    val sector: String? = null,
    val city: String? = null,
    val currency: String? = null
)

@Serializable
data class UpdateWorkspaceRequestDto(
    val name: String? = null,
    val legalName: String? = null,
    /** 10 haneli VKN ya da 11 haneli TCKN; sunucu bicimi dogruluyor. */
    val taxNumber: String? = null,
    val sector: String? = null,
    val city: String? = null,
    val country: String? = null,
    val currency: String? = null,
    val businessStage: String? = null,
    val employeeCount: Int? = null,
    val salesChannels: List<String>? = null,
    val primaryGoal: String? = null,
    val challenges: List<String>? = null,
    val monthlySales: Double? = null,
    val monthlyExpenses: Double? = null,
    val cashBalance: Double? = null,
    val debtBalance: Double? = null
)

@Serializable
data class InviteMemberRequestDto(
    val email: String,
    val role: String = "staff"
)

@Serializable
data class BusinessInvitationDto(
    val id: String,
    val email: String,
    val role: String,
    val invitedBy: String? = null,
    val expiresAt: String? = null,
    val createdAt: String? = null
)

@Serializable
data class UpdateMemberRoleRequestDto(
    val role: String
)

@Serializable
data class BusinessContactDto(
    val id: String,
    /*
     * 🔴 ZORUNLU ALANDI VE SUNUCU HIC GONDERMIYOR.
     * `/workspaces/:id/contacts` yaniti (workspace.ts:1114) alanlari tek tek
     * seciyor ve `workspaceId` o listede YOK. kotlinx.serialization eksik
     * zorunlu alanda istisna firlatiyordu; Kisiler ekrani "Isletme
     * yuklenemedi" hatasiyla HIC ACILMIYORDU.
     */
    val workspaceId: String? = null,
    val type: String = "customer",
    val name: String,
    val legalName: String? = null,
    val contactPerson: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val city: String? = null,
    val address: String? = null,
    val notes: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class ContactInputDto(
    val type: String = "customer",
    val name: String,
    val legalName: String? = null,
    val contactPerson: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val city: String? = null,
    val address: String? = null,
    val notes: String? = null
)

@Serializable
data class RecordContactRefDto(
    val id: String,
    val name: String
)

@Serializable
data class RecordAssigneeRefDto(
    val id: Int,
    val name: String
)

@Serializable
data class BusinessRecordDto(
    val id: String,
    val workspaceId: String,
    val type: String,
    val title: String,
    val description: String? = null,
    val direction: String = "neutral",
    val amount: Double? = null,
    val currency: String = "TRY",
    val status: String = "open",
    val priority: String = "normal",
    val dueAt: String? = null,
    val originalDueAt: String? = null,
    val completedAt: String? = null,
    val cancelledAt: String? = null,
    val contactId: String? = null,
    val contact: RecordContactRefDto? = null,
    val assignedToId: Int? = null,
    val assignedTo: RecordAssigneeRefDto? = null,
    val recurrenceRule: String? = null,
    val parentRecordId: String? = null,
    val metadata: Map<String, JsonElement> = emptyMap(),
    val createdAt: String? = null,
    val updatedAt: String? = null,

    /*
     * ⚠️ ASAGIDAKI UCU YALNIZ DETAY UCUNDA DOLU.
     *
     * Liste ucu (`/records`) bunlari getirmiyor; varsayilanlari bos
     * liste, yani liste ekrani hicbir sey cizmiyor. Sunucu detayda
     * `include: { history, reminders, documents }` yapiyor
     * (business-tracker.ts:654).
     *
     * 🔴 UCU DE MOBILDE HIC OKUNMUYORDU: kaydin dayanagi (hangi
     * faturadan geldigi), hatirlaticilari ve gecmisi webde var,
     * mobilde yoktu. "Bu rakam nereden geldi" sorusunun cevabi
     * `documents` icinde duruyor.
     */
    val documents: List<RecordDocumentLinkDto> = emptyList(),
    val reminders: List<RecordReminderDto> = emptyList(),
    val history: List<RecordHistoryDto> = emptyList()
)

/** Kayda bagli belge — `BusinessRecordDocument` + belgenin kendisi. */
@Serializable
data class RecordDocumentLinkDto(
    val id: String? = null,
    val createdAt: String? = null,
    val document: RecordDocumentDto? = null
)

@Serializable
data class RecordDocumentDto(
    val id: String? = null,
    val originalName: String? = null,
    val mimeType: String? = null,
    val sizeBytes: Long? = null,
    val category: String? = null,
    val documentDate: String? = null,
    /*
     * 🔴 HAM BIRAKILIYOR — TIPLI DEGIL. Sebebi sunucudaki bir TUTARSIZLIK.
     *
     * Olculdu (07.09.2026): ayni alan iki ucta iki farkli bicimde
     * geliyor. Belge LISTESI ucu onu cozup NESNE olarak donduruyor
     * (`business-tracker.ts` -> `parseJson(document.analysis)`); kayit
     * DETAY ucu ise `recordJson` yalniz `record.metadata`yi cozdugu icin
     * ic ice gecmis belgenin alanini JSON DIZGESI olarak biraktiriyor.
     *
     * `DocumentAnalysisDto` olarak tiplendiginde detay ekrani tumden
     * dusuyordu ("İşletme yüklenemedi") -- cunku gelen sey bir nesne
     * degil, bir dize.
     *
     * ⚠️ Sunucuyu degistirmek yerine web ile AYNI savunma yapiliyor:
     * `KayitDetay.jsx` icindeki `analiziCoz` da "nesneyse al, dizgeyse
     * coz" diyor. Sunucunun bicimini degistirmek webin iki uctaki
     * davranisini de etkilerdi; iki ürün ayni tutarsizligi ayni sekilde
     * karsiliyor.
     *
     * Cozmek icin `eFaturayiCoz()` kullanilir.
     */
    val analysis: JsonElement? = null,
    val createdAt: String? = null
)

/**
 * `analysis` alanini e-Faturaya cevirir — nesne de olsa dize de olsa.
 *
 * Cozulemezse `null`: bozuk bir govdeden alan uydurmaktansa arayuz
 * "fatura okunamadi" diyor.
 */
fun RecordDocumentDto.eFaturayiCoz(): EFaturaDto? {
    val ham = analysis ?: return null
    return try {
        val nesne = when (ham) {
            /* Dize hali: icerigi ikinci kez ayristiriliyor. */
            is JsonPrimitive -> if (ham.isString) {
                DtoJson.parseToJsonElement(ham.content)
            } else return null
            else -> ham
        }
        val eFatura = (nesne as? JsonObject)?.get("eFatura") ?: return null
        if (eFatura is JsonNull) return null
        DtoJson.decodeFromJsonElement(EFaturaDto.serializer(), eFatura)
    } catch (e: Exception) {
        null
    }
}

/*
 * Yalniz bu cozumleme icin; ag katmaninin Json'u ile ayni ayarlar.
 * `ignoreUnknownKeys`: `analysis` icinde e-Fatura disinda ozet,
 * anahtar kelimeler gibi alanlar da var.
 */
private val DtoJson = kotlinx.serialization.json.Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

/**
 * UBL-TR e-Fatura'nin cozumlenmis hali — sunucudaki `UblFatura`
 * (`src/services/e-fatura.ts:46`) ile birebir.
 *
 * ⚠️ `vadeTarihi` orneklerin %86'sinda YOK ve bu OLAGAN. Arayuz
 * bosken "faturada belirtilmemiş" yaziyor; duzenleme tarihini vade
 * yerine koymak, olmayan bir vadeyi varmis gibi gostermek olurdu.
 */
@Serializable
data class EFaturaDto(
    val id: String? = null,
    val duzenlemeTarihi: String? = null,
    val vadeTarihi: String? = null,
    val paraBirimi: String? = null,
    val odenecekTutar: Double? = null,
    val kdvHaricTutar: Double? = null,
    val satici: EFaturaTarafDto? = null,
    val alici: EFaturaTarafDto? = null
)

@Serializable
data class EFaturaTarafDto(
    val unvan: String? = null,
    val kimlik: String? = null,
    /** VKN ya da TCKN. */
    val kimlikTuru: String? = null
)

/** `BusinessReminder` — vade hatirlaticisi. */
@Serializable
data class RecordReminderDto(
    val id: String? = null,
    val scheduledAt: String? = null,
    /** pending | sent */
    val status: String? = null,
    val sentAt: String? = null,
    val channel: String? = null
)

/** `BusinessRecordHistory` — kaydin uzerinde ne yapildigi. */
@Serializable
data class RecordHistoryDto(
    val id: String? = null,
    val action: String? = null,
    val reason: String? = null,
    val createdAt: String? = null
)

@Serializable
data class RecordInputDto(
    val type: String,
    val title: String,
    val description: String? = null,
    val direction: String = "neutral",
    val amount: Double? = null,
    val currency: String = "TRY",
    val priority: String = "normal",
    val dueAt: String? = null,
    val contactId: String? = null,
    val assignedToId: Int? = null,
    val recurrenceRule: String? = null,
    val metadata: Map<String, JsonElement>? = null
)

@Serializable
data class RecordUpdateDto(
    val type: String? = null,
    val title: String? = null,
    val description: String? = null,
    val direction: String? = null,
    val amount: Double? = null,
    val currency: String? = null,
    val status: String? = null,
    val priority: String? = null,
    val dueAt: String? = null,
    val contactId: String? = null,
    val assignedToId: Int? = null,
    val recurrenceRule: String? = null,
    val reason: String? = null,
    val metadata: Map<String, JsonElement>? = null
)

@Serializable
data class DeferRecordRequestDto(
    val dueAt: String,
    val reason: String
)

@Serializable
data class RecordListResponseDto(
    val records: List<BusinessRecordDto> = emptyList(),
    val total: Int = 0
)

@Serializable
data class TrackerSummaryDto(
    val counts: TrackerCountsDto = TrackerCountsDto(),
    val nextThirtyDays: TrackerWindowDto = TrackerWindowDto(),
    val upcoming: List<BusinessRecordDto> = emptyList(),
    val overdue: List<BusinessRecordDto> = emptyList(),
    val awaitingDirection: AwaitingDirectionDto? = null
)

@Serializable
data class TrackerCountsDto(
    val open: Int = 0,
    val overdue: Int = 0,
    val dueToday: Int = 0,
    val shipments: Int = 0,
    val deferred: Int = 0,
    val awaitingDirection: Int = 0
)

/**
 * Yonu belli olmayan ama TUTARI OLAN kayitlar.
 *
 * 🔴 MOBIL BU ALANI HIC OKUMUYORDU. Sunucu gonderiyor
 * (`src/services/business-tracker.ts` -> `trackerOzetiHesapla`), web Ana
 * Sayfa'da gosteriyor (`Dashboard.jsx:345`), mobilde karsiligi yoktu.
 *
 * Neden onemli: e-Fatura okundugunda VKN isletmenin vergi numarasiyla
 * eslesmezse yon `neutral` kaliyor. Bu kayitlar `payable` ya da `receivable`
 * toplamlarina GIRMIYOR -- yani tutari olan bir kayit hicbir yerde
 * gorunmuyor. Sunucu yorumu bunun urun sahibi tarafindan kullanim sirasinda
 * fark edildigini soyluyor.
 *
 * Tahminle borc/alacak saymak yanlis olurdu (kullanicinin alacagini borc
 * gostermek demek); bu yuzden kendi sayaciyla gorunur oluyor.
 */
@Serializable
data class AwaitingDirectionDto(
    val count: Int = 0,
    val amount: Double = 0.0
)

@Serializable
data class TrackerWindowDto(
    val payable: Double = 0.0,
    val receivable: Double = 0.0,
    val net: Double = 0.0
)

@Serializable
data class CalendarDayResponseDto(
    val from: String? = null,
    val to: String? = null,
    val days: Map<String, List<BusinessRecordDto>> = emptyMap(),
    val totals: CalendarTotalsDto = CalendarTotalsDto()
)

@Serializable
data class CalendarTotalsDto(
    val records: Int = 0,
    val payable: Double = 0.0,
    val receivable: Double = 0.0
)

@Serializable
data class BusinessNotificationDto(
    val id: String,
    val workspaceId: String,
    val userId: Int,
    val type: String? = null,
    val title: String? = null,
    val body: String? = null,
    val readAt: String? = null,
    val createdAt: String? = null,
    val record: NotificationRecordRefDto? = null
)

@Serializable
data class NotificationRecordRefDto(
    val id: String,
    val title: String? = null,
    val type: String? = null,
    val dueAt: String? = null,
    val status: String? = null
)

@Serializable
data class NotificationsResponseDto(
    val notifications: List<BusinessNotificationDto> = emptyList(),
    val unreadCount: Int = 0
)

@Serializable
data class WorkspaceSettingsDto(
    val id: String? = null,
    val timezone: String = "Europe/Istanbul",
    val locale: String = "tr-TR",
    val defaultCurrency: String = "TRY",
    val weekStartsOn: Int = 1,
    val notificationPrefs: Map<String, JsonElement> = emptyMap()
)

@Serializable
data class UpdateSettingsRequestDto(
    val timezone: String? = null,
    val locale: String? = null,
    val defaultCurrency: String? = null,
    val weekStartsOn: Int? = null,
    val notificationPrefs: Map<String, JsonElement>? = null
)

@Serializable
data class WorkspaceActivityDto(
    val id: String,
    val actorId: Int,
    val action: String,
    val entityType: String? = null,
    val entityId: String? = null,
    val metadata: Map<String, JsonElement> = emptyMap(),
    val createdAt: String? = null
)

@Serializable
data class ActivityResponseDto(
    val items: List<WorkspaceActivityDto> = emptyList(),
    val total: Int = 0
)

@Serializable
data class WorkspaceDocumentDto(
    val id: String,
    val originalName: String,
    val mimeType: String? = null,
    val sizeBytes: Long = 0,
    val category: String? = null,
    val documentDate: String? = null,
    val dueDate: String? = null,
    val analysisStatus: String? = null,
    val analysis: JsonElement? = null,
    val extractedText: String? = null,
    val suggestions: List<DocumentSuggestionDto> = emptyList(),
    val createdAt: String? = null,
    val workspaceId: String? = null
)

@Serializable
data class DocumentSuggestionDto(
    val id: String? = null,
    val type: String? = null,
    val title: String? = null,
    val description: String? = null,
    /*
     * 🔴 PAYLOAD ARTIK TIPLI.
     *
     * `JsonElement` olarak duruyordu ve arayuz onu HIC OKUMUYORDU:
     * belge yukleniyor, sunucu oneriyi uretiyor, mobil yalniz "analiz
     * edildi" rozetini gosterip oneriyi yutuyordu. Belge yuklemenin
     * asil faydasi -- okunan faturadan kayit acmak -- mobilde yoktu.
     */
    val payload: DocumentSuggestionPayloadDto? = null,
    /*
     * Sunucunun kendi guven olcusu (0..1). Webde yuzde olarak yaziliyor.
     * ⚠️ Yoksa yazilmiyor: 0 yazmak "hic guvenmiyorum" demek olurdu,
     * oysa bilinmeyen bir deger.
     */
    val confidence: Double? = null,
    /** Sunucunun oneriyi neye dayandirdigi — fatura no, tarih, tutar. */
    val evidence: JsonElement? = null,
    /** "proposed" | "accepted" | "rejected" */
    val status: String? = null,
    val suggestionType: String? = null
)

/**
 * Onerilen kaydin alanlari — sunucudaki `RecordSuggestionPayload`
 * (`src/services/document-suggestions.ts`) ile birebir.
 *
 * ⚠️ HICBIRI UYDURULMUYOR. `amount` ve `dueAt` null olabilir: e-Fatura
 * orneklerinin cogunda vade yok ve sunucu bilerek bos birakiyor.
 * Arayuz de bos biraktiginda tire yaziyor, sifir ya da bugunun tarihi
 * DEGIL.
 */
@Serializable
data class DocumentSuggestionPayloadDto(
    /** payment | receivable | promissory_note | purchase | shipment */
    val type: String? = null,
    val title: String? = null,
    val description: String? = null,
    /** payable | receivable | neutral */
    val direction: String? = null,
    val amount: Double? = null,
    val currency: String? = null,
    val dueAt: String? = null,
    val priority: String? = null
)

@Serializable
data class WorkspaceDocumentsResponseDto(
    val documents: List<WorkspaceDocumentDto> = emptyList(),
    val total: Int = 0
)

@Serializable
data class DocumentMetadataDto(
    val category: String? = null,
    val documentDate: String? = null,
    val dueDate: String? = null
)

// ============================================================================
// CANONICAL MARKETPLACE COMMERCE DTOs (/marketplace & /integrations)
// ============================================================================

@Serializable
data class SyncMarketplaceRequestDto(
    val workspaceId: String
)


// ============================================================================
// PAZARYERI SOZLESMESI
//
// 🔴 BU BLOK BIR ARIZANIN SONUCU. Onceki hali sunucunun GONDERMEDIGI alanlari
// ZORUNLU istiyordu: OrderDto `workspaceId` + `orderNumber`, ProductDto
// `workspaceId` + non-null `sku`. kotlinx.serialization her BASARILI yanitta
// MissingFieldException atiyordu; repository de bunu "istek basarisiz" sanip
// uydurma veriye dusuyordu. Yani basari dali OLU KODDU ve Siparisler/Urunler
// ekranlari %100 uydurma veri gosteriyordu.
//
// Bundan sonraki kural: ALANLAR SUNUCUNUN GERCEKTEN GONDERDIGI ADLA yazilir.
// Arayuzun alistigi adlar @SerialName ile eslenir ya da turetilmis ozellik
// olarak verilir -- ASLA savunmaci varsayilanla gizlenmez.
//
// Kaynak: src/services/integrations/marketplace-routes.ts (orderJson) ve
//         src/services/integrations/product-analytics.ts (productListItemFromRow)
// ============================================================================

/**
 * GET /integrations/{provider}/status
 *
 * `connected` varsayilani "yoksa true" idi; istek basarisiz olsa bile arayuz
 * "bagli" diyordu. Sunucu bu alani her zaman gonderiyor; varsayilan hem
 * gereksiz hem yaniltici.
 */
@Serializable
data class IntegrationStatusDto(
    val connected: Boolean,
    val syncing: Boolean = false,
    val circuitBreakerTripped: Boolean = false,
    val counts: IntegrationCountsDto = IntegrationCountsDto(),
    val latestRuns: List<IntegrationSyncRunDto> = emptyList()
) {
    /** En son tamamlanan esitlemenin bitis zamani; hic esitleme yoksa null. */
    val lastSyncedAt: String? get() = latestRuns.firstOrNull { it.finishedAt != null }?.finishedAt
}

@Serializable
data class IntegrationCountsDto(
    val orders: Int = 0,
    val products: Int = 0
)

@Serializable
data class IntegrationSyncRunDto(
    val id: String,
    val syncType: String? = null,
    val status: String? = null,
    val startedAt: String? = null,
    val finishedAt: String? = null,
    val recordsFetched: Int? = null,
    val recordsCreated: Int? = null,
    val recordsUpdated: Int? = null,
    val recordsSkipped: Int? = null,
    val errorCode: String? = null
)

/**
 * POST /integrations/{provider}/sync yaniti: `{ started, connectionId }`.
 *
 * Sunucu esitlemeyi BASLATIYOR, bitirmiyor. Onceki DTO `syncedCount` bekliyordu
 * ve repository hata durumunda "12 siparis esitlendi" diye uyduruyordu --
 * hicbir sey esitlenmemisken kullaniciya soylenen duz yanlis bir cumleydi.
 */
@Serializable
data class SyncStartedResponseDto(
    val started: Boolean = false,
    val connectionId: String? = null
)

@Serializable
data class OrderItemDto(
    val id: String,
    val externalProductId: String? = null,
    val sku: String? = null,
    val barcode: String? = null,
    val title: String? = null,
    val quantity: Int = 1,
    val unitPrice: Double? = null,
    @SerialName("grossAmount") val totalPrice: Double? = null,
    val discountAmount: Double? = null,
    val commissionAmount: Double? = null,
    val refundAmount: Double? = null,
    val netContribution: Double? = null
)

@Serializable
data class OrderDto(
    val id: String,
    val provider: String,
    val externalId: String? = null,
    @SerialName("externalOrderNumber") val orderNumber: String? = null,
    @SerialName("customerDisplayName") val customerName: String? = null,
    val currency: String? = null,
    val grossAmount: Double? = null,
    val discountAmount: Double? = null,
    @SerialName("commissionAmount") val commission: Double? = null,
    @SerialName("shippingAmount") val shipping: Double? = null,
    @SerialName("refundAmount") val refund: Double? = null,
    val netContribution: Double? = null,
    val status: String = "UNKNOWN",
    val orderDate: String? = null,
    @SerialName("syncedAt") val lastSyncedAt: String? = null,
    /** Liste ucunda `_count.items`ten gelir; detay ucunda gonderilmez. */
    @SerialName("itemCount") val itemsCount: Int? = null,
    /** Yalniz detay ucunda dolu gelir; listede hic gonderilmez. */
    val items: List<OrderItemDto> = emptyList()
)

/** GET /marketplace/orders -> `{ orders, total, limit, offset }` */
@Serializable
data class OrderListWireDto(
    val orders: List<OrderDto> = emptyList(),
    val total: Int = 0,
    val limit: Int = 0,
    val offset: Int = 0
)

/** GET /marketplace/orders/:orderId -> `{ order }` (sarmalayici!) */
@Serializable
data class OrderDetailWireDto(
    val order: OrderDto
)

/**
 * Repository'nin arayuze verdigi BIRLESIK sonuc.
 *
 * Bu bir TEL DTO'SU DEGIL: `integrationConnected` ve `lastSyncedAt` liste
 * ucundan GELMIYOR, durum ucundan geliyor. Onceki surumde ikisi de bu sinifta
 * "yoksa true" / sabit tarih varsayilaniyla duruyordu ve arayuz hicbir
 * entegrasyon yokken "bagli, en son 28.08 tarihinde esitlendi" diyordu.
 */
data class OrderListResponseDto(
    val orders: List<OrderDto> = emptyList(),
    val total: Int = 0,
    val lastSyncedAt: String? = null,
    val integrationConnected: Boolean = false
)

@Serializable
data class ProductPerformanceDto(
    val windowDays: Int = 30,
    val unitsSold: Int = 0,
    val orderCount: Int = 0,
    val grossSales: Double? = null,
    val averageSellingPrice: Double? = null,
    val returnedUnits: Int = 0,
    val returnRate: Double? = null,
    val commissionTotal: Double? = null,
    val shippingTotal: Double? = null,
    val netContribution: Double? = null,
    /**
     * Sunucu ekliyor: komisyon ve kargo bilesenleri eksikse arayuz "veri yok"
     * gostermeli, SIFIR GOSTERMEMELI.
     */
    val financialsAvailable: Boolean = false
)

@Serializable
data class ProductDto(
    val id: String,
    val provider: String,
    val externalId: String? = null,
    val title: String,
    val brand: String? = null,
    val category: String? = null,
    val sku: String? = null,
    val barcode: String? = null,
    val salePrice: Double? = null,
    val listPrice: Double? = null,
    @SerialName("stockQuantity") val stock: Int = 0,
    val isActive: Boolean = true,
    val imageUrl: String? = null,
    @SerialName("syncedAt") val lastSyncedAt: String? = null,
    val lowStock: Boolean = false,
    val performance: ProductPerformanceDto = ProductPerformanceDto(),
    val internalNote: String? = null,
    val tags: List<String> = emptyList(),
    val lowStockThresholdOverride: Int? = null,
    val isFavorite: Boolean = false
) {
    /**
     * "Indirimde mi" sunucuda bir ALAN DEGIL, bir SORGU SUZGECI. Urun satiri
     * yalniz satis ve liste fiyatini tasiyor; indirim bu ikisinden turetiliyor.
     */
    val onSale: Boolean get() = salePrice != null && listPrice != null && salePrice < listPrice

    val unitsSold: Int get() = performance.unitsSold
    val orderCount: Int get() = performance.orderCount
    val grossSales: Double? get() = performance.grossSales
    val returnRate: Double? get() = performance.returnRate

    /**
     * Sunucu urun satirinda para birimi GONDERMIYOR. Pazaryeri baglantilarinin
     * tamami TRY calisiyor; coklu para birimi gerekirse sunucudan gelmeli.
     */
    val currency: String get() = "TRY"
}

/** GET /marketplace/products -> `{ products, total, threshold, windowDays }` */
@Serializable
data class ProductListWireDto(
    val products: List<ProductDto> = emptyList(),
    val total: Int = 0,
    val threshold: Int = 0,
    val windowDays: Int = 30
)

/** GET /marketplace/products/:productId -> `{ product, performance, capabilities }` */
@Serializable
data class ProductDetailWireDto(
    val product: ProductDto,
    val performance: ProductPerformanceDto = ProductPerformanceDto()
)

/** Repository birlesik sonucu -- bkz. OrderListResponseDto notu. */
data class ProductListResponseDto(
    val products: List<ProductDto> = emptyList(),
    val total: Int = 0,
    val lastSyncedAt: String? = null,
    val integrationConnected: Boolean = false
)

@Serializable
data class UpdateProductSettingsRequestDto(
    val workspaceId: String,
    val internalNote: String? = null,
    val tags: List<String>? = null,
    val lowStockThresholdOverride: Int? = null,
    val isFavorite: Boolean? = null
)

/*
 * e-FATURA GELEN KUTUSU.
 *
 * 🔴 WEBDE VAR, MOBILDE HIC YOKTU. Isletme ayarlarinin uc kartindan biri
 * bu (`Settings.jsx`): isletmeye ait bir e-posta adresi acilıyor,
 * muhasebe programindan gonderilen faturalar onay bekleyen kayda
 * donusuyor. Mobil kullanici adresi goremiyor, yenileyemiyor,
 * kapatamiyor ve guvenilir gonderen ekleyemiyordu.
 *
 * ⚠️ Alan adlari TURKCE cunku sunucu oyle donuyor (workspace.ts:508).
 */
@Serializable
data class InboxStatusDto(
    val acik: Boolean = false,
    val adres: String? = null,
    /**
     * Kanal sunucuda yapilandirilmis mi (INBOUND_MAIL_SECRET).
     * Hazir degilse adres uretmek yaniltici olur: posta gelse bile
     * islenmez — arayuz bunu soylemek zorunda.
     */
    val kanalHazir: Boolean = false
)

@Serializable
data class InboxSenderDto(
    val id: String,
    val email: String,
    val label: String? = null,
    val createdAt: String? = null
)

@Serializable
data class InboxSendersResponseDto(
    val gonderenler: List<InboxSenderDto> = emptyList()
)

@Serializable
data class AddInboxSenderRequestDto(
    val email: String,
    val label: String? = null
)

/** `POST /workspaces/invitations/accept` yaniti. */
@Serializable
data class InvitationAcceptResponseDto(
    val accepted: Boolean = false,
    val workspaceId: String? = null
)

@Serializable
data class InvitationAcceptRequestDto(
    val token: String
)

/**
 * Belgeden cikan finansal model onerileri.
 *
 * `GET /workspaces/{ws}/documents/{id}/financial-model-suggestions`
 *
 * 🔴 Mobilde HIC YOKTU. Sunucu yuklenen belgenin metninden hangi
 * finansal modelin calistirilabilecegini ve o model icin verinin ne
 * kadarinin HAZIR oldugunu hesapliyor; web bunu belge kartinin altinda
 * dugme olarak gosteriyor (`Documents.jsx`).
 */
@Serializable
data class ModelOnerileriDto(
    val documentId: String? = null,
    val documentName: String? = null,
    val extractedFieldCount: Int = 0,
    val models: List<ModelOnerisiDto> = emptyList(),
    /*
     * Sunucunun kendi uyarisi — ornegin "Belgeden okunabilir finansal
     * alan çıkarılamadı." Bos donebilir; o zaman yazilmiyor.
     */
    val warning: String? = null
)

@Serializable
data class ModelOnerisiDto(
    val code: String,
    val name: String? = null,
    /** 0..1 arasi; modelin girdilerinin ne kadari belgeden dolduruldu. */
    val coverage: Double = 0.0,
    /*
     * Belgede bulunamayan girdiler — kullanicinin elle girecekleri.
     *
     * 🔴 ONCE `List<String>` YAZILMISTI VE ISTEK COKUYORDU. Yanilginin
     * kaynagi web: `Documents.jsx` bu alandan yalnizca `.length`
     * okuyor, dolayisiyla iceriginin ne oldugu oradan anlasilmiyor.
     * Sunucu ise `{key, label, unit}` nesneleri donduruyor
     * (olculdu 07.09.2026: "Expected beginning of the string, but got
     * { at path: $.models[3].missingFields[0]").
     *
     * ⚠️ `label` ve `unit` SIMDILIK CIZILMIYOR ama tipli tutuluyor:
     * eksik alanlari tek tek saymak yerine adiyla listelemek ileride
     * anlamli olacak ve o zaman DTO'yu yeniden kesfetmek gerekmesin.
     */
    val missingFields: List<ModelEksikAlanDto> = emptyList(),
    /*
     * Belgeden OKUNAN degerler: modelin girdi anahtari -> sayi.
     *
     * 🔴 Sunucu gonderiyordu, mobil okumuyordu — bu yuzden "hesaplama
     * öner" modeli aciyor ama faturadaki rakamlari TASIMIYORDU;
     * kullanici okunan sayilari elle yeniden yaziyordu (madde 18).
     *
     * ⚠️ Bu degerler DOGRULANMIS SAYILMAZ. Sunucu, belgeden gelen bir
     * alan `userVerified` olmadan modeli calistirmiyor. OCR ve
     * ayristirma yanilabilir.
     */
    val mappedInputs: Map<String, JsonElement> = emptyMap()
)

@Serializable
data class ModelEksikAlanDto(
    val key: String? = null,
    val label: String? = null,
    /** Para birimi ya da olcu — ornegin "TRY", "adet". */
    val unit: String? = null
)

/** `POST /workspaces/{ws}/records/import` istegi. */
@Serializable
data class RecordImportRequestDto(
    /** Once `/documents/upload` ile yuklenen dosyanin kimligi. */
    val fileId: String,
    /** hedefAlan -> dosyadaki sutun basligi. */
    val columnMapping: Map<String, String>,
    val previewOnly: Boolean = true
)

/**
 * Ic aktarma yaniti.
 *
 * `previewOnly = true` iken `preview`, `totalRows`, `validRows`,
 * `errors` ve `sample` dolu; `false` iken sunucu olusturulan kayitlarin
 * ozetini donduruyor.
 */
@Serializable
data class RecordImportResultDto(
    val preview: Boolean = false,
    val totalRows: Int = 0,
    val validRows: Int = 0,
    val imported: Int = 0,
    val errors: List<RecordImportErrorDto> = emptyList()
)

@Serializable
data class RecordImportErrorDto(
    val row: Int = 0,
    val field: String? = null,
    val message: String? = null
)
