package com.localkarar.app.ui

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
            .padding(LkSpacing.Space6),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "HOŞ GELDİN",
            style = LkTypography.getMicro().copy(fontWeight = FontWeight.Bold),
            color = LkPrimary
        )
        Spacer(Modifier.height(LkSpacing.Space2))

        Text(
            text = user?.name?.let { "Merhaba $it" } ?: "Hesabın hazır",
            style = LkTypography.getPageTitle(),
            color = LkTextPrimary
        )
        Spacer(Modifier.height(LkSpacing.Space3))

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
                    .background(LkSurfacePanel, LkShapes.SM)
                    .border(1.dp, LkLineSoft, LkShapes.SM)
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
            modifier = Modifier.fillMaxWidth()
        )
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
