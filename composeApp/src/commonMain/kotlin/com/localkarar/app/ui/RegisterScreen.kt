package com.localkarar.app.ui

import androidx.compose.foundation.layout.offset
import com.localkarar.app.ui.components.lkBinenYuzey
import com.localkarar.app.ui.theme.LkTextMuted
import com.localkarar.app.ui.components.LkYasalOnayPenceresi
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.Icon
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
import com.localkarar.app.auth.SosyalGirisSonucu
import com.localkarar.app.auth.rememberSosyalGiris
import kotlinx.coroutines.launch
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonSize
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
    /*
     * YASAL ONAY (urun sahibi, 18.09.2026): kutucuk KALIR; kutucuga dokununca
     * metinler pencerede acilir, onay orada verilir ve kutu isaretlenir.
     * Kutu isaretli degilken "Hesabi olustur" ya da Google/Apple dokunusu da
     * ayni pencereyi acar; onaydan sonra bekleyen eylem kosar.
     */
    var legalAccepted by remember { mutableStateOf(false) }
    var onayPenceresiAcik by remember { mutableStateOf(false) }
    var bekleyenOnayEylemi by remember { mutableStateOf<(() -> Unit)?>(null) }
    val sosyal = rememberSosyalGiris()
    val kapsam = rememberCoroutineScope()
    fun onaylaVeyaSor(eylem: () -> Unit) {
        if (legalAccepted) eylem() else { bekleyenOnayEylemi = eylem; onayPenceresiAcik = true }
    }
    fun sosyalBaslat(saglayici: String) {
        onaylaVeyaSor {
            kapsam.launch {
                val sonuc = if (saglayici == "google") sosyal.google() else sosyal.apple()
                when (sonuc) {
                    is SosyalGirisSonucu.Basarili -> viewModel.sosyalGiris(sonuc.kimlik, acceptedLegal = true)
                    is SosyalGirisSonucu.Hata -> viewModel.registerHata(sonuc.mesaj)
                    SosyalGirisSonucu.Iptal -> {}
                }
            }
        }
    }

    if (onayPenceresiAcik) {
        LkYasalOnayPenceresi(
            baslik = "Hesap açmadan önce",
            onaylaMetni = "Okudum, onaylıyorum",
            onOnayla = {
                legalAccepted = true
                onayPenceresiAcik = false
                val eylem = bekleyenOnayEylemi; bekleyenOnayEylemi = null
                eylem?.invoke()
            },
            onVazgec = { onayPenceresiAcik = false; bekleyenOnayEylemi = null }
        )
    }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.registerError.collectAsState()

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
                    text = "Hesap oluştur",
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
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = LkSpacing.Space6, vertical = LkSpacing.Space8)
            ) {
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
                    containerColor = LkSurfaceRaised,
                    shape = LkShapes.LG,
                    value = name,
                    onValueChange = { name = it },
                    label = "Ad Soyad",
                    placeholder = "Adınız ve Soyadınız"
                )

                Spacer(modifier = Modifier.height(LkSpacing.Space4))

                LkTextField(
                    containerColor = LkSurfaceRaised,
                    shape = LkShapes.LG,
                    value = email,
                    onValueChange = { email = it },
                    label = "E-posta",
                    placeholder = "ornek@sirket.com"
                )

                Spacer(modifier = Modifier.height(LkSpacing.Space4))

                LkPasswordTextField(
                    containerColor = LkSurfaceRaised,
                    shape = LkShapes.LG,
                    value = password,
                    onValueChange = { password = it },
                    label = "Parola (En az 10 karakter)",
                    placeholder = "••••••••"
                )

                Spacer(modifier = Modifier.height(LkSpacing.Space4))

                // Yasal onay kutusu: dokununca metinler acilir (isaretliyse kaldirir).
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { if (legalAccepted) legalAccepted = false else onayPenceresiAcik = true }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = legalAccepted,
                        onCheckedChange = { if (it) onayPenceresiAcik = true else legalAccepted = false },
                        colors = CheckboxDefaults.colors(
                            checkedColor = LkPrimary,
                            uncheckedColor = LkTextSecondary,
                            checkmarkColor = LkOnPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Kullanım Koşulları ve Aydınlatma Metni'ni okudum, onaylıyorum.",
                        style = LkTypography.getMicro(),
                        color = LkTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(LkSpacing.Space6))

                LkButton(
                    text = if (isLoading) "Hesap oluşturuluyor…" else "Hesabı oluştur",
                    onClick = { onaylaVeyaSor { viewModel.register(name, email, password, true, onRegistered) } },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    size = LkButtonSize.LG,
                    shape = LkShapes.FULL
                )

                /*
                 * GOOGLE / APPLE (16.09.2026) — foy "Giris 2" bu konumda gosteriyor.
                 * Kayit ekraninda yasal onay kutusu zaten var: isaretliyse belirtec
                 * acceptedLegal=true ile gider ve hesap tek adimda acilir; degilse
                 * once kutuyu isaretlemesi istenir (sunucuya bosuna gidilmez).
                 */
                if (sosyal.googleVar || sosyal.appleVar) {
                    Spacer(modifier = Modifier.height(LkSpacing.Space5))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f).height(1.dp).background(LkLineStrong))
                        Text("veya", style = LkTypography.getMicro(), color = LkTextMuted, modifier = Modifier.padding(horizontal = LkSpacing.Space3))
                        Box(Modifier.weight(1f).height(1.dp).background(LkLineStrong))
                    }
                    Spacer(modifier = Modifier.height(LkSpacing.Space4))
                    Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3), modifier = Modifier.fillMaxWidth()) {
                        if (sosyal.googleVar) {
                            LkButton(
                                text = "Google",
                                onClick = { sosyalBaslat("google") },
                                variant = LkButtonVariant.SECONDARY,
                                size = LkButtonSize.LG,
                                shape = LkShapes.FULL,
                                enabled = !isLoading,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (sosyal.appleVar) {
                            LkButton(
                                text = "Apple",
                                onClick = { sosyalBaslat("apple") },
                                variant = LkButtonVariant.SECONDARY,
                                size = LkButtonSize.LG,
                                shape = LkShapes.FULL,
                                enabled = !isLoading,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

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
