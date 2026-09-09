package com.localkarar.app.ui.screens.workspaces

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.decision.KararKaynagi
import com.localkarar.app.decision.KararRaporuUiState
import com.localkarar.app.decision.KararRaporuViewModel
import com.localkarar.app.decision.KararSatiri
import com.localkarar.app.decision.KararSuzgeci
import com.localkarar.app.decision.kararSuz
import com.localkarar.app.network.dto.TrackerAnaliziDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkEmptyState
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkHairline
import com.localkarar.app.ui.components.LkHeroPage
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkMetricCard
import com.localkarar.app.ui.components.LkPillChip
import com.localkarar.app.ui.components.LkSectionCard
import com.localkarar.app.ui.theme.*

/*
 * KARAR RAPORU EKRANI.
 *
 * Hedeflenen ve gerceklesen ALT ALTA degil, ayni kartta ard arda ve
 * etiketli. Mobilde iki sutun 360dp'de okunmuyor -- web iki sutun
 * kullaniyor cunku orada 700px var. Karsilastirmayi kuran sey sutun
 * degil, iki metnin ayni kartta ve ayni etiket dilinde durmasi.
 */
@Composable
fun KararRaporuScreen(
    viewModel: KararRaporuViewModel,
    onBack: () -> Unit,
    onGorevAc: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val suzgec by viewModel.suzgec.collectAsState()

    LkHeroPage(title = "Karar Raporu", onBack = onBack) {
        when (val state = uiState) {
            is KararRaporuUiState.Loading -> LkLoadingState()
            is KararRaporuUiState.Error -> LkErrorState(
                message = state.message,
                onRetry = { viewModel.load() }
            )
            is KararRaporuUiState.Content -> {
                val gorunen = kararSuz(state.satirlar, suzgec)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(LkSpacing.Space4),
                    verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
                ) {
                    item {
                        Text(
                            "Hedeflediğin sonuçla gerçekleşeni yan yana gör.",
                            style = LkTypography.getMetadata(),
                            color = LkTextSecondary
                        )
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                            LkMetricCard(
                                label = "Karar",
                                value = state.ozet.toplam.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            LkMetricCard(
                                label = "Sonucu yazılan",
                                value = state.ozet.degerlendirilen.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            LkMetricCard(
                                label = "Bekleyen",
                                value = state.ozet.bekleyen.toString(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    state.analiz?.let { analiz ->
                        item { YoneticiAnalizi(analiz) }
                    }

                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                            items(KararSuzgeci.entries.toList()) { secenek ->
                                LkPillChip(
                                    label = suzgecEtiketi(secenek),
                                    selected = suzgec == secenek,
                                    onClick = { viewModel.suzgecSec(secenek) }
                                )
                            }
                        }
                    }

                    if (gorunen.isEmpty()) {
                        item {
                            LkEmptyState(
                                title = if (state.satirlar.isEmpty()) "Takipte karar yok" else "Bu süzgeçte karar yok",
                                description = if (state.satirlar.isEmpty()) {
                                    "Karar araçlarında bir karar verip görevi bağladığında burada görünür."
                                } else {
                                    "Başka bir süzgeç deneyin."
                                },
                                icon = Icons.Outlined.Gavel
                            )
                        }
                    } else {
                        items(gorunen, key = { it.id }) { satir ->
                            KararKarti(satir = satir, onGorevAc = onGorevAc)
                        }
                    }
                }
            }
        }
    }
}

private fun suzgecEtiketi(suzgec: KararSuzgeci): String = when (suzgec) {
    KararSuzgeci.HEPSI -> "Hepsi"
    KararSuzgeci.BEKLEYEN -> "Sonucu bekleyen"
    KararSuzgeci.DEGERLENDIRILEN -> "Sonucu yazılan"
}

@Composable
private fun KararKarti(satir: KararSatiri, onGorevAc: (String) -> Unit) {
    LkSectionCard {
        Text(satir.baslik, style = LkTypography.getBodyStrong(), color = LkTextPrimary)
        satir.altBaslik?.let {
            Text(it, style = LkTypography.getMetadata(), color = LkTextSecondary)
        }
        Text(
            when (satir.kaynak) {
                KararKaynagi.KARAR_ARACI -> "Karar aracı"
                KararKaynagi.FINANSAL_MODEL -> "Finansal model"
            },
            style = LkTypography.getMicro(),
            color = LkTextMuted
        )

        LkHairline()

        AlanSatiri("HEDEFLENEN", satir.beklenen ?: "—")
        /* Sonuc yoksa alan BOS BIRAKILMIYOR: "henuz yazilmadi" demek,
           bos bir satirdan farkli bir sey soyluyor -- karar takibinin
           yarim kaldigini. */
        AlanSatiri(
            "GERÇEKLEŞEN",
            satir.gerceklesen ?: "Henüz yazılmadı",
            vurgusuz = satir.sonucBekliyor
        )
        /* Sapma SUNUCUDA HESAPLANMIYOR; yalnizca kullanici yazdiysa var. */
        satir.sapma?.let { AlanSatiri("SAPMA", it) }
        satir.ders?.let { AlanSatiri("ÇIKARILAN DERS", it) }

        val tarih = LkDateUtils.formatDate(satir.tarih)
        if (tarih.isNotBlank()) {
            Text("Karar: $tarih", style = LkTypography.getMicro(), color = LkTextMuted)
        }

        /* Finansal model kararinin acilacak bir gorevi yok; tiklaninca
           hicbir sey yapmayan dugme koymaktansa hic koymuyoruz. */
        satir.kayitId?.let { kayitId ->
            LkButton(
                text = "Görevi aç",
                onClick = { onGorevAc(kayitId) },
                variant = LkButtonVariant.SECONDARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AlanSatiri(etiket: String, deger: String, vurgusuz: Boolean = false) {
    Column {
        Text(etiket, style = LkTypography.getMicro(), color = LkTextMuted)
        Text(
            deger,
            style = LkTypography.getBodySmall(),
            color = if (vurgusuz) LkTextMuted else LkTextSecondary
        )
    }
}

/*
 * YONETICI ANALIZI.
 *
 * 🔴 "Karar basarisi" ORANI YOK ve sunucu da uretmiyor. Beklenen ile
 * gerceklesen serbest metin; farkini programla olcmek mumkun degil.
 * Gosterilen sey olculebilen: karar takip edilmis mi, gorev zamaninda
 * bitmis mi. Basari hukmunu metinleri okuyan yonetici veriyor -- ve bu
 * ekranda YAZIYOR, cunku "%73 basari" bekleyen biri sayinin
 * olmamasini eksiklik sanabilir.
 */
@Composable
private fun YoneticiAnalizi(analiz: TrackerAnaliziDto) {
    LkSectionCard {
        Text("Yönetici analizi", style = LkTypography.getBodyStrong(), color = LkTextPrimary)
        Text(
            "Kararın \"başarılı\" olup olmadığı hesaplanmıyor: hedeflenen ve " +
                "gerçekleşen serbest metin, farkları programla ölçülemez. " +
                "Aşağıdakiler ölçülebilenler.",
            style = LkTypography.getMetadata(),
            color = LkTextSecondary
        )

        Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
            LkMetricCard(
                label = "Sonucu yazılan karar",
                value = "${analiz.kararlar.takipEdilen} / ${analiz.kararlar.toplam}",
                modifier = Modifier.weight(1f)
            )
            LkMetricCard(
                label = "Sorumlusuz kayıt",
                value = analiz.gorevler.atanmamis.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        if (analiz.gorevler.kisiler.isEmpty()) {
            Text(
                "Henüz kimseye görev atanmamış.",
                style = LkTypography.getBodySmall(),
                color = LkTextSecondary
            )
        } else {
            LkHairline()
            /*
             * Tablo DEGIL, satirlar. Dar ekranda bes sutunlu bir tablo
             * ya yana kayar ya da okunmaz; her kisi kendi satirinda,
             * sayilar etiketli.
             */
            analiz.gorevler.kisiler.forEach { kisi ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(kisi.name, style = LkTypography.getBodySmall(), color = LkTextPrimary)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${kisi.tamamlanan}/${kisi.toplam} tamamlandı · ${kisi.zamaninda} zamanında",
                            style = LkTypography.getMicro(),
                            color = LkTextMuted
                        )
                        if (kisi.geciken > 0) {
                            Text(
                                "${kisi.geciken} geciken",
                                style = LkTypography.getMicro(),
                                color = LkDanger
                            )
                        }
                    }
                }
            }
        }
    }
}
