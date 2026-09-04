package com.localkarar.app.ui.screens.settings

import androidx.compose.ui.unit.sp
import com.localkarar.app.ui.components.LkHairline
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
import androidx.compose.material.icons.outlined.*
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
import com.localkarar.app.ui.components.LkPillChip
import com.localkarar.app.ui.theme.LocalThemeController
import com.localkarar.app.ui.theme.ThemeMode
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
    onOpenSupport: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenGuide: () -> Unit,
    onOpenNotifications: () -> Unit,
    onLogoutAll: (() -> Unit)? = null,
    onLogout: () -> Unit
) {
    var showLogoutAllDialog by remember { mutableStateOf(false) }

    if (showLogoutAllDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutAllDialog = false },
            title = { Text("Diğer Cihazlardan Çık", style = LkTypography.getBodyStrong(), color = LkTextPrimary) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "Bu cihaz haricindeki tüm diğer cihaz ve tarayıcılardaki aktif oturumlarınız sonlandırılacaktır. Devam etmek istiyor musunuz?",
                        style = LkTypography.getBodySmall(),
                        color = LkTextSecondary
                    )
                }
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
                        imageVector = Icons.Outlined.ChevronRight,
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
                    icon = Icons.Outlined.Person,
                    onClick = onOpenProfile
                )
                SettingItem(
                    label = "E-posta Değiştir",
                    description = "Hesabınıza bağlı e-posta adresini güncelleyin",
                    icon = Icons.Outlined.Email,
                    onClick = onOpenEmail
                )
                SettingItem(
                    label = "Şifre Değiştir",
                    description = "Giriş şifrenizi güncelleyin (en az 10 karakter)",
                    icon = Icons.Outlined.Lock,
                    onClick = onOpenPassword
                )
            }

            // Section: İşletme
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionHeader("İŞLETME")
                SettingItem(
                    label = "İşletmelerim",
                    description = "Bağlı işletmeleri görüntüle veya değiştir",
                    icon = Icons.Outlined.Business,
                    onClick = onOpenWorkspaces
                )
                if (activeWorkspaceId != null && onOpenWorkspaceSettings != null) {
                    SettingItem(
                        label = "İşletme Ayarları",
                        description = "Para birimi, saat dilimi ve bildirimler",
                        icon = Icons.Outlined.Tune,
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
                    icon = Icons.Outlined.Description,
                    onClick = onOpenConsents
                )
            }

            // Section: Oturum
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionHeader("OTURUM")
                SettingItem(
                    label = "Diğer Cihazlardaki Oturumları Kapat",
                    description = "Bu cihaz haricindeki tüm açık oturumları sonlandır",
                    icon = Icons.Outlined.Devices,
                    onClick = { showLogoutAllDialog = true }
                )
            }

            /*
             * GORUNUM.
             *
             * Webde tema secimi ust cubuktaki dugmede; mobilde kalici bir ust
             * cubuk olmadigi icin Ayarlar'a kondu. Uc secenek de webdeki
             * `ThemeContext` ile ayni: secim yapilmazsa SISTEM tercihi.
             *
             * Secim cikista SILINMEZ (`AppPreferences`), webde de oturumdan
             * bagimsiz.
             */
            val themeController = LocalThemeController.current
            if (themeController != null) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SectionHeader("GÖRÜNÜM")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)
                    ) {
                        listOf(
                            ThemeMode.SYSTEM to "Sistem",
                            ThemeMode.LIGHT to "Açık",
                            ThemeMode.DARK to "Koyu"
                        ).forEach { (mod, etiket) ->
                            LkPillChip(
                                label = etiket,
                                selected = themeController.mode == mod,
                                onClick = { themeController.select(mod) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Section: Yardım
            //
            // Bu bölüm mobilde YOKTU. Webde `/yardim` sayfası var; mobilde
            // karşılığı olmadığı için salt okunur moda düşen kullanıcı
            // uygulamadan destek isteyemiyordu (destek formu üyelik
            // kapısından muaf olan tek yazma yolu).
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionHeader("YARDIM")
                SettingItem(
                    label = "Bildirimler",
                    description = "Hesabınızla ilgili gelişmeler",
                    icon = Icons.Outlined.NotificationsNone,
                    onClick = onOpenNotifications
                )
                SettingItem(
                    label = "Kullanım Kılavuzu",
                    description = "Uygulamanın temel akışlarını öğrenin",
                    icon = Icons.Outlined.MenuBook,
                    onClick = onOpenGuide
                )
                SettingItem(
                    label = "LocalKarar Hakkında",
                    description = "Amaç, kapsam ve sorumluluklar",
                    icon = Icons.Outlined.Info,
                    onClick = onOpenAbout
                )
                SettingItem(
                    label = "Destek",
                    description = "Sorun bildirin, bize yazın",
                    icon = Icons.Outlined.HelpOutline,
                    onClick = onOpenSupport
                )
            }

            // Section: Hesap İşlemleri
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionHeader("HESAP İŞLEMLERİ")
                SettingItem(
                    label = "Hesabımı Sil",
                    description = "Tüm verileriniz kalıcı olarak silinir",
                    icon = Icons.Outlined.DeleteOutline,
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
                        imageVector = Icons.Outlined.ExitToApp,
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
        style = LkTypography.getLabel(),
        color = LkTextMuted,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(top = LkSpacing.Space4, bottom = LkSpacing.Space1)
    )
}

/**
 * Ayarlar satiri.
 *
 * 🔴 ONCEDEN HER SATIR AYRI KART IDI: 13 satirlik ekran bastan sona kart
 * yiginiydi ve uygulamanin geri kalaniyla ayni dili konusmuyordu. Prototipte
 * `.section-block` cercevesiz; satirlari sac teli cizgi ayirir.
 *
 * 🔴 IKON KUTUSU `LkSurfaceSignature` KULLANIYORDU (brand-700, temadan
 * BAGIMSIZ koyu lacivert). Acik temada aydinlik bir yuzeyin uzerinde koyu
 * bloklar olarak duruyordu. Prototipteki `.tactile-icon-box` ise
 * `--surface-subtle` + ince kenarlik; artik o.
 */
@Composable
private fun SettingItem(
    label: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    danger: Boolean = false
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = LkSpacing.Space3),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    if (danger) LkDanger.copy(alpha = 0.10f) else LkSurfaceSunken,
                    LkShapes.SM
                )
                .border(
                    1.dp,
                    if (danger) LkDanger.copy(alpha = 0.30f) else LkLineSoft,
                    LkShapes.SM
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (danger) LkDanger else LkPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(LkSpacing.Space4))
        Column(Modifier.weight(1f)) {
            Text(
                text = label,
                style = LkTypography.getBodyStrong(),
                color = if (danger) LkDanger else LkTextPrimary
            )
            Text(
                text = description,
                style = LkTypography.getMetadata(),
                color = LkTextMuted
            )
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = if (danger) LkDanger.copy(alpha = 0.6f) else LkTextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
    LkHairline()
}
