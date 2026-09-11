package com.localkarar.app.ui.screens.workspaces

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.core.LkFormatting
import com.localkarar.app.ui.components.LkButton
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
import com.localkarar.app.ui.theme.LkSuccess
import com.localkarar.app.ui.theme.LkTextSecondary
import com.localkarar.app.workspaces.WorkspaceRepository
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

private val LOAN_CURRENCIES = listOf("TRY", "USD", "EUR", "GBP")

@Composable
internal fun LoanListScreen(
    result: JsonObject?,
    error: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onAddLoan: () -> Unit,
    onRecord: (String) -> Unit
) {
    var expandedLoanId by rememberSaveable { mutableStateOf<String?>(null) }

    LkHeroPage(
        title = "Krediler",
        onBack = onBack,
        actions = {
            IconButton(onClick = onAddLoan) {
                Icon(Icons.Outlined.Add, contentDescription = "Kredi ekle", tint = LkHero.OnHero)
            }
        }
    ) {
        when {
            result == null && error.isBlank() -> LkLoadingState(desen = LkLoadingDesen.LISTE)
            error.isNotBlank() -> LkErrorState(message = error, onRetry = onRetry)
            else -> {
                val loans = result?.get("loans")?.jsonArray.orEmpty()
                if (loans.isEmpty()) {
                    LkEmptyState(
                        title = "İlk kredinizi ekleyin",
                        description = "Bankanın bildirdiği taksit planını kaydedin; kalan borç otomatik güncellensin.",
                        icon = Icons.Outlined.AccountBalance,
                        action = { LkButton(text = "Kredi ekle", onClick = onAddLoan) },
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
                                "Faiz veya taksit hesabı yapılmaz. Bankanın verdiği plan esas alınır; kredi kaydı kasaya para girişi oluşturmaz.",
                                color = LkTextSecondary
                            )
                        }
                        items(loans.size) { index ->
                            val loan = loans[index].jsonObject
                            val loanId = loan.value("id")
                            val currency = loan.value("currency")
                            val expanded = expandedLoanId == loanId
                            LkRowGroup {
                                LkListRow(
                                    baslik = loan.value("institution"),
                                    altBaslik = "${loan.value("paidCount")} / ${loan.value("installmentCount")} taksit ödendi · $currency",
                                    kategori = "Kalan",
                                    tutar = LkFormatting.formatMoney(loan.value("remainingBalance").toDoubleOrNull(), currency),
                                    onClick = { expandedLoanId = if (expanded) null else loanId },
                                    ikon = { Icon(Icons.Outlined.AccountBalance, contentDescription = null, tint = LkPrimary) }
                                )
                                if (expanded) {
                                    LkHairline()
                                    Column(Modifier.padding(vertical = LkSpacing.Space2)) {
                                        LkListRow(
                                            baslik = "Toplam geri ödeme",
                                            tutar = LkFormatting.formatMoney(loan.value("totalRepayment").toDoubleOrNull(), currency)
                                        )
                                        loan["records"]?.jsonArray?.forEach { element ->
                                            val record = element.jsonObject
                                            val paid = record.value("status") == "completed"
                                            LkHairline()
                                            LkListRow(
                                                baslik = "${record.value("installmentNo")}. taksit",
                                                altBaslik = LkDateUtils.formatDate(record.value("dueAt")),
                                                kategori = if (paid) "Ödendi" else "Bekliyor",
                                                tutar = LkFormatting.formatMoney(record.value("amount").toDoubleOrNull(), currency),
                                                tutarRengi = if (paid) LkSuccess else com.localkarar.app.ui.theme.LkTextPrimary,
                                                onClick = { onRecord(record.value("id")) }
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
    }
}

@Composable
fun LoanCreateScreen(
    workspaceId: String,
    repository: WorkspaceRepository,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    var institution by rememberSaveable { mutableStateOf("") }
    var currency by rememberSaveable { mutableStateOf("TRY") }
    var amount by rememberSaveable { mutableStateOf("") }
    var installmentCount by rememberSaveable { mutableStateOf("12") }
    var installmentAmount by rememberSaveable { mutableStateOf("") }
    var firstPaymentDate by rememberSaveable { mutableStateOf<LocalDate?>(LkDateUtils.today()) }
    val requestKey = rememberSaveable { requestKey() }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LkHeroPage(title = "Yeni kredi", onBack = onBack) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(LkSpacing.Space4),
            verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
        ) {
            item { Text("Rakamları bankanın ödeme planından aynen girin. LocalKarar faiz hesabı yapmaz.", color = LkTextSecondary) }
            item {
                LkTextField(
                    value = institution,
                    onValueChange = { institution = it; error = "" },
                    label = "Banka veya kurum",
                    placeholder = "Örn: İşletme kredisi"
                )
            }
            item {
                LkSectionHeader(title = "Para birimi")
                Spacer(Modifier.height(LkSpacing.Space2))
                Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                    LOAN_CURRENCIES.forEach { value ->
                        LkChip(text = value, selected = currency == value, onClick = { currency = value })
                    }
                }
            }
            item {
                LkNumericField(value = amount, onValueChange = { amount = it; error = "" }, label = "Kredi anaparası", placeholder = "0,00", suffix = currency)
            }
            item {
                LkNumericField(value = installmentCount, onValueChange = { installmentCount = it; error = "" }, label = "Taksit sayısı", placeholder = "12")
            }
            item {
                LkNumericField(value = installmentAmount, onValueChange = { installmentAmount = it; error = "" }, label = "Bankanın bildirdiği taksit tutarı", placeholder = "0,00", suffix = currency)
            }
            item {
                LkDateField(label = "İlk taksit tarihi", date = firstPaymentDate, onDateSelected = { firstPaymentDate = it; error = "" })
            }
            if (error.isNotBlank()) item { Text(error, color = com.localkarar.app.ui.theme.LkDanger) }
            item {
                LkButton(
                    text = if (busy) "Kaydediliyor…" else "Krediyi kaydet",
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val principal = LkFormatting.parseDecimal(amount)
                        val payment = LkFormatting.parseDecimal(installmentAmount)
                        val count = installmentCount.toIntOrNull()
                        val date = firstPaymentDate
                        if (institution.isBlank() || principal == null || principal <= 0 || payment == null || payment <= 0 || count == null || count !in 1..360 || date == null) {
                            error = "Kurum, tutar, taksit sayısı ve tarihi kontrol edin."
                            return@LkButton
                        }
                        val body = buildJsonObject {
                            put("institution", institution.trim())
                            put("amount", principal)
                            put("currency", currency)
                            put("installmentCount", count)
                            put("installmentAmount", payment)
                            put("firstPaymentAt", financeDate(date.toString()))
                            put("requestKey", requestKey)
                        }
                        busy = true
                        error = ""
                        scope.launch {
                            repository.saveFinance(workspaceId, "loans", body)
                                .onSuccess { onSaved() }
                                .onFailure { error = it.message ?: "Kredi kaydedilemedi." }
                            busy = false
                        }
                    }
                )
            }
        }
    }
}
