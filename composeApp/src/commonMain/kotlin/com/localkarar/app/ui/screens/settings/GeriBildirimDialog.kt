package com.localkarar.app.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.components.LkPillChip
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.theme.LkPrimary
import com.localkarar.app.ui.theme.LkSurfaceRaised
import com.localkarar.app.ui.theme.LkTextMuted
import com.localkarar.app.ui.theme.LkTextPrimary
import com.localkarar.app.ui.theme.LkTextSecondary
import com.localkarar.app.ui.theme.LkTypography

/**
 * KISA GERİ BİLDİRİM (29.09.2026).
 *
 * "Destek" formu ad + e-posta + konu + en az 20 karakter istiyor; giriş
 * yapmış biri için "iki cümle yazıp geçeyim" anını öldüren bir form. Burada
 * kimlik zaten belli: bir kategori dokun, kısa bir şey yaz, gönder. Sunucu
 * (POST /support/feedback) işletmeciye e-postayla iletir; mesaj saklanmaz.
 *
 * Hesap ve sürüm sunucuya jetonla / [surum] ile gider; kullanıcıya soru
 * sorulmaz.
 */
private val KATEGORILER = listOf(
    "oneri" to "Öneri",
    "sorun" to "Sorun",
    "begeni" to "Beğendim"
)

/** En az karakter: sunucu şemasıyla aynı (10). */
private const val EN_AZ = 10

@Composable
fun GeriBildirimDialog(
    onVazgec: () -> Unit,
    onGonder: (kategori: String, mesaj: String, sonuc: (Boolean) -> Unit) -> Unit
) {
    var kategori by remember { mutableStateOf("oneri") }
    var mesaj by remember { mutableStateOf("") }
    var gonderiliyor by remember { mutableStateOf(false) }
    val yeterli = mesaj.trim().length >= EN_AZ

    AlertDialog(
        onDismissRequest = { if (!gonderiliyor) onVazgec() },
        backgroundColor = LkSurfaceRaised,
        title = {
            Text("Geri bildirim", style = LkTypography.getSectionTitle(), color = LkTextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Neyi beğendiniz, neyi değiştirelim? Kısa yazmanız yeterli.",
                    style = LkTypography.getBodySmall(),
                    color = LkTextSecondary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KATEGORILER.forEach { (kod, etiket) ->
                        LkPillChip(label = etiket, selected = kategori == kod, onClick = { kategori = kod })
                    }
                }
                LkTextField(
                    value = mesaj,
                    onValueChange = { if (it.length <= 2000) mesaj = it },
                    placeholder = "Buraya yazın…",
                    singleLine = false,
                    otoBuyuyen = true,
                    enFazlaSatir = 6,
                    enabled = !gonderiliyor,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    if (yeterli) "Hazır." else "En az $EN_AZ karakter (${mesaj.trim().length}/$EN_AZ)",
                    style = LkTypography.getMicro(),
                    color = LkTextMuted
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = yeterli && !gonderiliyor,
                onClick = {
                    gonderiliyor = true
                    onGonder(kategori, mesaj.trim()) { basarili ->
                        gonderiliyor = false
                        if (basarili) onVazgec()
                    }
                }
            ) {
                Text(if (gonderiliyor) "Gönderiliyor…" else "Gönder", color = LkPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(enabled = !gonderiliyor, onClick = onVazgec) {
                Text("Vazgeç", color = LkTextSecondary)
            }
        }
    )
}
