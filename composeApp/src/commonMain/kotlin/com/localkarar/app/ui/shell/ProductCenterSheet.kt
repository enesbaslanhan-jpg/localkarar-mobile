package com.localkarar.app.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.localkarar.app.navigation.Destination
import com.localkarar.app.ui.theme.*

private data class ProductItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val destination: Destination
)

private data class ProductGroup(
    val groupTitle: String,
    val items: List<ProductItem>
)

@Composable
fun ProductCenterSheet(
    activeWorkspaceId: String?,
    onNavigate: (Destination) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val groups = listOf(
        ProductGroup(
            groupTitle = "KARAR VER",
            items = listOf(
                ProductItem(
                    title = "Karar Araçları",
                    description = "Karar matrisleri & analizler",
                    icon = Icons.Outlined.AccountBalance,
                    destination = Destination.DecisionTools()
                ),
                ProductItem(
                    title = "Hesaplamalar",
                    description = "Finansal model & formüller",
                    icon = Icons.Outlined.Calculate,
                    destination = Destination.Calculations
                )
            )
        ),
        ProductGroup(
            groupTitle = "ÖĞREN",
            items = listOf(
                ProductItem(
                    title = "Kurslar",
                    description = "Eğitim modülleri & dersler",
                    icon = Icons.Outlined.School,
                    destination = Destination.Courses
                ),
                ProductItem(
                    title = "AI Mentor",
                    description = "Kişisel yapay zeka danışmanı",
                    icon = Icons.Outlined.Psychology,
                    destination = Destination.AiMentor
                )
                /*
                 * BILGI KUTUPHANESI ve OGRENME YOLU URUNDEN KALDIRILDI.
                 *
                 * Urun sahibi karari (03.09.2026): deneme amacli iceriklerdi;
                 * urunun ogrenme yuzeyi 38 kanonik kurs. Webde de rotalari
                 * silindi, yalniz menuden cikarilmakla kalinmadi.
                 *
                 * Ekranlar, ViewModel'ler ve DTO'lar da silindi: ulasilamayan
                 * kod birakmak, sonraki kisiye calisan bir ozellik gibi
                 * gorunur.
                 */

            )
        ),
        ProductGroup(
            groupTitle = "TAKİP ET",
            items = listOf(
                ProductItem(
                    title = "Haberler",
                    description = "Gündem ve mevzuat akışı",
                    icon = Icons.Outlined.Newspaper,
                    destination = Destination.News
                ),
                ProductItem(
                    title = "İşletme Takibi",
                    description = "Kayıtlar, belgeler & operasyon",
                    icon = Icons.Outlined.Business,
                    destination = if (activeWorkspaceId != null) {
                        Destination.WorkspaceHome(activeWorkspaceId)
                    } else {
                        Destination.Workspaces
                    }
                )
            )
        ),
        ProductGroup(
            groupTitle = "SOSYAL",
            items = listOf(
                ProductItem(
                    title = "Topluluk",
                    description = "Paylaşımlar ve etkileşim",
                    icon = Icons.Outlined.Groups,
                    destination = Destination.Community()
                ),
                ProductItem(
                    title = "Profil",
                    description = "Kullanıcı bilgileri & ayarlar",
                    icon = Icons.Outlined.Person,
                    destination = Destination.Profile
                )
            )
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LkSurfaceCanvas, shape = LkShapes.LG)
            .padding(horizontal = LkSpacing.Space6, vertical = LkSpacing.Space6)
            .verticalScroll(rememberScrollState())
    ) {
        // Sheet Handle / Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(LkShapes.SM)
                        .background(LkSurfaceSignature),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Apps,
                        contentDescription = "Ürün Merkezi",
                        tint = LkPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(LkSpacing.Space3))
                Column {
                    Text(
                        text = "Ürün Merkezi",
                        style = LkTypography.getSectionTitle(),
                        color = LkTextPrimary
                    )
                    Text(
                        text = "Tüm LocalKarar modülleri ve araçları",
                        style = LkTypography.getMicro(),
                        color = LkTextSecondary
                    )
                }
            }

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Kapat",
                    tint = LkTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(LkSpacing.Space4))
        Divider(color = LkLineSoft)
        Spacer(modifier = Modifier.height(LkSpacing.Space4))

        // Product Groups
        groups.forEachIndexed { groupIndex, group ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = group.groupTitle,
                    style = LkTypography.getMetadata(),
                    color = LkPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = LkSpacing.Space2)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
                ) {
                    group.items.forEach { item ->
                        ProductCard(
                            item = item,
                            onClick = { onNavigate(item.destination) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (groupIndex < groups.size - 1) {
                Spacer(modifier = Modifier.height(LkSpacing.Space5))
            }
        }

        Spacer(modifier = Modifier.height(LkSpacing.Space8))
    }
}

@Composable
private fun ProductCard(
    item: ProductItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(LkShapes.MD)
            .background(LkSurfacePanel)
            .border(1.dp, LkLineSoft, LkShapes.MD)
            .clickable(onClick = onClick)
            .padding(LkSpacing.Space4)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(LkShapes.SM)
                        .background(LkSurfaceSunken),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = LkPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(LkSpacing.Space3))
                Text(
                    text = item.title,
                    style = LkTypography.getBodyStrong(),
                    color = LkTextPrimary,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(LkSpacing.Space2))
            Text(
                text = item.description,
                style = LkTypography.getMicro(),
                color = LkTextSecondary,
                maxLines = 2
            )
        }
    }
}
