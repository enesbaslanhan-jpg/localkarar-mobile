package com.localkarar.app.ui.components.decision

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.decision.DecisionFollowUpViewModel
import com.localkarar.app.network.dto.BusinessRecordDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.theme.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * KARAR TAKIBI — "bu karar tuttu mu?"
 *
 * 🔴 MOBILDE HIC YOKTU. Karar araclarinin ogrenme dongusu bu: karari
 * bir goreve bagla, vakti gelince gercek sonucu ve cikarilan dersi yaz.
 * Webde sonucun altinda duruyor (`DecisionFollowUp.jsx`).
 *
 * ⚠️ ISLETME YOKSA FORM CIZILMIYOR. Takip bir isletmenin gorevi olarak
 * aciliyor; isletme secili degilken form gostermek, kaydedilemeyecek
 * bir form gostermek olurdu. Onun yerine sebebi yaziliyor.
 */
@Composable
fun LkKararTakibi(
    viewModel: DecisionFollowUpViewModel?,
    kararBasligi: String,
    isletmeSecili: Boolean,
    modifier: Modifier = Modifier
) {
    if (viewModel == null || !isletmeSecili) {
        Column(modifier = modifier.fillMaxWidth()) {
            BolumBasligi()
            Text(
                text = "Kararı takibe almak için önce bir işletme seçin. " +
                    "Takip, o işletmenin görevleri arasında açılır.",
                style = LkTypography.getMetadata(),
                color = LkTextSecondary
            )
        }
        return
    }

    val takipler by viewModel.takipler.collectAsState()
    val islemde by viewModel.islemde.collectAsState()
    var formAcik by remember { mutableStateOf(false) }
    var sonucYazilan by remember { mutableStateOf<BusinessRecordDto?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        BolumBasligi()

        takipler.forEach { kayit ->
            TakipSatiri(
                kayit = kayit,
                onSonucYaz = { sonucYazilan = kayit }
            )
        }

        if (!formAcik) {
            Spacer(Modifier.height(LkSpacing.Space2))
            LkButton(
                text = if (takipler.isEmpty()) "Bu kararı takibe al" else "Yeni takip ekle",
                variant = LkButtonVariant.SECONDARY,
                onClick = { formAcik = true },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            TakipFormu(
                kararBasligi = kararBasligi,
                islemde = islemde,
                onVazgec = { formAcik = false },
                onKaydet = { baslik, beklenen, vade ->
                    viewModel.takipOlustur(kararBasligi, baslik, beklenen, vade) { formAcik = false }
                }
            )
        }
    }

    sonucYazilan?.let { kayit ->
        SonucFormu(
            kayit = kayit,
            islemde = islemde,
            onVazgec = { sonucYazilan = null },
            onKaydet = { gercek, ders ->
                viewModel.sonucuKaydet(kayit, gercek, ders) { sonucYazilan = null }
            }
        )
    }
}

@Composable
private fun BolumBasligi() {
    Text("KARAR TAKİBİ", style = LkTypography.getMicro(), color = LkTextMuted)
    Spacer(Modifier.height(LkSpacing.Space2))
}

@Composable
private fun TakipSatiri(kayit: BusinessRecordDto, onSonucYaz: () -> Unit) {
    val takip = kayit.metadata["decisionFollowUp"] as? JsonObject
    val beklenen = takip?.get("expectedOutcome")?.jsonPrimitive?.contentOrNull
    val gercek = takip?.get("actualOutcome")?.jsonPrimitive?.contentOrNull
    val ders = takip?.get("lessonLearned")?.jsonPrimitive?.contentOrNull

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = LkSpacing.Space3)
            .clip(LkShapes.SM)
            .border(1.dp, LkLineSoft, LkShapes.SM)
            .background(LkSurfaceRaised)
            .padding(LkSpacing.Space3)
    ) {
        Text(kayit.title, style = LkTypography.getBody(), color = LkTextPrimary)

        /* Vade yalniz cozulebiliyorsa. */
        LkDateUtils.formatDate(kayit.dueAt).takeIf { it.isNotBlank() }?.let {
            Text("Gözden geçirme: $it", style = LkTypography.getMicro(), color = LkTextMuted)
        }

        if (!beklenen.isNullOrBlank()) {
            Spacer(Modifier.height(LkSpacing.Space2))
            Text("Beklenen: $beklenen", style = LkTypography.getMetadata(), color = LkTextSecondary)
        }
        if (!gercek.isNullOrBlank()) {
            Text("Gerçekleşen: $gercek", style = LkTypography.getMetadata(), color = LkTextSecondary)
        }
        if (!ders.isNullOrBlank()) {
            Text("Çıkarılan ders: $ders", style = LkTypography.getMetadata(), color = LkTextSecondary)
        }

        /*
         * Sonuc yazma dugmesi yalniz HENUZ KAPANMAMIS takipte.
         * Tamamlanmis bir takibe ikinci kez sonuc yazdirmak, ilk
         * yaziyi sessizce ezmek olurdu.
         */
        if (kayit.status != "completed") {
            Spacer(Modifier.height(LkSpacing.Space2))
            LkButton(
                text = "Sonucu yaz",
                variant = LkButtonVariant.QUIET,
                onClick = onSonucYaz,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TakipFormu(
    kararBasligi: String,
    islemde: Boolean,
    onVazgec: () -> Unit,
    onKaydet: (baslik: String, beklenen: String, vadeIso: String) -> Unit
) {
    /* Baslik karar adiyla ON DOLU: kullanicinin en olasi cevabi bu ve
       degistirebiliyor. */
    var baslik by remember { mutableStateOf("$kararBasligi — sonucu gözden geçir") }
    var beklenen by remember { mutableStateOf("") }
    var vade by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)) {
        LkTextField(value = baslik, onValueChange = { baslik = it }, label = "Görev başlığı")
        LkTextField(
            value = beklenen,
            onValueChange = { beklenen = it },
            label = "Ne olmasını bekliyorsun?"
        )
        LkTextField(
            value = vade,
            onValueChange = { vade = it },
            label = "Gözden geçirme tarihi (GG.AA.YYYY)"
        )

        val vadeIso = tarihiIsoyaCevir(vade)
        /* Tarih cozulemiyorsa kaydet KAPALI ve sebebi yaziliyor --
           sessizce bugunu koymak, kullanicinin koymadigi bir tarihi
           onun yerine secmek olurdu. */
        if (vade.isNotBlank() && vadeIso == null) {
            Text(
                text = "Tarihi GG.AA.YYYY biçiminde yazın (örnek: 15.11.2026).",
                style = LkTypography.getMicro(),
                color = LkWarning
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
            LkButton(
                text = if (islemde) "Kaydediliyor..." else "Takibe al",
                onClick = { vadeIso?.let { onKaydet(baslik, beklenen, it) } },
                enabled = !islemde && baslik.isNotBlank() &&
                    beklenen.isNotBlank() && vadeIso != null,
                modifier = Modifier.weight(1f)
            )
            LkButton(
                text = "Vazgeç",
                variant = LkButtonVariant.SECONDARY,
                onClick = onVazgec,
                enabled = !islemde,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SonucFormu(
    kayit: BusinessRecordDto,
    islemde: Boolean,
    onVazgec: () -> Unit,
    onKaydet: (gercek: String, ders: String) -> Unit
) {
    var gercek by remember { mutableStateOf("") }
    var ders by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onVazgec,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.BottomCenter) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(LkShapes.LG)
                    .background(LkSurfaceElevated)
                    .padding(LkSpacing.Space5),
                verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
            ) {
                Text("Sonucu yaz", style = LkTypography.getSectionTitle(), color = LkTextPrimary)
                Text(kayit.title, style = LkTypography.getMetadata(), color = LkTextSecondary)

                LkTextField(
                    value = gercek,
                    onValueChange = { gercek = it },
                    label = "Gerçekte ne oldu?"
                )
                LkTextField(
                    value = ders,
                    onValueChange = { ders = it },
                    label = "Çıkarılan ders (isteğe bağlı)"
                )

                LkButton(
                    text = if (islemde) "Kaydediliyor..." else "Kaydet ve kapat",
                    onClick = { onKaydet(gercek, ders) },
                    enabled = !islemde && gercek.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                LkButton(
                    text = "Vazgeç",
                    variant = LkButtonVariant.SECONDARY,
                    onClick = onVazgec,
                    enabled = !islemde,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * GG.AA.YYYY -> ISO 8601.
 *
 * ⚠️ Saat 12:00 seciliyor, gece yarisi DEGIL: web de oyle yapiyor
 * (`new Date(\`${dueDate}T12:00:00\`)`). Gece yarisi, zaman dilimi
 * kaymalarinda tarihi bir onceki gune dusurebiliyor.
 *
 * Cozulemezse `null`; cagiran taraf kaydetmeyi kapatiyor.
 */
private fun tarihiIsoyaCevir(girdi: String): String? {
    val parcalar = girdi.trim().split(".", "/", "-").filter { it.isNotBlank() }
    if (parcalar.size != 3) return null
    val gun = parcalar[0].toIntOrNull() ?: return null
    val ay = parcalar[1].toIntOrNull() ?: return null
    val yil = parcalar[2].toIntOrNull() ?: return null
    if (gun !in 1..31 || ay !in 1..12 || yil !in 2000..2100) return null
    val gg = gun.toString().padStart(2, '0')
    val aa = ay.toString().padStart(2, '0')
    return "$yil-$aa-${gg}T12:00:00.000Z"
}
