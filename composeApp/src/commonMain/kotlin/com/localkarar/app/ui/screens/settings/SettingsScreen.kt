package com.localkarar.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*

@Composable
fun SettingsScreen(
    userName: String,
    userEmail: String,
    onOpenProfile: () -> Unit,
    onOpenWorkspaces: () -> Unit,
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(LkShapes.MD)
                    .border(1.dp, LkLineStrong, LkShapes.MD)
                    .clickable(onClick = onOpenProfile),
                backgroundColor = LkSurfacePanel,
                elevation = 0.dp
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(LkShapes.FULL)
                            .background(LkSurfaceSignature),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase().ifBlank { "U" },
                            style = LkTypography.getSectionTitle(),
                            color = LkPrimary
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(userName, style = LkTypography.getBodyStrong(), color = LkTextPrimary, fontWeight = FontWeight.Bold)
                        Text(userEmail, style = LkTypography.getBodySmall(), color = LkTextSecondary)
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Profil",
                        tint = LkTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Section: Profil ve İşletme
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionHeader("PROFİL VE İŞLETME")
                SettingItem(
                    label = "Profil Bilgileri",
                    description = "Kişisel detaylar ve tercihler",
                    icon = Icons.Default.Person,
                    onClick = onOpenProfile
                )
                SettingItem(
                    label = "İşletmelerim",
                    description = "Bağlı işletmeleri görüntüle veya değiştir",
                    icon = Icons.Default.Business,
                    onClick = onOpenWorkspaces
                )
            }

            // Section: Güvenlik ve Gizlilik
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionHeader("GÜVENLİK VE GİZLİLİK")
                SettingItem(
                    label = "Şifre Değiştir",
                    description = "Giriş şifrenizi güncelleyin",
                    icon = Icons.Default.Lock,
                    onClick = onOpenPassword
                )
                SettingItem(
                    label = "E-posta Değiştir",
                    description = "Hesabınıza bağlı e-posta adresini güncelleyin",
                    icon = Icons.Default.Email,
                    onClick = onOpenEmail
                )
            }

            // Section: Tercihler & Uygulama
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionHeader("UYGULAMA VE TERCİHLER")
                SettingItem(
                    label = "Dil / Language",
                    description = "Türkçe (Varsayılan)",
                    icon = Icons.Default.Language,
                    onClick = { /* Default */ }
                )
                SettingItem(
                    label = "Sürüm",
                    description = "LocalKarar Native v2.0.0 (Compose Multiplatform)",
                    icon = Icons.Default.Info,
                    onClick = { /* Info */ }
                )
            }

            // Section: Hesap İşlemleri
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionHeader("HESAP")
                SettingItem(
                    label = "Hesabımı Sil",
                    description = "Tüm verileriniz kalıcı olarak silinir",
                    icon = Icons.Default.DeleteOutline,
                    onClick = onOpenDeleteAccount,
                    danger = true
                )
            }

            Spacer(Modifier.height(8.dp))

            // Logout Button
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = LkSurfacePanel,
                    contentColor = LkDanger
                ),
                shape = LkShapes.MD,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(1.dp, LkLineStrong, LkShapes.MD)
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    tint = LkDanger,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Çıkış Yap",
                    style = LkTypography.getBodyStrong(),
                    color = LkDanger
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = LkTypography.getMetadata(),
        color = LkPrimary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun SettingItem(
    label: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    danger: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LkShapes.MD)
            .border(1.dp, LkLineSoft, LkShapes.MD)
            .clickable(onClick = onClick),
        backgroundColor = LkSurfacePanel,
        elevation = 0.dp
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(LkShapes.SM)
                    .background(if (danger) LkSurfaceSunken else LkSurfaceSunken),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (danger) LkDanger else LkPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = LkTypography.getBody(),
                    color = if (danger) LkDanger else LkTextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = LkTypography.getMicro(),
                    color = LkTextSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = LkTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}