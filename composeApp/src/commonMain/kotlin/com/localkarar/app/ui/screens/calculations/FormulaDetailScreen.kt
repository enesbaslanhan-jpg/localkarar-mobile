package com.localkarar.app.ui.screens.calculations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.calculations.FormulaCalculatorUiState
import com.localkarar.app.calculations.FormulaCalculatorViewModel
import com.localkarar.app.core.LkFormatting
import com.localkarar.app.core.displayValue
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkChip
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkInfoPanel
import com.localkarar.app.ui.components.LkNumericField
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.components.LkResultRow
import com.localkarar.app.ui.components.LkSectionHeader
import com.localkarar.app.ui.theme.*

internal val FORMULA_RESULT_LABELS = mapOf(
    "kar" to "Kâr",
    "kar_marji" to "Kâr marjı",
    "durum" to "Durum",
    "katki_payi" to "Katkı payı",
    "basabas_adet" to "Başabaş adedi",
    "basabas_gelir" to "Başabaş geliri",
    "net_pozisyon" to "Net nakit pozisyonu",
    "nakit_oran" to "Nakit oranı",
    "isletme_sermayesi" to "İşletme sermayesi",
    "net_kar" to "Net kâr",
    "roi_yuzde" to "ROI",
    "devir_hizi" to "Stok devir hızı",
    "stokta_kalma_gunu" to "Stokta kalma süresi",
    "cac" to "Müşteri edinme maliyeti",
    "ltv" to "Müşteri yaşam boyu değeri",
    "ltv_cac_orani" to "LTV/CAC oranı",
    "degerlendirme" to "Değerlendirme",
    "indirimli_fiyat" to "İndirimli fiyat",
    "normal_kar" to "Normal kâr",
    "kampanya_kar" to "Kampanya kârı",
    "kar_farki" to "Kâr farkı",
    "aylik_taksit" to "Aylık taksit",
    "toplam_odeme" to "Toplam ödeme",
    "toplam_faiz" to "Toplam faiz",
    "birim_maliyet_try" to "Birim maliyet (TRY)",
    "birim_maliyet_usd" to "Birim maliyet (USD)",
    "toplam_maliyet" to "Toplam maliyet",
    "gercek_birim_maliyet" to "Gerçek birim maliyet",
    "onerilen_kdv_haric_fiyat" to "Önerilen KDV hariç fiyat",
    "komisyon_tutari" to "Komisyon tutarı",
    "odeme_kesintisi" to "Ödeme kesintisi",
    "birim_katki" to "Birim katkı",
    "gerceklesen_marj" to "Gerçekleşen marj",
    "kdv_haric_tutar" to "KDV hariç tutar",
    "kdv_tutari" to "KDV tutarı",
    "kdv_dahil_tutar" to "KDV dahil tutar",
    "toplam_giris" to "Toplam kasa girişi",
    "toplam_cikis" to "Toplam kasa çıkışı",
    "beklenen_kasa" to "Beklenen kasa",
    "aylik_nakit_acigi" to "Aylık nakit açığı",
    "dayanma_suresi_ay" to "Nakit dayanma süresi (ay)",
    "toplam_uretim_maliyeti" to "Toplam üretim maliyeti",
    "birim_maliyet" to "Birim maliyet",
    "vadeli_toplam" to "Vadeli toplam",
    "vade_farki" to "Vade farkı",
    "aylik_esit_odeme" to "Aylık eşit ödeme",
    "siparis_toplam_maliyeti" to "Sipariş toplam maliyeti",
    "siparis_katkisi" to "Sipariş katkısı",
    "siparis_marji" to "Sipariş marjı",
)

private fun resultTone(status: String): ResultTone {
    val lower = status.lowercase()
    return when {
        lower.contains("kârlı") || lower.contains("pozitif") || lower.contains("yeterli") || lower.contains("sağlıklı") || lower.contains("6 ay") || lower.contains("tüketimi yok") -> ResultTone.Success
        lower.contains("kritik") || lower.contains("zarar") || lower.contains("negatif") || lower.contains("yetersiz") || lower.contains("açığı") -> ResultTone.Danger
        else -> ResultTone.Neutral
    }
}

private enum class ResultTone { Success, Danger, Neutral }

private fun ResultTone.badgeColor(): androidx.compose.ui.graphics.Color {
    return when (this) {
        ResultTone.Success -> LkSuccess
        ResultTone.Danger -> LkDanger
        ResultTone.Neutral -> LkWarning
    }
}

@Composable
fun FormulaDetailScreen(
    viewModel: FormulaCalculatorViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var actionError by remember { mutableStateOf<String?>(null) }

    LkPageLayout(title = "Hızlı Hesaplama", onBack = onBack) {
        when (val state = uiState) {
            is FormulaCalculatorUiState.Error -> LkErrorState(
                message = state.message,
                onRetry = null
            )
            is FormulaCalculatorUiState.Content -> {
                val formula = state.formula
                val inputValues = remember(formula.id) {
                    mutableStateMapOf<String, String>().apply {
                        putAll(state.initialInputs)
                    }
                }
                val inputErrors = remember(formula.id) {
                    mutableStateMapOf<String, String>()
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(LkSpacing.Space4),
                    verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
                ) {
                    item {
                        LkSectionHeader(
                            title = formula.name,
                            subtitle = formula.description
                        )
                        Spacer(modifier = Modifier.height(LkSpacing.Space2))
                        LkChip(text = formulaCategoryLabel(formula.category))
                    }

                    if (!formula.warning.isNullOrBlank()) {
                        item {
                            LkInfoPanel(title = "Uyarı", icon = Icons.Default.Warning) {
                                Text(
                                    text = formula.warning,
                                    style = LkTypography.getBodySmall(),
                                    color = LkWarning
                                )
                            }
                        }
                    }

                    item {
                        LkSectionHeader(title = "Girdiler")
                    }

                    formula.inputs.forEach { input ->
                        item {
                            LkNumericField(
                                value = inputValues[input.name] ?: "",
                                onValueChange = { newValue ->
                                    inputValues[input.name] = newValue
                                    inputErrors.remove(input.name)
                                },
                                label = input.label,
                                placeholder = "Değer girin",
                                error = inputErrors[input.name],
                                suffix = input.unit
                            )
                        }
                    }

                    item {
                        if (actionError != null) {
                            Text(
                                text = actionError!!,
                                style = LkTypography.getBodySmall(),
                                color = LkDanger
                            )
                            Spacer(modifier = Modifier.height(LkSpacing.Space2))
                        }
                        LkButton(
                            text = if (state.result == null) "Hesapla" else "Yeniden Hesapla",
                            onClick = {
                                actionError = null
                                val inputs = mutableMapOf<String, Double>()
                                var valid = true
                                formula.inputs.forEach { input ->
                                    val raw = inputValues[input.name]?.trim().orEmpty()
                                    if (raw.isEmpty()) {
                                        inputErrors[input.name] = "Değer girin"
                                        valid = false
                                    } else {
                                        val parsed = LkFormatting.parseDecimal(raw)
                                        if (parsed == null) {
                                            inputErrors[input.name] = "Geçersiz sayı"
                                            valid = false
                                        } else {
                                            if (input.min != null && parsed < input.min) {
                                                inputErrors[input.name] = "En az ${input.min} olmalı"
                                                valid = false
                                            } else if (input.max != null && parsed > input.max) {
                                                inputErrors[input.name] = "En fazla ${input.max} olmalı"
                                                valid = false
                                            } else {
                                                inputs[input.name] = parsed
                                            }
                                        }
                                    }
                                }
                                if (valid) {
                                    viewModel.calculate(inputs) { message ->
                                        actionError = message
                                    }
                                }
                            },
                            enabled = !state.isCalculating,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (state.result != null) {
                        item {
                            LkInfoPanel(title = "Sonuç") {
                                val result = state.result!!
                                val resultEntries = result.result.filterKeys { it != "warnings" && it != "assumptions" }
                                
                                // Show durum/status badge first if present
                                result.result["durum"]?.let { durumValue ->
                                    val durumText = durumValue.displayValue()
                                    val tone = resultTone(durumText)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        LkChip(
                                            text = durumText,
                                            background = tone.badgeColor().copy(alpha = 0.15f),
                                            contentColor = tone.badgeColor()
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(LkSpacing.Space3))
                                }

                                resultEntries.forEach { (key, value) ->
                                    LkResultRow(
                                        label = formulaResultLabel(key),
                                        value = value.displayValue()
                                    )
                                    Spacer(modifier = Modifier.height(LkSpacing.Space2))
                                }
                                result.warnings.forEach { warning ->
                                    Spacer(modifier = Modifier.height(LkSpacing.Space2))
                                    Text(
                                        text = warning,
                                        style = LkTypography.getMetadata(),
                                        color = LkWarning
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}