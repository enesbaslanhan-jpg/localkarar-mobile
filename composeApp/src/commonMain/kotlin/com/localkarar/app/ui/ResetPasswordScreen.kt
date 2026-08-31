package com.localkarar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.localkarar.app.auth.AuthViewModel
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkPasswordTextField
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.theme.*

@Composable
fun ResetPasswordScreen(
    initialToken: String = "",
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    var token by remember { mutableStateOf(initialToken) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val serverError by viewModel.resetError.collectAsState()

    val displayError = localError ?: serverError

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LkSurfaceCanvas)
            .padding(LkSpacing.Space6),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .background(LkSurfacePanel, LkShapes.MD)
                .border(1.dp, LkLineStrong, LkShapes.MD)
                .padding(LkSpacing.Space6)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(LkSurfaceSignature, shape = LkShapes.MD)
                        .border(1.dp, LkLineSoft, LkShapes.MD),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = LkPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(LkSpacing.Space4))

                Text(
                    text = "Yeni Şifre Belirleyin",
                    style = LkTypography.getSectionTitle(),
                    color = LkTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(LkSpacing.Space2))

                Text(
                    text = "E-postanıza iletilen sıfırlama kodunu ve yeni şifrenizi girin.",
                    style = LkTypography.getBodySmall(),
                    color = LkTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(LkSpacing.Space6))

                if (displayError != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LkDanger.copy(alpha = 0.12f), LkShapes.SM)
                            .border(1.dp, LkDanger.copy(alpha = 0.3f), LkShapes.SM)
                            .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space3)
                    ) {
                        Text(
                            text = displayError,
                            color = LkDanger,
                            style = LkTypography.getBodySmall(),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(LkSpacing.Space4))
                }

                LkTextField(
                    value = token,
                    onValueChange = { token = it; localError = null },
                    label = "Sıfırlama Kodu / Token",
                    placeholder = "E-postadaki kod"
                )

                Spacer(modifier = Modifier.height(LkSpacing.Space4))

                LkPasswordTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it; localError = null },
                    label = "Yeni Şifre (En az 8 karakter)",
                    placeholder = "••••••••"
                )

                Spacer(modifier = Modifier.height(LkSpacing.Space4))

                LkPasswordTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; localError = null },
                    label = "Yeni Şifre Tekrar",
                    placeholder = "••••••••"
                )

                Spacer(modifier = Modifier.height(LkSpacing.Space6))

                LkButton(
                    text = if (isLoading) "Güncelleniyor..." else "Şifreyi Güncelle",
                    onClick = {
                        if (newPassword != confirmPassword) {
                            localError = "Girdiğiniz şifreler birbiriyle eşleşmiyor."
                        } else if (newPassword.length < 8) {
                            localError = "Yeni şifre en az 8 karakter olmalıdır."
                        } else if (token.isBlank()) {
                            localError = "Lütfen sıfırlama kodunu girin."
                        } else {
                            localError = null
                            viewModel.confirmPasswordReset(token, newPassword)
                        }
                    },
                    enabled = token.isNotBlank() && newPassword.isNotBlank() && confirmPassword.isNotBlank() && !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(LkSpacing.Space6))

                Text(
                    text = "Giriş ekranına dön",
                    style = LkTypography.getBodySmall(),
                    color = LkTextSecondary,
                    modifier = Modifier
                        .clickable(onClick = onNavigateToLogin)
                        .padding(vertical = LkSpacing.Space2)
                )
            }
        }
    }
}
