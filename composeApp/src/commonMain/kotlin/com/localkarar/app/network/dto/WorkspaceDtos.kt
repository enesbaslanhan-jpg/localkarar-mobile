package com.localkarar.app.network.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

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
    val workspaceId: String,
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
    val updatedAt: String? = null
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
    val overdue: List<BusinessRecordDto> = emptyList()
)

@Serializable
data class TrackerCountsDto(
    val open: Int = 0,
    val overdue: Int = 0,
    val dueToday: Int = 0,
    val shipments: Int = 0,
    val deferred: Int = 0
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
    val payload: JsonElement? = null,
    val evidence: JsonElement? = null,
    val status: String? = null
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
// COMMERCE: SİPARİŞLER (ORDERS) & ÜRÜNLER (PRODUCTS) DTOs
// ============================================================================

@Serializable
data class OrderItemDto(
    val productId: String? = null,
    val productName: String,
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val totalPrice: Double = 0.0
)

@Serializable
data class OrderDto(
    val id: String,
    val workspaceId: String,
    val orderNumber: String,
    val customerName: String,
    val contactId: String? = null,
    val itemsCount: Int = 1,
    val totalAmount: Double = 0.0,
    val currency: String = "TRY",
    val status: String = "pending", // pending, processing, shipped, delivered, cancelled
    val paymentStatus: String = "unpaid", // unpaid, paid, partially_paid, refunded
    val orderDate: String? = null,
    val deliveryDate: String? = null,
    val notes: String? = null,
    val items: List<OrderItemDto> = emptyList()
)

@Serializable
data class CreateOrderRequestDto(
    val orderNumber: String,
    val customerName: String,
    val contactId: String? = null,
    val totalAmount: Double,
    val currency: String = "TRY",
    val status: String = "pending",
    val paymentStatus: String = "unpaid",
    val orderDate: String? = null,
    val deliveryDate: String? = null,
    val notes: String? = null,
    val items: List<OrderItemDto> = emptyList()
)

@Serializable
data class UpdateOrderRequestDto(
    val customerName: String? = null,
    val contactId: String? = null,
    val totalAmount: Double? = null,
    val currency: String? = null,
    val status: String? = null,
    val paymentStatus: String? = null,
    val deliveryDate: String? = null,
    val notes: String? = null,
    val items: List<OrderItemDto>? = null
)

@Serializable
data class OrderListResponseDto(
    val orders: List<OrderDto> = emptyList(),
    val total: Int = 0
)

@Serializable
data class ProductDto(
    val id: String,
    val workspaceId: String,
    val code: String, // SKU
    val name: String,
    val category: String = "Genel",
    val price: Double = 0.0,
    val costPrice: Double = 0.0,
    val currency: String = "TRY",
    val stockQuantity: Int = 0,
    val minStockLevel: Int = 5,
    val unit: String = "Adet",
    val status: String = "active", // active, inactive, out_of_stock
    val description: String? = null,
    val createdAt: String? = null
)

@Serializable
data class CreateProductRequestDto(
    val code: String,
    val name: String,
    val category: String = "Genel",
    val price: Double,
    val costPrice: Double = 0.0,
    val currency: String = "TRY",
    val stockQuantity: Int = 0,
    val minStockLevel: Int = 5,
    val unit: String = "Adet",
    val status: String = "active",
    val description: String? = null
)

@Serializable
data class UpdateProductRequestDto(
    val code: String? = null,
    val name: String? = null,
    val category: String? = null,
    val price: Double? = null,
    val costPrice: Double? = null,
    val currency: String? = null,
    val stockQuantity: Int? = null,
    val minStockLevel: Int? = null,
    val unit: String? = null,
    val status: String? = null,
    val description: String? = null
)

@Serializable
data class ProductListResponseDto(
    val products: List<ProductDto> = emptyList(),
    val total: Int = 0
)