package com.localkarar.app.ui.components.decision


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

data class ToolStatusConfig(val label: String, val bg: Color, val fg: Color)

fun iconForDecisionCheck(code: String?): ImageVector {
    if (code == null) return Icons.Default.Build
    val c = code.uppercase()
    return when {
        c.contains("PROFIT") -> Icons.Default.TrendingUp
        c.contains("DISCOUNT") -> Icons.Default.ShoppingCart
        c.contains("FREESHIP") -> Icons.Default.LocalShipping
        c.contains("MARKETPLACE") -> Icons.Default.Store
        c.contains("ADS") -> Icons.Default.AdsClick
        c.contains("HIRE") -> Icons.Default.PersonAdd
        c.contains("LOAN") -> Icons.Default.AccountBalance
        c.contains("CASHFLOW") -> Icons.Default.AccountBalanceWallet
        c.contains("BRANCH") -> Icons.Default.Business
        c.contains("CAMPAIGN") -> Icons.Default.Campaign
        c.contains("STOCK") -> Icons.Default.Inventory
        c.contains("CONTINUE") -> Icons.Default.Search
        else -> Icons.Default.Build
    }
}

@Composable
fun LkDecisionToolCard(
    title: String,
    description: String,
    category: String,
    code: String,
    status: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LkSurfacePanel)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(LkPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconForDecisionCheck(code),
                        contentDescription = category,
                        tint = LkPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = category,
                        style = LkTypography.getMetadata(),
                        color = LkTextMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = title,
                        style = LkTypography.getCardTitle(),
                        color = LkTextPrimary
                    )
                }
            }
            if (status != null && status != "not_started") {
                val config = when (status) {
                    "completed" -> ToolStatusConfig("Tamamlandı", LkSuccess.copy(alpha = 0.15f), LkSuccess)
                    else -> ToolStatusConfig("Devam", LkPrimary.copy(alpha = 0.15f), LkPrimary)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(config.bg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = config.label,
                        style = LkTypography.getMicro(),
                        color = config.fg
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = LkTypography.getBodySmall(),
            color = LkTextSecondary
        )
    }
}




