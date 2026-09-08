package com.localkarar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.LkOnPrimary
import com.localkarar.app.ui.theme.LkPrimaryFill
import com.localkarar.app.ui.theme.LkShapes
import com.localkarar.app.ui.theme.LkSurfaceSunken
import com.localkarar.app.ui.theme.LkTextSecondary
import com.localkarar.app.ui.theme.LkTypography

/**
 * SEGMENT — mockup'in "hap icinde hap" deseni.
 *
 * Ayri ayri duran haplardan farki: secenekler TEK bir cukur yolun icinde
 * duruyor, secili olan o yolun icinde dolu bir hap. Boylece secenekler
 * birbirinin ALTERNATIFI oldugunu soyluyor; ayri haplar coklu secim gibi
 * okunuyordu.
 *
 * Iki-dort secenek icin. Daha fazlasi kayan hap seridi olur (`LkChip`).
 *
 * Dokunma hedefi 44dp (§19): gorsel yukseklik 36dp, kalani dolgu.
 */
@Composable
fun <T> LkSegment(
    secenekler: List<T>,
    secili: T,
    etiket: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(LkShapes.FULL)
            .background(LkSurfaceSunken)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        secenekler.forEach { secenek ->
            val aktif = secenek == secili
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp)
                    .clip(LkShapes.FULL)
                    .background(if (aktif) LkPrimaryFill else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable { onSelect(secenek) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = etiket(secenek),
                    style = LkTypography.getLabel(),
                    color = if (aktif) LkOnPrimary else LkTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
