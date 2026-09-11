package com.localkarar.app.ui.screens.workspaces

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.WorkspaceRepository
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*

@Composable
fun FinanceOverviewPanel(workspaceId: String, repository: WorkspaceRepository, onOpenAccounts: () -> Unit, onOpenRecord: (String) -> Unit) {
    var accounts by remember(workspaceId) { mutableStateOf<JsonObject?>(null) }
    var deadlines by remember(workspaceId) { mutableStateOf<JsonObject?>(null) }
    var error by remember { mutableStateOf("") }; var revision by remember { mutableStateOf(0) }
    LaunchedEffect(workspaceId, revision) {
        error = ""
        repository.finance(workspaceId, "accounts").onSuccess { accounts = it }.onFailure { error = it.message ?: "Hesaplar yüklenemedi." }
        repository.finance(workspaceId, "finance/deadlines").onSuccess { deadlines = it }.onFailure { error = it.message ?: "Hatırlatmalar yüklenemedi." }
    }
    Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)) {
        Text("Bugünkü kasa / banka durumu", style = LkTypography.getTitleS())
        if (error.isNotBlank()) { Text(error); TextButton(onClick = { revision++ }) { Text("Tekrar dene") } }
        if (accounts == null && error.isBlank()) Text("Yükleniyor…")
        val rows = accounts?.get("accounts")?.jsonArray.orEmpty()
        if (accounts != null && rows.isEmpty()) Text("Canlı bakiyeyi görmek için kasa veya banka hesabı ekleyin.")
        rows.forEach { row -> val a = row.jsonObject
            Text("${a.value("name")}: ${a.value("balance")} ${a.value("currency")} · Valör bekleyen: ${a.value("inTransit")} ${a.value("currency")}")
        }
        TextButton(onClick = onOpenAccounts) { Text("Kasa / Banka hesaplarını aç") }
        Text("Vergi / SGK — geciken ve 30 gün içinde yaklaşan", style = LkTypography.getTitleS())
        deadlines?.let { d ->
            Text(d.value("notice"))
            val reminders = d["records"]?.jsonArray.orEmpty()
            if (reminders.isEmpty()) Text("Bu aralıkta hatırlatma yok. Vergi profilinizi işletme ayarlarından oluşturabilirsiniz.")
            reminders.forEach { row -> val r = row.jsonObject
                TextButton(onClick = { onOpenRecord(r.value("id")) }) { Text("${r.value("title")} · ${r.value("dueAt").take(10)}") }
            }
        }
    }
}

@Composable
fun MonthlyFinancePanel(workspaceId: String, repository: WorkspaceRepository) {
    var month by rememberSaveable { mutableStateOf(LkDateUtils.today().toString().take(7)) }
    var result by remember { mutableStateOf<JsonObject?>(null) }; var error by remember { mutableStateOf("") }
    LaunchedEffect(month, workspaceId) {
        if(!Regex("\\d{4}-(0[1-9]|1[0-2])").matches(month)) return@LaunchedEffect
        result = null; error = ""
        repository.finance(workspaceId, "finance/monthly?month=$month").onSuccess { result = it }.onFailure { error = it.message ?: "Rapor yüklenemedi." }
    }
    Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
        Text("Aylık gelir / gider", style = LkTypography.getBodyStrong())
        Text("Tamamlanan kayıtlara göre nakit esaslı özet. Kredi, sermaye ve transfer hariç; muhasebesel kâr değildir.", color = LkTextSecondary)
        FinanceTextField("Ay (YYYY-AA)", month) { month = it }
        if(error.isNotBlank()) Text(error)
        result?.get("current")?.jsonObject?.get("currencies")?.jsonObject?.let { currencies ->
            if(currencies.isEmpty()) Text("Bu ay tamamlanan gelir / gider kaydı yok.")
            currencies.forEach { (currency, raw) ->
                val v = raw.jsonObject
                val previous = result?.get("previous")?.jsonObject?.get("currencies")?.jsonObject?.get(currency)?.jsonObject?.value("net") ?: "0.00"
                Text("$currency · Gelir: ${v.value("income")} · Gider: ${v.value("expense")}\nNet: ${v.value("net")} · Önceki ay net: $previous")
            }
        }
    }
}

@Composable
fun RenewalFinancePanel(workspaceId: String, repository: WorkspaceRepository, onSaved: () -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }; var template by rememberSaveable { mutableStateOf("insurance") }
    var date by rememberSaveable { mutableStateOf("") }; var yearly by rememberSaveable { mutableStateOf(false) }
    var key by rememberSaveable { mutableStateOf(kotlin.random.Random.nextBytes(16).joinToString("") { (it.toInt() and 255).toString(16).padStart(2, '0') }.let { "${it.take(8)}-${it.substring(8,12)}-4${it.substring(13,16)}-a${it.substring(17,20)}-${it.takeLast(12)}" }) }
    var busy by remember { mutableStateOf(false) }; var message by remember { mutableStateOf("") }; val scope = rememberCoroutineScope()
    Column {
        TextButton(onClick = { expanded = !expanded }) { Text("Yenileme hatırlatması ekle") }
        if(expanded) {
            val templates = mapOf("tax_certificate" to "Vergi levhası kontrolü", "business_license" to "İşyeri ruhsatı kontrolü", "vehicle_inspection" to "Araç muayenesi", "insurance" to "Sigorta", "comprehensive_insurance" to "Kasko")
            templates.forEach { (id, title) -> Row { RadioButton(selected = template == id, onClick = { template = id }); Text(title) } }
            FinanceTextField("Belgenizdeki tarih (YYYY-AA-GG)", date) { date = it }
            Row { Checkbox(checked = yearly, onCheckedChange = { yearly = it }); Text("Yıllık tekrarla; belge süresini teyit edin") }
            if(message.isNotBlank()) Text(message)
            LkButton(text = "Hatırlatma ekle", enabled = !busy, onClick = {
                val body = runCatching { buildJsonObject { put("template", template); put("dueAt", financeDate(date)); put("yearly", yearly); put("requestKey", key) } }.getOrElse { message = "Tarihi kontrol edin."; return@LkButton }
                busy = true
                scope.launch { repository.saveFinance(workspaceId, "renewals", body).onSuccess {
                    key = kotlin.random.Random.nextBytes(16).joinToString("") { (it.toInt() and 255).toString(16).padStart(2, '0') }.let { "${it.take(8)}-${it.substring(8,12)}-4${it.substring(13,16)}-a${it.substring(17,20)}-${it.takeLast(12)}" }
                    expanded = false; date = ""; message = "Hatırlatma kaydedildi."; onSaved()
                }.onFailure { message = it.message ?: "Kaydedilemedi." }; busy = false }
            })
        }
    }
}

@Composable
fun TaxFinancePanel(workspaceId: String, repository: WorkspaceRepository) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var company by rememberSaveable { mutableStateOf(false) }; var quarterly by rememberSaveable { mutableStateOf(false) }
    var employees by rememberSaveable { mutableStateOf(false) }; var bagkur by rememberSaveable { mutableStateOf(false) }
    var year by rememberSaveable { mutableStateOf(LkDateUtils.today().year.toString()) }
    val dates = remember { mutableStateMapOf<String, String>() }
    var message by remember { mutableStateOf("") }; var busy by remember { mutableStateOf(false) }
    val uri = LocalUriHandler.current; val scope = rememberCoroutineScope()
    LaunchedEffect(workspaceId) {
        repository.finance(workspaceId, "tax-profile").onSuccess {
            val p = it["profile"]?.jsonObject ?: return@onSuccess
            company = p.value("taxpayerType") == "limited"; quarterly = p.value("vatPeriod") == "quarterly"
            employees = p.value("hasEmployees") == "true"; bagkur = p.value("bagkur") == "true"
            p["deadlines"]?.jsonArray?.forEach { d -> dates[d.jsonObject.value("kind")] = d.jsonObject.value("firstDueAt").take(10) }
        }.onFailure { message = it.message ?: "Profil yüklenemedi." }
    }
    Column {
        TextButton(onClick = { expanded = !expanded }) { Text("Vergi ve SGK hatırlatmaları") }
        if(expanded) {
            Text("İlk tarihi GİB’den, SGK ve Bağ-Kur tarihlerini SGK’dan teyit edin. Sonraki dönemler planlamadır; tatil ve süre uzatımları otomatik uygulanmaz.")
            TextButton(onClick = { uri.openUri("https://gib.gov.tr/vergi-takvimi") }) { Text("GİB vergi takvimi") }
            TextButton(onClick = { uri.openUri("https://www.sgk.gov.tr") }) { Text("SGK") }
            Row { Checkbox(company, { company = it }); Text("Limited şirket (kapalıysa şahıs)") }
            Row { Checkbox(quarterly, { quarterly = it }); Text("3 aylık KDV (uygunluğu teyit edin)") }
            Row { Checkbox(employees, { employees = it }); Text("SGK’lı personel var") }
            Row { Checkbox(bagkur, { bagkur = it }); Text("Bağ-Kur yükümlülüğü var") }
            FinanceTextField("Takvim yılı", year) { year = it }
            mapOf("vat" to "KDV", "withholding" to "Muhtasar", "provisional" to "Geçici vergi", "sgk" to "SGK", "bagkur" to "Bağ-Kur").forEach { (kind, title) ->
                FinanceTextField("$title ilk tarih (YYYY-AA-GG, uygunsa)", dates[kind] ?: "") { dates[kind] = it }
            }
            if(message.isNotBlank()) Text(message)
            LkButton(text = "Profili kaydet ve takvime ekle", enabled = !busy, onClick = {
                val body = runCatching { buildJsonObject {
                    put("taxpayerType", if(company) "limited" else "individual"); put("vatPeriod", if(quarterly) "quarterly" else "monthly")
                    put("hasEmployees", employees); put("bagkur", bagkur); put("year", year.toInt())
                    putJsonArray("deadlines") { dates.filterValues { it.isNotBlank() }.forEach { (kind, value) -> add(buildJsonObject { put("kind", kind); put("firstDueAt", financeDate(value)) }) } }
                } }.getOrElse { message = "Tarihleri kontrol edin."; return@LkButton }
                busy = true
                scope.launch { repository.saveFinance(workspaceId, "tax-profile", body).onSuccess { message = "${it.value("created")} hatırlatma eklendi. Mevcut tarihler korunur; Kayıtlar’dan düzenleyebilirsiniz." }.onFailure { message = it.message ?: "Kaydedilemedi." }; busy = false }
            })
        }
    }
}

