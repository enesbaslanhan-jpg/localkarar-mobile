package com.localkarar.app.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun LkNumericField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    label: String? = null,
    placeholder: String = "",
    error: String? = null,
    suffix: String? = null
) {
    LkTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        error = error,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        trailingContent = if (suffix != null) {
            {
                androidx.compose.material.Text(
                    text = suffix,
                    style = com.localkarar.app.ui.theme.LkTypography.getMetadata(),
                    color = com.localkarar.app.ui.theme.LkTextMuted
                )
            }
        } else null
    )
}