package com.localkarar.app.ui.components

import com.localkarar.app.core.trBuyuk
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.LkBrand
import com.localkarar.app.ui.theme.LkSpacing
import com.localkarar.app.ui.theme.LkSurfaceCanvas
import com.localkarar.app.ui.theme.LkTypography
import com.localkarar.app.ui.theme.lkIsDark

/*
 * KAPAK BASLIGI — sosyal profil deseni.
 *
 * UZAK FOTOGRAF ARTIK CIZILIYOR (Coil 3).
 *
 * coverUrl ve avatarUrl sunucudan GORELI yol olarak geliyor
 * (src/services/auth.ts:174 -> "/auth/avatar/<dosya>"); LkRemoteImage
 * taban adresi basa ekliyor, yoksa istek hicbir yere gitmez.
 *
 * Fotograf yoksa, yuklenirken ya da dosya silinmisse kodla cizilen kapak
 * ve bas harf avatari gosteriliyor — hicbir durumda bos kutu kalmiyor.
 *
 * ⚠️ Kapaklar sunucuda HIC SERVIS EDILMIYORDU: GET /auth/avatar/:storedName
 * yalniz avatarStoredName ile sorguluyordu ve her kapak istegi 404
 * donuyordu. Web deposunda duzeltildi (a36dff1) ve regresyon testi eklendi.
 */
@Composable
fun LkCoverHeader(
    ad: String,
    modifier: Modifier = Modifier,
    kapakUrl: String? = null,
    avatarUrl: String? = null,
    kapakYuksekligi: androidx.compose.ui.unit.Dp = 132.dp,
    avatarBoyutu: androidx.compose.ui.unit.Dp = 88.dp,
    icerik: @Composable () -> Unit
) {
    val koyu = lkIsDark()
    Box(modifier.fillMaxWidth()) {
        /*
         * Kapak bandi. Fotograf varsa o cizilir; yoksa (ya da yuklenirken,
         * ya da dosya silinmisse) kodla cizilen desen. Bos bir kutu hicbir
         * durumda gorunmez.
         */
        LkRemoteImage(
            url = kapakUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(kapakYuksekligi)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(kapakYuksekligi)
                    .background(
                        Brush.linearGradient(
                            if (koyu) listOf(LkBrand.B700, LkBrand.B600)
                            else listOf(LkBrand.B600, LkBrand.B400)
                        )
                    )
            ) {
                Canvas(Modifier.fillMaxWidth().height(kapakYuksekligi)) {
                    drawCircle(
                        Color(0x1AFFFFFF),
                        radius = size.height * 0.58f,
                        center = Offset(size.width * 0.80f, size.height * 0.15f)
                    )
                    drawCircle(
                        Color(0x14FFFFFF),
                        radius = size.height * 0.33f,
                        center = Offset(size.width * 0.94f, size.height * 0.79f)
                    )
                    drawCircle(
                        Color(0x0FFFFFFF),
                        radius = size.height * 0.41f,
                        center = Offset(size.width * 0.18f, size.height * 0.91f)
                    )
                }
            }
        }

        /*
         * Avatar kapagin ALT SINIRINA biniyor. Kapak yuksekliginden avatarin
         * yarisi kadar asagi kaydiriliyor; halka rengi zeminle ayni, boylece
         * avatar kapaktan "kesilmis" gibi degil, uzerine oturmus gibi duruyor.
         */
        Box(
            modifier = Modifier
                .padding(start = LkSpacing.Space5)
                .offset(y = kapakYuksekligi - avatarBoyutu / 2)
                .size(avatarBoyutu)
                .clip(CircleShape)
                .background(LkSurfaceCanvas)
                .padding(4.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(LkBrand.B500, LkBrand.B700))
                ),
            contentAlignment = Alignment.Center
        ) {
            /* Avatar fotografi; yoksa bas harf. */
            LkRemoteImage(
                url = avatarUrl,
                contentDescription = ad,
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = ad.trim().take(1).trBuyuk().ifBlank { "?" },
                        style = LkTypography.getTitleL(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        /* Govde: avatarin altta kalan yarisi kadar bosluk birakiyor. */
        Box(Modifier.padding(top = kapakYuksekligi + avatarBoyutu / 2 + LkSpacing.Space3)) {
            icerik()
        }
    }
}
