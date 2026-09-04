package com.localkarar.app.ui.screens.calculations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localkarar.app.calculations.calculatePriceArchitecture
import com.localkarar.app.ui.components.*
import com.localkarar.app.ui.theme.*

/*
 * FIYATLANDIRMA SIHIRBAZI — prototipteki "Canli Simulasyon" blogu.
 *
 * Prototip bunu KUTUSUZ istiyor ("Ana Sayfa Pulse gibi"), bu yuzden
 * `LkSection` kullaniliyor, kart degil.
 *
 * ⚠️ PROTOTIPTEKI RAKAMLAR TUTARSIZDI: ₺850 maliyet ve %40 marj icin ₺1.450
 * yaziyordu; gercek formul ₺1.416,67 veriyor (850 / 0,60). Prototipteki deger
 * suslemeydi -- burada GERCEK formul kullaniliyor.
 *
 * ⚠️ GOSTERILEN DORT ALAN SIFIR: gercek `fiyat_mimarisi` modelinin yedi girdisi
 * var (operasyon maliyeti, sabit gider payi, iade riski, komisyon, odeme
 * kesintisi dahil). Sihirbaz bunlardan ikisini aciyor; kalanlar 0 kabul
 * ediliyor ve bu ekranda ACIKCA yaziyor. Yoksa kullanici komisyonu hesaba
 * katilmis saniyip eksik fiyat belirlerdi -- gercek parayla yanlis karar.
 */
@Composable
fun PricingWizardSection(
    onOpenFullTool: () -> Unit,
    modifier: Modifier = Modifier
) {
    var maliyet by remember { mutableStateOf(850.0) }
    var marj by remember { mutableStateOf(40f) }

    val sonuc = remember(maliyet, marj) {
        calculatePriceArchitecture(
            dogrudanMaliyet = maliyet,
            operasyonMaliyeti = 0.0,
            sabitGiderPayi = 0.0,
            iadeRiskPayi = 0.0,
            komisyonOrani = 0.0,
            odemeOrani = 0.0,
            hedefMarj = marj.toDouble()
        )
    }

    LkSection(
        title = "Fiyatlandırma Sihirbazı",
        modifier = modifier,
        trailing = { LkPulseBadge("Canlı Simülasyon") }
    ) {
        Text(
            "Maliyet ve hedef kâr oranını belirle; KDV hariç satış fiyatını anında gör.",
            style = LkTypography.getBodySmall(),
            color = LkTextSecondary
        )

        LkNumericField(
            value = if (maliyet == 0.0) "" else maliyet.toLong().toString(),
            onValueChange = { maliyet = it.toDoubleOrNull() ?: 0.0 },
            label = "Birim maliyet (₺)"
        )

        // İMZA PANELİ — webdeki `financeResultHero` karsiligi.
        //
        // 🔴 EKRANLARDA ODAK NOKTASI YOKTU. Her sey acik gri yuzeyler
        // uzerinde metin satiriydi; goz nereye bakacagini bilmiyordu.
        // Webde tam bu ekranda (ToolsPage) koyu bir hero paneli var:
        // `background: var(--surface-signature)`, uzerinde dev rakam ve
        // sag ust kosede tasan yari saydam daire. Renk tokenlari mobilde de
        // TANIMLIYDI (`LkSurfaceSignature`) ama hicbir yerde kullanilmiyordu.
        //
        // §3.2 bu panelleri `shadow-dark` istisnasi olarak taniyor; tema
        // duyarli DEGIL, iki temada da ayni koyu zemin (Color.kt notu).
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(LkShapes.LG)
                .background(LkSurfaceSignature)
        ) {
            // Webdeki `::after` dairesi: 260px, sag ust kosede, %7 opak.
            // Salt dekoratif -- panelin duz bir dikdortgen olmasini onluyor.
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .offset(x = 96.dp, y = (-84).dp)
                    .clip(CircleShape)
                    .background(LkOnSignature.copy(alpha = 0.07f))
                    .align(Alignment.TopEnd)
            )

            Column(modifier = Modifier.padding(LkSpacing.Space6)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "ÖNERİLEN SATIŞ FİYATI",
                        style = LkTypography.getMetadata(),
                        color = LkOnSignatureDim,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        "Hedef kâr: %${marj.toInt()}",
                        style = LkTypography.getLabel(),
                        color = LkOnSignature,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(LkSpacing.Space2))
                Text(
                    text = if (sonuc != null) formatTry(sonuc.onerilenKdvHaricFiyat) else "—",
                    style = LkTypography.getDisplay().copy(fontFeatureSettings = "tnum"),
                    color = LkOnSignature
                )
                Text(
                    "KDV hariç yönetim fiyatı",
                    style = LkTypography.getMetadata(),
                    color = LkOnSignatureDim
                )

                Spacer(Modifier.height(LkSpacing.Space4))
                Slider(
                    value = marj,
                    onValueChange = { marj = it },
                    // Ust sinir 80: komisyon+odeme+marj toplami %100'e
                    // ulasirsa formul hesaplayamiyor.
                    valueRange = 5f..80f,
                    colors = SliderDefaults.colors(
                        thumbColor = LkOnSignature,
                        activeTrackColor = LkOnSignature,
                        inactiveTrackColor = LkOnSignature.copy(alpha = 0.25f)
                    )
                )
            }
        }

        LkHairline()

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = LkSpacing.Space2),
            horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
        ) {
            WizardMetric("Birim Maliyet", formatTry(maliyet), LkTextPrimary, Modifier.weight(1f))
            WizardMetric("Kâr Marjı", "%${marj.toInt()}", LkTextPrimary, Modifier.weight(1f))
            WizardMetric(
                "Net Kâr",
                if (sonuc != null) "+" + formatTry(sonuc.birimKatki) else "—",
                LkSuccess,
                Modifier.weight(1f)
            )
        }

        // Eksik girdiler ACIKCA soyleniyor; sessizce 0 kabul etmek yanlis
        // fiyata yol acardi.
        Text(
            "Komisyon, kargo, sabit gider ve iade riski bu önizlemede sıfır kabul edilir.",
            style = LkTypography.getMetadata(),
            color = LkTextMuted
        )

        LkButton(
            text = "Tüm alanlarla hesapla",
            variant = LkButtonVariant.SECONDARY,
            size = LkButtonSize.SM,
            onClick = onOpenFullTool
        )
    }
}

@Composable
private fun WizardMetric(
    etiket: String,
    deger: String,
    renk: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(etiket, style = LkTypography.getBodySmall(), color = LkTextMuted)
        Text(
            text = deger,
            style = LkTypography.getMetric().copy(fontFeatureSettings = "tnum"),
            color = renk,
            fontWeight = FontWeight.Bold
        )
    }
}
