package com.localkarar.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.localkarar.app.settings.SettingsViewModel
import com.localkarar.app.settings.roleLabel
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*

@Composable
fun SettingsScreen(
    userName: String,
    userEmail: String,
    userRole: String? = null,
    userAvatarUrl: String? = null,
    activeWorkspaceId: String? = null,
    viewModel: SettingsViewModel? = null,
    onOpenProfile: () -> Unit,
    onOpenWorkspaces: () -> Unit,
    onOpenWorkspaceSettings: ((String) -> Unit)? = null,
    onOpenPassword: () -> Unit,
    onOpenEmail: () -> Unit,
    onOpenConsents: () -> Unit,
    onOpenDeleteAccount: () -> Unit,
    onLogoutAll: (() -> Unit)? = null,
    onLogout: () -> Unit
) {
    var showLogoutAllDialog by remember { mutableStateOf(false) }

    if (showLogoutAllDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutAllDialog = false },
            title = { Text("Diğer Cihazlardan Çık", style = LkTypography.getBodyStrong(), color = LkTextPrimary) },
            text = {
                Text(
                    "Bu cihaz haricindeki tüm diğer cihaz ve tarayıcılardaki aktif oturumlarınız sonlandırılacaktır. Devam etmek istiyor musunuz?",
                    style = LkTypography.getBodySmall(),
                    color = LkTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutAllDialog = false
                        onLogoutAll?.invoke()
                    }
                ) {
                    Text("Oturumları Kapat", color = LkPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutAllDialog = false }) {
                    Text("Vazgeç", color = LkTextSecondary)
                }
            },
            backgroundColor = LkSurfacePanel
        )
    }

    LkPageLayout(title = "Ayarlar", onBack = null) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header Card
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
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(LkSurfaceSignature)
                            .border(1.dp, LkLineStrong, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase().ifBlank { "U" },
                            style = LkTypography.getSectionTitle(),
                            color = LkPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userName,
                                style = LkTypography.getBodyStrong(),
                                color = LkTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LkPrimary.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = roleLabel(userRole),
                                    style = LkTypography.getMicro(),
                                    color = LkPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(Modifier.height(2.dp))
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

            viewModel?.notice?.let {
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

            // Section: Hesap
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionHeader("HESAP")
                SettingItem(
                    label = "Profil Bilgileri",
                    description = "Görünen ad ve profil fotoğrafı",
                    icon = Icons.Default.Person,
                    onClick = onOpenProfile
                )
                SettingItem(
                    label = "E-posta Değiştir",
                    description = "Hesabınıza bağlı e-posta adresini güncelleyin",
                    icon = Icons.Default.Email,
                    onClick = onOpenEmail
                )
                SettingItem(
                    label = "Şifre Değiştir",
                    description = "Giriş şifrenizi güncelleyin (en az 10 karakter)",
                    icon = Icons.Default.Lock,
                    onClick = onOpenPassword
                )
            }

            // Section: İşletme
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionHeader("İŞLETME")
                SettingItem(
                    label = "İşletmelerim",
                    description = "Bağlı işletmeleri görüntüle veya değiştir",
                    icon = Icons.Default.Business,
                    onClick = onOpenWorkspaces
                )
                if (activeWorkspaceId != null && onOpenWorkspaceSettings != null) {
                    SettingItem(
                        label = "İşletme Ayarları",
                        description = "Para birimi, saat dilimi ve bildirimler",
                        icon = Icons.Default.Tune,
                        onClick = { onOpenWorkspaceSettings(activeWorkspaceId) }
                    )
                }
            }

            // Section: Gizlilik ve Yasal
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionHeader("GİZLİLİK VE YASAL")
                SettingItem(
                    label = "Yasal Bilgiler ve Onaylar",
                    description = "Kullanım koşulları, KVKK ve onay durumu",
                    icon = Icons.Default.Description,
                    onClick = onOpenConsents
                )
            }

            // Section: Oturum
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionHeader("OTURUM")
                SettingItem(
                    label = "Diğer Cihazlardaki Oturumları Kapat",
                    description = "Bu cihaz haricindeki tüm açık oturumları sonlandır",
                    icon = Icons.Default.Devices,
                    onClick = { showLogoutAllDialog = true }
                )
            }

            // Section: Hesap İşlemleri
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionHeader("HESAP İŞLEMLERİ")
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
                border = ButtonDefaults.outlinedBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(LkDanger.copy(alpha = 0.5f))
                ),
                elevation = ButtonDefaults.elevation(0.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Çıkış Yap",
                        tint = LkDanger,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Çıkış Yap",
                        style = LkTypography.getBodyStrong(),
                        color = LkDanger
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = LkTypography.getMicro(),
        color = LkTextSecondary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
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
            .border(
                1.dp,
                if (danger) LkDanger.copy(alpha = 0.3f) else LkLineSoft,
                LkShapes.MD
            )
            .clickable(onClick = onClick),
        backgroundColor = LkSurfacePanel,
        elevation = 0.dp
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(LkShapes.SM)
                    .background(if (danger) LkDanger.copy(alpha = 0.1f) else LkSurfaceSignature),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (danger) LkDanger else LkPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = LkTypography.getBody(),
                    color = if (danger) LkDanger else LkTextPrimary,
                    fontWeight = FontWeight.SemiBold
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
                tint = if (danger) LkDanger.copy(alpha = 0.6f) else LkTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}