package com.localkarar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.TextButton
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
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    var email by remember { mutableStateOf("admin@localakademi.com") }
    var password by remember { mutableStateOf("admin123") }
    
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.loginError.collectAsState()

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
                // App Brand
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
                    text = "LocalKarar'a Giriş Yap",
                    style = LkTypography.getSectionTitle(),
                    color = LkTextPrimary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(LkSpacing.Space2))
                
                Text(
                    text = "Devam etmek için kurumsal hesabınıza giriş yapın.",
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
                    value = email,
                    onValueChange = { email = it },
                    label = "Kurumsal E-posta",
                    placeholder = "ornek@sirket.com"
                )
                
                Spacer(modifier = Modifier.height(LkSpacing.Space4))
                
                LkPasswordTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Şifre",
                    placeholder = "••••••••"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Şifremi unuttum",
                        style = LkTypography.getMicro(),
                        color = LkPrimary,
                        modifier = Modifier
                            .clickable(onClick = onNavigateToForgotPassword)
                            .padding(vertical = LkSpacing.Space2)
                    )
                }
                
                Spacer(modifier = Modifier.height(LkSpacing.Space4))
                
                LkButton(
                    text = if (isLoading) "Giriş Yapılıyor..." else "Giriş Yap",
                    onClick = { viewModel.login(email, password) },
                    enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(LkSpacing.Space6))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Hesabınız yok mu?",
                        style = LkTypography.getBodySmall(),
                        color = LkTextSecondary
                    )
                    Spacer(modifier = Modifier.width(LkSpacing.Space2))
                    Text(
                        text = "Kayıt Ol",
                        style = LkTypography.getBodySmall(),
                        color = LkPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onNavigateToRegister)
                    )
                }
            }
        }
    }
}
