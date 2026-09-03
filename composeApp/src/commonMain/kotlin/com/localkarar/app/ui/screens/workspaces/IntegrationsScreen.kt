package com.localkarar.app.ui.screens.workspaces

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.SecureScreen
import com.localkarar.app.network.dto.IntegrationConnectionDto
import com.localkarar.app.network.dto.MarketplaceEntryDto
import com.localkarar.app.ui.components.LkButton
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

    Scaffold(
        backgroundColor = LkSurfaceCanvas,
        topBar = {
            TopAppBar(
                backgroundColor = LkSurfacePanel,
                contentColor = LkTextPrimary,
                elevation = 0.dp,
                title = {
                    Column {
                        Text(
                            text = "Pazaryeri Entegrasyonları",
                            style = LkTypography.getSectionTitle()
                        )
                        Text(
                            text = "Bağlantı, eşitleme ve durum",
                            style = LkTypography.getMicro(),
                            color = LkTextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        when (val durum = uiState) {
            is IntegrationsUiState.Loading -> LkLoadingState(modifier = Modifier.padding(padding))

            is IntegrationsUiState.Error -> LkErrorState(
                message = durum.mesaj,
                onRetry = { viewModel.yukle(workspaceId) },
                modifier = Modifier.padding(padding),
                hata = durum.hata
            )

            is IntegrationsUiState.Content -> {
                if (durum.katalog.isEmpty()) {
                    LkEmptyState(
                        title = "Pazaryeri bulunamadı",
                        description = "Sunucu şu an bağlanabilecek bir pazaryeri bildirmiyor.",
                        modifier = Modifier.padding(padding)
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LkSurfacePanel, RoundedCornerShape(12.dp))
            .padding(LkSpacing.Space4)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
            Text(
                text = "Son eşitleme: ${baglanti.lastSyncedAt}",
                style = LkTypography.getMetadata(),
                color = LkTextMuted
            )
        }

        if (saglayici.comingSoon || !saglayici.enabled) return@Column

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
