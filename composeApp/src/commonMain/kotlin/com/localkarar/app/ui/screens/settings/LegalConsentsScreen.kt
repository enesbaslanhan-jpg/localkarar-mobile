package com.localkarar.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.openExternalUrl
import com.localkarar.app.network.ApiConfig
import com.localkarar.app.settings.SettingsViewModel
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*

@Composable
fun LegalConsentsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.loadConsents()
    }

    LkPageLayout(title = "Yasal Bilgiler ve Onaylar", onBack = onBack) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "LocalKarar platformu kullanım ve gizlilik koşulları aşağıda listelenmiştir. Yasal mevzuat gereğince güncellenen metinleri onaylayabilirsiniz.",
                style = LkTypography.getBodySmall(),
                color = LkTextSecondary
            )

            if (viewModel.consentsLoading && viewModel.legalDocuments.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LkPrimary)
                }
            } else {
                viewModel.legalDocuments.forEach { doc ->
                    val isMissing = viewModel.missingConsents.any { it.type == doc.type }
                    val acceptedItem = viewModel.acceptedConsents.find { it.documentType == doc.type }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(LkShapes.MD)
                            .border(1.dp, if (isMissing) LkWarning.copy(alpha = 0.5f) else LkLineSoft, LkShapes.MD),
                        backgroundColor = LkSurfacePanel,
                        elevation = 0.dp
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = doc.title,
                                    style = LkTypography.getBodyStrong(),
                                    color = LkTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.width(8.dp))
                                if (isMissing) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(LkWarning.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = "Onay Bekliyor",
                                            tint = LkWarning,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "Onay Bekliyor",
                                            style = LkTypography.getMicro(),
                                            color = LkWarning,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(LkSuccess.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Onaylandı",
                                            tint = LkSuccess,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "Onaylandı",
                                            style = LkTypography.getMicro(),
                                            color = LkSuccess,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Versiyon: ${doc.version}" + (acceptedItem?.acceptedAt?.let { " • Onay Tarihi: ${it.take(10)}" } ?: ""),
                                style = LkTypography.getMicro(),
                                color = LkTextSecondary
                            )

                            if (!doc.summary.isNullOrBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = doc.summary,
                                    style = LkTypography.getBodySmall(),
                                    color = LkTextPrimary
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
                             * METIN KOPYALANMIYOR, gercek sayfa aciliyor.
                             * Sebep: metinler 117 KB ve surumleriyle birlikte
                             * hareket etmek zorunda -- `privacy.js` basindaki
                             * not bunu acikca yaziyor ("once burasi
                             * guncellenir, sonra version artirilir"). Ikinci
                             * bir kopya, surum artisinda sessizce eskiyip
                             * kullaniciya YANLIS metni onaylatirdi.
                             *
                             * Adres `ApiConfig.baseUrl` uzerinden: ayni
                             * Fastify hem SPA'yi hem API'yi suniyor, yani
                             * gelistirmede 10.0.2.2:3000, uretimde
                             * localkarar.com -- ikisi de dogru sayfayi acar.
                             * Belge `type` degerleri web rotalariyla birebir
                             * ayni (terms, privacy, cookies, ...).
                             */
                            Spacer(Modifier.height(12.dp))
                            TextButton(
                                onClick = { openExternalUrl(ApiConfig.baseUrl + "/" + doc.type) },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    "Metni oku",
                                    style = LkTypography.getBodySmall(),
                                    color = LkPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                if (viewModel.missingConsents.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    LkButton(
                        text = "Güncel Metinleri Onayla",
                        onClick = { viewModel.acceptConsents() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            viewModel.notice?.let {
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = if (viewModel.noticeIsError) LkDanger.copy(alpha = 0.15f) else LkPrimary.copy(alpha = 0.15f),
                    elevation = 0.dp
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = it,
                            style = LkTypography.getBodySmall(),
                            color = if (viewModel.noticeIsError) LkDanger else LkPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.clearNotice() }) {
                            Text("Tamam", color = LkTextPrimary)
                        }
                    }
                }
            }
        }
    }
}
