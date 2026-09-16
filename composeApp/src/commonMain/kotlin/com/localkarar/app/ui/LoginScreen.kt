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
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.localkarar.app.auth.AuthViewModel
import com.localkarar.app.auth.SosyalGirisSonucu
import com.localkarar.app.auth.rememberSosyalGiris
import com.localkarar.app.core.openExternalUrl
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import kotlinx.coroutines.launch
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonSize
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkPasswordTextField
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.theme.*

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    // Kimlik bilgisi girilen ekran: ekran goruntusu ve son-uygulamalar
    // kucuk resmi engelleniyor. Gerekce SecureScreen belgesinde.
    SecureScreen()
    /*
     * 🔴 BU ALANLAR ONCEDEN DOLU GELIYORDU:
     *
     *     mutableStateOf("admin@localakademi.com")
     *     mutableStateOf("admin123")
     *
     * Gelistirme kolayligi icin konmus ve KALMIS. Sonuclari:
     *
     *  1. Yayimlanan uygulamayi acan HERKES giris ekraninda bir yonetici
     *     e-postasini ve parolasini goruyordu. O hesap uretimde varsa
     *     dogrudan ele gecirme; yoksa bile gecerli bir yonetici adresinin
     *     sizmasi ve parola kalibinin ipucu.
     *  2. Parola alani doluyken kullanici "giris yap"a basinca kendi hesabi
     *     yerine baskasininkini denemis oluyordu.
     *
     * DEVELOPMENT_STATUS.md "M2 - Demo Auth Removal: COMPLETE - hardcoded
     * tokens, demo student bypass and fake users removed" diyordu; bu iki
     * satir o temizlikten kacmisti. Belge dogru sanildigi icin kimse bakmadi.
     */
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    /* Sosyal giris (16.09.2026): platform koprusu + onay bekleyen kimlik. */
    val sosyal = rememberSosyalGiris()
    val kapsam = rememberCoroutineScope()
    val onayBekleyen by viewModel.onayBekleyenSosyal.collectAsState()
    var sosyalOnay by remember { mutableStateOf(false) }
    fun sosyalBaslat(saglayici: String) {
        kapsam.launch {
            val sonuc = if (saglayici == "google") sosyal.google() else sosyal.apple()
            when (sonuc) {
                is SosyalGirisSonucu.Basarili -> viewModel.sosyalGiris(sonuc.kimlik)
                is SosyalGirisSonucu.Hata -> viewModel.sosyalHata(sonuc.mesaj)
                SosyalGirisSonucu.Iptal -> {}
            }
        }
    }
    
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.loginError.collectAsState()

    /*
     * §24.6 — GIRIS AKISI: webin gradyaninin BIREBIR kendisi.
     *
     * `LkHeroTone.Auth` durak kumesi `--auth-gradient`'tan geliyor
     * (#060F14 → #0E2530 → #1B4356 → #275C72 → #2F6A82, 158deg). Calisma
     * ekranlarinin gradyani biraz acilmis bir turevi; giris oncesi akis
     * webdekiyle AYNI gorunmeli, kullanici ayni urunde oldugunu bilsin.
     *
     * Marka isareti artik "LK" yazan bir kutu degil, gercek isaret
     * (`LkBrandMark` -> local_karar_mark.png).
     */
    Column(modifier = Modifier.fillMaxSize()) {

        LkHeroBlock(tone = LkHeroTone.Auth) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = LkSpacing.Space6,
                        end = LkSpacing.Space6,
                        top = LkSpacing.Space10,
                        bottom = LkSpacing.Space10
                    )
            ) {
                LkBrandMark(size = 58.dp, hareketli = true)

                Spacer(modifier = Modifier.height(LkSpacing.Space4))

                Text(
                    text = "İşletmen için doğru kararlar",
                    style = LkTypography.getTitleL(),
                    color = LkHero.OnHero,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(LkSpacing.Space2))

                Text(
                    text = "Tahmine değil, kendi rakamlarına dayanan kararlar.",
                    style = LkTypography.getBody(),
                    // §24.6: %85 beyaz opakligin ALTINA inilmez.
                    color = LkHero.OnHeroSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .offset(y = (-22).dp)
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
                    .padding(horizontal = LkSpacing.Space6, vertical = LkSpacing.Space8)
            ) {

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
                    containerColor = LkSurfaceRaised,
                    shape = LkShapes.LG,
                    value = email,
                    onValueChange = { email = it },
                    label = "E-posta",
                    placeholder = "mail@ornek.com"
                )
                
                Spacer(modifier = Modifier.height(LkSpacing.Space4))
                
                LkPasswordTextField(
                    containerColor = LkSurfaceRaised,
                    shape = LkShapes.LG,
                    value = password,
                    onValueChange = { password = it },
                    label = "Parola",
                    placeholder = "••••••••"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Parolamı unuttum",
                        style = LkTypography.getMicro(),
                        color = LkPrimary,
                        modifier = Modifier
                            .clickable(onClick = onNavigateToForgotPassword)
                            .padding(vertical = LkSpacing.Space2)
                    )
                }
                
                Spacer(modifier = Modifier.height(LkSpacing.Space4))
                
                LkButton(
                    text = if (isLoading) "Giriş Yapılıyor..." else "Giriş Yap",
                    onClick = { viewModel.login(email, password) },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    size = LkButtonSize.LG,
                    shape = LkShapes.FULL
                )

                /*
                 * GOOGLE / APPLE (16.09.2026). Yalniz platformun destekledigi
                 * dugme cizilir: Android'de Google, iOS'ta Google + Apple. Islevsiz
                 * "yakinda" dugmesi YOK (App Store incelemesi buna takiliyor).
                 * Belirtec sunucuya gider; ilk giriste sunucu onay isterse asagidaki
                 * onay kutusu acilir.
                 */
                if (sosyal.googleVar || sosyal.appleVar) {
                    Spacer(modifier = Modifier.height(LkSpacing.Space5))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.weight(1f).height(1.dp).background(LkLineStrong))
                        Text(
                            "veya",
                            style = LkTypography.getMicro(),
                            color = LkTextMuted,
                            modifier = Modifier.padding(horizontal = LkSpacing.Space3)
                        )
                        Box(Modifier.weight(1f).height(1.dp).background(LkLineStrong))
                    }
                    Spacer(modifier = Modifier.height(LkSpacing.Space4))
                    if (onayBekleyen != null) {
                        /* Ilk sosyal giris: kayittaki yasal onayin aynisi. */
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(LkSurfacePanel, LkShapes.MD)
                                .padding(LkSpacing.Space4)
                        ) {
                            Text(
                                "Yeni hesap oluşturuluyor — devam etmek için onayla",
                                style = LkTypography.getBodyStrong(),
                                color = LkTextPrimary
                            )
                            Spacer(Modifier.height(LkSpacing.Space2))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = sosyalOnay,
                                    onCheckedChange = { sosyalOnay = it },
                                    colors = CheckboxDefaults.colors(checkedColor = LkPrimary)
                                )
                                Text(
                                    buildString {
                                        append("Kullanım Koşulları'nı ve Aydınlatma Metni'ni okudum, onaylıyorum.")
                                    },
                                    style = LkTypography.getBodySmall(),
                                    color = LkTextSecondary,
                                    modifier = Modifier.clickable { sosyalOnay = !sosyalOnay }
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                                Text(
                                    "Kullanım Koşulları",
                                    style = LkTypography.getMicro(), color = LkPrimary,
                                    modifier = Modifier.clickable { openExternalUrl("https://localkarar.com/terms") }
                                )
                                Text(
                                    "Aydınlatma Metni",
                                    style = LkTypography.getMicro(), color = LkPrimary,
                                    modifier = Modifier.clickable { openExternalUrl("https://localkarar.com/privacy") }
                                )
                            }
                            Spacer(Modifier.height(LkSpacing.Space3))
                            LkButton(
                                text = if (isLoading) "Hesap açılıyor..." else "Onayla ve devam et",
                                onClick = { viewModel.sosyalOnayla() },
                                enabled = sosyalOnay && !isLoading,
                                modifier = Modifier.fillMaxWidth(),
                                size = LkButtonSize.LG,
                                shape = LkShapes.FULL
                            )
                            TextButton(onClick = { viewModel.sosyalOnayiVazgec(); sosyalOnay = false }) {
                                Text("Vazgeç", color = LkTextSecondary)
                            }
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space3), modifier = Modifier.fillMaxWidth()) {
                            if (sosyal.googleVar) {
                                LkButton(
                                    text = "Google",
                                    onClick = { sosyalBaslat("google") },
                                    variant = LkButtonVariant.SECONDARY,
                                    enabled = !isLoading,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (sosyal.appleVar) {
                                LkButton(
                                    text = "Apple",
                                    onClick = { sosyalBaslat("apple") },
                                    variant = LkButtonVariant.SECONDARY,
                                    enabled = !isLoading,
                                    modifier = Modifier.weight(1f)
                                )
                            }
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
                        text = "Hesabınız yok mu?",
                        style = LkTypography.getBodySmall(),
                        color = LkTextSecondary
                    )
                    Spacer(modifier = Modifier.width(LkSpacing.Space2))
                    Text(
                        text = "Kayıt Ol",
                        style = LkTypography.getBodySmall(),
                        color = LkPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onNavigateToRegister)
                    )
                }
            }
        }
    }
}
