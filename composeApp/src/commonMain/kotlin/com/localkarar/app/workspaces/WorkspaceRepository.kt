package com.localkarar.app.workspaces

import com.localkarar.app.network.ApiConfig
import com.localkarar.app.network.SafeApiClient
import com.localkarar.app.network.dto.ActivityResponseDto
import com.localkarar.app.network.dto.BusinessContactDto
import com.localkarar.app.network.dto.BusinessInvitationDto
import com.localkarar.app.network.dto.BusinessNotificationDto
import com.localkarar.app.network.dto.BusinessRecordDto
import com.localkarar.app.network.dto.CalendarDayResponseDto
import com.localkarar.app.network.dto.ContactInputDto
import com.localkarar.app.network.dto.CreateWorkspaceRequestDto
import com.localkarar.app.network.dto.DeferRecordRequestDto
import com.localkarar.app.network.dto.DocumentMetadataDto
import com.localkarar.app.network.dto.InviteMemberRequestDto
import com.localkarar.app.network.dto.NotificationsResponseDto
import com.localkarar.app.network.dto.RecordInputDto
import com.localkarar.app.network.dto.RecordListResponseDto
import com.localkarar.app.network.dto.RecordUpdateDto
import com.localkarar.app.network.dto.TrackerSummaryDto
import com.localkarar.app.network.dto.UpdateMemberRoleRequestDto
import com.localkarar.app.network.dto.UpdateSettingsRequestDto
import com.localkarar.app.network.dto.UpdateWorkspaceRequestDto
import com.localkarar.app.network.dto.WorkspaceDetailDto
import com.localkarar.app.network.dto.WorkspaceDocumentsResponseDto
import com.localkarar.app.network.dto.WorkspaceListResponseDto
import com.localkarar.app.network.dto.WorkspaceMemberDto
import com.localkarar.app.network.dto.WorkspaceSettingsDto

class WorkspaceRepository(private val api: SafeApiClient) {

    private val base = ApiConfig.baseUrl

    suspend fun listWorkspaces(): Result<WorkspaceListResponseDto> {
        return api.get("$base/api/workspaces")
    }

    suspend fun createWorkspace(body: CreateWorkspaceRequestDto): Result<WorkspaceDetailDto> {
        return api.post("$base/api/workspaces", body)
    }

    suspend fun getWorkspace(workspaceId: String): Result<WorkspaceDetailDto> {
        return api.get("$base/api/workspaces/$workspaceId")
    }

    suspend fun updateWorkspace(workspaceId: String, body: UpdateWorkspaceRequestDto): Result<WorkspaceDetailDto> {
        return api.put("$base/api/workspaces/$workspaceId", body)
    }

    suspend fun deleteWorkspace(workspaceId: String): Result<Unit> {
        return api.delete("$base/api/workspaces/$workspaceId")
    }

    suspend fun getMembers(workspaceId: String): Result<List<WorkspaceMemberDto>> {
        return api.get("$base/api/workspaces/$workspaceId/members")
    }

    suspend fun updateMemberRole(workspaceId: String, memberId: String, role: String): Result<Unit> {
        return api.put(
            "$base/api/workspaces/$workspaceId/members/$memberId/role",
            UpdateMemberRoleRequestDto(role)
        )
    }

    suspend fun removeMember(workspaceId: String, memberId: String): Result<Unit> {
        return api.delete("$base/api/workspaces/$workspaceId/members/$memberId")
    }

    suspend fun getInvitations(workspaceId: String): Result<List<BusinessInvitationDto>> {
        return api.get("$base/api/workspaces/$workspaceId/invitations")
    }

    suspend fun inviteMember(workspaceId: String, email: String, role: String): Result<Unit> {
        return api.post("$base/api/workspaces/$workspaceId/members", InviteMemberRequestDto(email, role))
    }

    suspend fun cancelInvitation(workspaceId: String, invitationId: String): Result<Unit> {
        return api.delete("$base/api/workspaces/$workspaceId/invitations/$invitationId")
    }

    suspend fun getContacts(workspaceId: String): Result<List<BusinessContactDto>> {
        return api.get("$base/api/workspaces/$workspaceId/contacts")
    }

    suspend fun createContact(workspaceId: String, body: ContactInputDto): Result<BusinessContactDto> {
        return api.post("$base/api/workspaces/$workspaceId/contacts", body)
    }

    suspend fun updateContact(workspaceId: String, contactId: String, body: ContactInputDto): Result<Unit> {
        return api.put("$base/api/workspaces/$workspaceId/contacts/$contactId", body)
    }

    suspend fun deleteContact(workspaceId: String, contactId: String): Result<Unit> {
        return api.delete("$base/api/workspaces/$workspaceId/contacts/$contactId")
    }

    suspend fun getSettings(workspaceId: String): Result<WorkspaceSettingsDto> {
        return api.get("$base/api/workspaces/$workspaceId/settings")
    }

    suspend fun updateSettings(workspaceId: String, body: UpdateSettingsRequestDto): Result<WorkspaceSettingsDto> {
        return api.put("$base/api/workspaces/$workspaceId/settings", body)
    }

    suspend fun getActivity(workspaceId: String, limit: Int = 100): Result<ActivityResponseDto> {
        return api.get("$base/api/workspaces/$workspaceId/activity?limit=$limit")
    }

    suspend fun getTrackerSummary(workspaceId: String): Result<TrackerSummaryDto> {
        return api.get("$base/api/workspaces/$workspaceId/tracker/summary")
    }

    suspend fun getCalendar(workspaceId: String, from: String, to: String): Result<CalendarDayResponseDto> {
        return api.get("$base/api/workspaces/$workspaceId/tracker/calendar?from=$from&to=$to")
    }

    suspend fun getRecords(
        workspaceId: String,
        status: String? = null,
        type: String? = null,
        direction: String? = null,
        limit: Int = 100,
        offset: Int = 0
    ): Result<RecordListResponseDto> {
        val params = mutableListOf<String>()
        if (!status.isNullOrBlank()) params.add("status=$status")
        if (!type.isNullOrBlank()) params.add("type=$type")
        if (!direction.isNullOrBlank()) params.add("direction=$direction")
        params.add("limit=$limit")
        params.add("offset=$offset")
        val query = if (params.isEmpty()) "" else "?" + params.joinToString("&")
        return api.get("$base/api/workspaces/$workspaceId/records$query")
    }

    suspend fun getRecord(workspaceId: String, recordId: String): Result<BusinessRecordDto> {
        return api.get("$base/api/workspaces/$workspaceId/records/$recordId")
    }

    suspend fun createRecord(workspaceId: String, body: RecordInputDto): Result<BusinessRecordDto> {
        return api.post("$base/api/workspaces/$workspaceId/records", body)
    }

    suspend fun updateRecord(workspaceId: String, recordId: String, body: RecordUpdateDto): Result<BusinessRecordDto> {
        return api.patch("$base/api/workspaces/$workspaceId/records/$recordId", body)
    }

    suspend fun deleteRecord(workspaceId: String, recordId: String): Result<Unit> {
        return api.delete("$base/api/workspaces/$workspaceId/records/$recordId")
    }

    suspend fun deferRecord(workspaceId: String, recordId: String, dueAt: String, reason: String): Result<BusinessRecordDto> {
        return api.post(
            "$base/api/workspaces/$workspaceId/records/$recordId/defer",
            DeferRecordRequestDto(dueAt, reason)
        )
    }

    suspend fun getNotifications(workspaceId: String): Result<NotificationsResponseDto> {
        return api.get("$base/api/workspaces/$workspaceId/notifications")
    }

    suspend fun markNotificationRead(workspaceId: String, notificationId: String): Result<Unit> {
        return api.patch("$base/api/workspaces/$workspaceId/notifications/$notificationId/read")
    }

    suspend fun markAllNotificationsRead(workspaceId: String): Result<Unit> {
        return api.post("$base/api/workspaces/$workspaceId/notifications/read-all")
    }

    suspend fun getDocuments(workspaceId: String): Result<WorkspaceDocumentsResponseDto> {
        return api.get("$base/api/workspaces/$workspaceId/documents")
    }

    suspend fun deleteDocument(workspaceId: String, documentId: String): Result<Unit> {
        return api.delete("$base/api/workspaces/$workspaceId/documents/$documentId")
    }

    suspend fun updateDocumentMetadata(workspaceId: String, documentId: String, body: DocumentMetadataDto): Result<Unit> {
        return api.patch("$base/api/workspaces/$workspaceId/documents/$documentId", body)
    }
}