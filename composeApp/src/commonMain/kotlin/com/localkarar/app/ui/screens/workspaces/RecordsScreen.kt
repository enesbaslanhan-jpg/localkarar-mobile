package com.localkarar.app.ui.screens.workspaces

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.IconButton
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.NorthEast
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.SouthWest
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.core.rememberFileSharer
import com.localkarar.app.core.LkFormatting
import com.localkarar.app.network.dto.BusinessRecordDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkChip
import com.localkarar.app.ui.components.LkEmptyState
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkHairline
import com.localkarar.app.ui.components.LkHeroPage
import com.localkarar.app.ui.components.LkListRow
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkRowGroup
import com.localkarar.app.ui.components.rememberLkSayac
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.KayitYonu
import com.localkarar.app.workspaces.RecordsUiState
import com.localkarar.app.workspaces.RecordsViewModel

fun recordTypeLabel(type: String): String {
    return when (type) {
        "payment" -> "Ödeme"
        "receivable" -> "Tahsilat"
        "promissory_note" -> "Senet"
        "purchase" -> "Satın Alma"
        "shipment" -> "Sevkiyat"
        "task" -> "Görev"
        "deferred" -> "Ertelenen"
        else -> "Diğer"
    }
}

fun recordStatusLabel(status: String): String {
    return when (status) {
        "open" -> "Açık"
        "in_progress" -> "Devam Ediyor"
        "completed" -> "Tamamlandı"
        "cancelled" -> "İptal"
        "deferred" -> "Ertelendi"
        else -> status
    }
}

private val STATUS_FILTERS = listOf<String?>(null, "open", "in_progress", "completed", "deferred")

/**
 * Mockup "Takip 2 — Kayıtlar".
 *
 * 🔴 UC SAPMA DUZELTILDI:
 *
 * 1. YON SUZGECI YOKTU. Mockup'ta haplar Tümü · Tahsilat · Ödeme ·
 *    Yönü belirsiz. Ozellikle sonuncusu: tutari olan ama borc mu alacak mi
 *    belli olmayan kayitlara mobilde ulasmanin hicbir yolu yoktu — onceki
 *    turda yakalanan `awaitingDirection` sapmasinin son ayagi.
 * 2. HERO'DA TEK SATIR METIN VARDI ("25 kayıt"). Mockup iki rakam
 *    gosteriyor: acik kayit ve yonu belirsiz. Ikisi de ozet ucundan
 *    (`counts.open`, `counts.awaitingDirection`) geliyor, uydurulmadi.
 * 3. HAP SERIDI BIR LazyColumn'UN ICINDEYDI ve `weight(1f)` aliyordu:
 *    ekranin altida biri suzgec seridine gidiyordu. Artik listenin
 *    kendi basligi.
 */
@Composable
fun RecordsScreen(
    viewModel: RecordsViewModel,
    /**
     * Toplu ice aktarma paneli. `null` ise dugme cizilmiyor --
     * ice aktarma isletme bazli bir yetki isi ve ekranin kendisi
     * karar vermiyor.
     */
    importViewModel: com.localkarar.app.workspaces.RecordImportViewModel? = null,
    onOpenRecord: (String) -> Unit,
    onAddRecord: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var statusFilter by remember { mutableStateOf<String?>(null) }
    var yonFilter by remember { mutableStateOf(KayitYonu.TUMU) }

    /*
     * DISA AKTARMA.
     *
     * `rememberFileSharer` null donerse dugme HIC cizilmiyor — kamera
     * dugmesiyle ayni kural. Bugun iki platformda da dolu, ama kural
     * platformun degil arayuzun sorumlulugu.
     */
    val paylas = rememberFileSharer()
    val disaAktariliyor by viewModel.disaAktariliyor.collectAsState()
    var bicimSecimiAcik by remember { mutableStateOf(false) }
    var iceAktarmaAcik by remember { mutableStateOf(false) }

    LkHeroPage(
        title = "Kayıtlar",
        onBack = onBack,
        actions = {
            if (importViewModel != null) {
                IconButton(onClick = { iceAktarmaAcik = true }) {
                    Icon(
                        Icons.Outlined.FileUpload,
                        contentDescription = "İçe aktar",
                        tint = LkHero.OnHero
                    )
                }
            }
            if (paylas != null) {
                IconButton(
                    onClick = { bicimSecimiAcik = true },
                    enabled = disaAktariliyor == null
                ) {
                    Icon(
                        Icons.Outlined.IosShare,
                        contentDescription = "Dışa aktar",
                        tint = LkHero.OnHero
                    )
                }
            }
        },
        heroExtra = {
            val ozet = (uiState as? RecordsUiState.Content)?.summary
            if (ozet != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LkSpacing.Space5, vertical = LkSpacing.Space4)
                ) {
                    HeroSayac("AÇIK KAYIT", ozet.counts.open, Modifier.weight(1f))
                    Box(
                        Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(Color(0x29FFFFFF))
                    )
                    HeroSayac(
                        "YÖNÜ BELİRSİZ",
                        ozet.counts.awaitingDirection,
                        Modifier.weight(1f).padding(start = LkSpacing.Space4)
                    )
                }
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            /*
             * Yon suzgeci — mockup'in hap seridi. Durum suzgeci altinda
             * ayri satirda kaldi: mockup'ta yok ama calisan bir suzgec,
             * tasarim turu ozellik SILMEZ.
             */
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = LkSpacing.Space4, end = LkSpacing.Space4, top = LkSpacing.Space3),
                horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)
            ) {
                items(KayitYonu.entries.toList()) { yon ->
                    val secili = yonFilter == yon
                    LkChip(
                        text = yon.etiket,
                        background = if (secili) LkPrimary else LkSurfaceRaised,
                        contentColor = if (secili) LkOnPrimary else LkTextSecondary,
                        modifier = Modifier.clickable {
                            yonFilter = yon
                            viewModel.setDirection(yon)
                        }
                    )
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space2),
                horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)
            ) {
                items(STATUS_FILTERS) { filter ->
                    val label = when (filter) {
                        null -> "Tüm durumlar"
                        else -> recordStatusLabel(filter)
                    }
                    val selected = statusFilter == filter
                    LkChip(
                        text = label,
                        background = if (selected) LkPrimary else LkSurfaceRaised,
                        contentColor = if (selected) LkOnPrimary else LkTextSecondary,
                        modifier = Modifier.clickable {
                            statusFilter = filter
                            viewModel.setFilter(filter, null)
                        }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (val state = uiState) {
                    is RecordsUiState.Loading -> LkLoadingState()
                    is RecordsUiState.Error -> LkErrorState(
                        message = state.message,
                        onRetry = { viewModel.load() }
                    )
                    is RecordsUiState.Content -> {
                        if (state.records.isEmpty()) {
                            LkEmptyState(
                                title = "Kayıt bulunamadı",
                                description = if (yonFilter == KayitYonu.BELIRSIZ) {
                                    "Yönü belirsiz kayıt yok — tutarı olan her kayıt borç ya da alacak olarak işaretli."
                                } else {
                                    "Bu filtreye uygun kayıt yok."
                                }
                            )
                        } else {
                            /* Satirlar TEK yukseltilmis yuzeyde; kart yigini degil. */
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(LkSpacing.Space4)
                            ) {
                                item {
                                    LkRowGroup {
                                        state.records.forEachIndexed { i, record ->
                                            RecordCard(
                                                record = record,
                                                onClick = { onOpenRecord(record.id) }
                                            )
                                            if (i != state.records.lastIndex) LkHairline()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            LkButton(
                text = "Yeni Kayıt",
                onClick = onAddRecord,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(LkSpacing.Space4)
            )
        }
    }

    /*
     * BICIM SECIMI.
     *
     * Uc bicim de webdekiyle ayni ve ne ise yaradiklari YAZIYOR: "Excel"
     * demek yetmiyor, kullanici hangisini muhasebecisine gonderecegini
     * bilmeli.
     */
    if (iceAktarmaAcik && importViewModel != null) {
        RecordImportPanel(
            viewModel = importViewModel,
            onKapat = { iceAktarmaAcik = false },
            onBitti = {
                iceAktarmaAcik = false
                /* Yeni kayitlar listede gorunmeli; aksi halde kullanici
                   "oluşturuldu" deyip bos liste gorurdu. */
                viewModel.refresh()
            }
        )
    }

    if (bicimSecimiAcik && paylas != null) {
        BicimSecimPaneli(
            onKapat = { bicimSecimiAcik = false },
            onSec = { bicim ->
                bicimSecimiAcik = false
                viewModel.disaAktar(bicim, paylas)
            }
        )
    }
}

/**
 * DISA AKTARMA BICIMI.
 *
 * Uc bicim de webdekiyle ayni. Her birinin NE ISE YARADIGI yaziyor:
 * "Excel" demek tek basina yetmiyor, kullanici muhasebecisine hangisini
 * gonderecegini bilmeli.
 *
 * `BelgeYuklemePaneli` ile ayni desen — ekrana ait bir secim, kabuk
 * seviyesindeki `LkMenuSheet` degil.
 */
@Composable
private fun BicimSecimPaneli(
    onKapat: () -> Unit,
    onSec: (String) -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onKapat,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(LkShapes.LG)
                    .background(LkSurfaceElevated)
                    .padding(LkSpacing.Space5),
                verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
            ) {
                Text(
                    text = "Dışa aktar",
                    style = LkTypography.getSectionTitle(),
                    color = LkTextPrimary
                )
                /* Kullanicinin ne indirdigini bilmesi icin: ekrandaki
                   suzgec dosyaya da uygulaniyor. */
                Text(
                    text = "Ekrandaki filtreye uyan kayıtlar aktarılır.",
                    style = LkTypography.getMetadata(),
                    color = LkTextSecondary
                )

                listOf(
                    Triple("pdf", "PDF", "Yazdırmaya ve göndermeye uygun"),
                    Triple("xlsx", "Excel", "Hesap tablosunda düzenlenebilir"),
                    Triple("csv", "CSV", "Başka programlara aktarmak için")
                ).forEach { (kod, ad, aciklama) ->
                    LkButton(
                        text = "$ad — $aciklama",
                        variant = LkButtonVariant.SECONDARY,
                        onClick = { onSec(kod) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                LkButton(
                    text = "Vazgeç",
                    variant = LkButtonVariant.QUIET,
                    onClick = onKapat,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/** Hero icindeki adet sayaci. Para degil, bu yuzden bicimlenmiyor — sadece sayiyor. */
@Composable
private fun HeroSayac(etiket: String, adet: Int, modifier: Modifier = Modifier) {
    val anlik = rememberLkSayac(adet.toDouble())
    Column(modifier) {
        Text(etiket, style = LkTypography.getMetadata(), color = LkHero.OnHeroSecondary)
        Spacer(Modifier.height(LkSpacing.Space1))
        Text(
            text = anlik.toInt().toString(),
            style = LkTypography.getTitleS(),
            color = LkHero.OnHero,
            maxLines = 1
        )
    }
}

@Composable
fun RecordCard(
    record: BusinessRecordDto,
    onClick: () -> Unit
) {
    val dueDate = LkDateUtils.parseDate(record.dueAt)
    val overdue = dueDate?.let { LkDateUtils.daysUntil(it) } ?: 0
    val isActive = record.status == "open" || record.status == "in_progress"

    /*
     * §24 satir anatomisi — ikon kutucugu | baslik + durum/vade | tur | tutar.
     *
     * 🔴 ONCEDEN KART YIGINIYDI: her kayit kendi kenarlikli kutusundaydi ve
     * icinde iki hap vardi. On kayitlik bir liste on ayri cerceve demekti;
     * goz nereye bakacagini bilmiyordu. Artik tek yuzey, satirlar dikey
     * ayracla ayriliyor.
     *
     * Gecikme SADECE RENKLE degil "Gecikti" kelimesiyle de belirtiliyor.
     */
    val gecikti = record.dueAt != null && isActive && overdue < 0
    val yonuBelirsiz = record.direction == "neutral" && record.amount != null
    val altSatir = listOfNotNull(
        recordStatusLabel(record.status).takeIf { it.isNotBlank() },
        when {
            gecikti -> "Gecikti"
            record.dueAt != null && isActive && dueDate != null -> LkDateUtils.formatShortDate(dueDate)
            else -> null
        },
        /* Yon atanmamis kayit RENKLE degil KELIMEYLE isaretli (§24 erisilebilirlik). */
        if (yonuBelirsiz) "yön atanmadı" else null
    ).joinToString(" · ").ifBlank { null }

    LkListRow(
        baslik = record.title,
        altBaslik = altSatir,
        kategori = if (yonuBelirsiz) "Belirsiz" else recordTypeLabel(record.type),
        tutar = record.amount?.let { LkFormatting.formatMoney(it, record.currency) },
        tutarRengi = if (gecikti) LkDanger else LkTextPrimary,
        onClick = onClick,
        ikon = {
            Icon(
                when {
                    yonuBelirsiz -> Icons.Outlined.HelpOutline
                    record.direction == "receivable" -> Icons.Outlined.SouthWest
                    record.direction == "payable" -> Icons.Outlined.NorthEast
                    else -> Icons.Outlined.ReceiptLong
                },
                contentDescription = null,
                tint = when {
                    gecikti -> LkDanger
                    yonuBelirsiz -> LkWarning
                    else -> LkTileInk
                },
                modifier = Modifier.size(21.dp)
            )
        }
    )
}
