package com.localkarar.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.components.LkBrandMark
import com.localkarar.app.ui.components.LkPusulaKadrani
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonSize
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkHeroBlock
import com.localkarar.app.ui.components.LkHeroTone
import com.localkarar.app.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Mockup "Giriş 1 — Karşılama".
 *
 * 🔴 GIRIS AKISININ ILK EKRANI HIC YOKTU: uygulama dogrudan giris
 * formuna aciliyordu. Mockup'ta once bir karsilama var — uygulamanin ne
 * yaptigini bir cumlede soyleyip iki yol veriyor: hesap olustur, giris yap.
 *
 * Hero kompozisyonu KODLA cizilmis: gorsel dosyasi yok, iki temada da
 * calisiyor ve boyutu ekrana gore uyum sagliyor (mockup'in kendi notu).
 * Ustune pusula yonu: −142°'den donup kuzeyi buluyor, TEK SEFERLIK.
 */
@Composable
fun AuthWelcomeScreen(
    onCreateAccount: () -> Unit,
    onLogin: () -> Unit
) {
    /*
     * FOY "GIRIS 1" DUZENI (13.09.2026).
     *
     * Onceki hal: hero 280dp sabit, baslik ve alt metin ASAGIDAKI
     * yuzeyde, dugmeler en altta -- arada bir ekran boyu bosluk
     * kaliyordu. Foyde baslik ve alt metin HERO ICINDE (gradyan ustunde,
     * beyaz), alt yuzey yalniz iki dugmeyi tasiyor ve kisa. Hero kalan
     * yuksekligi aliyor; bosluk kompozisyonun icinde eriyor.
     */
    Column(modifier = Modifier.fillMaxSize()) {

        LkHeroBlock(modifier = Modifier.weight(1f), tone = LkHeroTone.Auth) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = LkSpacing.Space6, vertical = LkSpacing.Space6),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    /* Kadran ve isaret acilis ekraniyla AYNI kompozisyon:
                       uygulama acilistan karsilamaya tek bir kareyle akiyor. */
                    LkPusulaKadrani(Modifier.size(260.dp))
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(androidx.compose.ui.graphics.Color(0x33061018))
                            .border(
                                1.dp,
                                androidx.compose.ui.graphics.Color(0x33FFFFFF),
                                RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        LkBrandMark(size = 68.dp, hareketli = true)
                    }
                }

                Spacer(Modifier.height(LkSpacing.Space6))

                Text(
                    text = "Kararlarını tahmine bırakma",
                    style = LkTypography.getTitleL(),
                    color = LkHero.OnHero,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(LkSpacing.Space3))

                Text(
                    text = "Kendi verinden çıkan sayılarla fiyat koy, tedarikçi seç, " +
                        "yatırım kararı ver.",
                    style = LkTypography.getBody(),
                    color = LkHero.OnHeroSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        /* Alt yuzey: yalniz dugmeler. Ust kose yuvarlagi hero'nun ustune
           biniyor (-22dp), oteki ekranlarla ayni kural. */
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-22).dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(LkSurfaceCanvas)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = LkSpacing.Space6, vertical = LkSpacing.Space6),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LkButton(
                text = "Hesap oluştur",
                onClick = onCreateAccount,
                modifier = Modifier.fillMaxWidth(),
                /* Giris akisi foyu: hap dugme, giris ekranlariyla ayni (16.09.2026). */
                size = LkButtonSize.LG,
                shape = LkShapes.FULL
            )

            Spacer(Modifier.height(LkSpacing.Space3))

            LkButton(
                text = "Giriş yap",
                variant = LkButtonVariant.SECONDARY,
                onClick = onLogin,
                modifier = Modifier.fillMaxWidth(),
                /* Giris akisi foyu: hap dugme, giris ekranlariyla ayni (16.09.2026). */
                size = LkButtonSize.LG,
                shape = LkShapes.FULL
            )

            Spacer(Modifier.height(LkSpacing.Space2))
        }
    }
}
