package com.localkarar.app.ui.screens.workspaces

import com.localkarar.app.ui.components.LkHeroPage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.core.SecureScreen
import com.localkarar.app.network.dto.IntegrationConnectionDto
import com.localkarar.app.network.dto.MarketplaceEntryDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkCard
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkChip
import com.localkarar.app.ui.components.LkEmptyState
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkPasswordTextField
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.IntegrationsUiState
import com.localkarar.app.workspaces.IntegrationsViewModel

/**
 * Pazaryeri entegrasyonlari.
 *
 * Sunucuda dort saglayici icin tam yasam dongusu (baglan/durum/esitle/kes)
 * ZATEN hazirdi; eksik olan mobil yuzeydi. O eksiklik yuzunden kullanici
 * mobilden hicbir pazaryeri baglayamiyor, Siparisler ve Urunler ekranlarina
 * gercek veri hicbir zaman gelmiyordu -- ve uydurma veri o boslugu
 * "makul" gorunerek dolduruyordu.
 */
@Composable
fun IntegrationsScreen(
    workspaceId: String,
    viewModel: IntegrationsViewModel,
    onNavigateBack: () -> Unit
) {
    // Pazaryeri kimlik bilgisi (API secret, parola) giriliyor.
    SecureScreen()
    val uiState by viewModel.uiState.collectAsState()
    val islemDevamEdiyor by viewModel.islemDevamEdiyor.collectAsState()

    LaunchedEffect(workspaceId) { viewModel.yukle(workspaceId) }

    /* §0: ham Scaffold + TopAppBar yerine hero kabugu. */
    LkHeroPage(
        title = "Entegrasyonlar",
        onBack = onNavigateBack,
        heroExtra = {
            val content = uiState as? IntegrationsUiState.Content
            Text(
                text = content?.let {
                    "${it.katalog.size} bağlantı · ${it.baglantilar.count { connection -> connection.status == "ACTIVE" }} çalışıyor"
                } ?: "Bağlantı, eşitleme ve durum",
                style = LkTypography.getMetadata(),
                color = LkHero.OnHeroSecondary,
                modifier = Modifier.padding(
                    start = LkSpacing.Space5,
                    top = LkSpacing.Space2
                )
            )
        }
    ) {
        when (val durum = uiState) {
            is IntegrationsUiState.Loading -> LkLoadingState(modifier = Modifier)

            is IntegrationsUiState.Error -> LkErrorState(
                message = durum.mesaj,
                onRetry = { viewModel.yukle(workspaceId) },
                modifier = Modifier,
                hata = durum.hata
            )

            is IntegrationsUiState.Content -> {
                if (durum.katalog.isEmpty()) {
                    LkEmptyState(
                        title = "Pazaryeri bulunamadı",
                        description = "Sunucu şu an bağlanabilecek bir pazaryeri bildirmiyor.",
                        modifier = Modifier
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            
                            .verticalScroll(rememberScrollState())
                            .padding(LkSpacing.Space4),
                        verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
                    ) {
                        durum.katalog.forEach { saglayici ->
                            SaglayiciKarti(
                                saglayici = saglayici,
                                baglanti = durum.baglantilar.firstOrNull {
                                    it.provider == saglayici.provider
                                },
                                islemDevamEdiyor = islemDevamEdiyor,
                                workspaceId = workspaceId,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SaglayiciKarti(
    saglayici: MarketplaceEntryDto,
    baglanti: IntegrationConnectionDto?,
    islemDevamEdiyor: Boolean,
    workspaceId: String,
    viewModel: IntegrationsViewModel
) {
    var formAcik by remember { mutableStateOf(false) }

    val bagli = baglanti != null && baglanti.status == "ACTIVE"

    LkCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SaglayiciIsareti(saglayici.provider, saglayici.label)
            Spacer(modifier = Modifier.width(LkSpacing.Space3))
            /*
             * Baglanti durumu NOKTAYLA da soyleniyor — mockup'ta bagli
             * saglayici yesil nokta tasiyor. Nokta tek basina kalmiyor,
             * yanindaki hap ayni seyi kelimeyle de yaziyor (§19: durum
             * yalniz renge yaslanmaz).
             */
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        when {
                            saglayici.comingSoon -> LkTextMuted
                            bagli -> LkSuccess
                            baglanti != null -> LkWarning
                            else -> LkLineStrong
                        },
                        CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(LkSpacing.Space3))
            Text(
                text = saglayici.label,
                style = LkTypography.getCardTitle(),
                color = LkTextPrimary,
                modifier = Modifier.weight(1f)
            )
            // Amazon icin comingSoon = true: SP-API gelistirici onayi olmadan
            // gercek bagdastirici yazilamiyor. Kart yalnizca varligi bildiriyor.
            when {
                saglayici.comingSoon -> LkChip(text = "Yakında")
                baglanti != null && baglanti.status == "ACTIVE" ->
                    LkChip(text = "Bağlı", background = LkSurfaceSignature, contentColor = LkOnSignature)
                baglanti != null -> LkChip(text = baglanti.status)
                else -> LkChip(text = "Bağlı değil")
            }
        }

        if (baglanti?.lastSyncedAt != null) {
            Spacer(modifier = Modifier.height(LkSpacing.Space1))
            /*
             * 🔴 HAM ISO DAMGASI BASILIYORDU: "Son eşitleme:
             * 2026-09-05T09:41:12.482Z". Mockup "2 dk önce eşitlendi"
             * diyor ve dogru olan da bu — kullanicinin sordugu sey
             * damganin kendisi degil, uzerinden ne kadar gectigi.
             */
            Text(
                text = LkDateUtils.formatTimeAgo(baglanti.lastSyncedAt) + " eşitlendi",
                style = LkTypography.getMetadata(),
                color = LkTextMuted
            )
        }

        if (saglayici.comingSoon || !saglayici.enabled) return@LkCard

        Spacer(modifier = Modifier.height(LkSpacing.Space3))

        if (baglanti != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                LkButton(
                    text = "Şimdi eşitle",
                    onClick = { viewModel.simdiEsitle(workspaceId, saglayici.provider) },
                    enabled = !islemDevamEdiyor
                )
                LkButton(
                    text = "Bağlantıyı kes",
                    onClick = { viewModel.baglantiyiKes(workspaceId, saglayici.provider) },
                    variant = LkButtonVariant.SECONDARY,
                    enabled = !islemDevamEdiyor
                )
            }
        } else if (!formAcik) {
            LkButton(text = "Bağla", onClick = { formAcik = true }, enabled = !islemDevamEdiyor)
        } else {
            BaglantiFormu(
                provider = saglayici.provider,
                islemDevamEdiyor = islemDevamEdiyor,
                workspaceId = workspaceId,
                viewModel = viewModel,
                onVazgec = { formAcik = false }
            )
        }
    }
}

/**
 * Kimlik bilgisi formu.
 *
 * Her saglayicinin alanlari FARKLI ve sunucu ayri zod semalariyla doguluyor;
 * tek bir "genel" form, yanlis alan adlariyla 422 almanin en kolay yolu olurdu.
 *
 * ⚠️ Girilen degerler MOBILDE SAKLANMIYOR. Sunucuya gonderiliyor, orada
 * sifrelenip tutuluyor ve hicbir yanitta geri verilmiyor.
 */
@Composable
private fun BaglantiFormu(
    provider: String,
    islemDevamEdiyor: Boolean,
    workspaceId: String,
    viewModel: IntegrationsViewModel,
    onVazgec: () -> Unit
) {
    var alan1 by remember { mutableStateOf("") }
    var alan2 by remember { mutableStateOf("") }
    var alan3 by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
        when (provider) {
            "TRENDYOL" -> {
                LkTextField(
                    value = alan1,
                    onValueChange = { alan1 = it },
                    label = "Satıcı ID",
                    // Sunucu ^\d+$ zorunlu kiliyor; sayisal klavye kullaniciyi
                    // bastan dogru yone itiyor.
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                LkTextField(value = alan2, onValueChange = { alan2 = it }, label = "API Key")
                LkPasswordTextField(value = alan3, onValueChange = { alan3 = it }, label = "API Secret")
            }
            "HEPSIBURADA" -> {
                LkTextField(value = alan1, onValueChange = { alan1 = it }, label = "Satıcı ID")
                LkTextField(value = alan2, onValueChange = { alan2 = it }, label = "Kullanıcı adı")
                LkPasswordTextField(value = alan3, onValueChange = { alan3 = it }, label = "Parola")
            }
            "N11" -> {
                LkTextField(value = alan1, onValueChange = { alan1 = it }, label = "Mağaza adı")
                LkTextField(value = alan2, onValueChange = { alan2 = it }, label = "App Key")
                LkPasswordTextField(value = alan3, onValueChange = { alan3 = it }, label = "App Secret")
            }
            "SHOPIFY" -> {
                LkTextField(
                    value = alan1,
                    onValueChange = { alan1 = it },
                    label = "Mağaza alan adı",
                    placeholder = "magazaniz.myshopify.com"
                )
                Text(
                    text = "Shopify izni tarayıcıda açılır. Parolanız uygulamaya girilmez.",
                    style = LkTypography.getMetadata(),
                    color = LkTextMuted
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
            LkButton(
                text = "Bağlan",
                enabled = !islemDevamEdiyor,
                onClick = {
                    when (provider) {
                        "TRENDYOL" -> viewModel.trendyolBagla(workspaceId, alan1, alan2, alan3)
                        "HEPSIBURADA" -> viewModel.hepsiburadaBagla(workspaceId, alan1, alan2, alan3)
                        "N11" -> viewModel.n11Bagla(workspaceId, alan1, alan2, alan3)
                        "SHOPIFY" -> viewModel.shopifyBagla(workspaceId, alan1)
                    }
                }
            )
            LkButton(
                text = "Vazgeç",
                onClick = onVazgec,
                variant = LkButtonVariant.SECONDARY,
                enabled = !islemDevamEdiyor
            )
        }
    }
}

/**
 * Pazaryeri isareti.
 *
 * 🔴 BES SAGLAYICI DA AYNI GORUNUYORDU: kartlarda yalnizca durum noktasi
 * ve ad vardi, listeyi tararken hangi pazaryerine baktigini ancak yaziyi
 * okuyarak anliyordun.
 *
 * ⚠️ GERCEK LOGOLAR KULLANILMIYOR. Trendyol / Hepsiburada / n11 / Shopify
 * logolari tescilli marka; dosyalari uygulamaya koymak marka sahibinin
 * kullanim izniyle olur. Yerine her pazaryerinin KENDI MARKA RENGINDE
 * bas harf kutucugu ciziliyor: taniticiligi veriyor, izinsiz varlik
 * tasimiyor. Izinli logo dosyalari gelirse yalniz bu bilesenin ici
 * degisir.
 */
@Composable
private fun SaglayiciIsareti(provider: String, label: String) {
    val (zemin, harf) = when (provider.uppercase()) {
        "TRENDYOL" -> Color(0xFFF27A1A) to "ty"
        "HEPSIBURADA" -> Color(0xFFFF6000) to "hb"
        "N11" -> Color(0xFFE4022C) to "n11"
        "SHOPIFY" -> Color(0xFF5E8E3E) to "S"
        "AMAZON" -> Color(0xFFFF9900) to "a"
        else -> LkSurfaceTile to label.take(1).uppercase()
    }
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(LkShapes.MD)
            .background(zemin),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = harf,
            style = LkTypography.getBodyStrong(),
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}
