package com.localkarar.app.ui.screens.home

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

fun formatMoney(amount: Double?): String {
    if (amount == null) return "₺0"
    return "₺${amount.toInt()}" // Simplified formatter for parity matching
}

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
    onOpenProductCenter: () -> Unit = {}
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
                    LkLoadingState()
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
                        onOpenProductCenter = onOpenProductCenter
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
    onOpenProductCenter: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = LkSpacing.Space6),
        verticalArrangement = Arrangement.spacedBy(LkSpacing.Space6)
        // Prototipte `.page-view { gap: 24px }` — bolumleri BOSLUK ayirir.
    ) {
        // Baslik — prototipteki selamlama.
        //
        // Uc dugmeli [Hesapla | Mentor | Karar Ver] satiri KALDIRILDI (urun
        // sahibi karari, 04.09.2026). Hesaplamalar zaten alt dockta; Mentor
        // asagidaki seritte; Karar Araclari, Kurslar ve Haberler ise sagdaki
        // izgara dugmesinin actigi Urun Merkezinde. Yani erisim kaybi yok.
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = LkSpacing.Space6),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${gunSelamlamasi()}, ${ilkAd(state.dashboardData.user.name)}",
                    style = LkTypography.getPageTitle(),
                    color = LkTextPrimary
                )
                Text(
                    text = "Bugün işletmenizde ne önemli?",
                    style = LkTypography.getBody(),
                    color = LkTextSecondary
                )
            }
            IconButton(onClick = onOpenProductCenter) {
                Icon(Icons.Outlined.GridView, contentDescription = "Tüm Modüller", tint = LkPrimary)
            }
        }

        // Business Pulse
        BusinessPulseCard(
            tracker = state.trackerSummary,
            onNavigateToWorkspaces = onNavigateToWorkspaces
        )

        // TasksPanel (Sıradaki işler)
        TasksPanel(
            records = state.trackerRecords,
            upcomingTasks = state.dashboardData.upcomingTasks,
            activeWorkspaceId = state.activeWorkspaceId,
            onNavigateToTracker = onNavigateToTracker
        )

        // Hizli Islemler + Mentor seridi
        QuickActionsCard(
            activeWorkspaceId = state.activeWorkspaceId,
            onQuickAction = onQuickAction,
            onNavigateToWorkspaces = onNavigateToWorkspaces,
            onNavigateToMentor = onNavigateToMentor
        )

        // DecisionsPanel (Son kararlar)
        DecisionsPanel(
            decisionHistory = state.decisionHistory,
            onNavigateToDecisions = onNavigateToDecisions,
            onNavigateToDecisionDetail = onNavigateToDecisionDetail
        )

        // Dock Scaffold'un bottomBar'inda; alt dolguyu Scaffold hesapliyor.
        // Burada yalniz son bolumun dock'a yapismamasi icin nefes payi var.
        Spacer(modifier = Modifier.height(LkSpacing.Space6))
    }
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

    LkSection(
        title = "Business Pulse",
        trailing = { LkPulseBadge("Son 30 Gün") }
    ) {
        Text(ozet, style = LkTypography.getBodySmall(), color = LkTextSecondary)
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onNavigateToWorkspaces() },
            horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
        ) {
            PulseMetric("Tahsilat", formatMoney(tracker.nextThirtyDays?.receivable), LkTextPrimary, Modifier.weight(1f))
            PulseMetric("Ödeme", formatMoney(tracker.nextThirtyDays?.payable), LkTextPrimary, Modifier.weight(1f))
            PulseMetric(
                "Net",
                (if (net > 0) "+" else "") + formatMoney(net),
                // Sifir olumlu DEGIL: net 0 iken yesil "kazanctasin" izlenimi
                // verirdi. Uc durum ayri.
                when {
                    net < 0 -> LkDanger
                    net > 0 -> LkSuccess
                    else -> LkTextPrimary
                },
                Modifier.weight(1f)
            )
        }

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
        Text(etiket, style = LkTypography.getBodySmall(), color = LkTextMuted)
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
                tur = RECORD_TYPE_LABEL[r.type] ?: "Kayıt"
            )
        }
    } else {
        (upcomingTasks ?: emptyList()).take(3).map { t ->
            GorevSatiri(
                baslik = t.title,
                tamam = t.status == "completed",
                oncelik = null,
                tarih = shortDate(t.updatedAt ?: t.createdAt),
                tur = "Öğrenme"
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
                style = LkTypography.getMicro(),
                color = LkTextMuted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(top = if (grupSira == 0) 0.dp else LkSpacing.Space3)
            )

            val grupSatirlari = gruplu[grup].orEmpty()
            grupSatirlari.forEachIndexed { i, satir ->
                GorevSatiriGorunumu(
                    satir = satir,
                    onClick = { if (activeWorkspaceId != null) onNavigateToTracker(activeWorkspaceId) }
                )
                // Son satirdan sonra cizgi yok — prototipteki :last-child.
                val sonSatir = grupSira == gruplar.lastIndex && i == grupSatirlari.lastIndex
                if (!sonSatir) LkHairline()
            }
        }
    }
}

private data class GorevSatiri(
    val baslik: String,
    val tamam: Boolean,
    val oncelik: String?,
    val tarih: String,
    val tur: String
)

@Composable
private fun GorevSatiriGorunumu(satir: GorevSatiri, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = LkSpacing.Space3),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (satir.tamam) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (satir.tamam) LkSuccess else LkTextMuted,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(LkSpacing.Space4))
        Column(modifier = Modifier.weight(1f)) {
            Text(satir.baslik, style = LkTypography.getBodyStrong(), color = LkTextPrimary)
            Text(
                text = listOf(satir.tarih, satir.tur).filter { it.isNotBlank() }.joinToString(" · "),
                style = LkTypography.getMicro(),
                color = LkTextMuted
            )
        }
        if (satir.oncelik != null) {
            val oncelikRengi = when (satir.oncelik) {
                "high" -> LkDanger
                "low" -> LkSuccess
                else -> LkWarning
            }
            Text(
                PRIORITY_LABEL[satir.oncelik] ?: "Orta",
                style = LkTypography.getMicro(),
                color = oncelikRengi,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(LkSpacing.Space2))
        }
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = LkTextMuted, modifier = Modifier.size(16.dp))
    }
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
    onNavigateToWorkspaces: () -> Unit,
    onNavigateToMentor: () -> Unit
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
            ) {
                HIZLI_ISLEMLER.forEach { islem ->
                    LkTactileAction(
                        icon = islem.ikon,
                        label = islem.etiket,
                        onClick = { onQuickAction(activeWorkspaceId, islem.tur, islem.yon) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(LkSpacing.Space2))
        LkHairline()

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = LkSpacing.Space2),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Kararsız kaldığında AI Mentor", style = LkTypography.getBodyStrong(), color = LkTextPrimary)
                Text(
                    "Yalnızca ihtiyaç olduğunda açılır; ekranda sürekli durmaz.",
                    style = LkTypography.getMicro(),
                    color = LkTextMuted
                )
            }
            Spacer(modifier = Modifier.width(LkSpacing.Space3))
            LkButton(text = "Danış", variant = LkButtonVariant.SECONDARY, onClick = onNavigateToMentor)
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

        kararlar.forEachIndexed { sira, oturum ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToDecisionDetail(oturum.id) }
                    .padding(vertical = LkSpacing.Space3),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.AccountBalance,
                    contentDescription = null,
                    tint = LkPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(LkSpacing.Space4))
                Column(modifier = Modifier.weight(1f)) {
                    Text(oturum.decisionCheckTitle, style = LkTypography.getBodyStrong(), color = LkTextPrimary)
                    Text(shortDate(oturum.completedAt), style = LkTypography.getMetadata(), color = LkTextMuted)
                }
                Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = LkTextMuted, modifier = Modifier.size(16.dp))
            }
            // Son satirdan sonra cizgi yok.
            if (sira != kararlar.lastIndex) LkHairline()
        }
    }
}
