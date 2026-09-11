package com.localkarar.app.ui.screens.workspaces

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.localkarar.app.network.dto.UpdateWorkspaceRequestDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkChip
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkHairline
import com.localkarar.app.ui.components.LkHeroPage
import com.localkarar.app.ui.components.LkListRow
import com.localkarar.app.ui.components.LkLoadingDesen
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkNotice
import com.localkarar.app.ui.components.LkNumericField
import com.localkarar.app.ui.components.LkRowGroup
import com.localkarar.app.ui.components.LkSectionHeader
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.WorkspaceSettingsUiState
import com.localkarar.app.workspaces.WorkspaceSettingsViewModel

private val CURRENCY_OPTIONS = listOf("TRY", "USD", "EUR", "GBP")

private fun currencyLabel(code: String): String = when (code) {
    "TRY" -> "Türk lirası (₺)"
    "USD" -> "Amerikan doları (\$)"
    "EUR" -> "Euro (€)"
    "GBP" -> "İngiliz sterlini (£)"
    else -> code
}

/** Webdeki `settings.stage.*` ile birebir ayni liste ve sira. */
private val STAGE_OPTIONS = listOf(
    "idea" to "Fikir aşaması",
    "startup" to "Yeni kuruldu",
    "growth" to "Büyüme aşaması",
    "established" to "Yerleşik işletme",
    "transformation" to "Dönüşüm aşaması"
)

private val TIMEZONE_OPTIONS = listOf(
    "Europe/Istanbul" to "İstanbul (GMT+3)",
    "Europe/London" to "Londra (GMT+0/+1)",
    "America/New_York" to "New York (GMT-5/-4)"
)

private val LOCALE_OPTIONS = listOf(
    "tr-TR" to "Türkçe (Türkiye)",
    "en-US" to "İngilizce biçim (ABD)"
)

/**
 * ISLETME AYARLARI — mockup "Takip 14".
 *
 * 🔴 EKRAN WEBIN UC KARTINDAN BIRINI BILE TAM KARSILAMIYORDU.
 *
 * Webde (`Settings.jsx`) uc bolum var:
 *   1. Isletme profili — ad, unvan, vergi no, sektor, sehir, asama,
 *      calisan sayisi, satis kanallari, hedef, zorluklar + finansal ozet
 *   2. e-Fatura gelen kutusu — adres ac/yenile/kapat, guvenilir gonderenler
 *   3. Calisma alani tercihleri — saat dilimi, bicim, para birimi, hafta basi
 *
 * Mobilde YALNIZ isletme adi ve para birimi vardi; saat dilimi ile dil
 * "Değişiklik web sürümünden yapılır" notuyla salt okunur duruyordu, gelen
 * kutusu hic yoktu. Uc bolum de artik burada.
 *
 * ⚠️ Isletme profili KOZMETIK DEGIL: mentor onerileri ve isletme takibi
 * bu alanlara gore kisisellestiriliyor (webin kendi aciklamasi).
 */
@Composable
fun WorkspaceSettingsScreen(
    viewModel: WorkspaceSettingsViewModel,
    onBack: () -> Unit,
    onDeleted: () -> Unit = onBack,
    financeContent: @Composable () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var actionError by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }
    var silmeOnayi by remember { mutableStateOf(false) }
    var kutuKapatmaOnayi by remember { mutableStateOf(false) }

    LkHeroPage(title = "İşletme ayarları", onBack = onBack) {
        when (val state = uiState) {
            is WorkspaceSettingsUiState.Loading -> LkLoadingState(desen = LkLoadingDesen.FORM)
            is WorkspaceSettingsUiState.Error -> LkErrorState(
                message = state.message,
                onRetry = { viewModel.load() }
            )
            is WorkspaceSettingsUiState.Content -> {
                val isletme = state.workspace

                /*
                 * Taslak durum ISLETME KIMLIGINE bagli hatirlaniyor:
                 * kaydettikten sonra sunucudan donen nesne degisiyor ve
                 * `remember(isletme)` yazilsaydi kullanicinin yazdiklari
                 * kayit aninda sifirlanirdi.
                 */
                val anahtar = isletme?.id ?: state.settings.id
                var ad by remember(anahtar) { mutableStateOf(isletme?.name.orEmpty()) }
                var unvan by remember(anahtar) { mutableStateOf(isletme?.legalName.orEmpty()) }
                var vergiNo by remember(anahtar) { mutableStateOf(isletme?.taxNumber.orEmpty()) }
                var sektor by remember(anahtar) { mutableStateOf(isletme?.sector.orEmpty()) }
                var sehir by remember(anahtar) { mutableStateOf(isletme?.city.orEmpty()) }
                var asama by remember(anahtar) { mutableStateOf(isletme?.businessStage) }
                var calisan by remember(anahtar) {
                    mutableStateOf(isletme?.employeeCount?.toString().orEmpty())
                }
                var kanallar by remember(anahtar) {
                    mutableStateOf(isletme?.salesChannels?.joinToString(", ").orEmpty())
                }
                var hedef by remember(anahtar) { mutableStateOf(isletme?.primaryGoal.orEmpty()) }
                var zorluklar by remember(anahtar) {
                    mutableStateOf(isletme?.challenges?.joinToString(", ").orEmpty())
                }
                var aylikSatis by remember(anahtar) { mutableStateOf(sayiMetni(isletme?.monthlySales)) }
                var aylikGider by remember(anahtar) { mutableStateOf(sayiMetni(isletme?.monthlyExpenses)) }
                var nakit by remember(anahtar) { mutableStateOf(sayiMetni(isletme?.cashBalance)) }
                var borc by remember(anahtar) { mutableStateOf(sayiMetni(isletme?.debtBalance)) }

                var currency by remember(anahtar) { mutableStateOf(state.settings.defaultCurrency) }
                var timezone by remember(anahtar) { mutableStateOf(state.settings.timezone) }
                var locale by remember(anahtar) { mutableStateOf(state.settings.locale) }
                var haftaBasi by remember(anahtar) { mutableStateOf(state.settings.weekStartsOn) }

                var yeniGonderen by remember { mutableStateOf("") }
                var yeniGonderenEtiket by remember { mutableStateOf("") }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = LkSpacing.Space4,
                        end = LkSpacing.Space4,
                        top = LkSpacing.Space4,
                        bottom = LkSpacing.Space10
                    ),
                    verticalArrangement = Arrangement.spacedBy(LkSpacing.Space5)
                ) {
                    if (actionError != null || notice != null) {
                        item {
                            LkNotice(
                                metin = actionError ?: notice.orEmpty(),
                                hataMi = actionError != null,
                                onKapat = { actionError = null; notice = null }
                            )
                        }
                    }

                    // ---------------------------------------------- PROFIL
                    if (isletme != null) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)) {
                                LkSectionHeader(title = "İŞLETME PROFİLİ")
                                Text(
                                    text = "Mentor önerileri ve işletme takibi bu bilgilerle " +
                                        "kişiselleştirilir.",
                                    style = LkTypography.getBodySmall(),
                                    color = LkTextSecondary
                                )

                                LkTextField(
                                    value = ad,
                                    onValueChange = { ad = it },
                                    label = "İşletme adı"
                                )
                                LkTextField(
                                    value = unvan,
                                    onValueChange = { unvan = it },
                                    label = "Resmî unvan"
                                )
                                /*
                                 * ⚠️ YALNIZ RAKAM. Sunucu 10 ya da 11 hane
                                 * bekliyor; bosluk ve nokta girilmis bir
                                 * numara sunucuda reddedilirdi ve kullanici
                                 * sebebini goremezdi.
                                 */
                                LkTextField(
                                    value = vergiNo,
                                    onValueChange = { yeni ->
                                        vergiNo = yeni.filter { it.isDigit() }.take(11)
                                    },
                                    label = "Vergi / TC kimlik no",
                                    placeholder = "10 haneli VKN ya da 11 haneli TCKN",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                Text(
                                    text = "e-Fatura yüklerken gelen/giden ayrımı bu numaradan yapılır.",
                                    style = LkTypography.getMicro(),
                                    color = LkTextMuted
                                )
                                LkTextField(
                                    value = sektor,
                                    onValueChange = { sektor = it },
                                    label = "Sektör",
                                    placeholder = "Örn. E-ticaret, tekstil"
                                )
                                LkTextField(
                                    value = sehir,
                                    onValueChange = { sehir = it },
                                    label = "Şehir"
                                )

                                Text(
                                    text = "İŞLETME AŞAMASI",
                                    style = LkTypography.getMicro(),
                                    color = LkTextSecondary
                                )
                                SecimSeridi(
                                    secenekler = STAGE_OPTIONS,
                                    secili = asama,
                                    onSecim = { asama = if (asama == it) null else it }
                                )

                                LkTextField(
                                    value = calisan,
                                    onValueChange = { yeni -> calisan = yeni.filter { it.isDigit() } },
                                    label = "Çalışan sayısı",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                LkTextField(
                                    value = kanallar,
                                    onValueChange = { kanallar = it },
                                    label = "Satış kanalları",
                                    placeholder = "Mağaza, web sitesi, Trendyol, Instagram"
                                )
                                Text(
                                    text = "Birden fazla kanalı virgülle ayırın.",
                                    style = LkTypography.getMicro(),
                                    color = LkTextMuted
                                )
                                LkTextField(
                                    value = hedef,
                                    onValueChange = { hedef = it },
                                    label = "Öncelikli hedef",
                                    placeholder = "Örn. E-ticaret satışlarını artırmak"
                                )
                                LkTextField(
                                    value = zorluklar,
                                    onValueChange = { zorluklar = it },
                                    label = "Temel zorluklar",
                                    placeholder = "Nakit akışı, müşteri bulma, kargo maliyeti"
                                )
                                Text(
                                    text = "Birden fazla konuyu virgülle ayırın.",
                                    style = LkTypography.getMicro(),
                                    color = LkTextMuted
                                )
                            }
                        }

                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)) {
                                LkSectionHeader(title = "FİNANSAL ÖZET")
                                LkNumericField(
                                    value = aylikSatis,
                                    onValueChange = { aylikSatis = it },
                                    label = "Aylık satış",
                                    suffix = currency
                                )
                                LkNumericField(
                                    value = aylikGider,
                                    onValueChange = { aylikGider = it },
                                    label = "Aylık gider",
                                    suffix = currency
                                )
                                LkNumericField(
                                    value = nakit,
                                    onValueChange = { nakit = it },
                                    label = "Nakit bakiyesi",
                                    suffix = currency
                                )
                                LkNumericField(
                                    value = borc,
                                    onValueChange = { borc = it },
                                    label = "Borç bakiyesi",
                                    suffix = currency
                                )
                                LkButton(
                                    text = if (state.isSaving) "Kaydediliyor…" else "İşletme bilgilerini kaydet",
                                    enabled = !state.isSaving && ad.isNotBlank(),
                                    onClick = {
                                        actionError = null
                                        notice = null
                                        viewModel.saveProfile(
                                            body = profilIstegi(
                                                ad, unvan, vergiNo, sektor, sehir, asama,
                                                calisan, kanallar, hedef, zorluklar,
                                                aylikSatis, aylikGider, nakit, borc
                                            ),
                                            onError = { actionError = it },
                                            onSaved = { notice = "İşletme bilgileri kaydedildi." }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // ----------------------------------------- GELEN KUTUSU
                    val kutu = state.inbox
                    if (kutu != null) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)) {
                                LkSectionHeader(title = "e-FATURA GELEN KUTUSU")
                                Text(
                                    text = "Muhasebe programınızdan faturaları bu adrese gönderin; " +
                                        "onay bekleyen kayıt olarak düşsün.",
                                    style = LkTypography.getBodySmall(),
                                    color = LkTextSecondary
                                )

                                /*
                                 * ⚠️ KANAL HAZIR DEGILSE ONCE BU SOYLENIYOR:
                                 * adres uretilse bile gelen posta islenmez.
                                 * Webde de ayni uyari var.
                                 */
                                if (!kutu.kanalHazir) {
                                    Text(
                                        text = "Bu özellik sunucuda henüz yapılandırılmadı. " +
                                            "Adres oluştursanız da gelen posta işlenmez.",
                                        style = LkTypography.getBodySmall(),
                                        color = LkWarning
                                    )
                                }

                                if (kutu.acik && !kutu.adres.isNullOrBlank()) {
                                    AdresSatiri(kutu.adres)
                                    Text(
                                        text = "Bu adrese yalnız işletmenin üyeleri ve aşağıdaki " +
                                            "güvenilir gönderenler yazabilir; başka adreslerden " +
                                            "gelen posta sessizce atılır. Adres sızarsa yenileyin.",
                                        style = LkTypography.getMicro(),
                                        color = LkTextMuted
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                                        LkButton(
                                            text = "Adresi yenile",
                                            variant = LkButtonVariant.SECONDARY,
                                            enabled = !state.isInboxBusy,
                                            onClick = {
                                                actionError = null
                                                viewModel.inboxAcVeyaYenile { actionError = it }
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                        LkButton(
                                            text = "Kapat",
                                            variant = LkButtonVariant.SECONDARY,
                                            enabled = !state.isInboxBusy,
                                            onClick = { kutuKapatmaOnayi = true },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                } else {
                                    LkButton(
                                        text = if (state.isInboxBusy) "Hazırlanıyor…" else "Gelen kutusunu aç",
                                        enabled = !state.isInboxBusy,
                                        onClick = {
                                            actionError = null
                                            viewModel.inboxAcVeyaYenile { actionError = it }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        if (kutu.acik) {
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)) {
                                    LkSectionHeader(title = "GÜVENİLİR GÖNDERENLER")
                                    Text(
                                        text = "Faturayı düzenleyen adresi ekleyin — yönlendirilen " +
                                            "postada gönderen siz değil, faturayı kesen görünür. " +
                                            "Liste boşken yalnız üyeler gönderebilir.",
                                        style = LkTypography.getBodySmall(),
                                        color = LkTextSecondary
                                    )

                                    if (state.inboxSenders.isEmpty()) {
                                        Text(
                                            text = "Henüz güvenilir gönderen eklenmedi.",
                                            style = LkTypography.getBodySmall(),
                                            color = LkTextMuted
                                        )
                                    } else {
                                        LkRowGroup {
                                            state.inboxSenders.forEachIndexed { index, gonderen ->
                                                if (index > 0) LkHairline()
                                                LkListRow(
                                                    baslik = gonderen.email,
                                                    altBaslik = gonderen.label,
                                                    sag = {
                                                        IconButton(
                                                            onClick = {
                                                                actionError = null
                                                                viewModel.gonderenCikar(gonderen.id) {
                                                                    actionError = it
                                                                }
                                                            }
                                                        ) {
                                                            Icon(
                                                                Icons.Outlined.Close,
                                                                contentDescription = "Gönderen çıkar",
                                                                tint = LkTextMuted,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    LkTextField(
                                        value = yeniGonderen,
                                        onValueChange = { yeniGonderen = it },
                                        label = "Güvenilir gönderen e-posta adresi",
                                        placeholder = "fatura@tedarikci.com",
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                                    )
                                    LkTextField(
                                        value = yeniGonderenEtiket,
                                        onValueChange = { yeniGonderenEtiket = it },
                                        label = "Açıklama (isteğe bağlı)"
                                    )
                                    LkButton(
                                        text = "Ekle",
                                        variant = LkButtonVariant.SECONDARY,
                                        enabled = !state.isInboxBusy && yeniGonderen.isNotBlank(),
                                        onClick = {
                                            actionError = null
                                            viewModel.gonderenEkle(
                                                yeniGonderen,
                                                yeniGonderenEtiket
                                            ) { actionError = it }
                                            yeniGonderen = ""
                                            yeniGonderenEtiket = ""
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    // ------------------------------------------- TERCIHLER
                    item { financeContent() }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)) {
                            LkSectionHeader(title = "ÇALIŞMA ALANI TERCİHLERİ")

                            Text("SAAT DİLİMİ", style = LkTypography.getMicro(), color = LkTextSecondary)
                            SecimSeridi(
                                secenekler = TIMEZONE_OPTIONS,
                                secili = timezone,
                                onSecim = { timezone = it }
                            )

                            Text(
                                "TARİH VE SAYI BİÇİMİ",
                                style = LkTypography.getMicro(),
                                color = LkTextSecondary
                            )
                            SecimSeridi(
                                secenekler = LOCALE_OPTIONS,
                                secili = locale,
                                onSecim = { locale = it }
                            )
                            Text(
                                text = "Bu seçenek yalnız tarih ve sayı gösterimini değiştirir; " +
                                    "arayüz dili Türkçedir.",
                                style = LkTypography.getMicro(),
                                color = LkTextMuted
                            )

                            Text("PARA BİRİMİ", style = LkTypography.getMicro(), color = LkTextSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                                CURRENCY_OPTIONS.forEach { option ->
                                    LkChip(
                                        text = option,
                                        background = if (currency == option) LkPrimary else LkSurfaceRaised,
                                        contentColor = if (currency == option) LkOnPrimary else LkTextSecondary,
                                        modifier = Modifier.clickable { currency = option }
                                    )
                                }
                            }
                            Text(
                                text = currencyLabel(currency),
                                style = LkTypography.getMetadata(),
                                color = LkTextMuted
                            )

                            Text(
                                "HAFTA BAŞLANGICI",
                                style = LkTypography.getMicro(),
                                color = LkTextSecondary
                            )
                            SecimSeridi(
                                secenekler = listOf("1" to "Pazartesi", "0" to "Pazar"),
                                secili = haftaBasi.toString(),
                                onSecim = { haftaBasi = it.toIntOrNull() ?: 1 }
                            )

                            LkButton(
                                text = if (state.isSaving) "Kaydediliyor…" else "Tercihleri kaydet",
                                onClick = {
                                    actionError = null
                                    notice = null
                                    viewModel.save(
                                        defaultCurrency = currency,
                                        timezone = timezone,
                                        locale = locale,
                                        weekStartsOn = haftaBasi,
                                        onError = { actionError = it }
                                    )
                                    notice = "Çalışma alanı tercihleri kaydedildi."
                                },
                                enabled = !state.isSaving,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    /*
                     * TEHLIKELI ALAN. Digerlerinden bosluk ve baslikla
                     * ayriliyor; yanlislikla dokunulacak bir yerde degil.
                     */
                    if (isletme != null) {
                        item {
                            Spacer(modifier = Modifier.height(LkSpacing.Space6))
                            Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                                LkSectionHeader(title = "TEHLİKELİ ALAN")
                                LkRowGroup {
                                    LkListRow(
                                        baslik = "İşletmeyi sil",
                                        altBaslik = "Kayıtlar, ürünler, belgeler ve " +
                                            "${isletme.memberCount} üye erişimi silinir",
                                        tutarRengi = LkDanger,
                                        onClick = { silmeOnayi = true }
                                    )
                                }
                            }
                        }
                    }
                }

                if (kutuKapatmaOnayi) {
                    LkOnayDialogu(
                        baslik = "Gelen kutusunu kapat",
                        metin = "Adres kapatılacak; bu adrese gönderilen postalar artık " +
                            "işlenmeyecek.",
                        onaylaEtiketi = "Kapat",
                        onOnayla = {
                            kutuKapatmaOnayi = false
                            actionError = null
                            viewModel.inboxKapat { actionError = it }
                        },
                        onVazgec = { kutuKapatmaOnayi = false }
                    )
                }

                if (silmeOnayi && isletme != null) {
                    LkOnayDialogu(
                        baslik = "İşletmeyi sil",
                        metin = "\"${isletme.name}\" işletmesi ve ona bağlı bütün kayıtlar, " +
                            "ürünler, siparişler ve belgeler kalıcı olarak silinir. " +
                            "Bu işlem geri alınamaz.",
                        onaylaEtiketi = "Kalıcı olarak sil",
                        onOnayla = {
                            silmeOnayi = false
                            actionError = null
                            viewModel.delete(onDeleted = onDeleted, onError = { actionError = it })
                        },
                        onVazgec = { silmeOnayi = false }
                    )
                }
            }
        }
    }
}

/**
 * Gelen kutusu adresi.
 *
 * Kopyalanabilir olmasi sart: adres rastgele uretiliyor, elle yazilmasi
 * beklenemez ve kullanicinin onu muhasebe programina tasimasi gerekiyor.
 */
@Composable
private fun AdresSatiri(adres: String) {
    val pano = LocalClipboardManager.current
    LkRowGroup {
        LkListRow(
            baslik = adres,
            altBaslik = "Gelen kutusu adresi",
            onClick = { pano.setText(AnnotatedString(adres)) },
            sag = {
                Icon(
                    Icons.Outlined.ContentCopy,
                    contentDescription = "Adresi kopyala",
                    tint = LkTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
    }
}

/** Deger/etiket ciftlerinden kurulu tek secimli hap seridi. */
@Composable
private fun SecimSeridi(
    secenekler: List<Pair<String, String>>,
    secili: String?,
    onSecim: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
        secenekler.chunked(2).forEach { satir ->
            Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                satir.forEach { (deger, etiket) ->
                    LkChip(
                        text = etiket,
                        background = if (secili == deger) LkPrimary else LkSurfaceRaised,
                        contentColor = if (secili == deger) LkOnPrimary else LkTextSecondary,
                        modifier = Modifier.clickable { onSecim(deger) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LkOnayDialogu(
    baslik: String,
    metin: String,
    onaylaEtiketi: String,
    onOnayla: () -> Unit,
    onVazgec: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onVazgec,
        title = { Text(baslik, style = LkTypography.getCardTitle(), color = LkTextPrimary) },
        text = { Text(metin, style = LkTypography.getBodySmall(), color = LkTextSecondary) },
        confirmButton = {
            TextButton(onClick = onOnayla) { Text(onaylaEtiketi, color = LkDanger) }
        },
        dismissButton = {
            TextButton(onClick = onVazgec) { Text("Vazgeç", color = LkTextSecondary) }
        },
        backgroundColor = LkSurfaceRaised
    )
}

private fun sayiMetni(deger: Double?): String = when {
    deger == null -> ""
    deger == deger.toLong().toDouble() -> deger.toLong().toString()
    else -> deger.toString()
}

/**
 * Form alanlarindan sunucu istegi kurar.
 *
 * ⚠️ Bos birakilan METIN alanlari `null` gonderiliyor, "" DEGIL: sunucu
 * `legalName`i nullable kabul ediyor ama `sector`/`city` icin bos metin
 * anlamli bir deger olur ve kullanicinin doldurdugunu silmis gibi
 * gorunurdu. Sayi alanlarinda ayni sey: bos = "dokunma".
 */
private fun profilIstegi(
    ad: String,
    unvan: String,
    vergiNo: String,
    sektor: String,
    sehir: String,
    asama: String?,
    calisan: String,
    kanallar: String,
    hedef: String,
    zorluklar: String,
    aylikSatis: String,
    aylikGider: String,
    nakit: String,
    borc: String
): UpdateWorkspaceRequestDto = UpdateWorkspaceRequestDto(
    name = ad.trim().ifBlank { null },
    legalName = unvan.trim(),
    taxNumber = vergiNo.trim().ifBlank { null },
    sector = sektor.trim().ifBlank { null },
    city = sehir.trim().ifBlank { null },
    businessStage = asama,
    employeeCount = calisan.trim().toIntOrNull(),
    salesChannels = listeyeCevir(kanallar),
    primaryGoal = hedef.trim().ifBlank { null },
    challenges = listeyeCevir(zorluklar),
    monthlySales = ondalik(aylikSatis),
    monthlyExpenses = ondalik(aylikGider),
    cashBalance = ondalik(nakit),
    debtBalance = ondalik(borc)
)

private fun listeyeCevir(ham: String): List<String> =
    ham.split(',').map { it.trim() }.filter { it.isNotEmpty() }

/** Virgullu yazim da kabul ediliyor: Turkce klavyede ondalik ayraci virgul. */
private fun ondalik(ham: String): Double? =
    ham.trim().replace(',', '.').toDoubleOrNull()
