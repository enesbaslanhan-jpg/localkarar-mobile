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
import androidx.compose.material.icons.outlined.*

data class ToolStatusConfig(val label: String, val bg: Color, val fg: Color)

fun iconForDecisionCheck(code: String?): ImageVector {
    if (code == null) return Icons.Outlined.Build
    val c = code.uppercase()
    return when {
        c.contains("PROFIT") -> Icons.Outlined.TrendingUp
        c.contains("DISCOUNT") -> Icons.Outlined.ShoppingCart
        c.contains("FREESHIP") -> Icons.Outlined.LocalShipping
        c.contains("MARKETPLACE") -> Icons.Outlined.Store
        c.contains("ADS") -> Icons.Outlined.AdsClick
        c.contains("HIRE") -> Icons.Outlined.PersonAdd
        c.contains("LOAN") -> Icons.Outlined.AccountBalance
        c.contains("CASHFLOW") -> Icons.Outlined.AccountBalanceWallet
        c.contains("BRANCH") -> Icons.Outlined.Business
        c.contains("CAMPAIGN") -> Icons.Outlined.Campaign
        c.contains("STOCK") -> Icons.Outlined.Inventory
        c.contains("CONTINUE") -> Icons.Outlined.Search
        else -> Icons.Outlined.Build
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
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = LkTypography.getBodySmall(),
            color = LkTextSecondary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        val ctaText = when (status) {
            "completed", "complete" -> "Sonucu Gör"
            "in_progress", "started" -> "Devam Et"
            else -> "Aracı Aç"
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(LkPrimary)
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = ctaText,
                    color = LkOnPrimary,
                    style = LkTypography.getBody()
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = LkOnPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}




