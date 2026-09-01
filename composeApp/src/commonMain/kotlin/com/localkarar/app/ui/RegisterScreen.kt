package com.localkarar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Text
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
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var legalAccepted by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.registerError.collectAsState()

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
                // Brand Header
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(LkSurfaceSignature, shape = LkShapes.MD)
                        .border(1.dp, LkLineSoft, LkShapes.MD),
                    contentAlignment = Alignment.Center
                ) {
                    Text("LK", style = LkTypography.getSectionTitle(), color = LkPrimary, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(LkSpacing.Space4))

                Text(
                    text = "LocalKarar Hesabı Oluştur",
                    style = LkTypography.getSectionTitle(),
                    color = LkTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(LkSpacing.Space2))

                Text(
                    text = "Girişiminiz ve işletmeniz için profesyonel karar ekosistemi.",
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

                LkTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Ad Soyad",
                    placeholder = "Adınız ve Soyadınız"
                )

                Spacer(modifier = Modifier.height(LkSpacing.Space4))

                LkTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Kurumsal E-posta",
                    placeholder = "adiniz@sirketiniz.com"
                )

                Spacer(modifier = Modifier.height(LkSpacing.Space4))

                LkPasswordTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Şifre (En az 10 karakter)",
                    placeholder = "••••••••"
                )

                Spacer(modifier = Modifier.height(LkSpacing.Space4))

                // Legal Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { legalAccepted = !legalAccepted }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = legalAccepted,
                        onCheckedChange = { legalAccepted = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = LkPrimary,
                            uncheckedColor = LkTextSecondary,
                            checkmarkColor = LkOnPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Kullanım Koşulları ve Gizlilik Politikası'nı okudum, onaylıyorum.",
                        style = LkTypography.getMicro(),
                        color = LkTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(LkSpacing.Space6))

                LkButton(
                    text = if (isLoading) "Hesap Oluşturuluyor..." else "Kayıt Ol",
                    onClick = { viewModel.register(name, email, password, legalAccepted) },
                    enabled = name.isNotBlank() && email.isNotBlank() && password.length >= 10 && legalAccepted && !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(LkSpacing.Space6))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Zaten hesabınız var mı?",
                        style = LkTypography.getBodySmall(),
                        color = LkTextSecondary
                    )
                    Spacer(modifier = Modifier.width(LkSpacing.Space2))
                    Text(
                        text = "Giriş Yap",
                        style = LkTypography.getBodySmall(),
                        color = LkPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onNavigateToLogin)
                    )
                }
            }
        }
    }
}
