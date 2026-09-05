package com.localkarar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.LkHero
import com.localkarar.app.ui.theme.LkSpacing
import com.localkarar.app.ui.theme.LkSurfaceCanvas
import com.localkarar.app.ui.theme.lkIsDark

/*
 * HERO BASLIK BLOGU — `DESIGN.md` §24.6.
 *
 * Uygulamanin karakterini bu tek karar tasiyor: marka renginde tam genislikte
 * baslik blogu, uzerine YUKARI dogru binen 28dp yaricapli yuzey. Incelenen
 * yedi finans kitinin dordunde ayni yapi var.
 *
 * 🔴 KIRPMA TUZAGI — MOCKUP'TA BIR KEZ DUSULDU.
 *
 * Ozet karti "hero'nun uzerine tassin" diye NEGATIF ust bosluk verilirse,
 * kaydirilabilir yuzeyin tasma kirpmasi kartin ust kismini KESER. Mockup'ta
 * kartin ust 24 pikseli gorunmuyordu ve "ustu kapanmis gibi" duruyordu.
 *
 * Dogrusu: binen etki YUZEYIN KENDISINDE (`offset` + `clip`), icerikte degil.
 * Yuzeyin icindeki hicbir sey negatif bosluk kullanmaz; derinligi golge tasir.
 */

/** Hero gradyaninin hangi kumeyi kullanacagi. */
enum class LkHeroTone {
    /** Calisma ekranlari. */
    App,

    /** Giris oncesi akis — webin gradyaninin birebir kendisi. */
    Auth
}

/** Yalnizca marka blogu; binen yuzey cagiran tarafta. */
@Composable
fun LkHeroBlock(
    modifier: Modifier = Modifier,
    tone: LkHeroTone = LkHeroTone.App,
    content: @Composable ColumnScope.() -> Unit
) {
    val stops = when {
        tone == LkHeroTone.Auth -> LkHero.AuthStops
        lkIsDark() -> LkHero.DarkStops
        else -> LkHero.LightStops
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            /*
             * CSS 158deg -> yon vektoru (sin158, -cos158) = (0.375, 0.927).
             * Yani cogunlukla asagi, hafif saga. Oran x:y ~ 0.40:1.
             * `drawBehind` kullaniliyor cunku dogru aciyi vermek icin
             * yuzeyin gercek olcusu gerekiyor.
             */
            .drawBehind {
                drawRect(
                    Brush.linearGradient(
                        colors = stops,
                        start = Offset(0f, 0f),
                        end = Offset(size.width * 0.40f, size.height)
                    )
                )
            },
        content = content
    )
}

/**
 * Hero + uzerine binen yuzey.
 *
 * @param overlap yuzeyin marka bloguna ne kadar binecegi. Degistirilmesi
 *        gerekirse hero'nun alt bosluguyla birlikte dusunulmeli.
 */
@Composable
fun LkHeroScaffold(
    modifier: Modifier = Modifier,
    tone: LkHeroTone = LkHeroTone.App,
    overlap: androidx.compose.ui.unit.Dp = 22.dp,
    hero: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        LkHeroBlock(tone = tone) {
            hero()
            /* Binen yuzeyin altinda kalacak pay. */
            Box(Modifier.fillMaxWidth().padding(bottom = overlap + 8.dp))
        }

        /*
         * `weight(1f)` — `fillMaxSize()` DEGIL.
         *
         * Column icinde `fillMaxSize()` KALAN degil TUM yuksekligi ister;
         * onceki turda tam bu yuzden 50 kaydirilabilir ekranin son satiri
         * dock altinda kaliyordu.
         */
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .offset(y = -overlap)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(LkSurfaceCanvas),
            verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
        ) {
            Box(Modifier.padding(top = LkSpacing.Space5))
            content()
        }
    }
}
