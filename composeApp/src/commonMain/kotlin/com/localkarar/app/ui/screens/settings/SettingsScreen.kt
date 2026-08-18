package com.localkarar.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*

@Composable
fun SettingsScreen(
    userName: String,
    userEmail: String,
    onOpenProfile: () -> Unit,
    onOpenPassword: () -> Unit,
    onOpenEmail: () -> Unit,
    onOpenDeleteAccount: () -> Unit,
    onLogout: () -> Unit
) {
    LkPageLayout(title = "Ayarlar", onBack = null) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenProfile),
                backgroundColor = LkSurfacePanel,
                elevation = 0.dp
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(userName, style = LkTypography.getBodyStrong(), fontWeight = FontWeight.Bold)
                        Text(userEmail, style = LkTypography.getBodySmall(), color = LkTextSecondary)
                    }
                    Text("→", color = LkPrimary, style = LkTypography.getBodyStrong())
                }
            }
            Spacer(Modifier.height(16.dp))
            SectionTitle("Hesap")
            SettingItem("Şifre Değiştir", onOpenPassword)
            SettingItem("E-posta Değiştir", onOpenEmail)
            SettingItem("Hesabımı Sil", onOpenDeleteAccount, danger = true)
            Spacer(Modifier.height(24.dp))
            TextButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Çıkış Yap", color = LkDanger, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = LkTypography.getMetadata(),
        color = LkPrimary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SettingItem(
    label: String,
    onClick: () -> Unit,
    danger: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick),
        backgroundColor = LkSurfacePanel,
        elevation = 0.dp
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = LkTypography.getBody(),
                color = if (danger) LkDanger else LkTextPrimary,
                modifier = Modifier.weight(1f)
            )
            Text("→", color = LkTextSecondary)
        }
    }
}