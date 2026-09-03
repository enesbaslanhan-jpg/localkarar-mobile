package com.localkarar.app.ui.screens.settings

import com.localkarar.app.core.SecureScreen
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.localkarar.app.auth.UserDto
import com.localkarar.app.settings.SettingsViewModel
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*

@Composable
fun EmailChangeScreen(
    viewModel: SettingsViewModel,
    onNewSession: (String, UserDto) -> Unit,
    onBack: () -> Unit
) {
    // Kimlik bilgisi girilen ekran: ekran goruntusu ve son-uygulamalar
    // kucuk resmi engelleniyor. Gerekce SecureScreen belgesinde.
    SecureScreen()
    LkPageLayout(title = "E-posta Değiştir", onBack = onBack) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Hesabınıza bağlı e-posta adresini güncelleyebilirsiniz. Güvenliğiniz için mevcut şifrenizi doğrulamanız gerekmektedir.",
                style = LkTypography.getBodySmall(),
                color = LkTextSecondary
            )

            OutlinedTextField(
                value = viewModel.emailNew,
                onValueChange = { viewModel.onEmailNewChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Yeni E-posta Adresi") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                textStyle = LkTypography.getBody()
            )

            OutlinedTextField(
                value = viewModel.emailCurrentPassword,
                onValueChange = { viewModel.onEmailCurrentPasswordChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Mevcut Şifre") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                textStyle = LkTypography.getBody()
            )

            Spacer(Modifier.height(8.dp))

            if (viewModel.emailLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LkPrimary)
                }
            } else {
                LkButton(
                    text = "E-postayı Güncelle",
                    onClick = { viewModel.changeEmail(onNewSession) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            viewModel.notice?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = if (viewModel.noticeIsError) LkDanger.copy(alpha = 0.15f) else LkSuccess.copy(alpha = 0.15f),
                    elevation = 0.dp
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = it,
                            style = LkTypography.getBodySmall(),
                            color = if (viewModel.noticeIsError) LkDanger else LkSuccess,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.clearNotice() }) {
                            Text("Tamam", color = LkTextPrimary)
                        }
                    }
                }
            }
        }
    }
}