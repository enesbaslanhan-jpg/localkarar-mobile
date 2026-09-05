package com.localkarar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.LkHero
import com.localkarar.app.ui.theme.LkSpacing
import com.localkarar.app.ui.theme.LkSurfaceCanvas
import com.localkarar.app.ui.theme.LkTypography

/*
 * §24.6 — HERO SAYFA KABUGU.
 *
 * `LkPageLayout` ile AYNI IMZA: title / onBack / actions / content. Boylece
 * bir ekrani yeni tasarim diline tasimak tek kelimelik bir degisiklik
 * (`LkPageLayout` -> `LkHeroPage`) ve 48 ekranin her birini elle yeniden
 * yapilandirma riski ortadan kalkiyor.
 *
 * Farklar:
 *   - Baslik cubugu marka renginde bir blok icinde; ustune 28dp yaricapli
 *     yuzey biniyor.
 *   - `TopAppBar` ve `Divider` yok: ayrim cizgiyle degil yuzey ve renkle
 *     yapiliyor (§3.1 oncelik sirasi).
 *   - Baslik ve eylem ikonlari beyaz. §24.6 geregi ikincil metin %85 beyaz
 *     opakligin altina inmez.
 *
 * ⚠️ Icerik kaydirilabilirse kaydirma KABIN ICINDE olmali. Binen yuzey
 * `clip` ediyor; icerige negatif ust bosluk verilirse ustu kirpilir —
 * mockup'ta bu tuzaga bir kez dusuldu.
 */
@Composable
fun LkHeroPage(
    title: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier,
    /** Baslik altinda hero icinde duracak ek icerik (ozet rakamlar vb.). */
    heroExtra: (@Composable () -> Unit)? = null,
    overlap: Dp = 22.dp,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {

        LkHeroBlock {
            if (title != null || onBack != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = if (onBack != null) LkSpacing.Space2 else LkSpacing.Space5,
                            end = LkSpacing.Space3,
                            top = LkSpacing.Space4
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.Outlined.ArrowBack,
                                contentDescription = "Geri",
                                tint = LkHero.OnHero
                            )
                        }
                        Box(Modifier.width(LkSpacing.Space1))
                    }
                    Text(
                        text = title.orEmpty(),
                        style = LkTypography.getTitleS(),
                        color = LkHero.OnHero,
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        content = actions
                    )
                }
            }

            heroExtra?.invoke()

            /* Binen yuzeyin altinda kalacak pay. */
            Box(Modifier.fillMaxWidth().height(overlap + LkSpacing.Space4))
        }

        /*
         * `weight(1f)` — `fillMaxSize()` DEGIL. Column icinde `fillMaxSize()`
         * KALAN degil TUM yuksekligi ister; onceki turda tam bu yuzden 50
         * kaydirilabilir ekranin son satiri dock altinda kaliyordu.
         */
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .offset(y = -overlap)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(LkSurfaceCanvas)
        ) {
            content()
        }
    }
}
