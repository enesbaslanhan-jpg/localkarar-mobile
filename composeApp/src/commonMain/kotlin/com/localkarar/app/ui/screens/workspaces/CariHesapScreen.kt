package com.localkarar.app.ui.screens.workspaces

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.core.LkFormatting
import com.localkarar.app.core.rememberFileSharer
import com.localkarar.app.network.dto.CariBakiyeDto
import com.localkarar.app.network.dto.CariHareketDto
import com.localkarar.app.ui.components.LkEmptyState
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkHairline
import com.localkarar.app.ui.components.LkHeroPage
import com.localkarar.app.ui.components.LkListRow
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkRowGroup
import com.localkarar.app.ui.components.LkSectionCard
import com.localkarar.app.ui.components.LkSectionHeader
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.CariHesapUiState
import com.localkarar.app.workspaces.CariHesapViewModel
import com.localkarar.app.workspaces.kapandiMi
import kotlin.math.abs

/*
 * CARI HESAP EKRANI — "Ahmet'e ne kadar borcum var?"
 *
 * 🔴 Bu sorunun cevabi urunde hicbir yerde yoktu: kisi karti
 * ad/telefon/sehir gosteriyordu, kayitlar kisiye baglanabiliyordu ama
 * TOPLANMIYORDU. Bir esnafin defterinde ilk baktigi sayi budur.
 *
 * ⚠️ WEBDE PANEL, MOBILDE EKRAN. Web listeden cikmadan bakilip
 * kapatilabilen bir katman kullaniyor; 360dp'de ayni katman listenin
 * ustunu tamamen ortup yarim kalmis bir sayfa gibi gorunurdu. Ekran
 * kendi geri dugmesiyle geliyor.
 */
@Composable
fun CariHesapScreen(
    viewModel: CariHesapViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val paylas = rememberFileSharer()

    /* Kisi adi rotada tasinmiyor (isimde ":" gecebilir); sunucudan
       gelen adla yaziliyor, gelene kadar genel baslik duruyor. */
    val kisiAdi = (uiState as? CariHesapUiState.Content)?.hesap?.contact?.name ?: "Cari Hesap"

    LkHeroPage(
        title = kisiAdi,
        onBack = onBack,
        actions = {
            /* Paylasim yolu olmayan platformda dugme HIC cizilmiyor:
               calismayan bir dugme kullaniciyi bosuna dokundurur. */
            if (paylas != null) {
                val hazirlaniyor = (uiState as? CariHesapUiState.Content)?.ekstreHazirlaniyor == true
                IconButton(
                    onClick = { viewModel.ekstrePaylas(paylas) },
                    enabled = !hazirlaniyor
                ) {
                    Icon(
                        Icons.Outlined.IosShare,
                        contentDescription = "Ekstreyi paylaş",
                        tint = LkHero.OnHero
                    )
                }
            }
        }
    ) {
        when (val state = uiState) {
            is CariHesapUiState.Loading -> LkLoadingState()
            is CariHesapUiState.Error -> LkErrorState(
                message = state.message,
                onRetry = { viewModel.load() }
            )
            is CariHesapUiState.Content -> {
                val hesap = state.hesap
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(LkSpacing.Space4),
                    verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
                ) {
                    /*
                     * 🔴 PARA BIRIMLERI TOPLANMIYOR.
                     *
                     * Kur bilgisi sistemde yok; 5.000 TL ile 200 USD'yi
                     * tek sayida toplamak uydurma bir rakam uretirdi.
                     * Her para birimi kendi kartinda — webdeki kural
                     * birebir ayni.
                     */
                    if (hesap.bakiyeler.isEmpty()) {
                        item {
                            Text(
                                "Açık hesap yok.",
                                style = LkTypography.getMetadata(),
                                color = LkTextSecondary
                            )
                        }
                    } else {
                        bakiyeler(hesap.bakiyeler)
                    }

                    item { LkSectionHeader(title = "Hareketler") }

                    if (hesap.hareketler.isEmpty()) {
                        item {
                            LkEmptyState(
                                title = "Bu kişiye bağlı kayıt yok",
                                description = "Ödeme, tahsilat ya da senet kaydını kişiye bağladığında burada görünür.",
                                icon = Icons.Outlined.ReceiptLong
                            )
                        }
                    } else {
                        item {
                            LkRowGroup {
                                hesap.hareketler.forEachIndexed { i, hareket ->
                                    HareketSatiri(hareket)
                                    if (i != hesap.hareketler.lastIndex) LkHairline()
                                }
                            }
                        }
                    }

                    /* Sessizce kirpmak "hepsi bu" izlenimi verirdi. */
                    if (hesap.kirpildi) {
                        item {
                            Text(
                                "Yalnız son 200 hareket gösteriliyor.",
                                style = LkTypography.getMetadata(),
                                color = LkTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun LazyListScope.bakiyeler(liste: List<CariBakiyeDto>) {
    liste.forEach { bakiye -> item { BakiyeKarti(bakiye) } }
}

@Composable
private fun BakiyeKarti(bakiye: CariBakiyeDto) {
    LkSectionCard {
        Column(
            modifier = Modifier.padding(LkSpacing.Space4),
            verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)
        ) {
            BakiyeSatiri("Alacak", LkFormatting.formatMoney(bakiye.alacak, bakiye.currency))
            BakiyeSatiri("Borç", LkFormatting.formatMoney(bakiye.borc, bakiye.currency))
            LkHairline()
            BakiyeSatiri(
                etiket = if (bakiye.bakiye >= 0) "Net alacak" else "Net borç",
                deger = LkFormatting.formatMoney(abs(bakiye.bakiye), bakiye.currency),
                renk = if (bakiye.bakiye >= 0) LkSuccess else LkDanger,
                vurgulu = true
            )
        }
    }
}

@Composable
private fun BakiyeSatiri(
    etiket: String,
    deger: String,
    renk: Color = LkTextPrimary,
    vurgulu: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(etiket, style = LkTypography.getBodySmall(), color = LkTextSecondary)
        Text(
            deger,
            style = if (vurgulu) LkTypography.getBodyStrong() else LkTypography.getBodySmall(),
            color = renk
        )
    }
}

@Composable
private fun HareketSatiri(hareket: CariHareketDto) {
    val kapandi = hareket.kapandiMi()
    val alacak = hareket.direction == "receivable"
    LkListRow(
        baslik = hareket.title,
        altBaslik = listOfNotNull(
            LkDateUtils.formatDate(hareket.dueAt ?: hareket.createdAt).takeIf { it.isNotBlank() },
            cariDurumEtiketi(hareket.status)
        ).joinToString(" · "),
        kategori = recordTypeLabel(hareket.type),
        tutar = hareket.amount?.let { LkFormatting.formatMoney(it, hareket.currency) },
        /*
         * Kapanan hareket solgun: bakiyeye girmiyor ama gecmiste
         * duruyor. Gizlemek "bu odeme hic olmadi" demek olurdu.
         */
        tutarRengi = when {
            kapandi -> LkTextMuted
            alacak -> LkSuccess
            else -> LkDanger
        }
    )
}

private fun cariDurumEtiketi(durum: String): String = when (durum) {
    "open" -> "Açık"
    "in_progress" -> "Devam ediyor"
    "completed" -> "Tamamlandı"
    "cancelled" -> "İptal"
    "deferred" -> "Ertelendi"
    else -> durum
}
