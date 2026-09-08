package com.localkarar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.LkDanger
import com.localkarar.app.ui.theme.LkShapes
import com.localkarar.app.ui.theme.LkSuccess
import com.localkarar.app.ui.theme.LkSurfaceSunken
import com.localkarar.app.ui.theme.LkTextSecondary
import com.localkarar.app.ui.theme.LkTypography
import com.localkarar.app.ui.theme.LkWarning

/** Sunucunun TEK zorunlu kurali (`auth.ts`: PASSWORD_MIN = 10). */
private const val ZORUNLU_UZUNLUK = 10

/**
 * Parola gucu — mockup "Ayar 4".
 *
 * ⚠️ ZORUNLU KURAL ILE TAVSIYE AYRI. Sunucu yalniz uzunluk dayatiyor;
 * rakam/buyuk harf/isaret zorunlu DEGIL. Bunlari "gerekli" diye yazmak
 * kullaniciya var olmayan bir kural ogretmek olurdu — tavsiye olarak
 * yaziliyor ve dugmeyi engellemiyor.
 *
 * 🔴 "Zayıf" demek yetmez: eksigin NE oldugu yaziyor. "Zayıf" tek basina
 * kullaniciya ne yapacagini soylemiyor.
 */
@Composable
fun LkPasswordStrength(parola: String, modifier: Modifier = Modifier) {
    if (parola.isEmpty()) return

    val yeterinceUzun = parola.length >= ZORUNLU_UZUNLUK
    val rakamVar = parola.any { it.isDigit() }
    val buyukVar = parola.any { it.isUpperCase() }
    val isaretVar = parola.any { !it.isLetterOrDigit() }

    val puan = listOf(yeterinceUzun, rakamVar, buyukVar, isaretVar).count { it }
    val renk = when {
        !yeterinceUzun -> LkDanger
        puan >= 4 -> LkSuccess
        puan == 3 -> LkWarning
        else -> LkWarning
    }

    val eksikler = buildList {
        if (!rakamVar) add("bir rakam")
        if (!buyukVar) add("bir büyük harf")
        if (!isaretVar) add("bir noktalama işareti")
    }

    val mesaj = when {
        !yeterinceUzun ->
            "En az $ZORUNLU_UZUNLUK karakter gerekiyor — ${ZORUNLU_UZUNLUK - parola.length} karakter daha."
        eksikler.isEmpty() -> "Güçlü parola."
        else -> eksikler.joinToString(" ve ") + " eklerseniz daha güçlü olur."
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(4) { i ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(LkShapes.FULL)
                        .background(if (i < puan) renk else LkSurfaceSunken)
                )
            }
        }
        Text(
            text = mesaj,
            style = LkTypography.getMetadata(),
            color = if (yeterinceUzun) LkTextSecondary else LkDanger
        )
    }
}
