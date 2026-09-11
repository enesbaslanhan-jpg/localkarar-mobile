package com.localkarar.app.ui.screens.workspaces

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.workspaces.WorkspaceRepository
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.random.Random

internal fun requestKey(): String {
    fun hex(length: Int) = (1..length)
        .map { "0123456789abcdef"[Random.nextInt(16)] }
        .joinToString("")
    return "${hex(8)}-${hex(4)}-4${hex(3)}-a${hex(3)}-${hex(12)}"
}

internal fun JsonObject.value(key: String) = this[key]?.jsonPrimitive?.contentOrNull.orEmpty()

internal fun financeDate(value: String): String = "${LocalDate.parse(value)}T09:00:00+03:00"

@Composable
internal fun FinanceTextField(label: String, value: String, onChange: (String) -> Unit) {
    LkTextField(value = value, onValueChange = onChange, label = label)
}

@Composable
fun BusinessFinanceScreen(
    workspaceId: String,
    section: String,
    repository: WorkspaceRepository,
    onBack: () -> Unit,
    onRecord: (String) -> Unit,
    onAddAccount: () -> Unit,
    onAddLoan: () -> Unit,
    onAddEmployee: () -> Unit,
    onAddLeave: (String) -> Unit
) {
    var result by remember(workspaceId, section) { mutableStateOf<JsonObject?>(null) }
    var error by remember(workspaceId, section) { mutableStateOf("") }
    var revision by remember(workspaceId, section) { mutableStateOf(0) }

    LaunchedEffect(workspaceId, section, revision) {
        result = null
        error = ""
        repository.finance(workspaceId, section)
            .onSuccess { result = it }
            .onFailure { error = it.message ?: "Yüklenemedi." }
    }

    when (section) {
        "accounts" -> AccountListScreen(
            result = result,
            error = error,
            onRetry = { revision++ },
            onBack = onBack,
            onAddAccount = onAddAccount
        )
        "loans" -> LoanListScreen(
            result = result,
            error = error,
            onRetry = { revision++ },
            onBack = onBack,
            onAddLoan = onAddLoan,
            onRecord = onRecord
        )
        else -> EmployeeListScreen(
            result = result,
            error = error,
            onRetry = { revision++ },
            onBack = onBack,
            onAddEmployee = onAddEmployee,
            onAddLeave = onAddLeave
        )
    }
}
