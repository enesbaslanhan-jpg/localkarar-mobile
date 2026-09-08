package com.localkarar.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.localkarar.app.network.ApiError
import com.localkarar.app.ui.theme.*

/**
 * Yukleme durumunun GORSEL kimligi — §24 dort zorunlu animasyondan ikincisi.
 *
 * 🔴 ONCEDEN EKRANIN ORTASINDA DONEN HALKAYDI. `LkSkeleton` bilesen
 * katmaninda yaziliydi ama HICBIR EKRAN CAGIRMIYORDU; bekleme her yerde
 * ayni anlamsiz halkayla gecistiriliyordu. Iskelet, gelecek icerigin
 * SEKLINI onceden cizer: goz yerlesime hazirlanir, bekleme kisa hissedilir.
 *
 * Desen cagiran tarafca secilir, cunku liste ekraniyla form ekraninin
 * iskeleti ayni degil; yanlis iskelet bosluga bakmaktan daha kotudur.
 *
 * Hareket kisitlamasi acikken parlama durur (`LkSkeleton` icinde), yer
 * tutucular duz yuzey olarak kalir.
 */
@Composable
fun LkLoadingState(
    modifier: Modifier = Modifier,
    desen: LkLoadingDesen = LkLoadingDesen.LISTE
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(LkSpacing.Space4)
            .semantics { contentDescription = "Yükleniyor" },
        verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
    ) {
        when (desen) {
            LkLoadingDesen.LISTE -> LkSkeletonRows(satir = 5)

            LkLoadingDesen.DETAY -> {
                LkCard {
                    LkSkeleton(Modifier.fillMaxWidth(0.42f).height(12.dp))
                    Spacer(Modifier.height(LkSpacing.Space3))
                    LkSkeleton(Modifier.fillMaxWidth(0.66f).height(34.dp), LkShapes.MD)
                    Spacer(Modifier.height(LkSpacing.Space4))
                    Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space4)) {
                        LkSkeleton(Modifier.weight(1f).height(46.dp), LkShapes.MD)
                        LkSkeleton(Modifier.weight(1f).height(46.dp), LkShapes.MD)
                    }
                }
                LkSkeletonRows(satir = 3)
            }

            LkLoadingDesen.FORM -> {
                repeat(4) {
                    Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                        LkSkeleton(Modifier.fillMaxWidth(0.3f).height(11.dp))
                        LkSkeleton(Modifier.fillMaxWidth().height(52.dp), LkShapes.LG)
                    }
                }
                LkSkeleton(Modifier.fillMaxWidth().height(52.dp), LkShapes.FULL)
            }
        }
    }
}

/** Iskeletin hangi icerigi taklit edecegi. */
enum class LkLoadingDesen { LISTE, DETAY, FORM }

/**
 * Satir ici / dugme ici bekleme.
 *
 * Iskelet ekranin tamamini temsil eder; bu ise TEK bir islemin surdugunu
 * soyler (kaydet, esitle, gonder). Ikisi ayni sey degil.
 */
@Composable
fun LkLoadingSpinner(
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    /** Renkli zemin uzerinde (hero, birincil dugme) marka rengi okunmaz. */
    renk: Color = LkPrimary
) {
    /*
     * 🔴 DONEN MAVI CEMBER (Material `CircularProgressIndicator`)
     * KALDIRILDI. Sistemin kendi bileseniydi: kendi olcusunu, kendi
     * hizini ve kendi mavisini getiriyordu; uygulamanin geri kalani §24
     * dilindeyken bekleme gostergesi baska bir uygulamadan gelmis gibi
     * duruyordu.
     *
     * Yerine UC NOKTA sirayla nefes aliyor. Sakin, ucuz ve marka
     * rengiyle cizilen bir isaret.
     *
     * ⚠️ HAREKET KISITLIYKEN DONMEZ: noktalar sabit ve tam opakliktadir
     * (§12) — bekleme yine anlasilir, ama ekranda titreyen bir sey olmaz.
     */
    val kisitli = isReducedMotionEnabled()
    val nokta = size / 3.2f
    val gecis = rememberInfiniteTransition(label = "yukleniyor")

    Row(
        modifier = modifier.height(size),
        horizontalArrangement = Arrangement.spacedBy(nokta / 2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val alpha = if (kisitli) 1f else gecis.animateFloat(
                initialValue = 0.35f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(560, delayMillis = index * 160),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "nokta$index"
            ).value

            Box(
                modifier = Modifier
                    .size(nokta)
                    .clip(CircleShape)
                    .background(renk.copy(alpha = alpha))
            )
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

@Composable
private fun hataGorunumu(hata: Throwable?): HataGorunumu = when (hata) {
    is ApiError.NetworkUnavailable -> HataGorunumu(
        simge = Icons.Outlined.CloudOff,
        baslik = "Bağlantı Yok",
        renk = LkTextSecondary,
        tekrarDenenebilir = true
    )
    is ApiError.Timeout -> HataGorunumu(
        simge = Icons.Outlined.Schedule,
        baslik = "Yanıt Gecikti",
        renk = LkTextSecondary,
        tekrarDenenebilir = true
    )
    is ApiError.NotFound -> HataGorunumu(
        simge = Icons.Outlined.SearchOff,
        baslik = "Bulunamadı",
        renk = LkTextSecondary,
        tekrarDenenebilir = false
    )
    // Uyelik suresinin dolmasi bir "hata" degil bir DURUM; kirmizi unlem
    // yanlis sinyal verir ve tekrar denemek hicbir seyi degistirmez.
    is ApiError.MembershipExpired -> HataGorunumu(
        simge = Icons.Outlined.Lock,
        baslik = "Üyelik Süresi Doldu",
        renk = LkWarning,
        tekrarDenenebilir = false
    )
    is ApiError.Forbidden, is ApiError.Unauthorized -> HataGorunumu(
        simge = Icons.Outlined.Lock,
        baslik = "Erişim Yok",
        renk = LkWarning,
        tekrarDenenebilir = false
    )
    else -> HataGorunumu(
        simge = Icons.Outlined.Error,
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
                imageVector = Icons.Outlined.SearchOff,
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
