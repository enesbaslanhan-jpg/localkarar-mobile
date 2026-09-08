package com.localkarar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.localkarar.app.auth.AuthViewModel
import com.localkarar.app.core.SecureScreen
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkHeroBlock
import com.localkarar.app.ui.components.LkHeroTone
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.theme.*
import kotlinx.coroutines.delay

/** Tekrar gonderme kilidi. Sunucu saatte 5 istekle sinirliyor. */
private const val TEKRAR_GONDER_SANIYE = 60

/**
 * Mockup "Giriş 5 — E-posta doğrulama".
 *
 * 🔴 BU EKRAN HIC YOKTU. `AuthenticatedVerificationScreen` adinda olu bir
 * dosya vardi (hicbir yerden cagrilmiyordu) ve icinde avatar + "Cikis yap"
 * duruyordu; dogrulama ile ilgisi yoktu. Uclar (`/auth/email/verify-request`,
 * `/auth/email/verify-confirm`) aylardir yaziliydi ve mobilde kullanilmiyordu.
 *
 * ⚠️ MOCKUP'TAN SAPMA — BILEREK: mockup "bağlantıya dokununca hesabın
 * açılır" diyor, sunucu ise 6 HANELI KOD gonderiyor ve bunu bilerek
 * yapiyor ("Bağlantı yerine kod: mobil istemcide derin bağlantı kurmaya
 * gerek kalmıyor", `auth.ts`). Yerlesim mockup'tan, akis sunucudan.
 *
 * Bekleme ekrani BOS DEGIL: hangi adrese gonderildigi yaziyor, sayac
 * doniyor, kod alani hazir.
 */
@Composable
fun EmailVerificationScreen(
    email: String,
    viewModel: AuthViewModel,
    onVerified: () -> Unit,
    onSkip: () -> Unit,
    onUseAnotherAddress: () -> Unit
) {
    SecureScreen()

    var kod by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()
    val hata by viewModel.verifyError.collectAsState()
    val gonderildi by viewModel.verifySent.collectAsState()
    val dogrulandi by viewModel.verifyDone.collectAsState()

    /*
     * Ilk kod ekran acilirken isteniyor; kullanicinin "gonder" demesi
     * gerekmiyor — kayittan gelen biri zaten kodu bekliyor.
     */
    LaunchedEffect(Unit) { viewModel.sendEmailVerification() }

    LaunchedEffect(dogrulandi) { if (dogrulandi) onVerified() }

    /*
     * Geri sayim: her yeniden gonderimde sifirlaniyor. `gonderildi`
     * anahtar degil — o bir kere true olup kaliyor; sayaci gonderim
     * SAYISI tetiklemeli, bu yuzden yerel bir sayac tutuluyor.
     */
    var gonderimNo by remember { mutableStateOf(0) }
    var kalan by remember { mutableStateOf(TEKRAR_GONDER_SANIYE) }
    LaunchedEffect(gonderimNo) {
        kalan = TEKRAR_GONDER_SANIYE
        while (kalan > 0) {
            delay(1000)
            kalan -= 1
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LkHeroBlock(tone = LkHeroTone.Auth) {
            Box(Modifier.fillMaxWidth().height(LkSpacing.Space12))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .offset(y = (-22).dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(LkSurfaceCanvas)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = LkSpacing.Space6, vertical = LkSpacing.Space8)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(LkSurfaceSignature, shape = LkShapes.MD)
                        .border(1.dp, LkLineSoft, LkShapes.MD),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MarkEmailUnread,
                        contentDescription = null,
                        tint = LkPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(Modifier.height(LkSpacing.Space4))

                Text(
                    text = "Gelen kutunu kontrol et",
                    style = LkTypography.getSectionTitle(),
                    color = LkTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(LkSpacing.Space2))

                Text(
                    text = "Doğrulama kodunu $email adresine gönderdik. " +
                        "Altı haneli kodu aşağıya yazınca hesabın doğrulanır.",
                    style = LkTypography.getBodySmall(),
                    color = LkTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(LkSpacing.Space6))

                if (hata != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LkDanger.copy(alpha = 0.12f), LkShapes.SM)
                            .border(1.dp, LkDanger.copy(alpha = 0.3f), LkShapes.SM)
                            .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space3)
                    ) {
                        Text(
                            text = hata!!,
                            color = LkDanger,
                            style = LkTypography.getBodySmall(),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.height(LkSpacing.Space4))
                }

                LkTextField(
                    value = kod,
                    onValueChange = { yeni -> kod = yeni.filter { it.isDigit() }.take(6) },
                    label = "Doğrulama kodu",
                    placeholder = "6 haneli kod"
                )

                Spacer(Modifier.height(LkSpacing.Space5))

                LkButton(
                    text = if (isLoading) "Doğrulanıyor..." else "Doğrula",
                    onClick = { viewModel.confirmEmailVerification(kod) },
                    enabled = kod.length == 6 && !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(LkSpacing.Space4))

                /*
                 * Sayac dolmadan dugme SOLUK degil, DEVRE DISI ve ne zaman
                 * acilacagini yaziyor. "Neden calismiyor" sorusunu ekranin
                 * kendisi yanitliyor.
                 */
                LkButton(
                    text = if (kalan > 0) "Tekrar gönder · $kalan saniye sonra" else "Tekrar gönder",
                    variant = LkButtonVariant.SECONDARY,
                    enabled = kalan == 0 && !isLoading,
                    onClick = {
                        viewModel.sendEmailVerification()
                        gonderimNo += 1
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (gonderildi && kalan > 0) {
                    Spacer(Modifier.height(LkSpacing.Space2))
                    Text(
                        text = "Kod gönderildi.",
                        style = LkTypography.getMetadata(),
                        color = LkTextMuted
                    )
                }

                Spacer(Modifier.height(LkSpacing.Space6))

                Text(
                    text = "Farklı bir adres kullan",
                    style = LkTypography.getBodySmall(),
                    color = LkPrimary,
                    modifier = Modifier
                        .clickable(onClick = onUseAnotherAddress)
                        .padding(vertical = LkSpacing.Space2)
                )

                /*
                 * ⚠️ MOCKUP'TA OLMAYAN TEK OGE. Sunucu dogrulanmamis
                 * hesabi ENGELLEMIYOR — web de engellemiyor. Bu ekrani
                 * cikissiz yapsaydik, mobilde var olmayan bir kapiyi
                 * kendimiz kurmus olurduk. Ikincil, sessiz bir baglanti.
                 */
                Text(
                    text = "Sonra doğrula",
                    style = LkTypography.getBodySmall(),
                    color = LkTextSecondary,
                    modifier = Modifier
                        .clickable(onClick = onSkip)
                        .padding(vertical = LkSpacing.Space2)
                )
            }
        }
    }
}
