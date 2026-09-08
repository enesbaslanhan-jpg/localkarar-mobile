package com.localkarar.app.ui.screens.workspaces

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
import androidx.compose.material.IconButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.core.rememberFileSharer
import com.localkarar.app.network.dto.eFaturayiCoz
import com.localkarar.app.core.LkFormatting
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkInfoPanel
import com.localkarar.app.ui.components.LkLoadingDesen
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkHeroPage
import com.localkarar.app.ui.components.LkResultRow
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.RecordDetailUiState
import com.localkarar.app.workspaces.RecordDetailViewModel
import kotlinx.datetime.LocalDate

@Composable
fun RecordDetailScreen(
    viewModel: RecordDetailViewModel,
    onEdit: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var actionError by remember { mutableStateOf<String?>(null) }
    var showDeferDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val paylas = rememberFileSharer()
    val pdfHazirlaniyor by viewModel.pdfHazirlaniyor.collectAsState()

    LkHeroPage(
        title = "Kayıt",
        onBack = onBack,
        actions = {
            /* PDF paylasimi — platformda paylasim yolu varsa. */
            if (paylas != null) {
                androidx.compose.material.IconButton(
                    onClick = { viewModel.pdfPaylas(paylas) },
                    enabled = !pdfHazirlaniyor
                ) {
                    Icon(
                        Icons.Outlined.IosShare,
                        contentDescription = "PDF olarak paylaş",
                        tint = LkHero.OnHero
                    )
                }
            }
            androidx.compose.material.IconButton(onClick = onEdit) {
                Icon(Icons.Outlined.Edit, contentDescription = "Düzenle", tint = LkHero.OnHero)
            }
        }
    ) {
        when (val state = uiState) {
            is RecordDetailUiState.Loading -> LkLoadingState(desen = LkLoadingDesen.DETAY)
            is RecordDetailUiState.Error -> LkErrorState(
                message = state.message,
                onRetry = { viewModel.load() }
            )
            is RecordDetailUiState.Content -> {
                val record = state.record
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(LkSpacing.Space4),
                    verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
                ) {
                    item {
                        if (actionError != null) {
                            Text(
                                text = actionError!!,
                                style = LkTypography.getBodySmall(),
                                color = LkDanger
                            )
                            Spacer(modifier = Modifier.height(LkSpacing.Space2))
                        }
                        LkInfoPanel(title = record.title) {
                            LkResultRow(
                                label = "Tür",
                                value = recordTypeLabel(record.type)
                            )
                            Spacer(modifier = Modifier.height(LkSpacing.Space2))
                            LkResultRow(
                                label = "Durum",
                                value = recordStatusLabel(record.status)
                            )
                            Spacer(modifier = Modifier.height(LkSpacing.Space2))
                            record.amount?.let {
                                LkResultRow(
                                    label = "Tutar",
                                    value = LkFormatting.formatMoney(it, record.currency),
                                    valueColor = when (record.direction) {
                                        "payable" -> LkDanger
                                        "receivable" -> LkSuccess
                                        else -> LkTextPrimary
                                    }
                                )
                                Spacer(modifier = Modifier.height(LkSpacing.Space2))
                            }
                            record.dueAt?.let {
                                LkResultRow(label = "Son Tarih", value = LkDateUtils.formatDateTime(it))
                                Spacer(modifier = Modifier.height(LkSpacing.Space2))
                            }
                            record.contact?.let {
                                LkResultRow(label = "Kişi", value = it.name)
                                Spacer(modifier = Modifier.height(LkSpacing.Space2))
                            }
                            record.assignedTo?.let {
                                LkResultRow(label = "Sorumlu", value = it.name)
                                Spacer(modifier = Modifier.height(LkSpacing.Space2))
                            }
                            record.recurrenceRule?.let {
                                LkResultRow(label = "Tekrar", value = it.replaceFirstChar { c -> c.uppercase() })
                                Spacer(modifier = Modifier.height(LkSpacing.Space2))
                            }
                            if (!record.description.isNullOrBlank()) {
                                Text(
                                    text = record.description,
                                    style = LkTypography.getBodySmall(),
                                    color = LkTextSecondary
                                )
                            }
                        }
                    }

                    /*
                     * DAYANAK — "bu rakam nereden geldi".
                     *
                     * 🔴 MOBILDE HIC YOKTU. e-Faturadan acilan kayitta
                     * webde faturanin KENDI alanlari gosteriliyor
                     * (`KayitDetay.jsx`); mobilde kullanici tutara
                     * korlemesine guvenmek zorundaydi.
                     */
                    /*
                     * ⚠️ BOLUM ARTIK BELGE YOKKEN DE CIZILIYOR (madde 11).
                     * Onceden yalniz bagli belge varken gorunuyordu;
                     * belge baglamanin kullaniciya sunulan hicbir yolu
                     * olmadigi icin bu dogal goruntuyordu. Baglama
                     * eklenince bolumu gizlemek, islevin bulunamamasi
                     * demek olurdu.
                     */
                    item {
                        KaynakBelgeBolumu(
                            belgeler = record.documents,
                            secilebilirBelgeler = state.secilebilirBelgeler,
                            seciciAcik = state.belgeSeciciAcik,
                            yukleniyor = state.belgeYukleniyor,
                            islemVar = state.isActing,
                            onSeciciAc = { viewModel.belgeSeciciyiAc() },
                            onSeciciKapat = { viewModel.belgeSeciciyiKapat() },
                            onBagla = { viewModel.belgeBagla(it) },
                            onKopar = { viewModel.belgeBaginiKopar(it) }
                        )
                    }

                    if (record.reminders.isNotEmpty()) {
                        item { HatirlaticiBolumu(record.reminders) }
                    }

                    if (record.history.isNotEmpty()) {
                        item { GecmisBolumu(record.history) }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
                        ) {
                            LkButton(
                                text = "Tamamla",
                                variant = LkButtonVariant.SECONDARY,
                                enabled = record.status != "completed" && !state.isActing,
                                onClick = {
                                    actionError = null
                                    viewModel.setStatus("completed") { actionError = it }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            LkButton(
                                text = "İptal",
                                variant = LkButtonVariant.QUIET,
                                enabled = record.status != "cancelled" && !state.isActing,
                                onClick = {
                                    actionError = null
                                    viewModel.setStatus("cancelled") { actionError = it }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
                        ) {
                            LkButton(
                                text = "Düzenle",
                                variant = LkButtonVariant.SECONDARY,
                                onClick = onEdit,
                                modifier = Modifier.weight(1f)
                            )
                            LkButton(
                                text = "Ertele",
                                variant = LkButtonVariant.SECONDARY,
                                enabled = record.status != "deferred" && !state.isActing,
                                onClick = { showDeferDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        LkButton(
                            text = "Kaydı Sil",
                            variant = LkButtonVariant.DANGER,
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    if (showDeferDialog) {
        DeferRecordDialog(
            isActing = (uiState as? RecordDetailUiState.Content)?.isActing == true,
            onDismiss = { showDeferDialog = false },
            onDefer = { dueDate, reason ->
                actionError = null
                viewModel.defer(
                    dueAt = "${dueDate}T12:00:00.000Z",
                    reason = reason,
                    onError = { actionError = it }
                )
                showDeferDialog = false
            }
        )
    }

    if (showDeleteConfirm) {
        androidx.compose.material.AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            backgroundColor = LkSurfacePanel,
            title = {
                Text(
                    text = "Kaydı Sil",
                    style = LkTypography.getBodyStrong(),
                    color = LkTextPrimary
                )
            },
            text = {
                Text(
                    text = "Bu kayıt kalıcı olarak arşivlenecek. Devam etmek istiyor musunuz?",
                    style = LkTypography.getBodySmall(),
                    color = LkTextSecondary
                )
            },
            confirmButton = {
                LkButton(
                    text = "Evet, Sil",
                    variant = LkButtonVariant.DANGER,
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.delete { success ->
                            if (success) onBack() else actionError = "Kayıt silinemedi."
                        }
                    }
                )
            },
            dismissButton = {
                LkButton(
                    text = "Vazgeç",
                    variant = LkButtonVariant.QUIET,
                    onClick = { showDeleteConfirm = false }
                )
            }
        )
    }
}

@Composable
private fun DeferRecordDialog(
    isActing: Boolean,
    onDismiss: () -> Unit,
    onDefer: (LocalDate, String) -> Unit
) {
    var dueDate by remember { mutableStateOf(LkDateUtils.dateAt(1)) }
    var reason by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    androidx.compose.material.AlertDialog(
        onDismissRequest = { if (!isActing) onDismiss() },
        backgroundColor = LkSurfacePanel,
        title = {
            Text(
                text = "Kaydı Ertele",
                style = LkTypography.getBodyStrong(),
                color = LkTextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)) {
                if (error != null) {
                    Text(text = error!!, style = LkTypography.getBodySmall(), color = LkDanger)
                }
                com.localkarar.app.ui.components.LkDateField(
                    label = "Yeni Tarih",
                    date = dueDate,
                    onDateSelected = { dueDate = it }
                )
                com.localkarar.app.ui.components.LkTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = "Gerekçe",
                    placeholder = "Erteleme nedeni"
                )
            }
        },
        confirmButton = {
            LkButton(
                text = if (isActing) "Erteleniyor..." else "Ertele",
                enabled = reason.isNotBlank() && !isActing,
                onClick = {
                    if (reason.isBlank()) {
                        error = "Gerekçe yazın"
                    } else {
                        onDefer(dueDate, reason.trim())
                    }
                }
            )
        },
        dismissButton = {
            LkButton(
                text = "Vazgeç",
                variant = LkButtonVariant.QUIET,
                onClick = onDismiss
            )
        }
    )
}

/**
 * KAYNAK BELGE — kaydin dayanagi.
 *
 * 🔴 MOBILDE HIC YOKTU. Sunucu detay ucunda bagli belgeleri ve
 * cozumlenmis e-Faturayi getiriyordu (`business-tracker.ts:672`);
 * mobil DTO bu alani hic tanimiyordu, dolayisiyla "bu tutar hangi
 * faturadan geldi" sorusunun cevabi yalniz webde vardi.
 *
 * ⚠️ Fatura cozumlenememisse ALANLAR UYDURULMUYOR: webdeki gibi tek
 * satirlik bir aciklama yaziliyor.
 */
@Composable
private fun KaynakBelgeBolumu(
    belgeler: List<com.localkarar.app.network.dto.RecordDocumentLinkDto>,
    secilebilirBelgeler: List<com.localkarar.app.network.dto.WorkspaceDocumentDto>?,
    seciciAcik: Boolean,
    yukleniyor: Boolean,
    islemVar: Boolean,
    onSeciciAc: () -> Unit,
    onSeciciKapat: () -> Unit,
    onBagla: (String) -> Unit,
    onKopar: (String) -> Unit
) {
    BolumBasligi("DAYANAK")

    if (belgeler.isEmpty()) {
        Text(
            text = "Bu kayda bağlı belge yok.",
            style = LkTypography.getMetadata(),
            color = LkTextSecondary
        )
        Spacer(Modifier.height(LkSpacing.Space3))
    }

    belgeler.forEach { bag ->
        val belge = bag.document ?: return@forEach
        /* Sunucu bu alani detay ucunda JSON DIZGESI olarak donduruyor;
           cozumleme `eFaturayiCoz` icinde — bkz. WorkspaceDtos.kt. */
        val fatura = belge.eFaturayiCoz()

        Column(modifier = Modifier.fillMaxWidth().padding(bottom = LkSpacing.Space3)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = belge.originalName ?: "Belge",
                    style = LkTypography.getBody(),
                    color = LkTextPrimary,
                    modifier = Modifier.weight(1f)
                )
                /*
                 * ⚠️ Yalniz BAGI kopariyor, belgeyi SILMIYOR.
                 * `contentDescription` bunu soyluyor: kullanici
                 * belgesini kaybetmekten korkmamali.
                 */
                IconButton(
                    onClick = { belge.id?.let(onKopar) },
                    enabled = !islemVar && belge.id != null
                ) {
                    Icon(
                        Icons.Outlined.LinkOff,
                        contentDescription = "Bağı kopar — belge silinmez",
                        tint = LkTextSecondary
                    )
                }
            }
            /* Boyut yalniz VARSA — 0 KB yazmak, bilinmeyeni bilinen
               gibi gostermek olurdu. */
            belge.sizeBytes?.takeIf { it > 0 }?.let { boyut ->
                Text(
                    text = "${boyut / 1024} KB",
                    style = LkTypography.getMicro(),
                    color = LkTextMuted
                )
            }

            Spacer(Modifier.height(LkSpacing.Space2))

            if (fatura == null) {
                Text(
                    text = "Bu belge e-Fatura olarak çözümlenemedi; alanlar okunamadı.",
                    style = LkTypography.getMetadata(),
                    color = LkTextSecondary
                )
            } else {
                Text(
                    text = "Fatura okundu — aşağıdaki alanlar faturadan alındı, tahmin edilmedi.",
                    style = LkTypography.getMicro(),
                    color = LkPrimary
                )
                Spacer(Modifier.height(LkSpacing.Space2))

                fatura.id?.let {
                    LkResultRow(label = "Fatura no", value = it)
                    Spacer(Modifier.height(LkSpacing.Space2))
                }
                fatura.duzenlemeTarihi?.let {
                    LkResultRow(label = "Düzenleme", value = LkDateUtils.formatDate(it))
                    Spacer(Modifier.height(LkSpacing.Space2))
                }
                /*
                 * ⚠️ VADE ORNEKLERIN %86'SINDA YOK ve bu olagan.
                 * Bosken duzenleme tarihini yazmak olmayan bir vade
                 * uydurmak olurdu; webde de "faturada belirtilmemiş"
                 * yaziyor.
                 */
                LkResultRow(
                    label = "Vade",
                    value = fatura.vadeTarihi?.let { LkDateUtils.formatDate(it) }
                        ?: "Faturada belirtilmemiş"
                )
                Spacer(Modifier.height(LkSpacing.Space2))

                fatura.odenecekTutar?.let {
                    LkResultRow(
                        label = "Tutar",
                        value = LkFormatting.formatMoney(it, fatura.paraBirimi)
                    )
                    Spacer(Modifier.height(LkSpacing.Space2))
                }

                TarafSatiri("Satıcı", fatura.satici)
                TarafSatiri("Alıcı", fatura.alici)
            }
        }
    }

    /*
     * BELGE BAGLA.
     *
     * ⚠️ Zaten bagli olanlar listede YOK. Sunucu ayni bagi ikinci kez
     * yazmiyor (upsert), ama kullaniciya hicbir seyi degistirmeyecek
     * bir secenek gostermek yaniltici olurdu.
     */
    if (!seciciAcik) {
        LkButton(
            text = "Belge bağla",
            variant = LkButtonVariant.SECONDARY,
            enabled = !islemVar,
            onClick = onSeciciAc
        )
    } else {
        val bagliKimlikler = belgeler.mapNotNull { it.document?.id }.toSet()
        val secenekler = secilebilirBelgeler?.filter { it.id !in bagliKimlikler }

        when {
            yukleniyor || secenekler == null -> Text(
                text = "Belgeler yükleniyor...",
                style = LkTypography.getMetadata(),
                color = LkTextSecondary
            )
            secenekler.isEmpty() -> Text(
                text = "Bağlanabilecek başka belge yok.",
                style = LkTypography.getMetadata(),
                color = LkTextSecondary
            )
            else -> secenekler.forEach { secenek ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !islemVar) { onBagla(secenek.id) }
                        /* §19: dokunma hedefi en az 44dp. */
                        .heightIn(min = 44.dp)
                        .padding(vertical = LkSpacing.Space2),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Description,
                        contentDescription = null,
                        tint = LkTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(LkSpacing.Space2))
                    Text(
                        text = secenek.originalName,
                        style = LkTypography.getBodySmall(),
                        color = LkTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    (secenek.documentDate ?: secenek.createdAt)?.let { tarih ->
                        Text(
                            text = LkDateUtils.formatDate(tarih),
                            style = LkTypography.getMicro(),
                            color = LkTextMuted
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(LkSpacing.Space2))
        LkButton(
            text = "Vazgeç",
            variant = LkButtonVariant.QUIET,
            onClick = onSeciciKapat
        )
    }
}

/** Satici/alici — unvan ve varsa VKN/TCKN. */
@Composable
private fun TarafSatiri(
    etiket: String,
    taraf: com.localkarar.app.network.dto.EFaturaTarafDto?
) {
    val unvan = taraf?.unvan
    /* Unvan yoksa tire: webdeki '—' ile ayni. */
    LkResultRow(label = etiket, value = unvan ?: "—")
    /* Kimlik yalniz varsa ve unvanin ALTINDA — ayri bir satir degil,
       aciklama. */
    taraf?.kimlik?.let { kimlik ->
        Text(
            text = listOfNotNull(taraf.kimlikTuru, kimlik).joinToString(" "),
            style = LkTypography.getMicro(),
            color = LkTextMuted
        )
    }
    Spacer(Modifier.height(LkSpacing.Space2))
}

/** Vade hatirlaticilari — webde de en fazla iki tanesi gosteriliyor. */
@Composable
private fun HatirlaticiBolumu(
    hatirlaticilar: List<com.localkarar.app.network.dto.RecordReminderDto>
) {
    BolumBasligi("HATIRLATICILAR")
    hatirlaticilar.take(2).forEach { h ->
        LkResultRow(
            label = h.scheduledAt?.let { LkDateUtils.formatDateTime(it) } ?: "—",
            /* Durum KELIMEYLE; renk tek basina tasiyici degil. */
            value = when (h.status) {
                "sent" -> "Gönderildi"
                "pending" -> "Bekliyor"
                else -> h.status ?: "—"
            }
        )
        Spacer(Modifier.height(LkSpacing.Space2))
    }
}

/** Kayit gecmisi — webde de son iki hareket. */
@Composable
private fun GecmisBolumu(
    gecmis: List<com.localkarar.app.network.dto.RecordHistoryDto>
) {
    BolumBasligi("GEÇMİŞ")
    gecmis.take(2).forEach { g ->
        LkResultRow(
            label = g.createdAt?.let { LkDateUtils.formatDateTime(it) } ?: "—",
            value = hareketEtiketi(g.action)
        )
        Spacer(Modifier.height(LkSpacing.Space2))
    }
}

/*
 * Sunucunun hareket kodlari kullaniciya HAM gosterilmiyor.
 *
 * `status_changed` gibi bir dize kullaniciya hicbir sey anlatmaz;
 * bilinmeyen bir kod gelirse kodu basmak yerine notr bir ifade
 * yaziliyor.
 */
private fun hareketEtiketi(action: String?): String = when (action) {
    "created" -> "Oluşturuldu"
    "updated" -> "Güncellendi"
    "status_changed" -> "Durum değişti"
    "deferred" -> "Ertelendi"
    "deleted" -> "Silindi"
    "document_attached" -> "Belge eklendi"
    null -> "—"
    else -> "Değişiklik"
}

@Composable
private fun BolumBasligi(baslik: String) {
    Text(
        text = baslik,
        style = LkTypography.getMicro(),
        color = LkTextMuted,
        modifier = Modifier.padding(bottom = LkSpacing.Space2)
    )
}
