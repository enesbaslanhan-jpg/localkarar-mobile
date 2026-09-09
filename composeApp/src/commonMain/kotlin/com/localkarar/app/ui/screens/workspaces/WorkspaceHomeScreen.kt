package com.localkarar.app.ui.screens.workspaces

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import com.localkarar.app.ui.screens.home.RECORD_TYPE_LABEL
import com.localkarar.app.ui.components.LkProgressPill
import com.localkarar.app.ui.components.rememberLkSayac
import com.localkarar.app.ui.components.rememberLkSheetState
import com.localkarar.app.ui.components.LkSheet
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
import com.localkarar.app.ui.components.LkLoadingDesen
import com.localkarar.app.ui.components.LkLoadingState
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
    onOpenKararRaporu: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val sayilar by viewModel.sayilar.collectAsState()
    val kararGorevleri by viewModel.kararGorevleri.collectAsState()
    val sheetState = rememberLkSheetState()
    var seciliDurum by remember { mutableStateOf<String?>(null) }
    val ordersLoaded by viewModel.ordersLoaded.collectAsState()

    val state = uiState

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
                /*
                 * 🔴 GERI OKU KOK EKRANDA DA CIZILIYOR VE HICBIR SEY
                 * YAPMIYORDU. Bu ekran alt gezinme sekmesi; yigininda
                 * altinda hicbir sey yok, `popBackStack` false donuyor.
                 * Calismayan bir kontrolu cizmek, kullaniciya uygulamanin
                 * bozuk oldugunu soyler. Ok yalniz gercekten donulecek bir
                 * yer varken var.
                 */
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Outlined.ArrowBack,
                            contentDescription = "Geri",
                            tint = LkHero.OnHero
                        )
                    }
                } else {
                    Spacer(Modifier.width(LkSpacing.Space5))
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
                            ozet.nextThirtyDays.receivable,
                            { LkFormatting.formatMoney(it, state.workspace.currency) },
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
                            ozet.nextThirtyDays.payable,
                            { LkFormatting.formatMoney(it, state.workspace.currency) },
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
            is WorkspaceHomeUiState.Loading -> LkLoadingState(desen = LkLoadingDesen.DETAY)
            is WorkspaceHomeUiState.Error -> LkErrorState(
                message = state.message,
                onRetry = { viewModel.load() }
            )
            is WorkspaceHomeUiState.Content -> {
                /*
                 * GENEL BAKIS — onaylanan foy ("Takip 1").
                 *
                 * 🔴 EKRAN HICBIR SORUYU CEVAPLAMIYORDU: hero'nun altinda
                 * yalniz "Yaklaşan Kayıtlar" ve bir dugme vardi; isletmenin
                 * geri kalanina ancak bolum cekmecesinden ulasiliyordu.
                 *
                 * Foydeki dort grup: DURUM (sayilar), PARA, TICARET,
                 * OPERASYON. Grup ve satir adlari bolum cekmecesiyle AYNI —
                 * ayni yere iki farkli isim ogretmiyoruz.
                 *
                 * ⚠️ Alt yazidaki her sayi sunucudan geliyor; gelmeyen sayi
                 * hic yazilmiyor (bkz. `WorkspaceHomeViewModel.BolumSayilari`).
                 */
                val summary = state.summary
                val paraBirimi = state.workspace.currency

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = LkSpacing.Space4,
                        end = LkSpacing.Space4,
                        top = LkSpacing.Space5,
                        bottom = LkSpacing.Space8
                    ),
                    verticalArrangement = Arrangement.spacedBy(LkSpacing.Space5)
                ) {

                    // ------------------------------------------- DURUM
                    if (summary != null) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                                LkSectionHeader(title = "BUGÜN NE DURUMDAYIM?")
                                Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                                    DurumKutusu(
                                        sayi = summary.counts.overdue,
                                        etiket = "Geciken",
                                        vurgu = LkDanger,
                                        onClick = onOpenRecords,
                                        modifier = Modifier.weight(1f)
                                    )
                                    DurumKutusu(
                                        sayi = summary.counts.dueToday,
                                        etiket = "Bugün",
                                        vurgu = LkWarning,
                                        onClick = onOpenCalendar,
                                        modifier = Modifier.weight(1f)
                                    )
                                    DurumKutusu(
                                        sayi = summary.counts.open,
                                        etiket = "Açık kayıt",
                                        vurgu = LkLineStrong,
                                        onClick = onOpenRecords,
                                        modifier = Modifier.weight(1f)
                                    )
                                    DurumKutusu(
                                        sayi = summary.counts.awaitingDirection,
                                        etiket = "Yön bekleyen",
                                        vurgu = LkSuccess,
                                        onClick = onOpenRecords,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    /*
                     * SON HAREKETLER — ekranin GERCEK VERI blogu.
                     *
                     * 🔴 BURADA "TİCARET" VE "OPERASYON" DIYE IKI GEZINTI
                     * GRUBU VARDI (Siparişler, Ürünler, Takvim, Belgeler,
                     * Bildirimler). Hepsi zaten ustteki bolum menusunde
                     * duruyor; ana ekran ayni menuyu ikinci kez cizmek
                     * yerine PARAYI gostermeli. Foydeki ekran da oyle:
                     * ozet → son hareketler → alttan acilan siparis
                     * cekmecesi.
                     */
                    if (summary != null) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    LkSectionHeader(
                                        title = "SON HAREKETLER",
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "Tümü ›",
                                        style = LkTypography.getLabel(),
                                        color = LkPrimary,
                                        modifier = Modifier
                                            .clickable(onClick = onOpenRecords)
                                            .padding(vertical = LkSpacing.Space2)
                                    )
                                }

                                /*
                                 * Gecikenler ONCE: vadesi gecmis bir kayit,
                                 * yaklasan bir kayittan her zaman daha
                                 * acildir. Ikisi de sunucudan geliyor
                                 * (`overdue`, `upcoming`).
                                 */
                                val hareketler = (summary.overdue + summary.upcoming).take(8)

                                LkRowGroup {
                                    hareketler.forEachIndexed { i, record ->
                                        UpcomingRecordRow(record, onOpen = { onOpenRecord(record.id) })
                                        if (i != hareketler.lastIndex) LkHairline()
                                    }

                                    /*
                                     * Yonu belirsiz kayitlar HICBIR toplama
                                     * girmiyor; bu satir olmadan ekranin
                                     * hicbir yerinde gorunmuyorlar.
                                     */
                                    val bekleyen = summary.awaitingDirection
                                    if (bekleyen != null && bekleyen.count > 0) {
                                        if (hareketler.isNotEmpty()) LkHairline()
                                        LkListRow(
                                            baslik = "${bekleyen.count} kaydın yönü belirsiz",
                                            altBaslik = "Tahsilat mı ödeme mi seçilmeli",
                                            tutar = LkFormatting.formatMoney(bekleyen.amount, paraBirimi),
                                            tutarRengi = LkWarning,
                                            onClick = onOpenRecords,
                                            ikon = {
                                                Icon(
                                                    Icons.Outlined.HelpOutline,
                                                    contentDescription = null,
                                                    tint = LkWarning,
                                                    modifier = Modifier.size(21.dp)
                                                )
                                            }
                                        )
                                    }

                                    if (hareketler.isEmpty() && (bekleyen == null || bekleyen.count == 0)) {
                                        LkListRow(
                                            baslik = "Hareket yok",
                                            altBaslik = "Önümüzdeki 30 günde vadesi gelen bir kayıt yok"
                                        )
                                    }
                                }
                            }
                        }
                    }


                    /*
                     * KARARLARDAN GELEN GOREVLER.
                     *
                     * 🔴 Bu bolum YOKTU. Karar araci karari bir goreve
                     * bagliyordu; o ekrandan cikinca gorev siradan bir
                     * kayda donusuyor, hangi karardan dogdugu ve ne
                     * beklendigi bir daha gorunmuyordu.
                     *
                     * Hic karar gorevi yoksa bolum CIZILMIYOR: bos bir
                     * kutu ana sayfada yer kaplamaktan baska bir sey
                     * yapmaz.
                     */
                    if (kararGorevleri.isNotEmpty()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    LkSectionHeader(
                                        title = "KARARLARDAN GELEN GÖREVLER",
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "Rapor ›",
                                        style = LkTypography.getLabel(),
                                        color = LkPrimary,
                                        modifier = Modifier
                                            .clickable(onClick = onOpenKararRaporu)
                                            .padding(vertical = LkSpacing.Space2)
                                    )
                                }
                                LkRowGroup {
                                    kararGorevleri.forEachIndexed { i, satir ->
                                        LkListRow(
                                            baslik = satir.baslik,
                                            /* Beklenen sonuc yoksa UYDURULMUYOR;
                                               satir alt yazisiz kaliyor. */
                                            altBaslik = satir.beklenen,
                                            /* ⚠️ `sonucBekliyor` DEGIL `sonucuYazilmali`:
                                               daha baslamamis bir goreve "Sonucu bekliyor"
                                               demek, is bitmeden sonuc bekleniyormus gibi
                                               gorunuyordu (emulatorde yakalandi). */
                                            kategori = when {
                                                satir.sonucuYazilmali -> "Sonucu bekliyor"
                                                satir.gecikti -> "Gecikti"
                                                else -> null
                                            },
                                            onClick = satir.kayitId?.let { id -> { onOpenRecord(id) } },
                                            ikon = {
                                                Icon(
                                                    Icons.Outlined.Gavel,
                                                    contentDescription = null,
                                                    tint = LkPrimary,
                                                    modifier = Modifier.size(21.dp)
                                                )
                                            }
                                        )
                                        if (i != kararGorevleri.lastIndex) LkHairline()
                                    }
                                }
                            }
                        }
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
                /*
                 * Durum haplari GERCEK filtre — sunucuya gitmiyor, zaten
                 * yuklu listeyi suzuyor. Donem haplarindan farki bu: burada
                 * suzulecek veri elimizde, orada yoktu.
                 *
                 * Yalnizca listede GERCEKTEN bulunan durumlar gosteriliyor;
                 * hicbir siparisin olmadigi bir duruma hap koymak bos sonuc
                 * veren bir dugme olurdu.
                 */
                val mevcutDurumlar = orders.map { it.status }.distinct()
                val haplar = listOf<String?>(null) + SIPARIS_DURUM_ETIKET.keys
                    .filter { it in mevcutDurumlar }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(
                            start = LkSpacing.Space4,
                            end = LkSpacing.Space4,
                            top = LkSpacing.Space2,
                            bottom = LkSpacing.Space3
                        ),
                    horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)
                ) {
                    haplar.forEach { durum ->
                        val secili = seciliDurum == durum
                        Text(
                            text = durum?.let { SIPARIS_DURUM_ETIKET[it] ?: it } ?: "Tümü",
                            style = LkTypography.getMetadata(),
                            color = if (secili) LkOnPrimary else LkTextSecondary,
                            modifier = Modifier
                                .clip(LkShapes.FULL)
                                .background(if (secili) LkPrimaryFill else LkSurfaceTile)
                                .clickable { seciliDurum = durum }
                                .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space2)
                        )
                    }
                }

                val gosterilen = orders.filter { seciliDurum == null || it.status == seciliDurum }

                LkRowGroup(
                    modifier = Modifier.padding(
                        start = LkSpacing.Space4,
                        end = LkSpacing.Space4
                    )
                ) {
                    gosterilen.take(12).forEachIndexed { i, order ->
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
                        if (i != minOf(11, gosterilen.lastIndex)) LkHairline()
                    }
                }
            }
        )
    }
    }   // cekmeceyi tasiyan Box
}

/**
 * Durum kutucugu — foydeki dort sayidan biri.
 *
 * Ust kenardaki renk seridi durumu renkle SOYLEMIYOR, yalniz ayirt
 * ediyor; anlam etiketten okunuyor (§19: durum yalniz renge yaslanmaz).
 */
@Composable
private fun DurumKutusu(
    sayi: Int,
    etiket: String,
    vurgu: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(LkShapes.MD)
            .background(LkSurfacePanel)
            .clickable(onClick = onClick)
            .heightIn(min = 76.dp)
            .padding(top = LkSpacing.Space2, bottom = LkSpacing.Space3),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .padding(bottom = LkSpacing.Space2)
                .width(24.dp)
                .height(3.dp)
                .clip(LkShapes.FULL)
                .background(vurgu)
        )
        Text(
            text = sayi.toString(),
            style = LkTypography.getTitleS().numeric(),
            color = LkTextPrimary
        )
        Text(
            text = etiket,
            style = LkTypography.getMicro(),
            color = LkTextSecondary,
            maxLines = 1
        )
    }
}

@Composable
private fun BolumIkonu(ikon: ImageVector) {
    Icon(
        imageVector = ikon,
        contentDescription = null,
        tint = LkTileInk,
        modifier = Modifier.size(21.dp)
    )
}

@Composable
private fun BolumOku() {
    Icon(
        Icons.Outlined.ChevronRight,
        contentDescription = null,
        tint = LkTextMuted,
        modifier = Modifier.size(16.dp)
    )
}

/** Iki parcadan yalniz DOLU olanlari birlestirir; hicbiri yoksa `null`. */
private fun bolumAltYazisi(vararg parcalar: String?): String? =
    parcalar.filterNotNull().joinToString(" · ").ifBlank { null }
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

/**
 * Hero icindeki tutar. Etiket §24.6 geregi %85 beyaz opaklikta.
 *
 * Tutar SAYARAK yukseliyor (§24 animasyon 1). Ham metin degil ham SAYI
 * aliyor; bicimleme sayacin her karesinde yeniden yapiliyor, yoksa
 * animasyon boyunca "₺0" yazip sonunda dogru degere ziplardi.
 */
@Composable
private fun HeroTutar(
    etiket: String,
    deger: Double,
    bicimle: (Double) -> String,
    modifier: Modifier = Modifier
) {
    val anlik = rememberLkSayac(deger)
    Column(modifier) {
        Text(etiket, style = LkTypography.getMetadata(), color = LkHero.OnHeroSecondary)
        Spacer(Modifier.height(LkSpacing.Space1))
        Text(
            bicimle(anlik),
            style = LkTypography.getTitleS().numeric(),
            color = LkHero.OnHero,
            maxLines = 1
        )
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
