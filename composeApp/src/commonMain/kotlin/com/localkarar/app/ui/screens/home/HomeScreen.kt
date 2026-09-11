package com.localkarar.app.ui.screens.home

import com.localkarar.app.ui.components.LkCard
import com.localkarar.app.ui.components.LkIconTile
import com.localkarar.app.ui.components.LkRowGroup
import com.localkarar.app.ui.components.LkListRow
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.localkarar.app.ui.components.LkMetric
import androidx.compose.foundation.layout.offset
import com.localkarar.app.core.LkFormatting
import com.localkarar.app.ui.components.LkHeroBlock
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localkarar.app.home.HomeUiState
import com.localkarar.app.home.HomeViewModel
import com.localkarar.app.network.dto.BusinessRecordDto
import com.localkarar.app.network.dto.DecisionHistorySessionDto
import com.localkarar.app.network.dto.TrackerSummaryDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkLoadingDesen
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.components.LkSection
import com.localkarar.app.ui.components.LkHairline
import com.localkarar.app.ui.components.LkPulseBadge
import com.localkarar.app.ui.components.LkSectionHeader
import com.localkarar.app.ui.components.LkTactileAction
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.plus
import kotlinx.datetime.DateTimeUnit
import com.localkarar.app.ui.theme.*

/*
 * 🔴 YEREL BICIMLEYICI HEM KIRPIYOR HEM AYRAC KOYMUYORDU.
 *
 * Eski govde: `"₺${amount.toInt()}"`. Iki ayri hata:
 *   1. `toInt()` KIRPAR, yuvarlamaz — ₺182.450,67 → ₺182450
 *   2. binlik ayraci yok — yedi haneli tutarlar okunamiyordu
 *
 * `LkFormatting.formatMoney` ikisini de dogru yapiyor ve uygulamanin geri
 * kalani zaten onu kullaniyor. Bu kopya yalniz Ana Sayfa'da, 7 yerde
 * cagriliyordu; imza korunarak cekirdege yonlendirildi.
 */
fun formatMoney(amount: Double?): String =
    if (amount == null) "₺0" else LkFormatting.formatMoney(amount)

fun shortDate(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return ""
    return dateStr.take(10) // Fallback for simple display
}

fun priorityLevel(raw: String?): String {
    val v = raw?.lowercase() ?: ""
    if (v in listOf("high", "urgent", "critical", "yüksek", "yuksek")) return "high"
    if (v in listOf("low", "düşük", "dusuk")) return "low"
    return "medium"
}

val PRIORITY_LABEL = mapOf("low" to "Düşük", "medium" to "Orta", "high" to "Yüksek")

val RECORD_TYPE_LABEL = mapOf(
    "payment" to "Ödeme", "receivable" to "Tahsilat", "promissory_note" to "Senet",
    "cheque" to "Çek",
    "purchase" to "Satın alma", "shipment" to "Sevkiyat", "task" to "Görev",
    "deferred" to "Ertelenen", "other" to "Kayıt"
)

/** Cihazin YEREL saatine gore selamlama. Sunucu saati degil -- selamlama
 *  kullaniciya ait bir sey, sunucunun bulundugu dilime bagli olmamali. */
fun gunSelamlamasi(): String {
    val saat = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    return when (saat) {
        in 5..11 -> "Günaydın"
        in 12..17 -> "İyi günler"
        else -> "İyi akşamlar"
    }
}

/** Selamlama icin ilk ad. Bos isim gelirse selamlama tek basina kalmasin
 *  diye genel bir hitap dondurulur. */
fun ilkAd(tamAd: String?): String {
    val ad = tamAd?.trim()?.split(" ")?.firstOrNull()?.takeIf { it.isNotBlank() }
    return ad ?: "hoş geldiniz"
}

/**
 * Ana Sayfa hizli islem dosemeleri.
 *
 * Tur/yon ciftleri webdeki `frontend/src/pages/Workspaces/Tracker.jsx`
 * QUICK_ACTIONS listesinin AYNISI -- ayni dort kayit, ayni sira, ayni yon.
 * Sapmasin diye burada tek yerde duruyor.
 */
data class HizliIslem(val tur: String, val yon: String, val etiket: String, val ikon: ImageVector)

val HIZLI_ISLEMLER = listOf(
    HizliIslem("payment", "payable", "Yeni Ödeme", Icons.Outlined.Receipt),
    HizliIslem("receivable", "receivable", "Yeni Tahsilat", Icons.Outlined.Payments),
    HizliIslem("promissory_note", "payable", "Yeni Senet", Icons.Outlined.Description),
    HizliIslem("shipment", "neutral", "Yeni Sevkiyat", Icons.Outlined.LocalShipping)
)

/*
 * KISAYOLLAR — ikinci satir.
 *
 * Urun sahibi istedi (11.09.2026): dort doseme yalniz "kayit ac"
 * eylemiydi; siparise, urune, bildirime ve karar araclarina ana
 * sayfadan ulasmak icin once Isletme Takibi'ne, sonra bolum seciciye
 * girmek gerekiyordu -- iki dokunus fazla.
 *
 * ⚠️ AYRI SATIR, AYNI IZGARA. Ustteki dort "Yeni ..." bir sey
 * OLUSTURUR, bunlar bir yere GIDER. Etiketler bunu soyluyor ("Yeni"
 * oneki yalniz ustte); tek satira karistirilsa kullanici hangi
 * dosemenin form acacagini kestiremezdi.
 */
data class Kisayol(val kod: String, val etiket: String, val ikon: ImageVector)

val KISAYOLLAR = listOf(
    Kisayol("orders", "Siparişler", Icons.Outlined.ShoppingBag),
    Kisayol("products", "Ürünler", Icons.Outlined.Inventory2),
    Kisayol("notifications", "Bildirimler", Icons.Outlined.Notifications),
    Kisayol("decisions", "Karar Araçları", Icons.Outlined.Gavel)
)

/**
 * Kaydin tarihine gore gun basligi.
 *
 * Prototipteki BUGÜN / YARIN gruplamasi. Tarihi olmayan ya da ikisinden de
 * uzak kayitlar "SONRAKİ" altinda toplanir; sessizce BUGÜN grubuna dusup
 * yanlis aciliyet hissi vermesin diye ayri tutuluyor.
 */
fun gunGrubu(tarihMetni: String?): String {
    val gun = tarihMetni?.take(10) ?: return "SONRAKİ"
    val bugun = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return when (gun) {
        bugun.toString() -> "BUGÜN"
        bugun.plus(1, DateTimeUnit.DAY).toString() -> "YARIN"
        else -> "SONRAKİ"
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel, 
    onNavigateToMentor: () -> Unit,
    onNavigateToDecisions: () -> Unit,
    onNavigateToDecisionDetail: (String) -> Unit,
    onNavigateToWorkspaces: () -> Unit,
    onNavigateToTracker: (String) -> Unit,
    /** Hizli Islem dosemesi: (isletmeId, kayitTuru, yon) */
    onQuickAction: (String, String, String) -> Unit,
    /** Kisayol dosemesi: (isletmeId, bolumKodu) -- KISAYOLLAR.kod */
    onKisayol: (String, String) -> Unit = { _, _ -> },
    onOpenProductCenter: () -> Unit = {},
    onOpenSearch: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.loadDashboard(isRefresh = true) }
    )

    LkPageLayout {
        Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    LkLoadingState(desen = LkLoadingDesen.DETAY)
                }
                is HomeUiState.Error -> {
                    LkErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadDashboard() }
                    )
                }
                is HomeUiState.Content -> {
                    DashboardContent(
                        state = state,
                        onNavigateToMentor = onNavigateToMentor,
                        onNavigateToDecisions = onNavigateToDecisions,
                        onNavigateToDecisionDetail = onNavigateToDecisionDetail,
                        onNavigateToWorkspaces = onNavigateToWorkspaces,
                        onNavigateToTracker = onNavigateToTracker,
                        onQuickAction = onQuickAction,
                        onKisayol = onKisayol,
                        onOpenProductCenter = onOpenProductCenter,
                        onOpenSearch = onOpenSearch
                    )
                }
            }
            
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun DashboardContent(
    state: HomeUiState.Content,
    onNavigateToMentor: () -> Unit,
    onNavigateToDecisions: () -> Unit,
    onNavigateToDecisionDetail: (String) -> Unit,
    onNavigateToWorkspaces: () -> Unit,
    onNavigateToTracker: (String) -> Unit,
    /** Hizli Islem dosemesi: (isletmeId, kayitTuru, yon) */
    onQuickAction: (String, String, String) -> Unit,
    onKisayol: (String, String) -> Unit,
    onOpenProductCenter: () -> Unit,
    onOpenSearch: () -> Unit
) {
    val scrollState = rememberScrollState()

    /*
     * §24.6 — HERO BASLIK BLOGU + BINEN YUZEY.
     *
     * Hero TAM GENISLIK; yatay dolgu hero'nun ICINDE, disinda degil. Disarida
     * olsaydi marka blogu kenarlara degmezdi ve desen bozulurdu.
     *
     * Kaydirma binen yuzeyde; hero sabit. Selamlama ve 30 gunluk net yukari
     * kaymiyor — finans uygulamalarinda hakim rakamin ekranda kalmasi
     * beklenen davranis.
     */
    Column(Modifier.fillMaxSize()) {

        LkHeroBlock {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = LkSpacing.Space5,
                        end = LkSpacing.Space5,
                        top = LkSpacing.Space5
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = gunSelamlamasi(),
                        style = LkTypography.getLabelM(),
                        // §24.6: hero icinde metin %85 beyaz opakligin ALTINA inmez.
                        color = LkHero.OnHeroSecondary
                    )
                    Text(
                        text = ilkAd(state.dashboardData.user.name),
                        style = LkTypography.getTitleL(),
                        color = LkHero.OnHero
                    )
                }
                /*
                 * GENEL ARAMA — webin ust cubuktaki arama kutusunun
                 * mobildeki girisi.
                 *
                 * ⚠️ Kutu DEGIL simge: hero'da bir metin alani hem
                 * selamlamayi asagi iter hem de klavyeyi Ana Sayfa'ya
                 * baglar. Simge tam ekran arama sayfasini aciyor.
                 */
                IconButton(onClick = onOpenSearch) {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = "Ara",
                        tint = LkHero.OnHero
                    )
                }
                IconButton(onClick = onOpenProductCenter) {
                    Icon(
                        Icons.Outlined.GridView,
                        contentDescription = "Tüm Modüller",
                        tint = LkHero.OnHero
                    )
                }
            }
            Spacer(Modifier.height(LkSpacing.Space6))
        }

        /*
         * `weight(1f)` — `fillMaxSize()` DEGIL. Column icinde `fillMaxSize()`
         * KALAN degil TUM yuksekligi ister; onceki turda 50 ekranin son satiri
         * bu yuzden dock altinda kaliyordu.
         */
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .offset(y = (-22).dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(LkSurfaceCanvas)
                .verticalScroll(scrollState)
                .padding(horizontal = LkSpacing.Space5),
            verticalArrangement = Arrangement.spacedBy(LkSpacing.Space6)
        ) {
        Spacer(Modifier.height(LkSpacing.Space5))

        // Business Pulse
        BusinessPulseCard(
            tracker = state.trackerSummary,
            onNavigateToWorkspaces = onNavigateToWorkspaces
        )

        // Hizli Islemler + Mentor seridi
        QuickActionsCard(
            activeWorkspaceId = state.activeWorkspaceId,
            onQuickAction = onQuickAction,
            onKisayol = onKisayol,
            onNavigateToWorkspaces = onNavigateToWorkspaces
        )

        // Mobil föy: özet → hızlı işlemler → sıradaki işler.
        TasksPanel(
            records = state.trackerRecords,
            upcomingTasks = state.dashboardData.upcomingTasks,
            activeWorkspaceId = state.activeWorkspaceId,
            onNavigateToTracker = onNavigateToTracker
        )

        MentorSupport(onNavigateToMentor)

        // DecisionsPanel (Son kararlar)
        DecisionsPanel(
            decisionHistory = state.decisionHistory,
            onNavigateToDecisions = onNavigateToDecisions,
            onNavigateToDecisionDetail = onNavigateToDecisionDetail
        )

        // Dock Scaffold'un bottomBar'inda; alt dolguyu Scaffold hesapliyor.
        // Burada yalniz son bolumun dock'a yapismamasi icin nefes payi var.
        Spacer(modifier = Modifier.height(LkSpacing.Space6))
        }   // binen yuzey
    }       // hero + yuzey
}

/**
 * Business Pulse — prototipteki ilk blok.
 *
 * Uc metrik SUNUCUDAN gelir (`trackerSummary.nextThirtyDays`); prototipteki
 * 42.000 gibi rakamlar taslak doldurmasiydi, kullanilmiyor.
 *
 * "Son 30 Gün" rozeti canli bir baglanti degil, donem etiketi -- nokta veri
 * tazeligini degil kapsanan araligi anlatir.
 */
@Composable
private fun BusinessPulseCard(tracker: TrackerSummaryDto?, onNavigateToWorkspaces: () -> Unit) {
    val gecikmis = tracker?.counts?.overdue ?: 0
    val net = tracker?.nextThirtyDays?.net ?: 0.0

    if (tracker == null) {
        LkSection(title = "Business Pulse") {
            Text(
                "İşletme görünümünüzü kurarak başlayın.",
                style = LkTypography.getBodyStrong(),
                color = LkTextPrimary
            )
            Text(
                "Gerçek metrikler için işletme profilinizi ve takip kayıtlarınızı oluşturun.",
                style = LkTypography.getBodySmall(),
                color = LkTextSecondary
            )
            LkButton(
                text = "İşletme oluştur",
                variant = LkButtonVariant.PRIMARY,
                onClick = onNavigateToWorkspaces
            )
        }
        return
    }

    val ozet = buildString {
        if (net < 0) append("Önümüzdeki 30 günde ${formatMoney(kotlin.math.abs(net))} nakit açığın görünüyor")
        else append("Önümüzdeki 30 günde ${formatMoney(net)} net nakit girişin görünüyor")
        if (gecikmis > 0) append(", $gecikmis kayıt gecikmiş durumda.") else append(", geciken kaydın yok.")
    }

    /*
     * §24 OZET KARTI — koyu zemin, 24dp dolgu, kodla cizilmis lekeler.
     *
     * Ekranin TEK hakim rakami 30 gunluk net; tahsilat ve odeme ikincil.
     * Onceki surumde ucu de esit agirliktaydi ve hicbiri one cikmiyordu.
     *
     * Lekeler kodla ciziliyor — gorsel dosyasi yok, iki temada da calisiyor.
     * Web'de `SahneDeseni` icin kullanilan teknigin aynisi.
     */
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .lkElevation(LkElevation.MD, LkShapes.Card)
            .clip(LkShapes.Card)
            .background(
                Brush.linearGradient(
                    listOf(LkBrand.B700, Color(0xFF16333F))
                )
            )
            .clickable { onNavigateToWorkspaces() }
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(
                color = Color(0x6B55879D),
                radius = size.minDimension * 0.42f,
                center = Offset(size.width * 1.02f, -size.height * 0.18f)
            )
            drawCircle(
                color = Color(0x3D7BA2B3),
                radius = size.minDimension * 0.26f,
                center = Offset(size.width * 0.86f, size.height * 1.18f)
            )
            drawCircle(
                color = Color(0x0FFFFFFF),
                radius = size.minDimension * 0.15f,
                center = Offset(-size.width * 0.04f, size.height * 0.82f)
            )
        }

        Column(Modifier.padding(LkSpacing.PadCard)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "30 GÜNLÜK NET",
                    style = LkTypography.getMetadata(),
                    color = LkHero.OnHeroSecondary
                )
                LkPulseBadge("Son 30 Gün", koyuZemin = true)
            }
            Spacer(Modifier.height(LkSpacing.Space2))

            /* Sayarak yukselen hakim rakam; tabular figurlerle ciziliyor. */
            LkMetric(
                hedef = net,
                bicimle = { v -> (if (v > 0) "+" else "") + formatMoney(v) },
                renk = LkHero.OnHero
            )

            Spacer(Modifier.height(LkSpacing.Space5))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0x29FFFFFF))
            )
            Spacer(Modifier.height(LkSpacing.Space4))

            Row(Modifier.fillMaxWidth()) {
                PulseMetric(
                    "TAHSİLAT",
                    formatMoney(tracker.nextThirtyDays?.receivable),
                    LkHero.OnHero,
                    Modifier.weight(1f)
                )
                Box(
                    Modifier
                        .width(1.dp)
                        .height(38.dp)
                        .background(Color(0x29FFFFFF))
                )
                PulseMetric(
                    "ÖDEME",
                    formatMoney(tracker.nextThirtyDays?.payable),
                    LkHero.OnHero,
                    Modifier.weight(1f).padding(start = LkSpacing.Space4)
                )
            }
        }
    }

    /*
     * Net'in isareti ARTIK RENKLE DEGIL, ozet cumlesiyle anlatiliyor.
     * Koyu kart uzerinde yesil/kirmizi §19 esigini gecmiyordu; ayrica
     * durumun tek basina renge yaslanmamasi §24 kurali.
     */
    Text(ozet, style = LkTypography.getBodySmall(), color = LkTextSecondary)

    /* `LkSection` baslik ZORUNLU istiyor; burada baslik yok — duz Column. */
    Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)) {

        // Yonu belirsiz kayitlar: tutari var ama hicbir toplama girmiyorlar.
        // Webde de Ana Sayfa'da gosteriliyor (`Dashboard.jsx:345`). Bu satir
        // olmadan o kayitlar ekranin HICBIR yerinde gorunmuyordu.
        val bekleyen = tracker.awaitingDirection
        if (bekleyen != null && bekleyen.count > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToWorkspaces() }
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Yön bekliyor · ${formatMoney(bekleyen.amount)}",
                        style = LkTypography.getBodyStrong(),
                        color = LkTextPrimary
                    )
                    Text(
                        "${bekleyen.count} kayıt · borç mu alacak mı belirsiz",
                        style = LkTypography.getMetadata(),
                        color = LkTextMuted
                    )
                }
                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = LkTextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun PulseMetric(etiket: String, deger: String, renk: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        /*
         * 🔴 OKUNMUYORDU. `LkTextMuted` acik temanin gri metni; koyu hero
         * kartinin ustunde "TAHSİLAT" ve "ÖDEME" neredeyse kayboluyordu
         * (urun sahibi ekran goruntusuyle bildirdi, 11.09.2026). Kart
         * her temada koyu; etiket de hero tokenini kullanmali.
         */
        Text(etiket, style = LkTypography.getBodySmall(), color = LkHero.OnHeroSecondary)
        // Tabular rakam: prototipte `font-feature-settings: "tnum"`. Uc metrik
        // alt alta hizali dursun diye; orantili rakamla sutunlar kayiyordu.
        Text(
            text = deger,
            style = LkTypography.getMetric().copy(fontFeatureSettings = "tnum"),
            color = renk,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Siradaki Isler — prototipteki `.section-block` + `.task-row`.
 *
 * Kart DEGIL: satirlari sac teli cizgi ayirir (`.task-row` alt kenarlik),
 * SON satirdan sonra cizgi yok (`.task-row:last-child`). Ilk uyarlamada
 * liste bir cerceve icindeydi; prototipte cerceve yok.
 */
@Composable
private fun TasksPanel(
    records: List<BusinessRecordDto>,
    upcomingTasks: List<com.localkarar.app.network.dto.UpcomingTaskDto>?,
    activeWorkspaceId: String?,
    onNavigateToTracker: (String) -> Unit
) {
    val gecerliKayitlar = records.filter { it.status != "completed" && it.status != "cancelled" }.take(3)
    val satirlar = if (gecerliKayitlar.isNotEmpty()) {
        gecerliKayitlar.map { r ->
            GorevSatiri(
                baslik = r.title,
                tamam = r.status == "completed",
                oncelik = priorityLevel(r.priority),
                tarih = shortDate(r.dueAt),
                tur = RECORD_TYPE_LABEL[r.type] ?: "Kayıt",
                yon = r.direction
            )
        }
    } else {
        (upcomingTasks ?: emptyList()).take(3).map { t ->
            GorevSatiri(
                baslik = t.title,
                tamam = t.status == "completed",
                oncelik = null,
                tarih = shortDate(t.updatedAt ?: t.createdAt),
                tur = "Öğrenme",
                yon = null
            )
        }
    }

    LkSection(
        title = "Sıradaki İşler",
        actionLabel = if (activeWorkspaceId != null) "Tümünü gör ›" else null,
        onAction = if (activeWorkspaceId != null) ({ onNavigateToTracker(activeWorkspaceId) }) else null
    ) {
        if (satirlar.isEmpty()) {
            Text("Şu an sırada bir iş yok.", style = LkTypography.getBodySmall(), color = LkTextSecondary)
            return@LkSection
        }

        // BUGÜN / YARIN gruplamasi. Grup basligi YALNIZ o grupta kayit varsa
        // cizilir; bos baslik listeyi oldugundan dolu gosterirdi.
        val gruplu = satirlar.groupBy { gunGrubu(it.tarih) }
        val gruplar = listOf("BUGÜN", "YARIN", "SONRAKİ").filter { gruplu[it]?.isNotEmpty() == true }

        gruplar.forEachIndexed { grupSira, grup ->
            Text(
                text = grup,
                style = LkTypography.getMetadata(),
                color = LkTextMuted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(top = if (grupSira == 0) 0.dp else LkSpacing.Space3)
            )

            /*
             * §24 — grubun satirlari TEK YUKSELTILMIS YUZEYDE toplaniyor.
             * Onceden zeminin uzerinde serbest duruyorlardi ve liste bir
             * nesne gibi degil, dagilmis metin gibi okunuyordu.
             */
            LkRowGroup {
                val grupSatirlari = gruplu[grup].orEmpty()
                grupSatirlari.forEachIndexed { i, satir ->
                    GorevSatiriGorunumu(
                        satir = satir,
                        onClick = { if (activeWorkspaceId != null) onNavigateToTracker(activeWorkspaceId) }
                    )
                    if (i != grupSatirlari.lastIndex) LkHairline()
                }
            }
        }
    }
}

private data class GorevSatiri(
    val baslik: String,
    val tamam: Boolean,
    val oncelik: String?,
    val tarih: String,
    val tur: String,
    /** "receivable" / "payable" / null (ogrenme gorevi). */
    val yon: String?
)

/*
 * §24 SATIR ANATOMISI — ikon kutucugu | baslik + tarih | kategori | oncelik.
 *
 * 🔴 ONCEDEN DUZ BIR SATIRDI: bos daire + iki satir metin. Referans
 * kitlerdeki okunur satir yapisi (ikon kutucugu ve DIKEY AYRACLI kategori
 * sutunu) hicbir yerde kullanilmiyordu; `LkListRow` yazilmis ama tek bir
 * ekrana bile baglanmamisti.
 *
 * Tamamlanan gorev ikon kutucugunun RENGIYLE degil, ikonun kendisiyle de
 * ayirt ediliyor (tik / bos daire) — durum tek basina renge yaslanmaz.
 */
@Composable
private fun GorevSatiriGorunumu(satir: GorevSatiri, onClick: () -> Unit) {
    val oncelikRengi = when (satir.oncelik) {
        "high" -> LkDanger
        "low" -> LkSuccess
        else -> LkWarning
    }
    LkListRow(
        baslik = satir.baslik,
        altBaslik = satir.tarih.takeIf { it.isNotBlank() },
        kategori = satir.tur.takeIf { it.isNotBlank() },
        onClick = onClick,
        ikon = {
            /*
             * Kayitlar ekraniyla AYNI ikon sozlugu: para giren asagi-sol,
             * cikan yukari-sag. Bos daire kaldirildi -- liste zaten
             * tamamlanmamis kayitlari suzuyor, tik dali hic calismiyordu.
             */
            Icon(
                when {
                    satir.tamam -> Icons.Outlined.CheckCircle
                    satir.yon == "receivable" -> Icons.Outlined.SouthWest
                    satir.yon == "payable" -> Icons.Outlined.NorthEast
                    satir.yon == null -> Icons.Outlined.School
                    else -> Icons.Outlined.ReceiptLong
                },
                contentDescription = null,
                tint = if (satir.tamam) LkSuccess else LkTileInk,
                modifier = Modifier.size(21.dp)
            )
        },
        sag = {
            if (satir.oncelik != null) {
                Text(
                    PRIORITY_LABEL[satir.oncelik] ?: "Orta",
                    style = LkTypography.getMetadata(),
                    color = oncelikRengi,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = LkSpacing.Space3)
                )
            }
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = LkTextMuted,
                modifier = Modifier.padding(start = LkSpacing.Space2).size(16.dp)
            )
        }
    )
}

/**
 * Hizli Islemler + Mentor seridi.
 *
 * Dort doseme webdeki `Tracker.jsx` QUICK_ACTIONS ile AYNI tur/yon ciftlerini
 * kullanir, boylece form dogru turde acilir. Mentor seridi prototipte ustten
 * sac teli cizgiyle ayrilir (`.mentor-inline { border-top }`), kart icinde
 * degil.
 */
@Composable
private fun QuickActionsCard(
    activeWorkspaceId: String?,
    onQuickAction: (String, String, String) -> Unit,
    onKisayol: (String, String) -> Unit,
    onNavigateToWorkspaces: () -> Unit
) {
    LkSection(title = "Hızlı İşlemler") {
        if (activeWorkspaceId == null) {
            // Kayit acilacak bir isletme yokken doseme gostermek olu dugme
            // demekti; yerine kurulum cagrisi.
            Text(
                "Kayıt açabilmek için önce bir işletme oluşturun.",
                style = LkTypography.getBodySmall(),
                color = LkTextSecondary
            )
            LkButton(
                text = "İşletme oluştur",
                variant = LkButtonVariant.SECONDARY,
                onClick = onNavigateToWorkspaces
            )
        } else {
            // Prototipte `.actions-grid { grid-template-columns: repeat(4,1fr) }`.
            // SpaceBetween ile dosemeler kendi metin genisliklerinde kaliyordu
            // ve "Yeni Tahsilat" gibi uzun etiketler kirpiliyordu; esit
            // sutun genisligi bunu cozer.
            /*
             * §24 IKON DOSEMESI IZGARASI.
             *
             * 🔴 ONCEDEN `LkTactileAction` KULLANILIYORDU ve kutucuk zemini
             * `LkPrimarySoft` idi: koyu temada zemine karisip kutucuk gibi
             * durmuyordu (olculdu, oran 1.138 < 1.20). Artik `LkIconTile`
             * ve olculmus `LkSurfaceTile`.
             */
            /*
             * 🔴 "YENI ODEME / TAHSILAT / SENET / SEVKIYAT" SATIRI KALDIRILDI
             * (urun sahibi, 11.09.2026). Kayit acmak Isletme Takibi'nin
             * isi; ana sayfadan en cok gidilen yerler siparis, urun,
             * bildirim ve karar araclari. Tur/yon onayarlari
             * `HIZLI_ISLEMLER` icinde duruyor -- WorkspaceHomeScreen
             * hizli islemleri hala oradan okuyor.
             */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
            ) {
                KISAYOLLAR.forEach { kisayol ->
                    LkIconTile(
                        etiket = kisayol.etiket,
                        onClick = { onKisayol(activeWorkspaceId, kisayol.kod) },
                        modifier = Modifier.weight(1f),
                        ikon = {
                            Icon(
                                kisayol.ikon,
                                contentDescription = null,
                                tint = LkTileInk,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    )
                }
            }
        }

    }
}

@Composable
private fun MentorSupport(onNavigateToMentor: () -> Unit) {
    LkSection(title = "Karar desteği") {
        LkCard(padding = LkSpacing.Space4) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Kararsız kaldığında AI Mentor",
                        style = LkTypography.getBodyStrong(),
                        color = LkTextPrimary
                    )
                    Text(
                        "Sorularını AI Mentor ile değerlendir.",
                        style = LkTypography.getMetadata(),
                        color = LkTextMuted
                    )
                }
                Spacer(modifier = Modifier.width(LkSpacing.Space3))
                LkButton(text = "Danış", variant = LkButtonVariant.SECONDARY, onClick = onNavigateToMentor)
            }
        }
    }
}

/**
 * Son kararlar.
 *
 * Prototipteki Ana Sayfa'da bu blok YOK -- gercek veriye dayandigi ve
 * kullanicinin karar gecmisine tek dokunusluk donus verdigi icin korundu.
 * Ama ayni acik bolum diline cekildi: kutu icinde birakilinca sayfadaki tek
 * cerceveli blok olarak sirtiyordu.
 */
@Composable
private fun DecisionsPanel(
    decisionHistory: List<DecisionHistorySessionDto>?,
    onNavigateToDecisions: () -> Unit,
    onNavigateToDecisionDetail: (String) -> Unit
) {
    val kararlar = decisionHistory
        ?.filter { it.status == "completed" && it.completedAt != null }
        ?.sortedByDescending { it.completedAt }
        ?.take(4)
        .orEmpty()

    LkSection(
        title = "Son kararlar",
        actionLabel = if (kararlar.isNotEmpty()) "Tümünü gör ›" else null,
        onAction = if (kararlar.isNotEmpty()) onNavigateToDecisions else null
    ) {
        if (kararlar.isEmpty()) {
            Text(
                "Henüz tamamlanmış bir karar yok.",
                style = LkTypography.getBodySmall(),
                color = LkTextSecondary
            )
            return@LkSection
        }

        /* §24 — kararlar da tek yukseltilmis yuzeyde, ayni satir anatomisiyle. */
        LkRowGroup {
            kararlar.forEachIndexed { sira, oturum ->
                LkListRow(
                    baslik = oturum.decisionCheckTitle,
                    altBaslik = shortDate(oturum.completedAt),
                    onClick = { onNavigateToDecisionDetail(oturum.id) },
                    ikon = {
                        Icon(
                            Icons.Outlined.AccountBalance,
                            contentDescription = null,
                            tint = LkTileInk,
                            modifier = Modifier.size(21.dp)
                        )
                    },
                    sag = {
                        Icon(
                            Icons.Outlined.ChevronRight,
                            contentDescription = null,
                            tint = LkTextMuted,
                            modifier = Modifier.padding(start = LkSpacing.Space2).size(16.dp)
                        )
                    }
                )
                if (sira != kararlar.lastIndex) LkHairline()
            }
        }
    }
}
