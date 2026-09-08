package com.localkarar.app.ui.components

import com.localkarar.app.core.trBuyuk
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localkarar.app.ui.theme.LkPrimary
import com.localkarar.app.ui.theme.LkPrimarySoft
import com.localkarar.app.ui.theme.LkTypography

/*
 * AVATAR — fotograf varsa fotograf, yoksa bas harf.
 *
 * Uygulamada dokuz ayri yerde elle yazilmis "bas harfli daire" vardi ve
 * hicbiri `avatarUrl`e bakmiyordu; sunucu fotografi donderse bile ekranda
 * hep harf gorunuyordu. Tek bilesende toplandi.
 *
 * Bas harf YEDEK, sussuz bir hata durumu degil: ag yavassa ya da dosya
 * silinmisse kullanici bos bir daire degil tanidik bir harf gorur.
 */
@Composable
fun LkAvatar(
    ad: String?,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
    boyut: Dp = 40.dp,
    zemin: Color = LkPrimarySoft,
    harfRengi: Color = LkPrimary,
    /** Resmi olmayan ozel durumlar icin (orn. resmi gonderilerde "LK"). */
    harfOverride: String? = null
) {
    val harf = harfOverride
        ?: ad?.trim()?.take(1)?.trBuyuk()?.takeIf { it.isNotBlank() }
        ?: "?"

    Box(
        modifier = modifier
            .size(boyut)
            .clip(CircleShape)
            .background(zemin),
        contentAlignment = Alignment.Center
    ) {
        LkRemoteImage(
            url = avatarUrl,
            contentDescription = ad,
            modifier = Modifier.fillMaxSize().clip(CircleShape)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = harf,
                    /* Harf boyutu dairenin ~%40'i: 24dp'lik bir avatarda
                       sabit 16sp tasar, 88dp'likte kaybolur. */
                    style = LkTypography.getBodyStrong().copy(
                        fontSize = (boyut.value * 0.40f).sp
                    ),
                    color = harfRengi,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
