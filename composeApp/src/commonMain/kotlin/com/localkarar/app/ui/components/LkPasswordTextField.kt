package com.localkarar.app.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation

@Composable
fun LkPasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    LkTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
    )
}
