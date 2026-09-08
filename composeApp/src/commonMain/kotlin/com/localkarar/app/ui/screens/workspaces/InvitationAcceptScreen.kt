package com.localkarar.app.ui.screens.workspaces

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkHeroPage
import com.localkarar.app.ui.components.LkInfoPanel
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.InvitationAcceptViewModel
import com.localkarar.app.workspaces.InvitationUiState

/**
 * EKIP DAVETINI KABUL ETME — `/davet?token=` baglantisinin indigi ekran.
 *
 * 🔴 MOBILDE HIC YOKTU. Ekip ekrani davet gonderiyor, sunucu e-postayi
 * atiyor, ama davetli kisi baglantiyi telefonunda actiginda uygulama
 * devreye girmiyordu — baglanti tarayiciya dusuyordu.
 *
 * ⚠️ DAVETIN ICERIGI GOSTERILMIYOR, CUNKU SUNUCU VERMIYOR. Hangi
 * isletme, kim davet etti, hangi rol -- bunlarin hicbiri kabul
 * ONCESINDE okunabilecek bir uctan gelmiyor (jeton yalnizca
 * `/invitations/accept` tarafindan cozuluyor). Isletme adini tahmin
 * edip yazmak, dogrulanmamis bir bilgiyi kullaniciya gercek gibi
 * sunmak olurdu; onun yerine ne olacagi ACIKCA anlatiliyor.
 */
@Composable
fun InvitationAcceptScreen(
    viewModel: InvitationAcceptViewModel,
    onOpenWorkspace: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LkHeroPage(title = "Ekip daveti", onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(LkSpacing.Space4),
            verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
        ) {
            when (val durum = uiState) {
                is InvitationUiState.Hazir, is InvitationUiState.Gonderiliyor -> {
                    LkInfoPanel(title = "Bir işletmeye davet edildiniz") {
                        Text(
                            text = "Kabul ettiğinizde bu işletmenin kayıtlarını, kişilerini " +
                                "ve belgelerini görebilirsiniz. Yetkiniz sizi davet eden " +
                                "kişinin verdiği role göre belirlenir.",
                            style = LkTypography.getBody(),
                            color = LkTextSecondary
                        )
                    }

                    val gonderiliyor = durum is InvitationUiState.Gonderiliyor
                    LkButton(
                        text = if (gonderiliyor) "Kabul ediliyor..." else "Daveti kabul et",
                        onClick = { viewModel.kabulEt() },
                        enabled = !gonderiliyor,
                        modifier = Modifier.fillMaxWidth()
                    )
                    LkButton(
                        text = "Şimdi değil",
                        variant = LkButtonVariant.SECONDARY,
                        onClick = onBack,
                        enabled = !gonderiliyor,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                is InvitationUiState.Basarili -> {
                    LkInfoPanel(title = "Davet kabul edildi") {
                        Text(
                            text = "Artık bu işletmenin ekibindesiniz.",
                            style = LkTypography.getBody(),
                            color = LkTextSecondary
                        )
                    }
                    /*
                     * Isletmeye gecis yalniz sunucu kimligi dondurduyse
                     * sunuluyor. Alan bossa dugme cizilmiyor -- nereye
                     * gidecegini bilmeyen bir dugme koymak yerine.
                     */
                    durum.workspaceId?.let { id ->
                        LkButton(
                            text = "İşletmeyi aç",
                            onClick = { onOpenWorkspace(id) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                is InvitationUiState.Hata -> {
                    LkInfoPanel(title = "Davet kabul edilemedi") {
                        /*
                         * Sunucunun mesaji gosteriliyor: "davet suresi
                         * doldu", "bu davet baska bir e-posta adresine
                         * gonderilmis" gibi ayrimlari yalniz sunucu
                         * biliyor ve kullanicinin ne yapacagi buna bagli.
                         */
                        Text(
                            text = durum.mesaj,
                            style = LkTypography.getBody(),
                            color = LkTextSecondary,
                            textAlign = TextAlign.Start
                        )
                    }
                    LkButton(
                        text = "Tekrar dene",
                        onClick = { viewModel.kabulEt() },
                        modifier = Modifier.fillMaxWidth()
                    )
                    LkButton(
                        text = "Kapat",
                        variant = LkButtonVariant.SECONDARY,
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
