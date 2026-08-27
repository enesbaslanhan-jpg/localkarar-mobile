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
import com.localkarar.app.network.dto.CreateProductRequestDto
import com.localkarar.app.network.dto.ProductDto
import com.localkarar.app.network.dto.UpdateProductRequestDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkNumericField
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.ProductsViewModel

private val PRODUCT_CATEGORIES = listOf("Tümü", "Hizmet", "Yazılım", "Donanım", "Genel")

@Composable
fun ProductsScreen(
    workspaceId: String,
    viewModel: ProductsViewModel,
    onNavigateBack: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductDto?>(null) }

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
                    Text(
                        text = "Ürünler ve Hizmetler",
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
                        Icon(Icons.Default.Add, contentDescription = "Yeni Ürün", tint = LkPrimary)
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
            // Metrics Row
            val activeCount = products.count { it.status == "active" }
            val lowStockCount = products.count { it.stockQuantity <= it.minStockLevel }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space3),
                horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
            ) {
                MetricCard(
                    title = "Toplam Ürün",
                    value = "${products.size}",
                    color = LkPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Aktif Katalog",
                    value = "$activeCount",
                    color = LkSuccess,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Kritik Stok",
                    value = "$lowStockCount",
                    color = if (lowStockCount > 0) LkDanger else LkTextSecondary,
                    modifier = Modifier.weight(1f)
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
                    placeholder = "Ürün Adı veya SKU Kod ile Ara...",
                    trailingContent = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = LkTextMuted)
                    }
                )
            }

            // Category Chips Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space2),
                horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)
            ) {
                items(PRODUCT_CATEGORIES) { category ->
                    val isSelected = selectedCategory == category
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
                            .clickable { viewModel.setCategoryFilter(workspaceId, category) }
                            .padding(horizontal = LkSpacing.Space3, vertical = 6.dp)
                    ) {
                        Text(
                            text = category,
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
                            text = "Henüz ürün veya hizmet eklenmedi.",
                            style = LkTypography.getSectionTitle(),
                            color = LkTextSecondary
                        )
                        Spacer(modifier = Modifier.height(LkSpacing.Space2))
                        Text(
                            text = "İşletmenizin satışını yaptığı ürün veya hizmet kalemlerini buradan tanımlayın.",
                            style = LkTypography.getBodySmall(),
                            color = LkTextMuted
                        )
                        Spacer(modifier = Modifier.height(LkSpacing.Space6))
                        LkButton(
                            text = "+ Yeni Ürün / Hizmet Ekle",
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
                    items(products, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onEdit = { editingProduct = product },
                            onDelete = { viewModel.deleteProduct(workspaceId, product.id) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        ProductFormDialog(
            product = null,
            onDismiss = { showCreateDialog = false },
            onConfirm = { req ->
                viewModel.createProduct(
                    workspaceId,
                    CreateProductRequestDto(
                        code = req.code,
                        name = req.name,
                        category = req.category,
                        price = req.price,
                        costPrice = req.costPrice,
                        stockQuantity = req.stockQuantity,
                        minStockLevel = req.minStockLevel,
                        unit = req.unit,
                        description = req.description
                    )
                ) {
                    showCreateDialog = false
                }
            }
        )
    }

    if (editingProduct != null) {
        ProductFormDialog(
            product = editingProduct,
            onDismiss = { editingProduct = null },
            onConfirm = { req ->
                viewModel.updateProduct(
                    workspaceId,
                    editingProduct!!.id,
                    UpdateProductRequestDto(
                        code = req.code,
                        name = req.name,
                        category = req.category,
                        price = req.price,
                        costPrice = req.costPrice,
                        stockQuantity = req.stockQuantity,
                        minStockLevel = req.minStockLevel,
                        unit = req.unit,
                        description = req.description
                    )
                ) {
                    editingProduct = null
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
private fun ProductCard(
    product: ProductDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    val isLowStock = product.stockQuantity <= product.minStockLevel
    val margin = if (product.price > 0 && product.costPrice > 0) {
        ((product.price - product.costPrice) / product.price * 100).toInt()
    } else null

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
                        text = product.name,
                        style = LkTypography.getBodyStrong(),
                        color = LkTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(LkSpacing.Space2))
                    Box(
                        modifier = Modifier
                            .background(LkPrimary.copy(alpha = 0.12f), LkShapes.SM)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = product.category,
                            style = LkTypography.getMicro(),
                            color = LkPrimary,
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
                        DropdownMenuItem(onClick = { expandedMenu = false; onEdit() }) {
                            Text("Düzenle", style = LkTypography.getBodySmall(), color = LkTextPrimary)
                        }
                        Divider(color = LkLineSoft)
                        DropdownMenuItem(onClick = { expandedMenu = false; onDelete() }) {
                            Text("Ürünü Sil", style = LkTypography.getBodySmall(), color = LkDanger)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "SKU: ${product.code}",
                style = LkTypography.getMicro(),
                color = LkTextMuted
            )

            if (!product.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.description,
                    style = LkTypography.getBodySmall(),
                    color = LkTextSecondary,
                    maxLines = 2
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
                Column {
                    Text(
                        text = "Satış Fiyatı: ${product.price.toInt()} ${product.currency}",
                        style = LkTypography.getBodyStrong(),
                        color = LkTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    if (product.costPrice > 0) {
                        Text(
                            text = "Maliyet: ${product.costPrice.toInt()} ₺ ${if (margin != null) "(%$margin Marj)" else ""}",
                            style = LkTypography.getMicro(),
                            color = LkTextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(
                            color = if (isLowStock) LkDanger.copy(alpha = 0.15f) else LkSuccess.copy(alpha = 0.15f),
                            shape = LkShapes.SM
                        )
                        .border(
                            width = 1.dp,
                            color = if (isLowStock) LkDanger.copy(alpha = 0.3f) else LkSuccess.copy(alpha = 0.3f),
                            shape = LkShapes.SM
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isLowStock) "Kritik: ${product.stockQuantity} ${product.unit}" else "Stok: ${product.stockQuantity} ${product.unit}",
                        style = LkTypography.getMicro(),
                        color = if (isLowStock) LkDanger else LkSuccess,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private data class ProductFormData(
    val code: String,
    val name: String,
    val category: String,
    val price: Double,
    val costPrice: Double,
    val stockQuantity: Int,
    val minStockLevel: Int,
    val unit: String,
    val description: String?
)

@Composable
private fun ProductFormDialog(
    product: ProductDto?,
    onDismiss: () -> Unit,
    onConfirm: (ProductFormData) -> Unit
) {
    var code by remember { mutableStateOf(product?.code ?: "PRD-${kotlin.random.Random.nextInt(100, 999)}") }
    var name by remember { mutableStateOf(product?.name ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "Genel") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var costPrice by remember { mutableStateOf(product?.costPrice?.toString() ?: "") }
    var stockQuantity by remember { mutableStateOf(product?.stockQuantity?.toString() ?: "0") }
    var unit by remember { mutableStateOf(product?.unit ?: "Adet") }
    var description by remember { mutableStateOf(product?.description ?: "") }

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
                    text = if (product == null) "Yeni Ürün / Hizmet Ekle" else "Ürünü Düzenle",
                    style = LkTypography.getSectionTitle(),
                    color = LkTextPrimary
                )
                Spacer(modifier = Modifier.height(LkSpacing.Space4))

                LkTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Ürün / Hizmet Adı",
                    placeholder = "Örn: Danışmanlık Paketi veya Ürün A"
                )
                Spacer(modifier = Modifier.height(LkSpacing.Space3))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)) {
                    LkTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = "SKU Kodu",
                        modifier = Modifier.weight(1f)
                    )
                    LkTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = "Kategori",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(LkSpacing.Space3))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)) {
                    LkNumericField(
                        value = price,
                        onValueChange = { price = it },
                        label = "Satış Fiyatı (TRY)",
                        modifier = Modifier.weight(1f)
                    )
                    LkNumericField(
                        value = costPrice,
                        onValueChange = { costPrice = it },
                        label = "Maliyet (TRY)",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(LkSpacing.Space3))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)) {
                    LkNumericField(
                        value = stockQuantity,
                        onValueChange = { stockQuantity = it },
                        label = "Mevcut Stok",
                        modifier = Modifier.weight(1f)
                    )
                    LkTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = "Birim",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(LkSpacing.Space3))

                LkTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Açıklama (Opsiyonel)",
                    placeholder = "Ürün özellikleri, detaylar..."
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
                            val parsedPrice = price.toDoubleOrNull() ?: 0.0
                            val parsedCost = costPrice.toDoubleOrNull() ?: 0.0
                            val parsedStock = stockQuantity.toIntOrNull() ?: 0
                            onConfirm(
                                ProductFormData(
                                    code = code,
                                    name = name,
                                    category = category,
                                    price = parsedPrice,
                                    costPrice = parsedCost,
                                    stockQuantity = parsedStock,
                                    minStockLevel = 5,
                                    unit = unit,
                                    description = description.ifBlank { null }
                                )
                            )
                        },
                        enabled = name.isNotBlank() && price.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
