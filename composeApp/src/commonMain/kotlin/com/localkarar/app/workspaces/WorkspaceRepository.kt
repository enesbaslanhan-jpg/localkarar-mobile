package com.localkarar.app.workspaces

import com.localkarar.app.network.ApiConfig
import com.localkarar.app.network.SafeApiClient
import com.localkarar.app.network.dto.*

class WorkspaceRepository(private val api: SafeApiClient) {

    private val base = ApiConfig.baseUrl

    suspend fun listWorkspaces(): Result<WorkspaceListResponseDto> {
        return api.get("$base/workspaces")
    }

    suspend fun createWorkspace(body: CreateWorkspaceRequestDto): Result<WorkspaceDetailDto> {
        return api.post("$base/workspaces", body)
    }

    suspend fun getWorkspace(workspaceId: String): Result<WorkspaceDetailDto> {
        return api.get("$base/workspaces/$workspaceId")
    }

    suspend fun updateWorkspace(workspaceId: String, body: UpdateWorkspaceRequestDto): Result<WorkspaceDetailDto> {
        return api.put("$base/workspaces/$workspaceId", body)
    }

    suspend fun deleteWorkspace(workspaceId: String): Result<Unit> {
        return api.delete("$base/workspaces/$workspaceId")
    }

    suspend fun getMembers(workspaceId: String): Result<List<WorkspaceMemberDto>> {
        return api.get("$base/workspaces/$workspaceId/members")
    }

    suspend fun updateMemberRole(workspaceId: String, memberId: String, role: String): Result<Unit> {
        return api.put(
            "$base/workspaces/$workspaceId/members/$memberId/role",
            UpdateMemberRoleRequestDto(role)
        )
    }

    suspend fun removeMember(workspaceId: String, memberId: String): Result<Unit> {
        return api.delete("$base/workspaces/$workspaceId/members/$memberId")
    }

    suspend fun getInvitations(workspaceId: String): Result<List<BusinessInvitationDto>> {
        return api.get("$base/workspaces/$workspaceId/invitations")
    }

    suspend fun inviteMember(workspaceId: String, email: String, role: String): Result<Unit> {
        return api.post("$base/workspaces/$workspaceId/members", InviteMemberRequestDto(email, role))
    }

    suspend fun cancelInvitation(workspaceId: String, invitationId: String): Result<Unit> {
        return api.delete("$base/workspaces/$workspaceId/invitations/$invitationId")
    }

    suspend fun getContacts(workspaceId: String): Result<List<BusinessContactDto>> {
        return api.get("$base/workspaces/$workspaceId/contacts")
    }

    suspend fun createContact(workspaceId: String, body: ContactInputDto): Result<BusinessContactDto> {
        return api.post("$base/workspaces/$workspaceId/contacts", body)
    }

    suspend fun updateContact(workspaceId: String, contactId: String, body: ContactInputDto): Result<Unit> {
        return api.put("$base/workspaces/$workspaceId/contacts/$contactId", body)
    }

    suspend fun deleteContact(workspaceId: String, contactId: String): Result<Unit> {
        return api.delete("$base/workspaces/$workspaceId/contacts/$contactId")
    }

    suspend fun getSettings(workspaceId: String): Result<WorkspaceSettingsDto> {
        return api.get("$base/workspaces/$workspaceId/settings")
    }

    suspend fun updateSettings(workspaceId: String, body: UpdateSettingsRequestDto): Result<WorkspaceSettingsDto> {
        return api.put("$base/workspaces/$workspaceId/settings", body)
    }

    suspend fun getActivity(workspaceId: String, limit: Int = 100): Result<ActivityResponseDto> {
        return api.get("$base/workspaces/$workspaceId/activity?limit=$limit")
    }

    suspend fun getTrackerSummary(workspaceId: String): Result<TrackerSummaryDto> {
        return api.get("$base/workspaces/$workspaceId/tracker/summary")
    }

    suspend fun getCalendar(workspaceId: String, from: String, to: String): Result<CalendarDayResponseDto> {
        return api.get("$base/workspaces/$workspaceId/tracker/calendar?from=$from&to=$to")
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
        return api.get("$base/workspaces/$workspaceId/records$query")
    }

    suspend fun getRecord(workspaceId: String, recordId: String): Result<BusinessRecordDto> {
        return api.get("$base/workspaces/$workspaceId/records/$recordId")
    }

    suspend fun createRecord(workspaceId: String, body: RecordInputDto): Result<BusinessRecordDto> {
        return api.post("$base/workspaces/$workspaceId/records", body)
    }

    suspend fun updateRecord(workspaceId: String, recordId: String, body: RecordUpdateDto): Result<BusinessRecordDto> {
        return api.patch("$base/workspaces/$workspaceId/records/$recordId", body)
    }

    suspend fun deleteRecord(workspaceId: String, recordId: String): Result<Unit> {
        return api.delete("$base/workspaces/$workspaceId/records/$recordId")
    }

    suspend fun deferRecord(workspaceId: String, recordId: String, dueAt: String, reason: String): Result<BusinessRecordDto> {
        return api.post(
            "$base/workspaces/$workspaceId/records/$recordId/defer",
            DeferRecordRequestDto(dueAt, reason)
        )
    }

    suspend fun getNotifications(workspaceId: String): Result<NotificationsResponseDto> {
        return api.get("$base/workspaces/$workspaceId/notifications")
    }

    suspend fun markNotificationRead(workspaceId: String, notificationId: String): Result<Unit> {
        return api.patch("$base/workspaces/$workspaceId/notifications/$notificationId/read")
    }

    suspend fun markAllNotificationsRead(workspaceId: String): Result<Unit> {
        return api.post("$base/workspaces/$workspaceId/notifications/read-all")
    }

    suspend fun getDocuments(workspaceId: String): Result<WorkspaceDocumentsResponseDto> {
        return api.get("$base/workspaces/$workspaceId/documents")
    }

    suspend fun deleteDocument(workspaceId: String, documentId: String): Result<Unit> {
        return api.delete("$base/workspaces/$workspaceId/documents/$documentId")
    }

    suspend fun updateDocumentMetadata(workspaceId: String, documentId: String, body: DocumentMetadataDto): Result<Unit> {
        return api.patch("$base/workspaces/$workspaceId/documents/$documentId", body)
    }

    // ========================================================================
    // ORDERS (SİPARİŞLER)
    // ========================================================================

    suspend fun getOrders(
        workspaceId: String,
        status: String? = null,
        query: String? = null
    ): Result<OrderListResponseDto> {
        val recordsResult = getRecords(workspaceId, limit = 200)
        return recordsResult.map { resp ->
            val commerceRecords = resp.records.filter { 
                it.type == "shipment" || it.type == "purchase" || it.type == "order" || it.type == "payment"
            }
            val orders = commerceRecords.map { rec ->
                val orderStatus = when (rec.status) {
                    "completed" -> "delivered"
                    "in_progress" -> "processing"
                    "cancelled" -> "cancelled"
                    else -> "pending"
                }
                OrderDto(
                    id = rec.id,
                    workspaceId = rec.workspaceId,
                    orderNumber = "SIP-${rec.id.takeLast(6).uppercase()}",
                    customerName = rec.contact?.name ?: rec.title,
                    contactId = rec.contactId,
                    itemsCount = 1,
                    totalAmount = rec.amount ?: 0.0,
                    currency = rec.currency,
                    status = orderStatus,
                    paymentStatus = if (rec.status == "completed") "paid" else "unpaid",
                    orderDate = rec.createdAt,
                    deliveryDate = rec.dueAt,
                    notes = rec.description
                )
            }
            val filtered = if (!status.isNullOrBlank()) {
                orders.filter { it.status.equals(status, ignoreCase = true) }
            } else orders
            val searched = if (!query.isNullOrBlank()) {
                filtered.filter { 
                    it.orderNumber.contains(query, ignoreCase = true) || 
                    it.customerName.contains(query, ignoreCase = true) 
                }
            } else filtered
            OrderListResponseDto(orders = searched, total = searched.size)
        }
    }

    suspend fun createOrder(workspaceId: String, body: CreateOrderRequestDto): Result<OrderDto> {
        val recordInput = RecordInputDto(
            type = "shipment",
            title = "${body.orderNumber} - ${body.customerName}",
            description = body.notes,
            direction = "receivable",
            amount = body.totalAmount,
            currency = body.currency,
            priority = "normal",
            dueAt = body.deliveryDate,
            contactId = body.contactId
        )
        return createRecord(workspaceId, recordInput).map { rec ->
            OrderDto(
                id = rec.id,
                workspaceId = rec.workspaceId,
                orderNumber = body.orderNumber,
                customerName = body.customerName,
                contactId = body.contactId,
                itemsCount = body.items.size.coerceAtLeast(1),
                totalAmount = body.totalAmount,
                currency = body.currency,
                status = body.status,
                paymentStatus = body.paymentStatus,
                orderDate = rec.createdAt,
                deliveryDate = body.deliveryDate,
                notes = body.notes,
                items = body.items
            )
        }
    }

    suspend fun updateOrderStatus(workspaceId: String, orderId: String, status: String): Result<Unit> {
        val recordStatus = when (status) {
            "delivered" -> "completed"
            "processing" -> "in_progress"
            "cancelled" -> "cancelled"
            else -> "open"
        }
        return updateRecord(workspaceId, orderId, RecordUpdateDto(status = recordStatus)).map { }
    }

    suspend fun deleteOrder(workspaceId: String, orderId: String): Result<Unit> {
        return deleteRecord(workspaceId, orderId)
    }

    // ========================================================================
    // PRODUCTS (ÜRÜNLER)
    // ========================================================================

    private val localProducts = mutableMapOf<String, MutableList<ProductDto>>()

    suspend fun getProducts(
        workspaceId: String,
        category: String? = null,
        query: String? = null
    ): Result<ProductListResponseDto> {
        val list = localProducts.getOrPut(workspaceId) {
            mutableListOf(
                ProductDto(
                    id = "p-1",
                    workspaceId = workspaceId,
                    code = "PRD-101",
                    name = "Standart Hizmet Paketi",
                    category = "Hizmet",
                    price = 4500.0,
                    costPrice = 1200.0,
                    stockQuantity = 99,
                    status = "active",
                    description = "Aylık standart danışmanlık ve operasyon paketi"
                ),
                ProductDto(
                    id = "p-2",
                    workspaceId = workspaceId,
                    code = "PRD-102",
                    name = "Premium Karar ve Analiz Lisansı",
                    category = "Yazılım",
                    price = 12500.0,
                    costPrice = 3000.0,
                    stockQuantity = 45,
                    status = "active",
                    description = "Yıllık tam erişim kurumsal karar araçları"
                ),
                ProductDto(
                    id = "p-3",
                    workspaceId = workspaceId,
                    code = "PRD-103",
                    name = "Operasyonel Destek Kiti",
                    category = "Donanım",
                    price = 850.0,
                    costPrice = 400.0,
                    stockQuantity = 3,
                    minStockLevel = 5,
                    status = "active",
                    description = "Fiziki operasyon takip donanım seti"
                )
            )
        }
        val filtered = if (!category.isNullOrBlank() && category != "Tümü") {
            list.filter { it.category.equals(category, ignoreCase = true) }
        } else list
        val searched = if (!query.isNullOrBlank()) {
            filtered.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.code.contains(query, ignoreCase = true) 
            }
        } else filtered
        return Result.success(ProductListResponseDto(products = searched, total = searched.size))
    }

    suspend fun createProduct(workspaceId: String, body: CreateProductRequestDto): Result<ProductDto> {
        val newProduct = ProductDto(
            id = "p-${kotlin.random.Random.nextInt(1000, 9999)}",
            workspaceId = workspaceId,
            code = body.code,
            name = body.name,
            category = body.category,
            price = body.price,
            costPrice = body.costPrice,
            currency = body.currency,
            stockQuantity = body.stockQuantity,
            minStockLevel = body.minStockLevel,
            unit = body.unit,
            status = body.status,
            description = body.description,
            createdAt = "2026-08-27T00:00:00Z"
        )
        val list = localProducts.getOrPut(workspaceId) { mutableListOf() }
        list.add(0, newProduct)
        return Result.success(newProduct)
    }

    suspend fun updateProduct(workspaceId: String, productId: String, body: UpdateProductRequestDto): Result<ProductDto> {
        val list = localProducts.getOrPut(workspaceId) { mutableListOf() }
        val idx = list.indexOfFirst { it.id == productId }
        if (idx == -1) return Result.failure(Exception("Ürün bulunamadı."))
        val old = list[idx]
        val updated = old.copy(
            code = body.code ?: old.code,
            name = body.name ?: old.name,
            category = body.category ?: old.category,
            price = body.price ?: old.price,
            costPrice = body.costPrice ?: old.costPrice,
            currency = body.currency ?: old.currency,
            stockQuantity = body.stockQuantity ?: old.stockQuantity,
            minStockLevel = body.minStockLevel ?: old.minStockLevel,
            unit = body.unit ?: old.unit,
            status = body.status ?: old.status,
            description = body.description ?: old.description
        )
        list[idx] = updated
        return Result.success(updated)
    }

    suspend fun deleteProduct(workspaceId: String, productId: String): Result<Unit> {
        val list = localProducts.getOrPut(workspaceId) { mutableListOf() }
        list.removeAll { it.id == productId }
        return Result.success(Unit)
    }
}