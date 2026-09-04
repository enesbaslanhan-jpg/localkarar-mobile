package com.localkarar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.LkDanger
import com.localkarar.app.ui.theme.LkPrimary
import com.localkarar.app.ui.theme.LkLineStrong
import com.localkarar.app.ui.theme.LkShapes
import com.localkarar.app.ui.theme.LkSurfaceSunken
import com.localkarar.app.ui.theme.LkTextMuted
import com.localkarar.app.ui.theme.LkTextPrimary
import com.localkarar.app.ui.theme.LkTextSecondary
import com.localkarar.app.ui.theme.LkTypography

/**
 * §7.1 kontrol kademeleri. `MD` varsayilandir.
 *
 * §7.1 kurali: "Button ve form kontrol olculeri esit adimda ilerler:
 * btn-md = control-md = 40dp." Bu yuzden degerler `LkButtonSize` ile ayni;
 * yan yana duran buton ve input arasinda yukseklik farki olusmaz.
 */
enum class LkFieldSize(val height: Dp, val horizontalPadding: Dp) {
    SM(32.dp, 10.dp),
    MD(40.dp, 12.dp),
    LG(48.dp, 14.dp)
}

@Composable
fun LkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    error: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    enabled: Boolean = true,
    trailingContent: @Composable (() -> Unit)? = null,
    /**
     * Cok satirli giris (destek mesaji, not, aciklama).
     *
     * Eklendi cunku uzun metin isteyen ekranlar kendi metin kutularini
     * yazmaya baslamisti; ayni bilesenin iki farkli gorunumu olmasi tasarim
     * sisteminin amacini bozar.
     *
     * `false` verildiginde sabit 40dp yukseklik birakiliyor ve yukseklik
     * cagiran tarafin `modifier`ina birakiliyor.
     */
    singleLine: Boolean = true,
    size: LkFieldSize = LkFieldSize.MD
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                // §4 `label` — 12sp/W600. Onceden govde stili (14sp) kullaniliyordu
                // ve etiket, icindeki degerle ayni agirlikta duruyordu.
                style = LkTypography.getLabel(),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // §7.2 kenarlik durumlari.
        //
        // 🔴 ONCEDEN ODAKSIZ KENARLIK `Color.Transparent`TI: kontrol sinirlari
        // gorunmuyordu ve input yalniz zemin ton farkiyla seciliyordu. §7.2
        // odaksiz durum icin `border-default` istiyor.
        val kenarRengi = when {
            error != null -> LkDanger
            isFocused -> LkPrimary
            else -> LkLineStrong
        }

        // §7.2 + §19: odak halkasi 3px, marka rengi %12. §19 "Visible focus"
        // hicbir componentte kaldirilamaz diyor; onceden hic yoktu.
        // Halka HER ZAMAN yer kaplar (odaksizken saydam), yoksa odaklanma
        // aninda kontrol 3px ziplardi.
        val halkaRengi = if (isFocused && error == null) {
            LkPrimary.copy(alpha = 0.12f)
        } else {
            Color.Transparent
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = (if (singleLine) Modifier.height(size.height + 6.dp) else Modifier.heightIn(min = 102.dp))
                .fillMaxWidth()
                .border(3.dp, halkaRengi, LkShapes.SM)
                .padding(3.dp)
                .background(if (enabled) LkSurfaceSunken else LkSurfaceSunken.copy(alpha = 0.5f), LkShapes.SM)
                .border(1.dp, kenarRengi, LkShapes.SM)
                .onFocusChanged { isFocused = it.isFocused },
            enabled = enabled,
            singleLine = singleLine,
            // §7.1: control-md yazi tipi `body` (14sp), `body-sm` degil.
            textStyle = LkTypography.getBody().copy(color = if (enabled) LkTextPrimary else LkTextMuted),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            cursorBrush = SolidColor(LkPrimary),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = size.horizontalPadding, vertical = if (singleLine) 0.dp else 10.dp),
                    // Cok satirlida metin USTTEN baslamali; ortalamak, uzun
                    // mesajda imleci kutunun ortasinda birakirdi.
                    verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = LkTypography.getBody().copy(color = LkTextMuted)
                            )
                        }
                        innerTextField()
                    }
                }
            }
        )

        if (error != null) {
            Text(
                text = error,
                style = LkTypography.getMetadata().copy(color = LkDanger),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

