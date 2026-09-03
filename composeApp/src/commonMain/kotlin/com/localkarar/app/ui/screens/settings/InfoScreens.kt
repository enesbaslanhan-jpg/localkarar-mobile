package com.localkarar.app.ui.screens.settings

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
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.theme.*

@Composable
fun AboutScreen(onNavigateBack: () -> Unit) = InfoScaffold("LOCALKARAR", "Hakkında", onNavigateBack) {
    InfoBlock("İşletmen için doğru kararlar", "LocalKarar, küçük ve orta ölçekli işletmeler için bir karar destek uygulaması. Tahmine değil, kendi rakamlarına dayanan kararlar vermene yardım eder.")
    InfoBlock("Kime göre?", "Mağazası, atölyesi, e-ticaret sitesi ya da hizmet işletmesi olan; rakamlarını takip etmek isteyen işletme sahipleri için tasarlandı.")
    InfoBlock("Neler var?", "Karar Araçları · İşletme Takibi · AI Mentor · Hesaplamalar · Kurslar · Topluluk")
    InfoBlock("Neyi yapmaz?", "LocalKarar bir muhasebe programı değildir; hukuk, vergi, muhasebe veya yatırım danışmanlığının yerine geçmez. Karar her zaman kullanıcıya aittir.")
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
    Scaffold(backgroundColor = LkSurfaceCanvas, topBar = {
        TopAppBar(backgroundColor = LkSurfaceCanvas, contentColor = LkTextPrimary, elevation = 0.dp,
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri") } },
            title = { Column { Text(eyebrow, style = LkTypography.getMicro(), color = LkTextMuted); Text(title, style = LkTypography.getPageTitle()) } })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(22.dp), content = content)
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
