package com.localkarar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.LkDanger
import com.localkarar.app.ui.theme.LkPrimary
import com.localkarar.app.ui.theme.LkShapes
import com.localkarar.app.ui.theme.LkSurfaceSunken
import com.localkarar.app.ui.theme.LkTextMuted
import com.localkarar.app.ui.theme.LkTextPrimary
import com.localkarar.app.ui.theme.LkTextSecondary
import com.localkarar.app.ui.theme.LkTypography

@Composable
fun LkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    error: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    enabled: Boolean = true,
    trailingContent: @Composable (() -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                style = LkTypography.getBodyStrong().copy(color = LkTextSecondary),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        val borderColor = when {
            error != null -> LkDanger
            isFocused -> LkPrimary
            else -> Color.Transparent
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(if (enabled) LkSurfaceSunken else LkSurfaceSunken.copy(alpha = 0.5f), LkShapes.SM)
                .border(1.dp, borderColor, LkShapes.SM)
                .onFocusChanged { isFocused = it.isFocused },
            enabled = enabled,
            textStyle = LkTypography.getBodySmall().copy(color = if (enabled) LkTextPrimary else LkTextMuted),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            cursorBrush = SolidColor(LkPrimary),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = LkTypography.getBodySmall().copy(color = LkTextMuted)
                            )
                        }
                        innerTextField()
                    }
                }
            }
        )

        if (error != null) {
            Text(
                text = error,
                style = LkTypography.getMetadata().copy(color = LkDanger),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

