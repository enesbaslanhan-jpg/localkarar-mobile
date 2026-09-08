package com.localkarar.app.ui.screens.workspaces

import com.localkarar.app.ui.components.LkLoadingSpinner
import com.localkarar.app.ui.components.LkFilterBar
import com.localkarar.app.ui.components.LkFilterGrup
import com.localkarar.app.ui.components.LkFilterSecenek
import com.localkarar.app.ui.components.LkHeroPage
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.localkarar.app.network.dto.OrderDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkCard
import com.localkarar.app.ui.components.LkPressable
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.DESTEKLENEN_SAGLAYICILAR
import com.localkarar.app.workspaces.TUM_SAGLAYICILAR
import com.localkarar.app.workspaces.OrdersViewModel

// WOOCOMMERCE KALDIRILDI: sunucu enum'u (ordersQuery, marketplace-routes.ts)
// yalniz dort saglayiciyi kabul ediyor; secildiginde 422 donuyordu ve hata
// uydurma listeye dusuruldugu icin hic gorunmuyordu.
private val PROVIDER_OPTIONS = listOf(TUM_SAGLAYICILAR) + DESTEKLENEN_SAGLAYICILAR

private val STATUS_OPTIONS = listOf(
    null to "Tümü",
    "CREATED" to "Yeni",
    "PROCESSING" to "İşleniyor",
    "SHIPPED" to "Kargoda",
    "DELIVERED" to "Teslim Edildi",
    "CANCELLED" to "İptal",
    "RETURNED" to "İade",
    "PARTIALLY_RETURNED" to "Kısmi İade"
)

@Composable
fun OrdersScreen(
    workspaceId: String,
    viewModel: OrdersViewModel,
    onNavigateBack: () -> Unit
) {
    val orders by viewModel.orders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val lastSyncedAt by viewModel.lastSyncedAt.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedProvider by viewModel.selectedProvider.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    // Note: searchQuery not present in canonical Web Orders.jsx (status is a deep-link filter)

    var selectedOrderDetail by remember { mutableStateOf<OrderDto?>(null) }

    LaunchedEffect(workspaceId) {
        viewModel.loadOrders(workspaceId)
    }

    /*
     * §0 IHLALI DUZELTILDI: burada ham `Scaffold` + `TopAppBar` vardi.
     * Material kendi olcu, renk ve yukseltme sistemini getiriyordu; ekran
     * uygulamanin geri kalanindan farkli gorunuyordu.
     *
     * Son esitleme zamani `heroExtra`da: baslik degil, baslikla ilgili bir
     * DURUM. Baslik satirina sikistirilinca iki satira kirilip cubugun
     * yuksekligini degistiriyordu.
     */
    LkHeroPage(
        title = "Siparişler",
        onBack = onNavigateBack,
        actions = {
            if (isSyncing) {
                LkLoadingSpinner(
                    modifier = Modifier.padding(end = 12.dp),
                    size = 20.dp,
                    renk = LkHero.OnHero
                )
            } else {
                IconButton(onClick = { viewModel.syncNow(workspaceId) }) {
                    Icon(
                        Icons.Outlined.Sync,
                        contentDescription = "Şimdi Eşitle",
                        tint = LkHero.OnHero
                    )
                }
            }
        },
        heroExtra = {
            if (!lastSyncedAt.isNullOrBlank() || orders.isNotEmpty()) {
                Text(
                    text = buildString {
                        append("${orders.size} sipariş")
                        lastSyncedAt?.let { append(" · Son eşitleme ${it.take(16).replace("T", " ")}") }
                    },
                    style = LkTypography.getMetadata(),
                    color = LkHero.OnHeroSecondary,
                    modifier = Modifier.padding(
                        start = LkSpacing.Space5,
                        top = LkSpacing.Space2
                    )
                )
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Integration Sync Status Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LkSurfacePanel)
                    .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space2),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(LkSuccess, LkShapes.FULL)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Entegrasyonlar Aktif",
                        style = LkTypography.getMicro(),
                        color = LkTextSecondary
                    )
                }

                Text(
                    text = "Şimdi Eşitle",
                    style = LkTypography.getMicro(),
                    color = LkPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.syncNow(workspaceId) }
                )
            }

            /*
             * 🔴 IKI SIRA MINIK HAP VARDI (11sp yazi, ~24dp yukseklik).
             * Dokunma hedegi §19'un 44dp esiginin yarisi kadardi ve
             * ekranin ustunde iki sira yer kapliyordu. Artik tek serit:
             * durum haplari gorunur, saglayici "Filtreler"in arkasinda.
             */
            LkFilterBar(
                birincil = STATUS_OPTIONS.map { (deger, etiket) -> LkFilterSecenek(etiket, deger) },
                seciliBirincil = selectedStatus,
                onBirincil = { viewModel.setStatusFilter(it) },
                gruplar = listOf(
                    LkFilterGrup(
                        baslik = "PAZARYERİ",
                        secenekler = PROVIDER_OPTIONS.map { saglayici ->
                            LkFilterSecenek(
                                saglayici,
                                if (saglayici == TUM_SAGLAYICILAR) null else saglayici
                            )
                        },
                        secili = selectedProvider,
                        onSecim = { viewModel.setProviderFilter(workspaceId, it) }
                    )
                ),
                modifier = Modifier.padding(vertical = LkSpacing.Space2)
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LkLoadingSpinner(size = 26.dp)
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
                            imageVector = Icons.Outlined.ReceiptLong,
                            contentDescription = null,
                            tint = LkTextMuted,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(LkSpacing.Space4))
                        Text(
                            text = "Pazaryeri siparişi bulunamadı.",
                            style = LkTypography.getSectionTitle(),
                            color = LkTextSecondary
                        )
                        Spacer(modifier = Modifier.height(LkSpacing.Space2))
                        Text(
                            text = "Bağlı Trendyol, Hepsiburada veya N11 mağazalarınızdan siparişleri çekmek için 'Şimdi Eşitle' butonuna dokunun.",
                            style = LkTypography.getBodySmall(),
                            color = LkTextMuted
                        )
                        Spacer(modifier = Modifier.height(LkSpacing.Space6))
                        LkButton(
                            text = "Şimdi Eşitle",
                            onClick = { viewModel.syncNow(workspaceId) }
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
                        MarketplaceOrderCard(
                            order = order,
                            onClick = { selectedOrderDetail = order }
                        )
                    }
                }
            }
        }
    }

    if (selectedOrderDetail != null) {
        OrderDetailDialog(
            order = selectedOrderDetail!!,
            onDismiss = { selectedOrderDetail = null }
        )
    }
}

@Composable
private fun MarketplaceOrderCard(
    order: OrderDto,
    onClick: () -> Unit
) {
    val providerColor = when (order.provider.uppercase()) {
        "TRENDYOL" -> Color(0xFFF27A1A)
        "HEPSIBURADA" -> Color(0xFFFF6000)
        "N11" -> Color(0xFF5E2D91)
        "SHOPIFY" -> Color(0xFF96BF48)
        else -> LkPrimary
    }

    val (statusColor, statusLabel) = when (order.status.uppercase()) {
        "DELIVERED" -> LkSuccess to "Teslim Edildi"
        "SHIPPED" -> LkPrimary to "Kargoda"
        "PROCESSING" -> LkWarning to "İşleniyor"
        "CANCELLED" -> LkDanger to "İptal Edildi"
        "RETURNED" -> Color(0xFFE91E63) to "İade Edildi"
        "PARTIALLY_RETURNED" -> Color(0xFFFF9800) to "Kısmi İade"
        else -> LkTextSecondary to "Yeni Sipariş"
    }

    /*
     * §24: kenarlikli kabuk degil yukseltilmis yuzey. Tiklama LkCard'in
     * disinda kaldigi icin basma geri bildirimi LkPressable'dan geliyor.
     */
    LkPressable(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
      LkCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(providerColor.copy(alpha = 0.15f), LkShapes.SM)
                            .border(1.dp, providerColor.copy(alpha = 0.3f), LkShapes.SM)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = order.provider,
                            style = LkTypography.getMicro(),
                            color = providerColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(LkSpacing.Space2))
                    Text(
                        text = order.orderNumber ?: "Sipariş no yok",
                        style = LkTypography.getBodyStrong(),
                        color = LkTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), LkShapes.SM)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = statusLabel,
                        style = LkTypography.getMicro(),
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(LkSpacing.Space2))

            if (!order.customerName.isNullOrBlank()) {
                Text(
                    text = "Müşteri: ${order.customerName}",
                    style = LkTypography.getBody(),
                    color = LkTextPrimary
                )
            }

            Text(
                text = "Tarih: ${order.orderDate?.take(10) ?: "-"}",
                style = LkTypography.getMicro(),
                color = LkTextMuted
            )

            Spacer(modifier = Modifier.height(LkSpacing.Space3))
            Divider(color = LkLineSoft)
            Spacer(modifier = Modifier.height(LkSpacing.Space2))

            // Financial Breakdown Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    if (order.grossAmount != null) {
                        Text(
                            text = "Brüt: ${order.grossAmount.toInt()} ${order.currency ?: "TRY"}",
                            style = LkTypography.getMicro(),
                            color = LkTextSecondary
                        )
                    }
                    if (order.commission != null) {
                        Text(
                            text = "Komisyon: -${order.commission.toInt()} ₺",
                            style = LkTypography.getMicro(),
                            color = LkDanger
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Net Katkı",
                        style = LkTypography.getMicro(),
                        color = LkTextMuted
                    )
                    Text(
                        text = "${order.netContribution?.toInt() ?: order.grossAmount?.toInt() ?: 0} ${order.currency ?: "TRY"}",
                        style = LkTypography.getBodyStrong(),
                        color = LkSuccess,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
      }
    }
}

@Composable
private fun OrderDetailDialog(
    order: OrderDto,
    onDismiss: () -> Unit
) {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sipariş Detayı",
                        style = LkTypography.getSectionTitle(),
                        color = LkTextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Outlined.Close, contentDescription = "Kapat", tint = LkTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(LkSpacing.Space4))

                Text(text = "Sipariş No: ${order.orderNumber ?: "—"}", style = LkTypography.getBodyStrong(), color = LkTextPrimary)
                Text(text = "Pazaryeri: ${order.provider}", style = LkTypography.getBodySmall(), color = LkPrimary)
                Text(text = "Müşteri: ${order.customerName ?: "-"}", style = LkTypography.getBodySmall(), color = LkTextSecondary)
                Text(text = "Tarih: ${order.orderDate?.take(16)?.replace("T", " ") ?: "-"}", style = LkTypography.getBodySmall(), color = LkTextMuted)

                Spacer(modifier = Modifier.height(LkSpacing.Space4))
                Divider(color = LkLineSoft)
                Spacer(modifier = Modifier.height(LkSpacing.Space3))

                Text(text = "Sipariş Kalemleri", style = LkTypography.getBodyStrong(), color = LkTextPrimary)
                Spacer(modifier = Modifier.height(LkSpacing.Space2))

                order.items.forEach { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LkSurfaceCanvas, LkShapes.SM)
                            .padding(LkSpacing.Space3)
                    ) {
                        Column {
                            Text(text = item.title ?: "Ürün adı yok", style = LkTypography.getBodySmall(), color = LkTextPrimary, fontWeight = FontWeight.SemiBold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "SKU: ${item.sku ?: "-"} | Adet: ${item.quantity}", style = LkTypography.getMicro(), color = LkTextMuted)
                                Text(text = "${item.totalPrice?.toInt() ?: 0} ${order.currency ?: "TRY"}", style = LkTypography.getBodySmall(), color = LkTextPrimary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(LkSpacing.Space2))
                }

                Spacer(modifier = Modifier.height(LkSpacing.Space3))
                Divider(color = LkLineSoft)
                Spacer(modifier = Modifier.height(LkSpacing.Space3))

                Text(text = "Finansal Dağılım", style = LkTypography.getBodyStrong(), color = LkTextPrimary)
                Spacer(modifier = Modifier.height(LkSpacing.Space2))

                DetailRow(label = "Brüt Tutar", value = "${order.grossAmount?.toInt() ?: 0} ${order.currency ?: "TRY"}", color = LkTextPrimary)
                if (order.commission != null) {
                    DetailRow(label = "Komisyon Kesintisi", value = "-${order.commission.toInt()} ₺", color = LkDanger)
                }
                if (order.shipping != null) {
                    DetailRow(label = "Kargo Kesintisi", value = "-${order.shipping.toInt()} ₺", color = LkDanger)
                }
                if (order.refund != null) {
                    DetailRow(label = "İade Tutarı", value = "-${order.refund.toInt()} ₺", color = LkDanger)
                }
                Divider(color = LkLineSoft, modifier = Modifier.padding(vertical = 4.dp))
                DetailRow(
                    label = "Net İşletme Katkısı",
                    value = "${order.netContribution?.toInt() ?: order.grossAmount?.toInt() ?: 0} ${order.currency ?: "TRY"}",
                    color = LkSuccess,
                    isBold = true
                )

                Spacer(modifier = Modifier.height(LkSpacing.Space6))

                LkButton(
                    text = "Kapat",
                    variant = LkButtonVariant.SECONDARY,
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    color: Color,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = LkTypography.getBodySmall(), color = LkTextSecondary)
        Text(
            text = value,
            style = LkTypography.getBodySmall(),
            color = color,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}
