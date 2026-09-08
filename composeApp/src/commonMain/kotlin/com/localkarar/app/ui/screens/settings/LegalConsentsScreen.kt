package com.localkarar.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.openExternalUrl
import com.localkarar.app.network.ApiConfig
import com.localkarar.app.settings.SettingsViewModel
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkHairline
import com.localkarar.app.ui.components.LkHeroPage
import com.localkarar.app.ui.components.LkLoadingDesen
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkNotice
import com.localkarar.app.ui.components.LkRowGroup
import com.localkarar.app.ui.theme.*

/**
 * Mockup "Ayar 6 — Yasal izinler".
 *
 * 🔴 KART YIGINIYDI: her belge kendi kenarlikli kutusundaydi. Mockup'ta
 * izinler TEK yuzeyde gruplanmis satirlar; ayarlar alaninin tamami boyle.
 *
 * ZORUNLU olan izin kapatilamaz ve NEDENI yaziyor; onay tarihi gorunur.
 * Durum yalniz renge yaslanmiyor — ikon ve kelime birlikte (§19).
 */
@Composable
fun LegalConsentsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.loadConsents()
    }

    LkHeroPage(title = "Yasal izinler", onBack = onBack) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(LkSpacing.Space4),
            verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
        ) {
            Text(
                "Kullanım ve gizlilik koşulları aşağıda. Mevzuat gereği güncellenen metinleri buradan onaylayabilirsin.",
                style = LkTypography.getBodySmall(),
                color = LkTextSecondary
            )

            if (viewModel.consentsLoading && viewModel.legalDocuments.isEmpty()) {
                LkLoadingState(desen = LkLoadingDesen.LISTE)
            } else {
                LkRowGroup {
                    viewModel.legalDocuments.forEachIndexed { index, doc ->
                        val eksik = viewModel.missingConsents.any { it.type == doc.type }
                        val onay = viewModel.acceptedConsents.find { it.documentType == doc.type }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { openExternalUrl(ApiConfig.baseUrl + "/" + doc.type) }
                                .padding(LkSpacing.Space4),
                            verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = doc.title,
                                    style = LkTypography.getBodyStrong(),
                                    color = LkTextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                DurumRozeti(eksik)
                            }

                            /*
                             * "Zorunlu" etiketi: bu izinler kapatilamaz.
                             * Sebebi de yaziyor — kapatilamayan bir secenegi
                             * gerekcesiz gostermek kullaniciyi acikta birakir.
                             */
                            Text(
                                text = buildString {
                                    append("Sürüm ${doc.version}")
                                    onay?.acceptedAt?.let { append(" · Onay: ${it.take(10)}") }
                                    append(" · zorunlu, kapatılamaz")
                                },
                                style = LkTypography.getMetadata(),
                                color = LkTextMuted
                            )

                            if (!doc.summary.isNullOrBlank()) {
                                Text(
                                    text = doc.summary,
                                    style = LkTypography.getBodySmall(),
                                    color = LkTextSecondary
                                )
                            }

                            /*
                             * 🔴 METNI OKUMANIN HICBIR YOLU YOKTU.
                             *
                             * Ekran belgeleri listeliyor, surumlerini
                             * gosteriyor ve ONAY ALIYOR -- ama kullanici
                             * onayladigi metni uygulamada hicbir yerde
                             * goremiyordu. Parity eksiginden once bir uyum
                             * sorunu: okunamayan bir metne onay aliniyordu.
                             *
                             * METIN KOPYALANMIYOR, gercek sayfa aciliyor:
                             * metinler 117 KB ve surumleriyle birlikte
                             * hareket etmek zorunda. Ikinci bir kopya, surum
                             * artisinda sessizce eskiyip kullaniciya YANLIS
                             * metni onaylatirdi.
                             */
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Metni oku",
                                    style = LkTypography.getLabel(),
                                    color = LkPrimary
                                )
                                Spacer(Modifier.width(LkSpacing.Space1))
                                Icon(
                                    Icons.Outlined.OpenInNew,
                                    contentDescription = null,
                                    tint = LkPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        if (index != viewModel.legalDocuments.lastIndex) LkHairline()
                    }
                }

                if (viewModel.missingConsents.isNotEmpty()) {
                    LkButton(
                        text = "Güncel metinleri onayla",
                        onClick = { viewModel.acceptConsents() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            viewModel.notice?.let {
                LkNotice(
                    metin = it,
                    hataMi = viewModel.noticeIsError,
                    onKapat = { viewModel.clearNotice() }
                )
            }
        }
    }
}

/** Onay durumu: ikon + kelime. Tek basina renk yeterli degil (§19). */
@Composable
private fun DurumRozeti(eksik: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(LkShapes.FULL)
            .background((if (eksik) LkWarning else LkSuccess).copy(alpha = 0.15f))
            .padding(horizontal = LkSpacing.Space2, vertical = LkSpacing.Space1)
    ) {
        Icon(
            imageVector = if (eksik) Icons.Outlined.Warning else Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = if (eksik) LkWarning else LkSuccess,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(LkSpacing.Space1))
        Text(
            text = if (eksik) "Onay bekliyor" else "Onaylandı",
            style = LkTypography.getMicro(),
            color = if (eksik) LkWarning else LkSuccess
        )
    }
}
