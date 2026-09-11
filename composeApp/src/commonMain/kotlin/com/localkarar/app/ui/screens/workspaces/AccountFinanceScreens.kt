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
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Wallet
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
import com.localkarar.app.ui.theme.LkTextSecondary
import com.localkarar.app.workspaces.WorkspaceRepository
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

private val ACCOUNT_CURRENCIES = listOf("TRY", "USD", "EUR", "GBP")

@Composable
internal fun AccountListScreen(
    result: JsonObject?,
    error: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onAddAccount: () -> Unit
) {
    LkHeroPage(
        title = "Kasa / Banka",
        onBack = onBack,
        actions = {
            IconButton(onClick = onAddAccount) {
                Icon(Icons.Outlined.Add, contentDescription = "Hesap ekle", tint = LkHero.OnHero)
            }
        }
    ) {
        when {
            result == null && error.isBlank() -> LkLoadingState(desen = LkLoadingDesen.LISTE)
            error.isNotBlank() -> LkErrorState(message = error, onRetry = onRetry)
            else -> {
                val accounts = result?.get("accounts")?.jsonArray.orEmpty()
                if (accounts.isEmpty()) {
                    LkEmptyState(
                        title = "İlk hesabınızı ekleyin",
                        description = "Kasa ve banka bakiyeleri para birimine göre ayrı gösterilir.",
                        icon = Icons.Outlined.AccountBalanceWallet,
                        action = { LkButton(text = "Hesap ekle", onClick = onAddAccount) },
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
                                "Kullanılabilir bakiye ve valör bekleyen tutar sunucudaki hareketlerden hesaplanır. Para birimleri birbirine eklenmez.",
                                color = LkTextSecondary
                            )
                        }
                        item {
                            LkRowGroup {
                                accounts.forEachIndexed { index, element ->
                                    val account = element.jsonObject
                                    val currency = account.value("currency")
                                    val isBank = account.value("type") == "bank"
                                    val inTransit = account.value("inTransit").toDoubleOrNull() ?: 0.0
                                    LkListRow(
                                        baslik = account.value("name"),
                                        altBaslik = buildString {
                                            append(if (isBank) "Banka" else "Kasa")
                                            append(" · ").append(currency)
                                            if (inTransit != 0.0) {
                                                append(" · Valör: ").append(LkFormatting.formatMoney(inTransit, currency))
                                            }
                                        },
                                        tutar = LkFormatting.formatMoney(account.value("balance").toDoubleOrNull(), currency),
                                        ikon = {
                                            Icon(
                                                if (isBank) Icons.Outlined.AccountBalance else Icons.Outlined.Wallet,
                                                contentDescription = null,
                                                tint = LkPrimary
                                            )
                                        }
                                    )
                                    if (index < accounts.lastIndex) LkHairline()
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
fun AccountCreateScreen(
    workspaceId: String,
    repository: WorkspaceRepository,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf("cash") }
    var currency by rememberSaveable { mutableStateOf("TRY") }
    var amount by rememberSaveable { mutableStateOf("") }
    var openingDate by rememberSaveable { mutableStateOf<LocalDate?>(LkDateUtils.today()) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LkHeroPage(title = "Yeni hesap", onBack = onBack) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(LkSpacing.Space4),
            verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
        ) {
            item {
                Text(
                    "Açılış bakiyesi yalnızca seçtiğiniz para biriminde tutulur.",
                    color = LkTextSecondary
                )
            }
            item {
                LkTextField(
                    value = name,
                    onValueChange = { name = it; error = "" },
                    label = "Hesap adı",
                    placeholder = "Örn: Merkez kasa"
                )
            }
            item {
                LkSectionHeader(title = "Hesap türü")
                Spacer(Modifier.height(LkSpacing.Space2))
                Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                    listOf("cash" to "Kasa", "bank" to "Banka").forEach { (value, label) ->
                        LkChip(text = label, selected = type == value, onClick = { type = value })
                    }
                }
            }
            item {
                LkSectionHeader(title = "Para birimi")
                Spacer(Modifier.height(LkSpacing.Space2))
                Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                    ACCOUNT_CURRENCIES.forEach { value ->
                        LkChip(text = value, selected = currency == value, onClick = { currency = value })
                    }
                }
            }
            item {
                LkNumericField(
                    value = amount,
                    onValueChange = { amount = it; error = "" },
                    label = "Açılış bakiyesi",
                    placeholder = "0,00",
                    suffix = currency
                )
            }
            item {
                LkDateField(
                    label = "Açılış tarihi",
                    date = openingDate,
                    onDateSelected = { openingDate = it; error = "" }
                )
            }
            if (error.isNotBlank()) {
                item { Text(error, color = com.localkarar.app.ui.theme.LkDanger) }
            }
            item {
                LkButton(
                    text = if (busy) "Kaydediliyor…" else "Hesabı kaydet",
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val parsedAmount = LkFormatting.parseDecimal(amount)
                        val date = openingDate
                        if (name.isBlank() || parsedAmount == null || date == null) {
                            error = "Hesap adını, açılış bakiyesini ve tarihi kontrol edin."
                            return@LkButton
                        }
                        val body = buildJsonObject {
                            put("name", name.trim())
                            put("type", type)
                            put("currency", currency)
                            put("openingBalance", parsedAmount)
                            put("openingAt", financeDate(date.toString()))
                        }
                        busy = true
                        error = ""
                        scope.launch {
                            repository.saveFinance(workspaceId, "accounts", body)
                                .onSuccess { onSaved() }
                                .onFailure { error = it.message ?: "Hesap kaydedilemedi." }
                            busy = false
                        }
                    }
                )
            }
        }
    }
}
