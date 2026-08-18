package com.localkarar.app.ui.screens.calculations

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.calculations.CalculationsUiState
import com.localkarar.app.calculations.CalculationsViewModel
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.core.displayValue
import com.localkarar.app.network.dto.FinancialModelDto
import com.localkarar.app.network.dto.FormulaDto
import com.localkarar.app.ui.components.LkChip
import com.localkarar.app.ui.components.LkEmptyState
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.components.LkSectionHeader
import com.localkarar.app.ui.theme.*

fun formulaCategoryLabel(category: String?): String {
    return when (category) {
        "daily" -> "Günlük İşlemler"
        "cash" -> "Nakit Akışı"
        "sales" -> "Satış & Pazarlama"
        "stock" -> "Stok Yönetimi"
        "growth" -> "Büyüme"
        else -> "Hesaplamalar"
    }
}

fun modelCategoryLabel(category: String): String {
    return when (category) {
        "liquidity" -> "Likidite"
        "profitability" -> "Kârlılık"
        "efficiency" -> "Verimlilik"
        "unit_economics" -> "Birim Ekonomisi"
        "cash_resilience" -> "Nakit Dayanıklılığı"
        "investment" -> "Yatırım"
        "valuation" -> "Değerleme"
        else -> category
    }
}

@Composable
fun CalculationsScreen(
    viewModel: CalculationsViewModel,
    onFormulaSelected: (FormulaDto) -> Unit,
    onModelSelected: (String) -> Unit,
    onRunsSelected: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Katalog", "Finansal Görünüm", "Geçmiş")

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
                            0 -> KatalogTab(state.formulas, state.models, onFormulaSelected, onModelSelected)
                            1 -> FinancialModelsTab(state.models, onModelSelected)
                            else -> HistoryTab(state.history)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KatalogTab(
    formulas: List<FormulaDto>,
    models: List<FinancialModelDto>,
    onFormulaSelected: (FormulaDto) -> Unit,
    onModelSelected: (String) -> Unit
) {
    if (formulas.isEmpty() && models.isEmpty()) {
        LkEmptyState(
            title = "Henüz hesaplama yok",
            description = "Hesaplama araçları yüklenemedi. Lütfen tekrar deneyin."
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(LkSpacing.Space4),
        verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
    ) {
        item {
            LkSectionHeader(
                title = "Hızlı Hesaplamalar",
                subtitle = "Günlük işletme hesaplamaları"
            )
        }
        items(formulas) { formula ->
            FormulaCard(formula = formula, onClick = { onFormulaSelected(formula) })
        }
        item {
            Spacer(modifier = Modifier.height(LkSpacing.Space2))
            LkSectionHeader(
                title = "Finansal Görünüm",
                subtitle = "Finansal analiz modelleri"
            )
        }
        items(models) { model ->
            ModelCard(model = model, onClick = { onModelSelected(model.code) })
        }
    }
}

@Composable
private fun FinancialModelsTab(
    models: List<FinancialModelDto>,
    onModelSelected: (String) -> Unit
) {
    if (models.isEmpty()) {
        LkEmptyState(
            title = "Model bulunamadı",
            description = "Finansal modeller yüklenemedi."
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(LkSpacing.Space4),
        verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
    ) {
        items(models) { model ->
            ModelDetailCard(model = model, onClick = { onModelSelected(model.code) })
        }
    }
}

@Composable
private fun HistoryTab(history: List<com.localkarar.app.network.dto.FormulaCalculationDto>) {
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LkSurfacePanel, LkShapes.MD)
                    .border(1.dp, LkLineStrong, LkShapes.MD)
                    .padding(LkSpacing.PadPanel)
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
                item.result.entries.take(3).forEach { (key, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = key.replace('_', ' ').replaceFirstChar { it.uppercase() },
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
    }
}

@Composable
private fun FormulaCard(
    formula: FormulaDto,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
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
                    text = formula.name,
                    style = LkTypography.getBodyStrong(),
                    color = LkTextPrimary
                )
                if (!formula.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(LkSpacing.Space1))
                    Text(
                        text = formula.description,
                        style = LkTypography.getMetadata(),
                        color = LkTextSecondary,
                        maxLines = 2
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.Calculate,
                contentDescription = null,
                tint = LkPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(LkSpacing.Space2))
        LkChip(text = formulaCategoryLabel(formula.category))
    }
}

@Composable
private fun ModelCard(
    model: FinancialModelDto,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LkSurfacePanel, LkShapes.MD)
            .border(1.dp, LkLineStrong, LkShapes.MD)
            .clickable(onClick = onClick)
            .padding(LkSpacing.PadPanel)
    ) {
        Text(
            text = model.name,
            style = LkTypography.getBodyStrong(),
            color = LkTextPrimary
        )
        if (model.purpose.isNotBlank()) {
            Spacer(modifier = Modifier.height(LkSpacing.Space1))
            Text(
                text = model.purpose,
                style = LkTypography.getMetadata(),
                color = LkTextSecondary,
                maxLines = 2
            )
        }
        Spacer(modifier = Modifier.height(LkSpacing.Space2))
        LkChip(text = modelCategoryLabel(model.category))
    }
}

@Composable
private fun ModelDetailCard(
    model: FinancialModelDto,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
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
                    text = model.name,
                    style = LkTypography.getBodyStrong(),
                    color = LkTextPrimary
                )
                Spacer(modifier = Modifier.height(LkSpacing.Space1))
                Text(
                    text = model.code,
                    style = LkTypography.getMicro(),
                    color = LkPrimary
                )
            }
            Icon(
                imageVector = Icons.Default.Insights,
                contentDescription = null,
                tint = LkPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(LkSpacing.Space2))
        Text(
            text = model.description,
            style = LkTypography.getBodySmall(),
            color = LkTextSecondary
        )
        if (!model.formula.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(LkSpacing.Space2))
            Text(
                text = model.formula,
                style = LkTypography.getMetadata(),
                color = LkTextMuted
            )
        }
        Spacer(modifier = Modifier.height(LkSpacing.Space2))
        Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
            LkChip(text = modelCategoryLabel(model.category))
            if (model.requirementCount > 0) {
                LkChip(text = "${model.requirementCount} girdi")
            }
        }
    }
}