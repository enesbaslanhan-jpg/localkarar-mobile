package com.localkarar.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.localkarar.app.auth.UserDto
import com.localkarar.app.core.SecureScreen
import com.localkarar.app.settings.SettingsViewModel
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkHeroPage
import com.localkarar.app.ui.components.LkNotice
import com.localkarar.app.ui.components.LkPasswordStrength
import com.localkarar.app.ui.components.LkPasswordTextField
import com.localkarar.app.ui.theme.*

/**
 * Mockup "Ayar 4 — Parola değiştir".
 *
 * 🔴 IKI SAPMA: ham `OutlinedTextField` (sistem alani yerine) ve GUC
 * GOSTERGESININ HIC OLMAMASI. Mockup'in kurali net: gosterge "zayıf"
 * demez, NE EKSIK oldugunu soyler.
 *
 * Tekrar alani eslesmiyorsa dugmeye basmadan once soyleniyor; hatayi
 * sunucudan ogrenmek gereksiz bir tur.
 */
@Composable
fun PasswordChangeScreen(
    viewModel: SettingsViewModel,
    onNewSession: (String, UserDto) -> Unit,
    onBack: () -> Unit
) {
    // Kimlik bilgisi girilen ekran: ekran goruntusu ve son-uygulamalar
    // kucuk resmi engelleniyor. Gerekce SecureScreen belgesinde.
    SecureScreen()

    val tekrarUyusmuyor = viewModel.passwordConfirm.isNotEmpty() &&
        viewModel.passwordConfirm != viewModel.passwordNew

    LkHeroPage(title = "Parola değiştir", onBack = onBack) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(LkSpacing.Space4),
            verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
        ) {
            Text(
                "Hesap güvenliğiniz için yeni parolanız en az 10 karakter olmalıdır.",
                style = LkTypography.getBodySmall(),
                color = LkTextSecondary
            )

            LkPasswordTextField(
                value = viewModel.passwordCurrent,
                onValueChange = { viewModel.onPasswordCurrentChange(it) },
                label = "Mevcut parola"
            )

            Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                LkPasswordTextField(
                    value = viewModel.passwordNew,
                    onValueChange = { viewModel.onPasswordNewChange(it) },
                    label = "Yeni parola"
                )
                LkPasswordStrength(viewModel.passwordNew)
            }

            Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                LkPasswordTextField(
                    value = viewModel.passwordConfirm,
                    onValueChange = { viewModel.onPasswordConfirmChange(it) },
                    label = "Yeni parola tekrar"
                )
                if (tekrarUyusmuyor) {
                    Text(
                        text = "İki parola aynı değil.",
                        style = LkTypography.getMetadata(),
                        color = LkDanger
                    )
                }
            }

            Spacer(Modifier.height(LkSpacing.Space2))

            LkButton(
                text = if (viewModel.passwordLoading) "Güncelleniyor..." else "Parolayı güncelle",
                onClick = { viewModel.changePassword { t, u -> onNewSession(t, u) } },
                enabled = !viewModel.passwordLoading && !tekrarUyusmuyor,
                modifier = Modifier.fillMaxWidth()
            )

            viewModel.notice?.let {
                LkNotice(
                    metin = it,
                    hataMi = viewModel.noticeIsError,
                    onKapat = { viewModel.clearNotice() }
                )
            }
        }
    }
}
