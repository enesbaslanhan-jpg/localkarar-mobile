package com.localkarar.app.ui

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import com.localkarar.app.ui.components.altBoslukla
import com.localkarar.app.ui.components.LocalAltBosluk
import com.localkarar.app.ui.components.lkBinenYuzey
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.localkarar.app.ui.components.LkBrandMark
import com.localkarar.app.ui.components.LkHeroBlock
import com.localkarar.app.ui.components.LkHeroTone
import com.localkarar.app.core.SecureScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.localkarar.app.auth.AuthViewModel
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonSize
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.theme.*

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onNavigateToResetPassword: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    // Kimlik bilgisi girilen ekran: ekran goruntusu ve son-uygulamalar
    // kucuk resmi engelleniyor. Gerekce SecureScreen belgesinde.
    SecureScreen()
    var email by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.resetError.collectAsState()
    val resetSuccess by viewModel.resetSuccess.collectAsState()

    /*
     * §24.6 — giris oncesi akis: webin --auth-gradient'inin birebir kendisi.
     * Panelin kenarligi kaldirildi; ayrim cizgiyle degil yuzeyle yapiliyor.
     */
    Column(modifier = Modifier.fillMaxSize()) {

        LkHeroBlock(tone = LkHeroTone.Auth) {
            /* Foy: hero ust bari — geri dugmesi + baslik, gradyan uzerinde (16.09.2026). */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = LkSpacing.Space3, end = LkSpacing.Space3, top = LkSpacing.Space6, bottom = LkSpacing.Space10),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(LkShapes.FULL)
                        .background(LkHero.OnHero.copy(alpha = 0.14f))
                        .clickable(onClick = onNavigateToLogin),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Geri", tint = LkHero.OnHero, modifier = Modifier.size(22.dp))
                }
                Text(
                    text = if (resetSuccess) "Gelen kutunu kontrol et" else "Parolamı unuttum",
                    style = LkTypography.getSectionTitle(),
                    color = LkHero.OnHero,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.size(44.dp))
            }

        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .lkBinenYuzey(22.dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                /* Foy "Giris 2-5": panel acik (surface-1), alanlar beyaz ve 16dp koseli,
                   ana dugme hap (16.09.2026). Onceki hali canvas + sunken gri alanlardi. */
                .background(LkSurfacePanel)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                .padding(bottom = LocalAltBosluk.current.calculateBottomPadding())
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = LkSpacing.Space6, vertical = LkSpacing.Space8)
            ) {
                // Header Icon
                Text(
                    text = if (resetSuccess) {
                        "$email adresi sistemde kayıtlıysa parola sıfırlama bağlantısı gönderildi. Bağlantı 1 saat geçerlidir."
                    } else {
                        "E-posta adresini yaz; kayıtlı bir hesap varsa sıfırlama bağlantısı göndereceğiz."
                    },
                    style = LkTypography.getBodySmall(),
                    color = LkTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(LkSpacing.Space6))

                if (error != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LkDanger.copy(alpha = 0.12f), LkShapes.SM)
                            .border(1.dp, LkDanger.copy(alpha = 0.3f), LkShapes.SM)
                            .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space3)
                    ) {
                        Text(
                            text = error!!,
                            color = LkDanger,
                            style = LkTypography.getBodySmall(),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(LkSpacing.Space4))
                }

                if (!resetSuccess) {
                    LkTextField(
                        containerColor = LkSurfaceRaised,
                        shape = LkShapes.LG,
                        value = email,
                        onValueChange = { email = it },
                        label = "E-posta",
                        placeholder = "ornek@sirket.com"
                    )

                    Spacer(modifier = Modifier.height(LkSpacing.Space6))

                    LkButton(
                        text = if (isLoading) "Gönderiliyor…" else "Bağlantı gönder",
                        onClick = { viewModel.requestPasswordReset(email) },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        size = LkButtonSize.LG,
                        shape = LkShapes.FULL
                    )

                    Spacer(modifier = Modifier.height(LkSpacing.Space4))

                    Text(
                        text = "Sıfırlama kodum var",
                        style = LkTypography.getBodySmall(),
                        color = LkPrimary,
                        modifier = Modifier
                            .clickable(onClick = onNavigateToResetPassword)
                            .padding(vertical = LkSpacing.Space2)
                    )
                } else {
                    LkButton(
                        text = "Sıfırlama Kodunu Gir",
                        onClick = onNavigateToResetPassword,
                        modifier = Modifier.fillMaxWidth(),
                        size = LkButtonSize.LG,
                        shape = LkShapes.FULL
                    )

                    Spacer(modifier = Modifier.height(LkSpacing.Space4))

                    LkButton(
                        text = "Girişe dön",
                        variant = LkButtonVariant.SECONDARY,
                        onClick = onNavigateToLogin,
                        modifier = Modifier.fillMaxWidth(),
                        size = LkButtonSize.LG,
                        shape = LkShapes.FULL
                    )
                }

                Spacer(modifier = Modifier.height(LkSpacing.Space4))

                if (!resetSuccess) {
                    Text(
                        text = "Giriş ekranına dön",
                        style = LkTypography.getBodySmall(),
                        color = LkTextSecondary,
                        modifier = Modifier
                            .clickable(onClick = onNavigateToLogin)
                            .padding(vertical = LkSpacing.Space2)
                    )
                }
            }
        }
    }
}
