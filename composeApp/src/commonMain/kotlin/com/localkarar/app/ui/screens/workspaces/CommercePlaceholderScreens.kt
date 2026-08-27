package com.localkarar.app.ui.screens.workspaces

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*

@Composable
fun OrdersScreen(
    workspaceId: String,
    onOpenSectionSelector: () -> Unit,
    onBack: () -> Unit
) {
    LkPageLayout(
        title = "İşletme Takibi",
        onBack = onBack,
        actions = {
            WorkspaceSectionPill(
                sectionName = "Siparişler",
                onClick = onOpenSectionSelector,
                modifier = Modifier.padding(end = 12.dp)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(LkSpacing.Space8),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(LkShapes.MD)
                    .background(LkSurfacePanel)
                    .border(1.dp, LkLineStrong, LkShapes.MD),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = LkPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(LkSpacing.Space6))
            Text(
                text = "Sipariş Yönetimi",
                style = LkTypography.getSectionTitle(),
                color = LkTextPrimary
            )
            Spacer(modifier = Modifier.height(LkSpacing.Space2))
            Text(
                text = "Müşteri ve tedarikçi sipariş takibi, fatura eşleştirmeleri ve durum güncellemeleri Ticaret paketi ile entegre edilecektir.",
                style = LkTypography.getBody(),
                color = LkTextSecondary,
                modifier = Modifier.padding(horizontal = LkSpacing.Space4)
            )
            Spacer(modifier = Modifier.height(LkSpacing.Space6))
            LkButton(
                text = "Bölüm Değiştir",
                variant = LkButtonVariant.SECONDARY,
                onClick = onOpenSectionSelector
            )
        }
    }
}

@Composable
fun ProductsScreen(
    workspaceId: String,
    onOpenSectionSelector: () -> Unit,
    onBack: () -> Unit
) {
    LkPageLayout(
        title = "İşletme Takibi",
        onBack = onBack,
        actions = {
            WorkspaceSectionPill(
                sectionName = "Ürünler",
                onClick = onOpenSectionSelector,
                modifier = Modifier.padding(end = 12.dp)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(LkSpacing.Space8),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(LkShapes.MD)
                    .background(LkSurfacePanel)
                    .border(1.dp, LkLineStrong, LkShapes.MD),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = LkPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(LkSpacing.Space6))
            Text(
                text = "Ürün ve Hizmet Kataloğu",
                style = LkTypography.getSectionTitle(),
                color = LkTextPrimary
            )
            Spacer(modifier = Modifier.height(LkSpacing.Space2))
            Text(
                text = "Ürün listesi, fiyatlandırma stratejileri, birim maliyetler ve stok durumu Ticaret paketi ile entegre edilecektir.",
                style = LkTypography.getBody(),
                color = LkTextSecondary,
                modifier = Modifier.padding(horizontal = LkSpacing.Space4)
            )
            Spacer(modifier = Modifier.height(LkSpacing.Space6))
            LkButton(
                text = "Bölüm Değiştir",
                variant = LkButtonVariant.SECONDARY,
                onClick = onOpenSectionSelector
            )
        }
    }
}
