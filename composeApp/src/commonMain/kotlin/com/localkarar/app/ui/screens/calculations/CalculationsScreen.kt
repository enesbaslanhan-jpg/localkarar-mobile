package com.localkarar.app.ui.screens.calculations

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.IconButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.outlined.ArrowBack
import com.localkarar.app.ui.components.LkHeroBlock
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.localkarar.app.calculations.CALCULATION_CATEGORIES
import com.localkarar.app.calculations.CalculationItem
import com.localkarar.app.calculations.CalculationsUiState
import com.localkarar.app.calculations.CalculationsViewModel
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.core.displayValue
import com.localkarar.app.navigation.Destination
import com.localkarar.app.navigation.NavController
import com.localkarar.app.network.dto.BusinessRecordDto
import com.localkarar.app.network.dto.FormulaCalculationDto
import com.localkarar.app.network.dto.FormulaDto
import com.localkarar.app.network.dto.TrackerSummaryDto
import com.localkarar.app.ui.components.LkChip
import com.localkarar.app.ui.components.LkEmptyState
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkTabs
import com.localkarar.app.ui.components.LkSectionHeader
import com.localkarar.app.ui.components.LkCard
import com.localkarar.app.ui.components.LkListRow
import com.localkarar.app.ui.components.LkRowGroup
import com.localkarar.app.ui.components.LkHairline
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkPressable
import com.localkarar.app.ui.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/** Ayni pakette PricingWizard da kullaniyor; private olamaz. */
/**
 * Tam lira olarak bicimlendirir.
 *
 * 🔴 ONCEDEN `toLong()` ILE KIRPIYORDU: 1416,67 ekrana 1.416 olarak
 * dusuyordu. Fiyatlandirma aracinda bu sistematik olarak hedef marjin
 * ALTINDA bir fiyat gosterir -- kullanici o fiyati uygularsa her satista
 * hedefledigi kari tutturamaz. Artik en yakin liraya yuvarlaniyor.
 *
 * ⚠️ Kurus yine gosterilmiyor; sunucu iki basamak dondurse de listelerde
 * tam lira daha okunur. Kurus onemli oldugunda (fatura, mutabakat) ayri
 * bir bicimlendirici gerekir.
 */
internal fun formatTry(amount: Double): String {
    val formatted = kotlin.math.round(kotlin.math.abs(amount)).toLong().toString()
        .reversed().chunked(3).joinToString(".").reversed()
    return if (amount < 0) "-₺$formatted" else "₺$formatted"
}

@Composable
fun CalculationsScreen(
    viewModel: CalculationsViewModel,
    onCalculationSelected: (CalculationItem) -> Unit,
    onDetailedSelected: ((CalculationItem) -> Unit)? = null,
    onKayitEkle: () -> Unit,
    onBelgeler: () -> Unit,
    onTakvim: () -> Unit,
    onBack: (() -> Unit)? = null,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val categoryFilter by viewModel.categoryFilter.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    /*
     * WEB ILE AYNI IKI SEKME.
     *
     * 🔴 "Finansal Gorunum" KALDIRILDI. Webde de vardi ve BILEREK silindi
     * (`frontend/src/pages/ToolsPage.jsx:219`): dort blogundan ucu Isletme
     * Takibi'ndeki veriyi oldugu gibi tekrarliyordu -- ayni iki uc
     * (`tracker.summary`, `tracker.list`) uc ayri ekranda cagriliyordu --
     * dorduncusu de "Gecmis" sekmesiyle ayniydi.
     *
     * Mobil bu sekmeyi tasimaya devam ediyordu ve ayni gereksiz iki istegi
     * yapiyordu. Hesaplamalar bir HESAP modulu; tahsilat/odeme defteri
     * Isletme Takibi'ne ait. Buraya tekrar eklenmemeli.
     */
    val tabs = listOf("Katalog", "Geçmiş")

    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) viewModel.refresh()
    }

    /*
     * §24.6 — hero baslik blogu + binen yuzey.
     *
     * Sekmeler binen YUZEYIN icinde, hero'nun degil: sekme secimi icerigi
     * degistiren bir kontrol, baslik degil. Hero'da olsaydi marka blogunun
     * yuksekligi sekme sayisina gore degisirdi.
     */
    Column(modifier = Modifier.fillMaxSize()) {

        LkHeroBlock {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = LkSpacing.Space4,
                        end = LkSpacing.Space4,
                        top = LkSpacing.Space4,
                        bottom = LkSpacing.Space6
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
                    text = "Hesaplamalar",
                    style = LkTypography.getTitleS(),
                    color = LkHero.OnHero,
                    modifier = Modifier.weight(1f).padding(start = LkSpacing.Space2)
                )
            }
        }

        /* `weight(1f)` — Column icinde `fillMaxSize()` TUM yuksekligi ister. */
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .offset(y = (-22).dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(LkSurfaceCanvas)
        ) {
            Spacer(modifier = Modifier.height(LkSpacing.Space5))

            // §11: sekmeler ortak bilesenden. Material TabRow kendi olcu ve
            // renk sistemini getiriyordu (§0 ihlali).
            LkTabs(
                tabs = tabs,
                selectedIndex = selectedTab,
                onSelect = { selectedTab = it },
                modifier = Modifier.padding(horizontal = LkSpacing.Space4)
            )
            Spacer(modifier = Modifier.height(LkSpacing.Space2))

            Box(modifier = Modifier.weight(1f)) {
                when (val state = uiState) {
                    is CalculationsUiState.Loading -> LkLoadingState()
                    is CalculationsUiState.Error -> LkErrorState(
                        message = state.message,
                        onRetry = { viewModel.load() }
                    )
                    is CalculationsUiState.Content -> {
                        when (selectedTab) {
                            0 -> KatalogTab(
                                catalog = state.catalog,
                                categoryFilter = categoryFilter,
                                onCategoryChanged = { viewModel.updateCategoryFilter(it) },
                                onCalculationSelected = onCalculationSelected,
                                onDetailedSelected = onDetailedSelected,
                                onKayitEkle = onKayitEkle,
                                onBelgeler = onBelgeler,
                                onTakvim = onTakvim,
                                onOpenPricingTool = {
                                    navController.navigateTo(Destination.FormulaDetail("fiyat_mimarisi"))
                                }
                            )
                            else -> GecmisTab(
                                history = state.history,
                                catalog = state.catalog,
                                onHistorySelected = { item, calculationItem ->
                                    if (calculationItem.formula != null) {
                                        navController.navigateTo(Destination.FormulaDetail(calculationItem.formula!!.id, item))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formulaResultLabel(key: String): String {
    return FORMULA_RESULT_LABELS[key] ?: key.replace('_', ' ').replaceFirstChar { it.uppercaseChar() }
}

// ─── KATALOG TAB ────────────────────────────────────────────

@Composable
private fun KatalogTab(
    catalog: List<CalculationItem>,
    categoryFilter: String,
    onCategoryChanged: (String) -> Unit,
    onCalculationSelected: (CalculationItem) -> Unit,
    onDetailedSelected: ((CalculationItem) -> Unit)? = null,
    onKayitEkle: () -> Unit,
    onBelgeler: () -> Unit,
    onTakvim: () -> Unit,
    onOpenPricingTool: () -> Unit
) {
    /*
     * KATALOG — onaylanan foy ("Hesap 1" / "Hesap 2").
     *
     * 🔴 18 ARAC TEK DUZ LISTEDEYDI, ustunde yatay kaydirilan bir kategori
     * hap seridi vardi. Serit ekranin ustunu kapliyor ama kategorinin kac
     * arac tasidigini soylemiyordu; kullanici araci ancak listeyi bastan
     * sona kaydirarak buluyordu.
     *
     * Yeni yapi: ARAMA → kategori izgarasi (sayilariyla) → kategoriye
     * girilince o kategorinin arac listesi. Kategoriler
     * `CALCULATION_CATEGORIES` ile birebir ayni; yeni kategori
     * uydurulmadi.
     */
    var arama by remember { mutableStateOf("") }

    val aramaSonucu = remember(catalog, arama) {
        val q = arama.trim()
        if (q.length < 2) emptyList()
        else catalog.filter {
            it.title.contains(q, ignoreCase = true) ||
                it.description.contains(q, ignoreCase = true)
        }
    }

    val kategoriAraclari = remember(catalog, categoryFilter) {
        if (categoryFilter == "all") emptyList()
        else catalog.filter { it.category == categoryFilter }
    }

    /*
     * 🔴 KATEGORIYE GIRINCE EKRAN ONCEKI KAYDIRMA KONUMUNDA KALIYORDU:
     * izgaranin altina inip bir kategoriye dokunan kullanici, arac
     * listesinin ORTASINDA aciyordu ve basligi hic gormuyordu.
     */
    val listeDurumu = rememberLazyListState()
    LaunchedEffect(categoryFilter) { listeDurumu.scrollToItem(0) }

    LazyColumn(
        state = listeDurumu,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = LkSpacing.Space4,
            end = LkSpacing.Space4,
            bottom = LkSpacing.Space8
        ),
        verticalArrangement = Arrangement.spacedBy(LkSpacing.Space5)
    ) {

        item {
            LkTextField(
                value = arama,
                onValueChange = { arama = it },
                placeholder = "Araç ara — “komisyon”, “başabaş”…",
                leadingContent = {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = null,
                        tint = LkTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        /*
         * ARAMA HER SEYIN ONUNE GECIYOR: yazi varken kategori izgarasi da
         * arac listesi de gizleniyor. Iki liste birden gostermek "hangisi
         * benim aradigim" sorusunu doguruyordu.
         */
        if (arama.trim().length >= 2) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                    LkSectionHeader(title = "ARAMA SONUCU")
                    if (aramaSonucu.isEmpty()) {
                        Text(
                            text = "“${arama.trim()}” için araç bulunamadı.",
                            style = LkTypography.getBodySmall(),
                            color = LkTextMuted
                        )
                    } else {
                        AracListesi(
                            araclar = aramaSonucu,
                            onCalculationSelected = onCalculationSelected,
                            onDetailedSelected = onDetailedSelected
                        )
                    }
                }
            }
            return@LazyColumn
        }

        if (categoryFilter == "all") {
            /* Fiyatlandirma sihirbazi — ekranin one cikan araci, kendi
               blogunda kaliyor. */
            item {
                Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                    LkSectionHeader(title = "SIK KULLANILAN")
                    PricingWizardSection(onOpenFullTool = onOpenPricingTool)
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                    LkSectionHeader(title = "KATEGORİLER")
                    /*
                     * Izgara ELLE kuruluyor (`chunked(2)`), `LazyVerticalGrid`
                     * ile degil: ic ice iki kaydirilabilir liste Compose'da
                     * olcum hatasi verir.
                     */
                    val kategoriler = CALCULATION_CATEGORIES.filter { it.key != "all" }
                    kategoriler.chunked(2).forEach { satir ->
                        Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                            satir.forEach { kategori ->
                                val adet = catalog.count { it.category == kategori.key }
                                KategoriKutusu(
                                    baslik = kategori.label,
                                    ikon = kategoriIkonu(kategori.key),
                                    adet = adet,
                                    onClick = { onCategoryChanged(kategori.key) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            /* Tek kalan kategori satiri germesin. */
                            if (satir.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            /* İşletme Takibi kisayollari BU EKRANDAN KALKTI: ayni uc giris
               (kayit, belge, takvim) artik Genel Bakis'in kendi
               gruplarinda ve ait olduklari yer orasi. */
        } else {
            item {
                val kategori = CALCULATION_CATEGORIES.firstOrNull { it.key == categoryFilter }
                Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.ArrowBack,
                            contentDescription = "Tüm kategoriler",
                            tint = LkTextSecondary,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(LkShapes.FULL)
                                .clickable { onCategoryChanged("all") }
                                .padding(9.dp)
                        )
                        Spacer(Modifier.width(LkSpacing.Space2))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = kategori?.label ?: "Araçlar",
                                style = LkTypography.getSectionTitle(),
                                color = LkTextPrimary
                            )
                            Text(
                                text = "${kategoriAraclari.size} araç",
                                style = LkTypography.getMetadata(),
                                color = LkTextSecondary
                            )
                        }
                    }

                    if (kategoriAraclari.isEmpty()) {
                        LkEmptyState(
                            title = "Sonuç bulunamadı",
                            description = "Bu kategoride hesaplama aracı yok."
                        )
                    } else {
                        AracListesi(
                            araclar = kategoriAraclari,
                            onCalculationSelected = onCalculationSelected,
                            onDetailedSelected = onDetailedSelected
                        )
                    }
                }
            }

            item {
                LkButton(
                    text = "Tüm kategoriler",
                    variant = LkButtonVariant.SECONDARY,
                    onClick = { onCategoryChanged("all") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Kategori kutucugu — foydeki izgara.
 *
 * Adet ONEMLI: hangi kategoriye girmeye deger oldugunu soyleyen tek
 * bilgi o. Hap seridinde bu bilgi yoktu.
 */
/**
 * Kategori ikonlari.
 *
 * Hepsi ayni hesap makinesi ikonuyla cizildiginde izgara alti ayni
 * kutucuk gibi okunuyordu; ikon ayirt etmiyorsa yer kaplamaktan baska
 * is gormez.
 */
private fun kategoriIkonu(anahtar: String): androidx.compose.ui.graphics.vector.ImageVector = when (anahtar) {
    "cash" -> Icons.Outlined.AccountBalanceWallet
    "profitability" -> Icons.Outlined.TrendingUp
    "customer" -> Icons.Outlined.People
    "operations" -> Icons.Outlined.Inventory2
    "growth" -> Icons.Outlined.Rocket
    "valuation" -> Icons.Outlined.Insights
    else -> Icons.Outlined.Calculate
}

@Composable
private fun KategoriKutusu(
    baslik: String,
    ikon: androidx.compose.ui.graphics.vector.ImageVector,
    adet: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(LkShapes.MD)
            .background(LkSurfacePanel)
            .clickable(onClick = onClick)
            .heightIn(min = 92.dp)
            .padding(LkSpacing.Space3),
        verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(LkShapes.SM)
                .background(LkSurfaceTile),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ikon,
                contentDescription = null,
                tint = LkTileInk,
                modifier = Modifier.size(17.dp)
            )
        }
        Text(
            text = baslik,
            style = LkTypography.getBodySmall(),
            color = LkTextPrimary,
            maxLines = 2
        )
        Text(
            text = if (adet == 1) "1 araç" else "$adet araç",
            style = LkTypography.getMicro(),
            color = LkTextMuted
        )
    }
}

/**
 * Arac listesi — KART DEGIL SATIR.
 *
 * Kartlarla bir ekrana iki arac siginiyordu; satirla bes tanesi
 * goruluyor. Rozet aracin kipini soyluyor: hizli hesap mi, ileri analiz
 * mi (`CalculationItem.modeLabels`).
 */
@Composable
private fun AracListesi(
    araclar: List<CalculationItem>,
    onCalculationSelected: (CalculationItem) -> Unit,
    onDetailedSelected: ((CalculationItem) -> Unit)? = null
) {
    LkRowGroup {
        araclar.forEachIndexed { index, arac ->
            if (index > 0) LkHairline()
            LkListRow(
                baslik = arac.title,
                altBaslik = arac.description.ifBlank {
                    "${arac.inputCount} bilgiyle hesaplanır"
                },
                kategori = if (arac.supportsQuickCalculation) "Hızlı" else "İleri analiz",
                onClick = {
                    if (arac.supportsQuickCalculation) onCalculationSelected(arac)
                    else onDetailedSelected?.invoke(arac) ?: onCalculationSelected(arac)
                },
                sag = {
                    Icon(
                        Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = LkTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}


@Composable
private fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    LkPressable(onClick = onClick, modifier = modifier) {
      LkCard(padding = LkSpacing.Space4) {
        /* §24 ikon kutucugu; ciplak ikon degil. */
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(LkSurfaceTile),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = LkTileInk,
                modifier = Modifier.size(21.dp)
            )
        }
        Spacer(modifier = Modifier.height(LkSpacing.Space3))
        Text(
            text = title,
            style = LkTypography.getBodySmall(),
            color = LkTextPrimary,
            maxLines = 2
        )
        Text(
            text = subtitle,
            style = LkTypography.getMicro(),
            color = LkTextSecondary,
            maxLines = 1
        )
      }
    }
}

@Composable
private fun CalculationCard(
    item: CalculationItem,
    onClick: () -> Unit,
    onOpenDetailed: (() -> Unit)? = null,
    onOpenQuick: (() -> Unit)? = null
) {
    LkPressable(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space1)
    ) {
      LkCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = LkTypography.getBodyStrong(),
                    color = LkTextPrimary
                )
                Spacer(modifier = Modifier.height(LkSpacing.Space1))
                Text(
                    text = item.description.ifBlank { "${item.inputCount} bilgiyle hesaplanır" },
                    style = LkTypography.getMetadata(),
                    color = LkTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Outlined.Calculate,
                contentDescription = null,
                tint = LkPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(LkSpacing.Space2))
        Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
            val categoryLabel = CALCULATION_CATEGORIES.firstOrNull { it.key == item.category }?.label ?: item.category
            LkChip(text = categoryLabel)
            if (item.supportsQuickCalculation) {
                LkChip(text = "Hızlı hesap", onClick = onOpenQuick)
            }
            if (item.supportsDetailedAnalysis) {
                LkChip(text = "Detaylı analiz mevcut", onClick = onOpenDetailed)
            }
        }
      }
    }
}

// ─── FİNANSAL GÖRÜNÜM TAB ──────────────────────────────────

@Composable
private fun RecordRow(
    record: BusinessRecordDto,
    onClick: () -> Unit,
    isOverdue: Boolean = false
) {
    val alacak = record.type == "receivable" || record.direction == "receivable"
    val directionLabel = if (alacak) "Tahsilat" else "Ödeme"
    val dateLabel = record.dueAt?.let { LkDateUtils.formatDate(it) }?.ifBlank { "Tarih yok" } ?: "Tarih yok"
    val gecikti = isOverdue ||
        (record.dueAt != null && record.dueAt < Clock.System.todayIn(TimeZone.currentSystemDefault()).toString())

    /*
     * §24 satir anatomisi -- Kayitlar ekraniyla AYNI ikon sozlugu ve ayni
     * duzen. Onceden bu ekranin kendi satir bicimi vardi ve ayni kayit iki
     * ekranda iki farkli sekilde goruntuleniyordu.
     *
     * Gecikme rengin yaninda "Gecikti" KELIMESIYLE de veriliyor.
     */
    LkListRow(
        baslik = record.title,
        altBaslik = listOf(dateLabel, if (gecikti) "Gecikti" else "Planlı").joinToString(" · "),
        kategori = directionLabel,
        tutar = if (record.amount != null) formatTry(record.amount) else null,
        tutarRengi = if (gecikti) LkDanger else LkTextPrimary,
        onClick = onClick,
        ikon = {
            Icon(
                if (alacak) Icons.Outlined.SouthWest else Icons.Outlined.NorthEast,
                contentDescription = null,
                tint = if (gecikti) LkDanger else LkTileInk,
                modifier = Modifier.size(21.dp)
            )
        }
    )
}

// ─── GEÇMİŞ TAB ────────────────────────────────────────────

@Composable
private fun GecmisTab(
    history: List<FormulaCalculationDto>,
    catalog: List<CalculationItem>,
    onHistorySelected: (FormulaCalculationDto, CalculationItem) -> Unit
) {
    if (history.isEmpty()) {
        LkEmptyState(
            title = "Henüz hesaplama geçmişi yok",
            description = "Bir hesaplama yaptığınızda geçmişiniz burada görünür."
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(LkSpacing.Space4),
        verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
    ) {
        items(history) { item ->
            val calculationItem = catalog.find { it.definition.formulaId == item.formulaId }
            if (calculationItem != null) {
                HistoryRow(
                    item = item,
                    calculationItem = calculationItem,
                    onClick = { onHistorySelected(item, calculationItem) }
                )
            }
        }
    }
}

@Composable
private fun HistoryRow(
    item: FormulaCalculationDto,
    calculationItem: CalculationItem,
    onClick: () -> Unit
) {
    /*
     * Onceki halde .padding() .clickable()'DAN ONCE geliyordu: dokunma alani
     * dolgunun disinda kaliyor, kartin kenarina basinca hicbir sey olmuyordu.
     * LkPressable tum karti tiklanabilir yapiyor.
     */
    LkPressable(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
      LkCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.formulaName,
                style = LkTypography.getBodyStrong(),
                color = LkTextPrimary
            )
            Text(
                text = LkDateUtils.formatDateTime(item.createdAt),
                style = LkTypography.getMicro(),
                color = LkTextSecondary
            )
        }
        Spacer(modifier = Modifier.height(LkSpacing.Space3))
        item.result.entries
            .filter { it.key != "durum" }
            .take(4)
            .forEach { (key, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formulaResultLabel(key),
                        style = LkTypography.getBodySmall(),
                        color = LkTextSecondary
                    )
                    Text(
                        text = value.displayValue(),
                        style = LkTypography.getBodyStrong(),
                        color = LkTextPrimary
                    )
                }
            }
      }
    }
}

fun formulaCategoryLabel(category: String?): String {
    val cat = category?.lowercase()
    return when (cat) {
        "cash", "nakit", "likidite" -> "Nakit & Likidite"
        "profitability", "karlilik", "fiyatlama" -> "Kârlılık & Fiyatlama"
        "customer", "musteri", "satis" -> "Satış & Müşteri"
        "operations", "stok", "operasyon" -> "Stok & Operasyon"
        "growth", "yatirim", "buyume" -> "Yatırım & Büyüme"
        "valuation", "degerleme", "ileri_analiz" -> "Değerleme & İleri Analiz"
        else -> category?.replace('_', ' ')?.let { s -> s.replaceFirstChar { c -> c.uppercaseChar() } } ?: "Genel"
    }
}

fun modelCategoryLabel(category: String): String {
    val cat = category.lowercase()
    return when (cat) {
        "cash", "nakit", "likidite" -> "Nakit & Likidite"
        "profitability", "karlilik", "fiyatlama" -> "Kârlılık & Fiyatlama"
        "customer", "musteri", "satis" -> "Satış & Müşteri"
        "operations", "stok", "operasyon" -> "Stok & Operasyon"
        "growth", "yatirim", "buyume" -> "Yatırım & Büyüme"
        "valuation", "degerleme", "ileri_analiz" -> "Değerleme & İleri Analiz"
        else -> category.replace('_', ' ').replaceFirstChar { c -> c.uppercaseChar() }
    }
}