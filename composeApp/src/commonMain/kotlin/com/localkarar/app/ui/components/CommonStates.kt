package com.localkarar.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.localkarar.app.network.ApiError
import com.localkarar.app.ui.theme.*

@Composable
fun LkLoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = LkPrimary)
            Spacer(modifier = Modifier.height(LkSpacing.Space4))
            Text(text = "Yükleniyor...", style = LkTypography.getBodySmall(), color = LkTextSecondary)
        }
    }
}

/**
 * Hatanin GORSEL kimligi.
 *
 * NEDEN VAR:
 *
 * `ApiError` sekiz varyant tanimliyordu (NetworkUnavailable, Timeout,
 * Unauthorized, Forbidden, NotFound, ValidationError, ServerError,
 * UnknownError) ama `is ApiError.` kalibi TUM commonMain icinde SIFIR kez
 * eslestiriliyordu. Her sey `e.message` olarak tek bir ekrana dusuyordu:
 * cevrimdisi olan kullanici ile 500 alan kullanici ayni kirmizi unlemi ve
 * ayni "Tekrar Dene" dugmesini goruyordu.
 *
 * Ayrim onemli, cunku kullanicinin YAPACAGI SEY farkli: cevrimdisiysa
 * baglantisini kontrol etmeli, yetkisi yoksa tekrar denemenin anlami yok,
 * kayit bulunamadiysa geri donmeli.
 */
private data class HataGorunumu(
    val simge: ImageVector,
    val baslik: String,
    val renk: Color,
    /** Tekrar denemenin ANLAMLI oldugu durumlar. Yetki hatasinda degil. */
    val tekrarDenenebilir: Boolean
)

private fun hataGorunumu(hata: Throwable?): HataGorunumu = when (hata) {
    is ApiError.NetworkUnavailable -> HataGorunumu(
        simge = Icons.Default.CloudOff,
        baslik = "Bağlantı Yok",
        renk = LkTextSecondary,
        tekrarDenenebilir = true
    )
    is ApiError.Timeout -> HataGorunumu(
        simge = Icons.Default.Schedule,
        baslik = "Yanıt Gecikti",
        renk = LkTextSecondary,
        tekrarDenenebilir = true
    )
    is ApiError.NotFound -> HataGorunumu(
        simge = Icons.Default.SearchOff,
        baslik = "Bulunamadı",
        renk = LkTextSecondary,
        tekrarDenenebilir = false
    )
    // Uyelik suresinin dolmasi bir "hata" degil bir DURUM; kirmizi unlem
    // yanlis sinyal verir ve tekrar denemek hicbir seyi degistirmez.
    is ApiError.MembershipExpired -> HataGorunumu(
        simge = Icons.Default.Lock,
        baslik = "Üyelik Süresi Doldu",
        renk = LkWarning,
        tekrarDenenebilir = false
    )
    is ApiError.Forbidden, is ApiError.Unauthorized -> HataGorunumu(
        simge = Icons.Default.Lock,
        baslik = "Erişim Yok",
        renk = LkWarning,
        tekrarDenenebilir = false
    )
    else -> HataGorunumu(
        simge = Icons.Default.Error,
        baslik = "Bir Hata Oluştu",
        renk = LkDanger,
        tekrarDenenebilir = true
    )
}

/**
 * @param hata Varsa hatanin kendisi. Verildiginde simge, baslik ve "Tekrar
 *        Dene" dugmesinin gorunup gorunmeyecegi TURE gore secilir. Verilmezse
 *        eski davranis (genel hata ekrani) korunur -- cagri yerleri asamali
 *        gecirilebilsin diye.
 */
@Composable
fun LkErrorState(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    hata: Throwable? = null
) {
    val gorunum = hataGorunumu(hata)

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(LkSpacing.Space8)
        ) {
            Icon(
                imageVector = gorunum.simge,
                contentDescription = gorunum.baslik,
                tint = gorunum.renk,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(LkSpacing.Space4))
            Text(
                text = gorunum.baslik,
                style = LkTypography.getSectionTitle(),
                color = LkTextPrimary
            )
            Spacer(modifier = Modifier.height(LkSpacing.Space2))
            Text(
                text = message,
                style = LkTypography.getBody(),
                color = LkTextSecondary,
                textAlign = TextAlign.Center
            )

            if (onRetry != null && gorunum.tekrarDenenebilir) {
                Spacer(modifier = Modifier.height(LkSpacing.Space8))
                LkButton(text = "Tekrar Dene", onClick = onRetry)
            }
        }
    }
}

/**
 * Aranan kayit yok.
 *
 * BOS LISTE ILE AYNI SEY DEGIL ve onceki halinde ikisi karisiyordu: hem liste
 * hem detay ekranlari "... bulunamadi" diyordu. Kullanici acisindan fark buyuk:
 * bos liste "henuz eklemedin", bulunamadi ise "bu kayit silinmis ya da sana
 * ait degil" demek.
 */
@Composable
fun LkNotFoundState(
    aciklama: String,
    baslik: String = "Bulunamadı",
    onGeri: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(LkSpacing.Space8)
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = baslik,
                tint = LkTextSecondary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(LkSpacing.Space4))
            Text(text = baslik, style = LkTypography.getSectionTitle(), color = LkTextPrimary)
            Spacer(modifier = Modifier.height(LkSpacing.Space2))
            Text(
                text = aciklama,
                style = LkTypography.getBody(),
                color = LkTextSecondary,
                textAlign = TextAlign.Center
            )
            if (onGeri != null) {
                Spacer(modifier = Modifier.height(LkSpacing.Space8))
                LkButton(text = "Geri Dön", onClick = onGeri)
            }
        }
    }
}
