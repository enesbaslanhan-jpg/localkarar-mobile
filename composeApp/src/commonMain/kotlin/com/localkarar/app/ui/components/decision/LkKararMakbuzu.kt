package com.localkarar.app.ui.components.decision

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.core.trBuyuk
import com.localkarar.app.core.rememberTextSharer
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkHairline
import com.localkarar.app.ui.theme.*
import kotlinx.serialization.json.*

/**
 * KARAR MAKBUZU — kararin saklanabilir ozeti.
 *
 * 🔴 MOBILDE HIC YOKTU. Webde her karar aracinin sonucunun yaninda bir
 * "Karar Fişi" duruyor (`DecisionReceipt.jsx`): hukum, dayanak
 * satirlari, sonraki adim ve yazdirma. Mobilde yalniz ayrintili sonuc
 * paneli vardi; kullanicinin kararini disari cikarmasinin yolu yoktu.
 *
 * ⚠️ "YAZDIR" YERINE "PAYLAS". Telefonda yazdirma dogal eylem degil;
 * paylasim sayfasi gondermeyi, kaydetmeyi ve yazici tanimliysa
 * yazdirmayi tek yerde veriyor. Webdeki niyet ("bu karari disari cikar")
 * korunuyor, araci mobil dunyaya cevriliyor.
 *
 * ⚠️ HICBIR ALAN UYDURULMUYOR. Webin kurali aynen gecerli: "bulunmayan
 * alan hic gosterilmez". Hukum yoksa rozet cizilmiyor, dayanak yoksa
 * bolum hic acilmiyor.
 */
@Composable
fun LkKararMakbuzu(
    snapshot: JsonElement?,
    baslik: String,
    tamamlanmaZamani: String?,
    onMentoraSor: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (snapshot == null || snapshot !is JsonObject) return
    val cikti = snapshot["calculationOutput"] as? JsonObject ?: return

    val hukum = cikti["decisionLabel"]?.jsonPrimitive?.contentOrNull
        ?: riskeGoreHukum(snapshot["riskLevel"]?.jsonPrimitive?.contentOrNull)
    val ton = cikti["decisionTone"]?.jsonPrimitive?.contentOrNull
        ?: riskeGoreTon(snapshot["riskLevel"]?.jsonPrimitive?.contentOrNull)
    val ozet = cikti["summary"]?.jsonPrimitive?.contentOrNull

    val anaSonuc = anaSonucBul(cikti)
    val kalemler = kalemleriKur(cikti, anaSonuc?.anahtar)
    val dayanak = dayanakKur(cikti)
    val sonrakiAdimlar = (cikti["safeNextSteps"] as? JsonArray)
        ?.mapNotNull { it.jsonPrimitive.contentOrNull }
        ?.filter { it.isNotBlank() }
        ?.take(3)
        .orEmpty()

    val paylas = rememberTextSharer()

    val (zeminRengi, vurguRengi, simge) = when (ton) {
        "good" -> Triple(LkSuccess.copy(alpha = 0.12f), LkSuccess, Icons.Outlined.CheckCircle)
        "warning" -> Triple(LkWarning.copy(alpha = 0.12f), LkWarning, Icons.Outlined.Warning)
        "bad" -> Triple(LkDanger.copy(alpha = 0.10f), LkDanger, Icons.Outlined.Cancel)
        else -> Triple(LkPrimary.copy(alpha = 0.08f), LkPrimary, Icons.Outlined.Info)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(LkShapes.MD)
            .background(LkSurfaceElevated)
            .border(1.dp, LkLineSoft, LkShapes.MD)
            .padding(LkSpacing.Space4)
    ) {
        Text(
            text = "KARAR MAKBUZU",
            style = LkTypography.getMicro(),
            color = LkTextMuted
        )
        Spacer(Modifier.height(LkSpacing.Space2))
        Text(baslik, style = LkTypography.getTitleS(), color = LkTextPrimary)

        /*
         * Tarih: once cagiranin verdigi, yoksa snapshot icindeki --
         * webdeki `completedAt || snapshot?.completedAt` ile ayni.
         * Cozulemezse HIC yazilmiyor; ham ISO dizesi basilmiyor.
         */
        LkDateUtils.formatDateTime(
            tamamlanmaZamani ?: snapshot["completedAt"]?.jsonPrimitive?.contentOrNull
        )
            .takeIf { it.isNotBlank() }
            ?.let {
                Spacer(Modifier.height(LkSpacing.Space1))
                Text(it, style = LkTypography.getMicro(), color = LkTextMuted)
            }

        /* HUKUM — ton hem renkle hem SIMGEYLE hem de KELIMEYLE veriliyor;
           renk tek basina tasiyici degil. */
        if (hukum != null) {
            Spacer(Modifier.height(LkSpacing.Space3))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(LkShapes.SM)
                    .background(zeminRengi)
                    .padding(LkSpacing.Space3),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(simge, contentDescription = null, tint = vurguRengi, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(LkSpacing.Space2))
                Text(hukum, style = LkTypography.getBody(), color = vurguRengi)
            }
        }

        if (!ozet.isNullOrBlank()) {
            Spacer(Modifier.height(LkSpacing.Space3))
            Text(ozet, style = LkTypography.getBodySmall(), color = LkTextSecondary)
        }

        /* ANA SONUC — makbuzun tek buyuk rakami. */
        anaSonuc?.let {
            Spacer(Modifier.height(LkSpacing.Space4))
            LkHairline()
            Spacer(Modifier.height(LkSpacing.Space3))
            Text(it.etiket.trBuyuk(), style = LkTypography.getMicro(), color = LkTextMuted)
            Spacer(Modifier.height(LkSpacing.Space1))
            Text(it.deger, style = LkTypography.getTitleL(), color = LkTextPrimary)
        }

        if (kalemler.isNotEmpty()) {
            Spacer(Modifier.height(LkSpacing.Space3))
            kalemler.forEach { kalem ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = LkSpacing.Space1),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(kalem.etiket, style = LkTypography.getMetadata(), color = LkTextSecondary)
                    Text(kalem.deger, style = LkTypography.getMetadata(), color = LkTextPrimary)
                }
            }
        }

        if (dayanak.isNotEmpty()) {
            Spacer(Modifier.height(LkSpacing.Space4))
            Text("DAYANAK", style = LkTypography.getMicro(), color = LkTextMuted)
            Spacer(Modifier.height(LkSpacing.Space2))
            dayanak.forEach {
                Text("• $it", style = LkTypography.getMetadata(), color = LkTextSecondary)
            }
        }

        if (sonrakiAdimlar.isNotEmpty()) {
            Spacer(Modifier.height(LkSpacing.Space4))
            Text("SONRAKİ ADIM", style = LkTypography.getMicro(), color = LkTextMuted)
            Spacer(Modifier.height(LkSpacing.Space2))
            sonrakiAdimlar.forEach {
                Text("• $it", style = LkTypography.getMetadata(), color = LkTextSecondary)
            }
        }

        Spacer(Modifier.height(LkSpacing.Space4))
        Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
            if (paylas != null) {
                LkButton(
                    text = "Paylaş",
                    variant = LkButtonVariant.SECONDARY,
                    onClick = {
                        paylas(
                            baslik,
                            makbuzMetni(baslik, hukum, ozet, anaSonuc, kalemler, dayanak, sonrakiAdimlar)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            LkButton(
                text = "Mentora sor",
                variant = LkButtonVariant.SECONDARY,
                onClick = {
                    /* Webdeki ile ayni baglam: baslik — hukum — ozet. */
                    onMentoraSor(listOfNotNull(baslik, hukum, ozet).joinToString(" — "))
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private data class MakbuzKalemi(val etiket: String, val deger: String, val anahtar: String? = null)

/*
 * Webde hukum once `decisionLabel`den, yoksa `riskLevel`den turuyor.
 * Ikisi de yoksa hukum YAZILMIYOR -- "bilinmiyor" yazmak bir hukum
 * degil, gurultudur.
 */
private fun riskeGoreHukum(risk: String?): String? = when (risk) {
    "low" -> "Karar güçlü görünüyor"
    "medium" -> "Dikkatli ilerleyin"
    "high", "critical" -> "Karar zayıf görünüyor"
    else -> null
}

private fun riskeGoreTon(risk: String?): String? = when (risk) {
    "low" -> "good"
    "medium" -> "warning"
    "high", "critical" -> "bad"
    else -> null
}

/*
 * ANA SONUC. Web ile ayni oncelik: once `contribution`, yoksa adinda
 * "katkı/net/marj" gecen ilk metrik.
 *
 * ⚠️ Girdi yankisi olan metrikler (fiyat, adet) ana sonuc YAPILMIYOR:
 * kullanicinin kendi yazdigi sayiyi "sonuc" diye buyuk puntoyla geri
 * gostermek, hesaplanmis bir sey varmis izlenimi verirdi.
 */
private val SONUC_DESENI = Regex("katkı|net|marj", RegexOption.IGNORE_CASE)

private fun anaSonucBul(cikti: JsonObject): MakbuzKalemi? {
    val katki = cikti["contribution"]?.jsonPrimitive?.doubleOrNull
    if (katki != null) {
        return MakbuzKalemi("Birim başına net katkı", degerYaz(katki, "money"))
    }
    val metrikler = cikti["metrics"] as? JsonArray ?: return null
    val sonuc = metrikler.firstOrNull { m ->
        val o = m as? JsonObject ?: return@firstOrNull false
        val etiket = o["label"]?.jsonPrimitive?.contentOrNull ?: return@firstOrNull false
        SONUC_DESENI.containsMatchIn(etiket) && o["value"]?.jsonPrimitive?.doubleOrNull != null
    } as? JsonObject ?: return null

    val etiket = sonuc["label"]!!.jsonPrimitive.content
    val deger = degerYaz(
        sonuc["value"]!!.jsonPrimitive.double,
        sonuc["format"]?.jsonPrimitive?.contentOrNull
    )
    return MakbuzKalemi(etiket, deger, sonuc["key"]?.jsonPrimitive?.contentOrNull ?: etiket)
}

private fun kalemleriKur(cikti: JsonObject, haricAnahtar: String?): List<MakbuzKalemi> {
    val metrikler = cikti["metrics"] as? JsonArray
    if (metrikler != null && metrikler.isNotEmpty()) {
        return metrikler.mapNotNull { m ->
            val o = m as? JsonObject ?: return@mapNotNull null
            val etiket = o["label"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val anahtar = o["key"]?.jsonPrimitive?.contentOrNull ?: etiket
            /* Ana sonuc olarak secilen metrik makbuzda IKI KEZ yazilmaz. */
            if (haricAnahtar != null && anahtar == haricAnahtar) return@mapNotNull null
            val sayi = o["value"]?.jsonPrimitive?.doubleOrNull ?: return@mapNotNull null
            MakbuzKalemi(etiket, degerYaz(sayi, o["format"]?.jsonPrimitive?.contentOrNull))
        }
    }

    /* DC-PROFIT-001 `metrics[]` uretmiyor; kendi alanlarindan kuruluyor. */
    return listOfNotNull(
        cikti["revenue"]?.jsonPrimitive?.doubleOrNull
            ?.let { MakbuzKalemi("Satış fiyatı", degerYaz(it, "money")) },
        cikti["totalKnownCost"]?.jsonPrimitive?.doubleOrNull
            ?.let { MakbuzKalemi("Bilinen toplam maliyet", degerYaz(it, "money")) },
        cikti["contributionMarginPercent"]?.jsonPrimitive?.doubleOrNull
            ?.let { MakbuzKalemi("Katkı marjı", degerYaz(it, "percent")) },
        cikti["breakEvenPrice"]?.jsonPrimitive?.doubleOrNull
            ?.let { MakbuzKalemi("Başabaş fiyatı", degerYaz(it, "money")) }
    )
}

/* Dayanak: formuller + risk uyarilari, webdeki gibi en fazla ucu. */
private fun dayanakKur(cikti: JsonObject): List<String> {
    val formuller = (cikti["formulas"] as? JsonArray).orEmpty().mapNotNull { e ->
        when (e) {
            is JsonPrimitive -> e.contentOrNull
            is JsonObject -> e["description"]?.jsonPrimitive?.contentOrNull
                ?: e["label"]?.jsonPrimitive?.contentOrNull
                ?: e["formula"]?.jsonPrimitive?.contentOrNull
            else -> null
        }
    }
    val uyarilar = (cikti["riskWarnings"] as? JsonArray).orEmpty()
        .mapNotNull { it.jsonPrimitive.contentOrNull }
    return (formuller + uyarilar).filter { it.isNotBlank() }.take(3)
}

private fun degerYaz(sayi: Double, bicim: String?): String = when (bicim) {
    "money" -> com.localkarar.app.core.LkFormatting.formatMoney(sayi)
    "percent" -> com.localkarar.app.core.LkFormatting.formatPercent(sayi)
    "months" -> "${com.localkarar.app.core.LkFormatting.formatNumber(sayi)} ay"
    "days" -> "${com.localkarar.app.core.LkFormatting.formatNumber(sayi)} gün"
    else -> com.localkarar.app.core.LkFormatting.formatNumber(sayi)
}

/*
 * Paylasilan metin.
 *
 * ⚠️ Ekranda ne varsa o paylasiliyor; paylasima ozel bir yorum ya da
 * "LocalKarar ile hesaplandi" gibi bir reklam satiri EKLENMIYOR.
 * Kullanicinin karari kullanicinindir.
 */
private fun makbuzMetni(
    baslik: String,
    hukum: String?,
    ozet: String?,
    anaSonuc: MakbuzKalemi?,
    kalemler: List<MakbuzKalemi>,
    dayanak: List<String>,
    sonrakiAdimlar: List<String>
): String = buildString {
    appendLine(baslik)
    hukum?.let { appendLine(it) }
    if (!ozet.isNullOrBlank()) { appendLine(); appendLine(ozet) }
    anaSonuc?.let { appendLine(); appendLine("${it.etiket}: ${it.deger}") }
    if (kalemler.isNotEmpty()) {
        appendLine()
        kalemler.forEach { appendLine("${it.etiket}: ${it.deger}") }
    }
    if (dayanak.isNotEmpty()) {
        appendLine(); appendLine("Dayanak:")
        dayanak.forEach { appendLine("- $it") }
    }
    if (sonrakiAdimlar.isNotEmpty()) {
        appendLine(); appendLine("Sonraki adım:")
        sonrakiAdimlar.forEach { appendLine("- $it") }
    }
}.trim()
