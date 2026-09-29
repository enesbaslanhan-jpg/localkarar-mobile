package com.localkarar.app.ui.shell

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.localkarar.app.ui.theme.LkPrimary
import com.localkarar.app.ui.theme.LkSurfaceRaised
import com.localkarar.app.ui.theme.LkTextPrimary
import com.localkarar.app.ui.theme.LkTextSecondary
import com.localkarar.app.ui.theme.LkTypography

/**
 * Telefon bildirimi için kendi açıklamamız (29.09.2026).
 *
 * iOS'un izin penceresi uygulama başına BİR KEZ çıkar ve reddedilirse geri
 * alınamaz (yalnız iOS Ayarları'ndan). O tek atışı, kullanıcı NEDEN
 * istediğimizi okuduktan sonra harcıyoruz. Vaat edilenler uygulamada
 * gerçekten var: vade hatırlatıcıları, düşük stok, geciken kargo, topluluk
 * yanıtı ve mesajı. Pazarlama bildirimi YOK; söz verilmiyor.
 */
@Composable
fun PushIzinSorusu(onAc: () -> Unit, onSonra: () -> Unit) {
    AlertDialog(
        onDismissRequest = onSonra,
        backgroundColor = LkSurfaceRaised,
        title = { Text("Telefonunuza haber verelim mi?", style = LkTypography.getSectionTitle(), color = LkTextPrimary) },
        text = {
            Text(
                "Vadesi gelen kayıtlar, düşük stok, geciken kargo ve topluluktaki yanıt ya da mesajlar için bildirim gönderelim. " +
                    "Reklam ya da duyuru göndermeyiz. İstediğiniz zaman iOS Ayarları'ndan kapatabilirsiniz.",
                style = LkTypography.getBodySmall(),
                color = LkTextSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onAc) { Text("Bildirimleri aç", color = LkPrimary, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onSonra) { Text("Şimdi değil", color = LkTextSecondary) }
        }
    )
}
