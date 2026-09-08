package com.localkarar.app.ui.screens.workspaces

import com.localkarar.app.ui.components.LkHairline
import com.localkarar.app.ui.components.LkRowGroup
import com.localkarar.app.ui.components.LkListRow
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
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.core.LkFormatting
import com.localkarar.app.core.rememberCameraCapture
import com.localkarar.app.core.rememberFilePicker
import com.localkarar.app.network.dto.WorkspaceDocumentDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkChip
import com.localkarar.app.ui.components.LkEmptyState
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkInfoPanel
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkHeroPage
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.DocumentsUiState
import com.localkarar.app.workspaces.DocumentsViewModel

@Composable
fun DocumentsScreen(
    viewModel: DocumentsViewModel,
    /**
     * Belgeden onerilen finansal modeli acar.
     *
     * Webdeki `/app/finance/models/:code?documentId=` ile ayni:
     * belge kimligi de tasiniyor ve model ekrani girdileri belgeden
     * on dolduruyor, kaynagi "belge" isaretliyor.
     */
    onOpenModel: (kod: String, belgeId: String) -> Unit = { _, _ -> },
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val yukleniyor by viewModel.yukleniyor.collectAsState()
    val islenenOneri by viewModel.islenenOneri.collectAsState()
    val modelOnerileri by viewModel.modelOnerileri.collectAsState()
    val modelAraniyor by viewModel.modelAraniyor.collectAsState()
    var deleteConfirmId by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }

    /*
     * Dosya secici. Platform karsiliklari (`rememberFilePicker`) ZATEN VARDI,
     * yalnizca kullanilmiyordu -- ekran "belge yukleme su an icin web
     * suruminde kullanilabilir" diyordu.
     *
     * Kategori GONDERILMIYOR (null): sunucu icerigi kendisi cozumluyor ve
     * e-faturayi taniyor. Yukleme aninda kullaniciya kategori sordurmak,
     * dogru cevabi zaten bilen bir sisteme gereksiz bir adim eklemek olurdu.
     */
    /*
     * 🔴 WEBDEKI YUKLEME EKRANI DORT SECENEK SUNUYOR, MOBIL BIR TANE.
     *
     * Web (`Workspaces/Documents.jsx`): belge TURU secimi + "Dosya seç",
     * "Fotoğraf seç", "Fotoğraf çek", "E-posta ile gönder" ve desteklenen
     * bicimlerin listesi. Mobilde tek bir "Belge yükle" dugmesi vardi;
     * tur hic sorulmuyor, e-posta yolu hic anlatilmiyordu.
     *
     * "Fotoğraf çek" ARTIK VAR (Android: FileProvider + TakePicture).
     * iOS'ta `rememberCameraCapture` null donuyor ve dugme HIC
     * cizilmiyor -- calismayan bir dugme yerine olmayan bir dugme.
     */
    var yuklemeAcik by remember { mutableStateOf(false) }
    var seciliTur by remember { mutableStateOf<String?>(null) }

    val fotografCek = rememberCameraCapture { cekilen ->
        if (cekilen != null) {
            viewModel.belgeYukle(cekilen.name, cekilen.bytes, seciliTur)
        }
    }

    val dosyaSec = rememberFilePicker { secilen ->
        if (secilen != null) {
            viewModel.belgeYukle(secilen.name, secilen.bytes, seciliTur)
        }
    }

    LkHeroPage(
        title = "Belgeler",
        onBack = onBack,
        actions = {
            IconButton(onClick = { yuklemeAcik = true }, enabled = !yukleniyor) {
                Icon(Icons.Outlined.UploadFile, contentDescription = "Belge yükle", tint = LkHero.OnHero)
            }
        },
        heroExtra = {
            val count = (uiState as? DocumentsUiState.Content)?.documents?.size
            count?.let {
                Text("$it belge", style = LkTypography.getMetadata(), color = LkHero.OnHeroSecondary,
                    modifier = Modifier.padding(start = LkSpacing.Space5, top = LkSpacing.Space2))
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is DocumentsUiState.Loading -> LkLoadingState()
                is DocumentsUiState.Error -> LkErrorState(
                    message = state.message,
                    onRetry = { viewModel.load() }
                )
                is DocumentsUiState.Content -> {
                    if (state.documents.isEmpty()) {
                        LkEmptyState(
                            title = "Henüz belge yok",
                            description = "Fatura, sözleşme veya makbuz yükleyin. e-Fatura XML dosyaları otomatik olarak çözümlenir.",
                            icon = Icons.Outlined.AttachFile,
                            action = {
                                LkButton(
                                    text = if (yukleniyor) "Yükleniyor..." else "Belge yükle",
                                    onClick = { yuklemeAcik = true },
                                    enabled = !yukleniyor
                                )
                            }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(LkSpacing.Space4),
                            verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
                        ) {
                            /* Tek yukseltilmis yuzey; kart yigini degil. */
                            item {
                                LkRowGroup {
                                    state.documents.forEachIndexed { i, document ->
                                        DocumentCard(
                                            document = document,
                                            onDelete = { deleteConfirmId = document.id },
                                            islenenOneri = islenenOneri,
                                            onOneriKabul = { viewModel.oneriyiKabulEt(it) },
                                            onOneriRet = { viewModel.oneriyiReddet(it) },
                                            modelOnerileri = modelOnerileri[document.id],
                                            modelAraniyor = modelAraniyor == document.id,
                                            onModelAra = { viewModel.modelOnerisiAra(document.id) },
                                            onModelAc = { kod -> onOpenModel(kod, document.id) }
                                        )
                                        if (i != state.documents.lastIndex) LkHairline()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (yuklemeAcik) {
        BelgeYuklemePaneli(
            seciliTur = seciliTur,
            onTur = { seciliTur = it },
            onFotografCek = fotografCek?.let { cek ->
                {
                    yuklemeAcik = false
                    cek()
                }
            },
            onDosyaSec = {
                yuklemeAcik = false
                dosyaSec()
            },
            onEposta = {
                yuklemeAcik = false
                notice = "İşletmenize ait bir e-posta adresi açıp faturaları oraya " +
                    "gönderebilirsiniz; gelen belgeler onay bekleyen kayıt olarak düşer. " +
                    "Adresi İşletme Ayarları > e-Fatura Gelen Kutusu bölümünden açın."
            },
            onKapat = { yuklemeAcik = false }
        )
    }

    if (notice != null) {
        androidx.compose.material.AlertDialog(
            onDismissRequest = { notice = null },
            backgroundColor = LkSurfacePanel,
            title = { Text(text = "Bilgi", style = LkTypography.getBodyStrong(), color = LkTextPrimary) },
            text = { Text(text = notice!!, style = LkTypography.getBodySmall(), color = LkTextSecondary) },
            confirmButton = { LkButton(text = "Tamam", onClick = { notice = null }) }
        )
    }

    deleteConfirmId?.let { documentId ->
        androidx.compose.material.AlertDialog(
            onDismissRequest = { deleteConfirmId = null },
            backgroundColor = LkSurfacePanel,
            title = {
                Text(
                    text = "Belgeyi Sil",
                    style = LkTypography.getBodyStrong(),
                    color = LkTextPrimary
                )
            },
            text = {
                Text(
                    text = "Bu belge kalıcı olarak arşivlenecek. Devam etmek istiyor musunuz?",
                    style = LkTypography.getBodySmall(),
                    color = LkTextSecondary
                )
            },
            confirmButton = {
                LkButton(
                    text = "Evet, Sil",
                    onClick = {
                        deleteConfirmId = null
                        viewModel.delete(documentId) { success ->
                            notice = if (success) null else "Belge silinemedi."
                        }
                    }
                )
            },
            dismissButton = {
                LkButton(
                    text = "Vazgeç",
                    variant = LkButtonVariant.QUIET,
                    onClick = { deleteConfirmId = null }
                )
            }
        )
    }
}

@Composable
private fun DocumentCard(
    document: WorkspaceDocumentDto,
    onDelete: () -> Unit,
    islenenOneri: String? = null,
    onOneriKabul: (String) -> Unit = {},
    onOneriRet: (String) -> Unit = {},
    modelOnerileri: com.localkarar.app.network.dto.ModelOnerileriDto? = null,
    modelAraniyor: Boolean = false,
    onModelAra: () -> Unit = {},
    onModelAc: (String) -> Unit = {}
) {
    /*
     * §24 satir anatomisi. Onceden her belge kenarlikli bir karttaydi ve
     * icinde iki hap daha vardi — liste uc kat yer kapliyordu.
     *
     * Analiz durumu alt satirda KELIMEYLE yaziliyor; hap renginden
     * okunmasi gerekmiyor.
     */
    /*
     * 🔴 HAM SUNUCU KODU EKRANA DUSUYORDU.
     *
     * Olculdu (07.09.2026, emulator): bir e-Fatura yuklendiginde satirin
     * altinda `review_required` yaziyordu. Sebep asagidaki `else -> it`
     * daliydi: yalniz uc deger ceviriliyor, sunucunun GERCEKTE urettigi
     * degerler (`review_required`, `accepted`, `rejected`,
     * `no_suggestion`) listede yoktu. Yani en sik gorulen durumda
     * kullaniciya makine kodu gosteriliyordu.
     *
     * ⚠️ Ifadeler WEBDEN alindi (`workspace.json` reviewBadge /
     * acceptedBadge / noSuggestionBadge) -- ayni durum iki uründe ayni
     * cumleyle anlatilsin.
     *
     * `else` artik kodu BASMIYOR: bilinmeyen bir durum gelirse notr bir
     * ifade yaziliyor. Kullaniciya anlamadigi bir dize gostermek,
     * hicbir sey gostermemekten kotudur.
     */
    val analiz = document.analysisStatus?.let {
        when (it) {
            "review_required" -> "Veriler algılandı · onay bekliyor"
            "accepted" -> "Takip kaydı oluşturuldu"
            "rejected" -> "Öneri yoksayıldı"
            "no_suggestion" -> "Metin okundu · takip bilgisi bulunamadı"
            "completed" -> "Analiz edildi"
            "processing" -> "Analiz ediliyor"
            "failed" -> "Analiz başarısız"
            else -> "İnceleniyor"
        }
    }
    LkListRow(
        baslik = document.originalName,
        altBaslik = listOfNotNull(
            analiz,
            document.documentDate?.let { LkDateUtils.formatDate(it) }
        ).joinToString(" · ").ifBlank { null },
        kategori = document.category?.let { documentCategoryLabel(it) },
        ikon = {
            Icon(
                imageVector = Icons.Outlined.AttachFile,
                contentDescription = null,
                tint = if (document.analysisStatus == "failed") LkWarning else LkTileInk,
                modifier = Modifier.size(21.dp)
            )
        },
        sag = {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Sil",
                    tint = LkTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    )

    /*
     * ONAY BEKLEYEN ONERILER — belge satirinin ALTINDA.
     *
     * Yalniz `proposed` cizilyor; kabul edilmis ya da reddedilmis oneri
     * artik bir soru degil (web de ayni suzgeci uyguluyor).
     */
    document.suggestions
        .filter { it.status == "proposed" && it.id != null }
        .forEach { oneri ->
            OneriSatiri(
                oneri = oneri,
                islemde = islenenOneri == oneri.id,
                /* Baska bir oneri islenirken hepsi kilitleniyor: liste
                   yenilenecegi icin ikinci bir istek yarista kalirdi. */
                kilitli = islenenOneri != null,
                onKabul = { onOneriKabul(oneri.id!!) },
                onRet = { onOneriRet(oneri.id!!) }
            )
        }

    /*
     * HESAPLAMA ONERISI — "bu belgeyle hangi hesabi yapabilirim".
     *
     * 🔴 Mobilde HIC YOKTU. Sunucu belgenin metninden hangi finansal
     * modelin calistirilabilecegini ve girdilerin ne kadarinin HAZIR
     * oldugunu hesapliyor; web bunu belge kartinin altinda gosteriyor.
     */
    ModelOnerisiBolumu(
        oneriler = modelOnerileri,
        araniyor = modelAraniyor,
        onAra = onModelAra,
        onModelAc = onModelAc
    )
}

@Composable
private fun ModelOnerisiBolumu(
    oneriler: com.localkarar.app.network.dto.ModelOnerileriDto?,
    araniyor: Boolean,
    onAra: () -> Unit,
    onModelAc: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = LkSpacing.Space5,
                end = LkSpacing.Space4,
                bottom = LkSpacing.Space3
            )
    ) {
        if (oneriler == null) {
            LkButton(
                text = if (araniyor) "Aranıyor..." else "Bu belgeyle hesaplama öner",
                variant = LkButtonVariant.QUIET,
                onClick = onAra,
                enabled = !araniyor,
                modifier = Modifier.fillMaxWidth()
            )
            return@Column
        }

        /*
         * Sunucunun uyarisi VARSA yaziliyor ("okunabilir finansal alan
         * çıkarılamadı" gibi) — bizim uydurdugumuz bir aciklama degil.
         */
        if (!oneriler.warning.isNullOrBlank()) {
            Text(
                text = oneriler.warning,
                style = LkTypography.getMetadata(),
                color = LkTextSecondary
            )
        }

        if (oneriler.models.isEmpty()) return@Column

        Text(
            text = "ÖNERİLEN HESAPLAMALAR",
            style = LkTypography.getMicro(),
            color = LkTextMuted
        )
        Spacer(Modifier.height(LkSpacing.Space2))

        /* Webde de en fazla dort tane gosteriliyor. */
        oneriler.models.take(4).forEach { model ->
            LkListRow(
                baslik = model.name ?: model.code,
                /*
                 * "Verinin %62'si hazır · 3 alan eksik" — kullanici
                 * modeli acmadan once ne kadar is kaldigini biliyor.
                 * Eksik alan yoksa o parca hic yazilmiyor.
                 */
                /*
                 * ⚠️ SAYIYA EK GETIRILMIYOR: "%100'si" YANLIS (dogrusu
                 * "%100'ü"), "%50'si" DOGRU. Ek sayinin okunusuna gore
                 * degisiyor (yüz→ü, elli→si, yetmiş→i) ve her sayi icin
                 * dogru eki uretmek ayri bir is. Ifadeyi eksiz kurmak
                 * hem dogru hem kisa.
                 */
                altBaslik = listOfNotNull(
                    "%${(model.coverage * 100).toInt()} hazır",
                    model.missingFields.size
                        .takeIf { it > 0 }
                        ?.let { "$it alan eksik" }
                ).joinToString(" · "),
                onClick = { onModelAc(model.code) }
            )
        }
    }
}

/**
 * Sunucunun belgeden cikardigi kayit onerisi.
 *
 * 🔴 MOBILDE HIC GORUNMUYORDU. Sunucu faturayi okuyup "bundan su kaydi
 * acayim mi" diye soruyordu; mobil yalniz "Analiz edildi" rozetini
 * cizip oneriyi yutuyordu.
 *
 * ⚠️ HICBIR SAYI UYDURULMUYOR. `amount` ve `dueAt` sunucuda bilerek
 * null olabiliyor (e-Fatura orneklerinin cogunda vade yok); burada da
 * yoksa YAZILMIYOR. Sifir ya da bugunun tarihini basmak, okunmamis bir
 * bilgiyi okunmus gibi gostermek olurdu.
 */
@Composable
private fun OneriSatiri(
    oneri: com.localkarar.app.network.dto.DocumentSuggestionDto,
    islemde: Boolean,
    kilitli: Boolean,
    onKabul: () -> Unit,
    onRet: () -> Unit
) {
    val p = oneri.payload
    val yonBelirsiz = p?.direction == "neutral"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = LkSpacing.Space5,
                end = LkSpacing.Space4,
                bottom = LkSpacing.Space3
            )
    ) {
        Text(
            text = "BU BELGEDEN KAYIT ÖNERİSİ",
            style = LkTypography.getMicro(),
            color = LkTextMuted
        )
        Spacer(Modifier.height(LkSpacing.Space1))

        Text(
            text = p?.title ?: oneri.title ?: "Kayıt önerisi",
            style = LkTypography.getBody(),
            color = LkTextPrimary
        )

        /*
         * Ozet satiri: tur · tutar · vade. Her parca YALNIZ varsa
         * ekleniyor, bosluk birakilmiyor.
         */
        val ozet = listOfNotNull(
            if (yonBelirsiz) "Yönü belirsiz" else p?.type?.let { kayitTuruEtiketi(it) },
            /* `formatMoney` para birimi SIMGESINI kendisi koyuyor —
               ayrica currency yazmak "₺1.250 TRY" uretirdi. */
            p?.amount?.let { tutar -> LkFormatting.formatMoney(tutar, p.currency) },
            p?.dueAt?.let { "Vade ${LkDateUtils.formatDate(it)}" }
        ).joinToString(" · ")

        if (ozet.isNotBlank()) {
            Spacer(Modifier.height(LkSpacing.Space1))
            Text(ozet, style = LkTypography.getMetadata(), color = LkTextSecondary)
        }

        /*
         * Yonu belirsiz oneride sunucunun ACIKLAMASI gosteriliyor: orada
         * kullanicinin ne yapabilecegi yaziyor ("işletme ayarlarında
         * vergi numaranızı girerseniz otomatik ayrılır"). Webde de boyle.
         */
        if (yonBelirsiz && !p?.description.isNullOrBlank()) {
            Spacer(Modifier.height(LkSpacing.Space2))
            Text(p!!.description!!, style = LkTypography.getMetadata(), color = LkTextSecondary)
        }

        /* Guven olcusu — YOKSA HIC YAZILMIYOR. */
        oneri.confidence?.let { guven ->
            Spacer(Modifier.height(LkSpacing.Space1))
            Text(
                text = "Sunucu güveni: %${(guven * 100).toInt()}",
                style = LkTypography.getMicro(),
                color = LkTextMuted
            )
        }

        Spacer(Modifier.height(LkSpacing.Space3))
        Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
            LkButton(
                text = if (islemde) "Oluşturuluyor..." else "Kayıt oluştur",
                onClick = onKabul,
                enabled = !kilitli,
                modifier = Modifier.weight(1f)
            )
            LkButton(
                text = "Yoksay",
                variant = LkButtonVariant.SECONDARY,
                onClick = onRet,
                enabled = !kilitli,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** Sunucunun kayit turu kodlari — `RecordSuggestionPayload.type`. */
private fun kayitTuruEtiketi(tur: String): String = when (tur) {
    "payment" -> "Ödeme"
    "receivable" -> "Tahsilat"
    "promissory_note" -> "Senet"
    "purchase" -> "Alış"
    "shipment" -> "Sevkiyat"
    else -> tur
}

fun documentCategoryLabel(category: String): String {
    return when (category) {
        "invoice" -> "Fatura"
        "receipt" -> "Fiş"
        "contract" -> "Sözleşme"
        "promissory_note" -> "Senet"
        "shipment" -> "Sevkiyat"
        "purchase" -> "Satın Alma"
        else -> "Diğer"
    }
}

/** Web ile ayni yedi tur; sunucu enum'u (business-tracker.ts:53) da bu. */
private val BELGE_TURLERI = listOf(
    null to "Otomatik",
    "invoice" to "Fatura",
    "receipt" to "Makbuz",
    "contract" to "Sözleşme",
    "promissory_note" to "Senet",
    "shipment" to "Sevkiyat",
    "purchase" to "Alış",
    "other" to "Diğer"
)

/**
 * Yukleme paneli — webdeki "Belge veya fotoğraf ekleyin" kartinin
 * mobil karsiligi.
 *
 * "Otomatik" VARSAYILAN ve ilk sirada: sunucu e-Fatura XML'ini kendisi
 * cozumluyor ve turu buluyor. Tur secimi kullaniciyi zorlayan bir adim
 * degil, gerektiginde duzelten bir secenek.
 */
@Composable
private fun BelgeYuklemePaneli(
    seciliTur: String?,
    onTur: (String?) -> Unit,
    onDosyaSec: () -> Unit,
    onFotografCek: (() -> Unit)?,
    onEposta: () -> Unit,
    onKapat: () -> Unit
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
                verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
            ) {
                Text(
                    text = "Belge ekle",
                    style = LkTypography.getSectionTitle(),
                    color = LkTextPrimary
                )
                Text(
                    text = "BELGE TÜRÜ",
                    style = LkTypography.getMicro(),
                    color = LkTextSecondary
                )
                Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                    BELGE_TURLERI.chunked(3).forEach { satir ->
                        Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                            satir.forEach { (deger, etiket) ->
                                LkChip(
                                    text = etiket,
                                    selected = seciliTur == deger,
                                    onClick = { onTur(deger) }
                                )
                            }
                        }
                    }
                }

                /*
                 * "Fotoğraf çek" EN USTTE: mobilde faturanin fotografini
                 * cekmek, dosya secmekten daha sik yapilan is. Platformda
                 * kamera yolu yoksa (`null`) dugme HIC cizilmiyor.
                 */
                if (onFotografCek != null) {
                    LkButton(
                        text = "Fotoğraf çek",
                        onClick = onFotografCek,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                LkButton(
                    text = "Dosya veya fotoğraf seç",
                    variant = if (onFotografCek != null) LkButtonVariant.SECONDARY
                    else LkButtonVariant.PRIMARY,
                    onClick = onDosyaSec,
                    modifier = Modifier.fillMaxWidth()
                )
                LkButton(
                    text = "E-posta ile gönder",
                    variant = LkButtonVariant.SECONDARY,
                    onClick = onEposta,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "PDF, DOCX, XLSX, PNG, JPEG, CSV, JSON, XML, TXT ve MD desteklenir. " +
                        "e-Fatura XML dosyaları otomatik çözümlenir; fotoğraflar cihazda " +
                        "Türkçe OCR ile okunur ve hiçbir kayıt siz onaylamadan kesinleşmez.",
                    style = LkTypography.getMicro(),
                    color = LkTextMuted
                )

                LkButton(
                    text = "Vazgeç",
                    variant = LkButtonVariant.GHOST,
                    onClick = onKapat,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
