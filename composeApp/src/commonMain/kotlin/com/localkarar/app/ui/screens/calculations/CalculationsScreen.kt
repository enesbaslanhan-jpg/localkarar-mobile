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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.AccountBalanceWallet
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
import com.localkarar.app.ui.components.LkSectionHeader
import com.localkarar.app.ui.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

private fun formatTry(amount: Double): String {
    val formatted = kotlin.math.abs(amount).toLong().toString()
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
    val tabs = listOf("Katalog", "Finansal Görünüm", "Geçmiş")

    LaunchedEffect(selectedTab) {
        if (selectedTab == 1 || selectedTab == 2) {
            viewModel.refresh()
        }
    }

    LkPageLayout(title = "Hesaplamalar", onBack = onBack) {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTab,
                backgroundColor = LkSurfaceCanvas,
                contentColor = LkPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = LkPrimary
                    )
                }
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = tab,
                                style = LkTypography.getBodySmall()
                            )
                        }
                    )
                }
            }

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
                                onNavigateToWorkspace = onNavigateToWorkspace
                            )
                            1 -> FinansalGorunumTab(
                                trackerSummary = state.trackerSummary,
                                openRecords = state.openRecords,
                                history = state.history,
                                onNavigateToWorkspace = onNavigateToWorkspace,
                                navController = navController,
                                catalog = state.catalog
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
    onNavigateToWorkspace: () -> Unit
) {
    val visibleItems = remember(catalog, categoryFilter) {
        if (categoryFilter == "all") catalog
        else catalog.filter { it.category == categoryFilter }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = LkSpacing.Space4)
    ) {
        // Quick workspace entry points
        item {
            Column(modifier = Modifier.padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space3)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
                ) {
                    QuickActionCard(
                        icon = Icons.Default.AccountBalanceWallet,
                        title = "Gelir, gider ve tahsilat",
                        subtitle = "Kayıt ekle",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToWorkspace
                    )
                    QuickActionCard(
                        icon = Icons.Default.Description,
                        title = "Fatura ve belgeler",
                        subtitle = "Belge yükle",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToWorkspace
                    )
                }
                Spacer(modifier = Modifier.height(LkSpacing.Space3))
                QuickActionCard(
                    icon = Icons.Default.CalendarToday,
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
                imageVector = Icons.Default.Calculate,
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
private fun FinansalGorunumTab(
    trackerSummary: TrackerSummaryDto?,
    openRecords: List<BusinessRecordDto>,
    history: List<FormulaCalculationDto>,
    onNavigateToWorkspace: () -> Unit,
    navController: NavController,
    catalog: List<CalculationItem>
) {

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(LkSpacing.Space4),
        verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
    ) {
        // Signature / headline panel
        item {
            FinanceSignaturePanel(trackerSummary, openRecords, onNavigateToWorkspace)
        }

        if (trackerSummary != null) {
            // Tahsilat ve ödeme defteri
            item {
                LkSectionHeader(
                    title = "Tahsilat ve ödeme defteri",
                    subtitle = "Yaklaşan açık kayıtlar"
                )
            }

            if (openRecords.isEmpty()) {
                item {
                    Text(
                        text = "Açık finans kaydı bulunmuyor.",
                        style = LkTypography.getBodySmall(),
                        color = LkTextMuted,
                        modifier = Modifier.padding(vertical = LkSpacing.Space2)
                    )
                }
            } else {
                items(openRecords.take(7), key = { it.id }) { record ->
                    RecordRow(record = record, onClick = onNavigateToWorkspace)
                }
            }

            // İstisnalar - overdue records
            val overdueRecords = openRecords.filter { record ->
                record.dueAt != null && record.dueAt < Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
            }
            if (overdueRecords.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(LkSpacing.Space2))
                    LkSectionHeader(
                        title = "İstisnalar",
                        subtitle = "Önce bakılması gerekenler"
                    )
                }
                items(overdueRecords.take(3), key = { "overdue-${it.id}" }) { record ->
                    RecordRow(record = record, onClick = onNavigateToWorkspace, isOverdue = true)
                }
            }
        }

        // Son hesaplamalar
        if (history.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(LkSpacing.Space2))
                LkSectionHeader(title = "Son hesaplamalar")
            }
            items(history.take(4), key = { "hist-${it.id}" }) { calc ->
                val formula = catalog.find { it.definition.formulaId == calc.formulaId }?.formula
                if (formula != null) {
                    FinansalGorunumHistoryRow(
                        item = calc,
                        onClick = { navController.navigateTo(Destination.FormulaDetail(calc.formulaId, calc)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FinansalGorunumHistoryRow(
    item: FormulaCalculationDto,
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

@Composable
private fun FinanceSignaturePanel(
    trackerSummary: TrackerSummaryDto?,
    openRecords: List<BusinessRecordDto>,

    onNavigateToWorkspace: () -> Unit
) {
    val overdueCount = openRecords.count { record ->
        record.dueAt != null && record.dueAt < Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
    }
    val net = trackerSummary?.nextThirtyDays?.net ?: 0.0

    val headline = when {
        trackerSummary == null -> "Finansal görünümünüzü işletme kayıtlarıyla kurun"
        overdueCount > 0 -> "Nakit görünümü kontrollü, $overdueCount kayıt dikkat istiyor"
        net < 0 -> "Önümüzdeki 30 gün için nakit planı gerekiyor"
        else -> "Nakit görünümü kontrollü"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LkSurfacePanel, LkShapes.MD)
            .border(1.dp, LkLineStrong, LkShapes.MD)
            .padding(LkSpacing.PadPanel)
    ) {
        Text(
            text = "Finansal görünüm",
            style = LkTypography.getMicro(),
            color = LkTextSecondary
        )
        Spacer(modifier = Modifier.height(LkSpacing.Space2))
        Text(
            text = headline,
            style = LkTypography.getSectionTitle(),
            color = LkTextPrimary
        )
        Spacer(modifier = Modifier.height(LkSpacing.Space2))

        if (trackerSummary != null) {
            val subtitle = if (overdueCount > 0) {
                "Geciken kayıtları ve yaklaşan vadeleri gözden geçirin."
            } else {
                "Yaklaşan tahsilat ve ödemeler kayıtlarınıza göre dengede."
            }
            Text(
                text = subtitle,
                style = LkTypography.getBodySmall(),
                color = LkTextSecondary
            )
            Spacer(modifier = Modifier.height(LkSpacing.Space4))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FinanceMetric(
                    label = "Alacak",
                    value = formatTry(trackerSummary.nextThirtyDays.receivable),
                    caption = "30 gün"
                )
                FinanceMetric(
                    label = "Borç",
                    value = formatTry(trackerSummary.nextThirtyDays.payable),
                    caption = "30 gün"
                )
                FinanceMetric(
                    label = "Net",
                    value = formatTry(net),
                    caption = if (net < 0) "Plan gerekli" else "Kontrollü",
                    isNegative = net < 0
                )
            }
        } else {
            Text(
                text = "İşletme takibine finansal kayıt eklediğinizde özet burada oluşur.",
                style = LkTypography.getBodySmall(),
                color = LkTextSecondary
            )
            Spacer(modifier = Modifier.height(LkSpacing.Space3))
            Button(
                onClick = onNavigateToWorkspace,
                colors = ButtonDefaults.buttonColors(backgroundColor = LkPrimary)
            ) {
                Text("İşletme kaydı ekle", color = LkSurfaceCanvas)
            }
        }
    }
}

@Composable
private fun FinanceMetric(
    label: String,
    value: String,
    caption: String,
    isNegative: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = LkTypography.getMicro(), color = LkTextSecondary)
        Text(
            text = value,
            style = LkTypography.getBodyStrong(),
            color = if (isNegative) LkDanger else LkTextPrimary
        )
        Text(text = caption, style = LkTypography.getMicro(), color = LkTextMuted)
    }
}

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