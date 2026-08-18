package com.localkarar.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.localkarar.app.auth.AuthViewModel
import com.localkarar.app.ui.components.LkPasswordTextField
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.theme.*

@Composable
fun LoginScreen(viewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.loginError.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LkSurfaceCanvas)
            .systemBarsPadding()
            .padding(LkSpacing.Space6),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .background(LkSurfacePanel, LkShapes.MD)
                .padding(LkSpacing.Space6)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Placeholder logo
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(LkPrimary, shape = LkShapes.MD),
                    contentAlignment = Alignment.Center
                ) {
                    Text("LK", style = LkTypography.getSectionTitle(), color = LkOnPrimary)
                }
                
                Spacer(modifier = Modifier.height(LkSpacing.Space6))
                
                Text(
                    text = "LocalKarar'a Giriş Yap",
                    style = LkTypography.getSectionTitle(),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(LkSpacing.Space2))
                
                Text(
                    text = "Devam etmek için kurumsal bilgilerinizi girin.",
                    style = LkTypography.getBodySmall(),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(LkSpacing.Space8))
                
                if (error != null) {
                    Text(
                        text = error!!,
                        color = LkDanger,
                        style = LkTypography.getBodyStrong(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = LkSpacing.Space4),
                        textAlign = TextAlign.Center
                    )
                }

                LkTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Kurumsal E-posta"
                )
                
                Spacer(modifier = Modifier.height(LkSpacing.Space4))
                
                LkPasswordTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Şifre"
                )
                
                Spacer(modifier = Modifier.height(LkSpacing.Space8))
                
                LkButton(
                    text = if (isLoading) "Giriş Yapılıyor..." else "Giriş Yap",
                    onClick = { viewModel.login(email, password) },
                    enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

