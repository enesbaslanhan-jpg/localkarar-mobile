package com.localkarar.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.SecureScreen
import com.localkarar.app.settings.SettingsViewModel
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkHeroPage
import com.localkarar.app.ui.components.LkPasswordTextField
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.theme.*

/**
 * Mockup "Ayar 9 — Hesabı sil".
 *
 * 🔴 UC SAPMA DUZELTILDI:
 *
 * 1. NE SILINECEGI SAYILMIYORDU. Tek bir kirmizi paragrafta "tüm ilişkili
 *    verileriniz" yaziyordu. Mockup bunlari TEK TEK sayiyor — kullanici
 *    neyi kaybettigini geri alinamaz bir islemden ONCE gormeli.
 * 2. Ham `OutlinedTextField` ve Material `Card` kullaniyordu; ekran
 *    uygulamanin alan ve yuzey dilinden ayri duruyordu.
 * 3. Yikici dugme diger alanlarla ayni bloktaydi; artik bosluk ve
 *    ayrimla en altta duruyor.
 *
 * ⚠️ SAYILAR UYDURULMADI. Mockup'ta "3 işletme ve 54 kayıt" gibi rakamlar
 * var; sunucuda hesabin silecegi kayitlari onceden sayan bir uc YOK.
 * Uydurma rakam, geri alinamaz bir onay ekraninda en kotu yerde yalan
 * soylemek olurdu — kalemler rakamsiz sayiliyor.
 */
@Composable
fun DeleteAccountScreen(
    viewModel: SettingsViewModel,
    onDeleted: () -> Unit,
    onBack: () -> Unit
) {
    // Kimlik bilgisi girilen ekran: ekran goruntusu ve son-uygulamalar
    // kucuk resmi engelleniyor. Gerekce SecureScreen belgesinde.
    SecureScreen()

    LkHeroPage(title = "Hesabı sil", onBack = onBack) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(LkSpacing.Space4),
            verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
        ) {
            Text(
                text = "Bu işlem geri alınamaz.",
                style = LkTypography.getCardTitle(),
                color = LkDanger
            )

            Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                Text(
                    text = "SİLİNECEKLER",
                    style = LkTypography.getMicro(),
                    color = LkTextSecondary
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LkDanger.copy(alpha = 0.08f), LkShapes.Card)
                        .padding(LkSpacing.Space4),
                    verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
                ) {
                    SilinecekSatir("İşletmeleriniz ve içindeki bütün kayıtlar")
                    SilinecekSatir("Ürünler, siparişler ve kişi listeleri")
                    SilinecekSatir("Yüklediğiniz bütün belgeler")
                    SilinecekSatir("Topluluk gönderileriniz, yorumlar ve takip bağlantıları")
                    SilinecekSatir("Hesaplama geçmişi ve karar oturumları")
                }
            }

            LkPasswordTextField(
                value = viewModel.deletePassword,
                onValueChange = { viewModel.onDeletePasswordChange(it) },
                label = "Mevcut parola"
            )

            LkTextField(
                value = viewModel.deleteConfirmation,
                onValueChange = { viewModel.onDeleteConfirmationChange(it) },
                label = "Onaylamak için HESABIMI SİL yazın",
                placeholder = "HESABIMI SİL"
            )

            viewModel.notice?.let {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (viewModel.noticeIsError) LkDanger.copy(alpha = 0.12f)
                            else LkSuccess.copy(alpha = 0.12f),
                            LkShapes.MD
                        )
                        .padding(horizontal = LkSpacing.Space3, vertical = LkSpacing.Space2),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = it,
                        style = LkTypography.getBodySmall(),
                        color = if (viewModel.noticeIsError) LkDanger else LkSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { viewModel.clearNotice() }) {
                        Text("Tamam", color = LkTextPrimary)
                    }
                }
            }

            Spacer(Modifier.height(LkSpacing.Space6))

            LkButton(
                text = if (viewModel.deleteLoading) "Siliniyor..." else "Hesabı kalıcı olarak sil",
                variant = LkButtonVariant.DANGER,
                onClick = { viewModel.deleteAccount(onDeleted) },
                enabled = !viewModel.deleteLoading,
                modifier = Modifier.fillMaxWidth()
            )

            LkButton(
                text = "Vazgeç",
                variant = LkButtonVariant.SECONDARY,
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(LkSpacing.Space6))
        }
    }
}

/** Silinecek kalem. Nokta ISARETI degil kucuk daire — liste isareti degil, tek tek sayim. */
@Composable
private fun SilinecekSatir(metin: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = Icons.Outlined.Circle,
            contentDescription = null,
            tint = LkDanger,
            modifier = Modifier.padding(top = 6.dp).size(8.dp)
        )
        Spacer(Modifier.width(LkSpacing.Space3))
        Text(
            text = metin,
            style = LkTypography.getBodySmall(),
            color = LkTextPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}
