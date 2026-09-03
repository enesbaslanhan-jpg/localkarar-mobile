package com.localkarar.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.localkarar.app.auth.UserDto
import com.localkarar.app.settings.SupportViewModel
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.theme.*

/**
 * Destek / yardim formu.
 *
 * Webdeki `/yardim` sayfasinin mobil karsiligi. Mobilde HIC YOKTU.
 *
 * 🔴 Neden yalnizca bir eksik ekran degil:
 *
 * `POST /support/contact` uyelik kapisinin MUAF listesinde
 * (membership-guard.ts). Yani ucretsiz suresi dolmus, hesabi salt okunur moda
 * dusmus bir kullanicinin kalan TEK yazma yolu bu form. Mobilde bulunmamasi,
 * tam da en cok yardima ihtiyaci olan kullanicinin uygulamadan destek
 * isteyememesi demekti.
 */
@Composable
fun SupportScreen(
    user: UserDto?,
    viewModel: SupportViewModel,
    onNavigateBack: () -> Unit
) {
    // Giris yapmis kullanicinin adi ve e-postasi biliniyor; tekrar yazdirmak
    // gereksiz surtunme. Alanlar duzenlenebilir kaliyor.
    var ad by remember { mutableStateOf(user?.name ?: "") }
    var eposta by remember { mutableStateOf(user?.email ?: "") }
    var konu by remember { mutableStateOf("") }
    var mesaj by remember { mutableStateOf("") }

    val gonderiliyor by viewModel.gonderiliyor.collectAsState()
    val gonderildi by viewModel.gonderildi.collectAsState()
    val hata by viewModel.hata.collectAsState()

    Scaffold(
        backgroundColor = LkSurfaceCanvas,
        topBar = {
            TopAppBar(
                backgroundColor = LkSurfacePanel,
                contentColor = LkTextPrimary,
                elevation = 0.dp,
                title = {
                    Column {
                        Text(text = "Destek", style = LkTypography.getSectionTitle())
                        Text(
                            text = "Sorununuzu yazın, dönüş yapalım",
                            style = LkTypography.getMicro(),
                            color = LkTextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        if (gonderildi) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(LkSpacing.Space8),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = LkPrimary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(LkSpacing.Space4))
                Text(
                    text = "Talebiniz iletildi",
                    style = LkTypography.getSectionTitle(),
                    color = LkTextPrimary
                )
                Spacer(modifier = Modifier.height(LkSpacing.Space2))
                Text(
                    text = "En kısa sürede e-posta ile dönüş yapacağız.",
                    style = LkTypography.getBody(),
                    color = LkTextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(LkSpacing.Space8))
                LkButton(text = "Kapat", onClick = onNavigateBack)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(LkSpacing.Space4),
            verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
        ) {
            LkTextField(value = ad, onValueChange = { ad = it }, label = "Adınız")
            LkTextField(
                value = eposta,
                onValueChange = { eposta = it },
                label = "E-posta",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            LkTextField(value = konu, onValueChange = { konu = it }, label = "Konu")
            LkTextField(
                value = mesaj,
                onValueChange = { mesaj = it },
                label = "Mesajınız",
                placeholder = "En az 20 karakter",
                singleLine = false,
                modifier = Modifier.heightIn(min = 140.dp)
            )

            if (hata != null) {
                Text(text = hata!!, style = LkTypography.getBodySmall(), color = LkDanger)
            }

            LkButton(
                text = if (gonderiliyor) "Gönderiliyor..." else "Gönder",
                onClick = { viewModel.gonder(ad, eposta, konu, mesaj) },
                enabled = !gonderiliyor,
                variant = LkButtonVariant.PRIMARY
            )
        }
    }
}
