package com.localkarar.app.ui.screens.workspaces

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.localkarar.app.ui.components.LkHeroPage
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.WorkspaceRepository
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.*
import kotlin.random.Random

private fun requestKey(): String {
    fun hex(n: Int) = (1..n).map { "0123456789abcdef"[Random.nextInt(16)] }.joinToString("")
    return "${hex(8)}-${hex(4)}-4${hex(3)}-a${hex(3)}-${hex(12)}"
}
internal fun JsonObject.value(key: String) = this[key]?.jsonPrimitive?.contentOrNull.orEmpty()
internal fun financeDate(value: String): String = "${LocalDate.parse(value)}T09:00:00+03:00"

@Composable
fun BusinessFinanceScreen(
    workspaceId: String, section: String, repository: WorkspaceRepository,
    onBack: () -> Unit, onRecord: (String) -> Unit
) {
    val title = when(section) { "loans" -> "Krediler"; "accounts" -> "Kasa / Banka"; else -> "Personel" }
    var result by remember(workspaceId, section) { mutableStateOf<JsonObject?>(null) }
    var error by remember { mutableStateOf("") }
    var revision by remember { mutableStateOf(0) }
    var adding by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(workspaceId, section, revision) {
        result = null; error = ""
        repository.finance(workspaceId, section).onSuccess { result = it }.onFailure { error = it.message ?: "Yüklenemedi." }
    }
    LkHeroPage(title = title, onBack = onBack) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(LkSpacing.Space4), verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)) {
            Text(when(section) {
                "loans" -> "Bankanın taksit tutarını girin. Faiz hesaplanmaz; kredi kaydı kasaya para girişi oluşturmaz."
                "accounts" -> "Açılış bakiyesi ve tamamlanan hareketler. Para birimleri ayrı tutulur."
                else -> "Maaş ve SGK tutarlarını siz girersiniz. Bordro hesabı yapılmaz."
            }, color = LkTextSecondary)
            LkButton(text = if (adding) "Vazgeç" else "Yeni ekle", onClick = { adding = !adding })
            if (adding) FinanceCreateForm(workspaceId, section, repository) { adding = false; revision++ }
            if (error.isNotBlank()) { Text(error); LkButton(text = "Yeniden dene", onClick = { revision++ }) }
            val items = result?.get(section)?.jsonArray
            if (result == null && error.isBlank()) Text("Yükleniyor…")
            if (items?.isEmpty() == true) Text("Henüz kayıt yok. Yeni ekle ile başlayın.")
            items?.forEach { element ->
                val item = element.jsonObject
                Divider()
                Text(item.value(if(section == "loans") "institution" else "name"), style = LkTypography.getBodyStrong())
                val currency = item.value("currency")
                when(section) {
                    "loans" -> {
                        Text("Kalan taksit toplamı: ${item.value("remainingBalance")} $currency")
                        Text("${item.value("paidCount")} / ${item.value("installmentCount")} taksit ödendi")
                        var expanded by remember(item.value("id")) { mutableStateOf(false) }
                        TextButton(onClick = { expanded = !expanded }) { Text("Taksit planı") }
                        if (expanded) item["records"]?.jsonArray?.forEach { r ->
                            val record = r.jsonObject
                            TextButton(onClick = { onRecord(record.value("id")) }) {
                                Text("${record.value("installmentNo")}. ${record.value("dueAt").take(10)} · ${record.value("amount")} $currency · ${if(record.value("status") == "completed") "Ödendi" else "Bekliyor"}")
                            }
                        }
                    }
                    "accounts" -> {
                        Text("Kullanılabilir: ${item.value("balance")} $currency")
                        Text("Valör bekleyen: ${item.value("inTransit")} $currency")
                    }
                    else -> {
                        Text("Maaş: ${item.value("salaryAmount")} $currency")
                        Text("Kullanılan izin / hak: ${item.value("leaveUsed")} / ${item.value("leaveAllowance")}")
                        LeaveEntry(workspaceId, item.value("id"), repository) { revision++ }
                    }
                }
            }
        }
    }
}

@Composable
internal fun FinanceTextField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
}

@Composable
private fun FinanceCreateForm(workspaceId: String, section: String, repository: WorkspaceRepository, onSaved: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf("") }
    var currency by rememberSaveable { mutableStateOf("TRY") }
    var count by rememberSaveable { mutableStateOf("12") }
    var installment by rememberSaveable { mutableStateOf("") }
    var bank by rememberSaveable { mutableStateOf(false) }
    var insured by rememberSaveable { mutableStateOf(false) }
    var salaryDate by rememberSaveable { mutableStateOf("") }
    var premiumDate by rememberSaveable { mutableStateOf("") }
    var premium by rememberSaveable { mutableStateOf("") }
    var leave by rememberSaveable { mutableStateOf("0") }
    val key = rememberSaveable { requestKey() }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    FinanceTextField(if(section == "loans") "Kurum" else "Ad", name) { name = it }
    FinanceTextField("Para birimi (TRY, USD, EUR, GBP)", currency) { currency = it.uppercase() }
    FinanceTextField(when(section) { "loans" -> "Kredi anaparası"; "accounts" -> "Açılış bakiyesi"; else -> "Maaş tutarı" }, amount) { amount = it }
    FinanceTextField(when(section) { "loans" -> "İlk taksit (YYYY-AA-GG)"; "accounts" -> "Açılış tarihi (YYYY-AA-GG)"; else -> "İşe giriş (YYYY-AA-GG)" }, date) { date = it }
    when(section) {
        "loans" -> { FinanceTextField("Taksit sayısı", count) { count = it }; FinanceTextField("Bankanın taksit tutarı", installment) { installment = it } }
        "accounts" -> Row { Checkbox(checked = bank, onCheckedChange = { bank = it }); Text("Banka hesabı (kapalıysa kasa)") }
        else -> {
            FinanceTextField("İlk maaş ödeme tarihi (YYYY-AA-GG)", salaryDate) { salaryDate = it }
            FinanceTextField("İzin hakkı (gün, manuel)", leave) { leave = it }
            Row { Checkbox(checked = insured, onCheckedChange = { insured = it }); Text("SGK’lı çalışan") }
            if(insured) {
                FinanceTextField("İlk SGK ödeme tarihi (YYYY-AA-GG)", premiumDate) { premiumDate = it }
                FinanceTextField("SGK primi (manuel)", premium) { premium = it }
                Text("Ödeme tarihini SGK’dan teyit edin.")
            }
        }
    }
    if(error.isNotBlank()) Text(error)
    LkButton(text = if(busy) "Kaydediliyor…" else "Kaydet", enabled = !busy, onClick = {
        val body = runCatching {
            require(name.isNotBlank())
            buildJsonObject {
                put("requestKey", key); put("currency", currency)
                when(section) {
                    "loans" -> { put("institution", name); put("amount", amount.replace(',', '.').toDouble()); put("firstPaymentAt", financeDate(date)); put("installmentCount", count.toInt()); put("installmentAmount", installment.replace(',', '.').toDouble()) }
                    "accounts" -> { put("name", name); put("openingBalance", amount.replace(',', '.').toDouble()); put("openingAt", financeDate(date)); put("type", if(bank) "bank" else "cash") }
                    else -> {
                        put("name", name); put("salaryAmount", amount.replace(',', '.').toDouble()); put("startedAt", financeDate(date)); put("firstSalaryAt", financeDate(salaryDate)); put("leaveAllowance", leave.replace(',', '.').toDouble()); put("insured", insured)
                        if(insured) { put("firstPremiumAt", financeDate(premiumDate)); put("premiumAmount", premium.replace(',', '.').toDouble()) }
                    }
                }
            }
        }.getOrElse { error = "Adı, tutarları ve tarihleri kontrol edin."; return@LkButton }
        busy = true; error = ""
        scope.launch { repository.saveFinance(workspaceId, section, body).onSuccess { onSaved() }.onFailure { error = it.message ?: "Kaydedilemedi." }; busy = false }
    })
}

@Composable
private fun LeaveEntry(workspaceId: String, employeeId: String, repository: WorkspaceRepository, onSaved: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var date by rememberSaveable { mutableStateOf("") }; var days by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf("") }; var busy by remember { mutableStateOf(false) }
    var key by rememberSaveable { mutableStateOf(requestKey()) }; val scope = rememberCoroutineScope()
    TextButton(onClick = { expanded = !expanded }) { Text("İzin kaydı ekle") }
    if(expanded) {
        FinanceTextField("Tarih (YYYY-AA-GG)", date) { date = it }; FinanceTextField("Gün sayısı", days) { days = it }
        if(error.isNotBlank()) Text(error)
        LkButton(text = "İzni kaydet", enabled = !busy, onClick = {
            val body = runCatching { buildJsonObject { put("date", financeDate(date)); put("days", days.replace(',', '.').toDouble()); put("requestKey", key) } }.getOrElse { error = "Tarih ve gün sayısını kontrol edin."; return@LkButton }
            busy = true
            scope.launch { repository.saveFinance(workspaceId, "employees/$employeeId/leaves", body).onSuccess { key = requestKey(); onSaved() }.onFailure { error = it.message ?: "Kaydedilemedi." }; busy = false }
        })
    }
}

