package com.localkarar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.localkarar.app.core.openExternalUrl
import com.localkarar.app.network.ApiConfig
import com.localkarar.app.ui.theme.LkPrimary
import com.localkarar.app.ui.theme.LkShapes
import com.localkarar.app.ui.theme.LkSpacing
import com.localkarar.app.ui.theme.LkSurfacePanel
import com.localkarar.app.ui.theme.LkSurfaceRaised
import com.localkarar.app.ui.theme.LkTextMuted
import com.localkarar.app.ui.theme.LkTextPrimary
import com.localkarar.app.ui.theme.LkTextSecondary
import com.localkarar.app.ui.theme.LkTypography

/*
 * YASAL ONAY PENCERESI (urun sahibi, 18.09.2026: "metinler otomatik acilmiyor,
 * acilip onaylatmasi lazim").
 *
 * Onceden formda kucuk bir onay kutusu ve alti cizili iki baglanti vardi;
 * kullanici kutuyu isaretleyip geciyordu, metinler hic acilmiyordu. Simdi
 * "Hesabi olustur" (ya da Google/Apple ile yeni hesap) ONCE bu pencereyi
 * acar: iki belge satir olarak listelenir, dokununca tarayicida acilir,
 * onay ancak bu pencereden verilir. Metinler webdeki tek kaynaktan
 * (/terms, /privacy) okunur; ikinci bir kopya tutulmaz.
 */
@Composable
fun LkYasalOnayPenceresi(
    baslik: String = "Devam etmeden önce",
    onaylaMetni: String = "Okudum, onaylıyorum",
    yukleniyor: Boolean = false,
    onOnayla: () -> Unit,
    onVazgec: () -> Unit
) {
    Dialog(onDismissRequest = { if (!yukleniyor) onVazgec() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(LkShapes.LG)
                .background(LkSurfacePanel)
                .padding(LkSpacing.Space5),
            verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
        ) {
            Text(baslik, style = LkTypography.getSectionTitle(), color = LkTextPrimary)
            Text(
                "Hesap açmak için aşağıdaki iki metni okuyup onaylaman gerekiyor. Dokununca tarayıcıda açılır.",
                style = LkTypography.getBodySmall(),
                color = LkTextSecondary
            )
            BelgeSatiri("Kullanım Koşulları", "Üyelik, ücretlendirme ve kullanım kuralları", "/terms")
            BelgeSatiri("Aydınlatma Metni (KVKK)", "Hangi veriler işlenir, ne kadar saklanır", "/privacy")
            Spacer(Modifier.height(LkSpacing.Space2))
            LkButton(
                text = if (yukleniyor) "Hesap açılıyor…" else onaylaMetni,
                onClick = onOnayla,
                enabled = !yukleniyor,
                modifier = Modifier.fillMaxWidth(),
                size = LkButtonSize.LG,
                shape = LkShapes.FULL
            )
            TextButton(onClick = onVazgec, enabled = !yukleniyor, modifier = Modifier.fillMaxWidth()) {
                Text("Vazgeç", color = LkTextSecondary)
            }
        }
    }
}

@Composable
private fun BelgeSatiri(baslik: String, aciklama: String, yol: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LkShapes.MD)
            .background(LkSurfaceRaised)
            .clickable { openExternalUrl(ApiConfig.baseUrl + yol) }
            .padding(horizontal = LkSpacing.Space3, vertical = LkSpacing.Space3),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Description, contentDescription = null, tint = LkPrimary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(LkSpacing.Space3))
        Column(Modifier.weight(1f)) {
            Text(baslik, style = LkTypography.getBodyStrong(), color = LkTextPrimary)
            Text(aciklama, style = LkTypography.getMicro(), color = LkTextMuted)
        }
        Icon(Icons.Outlined.OpenInNew, contentDescription = "Aç", tint = LkTextMuted, modifier = Modifier.size(16.dp))
    }
}
