package com.localkarar.app.ui.components.decision

import androidx.compose.foundation.background
import kotlinx.serialization.json.jsonPrimitive
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ArrowBack
import kotlinx.serialization.json.*

@Composable
fun LkDecisionResultPanel(
    snapshot: JsonElement?,
    toolCode: String,
    onRestart: () -> Unit,
    onListClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (snapshot == null || snapshot !is JsonObject) return
    val calculationOutput = snapshot["calculationOutput"]?.jsonObject ?: return
    val decisionLabel = calculationOutput["decisionLabel"]?.jsonPrimitive?.content 
        ?: if (toolCode == "DC-PROFIT-001") {
            val contribution = calculationOutput["contribution"]?.jsonPrimitive?.doubleOrNull ?: 0.0
            if (contribution > 0) "Ürün katkı üretiyor" else "Bu fiyatla ürün zarar ediyor"
        } else "BİLİNMİYOR"
        
    val decisionTone = calculationOutput["decisionTone"]?.jsonPrimitive?.content 
        ?: if (toolCode == "DC-PROFIT-001") {
            val contribution = calculationOutput["contribution"]?.jsonPrimitive?.doubleOrNull ?: 0.0
            if (contribution > 0) "good" else "bad"
        } else "neutral"
        
    val summary = calculationOutput["summary"]?.jsonPrimitive?.content 
        ?: if (toolCode == "DC-PROFIT-001") "Sonuç, girdiğiniz ürün başı maliyetler ve satış koşullarıyla hesaplandı." else ""

    val (bgColor, contentColor, icon) = when (decisionTone) {
        "good" -> Triple(LkSuccess.copy(alpha = 0.15f), LkSuccess, Icons.Default.CheckCircle)
        "warning" -> Triple(LkWarning.copy(alpha = 0.15f), LkWarning, Icons.Default.Warning)
        "bad" -> Triple(LkDanger.copy(alpha = 0.1f), LkDanger, Icons.Default.Cancel)
        else -> Triple(LkPrimary.copy(alpha = 0.1f), LkPrimary, Icons.Default.Info)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Hero result block
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = decisionTone,
                tint = contentColor,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = decisionLabel,
                style = LkTypography.getDisplay(),
                color = contentColor
            )
            if (summary.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = summary,
                    style = LkTypography.getBody(),
                    color = LkTextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            if (toolCode == "DC-PROFIT-001") {
                val contribution = calculationOutput["contribution"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                Spacer(modifier = Modifier.height(16.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Ürün başına katkı",
                        style = LkTypography.getBodySmall(),
                        color = contentColor.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${contribution.toInt()} ₺",
                        style = LkTypography.getDisplay(),
                        color = contentColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Metrics
        val metricsArray = calculationOutput["metrics"]?.jsonArray
        val hasMetrics = metricsArray != null && metricsArray.isNotEmpty()
        
        if (hasMetrics || toolCode == "DC-PROFIT-001") {
            Text(
                text = "Ana Göstergeler",
                style = LkTypography.getSectionTitle(),
                color = LkTextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LkSurfacePanel)
            ) {
                if (hasMetrics) {
                    metricsArray!!.forEachIndexed { index, el ->
                        val metric = el.jsonObject
                        val label = metric["label"]?.jsonPrimitive?.content ?: ""
                        val value = metric["value"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                        val format = metric["format"]?.jsonPrimitive?.content ?: "number"

                        val formattedValue = formatMetricValue(value, format)
                        MetricRow(label, formattedValue, index < metricsArray.size - 1)
                    }
                } else if (toolCode == "DC-PROFIT-001") {
                    val revenue = calculationOutput["revenue"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                    val totalCost = calculationOutput["totalKnownCost"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                    val margin = calculationOutput["contributionMarginPercent"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                    val breakEven = calculationOutput["breakEvenPrice"]?.jsonPrimitive?.doubleOrNull
                    
                    MetricRow("Toplam maliyet", formatMetricValue(totalCost, "money"), true)
                    MetricRow("Katkı marjı", formatMetricValue(margin, "percent"), true)
                    MetricRow("Başabaş fiyatı", if (breakEven != null) formatMetricValue(breakEven, "money") else "Hesaplanamadı", true)
                    MetricRow("Mevcut satış fiyatı", formatMetricValue(revenue, "money"), false)
                }
            }
        }

        // DC-PROFIT-001 Discount Scenario
        if (toolCode == "DC-PROFIT-001") {
            val discounted = calculationOutput["discountedScenario"]?.jsonObject
            if (discounted != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "İndirim sonrası senaryo",
                    style = LkTypography.getSectionTitle(),
                    color = LkTextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LkSurfacePanel)
                ) {
                    val sPrice = discounted["salePrice"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                    val sCost = discounted["totalCost"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                    val sContribution = discounted["contribution"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                    val sMargin = discounted["marginPercent"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                    
                    MetricRow("İndirimli fiyat", formatMetricValue(sPrice, "money"), true)
                    MetricRow("Yeni toplam maliyet", formatMetricValue(sCost, "money"), true)
                    MetricRow("Yeni katkı", formatMetricValue(sContribution, "money"), true)
                    MetricRow("Yeni marj", formatMetricValue(sMargin, "percent"), false)
                }
            }
        }

        // Scenarios
        val scenarios = calculationOutput["scenarios"]?.jsonArray
        if (scenarios != null && scenarios.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Senaryolar",
                style = LkTypography.getSectionTitle(),
                color = LkTextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            scenarios.forEach { el ->
                val scenario = el.jsonObject
                val label = scenario["label"]?.jsonPrimitive?.content ?: ""
                val value = scenario["value"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                val format = scenario["format"]?.jsonPrimitive?.content ?: "number"
                val detail = scenario["detail"]?.jsonPrimitive?.content ?: ""
                val tone = scenario["tone"]?.jsonPrimitive?.content ?: "neutral"
                
                val formattedValue = formatMetricValue(value, format)
                
                val (sBg, sColor) = when(tone) {
                    "good" -> Pair(LkSuccess.copy(alpha=0.1f), LkSuccess)
                    "bad" -> Pair(LkDanger.copy(alpha=0.1f), LkDanger)
                    "warning" -> Pair(LkWarning.copy(alpha=0.1f), LkWarning)
                    else -> Pair(LkSurfacePanel, LkTextPrimary)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(sBg)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = label,
                            style = LkTypography.getCardTitle(),
                            color = LkTextPrimary
                        )
                        if (detail.isNotBlank()) {
                            Text(
                                text = detail,
                                style = LkTypography.getBodySmall(),
                                color = LkTextSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = formattedValue,
                        style = LkTypography.getMetric(),
                        color = sColor
                    )
                }
            }
        }

        // Risk Warnings
        val riskWarnings = calculationOutput["riskWarnings"]?.jsonArray
        if (riskWarnings != null && riskWarnings.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = LkWarning,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Risk uyarıları",
                    style = LkTypography.getSectionTitle(),
                    color = LkWarning
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LkWarning.copy(alpha = 0.05f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                riskWarnings.forEach { warning ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = LkTypography.getBody(),
                            color = LkWarning,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = warning.jsonPrimitive.content,
                            style = LkTypography.getBody(),
                            color = LkTextPrimary
                        )
                    }
                }
            }
        }

        // Safe Next Steps
        val nextSteps = calculationOutput["safeNextSteps"]?.jsonArray
        if (nextSteps != null && nextSteps.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = LkSuccess,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Güvenli sonraki adımlar",
                    style = LkTypography.getSectionTitle(),
                    color = LkSuccess
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LkSuccess.copy(alpha = 0.05f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                nextSteps.forEach { step ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "✓",
                            style = LkTypography.getBody(),
                            color = LkSuccess,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = step.jsonPrimitive.content,
                            style = LkTypography.getBody(),
                            color = LkTextPrimary
                        )
                    }
                }
            }
        }

        // Formulas
        val formulas = calculationOutput["formulas"]?.jsonArray
        if (formulas != null && formulas.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Bu sonuca nasıl ulaşıldı?",
                style = LkTypography.getBodySmall(),
                color = LkTextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                formulas.forEach { formula ->
                    Text(
                        text = formula.jsonPrimitive.content,
                        style = LkTypography.getMicro(),
                        color = LkTextSecondary
                    )
                }
            }
        }

        // Actions
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LkButton(
                text = "Listeye dön",
                onClick = onListClick,
                variant = com.localkarar.app.ui.components.LkButtonVariant.SECONDARY,
                modifier = Modifier.weight(1f)
            )
            LkButton(
                text = "Yeniden Hesapla",
                onClick = onRestart,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricRow(label: String, formattedValue: String, showDivider: Boolean) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = LkTypography.getBody(),
                color = LkTextSecondary
            )
            Text(
                text = formattedValue,
                style = LkTypography.getMetric(),
                color = LkTextPrimary
            )
        }
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(LkLineSoft)
            )
        }
    }
}

private fun formatMetricValue(value: Double, format: String): String {
    return when (format) {
        "money" -> "${value.toInt()} ₺" // For now, simplified
        "percent" -> "%${value}"
        "months" -> "${value.toInt()} ay"
        "days" -> "${value.toInt()} gün"
        else -> value.toString()
    }
}
