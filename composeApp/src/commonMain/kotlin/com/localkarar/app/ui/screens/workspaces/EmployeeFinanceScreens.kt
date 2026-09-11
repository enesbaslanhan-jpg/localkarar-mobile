package com.localkarar.app.ui.screens.workspaces

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.core.LkFormatting
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonSize
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkChip
import com.localkarar.app.ui.components.LkDateField
import com.localkarar.app.ui.components.LkEmptyState
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkHairline
import com.localkarar.app.ui.components.LkHeroPage
import com.localkarar.app.ui.components.LkListRow
import com.localkarar.app.ui.components.LkLoadingDesen
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkNumericField
import com.localkarar.app.ui.components.LkRowGroup
import com.localkarar.app.ui.components.LkSectionHeader
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.theme.LkHero
import com.localkarar.app.ui.theme.LkPrimary
import com.localkarar.app.ui.theme.LkSpacing
import com.localkarar.app.ui.theme.LkTextSecondary
import com.localkarar.app.workspaces.WorkspaceRepository
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

private val EMPLOYEE_CURRENCIES = listOf("TRY", "USD", "EUR", "GBP")

@Composable
internal fun EmployeeListScreen(
    result: JsonObject?,
    error: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onAddEmployee: () -> Unit,
    onAddLeave: (String) -> Unit
) {
    LkHeroPage(
        title = "Personel",
        onBack = onBack,
        actions = {
            IconButton(onClick = onAddEmployee) {
                Icon(Icons.Outlined.Add, contentDescription = "Çalışan ekle", tint = LkHero.OnHero)
            }
        }
    ) {
        when {
            result == null && error.isBlank() -> LkLoadingState(desen = LkLoadingDesen.LISTE)
            error.isNotBlank() -> LkErrorState(message = error, onRetry = onRetry)
            else -> {
                val employees = result?.get("employees")?.jsonArray.orEmpty()
                if (employees.isEmpty()) {
                    LkEmptyState(
                        title = "İlk çalışanınızı ekleyin",
                        description = "Maaş planı ve izin hakkı çalışan kaydıyla birlikte oluşturulur.",
                        icon = Icons.Outlined.Badge,
                        action = { LkButton(text = "Çalışan ekle", onClick = onAddEmployee) },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(LkSpacing.Space4),
                        verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
                    ) {
                        item {
                            Text(
                                "Maaş ve SGK tutarları sizin girdiğiniz değerlerdir; bordro veya prim hesabı yapılmaz.",
                                color = LkTextSecondary
                            )
                        }
                        items(employees.size) { index ->
                            val employee = employees[index].jsonObject
                            val currency = employee.value("currency")
                            val used = employee.value("leaveUsed").toDoubleOrNull()
                            val allowance = employee.value("leaveAllowance").toDoubleOrNull()
                            LkRowGroup {
                                LkListRow(
                                    baslik = employee.value("name"),
                                    altBaslik = "${if (employee.value("insured") == "true") "SGK’lı çalışan" else "SGK kaydı yok"} · $currency",
                                    tutar = LkFormatting.formatMoney(employee.value("salaryAmount").toDoubleOrNull(), currency),
                                    ikon = { Icon(Icons.Outlined.Badge, contentDescription = null, tint = LkPrimary) }
                                )
                                LkHairline()
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space2),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
                                ) {
                                    Text(
                                        "Kullanılan izin: ${LkFormatting.formatNumber(used)} / ${LkFormatting.formatNumber(allowance)} gün",
                                        color = LkTextSecondary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    LkButton(
                                        text = "İzin ekle",
                                        onClick = { onAddLeave(employee.value("id")) },
                                        variant = LkButtonVariant.QUIET,
                                        size = LkButtonSize.SM
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmployeeCreateScreen(
    workspaceId: String,
    repository: WorkspaceRepository,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var currency by rememberSaveable { mutableStateOf("TRY") }
    var salaryAmount by rememberSaveable { mutableStateOf("") }
    var startedAt by rememberSaveable { mutableStateOf<LocalDate?>(LkDateUtils.today()) }
    var firstSalaryAt by rememberSaveable { mutableStateOf<LocalDate?>(LkDateUtils.today()) }
    var leaveAllowance by rememberSaveable { mutableStateOf("0") }
    var insured by rememberSaveable { mutableStateOf(false) }
    var firstPremiumAt by rememberSaveable { mutableStateOf<LocalDate?>(LkDateUtils.today()) }
    var premiumAmount by rememberSaveable { mutableStateOf("") }
    val requestKey = rememberSaveable { requestKey() }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LkHeroPage(title = "Yeni çalışan", onBack = onBack) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(LkSpacing.Space4),
            verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
        ) {
            item { Text("Maaş ve SGK tutarlarını resmi kayıtlarınızdaki haliyle girin.", color = LkTextSecondary) }
            item { LkTextField(value = name, onValueChange = { name = it; error = "" }, label = "Ad soyad", placeholder = "Çalışanın adı") }
            item {
                LkSectionHeader(title = "Para birimi")
                Spacer(Modifier.height(LkSpacing.Space2))
                Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                    EMPLOYEE_CURRENCIES.forEach { value -> LkChip(text = value, selected = currency == value, onClick = { currency = value }) }
                }
            }
            item { LkNumericField(value = salaryAmount, onValueChange = { salaryAmount = it; error = "" }, label = "Aylık maaş", placeholder = "0,00", suffix = currency) }
            item { LkDateField(label = "İşe giriş tarihi", date = startedAt, onDateSelected = { startedAt = it; error = "" }) }
            item { LkDateField(label = "İlk maaş ödeme tarihi", date = firstSalaryAt, onDateSelected = { firstSalaryAt = it; error = "" }) }
            item { LkNumericField(value = leaveAllowance, onValueChange = { leaveAllowance = it; error = "" }, label = "Yıllık izin hakkı (gün)", placeholder = "0") }
            item {
                LkSectionHeader(title = "SGK durumu")
                Spacer(Modifier.height(LkSpacing.Space2))
                Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                    LkChip(text = "SGK kaydı yok", selected = !insured, onClick = { insured = false })
                    LkChip(text = "SGK’lı çalışan", selected = insured, onClick = { insured = true })
                }
            }
            if (insured) {
                item { LkDateField(label = "İlk SGK ödeme tarihi", date = firstPremiumAt, onDateSelected = { firstPremiumAt = it; error = "" }) }
                item { LkNumericField(value = premiumAmount, onValueChange = { premiumAmount = it; error = "" }, label = "SGK prim tutarı", placeholder = "0,00", suffix = currency) }
                item { Text("SGK ödeme tarihini ve tutarını resmi kaydınızdan teyit edin.", color = LkTextSecondary) }
            }
            if (error.isNotBlank()) item { Text(error, color = com.localkarar.app.ui.theme.LkDanger) }
            item {
                LkButton(
                    text = if (busy) "Kaydediliyor…" else "Çalışanı kaydet",
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val salary = LkFormatting.parseDecimal(salaryAmount)
                        val allowance = LkFormatting.parseDecimal(leaveAllowance)
                        val premium = if (insured) LkFormatting.parseDecimal(premiumAmount) else null
                        val startDate = startedAt
                        val salaryDate = firstSalaryAt
                        val premiumDate = firstPremiumAt
                        if (name.isBlank() || salary == null || salary < 0 || allowance == null || allowance !in 0.0..366.0 || (allowance * 2) % 1 != 0.0 || startDate == null || salaryDate == null || salaryDate < startDate || (insured && (premium == null || premium < 0 || premiumDate == null))) {
                            error = "Ad, tutarlar ve tarihleri kontrol edin. İlk maaş tarihi işe girişten önce olamaz."
                            return@LkButton
                        }
                        val body = buildJsonObject {
                            put("name", name.trim()); put("salaryAmount", salary); put("currency", currency)
                            put("startedAt", financeDate(startDate.toString())); put("firstSalaryAt", financeDate(salaryDate.toString()))
                            put("leaveAllowance", allowance); put("insured", insured); put("requestKey", requestKey)
                            if (insured && premium != null && premiumDate != null) {
                                put("firstPremiumAt", financeDate(premiumDate.toString())); put("premiumAmount", premium)
                            }
                        }
                        busy = true; error = ""
                        scope.launch {
                            repository.saveFinance(workspaceId, "employees", body)
                                .onSuccess { onSaved() }
                                .onFailure { error = it.message ?: "Çalışan kaydedilemedi." }
                            busy = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun EmployeeLeaveScreen(
    workspaceId: String,
    employeeId: String,
    repository: WorkspaceRepository,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    var date by rememberSaveable { mutableStateOf<LocalDate?>(LkDateUtils.today()) }
    var days by rememberSaveable { mutableStateOf("") }
    val requestKey = rememberSaveable { requestKey() }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LkHeroPage(title = "İzin kaydı", onBack = onBack) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(LkSpacing.Space4),
            verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
        ) {
            item { Text("Kullanılan izin süresini yarım gün hassasiyetinde kaydedebilirsiniz.", color = LkTextSecondary) }
            item { LkDateField(label = "İzin tarihi", date = date, onDateSelected = { date = it; error = "" }) }
            item { LkNumericField(value = days, onValueChange = { days = it; error = "" }, label = "Kullanılan gün", placeholder = "0,5") }
            if (error.isNotBlank()) item { Text(error, color = com.localkarar.app.ui.theme.LkDanger) }
            item {
                LkButton(
                    text = if (busy) "Kaydediliyor…" else "İzni kaydet",
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val parsedDays = LkFormatting.parseDecimal(days)
                        val leaveDate = date
                        if (parsedDays == null || parsedDays <= 0 || parsedDays > 366 || (parsedDays * 2) % 1 != 0.0 || leaveDate == null) {
                            error = "İzin tarihi ve gün sayısını kontrol edin. Yarım gün adımları kullanın."
                            return@LkButton
                        }
                        val body = buildJsonObject {
                            put("date", financeDate(leaveDate.toString())); put("days", parsedDays); put("requestKey", requestKey)
                        }
                        busy = true; error = ""
                        scope.launch {
                            repository.saveFinance(workspaceId, "employees/$employeeId/leaves", body)
                                .onSuccess { onSaved() }
                                .onFailure { error = it.message ?: "İzin kaydedilemedi." }
                            busy = false
                        }
                    }
                )
            }
        }
    }
}
