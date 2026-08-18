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
fun DeleteAccountScreen(
    viewModel: SettingsViewModel,
    onDeleted: () -> Unit
) {
    LkPageLayout(title = "Hesabımı Sil", onBack = null) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = LkSurfaceSunken,
                elevation = 0.dp
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Hesabınız ve tüm verileriniz kalıcı olarak silinir. Bu işlem geri alınamaz.",
                        style = LkTypography.getBodySmall(),
                        color = LkDanger
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = viewModel.deletePassword,
                onValueChange = { viewModel.onDeletePasswordChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Mevcut Şifre") },
                singleLine = true,
                textStyle = LkTypography.getBody()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = viewModel.deleteConfirmation,
                onValueChange = { viewModel.onDeleteConfirmationChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Onay için: HESABIMI SİL") },
                singleLine = true,
                textStyle = LkTypography.getBody()
            )
            Spacer(Modifier.height(24.dp))
            if (viewModel.deleteLoading) {
                CircularProgressIndicator(
                    Modifier.align(Alignment.CenterHorizontally),
                    color = LkPrimary
                )
            } else {
                LkButton(
                    text = "Hesabımı Kalıcı Olarak Sil",
                    variant = LkButtonVariant.SECONDARY,
                    onClick = { viewModel.deleteAccount(onDeleted) },
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