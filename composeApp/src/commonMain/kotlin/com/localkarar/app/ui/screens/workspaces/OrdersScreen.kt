package com.localkarar.app.ui.screens.workspaces

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.localkarar.app.network.dto.CreateOrderRequestDto
import com.localkarar.app.network.dto.OrderDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkNumericField
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.OrdersViewModel

private val ORDER_STATUS_FILTERS = listOf(
    null to "Tümü",
    "pending" to "Beklemede",
    "processing" to "Hazırlanıyor",
    "delivered" to "Teslim Edildi",
    "cancelled" to "İptal"
)

@Composable
fun OrdersScreen(
    workspaceId: String,
    viewModel: OrdersViewModel,
    onNavigateBack: () -> Unit
) {
    val orders by viewModel.orders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(workspaceId) {
        viewModel.loadOrders(workspaceId)
    }

    Scaffold(
        backgroundColor = LkSurfaceCanvas,
        topBar = {
            TopAppBar(
                backgroundColor = LkSurfacePanel,
                contentColor = LkTextPrimary,
                elevation = 0.dp,
                title = {
                    Text(
                        text = "Siparişler",
                        style = LkTypography.getSectionTitle()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Yeni Sipariş", tint = LkPrimary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Metrics Summary Row
            val totalAmount = orders.sumOf { it.totalAmount }
            val deliveredCount = orders.count { it.status == "delivered" }
            val pendingCount = orders.count { it.status == "pending" || it.status == "processing" }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space3),
                horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
            ) {
                MetricCard(
                    title = "Toplam Sipariş",
                    value = "${orders.size}",
                    color = LkPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Bekleyen",
                    value = "$pendingCount",
                    color = LkWarning,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Toplam Ciro",
                    value = "${totalAmount.toInt()} ₺",
                    color = LkSuccess,
                    modifier = Modifier.weight(1.2f)
                )
            }

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space2)
            ) {
                LkTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(workspaceId, it) },
                    placeholder = "Sipariş No veya Müşteri Ara...",
                    trailingContent = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = LkTextMuted)
                    }
                )
            }

            // Filter Chips Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space2),
                horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)
            ) {
                items(ORDER_STATUS_FILTERS) { (status, label) ->
                    val isSelected = selectedStatus == status
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) LkPrimary else LkSurfacePanel,
                                shape = LkShapes.SM
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) LkPrimary else LkLineSoft,
                                shape = LkShapes.SM
                            )
                            .clickable { viewModel.setStatusFilter(workspaceId, status) }
                            .padding(horizontal = LkSpacing.Space3, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            style = LkTypography.getMicro(),
                            color = if (isSelected) LkOnPrimary else LkTextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LkPrimary)
                }
            } else if (error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(LkSpacing.Space6),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = error!!, color = LkDanger, style = LkTypography.getBodyStrong())
                        Spacer(modifier = Modifier.height(LkSpacing.Space4))
                        LkButton(
                            text = "Tekrar Dene",
                            onClick = { viewModel.loadOrders(workspaceId) }
                        )
                    }
                }
            } else if (orders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(LkSpacing.Space8),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = LkTextMuted,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(LkSpacing.Space4))
                        Text(
                            text = "Henüz sipariş kaydı bulunmuyor.",
                            style = LkTypography.getSectionTitle(),
                            color = LkTextSecondary
                        )
                        Spacer(modifier = Modifier.height(LkSpacing.Space2))
                        Text(
                            text = "Müşterilerinizden gelen siparişleri ve teslimat süreçlerini buradan yönetin.",
                            style = LkTypography.getBodySmall(),
                            color = LkTextMuted
                        )
                        Spacer(modifier = Modifier.height(LkSpacing.Space6))
                        LkButton(
                            text = "+ Yeni Sipariş Ekle",
                            onClick = { showCreateDialog = true }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space2),
                    verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
                ) {
                    items(orders, key = { it.id }) { order ->
                        OrderCard(
                            order = order,
                            onStatusChange = { newStatus ->
                                viewModel.updateStatus(workspaceId, order.id, newStatus)
                            },
                            onDelete = {
                                viewModel.deleteOrder(workspaceId, order.id)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateOrderDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { req ->
                viewModel.createOrder(workspaceId, req) {
                    showCreateDialog = false
                }
            }
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(LkSurfacePanel, LkShapes.MD)
            .border(1.dp, LkLineSoft, LkShapes.MD)
            .padding(LkSpacing.Space3)
    ) {
        Column {
            Text(text = title, style = LkTypography.getMicro(), color = LkTextSecondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, style = LkTypography.getBodyStrong(), color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun OrderCard(
    order: OrderDto,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    val statusColor = when (order.status) {
        "delivered" -> LkSuccess
        "processing" -> LkPrimary
        "cancelled" -> LkDanger
        else -> LkWarning
    }

    val statusText = when (order.status) {
        "delivered" -> "Teslim Edildi"
        "processing" -> "Hazırlanıyor"
        "cancelled" -> "İptal Edildi"
        else -> "Beklemede"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(LkSurfacePanel, LkShapes.MD)
            .border(1.dp, LkLineSoft, LkShapes.MD)
            .padding(LkSpacing.Space4)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = order.orderNumber,
                        style = LkTypography.getBodyStrong(),
                        color = LkTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(LkSpacing.Space2))
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.15f), LkShapes.SM)
                            .border(1.dp, statusColor.copy(alpha = 0.3f), LkShapes.SM)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = statusText,
                            style = LkTypography.getMicro(),
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { expandedMenu = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "İşlemler", tint = LkTextMuted)
                    }
                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false },
                        modifier = Modifier.background(LkSurfacePanel)
                    ) {
                        DropdownMenuItem(onClick = { expandedMenu = false; onStatusChange("processing") }) {
                            Text("Hazırlanıyor Yap", style = LkTypography.getBodySmall(), color = LkTextPrimary)
                        }
                        DropdownMenuItem(onClick = { expandedMenu = false; onStatusChange("delivered") }) {
                            Text("Teslim Edildi Yap", style = LkTypography.getBodySmall(), color = LkSuccess)
                        }
                        DropdownMenuItem(onClick = { expandedMenu = false; onStatusChange("cancelled") }) {
                            Text("İptal Et", style = LkTypography.getBodySmall(), color = LkDanger)
                        }
                        Divider(color = LkLineSoft)
                        DropdownMenuItem(onClick = { expandedMenu = false; onDelete() }) {
                            Text("Siparişi Sil", style = LkTypography.getBodySmall(), color = LkDanger)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(LkSpacing.Space2))

            Text(
                text = "Müşteri: ${order.customerName}",
                style = LkTypography.getBody(),
                color = LkTextPrimary
            )

            if (!order.notes.isNullOrBlank()) {
                Text(
                    text = order.notes,
                    style = LkTypography.getMicro(),
                    color = LkTextSecondary,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(LkSpacing.Space3))
            Divider(color = LkLineSoft)
            Spacer(modifier = Modifier.height(LkSpacing.Space2))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.deliveryDate?.take(10) ?: "Tarih belirtilmedi",
                    style = LkTypography.getMicro(),
                    color = LkTextMuted
                )
                Text(
                    text = "${order.totalAmount.toInt()} ${order.currency}",
                    style = LkTypography.getBodyStrong(),
                    color = LkPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CreateOrderDialog(
    onDismiss: () -> Unit,
    onConfirm: (CreateOrderRequestDto) -> Unit
) {
    var orderNumber by remember { mutableStateOf("SIP-${kotlin.random.Random.nextInt(1000, 9999)}") }
    var customerName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var deliveryDate by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(LkSurfacePanel, LkShapes.MD)
                .border(1.dp, LkLineStrong, LkShapes.MD)
                .padding(LkSpacing.Space6)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Yeni Sipariş Oluştur",
                    style = LkTypography.getSectionTitle(),
                    color = LkTextPrimary
                )
                Spacer(modifier = Modifier.height(LkSpacing.Space4))

                LkTextField(
                    value = orderNumber,
                    onValueChange = { orderNumber = it },
                    label = "Sipariş No"
                )
                Spacer(modifier = Modifier.height(LkSpacing.Space3))

                LkTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = "Müşteri Adı / Ünvanı",
                    placeholder = "Ahmet Yılmaz veya Şirket A.Ş."
                )
                Spacer(modifier = Modifier.height(LkSpacing.Space3))

                LkNumericField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = "Toplam Tutar (TRY)",
                    placeholder = "0.00"
                )
                Spacer(modifier = Modifier.height(LkSpacing.Space3))

                LkTextField(
                    value = deliveryDate,
                    onValueChange = { deliveryDate = it },
                    label = "Teslimat Tarihi",
                    placeholder = "YYYY-AA-GG"
                )
                Spacer(modifier = Modifier.height(LkSpacing.Space3))

                LkTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Sipariş Notu (Opsiyonel)",
                    placeholder = "Adres veya özel teslimat detayları"
                )
                Spacer(modifier = Modifier.height(LkSpacing.Space6))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
                ) {
                    LkButton(
                        text = "İptal",
                        variant = LkButtonVariant.SECONDARY,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    LkButton(
                        text = "Kaydet",
                        onClick = {
                            val parsedAmount = amount.toDoubleOrNull() ?: 0.0
                            onConfirm(
                                CreateOrderRequestDto(
                                    orderNumber = orderNumber,
                                    customerName = customerName,
                                    totalAmount = parsedAmount,
                                    deliveryDate = deliveryDate.ifBlank { null },
                                    notes = notes.ifBlank { null }
                                )
                            )
                        },
                        enabled = customerName.isNotBlank() && amount.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
