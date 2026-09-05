package com.localkarar.app.ui.components

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
 * 🔴 UZAK FOTOGRAF HENUZ CIZILEMIYOR.
 *
 * Sunucu ve mobil DTO `coverUrl` ile `avatarUrl` tasiyor (CommunityDtos.kt:143-144),
 * ama projede HICBIR goruntu yukleme kutuphanesi yok — ne Coil, ne Kamel.
 * `AsyncImage` benzeri tek bir cagri bile yok. Yani alan geliyor, cizilemiyor.
 *
 * Bu bilesen su an KODLA CIZILEN bir kapak ve bas harf avatari veriyor:
 * yerlesim, olcu ve binme dogru; fotograf gelince yalniz iki `Box`un icerigi
 * degisecek. Uydurma bir yer tutucu gorsel konmadi.
 *
 * ⚠️ Fotograflarin gercekten cizilmesi icin Coil 3 (multiplatform) gibi bir
 * bagimlilik eklenmeli. Bu ayri bir karar; §24 kapsaminda degil.
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
        /* Kapak bandi. `kapakUrl` bos olmasa da su an cizilemiyor. */
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
            Text(
                text = ad.trim().take(1).uppercase().ifBlank { "?" },
                style = LkTypography.getTitleL(),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        /* Govde: avatarin altta kalan yarisi kadar bosluk birakiyor. */
        Box(Modifier.padding(top = kapakYuksekligi + avatarBoyutu / 2 + LkSpacing.Space3)) {
            icerik()
        }
    }
}
