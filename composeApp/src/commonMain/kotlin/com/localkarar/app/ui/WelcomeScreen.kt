package com.localkarar.app.ui

import com.localkarar.app.ui.components.LkButtonSize
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.localkarar.app.auth.UserDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.theme.*

/**
 * KAYIT SONRASI KARSILAMA.
 *
 * Webdeki `/app/hosgeldin` sayfasinin karsiligi; mobilde HIC YOKTU ve
 * kayit biter bitmez kullanici dogrudan bos bir Kontrol Merkezi'ne
 * dusuyordu.
 *
 * 🔴 FIYAT TABLOSU BILEREK YOK.
 *
 * Webdeki sayfa kurucu uye fiyat zaman cizgisini gosteriyor (0 TL / 149 TL
 * / kurucu fiyati). Mobilde gosterilmiyor, iki sebeple:
 *
 *   1. Sunucu bu asamalari HICBIR UCTAN sunmuyor (`src/config/billing.ts`
 *      icinde duruyor). Mobile elle yazmak, fiyat degistiginde uygulamanin
 *      sessizce ESKI fiyati gostermesi demekti -- ve fiyat, sessizce
 *      yanlis olmasi en kotu alanlardan biri.
 *   2. Uygulama ici abonelik fiyati gostermek magaza faturalandirma
 *      kurallarinin tam ortasinda; urun sahibi bu arastirmayi surduruyor
 *      (03.09.2026). Karar cikmadan fiyat basmak, reddedilme riskini
 *      arastirmadan once almak olurdu.
 *
 * Bunun yerine ekran, sunucudan GELEN tek gercegi soyluyor:
 * `membership.state`. Ucretlendirme baslamadiysa bunu acikca yaziyor.
 *
 * 🔴 KART BILGISI ISTENMIYOR, ODEME PANELI ACILMIYOR -- webdeki kararla
 * ayni (WelcomePage.jsx, 28.08.2026).
 */
@Composable
fun WelcomeScreen(
    user: UserDto?,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LkSurfaceCanvas)
            .verticalScroll(rememberScrollState())
    ) {
        HosGeldinHero(user)

        Column(modifier = Modifier.padding(LkSpacing.Space6)) {
            Text(
                text = "LocalKarar, işletmenle ilgili kararları rakamlara dayandırman için kuruldu. " +
                    "Kurslar, karar araçları ve hesaplamalar burada bir arada.",
                style = LkTypography.getBody(),
                color = LkTextSecondary
            )

            Spacer(Modifier.height(LkSpacing.Space6))

            NeYapabilirsin("Kurslar", "Uygulamalı derslerle işletme finansını öğren.")
            NeYapabilirsin("Karar Araçları", "\"Bu indirimi yapabilir miyim?\" gibi soruları rakamla yanıtla.")
            NeYapabilirsin("Hesaplamalar", "Birim maliyet, kâr marjı, nakit akışı ve fazlası.")
            NeYapabilirsin("İşletme Takibi", "Kayıtlarını, belgelerini ve takvimini tek yerde tut.")

            /*
             * Ucretlendirme durumu SUNUCUDAN geliyor; burada varsayim
             * yapilmiyor. Alan yoksa (eski sunucu) hicbir sey yazilmiyor --
             * yanlis bir ucretlendirme vaadi, hic vaat etmemekten kotudur.
             */
            if (user?.membership?.state == "billing_not_started") {
                Spacer(Modifier.height(LkSpacing.Space5))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LkSurfaceRaised, LkShapes.SM)
                        .border(1.dp, LkLineStrong, LkShapes.SM)
                        .padding(LkSpacing.Space4)
                ) {
                    Text(
                        text = "Ücretlendirme henüz başlamadı; hiçbir tahsilat yapılmıyor ve kart bilgisi istenmiyor.",
                        style = LkTypography.getBodySmall(),
                        color = LkTextPrimary
                    )
                }
            }

            Spacer(Modifier.height(LkSpacing.Space6))

            /* Atlanabilir olmasi webdeki kararla ayni: karsilama bir engel degil. */
            LkButton(
                text = "LocalKarar'a başla",
                onClick = onStart,
                size = LkButtonSize.LG,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(LkSpacing.Space6))
        }
    }
}

/**
 * Karsilama hero paneli.
 *
 * 🔴 BU EKRAN DUZ BIR METIN LISTESIYDI: zemin rengi, uc paragraf, bir buton.
 * Uygulamanin ILK gordugu ekran ve hicbir agirligi yoktu.
 *
 * ⚠️ UYGULAMADA HIC GORSEL VARLIK YOK (yalniz `composeResources/font`).
 * Revolut gibi urunlerin karsilama ekranlarindaki 3B render'lar siparis
 * edilmis varliklar; burada uretilemez. Bunun yerine kompozisyon TAMAMEN
 * KODLA ciziliyor: es merkezli halkalar + radyal degrade. Varlik gerektirmez,
 * her ekran yogunlugunda keskin kalir, APK'ya bayt eklemez.
 *
 * Zemin `LkSurfaceSignature`: §3.2'nin koyu imza paneli istisnasi. Iki temada
 * da ayni koyu zemin -- karsilama ekrani markanin ilk izlenimi, temaya gore
 * degismemeli.
 */
@Composable
private fun HosGeldinHero(user: UserDto?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .background(LkSurfaceSignature)
    ) {
        // Es merkezli halkalar. Kalinlik ve saydamlik disa dogru azaliyor;
        // merkez sag ustte, boylece metnin okundugu sol alt bolge temiz kalir.
        Canvas(modifier = Modifier.fillMaxSize()) {
            val merkez = Offset(size.width * 0.82f, size.height * 0.24f)
            for (i in 1..6) {
                drawCircle(
                    color = LkOnSignature.copy(alpha = 0.16f / i),
                    radius = size.minDimension * (0.16f * i),
                    center = merkez,
                    style = Stroke(width = (7f - i).coerceAtLeast(1f))
                )
            }
            // Halkalarin merkezindeki yumusak isik.
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(LkOnSignature.copy(alpha = 0.10f), Color.Transparent),
                    center = merkez,
                    radius = size.minDimension * 0.42f
                ),
                radius = size.minDimension * 0.42f,
                center = merkez
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(LkSpacing.Space6)
        ) {
            Text(
                text = "HOŞ GELDİN",
                style = LkTypography.getMetadata(),
                color = LkOnSignatureDim,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(LkSpacing.Space2))
            Text(
                text = user?.name?.let { "Merhaba $it" } ?: "Hesabın hazır",
                // §4 `display` — uygulamanin tek hero basligi burasi.
                style = LkTypography.getDisplay(),
                color = LkOnSignature
            )
        }
    }
}

@Composable
private fun NeYapabilirsin(baslik: String, aciklama: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = LkSpacing.Space2),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(6.dp)
                .background(LkPrimary, CircleShape)
        )
        Spacer(Modifier.width(LkSpacing.Space3))
        Column(modifier = Modifier.weight(1f)) {
            Text(baslik, style = LkTypography.getBodyStrong(), color = LkTextPrimary)
            Text(aciklama, style = LkTypography.getBodySmall(), color = LkTextSecondary)
        }
    }
}
