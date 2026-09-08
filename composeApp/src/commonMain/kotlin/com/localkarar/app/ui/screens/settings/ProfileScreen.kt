package com.localkarar.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.localkarar.app.auth.UserDto
import com.localkarar.app.core.rememberFilePicker
import com.localkarar.app.settings.SettingsViewModel
import com.localkarar.app.settings.roleLabel
import com.localkarar.app.ui.components.LkAvatar
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkHeroPage
import com.localkarar.app.ui.components.LkLoadingSpinner
import com.localkarar.app.ui.components.LkNotice
import com.localkarar.app.ui.components.LkRemoteImage
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.theme.*

/**
 * Mockup "Ayar 2 — Profili düzenle".
 *
 * 🔴 EKRAN MOCKUP'IN YARISI KADARDI: yalnizca avatar ve ad vardi. Kapak
 * fotografi, hakkinda, konum ve site alanlari mockup'ta duruyor, sunucu
 * dordunu de kabul ediyor (`PATCH /auth/profile`, `POST /auth/cover`) ve
 * `UserDto` dordunu de tasiyor — mobilde girilecek yer yoktu.
 *
 * ⚠️ MOCKUP'TAN TEK EKSIK: "Kullanıcı adı". Sunucuda `username` diye bir
 * alan YOK; kaydedilmeyecek bir kutu koymak, kullanicinin yazdigini
 * sessizce yutmak olurdu.
 *
 * Kapak ve avatar mockup'taki gibi ust uste: avatar kapagin alt sinirina
 * biniyor.
 */
@Composable
fun ProfileScreen(
    viewModel: SettingsViewModel,
    user: UserDto?,
    onNewSession: (String, UserDto) -> Unit,
    onBack: () -> Unit
) {
    val currentUser = viewModel.user ?: user

    /* Alanlar ekran acilirken mevcut degerlerle doluyor. */
    LaunchedEffect(currentUser?.id) { viewModel.profilAlanlariniHazirla() }

    val avatarSec = rememberFilePicker { file ->
        if (file != null) viewModel.uploadAvatar(file.name, file.bytes, onNewSession)
    }
    val kapakSec = rememberFilePicker { file ->
        if (file != null) viewModel.uploadCover(file.name, file.bytes, onNewSession)
    }

    LkHeroPage(
        title = "Profili düzenle",
        onBack = onBack,
        actions = {
            LkButton(
                text = if (viewModel.nameLoading) "..." else "Kaydet",
                onClick = { viewModel.saveProfile { updated -> onNewSession("", updated) } },
                enabled = !viewModel.nameLoading,
                size = com.localkarar.app.ui.components.LkButtonSize.SM,
                modifier = Modifier.padding(end = LkSpacing.Space2)
            )
        }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = LkSpacing.Space8),
            verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
        ) {
            if (currentUser != null) {
                /*
                 * KAPAK + AVATAR. Avatar kapagin ALT SINIRINA biniyor
                 * (mockup "Ekran 4" ve "Ayar 2"); bu yuzden kapak
                 * blogunun altinda avatarin yarisi kadar bosluk var.
                 */
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(LkSurfaceSignature),
                        contentAlignment = Alignment.Center
                    ) {
                        val kapak = currentUser.coverUrl
                        if (!kapak.isNullOrBlank()) {
                            LkRemoteImage(
                                url = kapak,
                                contentDescription = "Kapak fotoğrafı",
                                modifier = Modifier.fillMaxSize(),
                                yedek = {}
                            )
                        }
                        if (viewModel.coverLoading) {
                            LkLoadingSpinner(size = 24.dp)
                        } else {
                            LkButton(
                                text = "Kapağı değiştir",
                                variant = LkButtonVariant.SECONDARY,
                                size = com.localkarar.app.ui.components.LkButtonSize.SM,
                                onClick = { kapakSec() }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(x = LkSpacing.Space5, y = 44.dp)
                    ) {
                        LkAvatar(
                            ad = currentUser.name,
                            avatarUrl = currentUser.avatarUrl,
                            boyut = 88.dp
                        )
                    }
                }

                Spacer(Modifier.height(48.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LkSpacing.Space4),
                    horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
                ) {
                    if (viewModel.avatarLoading) {
                        LkLoadingSpinner(size = 24.dp)
                    } else {
                        LkButton(
                            text = "Avatarı değiştir",
                            variant = LkButtonVariant.SECONDARY,
                            onClick = { avatarSec() }
                        )
                        if (!currentUser.avatarUrl.isNullOrBlank()) {
                            LkButton(
                                text = "Kaldır",
                                variant = LkButtonVariant.DANGER,
                                onClick = { viewModel.removeAvatar { _, u -> onNewSession("", u) } }
                            )
                        }
                    }
                }

                Text(
                    "Fotoğraf PNG veya JPEG, en fazla 5 MB olabilir.",
                    style = LkTypography.getMicro(),
                    color = LkTextSecondary,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(horizontal = LkSpacing.Space4)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LkSpacing.Space4),
                    verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
                ) {
                    LkTextField(
                        value = viewModel.editName,
                        onValueChange = { viewModel.onEditNameChange(it) },
                        label = "Görünen ad"
                    )
                    LkTextField(
                        value = viewModel.editBio,
                        onValueChange = { viewModel.onEditBioChange(it) },
                        label = "Hakkında",
                        placeholder = "İşletmeni bir iki cümleyle anlat",
                        singleLine = false
                    )
                    Text(
                        text = "${viewModel.editBio.length}/280",
                        style = LkTypography.getMicro(),
                        color = if (viewModel.editBio.length > 280) LkDanger else LkTextMuted
                    )
                    LkTextField(
                        value = viewModel.editLocation,
                        onValueChange = { viewModel.onEditLocationChange(it) },
                        label = "Konum",
                        placeholder = "Bursa"
                    )
                    LkTextField(
                        value = viewModel.editWebsite,
                        onValueChange = { viewModel.onEditWebsiteChange(it) },
                        label = "Site",
                        placeholder = "https://ornek.com"
                    )

                    /* Salt okunur: e-posta ve rol bu ekrandan degismiyor. */
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("E-POSTA ADRESİ", style = LkTypography.getMicro(), color = LkTextSecondary)
                        Text(currentUser.email, style = LkTypography.getBodyStrong(), color = LkTextPrimary)
                        Text(
                            "E-posta \"E-posta değiştir\" ekranından, doğrulamayla değişir.",
                            style = LkTypography.getMicro(),
                            color = LkTextMuted
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space1)) {
                        Text("HESAP ROLÜ", style = LkTypography.getMicro(), color = LkTextSecondary)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(LkPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = LkSpacing.Space2, vertical = LkSpacing.Space1)
                        ) {
                            Text(
                                text = roleLabel(currentUser.role),
                                style = LkTypography.getMicro(),
                                color = LkPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    viewModel.notice?.let {
                        LkNotice(
                            metin = it,
                            hataMi = viewModel.noticeIsError,
                            onKapat = { viewModel.clearNotice() }
                        )
                    }

                    LkButton(
                        text = if (viewModel.nameLoading) "Kaydediliyor..." else "Kaydet",
                        onClick = { viewModel.saveProfile { updated -> onNewSession("", updated) } },
                        enabled = !viewModel.nameLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
