package com.localkarar.app.ui.components.decision

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.localkarar.app.network.dto.DecisionQuestionDto
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun LkDecisionInput(
    question: DecisionQuestionDto,
    value: JsonElement?,
    isUnknown: Boolean = false,
    error: String? = null,
    onValueChange: (JsonElement?) -> Unit,
    onUnknownChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        when (question.type) {
            "boolean" -> {
                val isChecked = value?.jsonPrimitive?.booleanOrNull ?: false
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = question.label + if (question.required) " *" else "",
                            style = LkTypography.getSectionTitle(),
                            color = LkTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = question.description,
                            style = LkTypography.getBodySmall(),
                            color = LkTextSecondary
                        )
                    }
                    Switch(
                        checked = isChecked,
                        onCheckedChange = { onValueChange(JsonPrimitive(it)) },
                        enabled = !isUnknown,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = LkOnPrimary,
                            checkedTrackColor = LkPrimary,
                            uncheckedThumbColor = LkTextMuted,
                            uncheckedTrackColor = LkSurfaceSunken
                        )
                    )
                }
            }
            "choice" -> {
                var expanded by remember { mutableStateOf(false) }
                val selectedValue = value?.jsonPrimitive?.doubleOrNull?.toFloat()
                val selectedOption = question.options?.find { it.value == selectedValue }

                Column {
                    Text(
                        text = question.label + if (question.required) " *" else "",
                        style = LkTypography.getSectionTitle(),
                        color = LkTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = question.description,
                        style = LkTypography.getBodySmall(),
                        color = LkTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(LkSurfaceSunken)
                            .border(
                                1.dp,
                                if (error != null) LkDanger else LkLineStrong,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable(enabled = !isUnknown) { expanded = true }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedOption?.label ?: "Seçiniz",
                                style = LkTypography.getBody(),
                                color = if (selectedOption != null) LkTextPrimary else LkTextMuted
                            )
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowDown,
                                contentDescription = "Aç",
                                tint = LkTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    if (selectedOption?.description != null && !isUnknown) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedOption.description,
                            style = LkTypography.getMicro(),
                            color = LkTextSecondary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(LkSurfacePanel)
                    ) {
                        question.options?.forEach { option ->
                            DropdownMenuItem(
                                onClick = {
                                    onValueChange(JsonPrimitive(option.value))
                                    expanded = false
                                }
                            ) {
                                Column {
                                    Text(
                                        text = option.label ?: option.value.toString(),
                                        style = LkTypography.getBody(),
                                        color = LkTextPrimary
                                    )
                                    if (option.description != null) {
                                        Text(
                                            text = option.description,
                                            style = LkTypography.getMicro(),
                                            color = LkTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                // money, percentage, number, days, months
                val textValue = value?.jsonPrimitive?.doubleOrNull?.let { 
                    if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() 
                } ?: ""
                
                LkTextField(
                    value = textValue,
                    onValueChange = { input ->
                        if (input.isBlank()) {
                            onValueChange(null)
                        } else {
                            // Ensure only numbers and a single decimal point
                            val filtered = input.filter { it.isDigit() || it == '.' }
                            if (filtered.count { it == '.' } <= 1) {
                                val d = filtered.toDoubleOrNull()
                                if (d != null) {
                                    onValueChange(JsonPrimitive(d))
                                }
                            }
                        }
                    },
                    label = question.label + if (question.required) " *" else "",
                    placeholder = question.description,
                    error = error,
                    enabled = !isUnknown,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (question.type in listOf("days", "months", "count", "integer")) KeyboardType.Number else KeyboardType.Decimal
                    ),
                    trailingContent = question.suffix?.let {
                        {
                            Text(
                                text = it,
                                style = LkTypography.getBody(),
                                color = LkTextMuted,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                )
            }
        }
        if (question.allowUnknown) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUnknownChange(!isUnknown) }
                    .padding(top = 8.dp)
            ) {
                Checkbox(
                    checked = isUnknown,
                    onCheckedChange = { onUnknownChange(it) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = LkPrimary,
                        uncheckedColor = LkLineStrong
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bilmiyorum / Emin Değilim",
                    style = LkTypography.getBodySmall(),
                    color = LkTextSecondary
                )
            }
        }
        if (error != null && !isUnknown) {
            Text(
                text = error,
                color = LkDanger,
                style = LkTypography.getMicro(),
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}




