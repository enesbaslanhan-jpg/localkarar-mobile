package com.localkarar.app.ui.screens.workspaces

import com.localkarar.app.ui.components.rememberLkSheetState
import com.localkarar.app.ui.components.LkSheet
import com.localkarar.app.ui.screens.home.RECORD_TYPE_LABEL
import com.localkarar.app.ui.components.LkProgressPill
import com.localkarar.app.ui.components.LkIconTile
import com.localkarar.app.ui.components.LkRowGroup
import com.localkarar.app.ui.components.LkListRow
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.localkarar.app.ui.components.LkHeroBlock
import com.localkarar.app.ui.theme.LkTypography.numeric
import com.localkarar.app.ui.components.LkButton
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Today
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.core.LkFormatting
import com.localkarar.app.network.dto.BusinessRecordDto
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkInfoPanel
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkMetricCard
import com.localkarar.app.ui.components.LkSection
import com.localkarar.app.ui.components.LkTactileAction
import com.localkarar.app.ui.components.LkHairline
import com.localkarar.app.ui.components.LkSectionHeader
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.WorkspaceHomeUiState
import com.localkarar.app.workspaces.WorkspaceHomeViewModel

@Composable
fun WorkspaceHomeScreen(
    viewModel: WorkspaceHomeViewModel,
    onOpenRecords: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenProducts: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenDocuments: () -> Unit,
    onOpenTeam: () -> Unit,
    onOpenContacts: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenActivity: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRecord: (String) -> Unit,
    onAddRecord: () -> Unit,
    onOpenSectionSelector: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val ordersLoaded by viewModel.ordersLoaded.collectAsState()

    val state = uiState
    val sheetState = rememberLkSheetState()

    /*
     * §24.6 — hero baslik blogu + binen yuzey.
     *
     * Ekranin iki hakim rakami (30 gunluk alacak ve borc) LISTEDEN CIKIP
     * hero'ya tasindi. Onceden dort metrik listenin icinde esit agirliktaydi;
     * ekran acildiginda ilk okunan sey isletmenin adiydi, parasi degil.
     *
     * `LkPageLayout` kullanilmiyor: baslik cubugu marka blogunun ICINDE.
     *
     * En distaki `Box`: cekmece sayfanin UZERINDE duruyor, akisin icinde
     * degil. Sayfa normal kayiyor; cekmece uc kademe arasinda ayri
     * surukleniyor.
     */
    Box(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize()) {

        LkHeroBlock {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = LkSpacing.Space4,
                        end = LkSpacing.Space4,
                        top = LkSpacing.Space4
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Outlined.ArrowBack,
                        contentDescription = "Geri",
                        tint = LkHero.OnHero
                    )
                }
                Text(
                    text = "İşletme Takibi",
                    style = LkTypography.getTitleS(),
                    color = LkHero.OnHero,
                    modifier = Modifier.weight(1f).padding(start = LkSpacing.Space2)
                )
                WorkspaceSectionPill(
                    sectionName = "Genel Bakış",
                    onClick = onOpenSectionSelector
                )
            }

            if (state is WorkspaceHomeUiState.Content) {
                val ozet = state.summary
                if (ozet != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = LkSpacing.Space5, vertical = LkSpacing.Space4)
                    ) {
                        HeroTutar(
                            "30 GÜN ALACAK",
                            LkFormatting.formatMoney(ozet.nextThirtyDays.receivable, state.workspace.currency),
                            Modifier.weight(1f)
                        )
                        Box(
                            Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(androidx.compose.ui.graphics.Color(0x29FFFFFF))
                        )
                        HeroTutar(
                            "30 GÜN BORÇ",
                            LkFormatting.formatMoney(ozet.nextThirtyDays.payable, state.workspace.currency),
                            Modifier.weight(1f).padding(start = LkSpacing.Space4)
                        )
                    }

                    /*
                     * 🔴 MOCKUP'TAKI "EYLUL HEDEFININ %57'SI" UYDURMAYDI.
                     *
                     * Sunucuda hedef/target alani YOK -- ne TrackerSummaryDto'da
                     * ne baska bir uctan geliyor. Uydurma bir finansal hedefi
                     * ekrana basmak kullanicinin gercek sandigi bir sey
                     * gostermek olurdu.
                     *
                     * Yerine GERCEKTEN turetilebilen bir oran: onumuzdeki 30
                     * gunun toplam hareketi icinde tahsilatin payi. Etiket ne
                     * oldugunu acikca soyluyor; "hedef" demiyor.
                     */
                    val alacak = ozet.nextThirtyDays.receivable
                    val borc = ozet.nextThirtyDays.payable
                    val toplam = alacak + borc
                    if (toplam > 0.0) {
                        Column(
                            Modifier.fillMaxWidth().padding(
                                start = LkSpacing.Space5,
                                end = LkSpacing.Space5,
                                bottom = LkSpacing.Space2
                            )
                        ) {
                            /*
                             * Dolgu KOYU, yol ACIK — referans desenin yonu bu.
                             * Ilk denemede tersi yapilmisti (beyaz dolgu,
                             * saydam yol) ve hero uzerinde koca bir beyaz blok
                             * gibi bagirarak ekranin hakim ogesi oluyordu;
                             * oysa hakim oge ustteki tutarlar olmali.
                             */
                            LkProgressPill(
                                oran = (alacak / toplam).toFloat(),
                                sagDeger = LkFormatting.formatMoney(toplam, state.workspace.currency),
                                dolguRengi = LkBrand.B700,
                                dolguUstuRengi = androidx.compose.ui.graphics.Color.White,
                                yolRengi = androidx.compose.ui.graphics.Color(0xE8F1F5F7),
                                yolUstuRengi = LkBrand.B700
                            )
                            Spacer(Modifier.height(LkSpacing.Space2))
                            Text(
                                text = "30 günlük hareketin bu kadarı tahsilat.",
                                style = LkTypography.getMetadata(),
                                color = LkHero.OnHeroSecondary
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(LkSpacing.Space5))
        }

        /* `weight(1f)` — Column icinde `fillMaxSize()` KALAN degil TUM
           yuksekligi ister; icerik dock altinda kalirdi. */
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .offset(y = (-22).dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(LkSurfaceCanvas)
        ) {
        when (state) {
            is WorkspaceHomeUiState.Loading -> LkLoadingState()
            is WorkspaceHomeUiState.Error -> LkErrorState(
                message = state.message,
                onRetry = { viewModel.load() }
            )
            is WorkspaceHomeUiState.Content -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = LkSpacing.Space4,
                        end = LkSpacing.Space4,
                        top = LkSpacing.Space5,
                        bottom = LkSpacing.Space4
                    ),
                    verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
                ) {
                    item {
                        LkSectionHeader(
                            title = state.workspace.name,
                            subtitle = listOfNotNull(
                                state.workspace.sector,
                                state.workspace.city
                            ).joinToString(" • ").ifBlank { null }
                        )
                    }

                    state.summary?.let { summary ->
                        item {
                            // Prototipteki `metrics-row`: kutu YOK, bolum acik.
                            // Onceden dort ayri `LkMetricCard` vardi ve sayfa
                            // kart yigini gibi duruyordu.
                            LkSection(title = "Özet") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
                                ) {
                                    OzetMetrik("Açık Kayıt", summary.counts.open.toString(), LkTextPrimary, Modifier.weight(1f))
                                    OzetMetrik(
                                        "Geciken",
                                        summary.counts.overdue.toString(),
                                        if (summary.counts.overdue > 0) LkDanger else LkTextPrimary,
                                        Modifier.weight(1f)
                                    )
                                }
                                /*
                                 * 30 gunluk alacak ve borc BURADAN KALKTI —
                                 * hero'ya tasindilar. Ayni rakami iki yerde
                                 * gostermek hiyerarsiyi bozar ve kullaniciya
                                 * iki farkli sey sanilma riski verir.
                                 */

                                // Yonu belirsiz kayitlar hicbir toplama girmiyor;
                                // kendi satiri olmadan ekranda hic gorunmuyorlar.
                                val bekleyen = summary.awaitingDirection
                                if (bekleyen != null && bekleyen.count > 0) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(LkWarning.copy(alpha = 0.12f), LkShapes.SM)
                                            .padding(LkSpacing.Space3),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Outlined.HelpOutline,
                                            contentDescription = null,
                                            tint = LkWarning,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(LkSpacing.Space3))
                                        Column {
                                            Text(
                                                "Yön bekliyor · " + LkFormatting.formatMoney(bekleyen.amount, state.workspace.currency),
                                                style = LkTypography.getBodyStrong(),
                                                color = LkTextPrimary
                                            )
                                            Text(
                                                bekleyen.count.toString() + " kayıt · borç mu alacak mı belirsiz",
                                                style = LkTypography.getMetadata(),
                                                color = LkTextMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (summary.upcoming.isNotEmpty()) {
                            item {
                                LkSection(title = "Yaklaşan Kayıtlar") {
                                    /*
                                     * §24 — satirlar TEK YUKSELTILMIS YUZEYDE.
                                     *
                                     * Sarmalanmadiginda acik temada zeminle ayni
                                     * renge dusuyorlar ve liste bir nesne gibi
                                     * degil, zemine yazilmis metin gibi okunuyor.
                                     * Koyu temada daha az belliydi; emulatorde
                                     * acik tema karesinde ortaya cikti.
                                     */
                                    LkRowGroup {
                                        summary.upcoming.take(5).forEachIndexed { i, record ->
                                            UpcomingRecordRow(record, onOpen = { onOpenRecord(record.id) })
                                            if (i != minOf(4, summary.upcoming.lastIndex)) LkHairline()
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LkSectionHeader(title = "İşletme Bölümleri")
                            Text(
                                text = "Tümünü Seç",
                                style = LkTypography.getMicro(),
                                color = LkPrimary,
                                modifier = Modifier.clickable(onClick = onOpenSectionSelector)
                            )
                        }
                        Spacer(modifier = Modifier.height(LkSpacing.Space3))
                        SectionNavRow(
                            items = listOf(
                                SectionNavItem("Kayıtlar", Icons.Outlined.ReceiptLong, onOpenRecords),
                                SectionNavItem("Siparişler", Icons.Outlined.ShoppingCart, onOpenOrders),
                                SectionNavItem("Ürünler", Icons.Outlined.Inventory2, onOpenProducts),
                                SectionNavItem("Belgeler", Icons.Outlined.AttachFile, onOpenDocuments),
                                // Sira webdeki WORKSPACE_NAV_TABS ile AYNI olmali:
                                // bildirimler takvimden ONCE. Web tarafinda bu sirayi
                                // koruyan bir regresyon testi var (navigation.js notu).
                                SectionNavItem("Bildirimler", Icons.Outlined.Notifications, onOpenNotifications),
                                SectionNavItem("Takvim", Icons.Outlined.CalendarMonth, onOpenCalendar),
                                SectionNavItem("Ekip", Icons.Outlined.Group, onOpenTeam),
                                SectionNavItem("Kişiler", Icons.Outlined.Contacts, onOpenContacts),
                                SectionNavItem("Aktiviteler", Icons.Outlined.Construction, onOpenActivity),
                                SectionNavItem("Ayarlar", Icons.Outlined.Settings, onOpenSettings)
                            )
                        )
                    }

                    item {
                        LkButton(
                            text = "Yeni Kayıt Ekle",
                            onClick = onAddRecord,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        }   // binen yuzey
    }       // hero + yuzey

    /*
     * PAZARYERI SIPARISLERI CEKMECESI — §24, uc kademe.
     *
     * Tepeden bakis kademesinde baslik ve ilk satir gorunur; yukari
     * cekilince tam ekrana yaklasir ve ustte baglam icin bir serit kalir.
     * Arkadaki sayfa kaybolmaz — Apple Maps davranisi.
     *
     * Yalnizca siparis GERCEKTEN yuklendiyse ciziliyor. Entegrasyon
     * kapaliyken bos bir cekmece ekranin altini kaplamasin.
     */
    if (ordersLoaded && orders.isNotEmpty()) {
        LkSheet(
            state = sheetState,
            handle = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LkSpacing.Space5, vertical = LkSpacing.Space2),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Pazaryeri siparişleri",
                        style = LkTypography.getTitleS(),
                        color = LkTextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${orders.size} sipariş",
                        style = LkTypography.getMetadata(),
                        color = LkTileInk,
                        modifier = Modifier
                            .clip(LkShapes.FULL)
                            .background(LkSurfaceTile)
                            .padding(horizontal = LkSpacing.Space3, vertical = 4.dp)
                    )
                }
            },
            body = {
                LkRowGroup(
                    modifier = Modifier.padding(
                        start = LkSpacing.Space4,
                        end = LkSpacing.Space4,
                        top = LkSpacing.Space2
                    )
                ) {
                    orders.take(12).forEachIndexed { i, order ->
                        LkListRow(
                            /*
                             * OrderDto'da `productTitle` ve `totalAmount` YOK —
                             * once oyle varsayilmisti. Gercek alanlar:
                             * customerName / orderNumber / provider / grossAmount.
                             */
                            baslik = order.customerName
                                ?: order.orderNumber?.let { "Sipariş $it" }
                                ?: "Sipariş",
                            altBaslik = listOfNotNull(
                                order.provider,
                                order.orderNumber?.let { "#$it" }
                            ).joinToString(" · ").ifBlank { null },
                            kategori = SIPARIS_DURUM_ETIKET[order.status] ?: order.status,
                            tutar = order.grossAmount?.let {
                                LkFormatting.formatMoney(it, order.currency)
                            },
                            onClick = onOpenOrders,
                            ikon = {
                                Icon(
                                    Icons.Outlined.ShoppingCart,
                                    contentDescription = null,
                                    tint = LkTileInk,
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                        )
                        if (i != minOf(11, orders.lastIndex)) LkHairline()
                    }
                }
            }
        )
    }
    }   // cekmeceyi tasiyan Box
}

/*
 * Siparis durumlarinin Turkce karsiliklari.
 *
 * Ham sunucu degeri ("CREATED", "SHIPPED") ekrana YAZILMAZ; Siparisler
 * ekranindaki `STATUS_OPTIONS` ile ayni adlandirma kullaniliyor ki iki
 * ekran ayni duruma iki ad vermesin.
 */
private val SIPARIS_DURUM_ETIKET = mapOf(
    "CREATED" to "Yeni",
    "PROCESSING" to "İşleniyor",
    "SHIPPED" to "Kargoda",
    "DELIVERED" to "Teslim",
    "CANCELLED" to "İptal",
    "RETURNED" to "İade",
    "PARTIALLY_RETURNED" to "Kısmi iade"
)

/** Hero icindeki tutar. Etiket §24.6 geregi %85 beyaz opaklikta. */
@Composable
private fun HeroTutar(etiket: String, deger: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(etiket, style = LkTypography.getMetadata(), color = LkHero.OnHeroSecondary)
        Spacer(Modifier.height(LkSpacing.Space1))
        Text(
            deger,
            style = LkTypography.getTitleS().numeric(),
            color = LkHero.OnHero,
            maxLines = 1
        )
    }
}

@Composable
private fun OzetMetrik(
    etiket: String,
    deger: String,
    renk: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(etiket, style = LkTypography.getBodySmall(), color = LkTextMuted)
        Text(
            text = deger,
            style = LkTypography.getMetric().copy(fontFeatureSettings = "tnum"),
            color = renk,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}

private data class SectionNavItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
private fun SectionNavRow(items: List<SectionNavItem>) {
    // Prototipteki `actions-grid` + `tactile-action-btn` deseni.
    //
    // Onceden her bolum tam genislikte KART idi: 11 bolum icin ekranin
    // tamami kart yigini oluyordu ve Ana Sayfa'daki "Hizli Islemler"
    // izgarasindan farkli bir dil konusuyordu. Ayni sey ayni gorunmeli.
    //
    // Dort sutun: 11 oge uc satira sigiyor, dokunma hedefi korunuyor
    // (`LkTactileAction` icinde 44dp kutu + etiket).
    val satirlar = items.chunked(4)
    Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)) {
        satirlar.forEach { satir ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
            ) {
                satir.forEach { item ->
                    /* §24 — Ana Sayfa'daki hizli islem izgarasiyla AYNI
                       bilesen. Ayni sey ayni gorunmeli. */
                    LkIconTile(
                        etiket = item.label,
                        onClick = item.onClick,
                        modifier = Modifier.weight(1f),
                        ikon = {
                            Icon(
                                item.icon,
                                contentDescription = null,
                                tint = LkTileInk,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    )
                }
                // Son satir eksikse hizalama bozulmasin diye bosluk.
                repeat(4 - satir.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun UpcomingRecordRow(
    record: BusinessRecordDto,
    onOpen: () -> Unit
) {
    val dueDate = LkDateUtils.parseDate(record.dueAt)
    val overdue = dueDate?.let { LkDateUtils.daysUntil(it) } ?: 0
    val gecikti = record.dueAt != null && dueDate != null && overdue < 0

    /*
     * §24 satir anatomisi. Onceden ikonsuz, ayracsiz iki sutunlu bir satirdi.
     *
     * Gecikme durumu SADECE RENKLE degil, "Gecikti" kelimesiyle de
     * belirtiliyor — alt basligin icinde.
     */
    LkListRow(
        baslik = record.title,
        altBaslik = listOfNotNull(
            dueDate?.let { if (gecikti) "Gecikti · " + LkDateUtils.formatShortDate(it) else LkDateUtils.formatShortDate(it) }
        ).joinToString(" ").ifBlank { null },
        /*
         * 🔴 KATEGORI INGILIZCE CIKIYORDU: `record.type` ham sunucu degeri
         * ("payment", "promissory_note"). `replaceFirstChar { uppercase }`
         * bunu "Payment" yapiyordu ve ekranda oyle duruyordu.
         *
         * `RECORD_TYPE_LABEL` zaten Ana Sayfa'da bu eslemeyi tutuyor; ayni
         * kaynak kullaniliyor ki iki ekran ayni kayda iki ad vermesin.
         * Eslemede olmayan bir tur gelirse ham deger degil "Kayıt" yazilir.
         */
        kategori = RECORD_TYPE_LABEL[record.type] ?: "Kayıt",
        tutar = record.amount?.let { LkFormatting.formatMoney(it, record.currency) } ?: "—",
        tutarRengi = when {
            gecikti -> LkDanger
            record.direction == "receivable" -> LkTextPrimary
            else -> LkTextPrimary
        },
        onClick = onOpen,
        ikon = {
            Icon(
                when (record.direction) {
                    "receivable" -> Icons.Outlined.SouthWest
                    "payable" -> Icons.Outlined.NorthEast
                    else -> Icons.Outlined.ReceiptLong
                },
                contentDescription = null,
                tint = if (gecikti) LkDanger else LkTileInk,
                modifier = Modifier.size(21.dp)
            )
        }
    )
}