package com.localkarar.app.ui.components.decision

import androidx.compose.foundation.background
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
import com.localkarar.app.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ArrowForward
import kotlinx.serialization.json.*

@Composable
fun LkDecisionResultPanel(
    snapshot: JsonElement?,
    modifier: Modifier = Modifier
) {
    if (snapshot == null || snapshot !is JsonObject) return
    val calculationOutput = snapshot["calculationOutput"]?.jsonObject ?: return
    val decisionLabel = calculationOutput["decisionLabel"]?.jsonPrimitive?.content ?: "BİLİNMİYOR"
    val decisionTone = calculationOutput["decisionTone"]?.jsonPrimitive?.content ?: "neutral"
    val summary = calculationOutput["summary"]?.jsonPrimitive?.content ?: ""

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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = summary,
                style = LkTypography.getBody(),
                color = LkTextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Metrics
        val metrics = calculationOutput["metrics"]?.jsonArray
        if (metrics != null && metrics.isNotEmpty()) {
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
                metrics.forEachIndexed { index, el ->
                    val metric = el.jsonObject
                    val label = metric["label"]?.jsonPrimitive?.content ?: ""
                    val value = metric["value"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                    val format = metric["format"]?.jsonPrimitive?.content ?: "number"

                    val formattedValue = when (format) {
                        "money" -> "${value.toInt()} ₺"
                        "percent" -> "%$value"
                        "months" -> "${value.toInt()} Ay"
                        "days" -> "${value.toInt()} Gün"
                        else -> value.toString()
                    }

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
                    if (index < metrics.size - 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(LkLineSoft)
                        )
                    }
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
                
                val formattedValue = when (format) {
                    "money" -> "${value.toInt()} ₺"
                    "percent" -> "%$value"
                    else -> value.toString()
                }
                
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
                    Column {
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
                    Text(
                        text = formattedValue,
                        style = LkTypography.getMetric(),
                        color = sColor
                    )
                }
            }
        }

        // Safe Next Steps
        val nextSteps = calculationOutput["safeNextSteps"]?.jsonArray
        if (nextSteps != null && nextSteps.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Önerilen Adımlar",
                style = LkTypography.getSectionTitle(),
                color = LkTextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LkSurfacePanel)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                nextSteps.forEach { step ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = LkPrimary,
                            modifier = Modifier.size(16.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = step.jsonPrimitive.content,
                            style = LkTypography.getBody(),
                            color = LkTextPrimary
                        )
                    }
                }
            }
        }
    }
}



