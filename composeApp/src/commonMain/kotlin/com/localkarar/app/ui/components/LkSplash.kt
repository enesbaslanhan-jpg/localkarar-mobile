package com.localkarar.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.LkBrand
import com.localkarar.app.ui.theme.LkTypography
import com.localkarar.app.ui.theme.isReducedMotionEnabled
import kotlin.math.cos
import kotlin.math.sin

/**
 * PUSULA KADRANI — esmerkezli halkalar + 24 taksimat + yon oku.
 *
 * Karsilama ekraninda (mockup "Giriş 1") kullaniliyordu; acilis ekrani da
 * ayni isareti kullaniyor ki uygulama ACILISTAN karsilamaya kadar tek bir
 * gorsel fikirle aksin.
 *
 * ⚠️ SURESIZ DONMUYOR: ok −142°'den gelip kuzeye oturuyor ve duruyor.
 * Hareket kisitliyken dogrudan kuzeyde beliriyor.
 */
@Composable
fun LkPusulaKadrani(modifier: Modifier = Modifier) {
    val kisitli = isReducedMotionEnabled()
    val aci = remember { Animatable(if (kisitli) 0f else -142f) }

    LaunchedEffect(kisitli) {
        if (kisitli) aci.snapTo(0f) else aci.animateTo(0f, tween(1100))
    }

    /*
     * 🔴 ILK HALI FOYDEKI KADRAN DEGILDI: yalnizca esmerkezli halkalar ve
     * ince bir cizgiden ibaret bir okla. Foyde ("Giriş 1") gercek bir
     * pusula var — YON HARFLERI (K/D/G/B), taksimatlar ve altin ucu olan
     * bir igne.
     *
     * ⚠️ SURESIZ DONMUYOR: igne −142°'den gelip kuzeye oturuyor ve
     * duruyor. Hareket kisitliyken dogrudan kuzeyde beliriyor.
     */
    val cizgi = Color(0x33FFFFFF)
    val guclu = Color(0x66FFFFFF)
    val harfRengi = Color(0x99FFFFFF)
    val olcer = rememberTextMeasurer()
    val harfStili = LkTypography.getMicro().copy(color = harfRengi)

    Canvas(modifier = modifier) {
        val merkez = Offset(size.width / 2f, size.height / 2f)
        val disHalka = minOf(size.width, size.height) / 2f - 6f
        val kadran = disHalka * 0.86f

        /* Iki halka: dis cerceve ve taksimat halkasi. */
        drawCircle(cizgi, radius = disHalka, center = merkez, style = Stroke(1.dp.toPx()))
        drawCircle(
            cizgi.copy(alpha = 0.12f),
            radius = kadran * 0.72f,
            center = merkez,
            style = Stroke(1.dp.toPx())
        )

        /* 36 taksimat; her dokuzuncusu (ana yonler) uzun ve parlak. */
        repeat(36) { i ->
            val a = (i * 10f - 90f) * (kotlin.math.PI / 180f).toFloat()
            val anaYon = i % 9 == 0
            val disR = kadran
            val icR = kadran - if (anaYon) kadran * 0.14f else kadran * 0.07f
            drawLine(
                color = if (anaYon) guclu else cizgi,
                start = Offset(merkez.x + cos(a) * icR, merkez.y + sin(a) * icR),
                end = Offset(merkez.x + cos(a) * disR, merkez.y + sin(a) * disR),
                strokeWidth = if (anaYon) 2.5f else 1f,
                cap = StrokeCap.Round
            )
        }

        /* YON HARFLERI — kuzey/doğu/güney/batı, foydeki gibi. */
        val harfYaricapi = kadran * 0.80f
        listOf(
            "K" to -90f,
            "D" to 0f,
            "G" to 90f,
            "B" to 180f
        ).forEach { (harf, derece) ->
            val a = derece * (kotlin.math.PI / 180f).toFloat()
            val olcum = olcer.measure(harf, harfStili)
            drawText(
                textLayoutResult = olcum,
                topLeft = Offset(
                    merkez.x + cos(a) * harfYaricapi - olcum.size.width / 2f,
                    merkez.y + sin(a) * harfYaricapi - olcum.size.height / 2f
                )
            )
        }

        /*
         * IGNE: ust yarisi ALTIN (kuzeyi gosteren uc), alt yarisi beyaz.
         * Iki ince ucgen; cizgi degil, gercek bir igne silueti.
         */
        val donme = aci.value * (kotlin.math.PI / 180f).toFloat()
        fun nokta(uzunluk: Float, yanSapma: Float): Offset {
            val x = yanSapma
            val y = -uzunluk
            return Offset(
                merkez.x + (x * cos(donme) - y * sin(donme)),
                merkez.y + (x * sin(donme) + y * cos(donme))
            )
        }

        /*
         * ⚠️ IGNE MERKEZDEN BASLAMIYOR: ortada marka isareti duruyor ve
         * igne onun uzerinden gecince iki sekil birbirini yiyordu. Igne
         * isaretin DISINDA, kadranin ic halkasi ile dis taksimatlari
         * arasinda duruyor.
         */
        val disUc = kadran * 0.86f
        val icUc = kadran * 0.46f
        val genislik = kadran * 0.05f

        drawPath(
            Path().apply {
                moveTo(nokta(disUc, 0f).x, nokta(disUc, 0f).y)
                lineTo(nokta(icUc, genislik).x, nokta(icUc, genislik).y)
                lineTo(nokta(icUc, -genislik).x, nokta(icUc, -genislik).y)
                close()
            },
            color = Color(0xFFE3A857)
        )
        drawPath(
            Path().apply {
                moveTo(nokta(-disUc * 0.82f, 0f).x, nokta(-disUc * 0.82f, 0f).y)
                lineTo(nokta(-icUc, genislik).x, nokta(-icUc, genislik).y)
                lineTo(nokta(-icUc, -genislik).x, nokta(-icUc, -genislik).y)
                close()
            },
            color = Color(0x8CFFFFFF)
        )
    }
}

/**
 * ACILIS EKRANI — oturum kontrol edilirken gorunen kare.
 *
 * 🔴 BURADA DUZ BIR ZEMIN VE ORTASINDA MATERIAL'IN DONEN HALKASI VARDI.
 * Uygulamanin ilk karesi markasiz ve sistemin varsayilan bileseniyle
 * aciliyordu; kullanicinin gordugu ilk sey uygulamaya ait degildi.
 *
 * Artik marka gradyani + pusula kadrani + marka isareti: pencere acilis
 * temasiyla (Android `windowBackground`) ayni renk ailesinde, o yuzden
 * sistem karesinden Compose karesine gecis sicramiyor.
 */
@Composable
fun LkAcilisEkrani(
    modifier: Modifier = Modifier,
    isaretBoyutu: Dp = 76.dp
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(listOf(LkBrand.B700, LkBrand.B500))
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(260.dp),
            contentAlignment = Alignment.Center
        ) {
            LkPusulaKadrani(Modifier.fillMaxSize())
            /*
             * Marka isareti kadranin ORTASINDA, kendi yuvarlak karesinde —
             * foydeki "Giriş 1" boyle. Yuva olmadan isaret kadran
             * cizgilerinin uzerinde yuzuyor ve okunmuyordu.
             */
            Box(
                modifier = Modifier
                    .size(isaretBoyutu + 28.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0x33061018))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                LkBrandMark(size = isaretBoyutu, hareketli = true)
            }
        }
    }
}
