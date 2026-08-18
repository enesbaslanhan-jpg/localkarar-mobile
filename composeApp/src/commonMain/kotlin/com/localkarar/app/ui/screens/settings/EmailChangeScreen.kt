package com.localkarar.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onNewSession: (String, UserDto) -> Unit
) {
    LkPageLayout(title = "E-posta Değiştir", onBack = null) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.emailNew,
                onValueChange = { viewModel.onEmailNewChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Yeni E-posta") },
                singleLine = true,
                textStyle = LkTypography.getBody()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = viewModel.emailCurrentPassword,
                onValueChange = { viewModel.onEmailCurrentPasswordChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Mevcut Şifre") },
                singleLine = true,
                textStyle = LkTypography.getBody()
            )
            Spacer(Modifier.height(24.dp))
            if (viewModel.emailLoading) {
                CircularProgressIndicator(
                    Modifier.align(Alignment.CenterHorizontally),
                    color = LkPrimary
                )
            } else {
                LkButton(
                    text = "E-postayı Güncelle",
                    onClick = { viewModel.changeEmail(onNewSession) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            viewModel.notice?.let {
                Spacer(Modifier.height(16.dp))
                Text(
                    it,
                    style = LkTypography.getBodySmall(),
                    color = if (viewModel.noticeIsError) LkDanger else LkPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}