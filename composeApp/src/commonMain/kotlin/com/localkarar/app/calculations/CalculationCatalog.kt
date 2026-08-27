package com.localkarar.app.calculations

import com.localkarar.app.network.dto.FinancialModelDto
import com.localkarar.app.network.dto.FormulaDto

/**
 * PRESENTATION MAPPING — mirrors Web's calculationCatalog.js.
 *
 * This file maps backend formula IDs and model codes into a unified
 * user-facing catalog. It does NOT contain business formulas or
 * calculation logic — execution remains on the backend via
 * POST /formulas/:id/calculate and POST /workspaces/:id/financial-models/:code/runs.
 */

data class CalculationCategory(
    val key: String,
    val label: String
)

val CALCULATION_CATEGORIES = listOf(
    CalculationCategory("all", "Tümü"),
    CalculationCategory("cash", "Nakit & Likidite"),
    CalculationCategory("profitability", "Kârlılık & Fiyatlama"),
    CalculationCategory("customer", "Satış & Müşteri"),
    CalculationCategory("operations", "Stok & Operasyon"),
    CalculationCategory("growth", "Yatırım & Büyüme"),
    CalculationCategory("valuation", "Değerleme & İleri Analiz"),
)

data class CalculationDefinition(
    val id: String,
    val title: String,
    val category: String,
    val formulaId: String? = null,
    val modelCode: String? = null
) {
    val supportsQuickCalculation: Boolean get() = formulaId != null
    val supportsDetailedAnalysis: Boolean get() = modelCode != null
}

/**
 * All 34 catalog entries, ordered exactly as Web's CALCULATION_DEFINITIONS.
 * Source: calculationCatalog.js lines 12-48.
 */
val CALCULATION_DEFINITIONS = listOf(
    // Both simple + detailed (8 items)
    CalculationDefinition("customer-acquisition-cost", "Müşteri Edinme Maliyeti (CAC)", "customer", "cac", "CAC"),
    CalculationDefinition("customer-lifetime-value", "Müşteri Yaşam Boyu Değeri (LTV)", "customer", "ltv", "LTV"),
    CalculationDefinition("ltv-cac-ratio", "LTV/CAC Oranı", "customer", "ltv_cac", "LTV_CAC"),
    CalculationDefinition("break-even-quantity", "Başa Baş Satış Adedi", "profitability", "basabas_noktasi", "BREAK_EVEN_QUANTITY"),
    CalculationDefinition("cash-runway", "Nakit Dayanma Süresi", "cash", "nakit_dayanim", "RUNWAY"),
    CalculationDefinition("net-working-capital", "Net İşletme Sermayesi", "cash", "isletme_sermayesi", "NET_WORKING_CAPITAL"),
    CalculationDefinition("inventory-turnover-dio", "Stok Devir ve DIO", "operations", "stok_devir", "DIO"),
    CalculationDefinition("order-profitability", "Sipariş Kârlılığı", "profitability", "pazaryeri_siparis_kari", "ORDER_PROFITABILITY"),

    // Simple only (11 items)
    CalculationDefinition("price-architecture", "Fiyat Mimarisi ve Hedef Marj", "profitability", formulaId = "fiyat_mimarisi"),
    CalculationDefinition("profit-margin", "Kâr ve Kâr Marjı", "profitability", formulaId = "kar_hesabi"),
    CalculationDefinition("cash-position", "Nakit Pozisyonu", "cash", formulaId = "nakit_pozisyonu"),
    CalculationDefinition("roi", "Yatırım Getirisi (ROI)", "growth", formulaId = "roi"),
    CalculationDefinition("discount-profit", "İndirim/Kampanya Kârlılığı", "profitability", formulaId = "indirim_kar"),
    CalculationDefinition("loan-cost", "Kredi Taksiti ve Toplam Maliyet", "cash", formulaId = "kredi_maliyeti"),
    CalculationDefinition("export-unit-cost", "İhracat Birim Maliyeti", "operations", formulaId = "ihracat_maliyet"),
    CalculationDefinition("vat-addition", "KDV Ekleme", "profitability", formulaId = "kdv_ekleme"),
    CalculationDefinition("cash-closing", "Günlük Kasa Kapanışı", "cash", formulaId = "kasa_kapanis"),
    CalculationDefinition("term-difference", "Vade Farkı", "cash", formulaId = "vade_farki"),
    CalculationDefinition("unit-cost", "Gerçek Birim Maliyet", "operations", formulaId = "birim_maliyet"),

    // Detailed only (15 items)
    CalculationDefinition("current-ratio", "Cari Oran", "cash", modelCode = "CURRENT_RATIO"),
    CalculationDefinition("quick-ratio", "Asit-Test Oranı", "cash", modelCode = "QUICK_RATIO"),
    CalculationDefinition("dupont", "Üç Aşamalı DuPont", "profitability", modelCode = "DUPONT_3_STEP"),
    CalculationDefinition("profit-to-cash", "Kârdan Nakde Mutabakat", "cash", modelCode = "PROFIT_TO_CASH"),
    CalculationDefinition("cash-conversion-cycle", "Nakit Dönüşüm Döngüsü", "operations", modelCode = "CASH_CONVERSION_CYCLE"),
    CalculationDefinition("dso", "Tahsilat Süresi (DSO)", "operations", modelCode = "DSO"),
    CalculationDefinition("dpo", "Tedarikçi Ödeme Süresi (DPO)", "operations", modelCode = "DPO"),
    CalculationDefinition("contribution-margin", "Katkı Payı", "profitability", modelCode = "CONTRIBUTION_MARGIN"),
    CalculationDefinition("product-profitability", "Ürün Kârlılığı", "profitability", modelCode = "PRODUCT_PROFITABILITY"),
    CalculationDefinition("post-return-margin", "İade Sonrası Gerçek Marj", "profitability", modelCode = "POST_RETURN_MARGIN"),
    CalculationDefinition("cac-payback", "CAC Geri Ödeme Süresi", "customer", modelCode = "CAC_PAYBACK"),
    CalculationDefinition("gross-burn", "Brüt Nakit Tüketimi", "cash", modelCode = "GROSS_BURN"),
    CalculationDefinition("net-burn", "Net Nakit Tüketimi", "cash", modelCode = "NET_BURN"),
    CalculationDefinition("npv", "Net Bugünkü Değer", "growth", modelCode = "NPV"),
    CalculationDefinition("irr", "İç Verim Oranı", "growth", modelCode = "IRR"),
    CalculationDefinition("wacc-fcff-dcf", "Basitleştirilmiş WACC ve FCFF DCF", "valuation", modelCode = "WACC_FCFF_DCF"),
)

/**
 * A unified catalog item combining the presentation definition
 * with the actual backend data (formula/model) if available.
 */
data class CalculationItem(
    val definition: CalculationDefinition,
    val formula: FormulaDto? = null,
    val model: FinancialModelDto? = null,
) {
    val id: String get() = definition.id
    val title: String get() = definition.title
    val category: String get() = definition.category
    val description: String get() = formula?.description ?: model?.purpose ?: model?.description ?: ""
    val inputCount: Int get() = formula?.inputs?.size ?: model?.requirementCount ?: model?.inputs?.size ?: 0
    val supportsQuickCalculation: Boolean get() = definition.supportsQuickCalculation
    val supportsDetailedAnalysis: Boolean get() = definition.supportsDetailedAnalysis

    fun modeLabels(): List<String> {
        if (supportsQuickCalculation && supportsDetailedAnalysis) return listOf("Hızlı hesap", "Detaylı analiz mevcut")
        if (supportsQuickCalculation) return listOf("Hızlı hesap")
        return listOf("İleri analiz")
    }
}

/**
 * Builds the unified catalog by joining the presentation definitions
 * with actual backend formula/model data.
 *
 * Mirrors Web's buildCalculationCatalog() in calculationCatalog.js.
 */
fun buildCalculationCatalog(
    formulas: List<FormulaDto>,
    models: List<FinancialModelDto>
): List<CalculationItem> {
    val formulaMap = formulas.associateBy { it.id }
    val modelMap = models.associateBy { it.code }

    return CALCULATION_DEFINITIONS.map { definition ->
        CalculationItem(
            definition = definition,
            formula = definition.formulaId?.let { formulaMap[it] },
            model = definition.modelCode?.let { modelMap[it] }
        )
    }
}
