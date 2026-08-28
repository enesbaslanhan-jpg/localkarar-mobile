package com.localkarar.app.ui.screens.settings

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
import com.localkarar.app.settings.SettingsViewModel
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*

@Composable
fun DeleteAccountScreen(
    viewModel: SettingsViewModel,
    onDeleted: () -> Unit,
    onBack: () -> Unit
) {
    LkPageLayout(title = "Hesabımı Sil", onBack = onBack) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = LkDanger.copy(alpha = 0.12f),
                elevation = 0.dp
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "DİKKAT: Hesabınız ve tüm ilişkili verileriniz (çalışma alanları, kayıtlar, topluluk paylaşımları) kalıcı olarak silinecektir. Bu işlem geri alınamaz.",
                        style = LkTypography.getBodySmall(),
                        color = LkDanger
                    )
                }
            }

            OutlinedTextField(
                value = viewModel.deletePassword,
                onValueChange = { viewModel.onDeletePasswordChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Mevcut Şifre") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                textStyle = LkTypography.getBody()
            )

            OutlinedTextField(
                value = viewModel.deleteConfirmation,
                onValueChange = { viewModel.onDeleteConfirmationChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Onaylamak için 'HESABIMI SİL' yazınız") },
                singleLine = true,
                textStyle = LkTypography.getBody()
            )

            Spacer(Modifier.height(8.dp))

            if (viewModel.deleteLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LkDanger)
                }
            } else {
                LkButton(
                    text = "Hesabımı Kalıcı Olarak Sil",
                    variant = LkButtonVariant.SECONDARY,
                    onClick = { viewModel.deleteAccount(onDeleted) },
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