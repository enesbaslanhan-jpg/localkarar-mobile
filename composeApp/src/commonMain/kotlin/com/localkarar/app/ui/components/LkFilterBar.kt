package com.localkarar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.LkOnPrimary
import com.localkarar.app.ui.theme.LkPrimary
import com.localkarar.app.ui.theme.LkPrimaryFill
import com.localkarar.app.ui.theme.LkSpacing
import com.localkarar.app.ui.theme.LkElevation
import com.localkarar.app.ui.theme.LkShapes
import com.localkarar.app.ui.theme.LkSurfaceElevated
import com.localkarar.app.ui.theme.lkElevation
import com.localkarar.app.ui.theme.LkSurfaceRaised
import com.localkarar.app.ui.theme.LkTextPrimary
import com.localkarar.app.ui.theme.LkTextSecondary
import com.localkarar.app.ui.theme.LkTypography

/** Tek bir suzgec secenegi: gosterilen etiket + tasidigi deger. */
data class LkFilterSecenek(val etiket: String, val deger: String?)

/** Ikincil suzgec grubu — "Filtreler" acilinca gorunur. */
data class LkFilterGrup(
    val baslik: String,
    val secenekler: List<LkFilterSecenek>,
    val secili: String?,
    val onSecim: (String?) -> Unit,
    /**
     * Grubun VARSAYILAN degeri.
     *
     * 🔴 Rozette "1" yaziyordu ama kullanici hicbir sey secmemisti:
     * performans donemi varsayilan olarak "30" geliyor ve `null` olmadigi
     * icin "etkin suzgec" sayiliyordu. Etkin demek VARSAYILANDAN FARKLI
     * demek; grubun varsayilani burada bildiriliyor.
     */
    val varsayilan: String? = null
)

/**
 * SUZGEC SERIDI.
 *
 * 🔴 SIPARISLER VE URUNLER EKRANLARI TELEFONDA KULLANILAMIYORDU.
 *
 * Iki ekran da suzgeclerini elle cizilmis kutucuklarla gosteriyordu:
 * `getMicro()` (11sp) yazi, 4dp dikey dolgu, yani ~24dp yukseklik. §19'un
 * dokunma hedefi 44dp; bunlar yarisi kadardi ve parmakla isabet etmiyordu.
 * Ustelik Urunler'de bu seritlerden DORT tane alt alta duruyordu —
 * listeye sira gelmeden ekranin yarisi suzgecti.
 *
 * Yeni desen: ekranda HER ZAMAN tek bir serit (en cok kullanilan suzgec),
 * geri kalan gruplar "Filtreler" dugmesinin arkasinda. Haplar ortak
 * `LkChip` — 44dp hedef, 13sp etiket.
 */
@Composable
fun LkFilterBar(
    birincil: List<LkFilterSecenek>,
    seciliBirincil: String?,
    onBirincil: (String?) -> Unit,
    modifier: Modifier = Modifier,
    gruplar: List<LkFilterGrup> = emptyList()
) {
    var acik by remember { mutableStateOf(false) }

    /** Kac ikincil suzgec varsayilandan farkli? Rozette bu sayi var. */
    val etkinIkincil = gruplar.count { it.secili != it.varsayilan }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = LkSpacing.Space4
                ),
                horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)
            ) {
                items(birincil) { secenek ->
                    LkChip(
                        text = secenek.etiket,
                        selected = seciliBirincil == secenek.deger,
                        onClick = { onBirincil(secenek.deger) }
                    )
                }
            }

            if (gruplar.isNotEmpty()) {
                Box {
                    Row(
                        modifier = Modifier
                            .heightIn(min = 44.dp)
                            .clickable { acik = !acik }
                            .padding(horizontal = LkSpacing.Space4),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filtreler",
                            style = LkTypography.getLabel(),
                            color = if (etkinIkincil > 0) LkPrimary else LkTextSecondary
                        )
                        if (etkinIkincil > 0) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 6.dp)
                                    .size(18.dp)
                                    .background(LkPrimaryFill, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = etkinIkincil.toString(),
                                    style = LkTypography.getMicro(),
                                    color = LkOnPrimary
                                )
                            }
                        }
                        Icon(
                            imageVector = if (acik) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = null,
                            tint = if (etkinIkincil > 0) LkPrimary else LkTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    /*
                     * 🔴 IKINCIL SUZGECLER ONCE SATIR ICINDE ACILIYORDU
                     * (liste asagi kayiyordu), sonra EKRANIN ALTINDAN
                     * (dugme yukarida, panel asagida — dokunulan yerle
                     * acilan yer birbirini tutmuyordu).
                     *
                     * Dogru yer dugmenin KENDI ALTI: panel dugmeye bagli
                     * bir katman olarak aciliyor, arkadaki liste yerinden
                     * oynamiyor, disariya dokununca kapaniyor.
                     */
                    if (acik) {
                        Popup(
                            alignment = Alignment.TopEnd,
                            offset = IntOffset(0, with(LocalDensity.current) { 46.dp.roundToPx() }),
                            onDismissRequest = { acik = false },
                            properties = PopupProperties(focusable = true)
                        ) {
                            SuzgecPaneli(
                                gruplar = gruplar,
                                etkinIkincil = etkinIkincil,
                                onKapat = { acik = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * "Filtreler" dugmesinin altinda acilan panel.
 *
 * Genislik SABIT: kendi genisligini iceriginden alsaydi tek secenekli bir
 * grupta dar, uzun etiketli bir grupta ekrandan tasan bir kutu olurdu.
 */
@Composable
private fun SuzgecPaneli(
    gruplar: List<LkFilterGrup>,
    etkinIkincil: Int,
    onKapat: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(end = LkSpacing.Space3)
            .width(292.dp)
            .clip(LkShapes.LG)
            .lkElevation(LkElevation.OVERLAY, LkShapes.LG)
            .background(LkSurfaceElevated)
            .padding(LkSpacing.Space4),
        verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Filtreler",
                style = LkTypography.getBodyStrong(),
                color = LkTextPrimary,
                modifier = Modifier.weight(1f)
            )
            /* Varsayilandan sapan grup varsa hepsini geri alma yolu —
               yoksa kullanici her grubu tek tek gezmek zorunda kaliyordu. */
            if (etkinIkincil > 0) {
                Text(
                    text = "Temizle",
                    style = LkTypography.getLabel(),
                    color = LkPrimary,
                    modifier = Modifier
                        .clickable {
                            gruplar.forEach { grup ->
                                if (grup.secili != grup.varsayilan) grup.onSecim(grup.varsayilan)
                            }
                        }
                        .padding(start = LkSpacing.Space2, top = 6.dp, bottom = 6.dp)
                )
            }
        }

        gruplar.forEach { grup ->
            Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                Text(
                    text = grup.baslik,
                    style = LkTypography.getMicro(),
                    color = LkTextSecondary
                )
                /* Haplar ikiserli sariliyor; dar panelde yatay kaydirma
                   secenegin varligini gizlerdi. */
                grup.secenekler.chunked(2).forEach { ikili ->
                    Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                        ikili.forEach { secenek ->
                            LkChip(
                                text = secenek.etiket,
                                selected = grup.secili == secenek.deger,
                                onClick = { grup.onSecim(secenek.deger) }
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = "Bitti",
            style = LkTypography.getLabel(),
            color = LkPrimary,
            modifier = Modifier
                .align(Alignment.End)
                .heightIn(min = 44.dp)
                .clickable(onClick = onKapat)
                .padding(horizontal = LkSpacing.Space2, vertical = LkSpacing.Space3)
        )
    }
}
