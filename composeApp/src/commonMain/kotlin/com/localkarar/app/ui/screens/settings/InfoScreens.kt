package com.localkarar.app.ui.screens.settings

import com.localkarar.app.ui.components.LkHeroPage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.ui.text.style.TextAlign
import com.localkarar.app.core.openExternalUrl
import com.localkarar.app.network.ApiConfig
import com.localkarar.app.network.AppEnvironmentProvider
import com.localkarar.app.ui.components.LkBrandMark
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkHairline
import com.localkarar.app.ui.components.LkListRow
import com.localkarar.app.ui.components.LkRowGroup
import com.localkarar.app.ui.theme.*
/**
 * Mockup "Ayar 8 — Hakkında".
 *
 * 🔴 EKRAN MOCKUP'A HIC BENZEMIYORDU: dort paragraflik bir metin sayfasiydi.
 * Mockup'ta marka isareti, SURUM ve uc baglanti satiri var (acik kaynak
 * lisanslari, kullanim kosullari, gizlilik politikasi) — surum hicbir
 * yerde yazmiyordu, oysa destek talebinde ilk sorulan sey odur.
 *
 * ⚠️ Reader-app kurali: fiyat ya da yukseltme baglantisi YOK.
 */
@Composable
fun AboutScreen(onNavigateBack: () -> Unit) {
    LkHeroPage(title = "Hakkında", onBack = onNavigateBack) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(LkSpacing.Space4),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
        ) {
            Spacer(Modifier.height(LkSpacing.Space4))

            LkBrandMark(size = 64.dp)

            Text(
                text = "LocalKarar",
                style = LkTypography.getTitleL(),
                color = LkTextPrimary
            )
            Text(
                text = "Sürüm ${AppEnvironmentProvider.versionLabel}",
                style = LkTypography.getMetadata(),
                color = LkTextSecondary
            )
            Text(
                text = "İşletmenin kararlarını tahmine değil kendi verisine dayandırman için yapıldı.",
                style = LkTypography.getBody(),
                color = LkTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = LkSpacing.Space4)
            )

            Spacer(Modifier.height(LkSpacing.Space2))

            LkRowGroup {
                BelgeSatiri("Açık kaynak lisansları", "licenses")
                LkHairline()
                BelgeSatiri("Kullanım koşulları", "terms")
                LkHairline()
                BelgeSatiri("Gizlilik politikası", "privacy")
            }

            Spacer(Modifier.height(LkSpacing.Space4))

            /*
             * ⚠️ "Neyi yapmaz" metni KALDIRILMADI, kucultuldu: hukuki
             * sorumluluk reddi urun icin gerekli, ama ekranin tamamini
             * kaplamasi gerekmiyordu.
             */
            Text(
                text = "LocalKarar bir muhasebe programı değildir; hukuk, vergi, muhasebe " +
                    "veya yatırım danışmanlığının yerine geçmez. Karar her zaman kullanıcıya aittir.",
                style = LkTypography.getMicro(),
                color = LkTextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = LkSpacing.Space4)
            )
        }
    }
}

/** Sunucudaki gercek sayfaya giden satir; metin kopyalanmiyor. */
@Composable
private fun BelgeSatiri(baslik: String, yol: String) {
    LkListRow(
        baslik = baslik,
        onClick = { openExternalUrl(ApiConfig.baseUrl + "/" + yol) },
        sag = {
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = LkTextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    )
}

@Composable
fun GuideScreen(onNavigateBack: () -> Unit, onOpenSupport: () -> Unit) = InfoScaffold("YARDIM", "Kullanım Kılavuzu", onNavigateBack) {
    Text("Uygulamanın ana akışı beş adımda. İşine yarayan bölümden başlayabilirsin.", style = LkTypography.getBody(), color = LkTextSecondary)
    GuideStep("1", "İşletmeni oluştur", "İşletme Takibi’ni aç; işletme adını, gerekirse sektör ve şehri gir.")
    GuideStep("2", "Kayıt ekle", "Ödeme, tahsilat, senet veya sevkiyat türünü seç; tutar ve tarihi girip kaydet.")
    GuideStep("3", "Belge yükle", "Dosya seç veya fotoğraf çek. Okunan bilgileri kontrol et; onaylamadan kayıt oluşmaz.")
    GuideStep("4", "Pazaryeri mağazanı bağla", "Ayarlar → Entegrasyonlar’dan sağlayıcıyı seç. Eşitlemeden sonra ürün ve siparişler ilgili ekranlara gelir.")
    GuideStep("5", "Mentora sor", "Sorunu kendi cümlelerinle yaz; rakam ve mevzuat içeren yanıtları resmî kaynağından doğrula.")
    Spacer(Modifier.height(8.dp))
    LkButton(text = "Yardım ve İletişim", onClick = onOpenSupport, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun InfoScaffold(eyebrow: String, title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    /*
     * §0: ham Scaffold + TopAppBar yerine hero kabugu.
     *
     * Ustteki kucuk etiket (eyebrow) baslik satirinda degil heroExtra'da:
     * ayni satira sikistirilinca cubugun yuksekligi etiketin uzunluguna
     * gore degisiyordu.
     */
    LkHeroPage(title = title, onBack = onBack, heroExtra = {
        Text(
            eyebrow,
            style = LkTypography.getMetadata(),
            color = LkHero.OnHeroSecondary,
            modifier = Modifier.padding(start = LkSpacing.Space5, top = LkSpacing.Space2)
        )
    }) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
            content = content
        )
    }
}

@Composable
private fun InfoBlock(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = LkTypography.getSectionTitle(), color = LkTextPrimary)
        Text(body, style = LkTypography.getBody(), color = LkTextSecondary)
        Divider(color = LkLineSoft, modifier = Modifier.padding(top = 10.dp))
    }
}

@Composable
private fun GuideStep(number: String, title: String, body: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(Modifier.size(28.dp).background(LkPrimary.copy(alpha = 0.12f), LkShapes.SM), contentAlignment = Alignment.Center) {
            Text(number, style = LkTypography.getBodyStrong(), color = LkPrimary)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = LkTypography.getCardTitle(), color = LkTextPrimary)
            Text(body, style = LkTypography.getBodySmall(), color = LkTextSecondary)
        }
    }
}
