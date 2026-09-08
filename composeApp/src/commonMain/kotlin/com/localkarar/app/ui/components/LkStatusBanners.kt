package com.localkarar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.localkarar.app.auth.MembershipDto
import com.localkarar.app.ui.theme.*

/**
 * Cevrimdisi bandi — uygulama kabugunda, tum ekranlarin ustunde.
 *
 * Tek bir yerde durmasinin sebebi: cevrimdisilik ekrana ozgu bir durum degil.
 * Onceki halinde her ekran bunu kendi hata ekraninda, digerlerinden ayirt
 * edilemeyecek sekilde gosteriyordu.
 */
@Composable
fun LkOfflineBanner(gorunur: Boolean, modifier: Modifier = Modifier) {
    if (!gorunur) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(LkSurfaceRaised)
            .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.CloudOff,
            contentDescription = null,
            tint = LkTextSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(LkSpacing.Space2))
        Text(
            text = "İnternet bağlantısı yok. Değişiklikleriniz kaydedilemeyebilir.",
            style = LkTypography.getMetadata(),
            color = LkTextSecondary
        )
    }
}

/**
 * Uyelik durumu seridi.
 *
 * DENEME SURESI bitmek uzereyken kapatilabilir, SURE DOLDUGUNDA kapatilamaz.
 * Ikincisi bilincli: hesap salt okunur moda gectiginde kullanicinin yazma
 * denemesi sunucudan 403 ile donuyor ve serit kapatilabilir olsaydi kullanici
 * neden yazamadigini anlamadan uygulamayi kullanmaya calisirdi.
 *
 * 🔴 SATIN ALMAYA YONLENDIRME YOK -- NE UYGULAMA ICINDE NE DE DISARI.
 *
 * Onceki hali "Uyeligi baslat" dugmesi tasiyordu ve HARICI TARAYICIDA
 * uyelik sayfasini aciyordu. Uygulama ici satin alma olmadigi icin bunun
 * guvenli oldugu varsayilmisti; DEGILDI.
 *
 * Urun sahibinin magaza kurallari arastirmasi (03.09.2026) secilen modeli
 * netlestirdi: "yalnizca giris yap" (reader app). Kullanici webde
 * localkarar.com uzerinden odemesini yapip hesabini olusturuyor, mobil
 * uygulamaya yalnizca giris yapiyor. Apple ve Google bu modele izin
 * veriyor ve TEK SARTI, uygulamanin icinde satin almaya goturen bir
 * dugme ya da baglanti BULUNMAMASI. Harici tarayici acmak bu sarti
 * ihlal ediyordu; kaldirildi.
 *
 * Serit yine de KALIYOR ve durumu soyluyor: kullanici neden yazamadigini
 * bilmeli. Yaptigi tek sey durum bildirmek -- yonlendirme degil.
 */
@Composable
fun LkMembershipBanner(
    membership: MembershipDto?,
    onKapat: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (membership == null) return

    val suresiDoldu = membership.state == "expired"
    val uyariGoster = membership.showBanner == true

    if (!suresiDoldu && !uyariGoster) return

    val zemin = if (suresiDoldu) LkSurfaceSignature else LkSurfaceRaised
    val metin = if (suresiDoldu) {
        "Ücretsiz kullanım süreniz doldu. Hesabınız salt okunur modda; verileriniz duruyor."
    } else {
        val kalan = membership.trialDaysLeft ?: 0
        "Ücretsiz kullanım sürenizin bitmesine $kalan gün kaldı."
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(zemin)
            .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space3),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = null,
            tint = if (suresiDoldu) LkOnSignature else LkWarning,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(LkSpacing.Space2))
        Text(
            text = metin,
            style = LkTypography.getMetadata(),
            color = if (suresiDoldu) LkOnSignature else LkTextSecondary,
            modifier = Modifier.weight(1f)
        )
        // Kapatma yalnizca deneme uyarisinda. Sure dolduysa kapatilamaz.
        if (!suresiDoldu && onKapat != null) {
            Spacer(modifier = Modifier.width(LkSpacing.Space1))
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Kapat",
                tint = LkTextSecondary,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onKapat() }
            )
        }
    }
}

/**
 * Islem sonucu bildirimi — "kaydedildi", "gonderilemedi".
 *
 * 🔴 AYNI BLOK BES EKRANDA KOPYALANMISTI ve hepsi ham Material `Card`
 * kullaniyordu: yaricap 4dp (sistemde 12/20), yukseklik Material'in
 * varsayilani, koyu temada siyah golge. Tek yere alindi.
 *
 * Hata ve basari SADECE RENKLE ayrilmiyor: ikon da degisiyor (§19).
 */
@Composable
fun LkNotice(
    metin: String,
    hataMi: Boolean,
    onKapat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(LkShapes.MD)
            .background(
                if (hataMi) LkDanger.copy(alpha = 0.12f) else LkSuccess.copy(alpha = 0.12f)
            )
            .padding(start = LkSpacing.Space3, top = LkSpacing.Space2, bottom = LkSpacing.Space2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (hataMi) Icons.Outlined.ErrorOutline else Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = if (hataMi) LkDanger else LkSuccess,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(LkSpacing.Space3))
        Text(
            text = metin,
            style = LkTypography.getBodySmall(),
            color = if (hataMi) LkDanger else LkSuccess,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onKapat) {
            Text("Tamam", color = LkTextPrimary, style = LkTypography.getLabel())
        }
    }
}
