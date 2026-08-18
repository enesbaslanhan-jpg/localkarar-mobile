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
import com.localkarar.app.settings.SettingsViewModel
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*

@Composable
fun PasswordChangeScreen(viewModel: SettingsViewModel) {
    LkPageLayout(title = "Şifre Değiştir", onBack = null) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.passwordCurrent,
                onValueChange = { viewModel.onPasswordCurrentChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Mevcut Şifre") },
                singleLine = true,
                textStyle = LkTypography.getBody()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = viewModel.passwordNew,
                onValueChange = { viewModel.onPasswordNewChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Yeni Şifre (en az 8 karakter)") },
                singleLine = true,
                textStyle = LkTypography.getBody()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = viewModel.passwordConfirm,
                onValueChange = { viewModel.onPasswordConfirmChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Yeni Şifre (Tekrar)") },
                singleLine = true,
                textStyle = LkTypography.getBody()
            )
            Spacer(Modifier.height(24.dp))
            if (viewModel.passwordLoading) {
                CircularProgressIndicator(
                    Modifier.align(Alignment.CenterHorizontally),
                    color = LkPrimary
                )
            } else {
                LkButton(
                    text = "Şifreyi Güncelle",
                    onClick = { viewModel.changePassword() },
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