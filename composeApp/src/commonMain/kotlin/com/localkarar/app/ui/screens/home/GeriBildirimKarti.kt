package com.localkarar.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.LocalAppPreferences
import com.localkarar.app.core.PrefKeys
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkCard
import com.localkarar.app.ui.screens.settings.GeriBildirimDialog
import com.localkarar.app.ui.theme.LkSpacing
import com.localkarar.app.ui.theme.LkTextPrimary
import com.localkarar.app.ui.theme.LkTextSecondary
import com.localkarar.app.ui.theme.LkTypography
import kotlinx.datetime.Clock

/**
 * "Görüşünüzü paylaşır mısınız?" KARTI — tek seferlik (29.09.2026).
 *
 * Telefon bildirimi yerine bilerek KART: bildirim izni gerektirmez,
 * kullanıcıyı sistem düzeyinde rahatsız etmez; kullanıcı uygulamayı zaten
 * açmışken, yeterince denedikten sonra sorulur.
 *
 * Kurallar:
 *  - İlk açılış tarihi cihazda saklanır; kart [GUN_ESIGI] gün SONRA görünür
 *    (ilk günde "nasıl buldun" demek erken).
 *  - BİR KEZ: gönderildi de, "Sonra" da dendi de bir daha çıkmaz. Ayarlar →
 *    Geri bildirim gönder her zaman açıktır.
 *  - Ekran, sunucuya giden [onGonder] geri çağrısını dışarıdan alır (Ayarlar
 *    ile aynı yol: POST /support/feedback).
 */
private const val GUN_ESIGI = 3
private const val GUN_MS = 24L * 60 * 60 * 1000

@Composable
fun GeriBildirimKarti(
    onGonder: (kategori: String, mesaj: String, sonuc: (Boolean) -> Unit) -> Unit
) {
    val tercihler = LocalAppPreferences.current ?: return
    var gorunur by remember { mutableStateOf(false) }
    var dialogAcik by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val simdi = Clock.System.now().toEpochMilliseconds()
        val ilk = tercihler.getString(PrefKeys.ILK_ACILIS)?.toLongOrNull()
        if (ilk == null) {
            tercihler.putString(PrefKeys.ILK_ACILIS, simdi.toString())
        } else if (simdi - ilk >= GUN_ESIGI * GUN_MS && tercihler.getString(PrefKeys.GERIBILDIRIM_KARTI) == null) {
            gorunur = true
        }
    }

    fun kapat() {
        tercihler.putString(PrefKeys.GERIBILDIRIM_KARTI, "1")
        gorunur = false
    }

    if (gorunur) {
        LkCard(padding = LkSpacing.Space4) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Uygulamayı nasıl buldunuz?", style = LkTypography.getBodyStrong(), color = LkTextPrimary)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Bir öneriniz ya da karşılaştığınız bir sorun varsa iki cümleyle yazın; sonraki sürümleri buna göre şekillendiriyoruz.",
                    style = LkTypography.getBodySmall(),
                    color = LkTextSecondary
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LkButton(text = "Görüş bildir", onClick = { dialogAcik = true })
                    LkButton(text = "Sonra", variant = LkButtonVariant.GHOST, onClick = { kapat() })
                }
            }
        }
    }

    if (dialogAcik) {
        GeriBildirimDialog(
            onVazgec = { dialogAcik = false },
            onGonder = { kategori, mesaj, sonuc ->
                onGonder(kategori, mesaj) { basarili ->
                    if (basarili) kapat()
                    sonuc(basarili)
                }
            }
        )
    }
}
