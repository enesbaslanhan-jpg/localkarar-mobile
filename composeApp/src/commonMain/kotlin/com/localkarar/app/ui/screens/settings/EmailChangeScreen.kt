package com.localkarar.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.localkarar.app.auth.UserDto
import com.localkarar.app.core.SecureScreen
import com.localkarar.app.settings.SettingsViewModel
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkHeroPage
import com.localkarar.app.ui.components.LkNotice
import com.localkarar.app.ui.components.LkPasswordTextField
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.theme.*

/**
 * Mockup "Ayar 3 — E-posta değiştir".
 *
 * 🔴 HAM MATERIAL ALANLAR KULLANIYORDU. `OutlinedTextField` kendi
 * yaricapini (4dp), kendi odak halkasini ve kendi etiket hareketini
 * getiriyordu; mockup'ta alanlar 16 yaricapli ve odak halkasi gorunur.
 * `LkTextField` bunlarin hepsini tasiyor ve zaten yaziliydi.
 *
 * Dogrulama gerektiren islem: ne olacagi ONCEDEN yaziyor, surpriz yok.
 */
@Composable
fun EmailChangeScreen(
    viewModel: SettingsViewModel,
    onNewSession: (String, UserDto) -> Unit,
    onBack: () -> Unit
) {
    // Kimlik bilgisi girilen ekran: ekran goruntusu ve son-uygulamalar
    // kucuk resmi engelleniyor. Gerekce SecureScreen belgesinde.
    SecureScreen()
    LkHeroPage(title = "E-posta değiştir", onBack = onBack) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(LkSpacing.Space4),
            verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
        ) {
            Text(
                "Yeni adrese bir doğrulama bağlantısı gönderilecek. Doğrulanana kadar eski adresinizle giriş yapmaya devam edersiniz.",
                style = LkTypography.getBodySmall(),
                color = LkTextSecondary
            )

            LkTextField(
                value = viewModel.emailNew,
                onValueChange = { viewModel.onEmailNewChange(it) },
                label = "Yeni e-posta adresi",
                placeholder = "adiniz@sirketiniz.com",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            LkPasswordTextField(
                value = viewModel.emailCurrentPassword,
                onValueChange = { viewModel.onEmailCurrentPasswordChange(it) },
                label = "Mevcut parola"
            )

            Spacer(Modifier.height(LkSpacing.Space2))

            LkButton(
                text = if (viewModel.emailLoading) "Gönderiliyor..." else "Doğrulama gönder",
                onClick = { viewModel.changeEmail(onNewSession) },
                enabled = !viewModel.emailLoading,
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
