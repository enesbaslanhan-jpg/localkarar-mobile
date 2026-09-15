package com.localkarar.app.ui.screens.workspaces

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.LkFormatting
import com.localkarar.app.network.dto.ParaToplamiDto
import com.localkarar.app.network.dto.TrackerReportRowDto
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkHairline
import com.localkarar.app.ui.components.LkHeroPage
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkMetricCard
import com.localkarar.app.ui.components.LkPillChip
import com.localkarar.app.ui.components.LkSectionCard
import com.localkarar.app.ui.theme.*
import com.localkarar.app.ui.theme.LkTypography.numeric
import com.localkarar.app.workspaces.RaporDonemi
import com.localkarar.app.workspaces.RaporUiState
import com.localkarar.app.workspaces.RaporViewModel

/*
 * RAPOR EKRANI — donem raporu (Faz 2, 15.09.2026).
 *
 * Web Rapor sayfasinin mobil karsiligi: donem haplari (bugun / bu hafta /
 * bu ay), toplam kartlari, gun ya da hafta satirlari. Rakamlar SUNUCUDAN;
 * ekran toplamaz. Bos gunler "—" ile gosterilir ki takvim hissi kalsin.
 *
 * Dosya indirme (xlsx/pdf) mobilde yok: paylasim akisi tarayiciya
 * yonlendirir; Faz 3'te belge paylasimi ile birlikte gelecek.
 */
@Composable
fun RaporScreen(viewModel: RaporViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val donem by viewModel.donem.collectAsState()

    LkHeroPage(
        title = "Rapor",
        onBack = onBack,
        heroExtra = {
            Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                RaporDonemi.entries.forEach { d ->
                    LkPillChip(label = d.etiket, selected = donem == d, onClick = { viewModel.donemSec(d) })
                }
            }
        }
    ) {
        when (val state = uiState) {
            is RaporUiState.Loading -> LkLoadingState()
            is RaporUiState.Error -> LkErrorState(message = state.message, onRetry = { viewModel.load() })
            is RaporUiState.Content -> {
                val r = state.rapor
                val para = { v: Double -> LkFormatting.formatMoney(v, r.currency) }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(LkSpacing.Space4),
                    verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
                ) {
                    item {
                        Text(
                            "${tarih(r.period.from)} – ${tarih(r.period.to, birOnce = true)}" +
                                if (r.estimated) "  ·  tahmini" else "",
                            style = LkTypography.getMetadata(),
                            color = LkTextMuted
                        )
                    }
                    item {
                        val t = r.totals
                        Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                                LkMetricCard("Tahsil edilen", para(t.tahsilat.amount) + ayrica(t.tahsilat), modifier = Modifier.weight(1f))
                                LkMetricCard("Ödenen", para(t.odeme.amount) + ayrica(t.odeme), modifier = Modifier.weight(1f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                                LkMetricCard("Pazaryeri satışı", para(t.pazaryeriBrut.amount), modifier = Modifier.weight(1f))
                                LkMetricCard("Pazaryeri net", para(t.pazaryeriNet.amount), modifier = Modifier.weight(1f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                                LkMetricCard("İade", para(t.iade.amount), modifier = Modifier.weight(1f))
                                LkMetricCard("Net", para(t.net), modifier = Modifier.weight(1f))
                            }
                            Text(
                                "Net = tahsilat − ödeme + pazaryeri net · ${t.siparisSayisi} sipariş",
                                style = LkTypography.getMicro(),
                                color = LkTextMuted
                            )
                            if (r.estimated) {
                                Text(
                                    "Tahmini: pazaryeri ödeme süresi veya komisyon oranı ayarlarda girilmemiş; sağlayıcı varsayılanı kullanıldı.",
                                    style = LkTypography.getMicro(),
                                    color = LkWarning
                                )
                            }
                        }
                    }
                    item {
                        LkSectionCard {
                            Text(
                                if (r.granularity == "day") "Günlük dağılım" else "Haftalık dağılım (Pazartesi)",
                                style = LkTypography.getBodyStrong(),
                                color = LkTextPrimary
                            )
                            Spacer(Modifier.height(LkSpacing.Space2))
                            r.rows.forEachIndexed { i, satir ->
                                RaporSatir(satir, para)
                                if (i != r.rows.lastIndex) LkHairline()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RaporSatir(s: TrackerReportRowDto, para: (Double) -> String) {
    val bos = s.tahsilat == 0.0 && s.odeme == 0.0 && s.siparisSayisi == 0
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = LkSpacing.Space2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.width(72.dp)) {
            Text(tarih(s.from), style = LkTypography.getBodySmall(), color = LkTextPrimary, fontWeight = FontWeight.SemiBold)
            if (s.siparisSayisi > 0) Text("${s.siparisSayisi} sipariş", style = LkTypography.getMicro(), color = LkTextMuted)
        }
        if (bos) {
            Text("—", style = LkTypography.getBodySmall(), color = LkTextMuted, modifier = Modifier.weight(1f))
        } else {
            Column(Modifier.weight(1f)) {
                Text("Tahsilat ${para(s.tahsilat)} · Ödeme ${para(s.odeme)}", style = LkTypography.getMicro(), color = LkTextSecondary)
                Text("Pazaryeri ${para(s.pazaryeriNet)}" + if (s.iade > 0) " · İade ${para(s.iade)}" else "", style = LkTypography.getMicro(), color = LkTextSecondary)
            }
            Text(
                para(s.net),
                style = LkTypography.getBodyStrong().numeric(),
                color = if (s.net < 0) LkDanger else LkTextPrimary
            )
        }
    }
}

/** Para birimi disi tutarlar toplama girmez; kartta "+ 100 USD" notu. */
private fun ayrica(p: ParaToplamiDto): String =
    if (p.otherCurrencies.isEmpty()) "" else "  (+ " + p.otherCurrencies.joinToString(", ") { "${it.amount} ${it.currency}" } + ")"

/** ISO → "15 Eyl". Aralik bitisi disaridan sonraki gunun 00:00'i gelir; birOnce ile bir gun geri. */
private fun tarih(iso: String, birOnce: Boolean = false): String {
    if (iso.length < 10) return iso
    val yil = iso.substring(0, 4).toInt(); val ay = iso.substring(5, 7).toInt(); var gun = iso.substring(8, 10).toInt()
    // Sunucu UTC anini gonderir; Istanbul gunu = +3 saat. Saat 21:00Z ve sonrasi ertesi gundur.
    val saat = iso.substring(11, 13).toIntOrNull() ?: 0
    var ayIdx = ay
    if (saat >= 21) gun += 1
    if (birOnce) gun -= 1
    val ayGun = intArrayOf(31, if (yil % 4 == 0) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    if (gun > ayGun[ayIdx - 1]) { gun = 1; ayIdx += 1 }
    if (gun < 1) { ayIdx -= 1; if (ayIdx < 1) ayIdx = 12; gun = ayGun[ayIdx - 1] }
    val aylar = listOf("Oca", "Şub", "Mar", "Nis", "May", "Haz", "Tem", "Ağu", "Eyl", "Eki", "Kas", "Ara")
    return "$gun ${aylar[ayIdx - 1]}"
}
