package com.localkarar.app.ui.screens.settings

import androidx.compose.ui.unit.sp
import com.localkarar.app.ui.components.LkNotice
import com.localkarar.app.ui.components.LkSegment
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
import com.localkarar.app.ui.components.LkHeroPage
import com.localkarar.app.ui.components.LkPillChip
import com.localkarar.app.ui.theme.LocalThemeController
import com.localkarar.app.ui.theme.ThemeMode
import com.localkarar.app.ui.components.LkAvatar
import com.localkarar.app.ui.components.LkCard
import com.localkarar.app.ui.components.LkPressable
import com.localkarar.app.ui.components.LkRowGroup
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
    /**
     * Takip ve engelleme.
     *
     * 🔴 TOPLULUK PROFILININ ICINDE AYRI BIR KART OLARAK DURUYORDU:
     * kimlik blogunun altina sikismis, profile ait olmayan bir ayar
     * satiriydi. Ait oldugu yer Ayarlar — takip ettiklerin ve
     * engellediklerin HESABINA ait.
     */
    onOpenFollowBlock: (() -> Unit)? = null,
    onOpenWorkspaces: () -> Unit,
    /** Kurulum ve degerlendirme — `null` ise satir cizilmiyor. */
    onOpenOnboarding: (() -> Unit)? = null,
    onOpenAssessment: (() -> Unit)? = null,
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

    LkHeroPage(title = "Ayarlar", onBack = null) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header Card
            /* §24: kenarlikli Material Card degil yukseltilmis LkCard. */
            LkPressable(onClick = onOpenProfile, modifier = Modifier.fillMaxWidth()) {
              LkCard {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LkAvatar(ad = userName, boyut = 48.dp)
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
            }
            viewModel?.notice?.let {
                LkNotice(
                    metin = it,
                    hataMi = viewModel.noticeIsError,
                    onKapat = { viewModel.clearNotice() }
                )
            }
            /*
             * GORUNUM EN USTTE — mockup "Ayar 1"de profil satirinin hemen
             * altinda. Onceden oturum ve gizlilik bolumlerinin ARKASINDA,
             * ekranin ortasinda kaliyordu; en cok dokunulan ayar en zor
             * bulunan yerdeydi.
             */
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
                    /*
                     * Mockup'ta bu bir SEGMENT: uc secenek tek yolun
                     * icinde. Ayri ayri duran haplar coklu secim gibi
                     * okunuyordu; tema secimi tek secimdir.
                     */
                    LkSegment(
                        secenekler = listOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM),
                        secili = themeController.mode,
                        etiket = { mod ->
                            when (mod) {
                                ThemeMode.LIGHT -> "Açık"
                                ThemeMode.DARK -> "Koyu"
                                ThemeMode.SYSTEM -> "Sistem"
                            }
                        },
                        onSelect = { themeController.select(it) }
                    )
                }
            }

            // Section: Hesap
            Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                SectionHeader("HESAP")
                LkRowGroup {
                SettingItem(
                    label = "Profili düzenle",
                    description = "Görünen ad ve profil fotoğrafı",
                    icon = Icons.Outlined.Person,
                    onClick = onOpenProfile
                )
                if (onOpenFollowBlock != null) {
                    SettingItem(
                        label = "Takip ve engelleme",
                        description = "Takip ettiklerin ve engellediklerin",
                        icon = Icons.Outlined.PeopleOutline,
                        onClick = onOpenFollowBlock
                    )
                }
                SettingItem(
                    label = "E-posta Değiştir",
                    description = "Hesabınıza bağlı e-posta adresini güncelleyin",
                    icon = Icons.Outlined.Email,
                    onClick = onOpenEmail
                )
                SettingItem(
                    label = "Parola değiştir",
                    description = "Giriş parolanızı güncelleyin (en az 10 karakter)",
                    icon = Icons.Outlined.Lock,
                    onClick = onOpenPassword,
                    ayrac = false
                )
                }
            }

            // Section: İşletme
            Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                SectionHeader("İŞLETME")
                LkRowGroup {
                /*
                 * KURULUM ve DEGERLENDIRME — mobilde ilk kez.
                 *
                 * ⚠️ Ayarlara konuldu, ilk acilista ZORLA gosterilmiyor.
                 * Web de zorunlu tutmuyor; kurulum bir kolaylik, kapi
                 * degil. Kullanici hazir oldugunda buradan aciyor.
                 */
                onOpenOnboarding?.let { ac ->
                    SettingItem(
                        label = "İşletme kurulumu",
                        description = "Sektör, kanallar ve hedeflerini gir",
                        icon = Icons.Outlined.Tune,
                        onClick = ac
                    )
                }
                onOpenAssessment?.let { ac ->
                    SettingItem(
                        label = "İşletme değerlendirmesi",
                        description = "Güçlü ve zayıf alanlarını ölç",
                        icon = Icons.Outlined.Assessment,
                        onClick = ac
                    )
                }
                SettingItem(
                    label = "İşletmelerim",
                    description = "Bağlı işletmeleri görüntüle veya değiştir",
                    icon = Icons.Outlined.Business,
                    onClick = onOpenWorkspaces,
                    ayrac = activeWorkspaceId != null && onOpenWorkspaceSettings != null
                )
                if (activeWorkspaceId != null && onOpenWorkspaceSettings != null) {
                    SettingItem(
                        label = "İşletme Ayarları",
                        description = "Para birimi, saat dilimi ve bildirimler",
                        icon = Icons.Outlined.Tune,
                        onClick = { onOpenWorkspaceSettings(activeWorkspaceId) },
                        ayrac = false
                    )
                }
                }
            }

            // Section: Gizlilik ve Yasal
            Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                SectionHeader("GİZLİLİK VE YASAL")
                LkRowGroup {
                SettingItem(
                    label = "Yasal izinler",
                    description = "Kullanım koşulları, KVKK ve onay durumu",
                    icon = Icons.Outlined.Description,
                    onClick = onOpenConsents,
                    ayrac = false
                )
                }
            }

            // Section: Oturum
            Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                SectionHeader("OTURUM")
                LkRowGroup {
                SettingItem(
                    label = "Diğer Cihazlardaki Oturumları Kapat",
                    description = "Bu cihaz haricindeki tüm açık oturumları sonlandır",
                    icon = Icons.Outlined.Devices,
                    onClick = { showLogoutAllDialog = true },
                    ayrac = false
                )
                }
            }


            // Section: Yardım
            //
            // Bu bölüm mobilde YOKTU. Webde `/yardim` sayfası var; mobilde
            // karşılığı olmadığı için salt okunur moda düşen kullanıcı
            // uygulamadan destek isteyemiyordu (destek formu üyelik
            // kapısından muaf olan tek yazma yolu).
            Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                SectionHeader("YARDIM")
                LkRowGroup {
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
                    onClick = onOpenSupport,
                    ayrac = false
                )
                }
            }

            // Section: Hesap İşlemleri
            Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                SectionHeader("HESAP İŞLEMLERİ")
                LkRowGroup {
                SettingItem(
                    label = "Hesabı sil",
                    description = "Tüm verileriniz kalıcı olarak silinir",
                    icon = Icons.Outlined.DeleteOutline,
                    onClick = onOpenDeleteAccount,
                    danger = true,
                    ayrac = false
                )
                }
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
    danger: Boolean = false,
    /** Bolumun son satirinda kapali; grubun alt kenarina cizgi yapismasin. */
    ayrac: Boolean = true
) {
    /*
     * §24 ikon kutucugu: 44dp, 15dp yaricap, LkSurfaceTile zemin.
     * Onceki 36dp kutunun kenarligi vardi -- olculdugunde kutu ile yuzey
     * arasindaki kontrast 1.14 idi, sinir gorulmuyordu; kenarlik o eksigi
     * kapatmak icin konmustu. Olculmus LkSurfaceTile ile kenarlik gereksiz.
     */
    LkPressable(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
      Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = LkSpacing.PadCard, vertical = LkSpacing.Space3),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(15.dp))
                .background(if (danger) LkDanger.copy(alpha = 0.12f) else LkSurfaceTile),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (danger) LkDanger else LkTileInk,
                modifier = Modifier.size(21.dp)
            )
        }
        Spacer(Modifier.width(LkSpacing.Space3))
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
    }
    if (ayrac) LkHairline()
}
