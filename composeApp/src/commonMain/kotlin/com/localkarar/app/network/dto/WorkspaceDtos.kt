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
    val status: String,
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
    val type: String = "other",
    val name: String,
    val legalName: String? = null,
    val contactPerson: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val city: String? = null,
    val address: String? = null,
    val notes: String? = null,
    val status: String? = null,
    val createdAt: String? = null
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
data class TrackerSummaryDto(
    val counts: TrackerCountsDto = TrackerCountsDto(),
    val nextThirtyDays: NextThirtyDaysDto = NextThirtyDaysDto(),
    val upcoming: List<BusinessRecordDto> = emptyList()
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
data class NextThirtyDaysDto(
    val payable: Double = 0.0,
    val receivable: Double = 0.0,
    val net: Double = 0.0
)

@Serializable
data class BusinessRecordDto(
    val id: String,
    val workspaceId: String,
    val type: String = "other",
    val title: String,
    val description: String? = null,
    val direction: String = "neutral",
    val amount: Double? = null,
    val currency: String = "TRY",
    val priority: String = "normal",
    val status: String = "open",
    val dueAt: String? = null,
    val contactId: String? = null,
    val contact: ContactRefDto? = null,
    val assignedToId: Int? = null,
    val assignedTo: AssigneeRefDto? = null,
    val recurrenceRule: String? = null,
    val metadata: JsonElement? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val completedAt: String? = null,
    val cancelledAt: String? = null,
    val reason: String? = null
)

@Serializable
data class ContactRefDto(
    val id: String,
    val name: String
)

@Serializable
data class AssigneeRefDto(
    val id: Int,
    val name: String
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
    val recurrenceRule: String? = null
)

@Serializable
data class RecordUpdateDto(
    val type: String? = null,
    val title: String? = null,
    val description: String? = null,
    val direction: String? = null,
    val amount: Double? = null,
    val currency: String? = null,
    val priority: String? = null,
    val dueAt: String? = null,
    val contactId: String? = null,
    val assignedToId: Int? = null,
    val recurrenceRule: String? = null,
    val status: String? = null,
    val reason: String? = null
)

@Serializable
data class RecordListResponseDto(
    val records: List<BusinessRecordDto> = emptyList(),
    val total: Int = 0,
    val limit: Int = 0,
    val offset: Int = 0
)

@Serializable
data class DeferRecordRequestDto(
    val dueAt: String,
    val reason: String
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