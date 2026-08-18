package com.localkarar.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.localkarar.app.auth.UserDto
import com.localkarar.app.core.rememberFilePicker
import com.localkarar.app.settings.SettingsViewModel
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*

@Composable
fun ProfileScreen(
    viewModel: SettingsViewModel,
    user: UserDto?,
    onNewSession: (String, UserDto) -> Unit
) {
    val launchFilePicker = com.localkarar.app.core.rememberFilePicker { file ->
        if (file != null) {
            viewModel.uploadAvatar(file.name, file.bytes, onNewSession)
        }
    }

    LkPageLayout(title = "Profil", onBack = null) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (user != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = LkSurfacePanel,
                    elevation = 0.dp
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Ad Soyad", style = LkTypography.getMicro(), color = LkTextSecondary)
                        Text(user.name, style = LkTypography.getBodyStrong())
                        Spacer(Modifier.height(12.dp))
                        Text("E-posta", style = LkTypography.getMicro(), color = LkTextSecondary)
                        Text(user.email, style = LkTypography.getBodyStrong())
                        Spacer(Modifier.height(12.dp))
                        Text("Rol", style = LkTypography.getMicro(), color = LkTextSecondary)
                        Text(roleLabel(user.role), style = LkTypography.getBodyStrong())
                    }
                }
                Spacer(Modifier.height(16.dp))
                if (viewModel.avatarLoading) {
                    CircularProgressIndicator(color = LkPrimary)
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { launchFilePicker() }) {
                            Text("Fotoğraf Yükle")
                        }
                        if (user.avatarUrl != null) {
                            OutlinedButton(onClick = { viewModel.removeAvatar() }) {
                                Text("Fotoğrafı Kaldır")
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Fotoğraf PNG veya JPEG, en fazla 5 MB olabilir.",
                    style = LkTypography.getBodySmall(),
                    color = LkTextSecondary,
                    textAlign = TextAlign.Center
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
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = { viewModel.clearNotice() }) { Text("Kapat", color = LkTextSecondary) }
            }
        }
    }
}

fun roleLabel(role: String): String {
    return when (role) {
        "admin" -> "Yönetici"
        "user" -> "Kullanıcı"
        else -> role
    }
}