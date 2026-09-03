package com.localkarar.app.ui.screens.courses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.calculations.CALCULATION_DEFINITIONS
import com.localkarar.app.network.dto.CanonicalFormulaCardDto
import com.localkarar.app.network.dto.CanonicalMistakeCardDto
import com.localkarar.app.network.dto.CanonicalSectionsDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkMath
import com.localkarar.app.ui.theme.*

/*
 * KANONIK DERS BOLUMLERI — arayuz.
 *
 * Webdeki karsiligi frontend/src/components/course/CanonicalLessonSections.jsx.
 *
 * Ayristirma BURADA YAPILMIYOR; sunucudan yapisal olarak geliyor
 * (src/services/canonical-lesson.ts). Bu dosyanin isi yalnizca yerlesim ve
 * dugmeleri dogru hedefe baglamak.
 */

/**
 * Hesaplama kimligini mobilin acabilecegi bir hedefe cevirir.
 *
 * Web "Hesaplamayi Ac" dugmesiyle `/app/tools?view=calculator&tool=<id>`
 * adresine gidiyor. Mobilde tek bir "araclar" ekrani yok; hizli hesaplama
 * `FormulaDetail`, detayli analiz `FinancialModelDetail` ekraninda.
 *
 * Ikisi de varsa HIZLI olan aciliyor: ders akisindaki kullanici bir sonuca
 * bakmak istiyor, tam finansal model kurmak degil.
 *
 * Katalogda karsiligi yoksa `null` doner ve DUGME BASILMAZ — webin
 * "sahte route uretme" kurali.
 *
 * @return (hedefKimlik, detayliMi) ciftii; yoksa null.
 */
fun hesaplamaHedefi(calculationId: String?): Pair<String, Boolean>? {
    if (calculationId.isNullOrBlank()) return null
    val tanim = CALCULATION_DEFINITIONS.firstOrNull { it.id == calculationId } ?: return null
    tanim.formulaId?.let { return it to false }
    tanim.modelCode?.let { return it to true }
    return null
}

@Composable
fun CanonicalSections(
    sections: CanonicalSectionsDto,
    onOpenFormula: (String) -> Unit,
    onOpenModel: (String) -> Unit,
    onOpenDecisionTool: (String) -> Unit
) {
    val decision = sections.decision
    val hesaplamalar = sections.calculations.filter { hesaplamaHedefi(it.calculationId) != null }

    if (decision?.toolCode == null && hesaplamalar.isEmpty() &&
        sections.formulaCards.isEmpty() && sections.mistakeCards.isEmpty()
    ) return

    Divider(color = LkLineSoft, thickness = 1.dp)
    Spacer(modifier = Modifier.height(LkSpacing.Space6))

    if (decision?.toolCode != null) {
        KararAraciKarti(
            baslik = decision.toolTitle,
            baglam = decision.context,
            maddeler = decision.bullets,
            sonuc = decision.result,
            kod = decision.toolCode,
            onOpenDecisionTool = onOpenDecisionTool
        )
        sections.extraDecisions.forEach { ek ->
            LkButton(
                text = ek.title,
                variant = LkButtonVariant.SECONDARY,
                onClick = { onOpenDecisionTool(ek.code) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(LkSpacing.Space2))
        }
        Spacer(modifier = Modifier.height(LkSpacing.Space4))
    }

    if (hesaplamalar.isNotEmpty()) {
        Text(text = "Hesaplamalar", style = LkTypography.getCardTitle(), color = LkTextPrimary)
        Spacer(modifier = Modifier.height(LkSpacing.Space3))
        hesaplamalar.forEach { hesap ->
            val hedef = hesaplamaHedefi(hesap.calculationId) ?: return@forEach
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LkSurfaceSunken, LkShapes.SM)
                    .padding(LkSpacing.Space4)
            ) {
                Column {
                    Text(
                        text = hesap.title ?: hesap.label,
                        style = LkTypography.getBodyStrong(),
                        color = LkTextPrimary
                    )
                    val modlar = buildList {
                        if (hesap.hasSimple) add("Hızlı hesaplama")
                        if (hesap.hasDetailed) add("Detaylı analiz")
                    }
                    if (modlar.isNotEmpty()) {
                        Text(
                            text = modlar.joinToString(" · "),
                            style = LkTypography.getMetadata(),
                            color = LkTextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(LkSpacing.Space3))
                    LkButton(
                        text = "Hesaplamayı Aç",
                        variant = LkButtonVariant.SECONDARY,
                        onClick = {
                            if (hedef.second) onOpenModel(hedef.first) else onOpenFormula(hedef.first)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(LkSpacing.Space3))
        }
        Spacer(modifier = Modifier.height(LkSpacing.Space4))
    }

    if (sections.formulaCards.isNotEmpty() || sections.mistakeCards.isNotEmpty()) {
        Text(text = "Pratik Bilgi Kartları", style = LkTypography.getCardTitle(), color = LkTextPrimary)
        Spacer(modifier = Modifier.height(LkSpacing.Space3))

        sections.formulaCards.forEach { kart ->
            FormulKarti(kart, onOpenFormula, onOpenModel, onOpenDecisionTool)
            Spacer(modifier = Modifier.height(LkSpacing.Space3))
        }
        sections.mistakeCards.forEach { kart ->
            HataKarti(kart)
            Spacer(modifier = Modifier.height(LkSpacing.Space3))
        }
        Spacer(modifier = Modifier.height(LkSpacing.Space4))
    }
}

@Composable
private fun KararAraciKarti(
    baslik: String?,
    baglam: String,
    maddeler: List<String>,
    sonuc: String,
    kod: String,
    onOpenDecisionTool: (String) -> Unit
) {
    Text(
        text = "Karar Araçları Entegrasyonu",
        style = LkTypography.getCardTitle(),
        color = LkTextPrimary
    )
    Spacer(modifier = Modifier.height(LkSpacing.Space3))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(LkSurfacePanel, LkShapes.SM)
            .border(1.dp, LkLineSoft, LkShapes.SM)
            .padding(LkSpacing.Space4)
    ) {
        Column {
            if (baslik != null) {
                Text(baslik, style = LkTypography.getBodyStrong(), color = LkTextPrimary)
                Spacer(modifier = Modifier.height(LkSpacing.Space2))
            }
            if (baglam.isNotBlank()) {
                Text(baglam, style = LkTypography.getBody(), color = LkTextSecondary)
                Spacer(modifier = Modifier.height(LkSpacing.Space2))
            }
            maddeler.forEach { madde ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = LkSpacing.Space1)) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(5.dp)
                            .background(LkPrimary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(LkSpacing.Space3))
                    Text(
                        madde,
                        style = LkTypography.getBodySmall(),
                        color = LkTextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (sonuc.isNotBlank()) {
                Spacer(modifier = Modifier.height(LkSpacing.Space2))
                Text(sonuc, style = LkTypography.getMetadata(), color = LkTextSecondary)
            }
            Spacer(modifier = Modifier.height(LkSpacing.Space4))
            LkButton(
                text = "Karar Aracını Aç",
                onClick = { onOpenDecisionTool(kod) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    Spacer(modifier = Modifier.height(LkSpacing.Space4))
}

@Composable
private fun FormulKarti(
    kart: CanonicalFormulaCardDto,
    onOpenFormula: (String) -> Unit,
    onOpenModel: (String) -> Unit,
    onOpenDecisionTool: (String) -> Unit
) {
    val hedef = hesaplamaHedefi(kart.calculationId)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(LkSurfacePanel, LkShapes.SM)
            .border(1.dp, LkLineSoft, LkShapes.SM)
            .padding(LkSpacing.Space4)
    ) {
        Column {
            Text("FORMÜL KARTI", style = LkTypography.getMicro(), color = LkPrimary)
            Spacer(modifier = Modifier.height(LkSpacing.Space1))
            Text(kart.title, style = LkTypography.getBodyStrong(), color = LkTextPrimary)

            if (kart.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(LkSpacing.Space2))
                Text(kart.description, style = LkTypography.getBodySmall(), color = LkTextSecondary)
            }

            /*
             * Formuller GERCEK LaTeX tasiyor. `LkMath` blok kipinde ciziyor:
             * kesirler pay/payda duzeninde, uzun formuller yatay
             * kaydirilabilir. Ham `$$...$$` metni kullaniciya gosterilmez.
             */
            kart.formulas.forEach { formul ->
                Spacer(modifier = Modifier.height(LkSpacing.Space3))
                LkMath(latex = formul, blok = true)
            }

            kart.example?.let { ornek ->
                Spacer(modifier = Modifier.height(LkSpacing.Space3))
                Text("Örnek", style = LkTypography.getMicro(), color = LkTextSecondary)
                if (ornek.intro.isNotBlank()) {
                    Spacer(modifier = Modifier.height(LkSpacing.Space1))
                    Text(ornek.intro, style = LkTypography.getBodySmall(), color = LkTextSecondary)
                }
                ornek.formulas.forEach { formul ->
                    Spacer(modifier = Modifier.height(LkSpacing.Space2))
                    LkMath(latex = formul, blok = true)
                }
            }

            if (kart.interpretation.isNotBlank()) {
                Spacer(modifier = Modifier.height(LkSpacing.Space3))
                Text(kart.interpretation, style = LkTypography.getBodySmall(), color = LkTextPrimary)
            }

            if (hedef != null || kart.decisionToolCode != null) {
                Spacer(modifier = Modifier.height(LkSpacing.Space4))
                if (hedef != null) {
                    LkButton(
                        text = "Hesaplamayı Aç",
                        variant = LkButtonVariant.SECONDARY,
                        onClick = {
                            if (hedef.second) onOpenModel(hedef.first) else onOpenFormula(hedef.first)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (kart.decisionToolCode != null) {
                    if (hedef != null) Spacer(modifier = Modifier.height(LkSpacing.Space2))
                    LkButton(
                        text = kart.decisionToolTitle ?: "Karar Aracını Aç",
                        onClick = { onOpenDecisionTool(kart.decisionToolCode) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun HataKarti(kart: CanonicalMistakeCardDto) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(LkSurfacePanel, LkShapes.SM)
            .border(1.dp, LkLineSoft, LkShapes.SM)
            .padding(LkSpacing.Space4)
    ) {
        Column {
            Text("HATA / DOĞRU KARTI", style = LkTypography.getMicro(), color = LkWarning)
            if (kart.title != null) {
                Spacer(modifier = Modifier.height(LkSpacing.Space1))
                Text(kart.title, style = LkTypography.getBodyStrong(), color = LkTextPrimary)
            }
            if (kart.wrong != null) {
                Spacer(modifier = Modifier.height(LkSpacing.Space3))
                Text("Yaygın Hata", style = LkTypography.getMicro(), color = LkWarning)
                Spacer(modifier = Modifier.height(LkSpacing.Space1))
                Text(kart.wrong, style = LkTypography.getBodySmall(), color = LkTextPrimary)
            }
            if (kart.correct != null) {
                Spacer(modifier = Modifier.height(LkSpacing.Space3))
                Text("Doğru Yaklaşım", style = LkTypography.getMicro(), color = LkPrimary)
                Spacer(modifier = Modifier.height(LkSpacing.Space1))
                Text(kart.correct, style = LkTypography.getBodySmall(), color = LkTextPrimary)
            }
        }
    }
}
