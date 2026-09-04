package com.localkarar.app.ui.screens.calculations

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.localkarar.app.calculations.CALCULATION_CATEGORIES
import com.localkarar.app.calculations.CalculationItem
import com.localkarar.app.calculations.CalculationsUiState
import com.localkarar.app.calculations.CalculationsViewModel
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.core.displayValue
import com.localkarar.app.navigation.Destination
import com.localkarar.app.navigation.NavController
import com.localkarar.app.network.dto.BusinessRecordDto
import com.localkarar.app.network.dto.FormulaCalculationDto
import com.localkarar.app.network.dto.FormulaDto
import com.localkarar.app.network.dto.TrackerSummaryDto
import com.localkarar.app.ui.components.LkChip
import com.localkarar.app.ui.components.LkEmptyState
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.components.LkTabs
import com.localkarar.app.ui.components.LkSectionHeader
import com.localkarar.app.ui.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/** Ayni pakette PricingWizard da kullaniyor; private olamaz. */
/**
 * Tam lira olarak bicimlendirir.
 *
 * 🔴 ONCEDEN `toLong()` ILE KIRPIYORDU: 1416,67 ekrana 1.416 olarak
 * dusuyordu. Fiyatlandirma aracinda bu sistematik olarak hedef marjin
 * ALTINDA bir fiyat gosterir -- kullanici o fiyati uygularsa her satista
 * hedefledigi kari tutturamaz. Artik en yakin liraya yuvarlaniyor.
 *
 * ⚠️ Kurus yine gosterilmiyor; sunucu iki basamak dondurse de listelerde
 * tam lira daha okunur. Kurus onemli oldugunda (fatura, mutabakat) ayri
 * bir bicimlendirici gerekir.
 */
internal fun formatTry(amount: Double): String {
    val formatted = kotlin.math.round(kotlin.math.abs(amount)).toLong().toString()
        .reversed().chunked(3).joinToString(".").reversed()
    return if (amount < 0) "-₺$formatted" else "₺$formatted"
}

@Composable
fun CalculationsScreen(
    viewModel: CalculationsViewModel,
    onCalculationSelected: (CalculationItem) -> Unit,
    onDetailedSelected: ((CalculationItem) -> Unit)? = null,
    onNavigateToWorkspace: () -> Unit,
    onBack: () -> Unit,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val categoryFilter by viewModel.categoryFilter.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    /*
     * WEB ILE AYNI IKI SEKME.
     *
     * 🔴 "Finansal Gorunum" KALDIRILDI. Webde de vardi ve BILEREK silindi
     * (`frontend/src/pages/ToolsPage.jsx:219`): dort blogundan ucu Isletme
     * Takibi'ndeki veriyi oldugu gibi tekrarliyordu -- ayni iki uc
     * (`tracker.summary`, `tracker.list`) uc ayri ekranda cagriliyordu --
     * dorduncusu de "Gecmis" sekmesiyle ayniydi.
     *
     * Mobil bu sekmeyi tasimaya devam ediyordu ve ayni gereksiz iki istegi
     * yapiyordu. Hesaplamalar bir HESAP modulu; tahsilat/odeme defteri
     * Isletme Takibi'ne ait. Buraya tekrar eklenmemeli.
     */
    val tabs = listOf("Katalog", "Geçmiş")

    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) viewModel.refresh()
    }

    LkPageLayout(title = "Hesaplamalar", onBack = onBack) {
        Column(modifier = Modifier.fillMaxSize()) {
            // §11: sekmeler ortak bilesenden. Material TabRow kendi olcu ve
            // renk sistemini getiriyordu (§0 ihlali).
            LkTabs(
                tabs = tabs,
                selectedIndex = selectedTab,
                onSelect = { selectedTab = it },
                modifier = Modifier.padding(horizontal = LkSpacing.Space4)
            )
            Spacer(modifier = Modifier.height(LkSpacing.Space2))

            Box(modifier = Modifier.weight(1f)) {
                when (val state = uiState) {
                    is CalculationsUiState.Loading -> LkLoadingState()
                    is CalculationsUiState.Error -> LkErrorState(
                        message = state.message,
                        onRetry = { viewModel.load() }
                    )
                    is CalculationsUiState.Content -> {
                        when (selectedTab) {
                            0 -> KatalogTab(
                                catalog = state.catalog,
                                categoryFilter = categoryFilter,
                                onCategoryChanged = { viewModel.updateCategoryFilter(it) },
                                onCalculationSelected = onCalculationSelected,
                                onDetailedSelected = onDetailedSelected,
                                onNavigateToWorkspace = onNavigateToWorkspace,
                                onOpenPricingTool = {
                                    navController.navigateTo(Destination.FormulaDetail("fiyat_mimarisi"))
                                }
                            )
                            else -> GecmisTab(
                                history = state.history,
                                catalog = state.catalog,
                                onHistorySelected = { item, calculationItem ->
                                    if (calculationItem.formula != null) {
                                        navController.navigateTo(Destination.FormulaDetail(calculationItem.formula!!.id, item))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formulaResultLabel(key: String): String {
    return FORMULA_RESULT_LABELS[key] ?: key.replace('_', ' ').replaceFirstChar { it.uppercaseChar() }
}

// ─── KATALOG TAB ────────────────────────────────────────────

@Composable
private fun KatalogTab(
    catalog: List<CalculationItem>,
    categoryFilter: String,
    onCategoryChanged: (String) -> Unit,
    onCalculationSelected: (CalculationItem) -> Unit,
    onDetailedSelected: ((CalculationItem) -> Unit)? = null,
    onNavigateToWorkspace: () -> Unit,
    onOpenPricingTool: () -> Unit
) {
    val visibleItems = remember(catalog, categoryFilter) {
        if (categoryFilter == "all") catalog
        else catalog.filter { it.category == categoryFilter }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = LkSpacing.Space4)
    ) {
        // Prototipteki sira: filtre haplari → Fiyatlandirma Sihirbazi →
        // arac listesi. Sihirbaz ekranin one cikan blogu, en ustte.
        item {
            PricingWizardSection(
                onOpenFullTool = onOpenPricingTool,
                modifier = Modifier.padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space3)
            )
        }

        // Quick workspace entry points
        item {
            Column(modifier = Modifier.padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space3)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
                ) {
                    QuickActionCard(
                        icon = Icons.Outlined.AccountBalanceWallet,
                        title = "Gelir, gider ve tahsilat",
                        subtitle = "Kayıt ekle",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToWorkspace
                    )
                    QuickActionCard(
                        icon = Icons.Outlined.Description,
                        title = "Fatura ve belgeler",
                        subtitle = "Belge yükle",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToWorkspace
                    )
                }
                Spacer(modifier = Modifier.height(LkSpacing.Space3))
                QuickActionCard(
                    icon = Icons.Outlined.CalendarToday,
                    title = "Ödeme takvimi",
                    subtitle = "Vadeleri ve yaklaşan işlemleri gör",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToWorkspace
                )
            }
        }

        // Category filter chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space2),
                horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)
            ) {
                CALCULATION_CATEGORIES.forEach { category ->
                    LkChip(
                        text = category.label,
                        selected = categoryFilter == category.key,
                        onClick = { onCategoryChanged(category.key) }
                    )
                }
            }
        }

        // Section title
        item {
            Spacer(modifier = Modifier.height(LkSpacing.Space2))
            LkSectionHeader(
                title = "Bugün ne hesaplamak istiyorsunuz?",
                modifier = Modifier.padding(horizontal = LkSpacing.Space4)
            )
        }

        if (visibleItems.isEmpty()) {
            item {
                LkEmptyState(
                    title = "Sonuç bulunamadı",
                    description = "Bu kategoride hesaplama aracı yok."
                )
            }
        } else {
            items(visibleItems, key = { it.id }) { item ->
                CalculationCard(
                    item = item,
                    onClick = { onCalculationSelected(item) },
                    onOpenDetailed = { onDetailedSelected?.invoke(item) ?: onCalculationSelected(item) },
                    onOpenQuick = { onCalculationSelected(item) }
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .background(LkSurfacePanel, LkShapes.MD)
            .border(1.dp, LkLineStrong, LkShapes.MD)
            .clickable(onClick = onClick)
            .padding(LkSpacing.Space3)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LkPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(LkSpacing.Space2))
        Text(
            text = title,
            style = LkTypography.getBodySmall(),
            color = LkTextPrimary,
            maxLines = 2
        )
        Text(
            text = subtitle,
            style = LkTypography.getMicro(),
            color = LkTextSecondary,
            maxLines = 1
        )
    }
}

@Composable
private fun CalculationCard(
    item: CalculationItem,
    onClick: () -> Unit,
    onOpenDetailed: (() -> Unit)? = null,
    onOpenQuick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space1)
            .background(LkSurfacePanel, LkShapes.MD)
            .border(1.dp, LkLineStrong, LkShapes.MD)
            .clickable(onClick = onClick)
            .padding(LkSpacing.PadPanel)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = LkTypography.getBodyStrong(),
                    color = LkTextPrimary
                )
                Spacer(modifier = Modifier.height(LkSpacing.Space1))
                Text(
                    text = item.description.ifBlank { "${item.inputCount} bilgiyle hesaplanır" },
                    style = LkTypography.getMetadata(),
                    color = LkTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Outlined.Calculate,
                contentDescription = null,
                tint = LkPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(LkSpacing.Space2))
        Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
            val categoryLabel = CALCULATION_CATEGORIES.firstOrNull { it.key == item.category }?.label ?: item.category
            LkChip(text = categoryLabel)
            if (item.supportsQuickCalculation) {
                LkChip(text = "Hızlı hesap", onClick = onOpenQuick)
            }
            if (item.supportsDetailedAnalysis) {
                LkChip(text = "Detaylı analiz mevcut", onClick = onOpenDetailed)
            }
        }
    }
}

// ─── FİNANSAL GÖRÜNÜM TAB ──────────────────────────────────

@Composable
private fun RecordRow(
    record: BusinessRecordDto,
    onClick: () -> Unit,
    isOverdue: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LkSurfacePanel, LkShapes.MD)
            .border(1.dp, if (isOverdue) LkDanger.copy(alpha = 0.3f) else LkLineStrong, LkShapes.MD)
            .clickable(onClick = onClick)
            .padding(LkSpacing.Space3),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.title,
                style = LkTypography.getBodySmall(),
                color = LkTextPrimary
            )
            val directionLabel = if (record.type == "receivable" || record.direction == "receivable") "Tahsilat" else "Ödeme"
            val dateLabel = record.dueAt?.let { LkDateUtils.formatDate(it) }?.ifBlank { "Tarih yok" } ?: "Tarih yok"
            Text(
                text = "$dateLabel · $directionLabel",
                style = LkTypography.getMicro(),
                color = LkTextSecondary
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (record.amount != null) formatTry(record.amount) else "—",
                style = LkTypography.getBodyStrong(),
                color = LkTextPrimary
            )
            val statusText = if (isOverdue || (record.dueAt != null && record.dueAt < Clock.System.todayIn(TimeZone.currentSystemDefault()).toString())) "Gecikti" else "Planlı"
            Text(
                text = statusText,
                style = LkTypography.getMicro(),
                color = if (statusText == "Gecikti") LkDanger else LkTextMuted
            )
        }
    }
}

// ─── GEÇMİŞ TAB ────────────────────────────────────────────

@Composable
private fun GecmisTab(
    history: List<FormulaCalculationDto>,
    catalog: List<CalculationItem>,
    onHistorySelected: (FormulaCalculationDto, CalculationItem) -> Unit
) {
    if (history.isEmpty()) {
        LkEmptyState(
            title = "Henüz hesaplama geçmişi yok",
            description = "Bir hesaplama yaptığınızda geçmişiniz burada görünür."
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(LkSpacing.Space4),
        verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
    ) {
        items(history) { item ->
            val calculationItem = catalog.find { it.definition.formulaId == item.formulaId }
            if (calculationItem != null) {
                HistoryRow(
                    item = item,
                    calculationItem = calculationItem,
                    onClick = { onHistorySelected(item, calculationItem) }
                )
            }
        }
    }
}

@Composable
private fun HistoryRow(
    item: FormulaCalculationDto,
    calculationItem: CalculationItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LkSurfacePanel, LkShapes.MD)
            .border(1.dp, LkLineStrong, LkShapes.MD)
            .padding(LkSpacing.PadPanel)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.formulaName,
                style = LkTypography.getBodyStrong(),
                color = LkTextPrimary
            )
            Text(
                text = LkDateUtils.formatDateTime(item.createdAt),
                style = LkTypography.getMicro(),
                color = LkTextSecondary
            )
        }
        Spacer(modifier = Modifier.height(LkSpacing.Space3))
        item.result.entries
            .filter { it.key != "durum" }
            .take(4)
            .forEach { (key, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formulaResultLabel(key),
                        style = LkTypography.getBodySmall(),
                        color = LkTextSecondary
                    )
                    Text(
                        text = value.displayValue(),
                        style = LkTypography.getBodyStrong(),
                        color = LkTextPrimary
                    )
                }
            }
    }
}

fun formulaCategoryLabel(category: String?): String {
    val cat = category?.lowercase()
    return when (cat) {
        "cash", "nakit", "likidite" -> "Nakit & Likidite"
        "profitability", "karlilik", "fiyatlama" -> "Kârlılık & Fiyatlama"
        "customer", "musteri", "satis" -> "Satış & Müşteri"
        "operations", "stok", "operasyon" -> "Stok & Operasyon"
        "growth", "yatirim", "buyume" -> "Yatırım & Büyüme"
        "valuation", "degerleme", "ileri_analiz" -> "Değerleme & İleri Analiz"
        else -> category?.replace('_', ' ')?.let { s -> s.replaceFirstChar { c -> c.uppercaseChar() } } ?: "Genel"
    }
}

fun modelCategoryLabel(category: String): String {
    val cat = category.lowercase()
    return when (cat) {
        "cash", "nakit", "likidite" -> "Nakit & Likidite"
        "profitability", "karlilik", "fiyatlama" -> "Kârlılık & Fiyatlama"
        "customer", "musteri", "satis" -> "Satış & Müşteri"
        "operations", "stok", "operasyon" -> "Stok & Operasyon"
        "growth", "yatirim", "buyume" -> "Yatırım & Büyüme"
        "valuation", "degerleme", "ileri_analiz" -> "Değerleme & İleri Analiz"
        else -> category.replace('_', ' ').replaceFirstChar { c -> c.uppercaseChar() }
    }
}