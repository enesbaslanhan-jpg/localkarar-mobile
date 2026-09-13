package com.localkarar.app.ui

import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import com.localkarar.app.core.openExternalUrl
import com.localkarar.app.network.ApiConfig
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.localkarar.app.auth.AuthViewModel
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkPasswordTextField
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.theme.*

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegistered: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    // Kimlik bilgisi girilen ekran: ekran goruntusu ve son-uygulamalar
    // kucuk resmi engelleniyor. Gerekce SecureScreen belgesinde.
    SecureScreen()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var legalAccepted by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.registerError.collectAsState()

    /*
     * §24.6 — giris oncesi akis: webin --auth-gradient'inin birebir kendisi.
     * Panelin kenarligi kaldirildi; ayrim cizgiyle degil yuzeyle yapiliyor.
     */
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
                // Brand Header
                LkBrandMark(size = 56.dp, hareketli = true)

                Spacer(modifier = Modifier.height(LkSpacing.Space4))

                Text(
                    /* Metinler foy "Giris 2" ile birebir (13.09.2026): kisa baslik, pazarlama
                       cumlesi yok, "Kurumsal" sifati yok -- esnafin sirket e-postasi olmayabilir. */
                    text = "Hesap oluştur",
                    style = LkTypography.getSectionTitle(),
                    color = LkTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(LkSpacing.Space2))

                Text(
                    text = "Kendi verinden çıkan sayılarla karar ver.",
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

                LkTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Ad Soyad",
                    placeholder = "Adınız ve Soyadınız"
                )

                Spacer(modifier = Modifier.height(LkSpacing.Space4))

                LkTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "E-posta",
                    placeholder = "ornek@sirket.com"
                )

                Spacer(modifier = Modifier.height(LkSpacing.Space4))

                LkPasswordTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Parola (En az 10 karakter)",
                    placeholder = "••••••••"
                )

                Spacer(modifier = Modifier.height(LkSpacing.Space4))

                // Legal Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { legalAccepted = !legalAccepted }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = legalAccepted,
                        onCheckedChange = { legalAccepted = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = LkPrimary,
                            uncheckedColor = LkTextSecondary,
                            checkmarkColor = LkOnPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    /*
                     * 🔴 BELGE ADLARI TIKLANABILIR. Onceden duz metindi; kullanici
                     * "okudum" kutusunu isaretliyordu ama okuyacak bir yol yoktu
                     * (urun sahibi, 13.09.2026). Onay, okunabilen bir metne
                     * verilir; foy "Giris 2" notu da bunu soyluyor: "yasal onay
                     * kayit aninda ve okunabilir".
                     *
                     * Webdeki ayni belgeler aciliyor (/terms, /privacy); mobil
                     * Hakkinda ekrani da oraya bagli. Tek kaynak, iki kopya yok.
                     */
                    val onayMetni = buildAnnotatedString {
                        withLink(
                            LinkAnnotation.Url(
                                url = ApiConfig.baseUrl + "/terms",
                                styles = TextLinkStyles(SpanStyle(color = LkPrimary, textDecoration = TextDecoration.Underline)),
                                linkInteractionListener = { openExternalUrl(ApiConfig.baseUrl + "/terms") }
                            )
                        ) { append("Kullanım Koşulları") }
                        append(" ve ")
                        withLink(
                            LinkAnnotation.Url(
                                url = ApiConfig.baseUrl + "/privacy",
                                styles = TextLinkStyles(SpanStyle(color = LkPrimary, textDecoration = TextDecoration.Underline)),
                                linkInteractionListener = { openExternalUrl(ApiConfig.baseUrl + "/privacy") }
                            )
                        ) { append("Gizlilik Politikası") }
                        append("'nı okudum, onaylıyorum.")
                    }
                    Text(
                        text = onayMetni,
                        style = LkTypography.getMicro(),
                        color = LkTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(LkSpacing.Space6))

                LkButton(
                    text = if (isLoading) "Hesap oluşturuluyor…" else "Hesabı oluştur",
                    onClick = { viewModel.register(name, email, password, legalAccepted, onRegistered) },
                    enabled = name.isNotBlank() && email.isNotBlank() && password.length >= 10 && legalAccepted && !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                /*
                 * GOOGLE / APPLE DUGMELERI ILK MAGAZA SURUMUNDE YOK (14.09.2026).
                 *
                 * "YAKINDA" etiketli, tiklanmayan iki dugme duruyordu. App Store
                 * incelemesi islevsiz dugmeye takilabiliyor; calismayan bir sey
                 * gostermektense hic gostermemek secildi. Girisler Apple gelistirici
                 * hesabi ve OAuth kimlikleri gelince eklenecek; foy "Giris 2" onlari
                 * bu konumda gosteriyor, geri geldiklerinde yer belli.
                 */

                Spacer(modifier = Modifier.height(LkSpacing.Space6))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Zaten hesabınız var mı?",
                        style = LkTypography.getBodySmall(),
                        color = LkTextSecondary
                    )
                    Spacer(modifier = Modifier.width(LkSpacing.Space2))
                    Text(
                        text = "Giriş Yap",
                        style = LkTypography.getBodySmall(),
                        color = LkPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onNavigateToLogin)
                    )
                }
            }
        }
    }
}
