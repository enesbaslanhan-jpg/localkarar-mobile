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
import com.localkarar.app.ui.components.LkHairline
import com.localkarar.app.ui.components.LkListRow
import com.localkarar.app.ui.components.LkRowGroup
import com.localkarar.app.ui.components.LkMenuGrubu
import com.localkarar.app.ui.components.LkMenuOgesi
import com.localkarar.app.ui.components.LkMenuSheet
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

    /*
     * Ortak menu cekmecesi (`LkMenuSheet`).
     *
     * 🔴 BU YUZEY IKI KEZ YENIDEN CIZILDI: once iki sutunlu ciplak kart
     * izgarasi (urun sahibi begenmedi), sonra duz gruplanmis liste (fazla
     * sade bulundu). Ucuncusu ortak bilesende: gruplar cizgiyle
     * kapatiliyor, kutucuklar yuzeyden yukseliyor ve basilinca iceri
     * cokuyor. Isletme Bolumleri secicisiyle AYNI bilesen — ayni is icin
     * iki desen kalmadi.
     */
    LkMenuSheet(
        baslik = "Ürün Merkezi",
        altBaslik = "Tüm LocalKarar modülleri ve araçları",
        onClose = onClose,
        modifier = modifier,
        gruplar = groups.map { grup ->
            LkMenuGrubu(
                baslik = grup.groupTitle,
                ogeler = grup.items.map { oge ->
                    LkMenuOgesi(
                        id = oge.title,
                        baslik = oge.title,
                        aciklama = oge.description,
                        ikon = oge.icon,
                        onClick = { onNavigate(oge.destination) }
                    )
                }
            )
        }
    )
}
