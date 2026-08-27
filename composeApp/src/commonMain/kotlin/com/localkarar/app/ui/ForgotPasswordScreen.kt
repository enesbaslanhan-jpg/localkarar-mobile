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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.localkarar.app.auth.AuthViewModel
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.theme.*

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onNavigateToResetPassword: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.resetError.collectAsState()
    val resetSuccess by viewModel.resetSuccess.collectAsState()

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
                        imageVector = if (resetSuccess) Icons.Default.CheckCircle else Icons.Default.LockReset,
                        contentDescription = null,
                        tint = if (resetSuccess) LkSuccess else LkPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(LkSpacing.Space4))

                Text(
                    text = if (resetSuccess) "E-postanızı Kontrol Edin" else "Şifrenizi Sıfırlayın",
                    style = LkTypography.getSectionTitle(),
                    color = LkTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(LkSpacing.Space2))

                Text(
                    text = if (resetSuccess) {
                        "$email adresi sistemde kayıtlıysa şifre sıfırlama bağlantısı gönderildi. Bağlantı 1 saat geçerlidir."
                    } else {
                        "Hesabınızın e-posta adresini girin; sıfırlama bağlantısını iletelim."
                    },
                    style = LkTypography.getBodySmall(),
                    color = LkTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(LkSpacing.Space6))

                if (error != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LkDanger.copy(alpha = 0.12f), LkShapes.SM)
                            .border(1.dp, LkDanger.copy(alpha = 0.3f), LkShapes.SM)
                            .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space3)
                    ) {
                        Text(
                            text = error!!,
                            color = LkDanger,
                            style = LkTypography.getBodySmall(),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(LkSpacing.Space4))
                }

                if (!resetSuccess) {
                    LkTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Kayıtlı E-posta Adresi",
                        placeholder = "adiniz@sirketiniz.com"
                    )

                    Spacer(modifier = Modifier.height(LkSpacing.Space6))

                    LkButton(
                        text = if (isLoading) "Gönderiliyor..." else "Sıfırlama Bağlantısı Gönder",
                        onClick = { viewModel.requestPasswordReset(email) },
                        enabled = email.isNotBlank() && !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(LkSpacing.Space4))

                    Text(
                        text = "Sıfırlama kodum var",
                        style = LkTypography.getBodySmall(),
                        color = LkPrimary,
                        modifier = Modifier
                            .clickable(onClick = onNavigateToResetPassword)
                            .padding(vertical = LkSpacing.Space2)
                    )
                } else {
                    LkButton(
                        text = "Sıfırlama Kodunu Gir",
                        onClick = onNavigateToResetPassword,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(LkSpacing.Space4))

                    LkButton(
                        text = "Giriş Ekranına Dön",
                        variant = LkButtonVariant.SECONDARY,
                        onClick = onNavigateToLogin,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(LkSpacing.Space4))

                if (!resetSuccess) {
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
}
