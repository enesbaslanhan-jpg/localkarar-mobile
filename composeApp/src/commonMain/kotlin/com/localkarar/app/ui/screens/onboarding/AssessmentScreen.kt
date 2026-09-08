package com.localkarar.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.localkarar.app.network.dto.DegerlendirmeSonucuDto
import com.localkarar.app.network.dto.DegerlendirmeSorusuDto
import com.localkarar.app.onboarding.AssessmentViewModel
import com.localkarar.app.onboarding.DegerlendirmeUiState
import com.localkarar.app.ui.components.*
import com.localkarar.app.core.trBuyuk
import com.localkarar.app.ui.theme.*

/**
 * ISLETME OZ DEGERLENDIRMESI — mobilde ilk kez.
 *
 * 🔴 Web `/app/assessment` sunuyor, mobilde karsiligi YOKTU.
 *
 * ⚠️ SORULAR ALANA GORE GRUPLU ve hepsi tek sayfada. Webde cok adimli;
 * telefonda her soru icin ayri ekran, on iki soru icin on iki "Devam"
 * dokunusu demekti. Gruplu tek liste hem kisa hem de kullanicinin
 * nerede oldugunu gormesini sagliyor.
 *
 * ⚠️ Gonderim ancak HEPSI cevaplandiginda aciliyor: sunucu eksik cevabi
 * reddediyor ve kullaniciya makine listesi ("Eksik cevaplar: ...")
 * gostermenin anlami yok.
 */
@Composable
fun AssessmentScreen(
    viewModel: AssessmentViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LkHeroPage(title = "İşletme değerlendirmesi", onBack = onBack) {
        when (val durum = uiState) {
            is DegerlendirmeUiState.Yukleniyor -> LkLoadingState()

            is DegerlendirmeUiState.Hata -> LkErrorState(
                message = durum.mesaj,
                onRetry = { viewModel.yukle() }
            )

            is DegerlendirmeUiState.Sonuc -> SonucGorunumu(
                sonuc = durum.sonuc,
                onYenidenBasla = { viewModel.yenidenBasla() }
            )

            is DegerlendirmeUiState.Sorular -> {
                val tamam = durum.cevaplar.size >= durum.sorular.size
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(LkSpacing.Space4),
                    verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
                ) {
                    item {
                        Text(
                            text = "${durum.cevaplar.size} / ${durum.sorular.size} soru cevaplandı",
                            style = LkTypography.getMetadata(),
                            color = LkTextSecondary
                        )
                    }

                    /*
                     * Alan basligi YALNIZ degistiginde yaziliyor; her
                     * soruda tekrar etmek listeyi gurultuye bogardi.
                     */
                    var oncekiAlan: String? = null
                    durum.sorular.forEach { soru ->
                        val alan = soru.domainLabel
                        if (alan != null && alan != oncekiAlan) {
                            oncekiAlan = alan
                            item {
                                Text(
                                    /* ⚠️ Turkce buyuk harf: `uppercase()` yerel-bagimsiz
                                       ve "FINANSAL YÖNETIM" yaziyordu (olculdu
                                       08.09.2026, emulator). */
                                    text = alan.trBuyuk(),
                                    style = LkTypography.getMicro(),
                                    color = LkTextMuted
                                )
                            }
                        }
                        item {
                            SoruKarti(
                                soru = soru,
                                secili = durum.cevaplar[soru.id],
                                onSec = { viewModel.cevapla(soru.id, it) }
                            )
                        }
                    }

                    item {
                        LkButton(
                            text = when {
                                durum.gonderiliyor -> "Gönderiliyor..."
                                tamam -> "Değerlendirmeyi bitir"
                                else -> "Tüm soruları cevaplayın"
                            },
                            onClick = { viewModel.gonder() },
                            enabled = tamam && !durum.gonderiliyor,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SoruKarti(
    soru: DegerlendirmeSorusuDto,
    secili: String?,
    onSec: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = soru.title ?: soru.id,
            style = LkTypography.getBody(),
            color = LkTextPrimary
        )
        Spacer(Modifier.height(LkSpacing.Space2))

        soru.options.forEach { secenek ->
            val aktif = secili == secenek.value
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = LkSpacing.Space2)
                    .clip(LkShapes.SM)
                    .background(if (aktif) LkPrimarySoft else LkSurfaceRaised)
                    /* §19: dokunma hedefi en az 44dp. Dolgu tek basina
                       kisa etiketlerde bu yuksekligi garanti etmiyor. */
                    .heightIn(min = 44.dp)
                    .clickable { onSec(secenek.value) }
                    .padding(LkSpacing.Space3),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                /*
                 * ⚠️ Secim renkle DEGIL, isaretle de veriliyor: renk tek
                 * basina tasiyici olmamali.
                 */
                Text(
                    text = if (aktif) "●  " else "○  ",
                    style = LkTypography.getBody(),
                    color = if (aktif) LkPrimary else LkTextMuted
                )
                Text(
                    text = secenek.label ?: secenek.value,
                    style = LkTypography.getBodySmall(),
                    color = if (aktif) LkTextPrimary else LkTextSecondary
                )
            }
        }
    }
}

/*
 * ALAN KODU -> TURKCE AD.
 *
 * 🔴 Sonuc ekrani ham kodlari yaziyordu: "finance", "cyber", "ai"
 * (olculdu 08.09.2026, emulator). Turkce bir uygulamada makine kodu.
 *
 * ⚠️ Adlar UYDURULMADI; webin kendi tablosundan alindi
 * (`AssessmentPage.jsx` -> `domainLabels`, `tr/learning.json`). Sunucu
 * sonuc ucunda etiket DONMUYOR, yalniz kod donuyor -- bu yuzden
 * eslestirme istemcide.
 *
 * ⚠️ Tanimadigi kod gelirse kodun kendisi yaziliyor: bir alan
 * eklenirse ekran bos kalmasin.
 */
private val ALAN_ADLARI = mapOf(
    "finance" to "Finansal Yönetim",
    "sales" to "Satış ve Müşteri",
    "operations" to "Operasyon ve Kalite",
    "people" to "İnsan ve İş Güvenliği",
    "supply" to "Tedarik Zinciri",
    "cyber" to "Siber Güvenlik ve Veri",
    "export" to "İhracat Hazırlığı",
    "ai" to "Yapay Zekâ Hazırlığı"
)

private fun alanAdi(kod: String): String = ALAN_ADLARI[kod] ?: kod

@Composable
private fun SonucGorunumu(
    sonuc: DegerlendirmeSonucuDto,
    onYenidenBasla: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(LkSpacing.Space4),
        verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
    ) {
        item {
            LkSectionHeader(
                title = "Sonuçların",
                subtitle = sonuc.createdAt?.let {
                    com.localkarar.app.core.LkDateUtils.formatDate(it)
                }?.takeIf { it.isNotBlank() }
            )
        }

        /*
         * Puanlar 0–100. Sunucunun kendi olcegi; burada yeniden
         * yorumlanmiyor, oldugu gibi yaziliyor.
         */
        items(sonuc.scores.entries.toList()) { (alan, puan) ->
            LkResultRow(label = alanAdi(alan), value = "${puan.toInt()} / 100")
        }

        if (sonuc.priorityDomains.isNotEmpty()) {
            item {
                Column {
                    Text("ÖNCELİKLİ ALANLAR", style = LkTypography.getMicro(), color = LkTextMuted)
                    Spacer(Modifier.height(LkSpacing.Space2))
                    sonuc.priorityDomains.forEach {
                        Text("• ${alanAdi(it)}", style = LkTypography.getMetadata(), color = LkTextSecondary)
                    }
                }
            }
        }

        if (sonuc.recommendations.isNotEmpty()) {
            item {
                Column {
                    Text("ÖNERİLER", style = LkTypography.getMicro(), color = LkTextMuted)
                    Spacer(Modifier.height(LkSpacing.Space2))
                    sonuc.recommendations.forEach {
                        Text("• $it", style = LkTypography.getMetadata(), color = LkTextSecondary)
                    }
                }
            }
        }

        item {
            LkButton(
                text = "Yeniden değerlendir",
                variant = LkButtonVariant.SECONDARY,
                onClick = onYenidenBasla,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
