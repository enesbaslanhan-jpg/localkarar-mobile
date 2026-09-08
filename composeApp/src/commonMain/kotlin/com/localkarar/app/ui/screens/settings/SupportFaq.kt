package com.localkarar.app.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.components.LkHairline
import com.localkarar.app.ui.components.LkRowGroup
import com.localkarar.app.ui.theme.LkSpacing
import com.localkarar.app.ui.theme.LkTextMuted
import com.localkarar.app.ui.theme.LkTextPrimary
import com.localkarar.app.ui.theme.LkTextSecondary
import com.localkarar.app.ui.theme.LkTypography

/**
 * Sik sorulanlar — mockup "Ayar 7"nin ilk bolumu.
 *
 * ⚠️ METINLER UYDURULMADI. Sekizi de webin kendi cevirisinden birebir
 * alindi: `frontend/src/i18n/locales/tr/common.json` → `support.faq`.
 * Ayni soruya iki platformda iki farkli cevap vermek, kullaniciya
 * uygulamanin ne yaptigini yanlis ogretir.
 *
 * 🔴 MOBILDE SIK SORULANLAR HIC YOKTU: Destek ekrani dogrudan forma
 * aciliyordu. Mockup'in sirasi "once cevap, sonra iletisim" — cogu soru
 * yanitini burada bulur ve destek talebi hic acilmaz.
 */
internal val SUPPORT_FAQ: List<Pair<String, String>> = listOf(
    "E-posta doğrulama postası gelmedi, ne yapmalıyım?" to
        "Önce spam ve tanıtım klasörlerine bakın. Kod 15 dakika geçerlidir; " +
        "süresi geçtiyse uygulamadaki şeritten yeni kod isteyebilirsiniz. " +
        "Adresinizi yanlış yazdıysanız Ayarlar sayfasından değiştirebilirsiniz.",

    "Şifremi unuttum." to
        "Giriş ekranındaki “Şifremi unuttum” bağlantısını kullanın. Bağlantı 1 saat " +
        "ve yalnızca bir kez geçerlidir. Şifre sıfırlama yalnızca DOĞRULANMIŞ e-posta " +
        "adreslerine gönderilir; adresinizi henüz doğrulamadıysanız posta gelmez.",

    "Her açtığımda yeniden giriş yapmam gerekiyor." to
        "Oturumunuz 30 gün açık kalır. Bu süreden önce kopuyorsa, siteye bazen “www” " +
        "ile bazen “www”suz girmiş olabilirsiniz — tarayıcı bunları iki ayrı site sayar. " +
        "Adres artık tek biçime yönlendiriliyor; sorun sürerse bize yazın.",

    "İşletme çalışma alanı ne işe yarar?" to
        "Gelir, gider, cari hesap ve belgelerinizi tuttuğunuz alandır. Bir işletmeye " +
        "başka kişileri davet edebilirsiniz; davet, e-posta ile gönderilen tek " +
        "kullanımlık bir bağlantı üzerinden kabul edilir.",

    "Yüklediğim faturadan kayıt nasıl oluşuyor?" to
        "Belgedeki metin okunur ve size bir kayıt ÖNERİSİ sunulur. Siz onaylamadan " +
        "hiçbir şey işletme kayıtlarınıza yazılmaz. Öneriyi düzenleyebilir veya " +
        "reddedebilirsiniz.",

    "AI Mentor’un verdiği bilgiye güvenebilir miyim?" to
        "Mentor bir dil modeli kullanır ve kendinden emin görünen hatalı bilgi " +
        "üretebilir. Yanıtı mümkün olduğunda kendi içerik kütüphanemize dayandırır ve " +
        "kaynağı gösterir. Rakam, oran, süre ve mevzuat içeren bilgileri işlem yapmadan " +
        "önce resmî kaynağından doğrulayın. Mentor mali müşavir veya avukat yerine geçmez.",

    "Verilerim nerede tutuluyor?" to
        "Sunucularımız Fransa’dadır. Mentor yazışmaları Mistral AI (Fransa) tarafından " +
        "işlenir ve kötüye kullanım denetimi için 30 gün saklanır; model eğitiminde " +
        "kullanılmaz. Ayrıntılı döküm Gizlilik ve KVKK Aydınlatma Metni’ndedir.",

    "Hesabımı nasıl silerim?" to
        "Ayarlar → Hesabı sil. Tek sahibi olduğunuz bir işletme varsa önce başka bir " +
        "üyeyi sahip yapmanız gerekir. Silme sonrası verilerinize ne olduğu aydınlatma " +
        "metninde yazılıdır."
)

/** Acilip kapanan soru satiri. Dokunma hedefi tum satir. */
@Composable
internal fun SupportFaqList(modifier: Modifier = Modifier) {
    LkRowGroup(modifier) {
        SUPPORT_FAQ.forEachIndexed { index, (soru, cevap) ->
            var acik by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { acik = !acik }
                    .padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space3)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = soru,
                        style = LkTypography.getBodyStrong(),
                        color = LkTextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (acik) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        tint = LkTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
                AnimatedVisibility(visible = acik) {
                    Column {
                        Spacer(Modifier.height(LkSpacing.Space2))
                        Text(
                            text = cevap,
                            style = LkTypography.getBodySmall(),
                            color = LkTextSecondary
                        )
                    }
                }
            }

            if (index != SUPPORT_FAQ.lastIndex) LkHairline()
        }
    }
}
