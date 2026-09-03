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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.localkarar.app.network.dto.ProductDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkNumericField
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.DESTEKLENEN_SAGLAYICILAR
import com.localkarar.app.workspaces.TUM_SAGLAYICILAR
import com.localkarar.app.workspaces.ProductsViewModel

private val PROVIDERS = listOf(TUM_SAGLAYICILAR) + DESTEKLENEN_SAGLAYICILAR

private val WINDOW_OPTIONS = listOf(
    "7" to "7 Gün",
    "30" to "30 Gün",
    "90" to "90 Gün"
)

private val SORT_OPTIONS = listOf(
    "default" to "Varsayılan",
    "bestSelling" to "En Çok Satan",
    "topRevenue" to "En Çok Ciro",
    "mostReturned" to "En Çok İade"
)

@Composable
fun ProductsScreen(
    workspaceId: String,
    viewModel: ProductsViewModel,
    onNavigateBack: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedProvider by viewModel.selectedProvider.collectAsState()
    val selectedOnSale by viewModel.selectedOnSale.collectAsState()
    val selectedStockFilter by viewModel.selectedStockFilter.collectAsState()
    val selectedWindowDays by viewModel.selectedWindowDays.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var editingSettingsProduct by remember { mutableStateOf<ProductDto?>(null) }

    LaunchedEffect(workspaceId) {
        viewModel.loadProducts(workspaceId)
    }

    Scaffold(
        backgroundColor = LkSurfaceCanvas,
        topBar = {
            TopAppBar(
                backgroundColor = LkSurfacePanel,
                contentColor = LkTextPrimary,
                elevation = 0.dp,
                title = {
                    Column {
                        Text(
                            text = "Pazaryeri Ürünleri",
                            style = LkTypography.getSectionTitle()
                        )
                        Text(
                            text = "Katalog & Satış Performansı",
                            style = LkTypography.getMicro(),
                            color = LkTextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
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
            // Search Bar (Title, SKU, Barcode)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space2)
            ) {
                LkTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(workspaceId, it) },
                    placeholder = "Başlık, SKU veya Barkod ile Ara...",
                    trailingContent = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = LkTextMuted)
                    }
                )
            }

            // Provider Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LkSpacing.Space4, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)
            ) {
                items(PROVIDERS) { provider ->
                    val isSelected = (selectedProvider == null && provider == TUM_SAGLAYICILAR) || (selectedProvider == provider)
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
                            .clickable {
                                viewModel.setProviderFilter(workspaceId, if (provider == TUM_SAGLAYICILAR) null else provider)
                            }
                            .padding(horizontal = LkSpacing.Space3, vertical = 6.dp)
                    ) {
                        Text(
                            text = provider,
                            style = LkTypography.getMicro(),
                            color = if (isSelected) LkOnPrimary else LkTextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Sale & Stock Status Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LkSpacing.Space4, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)
            ) {
                item {
                    StatusFilterChip(
                        label = "Tümü",
                        isSelected = selectedOnSale == null && selectedStockFilter == null,
                        onClick = {
                            viewModel.setOnSaleFilter(workspaceId, null)
                            viewModel.setStockFilter(workspaceId, null)
                        }
                    )
                }
                item {
                    StatusFilterChip(
                        label = "Satışta",
                        isSelected = selectedOnSale == true && selectedStockFilter == null,
                        onClick = {
                            viewModel.setOnSaleFilter(workspaceId, true)
                            viewModel.setStockFilter(workspaceId, null)
                        }
                    )
                }
                item {
                    StatusFilterChip(
                        label = "Satışta Değil",
                        isSelected = selectedOnSale == false && selectedStockFilter == null,
                        onClick = {
                            viewModel.setOnSaleFilter(workspaceId, false)
                            viewModel.setStockFilter(workspaceId, null)
                        }
                    )
                }
                item {
                    StatusFilterChip(
                        label = "Kritik Stok",
                        isSelected = selectedStockFilter == "low",
                        onClick = {
                            viewModel.setOnSaleFilter(workspaceId, null)
                            viewModel.setStockFilter(workspaceId, "low")
                        }
                    )
                }
                item {
                    StatusFilterChip(
                        label = "Stok Tükendi",
                        isSelected = selectedStockFilter == "out",
                        onClick = {
                            viewModel.setOnSaleFilter(workspaceId, null)
                            viewModel.setStockFilter(workspaceId, "out")
                        }
                    )
                }
            }

            // Performance Window & Sorting Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LkSpacing.Space4, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Performance Window Chips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    WINDOW_OPTIONS.forEach { (windowKey, windowLabel) ->
                        val isSelected = selectedWindowDays == windowKey
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isSelected) LkPrimary.copy(alpha = 0.2f) else LkSurfacePanel,
                                    shape = LkShapes.SM
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) LkPrimary else LkLineSoft,
                                    shape = LkShapes.SM
                                )
                                .clickable { viewModel.setPerformanceWindow(workspaceId, windowKey) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = windowLabel,
                                style = LkTypography.getMicro(),
                                color = if (isSelected) LkPrimary else LkTextMuted,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Sort Chips (horizontal scrollable if needed)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(SORT_OPTIONS) { (sortKey, sortLabel) ->
                        val isSelected = selectedSort == sortKey
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isSelected) LkSuccess.copy(alpha = 0.15f) else LkSurfacePanel,
                                    shape = LkShapes.SM
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) LkSuccess else LkLineSoft,
                                    shape = LkShapes.SM
                                )
                                .clickable { viewModel.setSort(workspaceId, sortKey) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = sortLabel,
                                style = LkTypography.getMicro(),
                                color = if (isSelected) LkSuccess else LkTextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

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
                            onClick = { viewModel.loadProducts(workspaceId) }
                        )
                    }
                }
            } else if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(LkSpacing.Space8),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = LkTextMuted,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(LkSpacing.Space4))
                        Text(
                            text = "Filtrelere uygun ürün bulunamadı.",
                            style = LkTypography.getSectionTitle(),
                            color = LkTextSecondary
                        )
                        Spacer(modifier = Modifier.height(LkSpacing.Space2))
                        Text(
                            text = "Entegrasyonlarınızdan gelen katalog verilerini görüntülemek için filtreleri sıfırlayabilirsiniz.",
                            style = LkTypography.getBodySmall(),
                            color = LkTextMuted
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
                    items(products, key = { it.id }) { product ->
                        MarketplaceProductCard(
                            product = product,
                            onEditSettings = { editingSettingsProduct = product }
                        )
                    }
                }
            }
        }
    }

    if (editingSettingsProduct != null) {
        ProductLocalSettingsDialog(
            product = editingSettingsProduct!!,
            onDismiss = { editingSettingsProduct = null },
            onSave = { note, tags, threshold, fav ->
                viewModel.saveLocalSettings(
                    workspaceId,
                    editingSettingsProduct!!.id,
                    note,
                    tags,
                    threshold,
                    fav
                ) {
                    editingSettingsProduct = null
                }
            }
        )
    }
}

@Composable
private fun StatusFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) LkPrimary.copy(alpha = 0.15f) else LkSurfacePanel,
                shape = LkShapes.SM
            )
            .border(
                width = 1.dp,
                color = if (isSelected) LkPrimary else LkLineSoft,
                shape = LkShapes.SM
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = LkTypography.getMicro(),
            color = if (isSelected) LkPrimary else LkTextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun MarketplaceProductCard(
    product: ProductDto,
    onEditSettings: () -> Unit
) {
    val providerColor = when (product.provider.uppercase()) {
        "TRENDYOL" -> Color(0xFFF27A1A)
        "HEPSIBURADA" -> Color(0xFFFF6000)
        "N11" -> Color(0xFF5E2D91)
        "SHOPIFY" -> Color(0xFF96BF48)
        else -> LkPrimary
    }

    val isOutOfStock = product.stock == 0
    val isLowStock = product.stock in 1..5

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
                    Box(
                        modifier = Modifier
                            .background(providerColor.copy(alpha = 0.15f), LkShapes.SM)
                            .border(1.dp, providerColor.copy(alpha = 0.3f), LkShapes.SM)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = product.provider,
                            style = LkTypography.getMicro(),
                            color = providerColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(LkSpacing.Space2))

                    if (product.isFavorite) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favori",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                color = if (product.onSale) LkSuccess.copy(alpha = 0.15f) else LkTextMuted.copy(alpha = 0.15f),
                                shape = LkShapes.SM
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (product.onSale) "Satışta" else "Satışta Değil",
                            style = LkTypography.getMicro(),
                            color = if (product.onSale) LkSuccess else LkTextMuted,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                IconButton(
                    onClick = onEditSettings,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = "Yerel Ayarlar",
                        tint = LkTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(LkSpacing.Space2))

            Text(
                text = product.title,
                style = LkTypography.getBodyStrong(),
                color = LkTextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "SKU: ${product.sku ?: "—"} ${if (!product.barcode.isNullOrBlank()) "| Barkod: ${product.barcode}" else ""}",
                style = LkTypography.getMicro(),
                color = LkTextMuted
            )

            if (!product.internalNote.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Not: ${product.internalNote}",
                    style = LkTypography.getMicro(),
                    color = LkPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            if (product.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(product.tags) { tag ->
                        Box(
                            modifier = Modifier
                                .background(LkSurfaceCanvas, LkShapes.SM)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "#$tag", style = LkTypography.getMicro(), color = LkTextSecondary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(LkSpacing.Space3))
            Divider(color = LkLineSoft)
            Spacer(modifier = Modifier.height(LkSpacing.Space2))

            // Price & Stock Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${product.salePrice?.toInt() ?: 0} ${product.currency}",
                        style = LkTypography.getBodyStrong(),
                        color = LkTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    if (product.listPrice != null && product.listPrice > (product.salePrice ?: 0.0)) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${product.listPrice.toInt()} ₺",
                            style = LkTypography.getMicro(),
                            color = LkTextMuted,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(
                            color = when {
                                isOutOfStock -> LkDanger.copy(alpha = 0.15f)
                                isLowStock -> LkWarning.copy(alpha = 0.15f)
                                else -> LkSuccess.copy(alpha = 0.15f)
                            },
                            shape = LkShapes.SM
                        )
                        .border(
                            width = 1.dp,
                            color = when {
                                isOutOfStock -> LkDanger.copy(alpha = 0.3f)
                                isLowStock -> LkWarning.copy(alpha = 0.3f)
                                else -> LkSuccess.copy(alpha = 0.3f)
                            },
                            shape = LkShapes.SM
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when {
                            isOutOfStock -> "Tükendi"
                            isLowStock -> "Kritik: ${product.stock} Adet"
                            else -> "Stok: ${product.stock} Adet"
                        },
                        style = LkTypography.getMicro(),
                        color = when {
                            isOutOfStock -> LkDanger
                            isLowStock -> LkWarning
                            else -> LkSuccess
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(LkSpacing.Space2))

            // Performance Stats Grid (Satılan, Sipariş, Ciro, İade)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LkSurfaceCanvas, LkShapes.SM)
                    .padding(horizontal = LkSpacing.Space3, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PerformanceMetricItem(label = "Satılan", value = "${product.unitsSold} ad.")
                PerformanceMetricItem(label = "Sipariş", value = "${product.orderCount}")
                PerformanceMetricItem(label = "Ciro", value = "${product.grossSales?.toInt() ?: 0} ₺")
                PerformanceMetricItem(
                    label = "İade",
                    value = "%${product.returnRate ?: 0.0}",
                    valueColor = if ((product.returnRate ?: 0.0) > 3.0) LkDanger else LkTextSecondary
                )
            }
        }
    }
}

@Composable
private fun PerformanceMetricItem(
    label: String,
    value: String,
    valueColor: Color = LkTextPrimary
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = LkTypography.getMicro(), color = LkTextMuted)
        Text(text = value, style = LkTypography.getMicro(), color = valueColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ProductLocalSettingsDialog(
    product: ProductDto,
    onDismiss: () -> Unit,
    onSave: (internalNote: String?, tags: List<String>?, lowStockThreshold: Int?, isFavorite: Boolean) -> Unit
) {
    var internalNote by remember { mutableStateOf(product.internalNote ?: "") }
    var tagsText by remember { mutableStateOf(product.tags.joinToString(", ")) }
    var threshold by remember { mutableStateOf(product.lowStockThresholdOverride?.toString() ?: "5") }
    var isFavorite by remember { mutableStateOf(product.isFavorite) }

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
                    text = "Yerel Ürün Ayarları",
                    style = LkTypography.getSectionTitle(),
                    color = LkTextPrimary
                )
                Text(
                    text = product.title,
                    style = LkTypography.getBodySmall(),
                    color = LkTextSecondary,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(LkSpacing.Space4))

                LkTextField(
                    value = internalNote,
                    onValueChange = { internalNote = it },
                    label = "İç Not (Sadece siz görürsünüz)",
                    placeholder = "Tedarikçi notu, raf konumu vb."
                )

                Spacer(modifier = Modifier.height(LkSpacing.Space3))

                LkTextField(
                    value = tagsText,
                    onValueChange = { tagsText = it },
                    label = "Etiketler (Virgülle ayırın)",
                    placeholder = "Örn: Aksesuar, Bestseller"
                )

                Spacer(modifier = Modifier.height(LkSpacing.Space3))

                LkNumericField(
                    value = threshold,
                    onValueChange = { threshold = it },
                    label = "Kritik Stok Uyarısı Eşiği (Adet)",
                    placeholder = "5"
                )

                Spacer(modifier = Modifier.height(LkSpacing.Space3))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isFavorite = !isFavorite }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isFavorite,
                        onCheckedChange = { isFavorite = it },
                        colors = CheckboxDefaults.colors(checkedColor = LkPrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Favorilere Ekle",
                        style = LkTypography.getBodySmall(),
                        color = LkTextPrimary
                    )
                }

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
                            val parsedThreshold = threshold.toIntOrNull()
                            val parsedTags = tagsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            onSave(
                                internalNote.ifBlank { null },
                                if (parsedTags.isNotEmpty()) parsedTags else null,
                                parsedThreshold,
                                isFavorite
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
