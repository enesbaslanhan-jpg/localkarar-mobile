package com.localkarar.app.ui.screens.workspaces

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.localkarar.app.core.rememberFilePicker
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.ImportUiState
import com.localkarar.app.workspaces.RecordImportViewModel

/**
 * TOPLU KAYIT ICE AKTARMA PANELI.
 *
 * Webdeki uc adimli sihirbazin (`ImportDialog.jsx`) mobil karsiligi:
 * dosya sec -> ne anlasildigini gor -> onayla.
 *
 * ⚠️ SUTUN ESLESTIRME IZGARASI YOK, BILEREK: telefonda 11 alani elle
 * eslestirmek yapilabilir bir is degil. Eslestirme otomatik, ama
 * SONUCU kullaniciya yaziliyor -- dosyasinin yanlis okundugunu gorup
 * vazgecebilsin diye. Sessiz otomatik eslestirme, kullanicinin yanlis
 * sutundan tutar aktardigini ancak kayitlar olustuktan sonra fark
 * etmesi demekti.
 */
@Composable
fun RecordImportPanel(
    viewModel: RecordImportViewModel,
    onKapat: () -> Unit,
    onBitti: () -> Unit
) {
    val durum by viewModel.uiState.collectAsState()

    val dosyaSec = rememberFilePicker { secilen ->
        if (secilen != null) viewModel.dosyaSecildi(secilen.name, secilen.bytes)
    }

    Dialog(
        onDismissRequest = onKapat,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(LkShapes.LG)
                    .background(LkSurfaceElevated)
                    .padding(LkSpacing.Space5)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
            ) {
                Text(
                    text = "Kayıtları içe aktar",
                    style = LkTypography.getSectionTitle(),
                    color = LkTextPrimary
                )

                when (val d = durum) {
                    is ImportUiState.DosyaBekleniyor -> {
                        Text(
                            text = "CSV dosyanızın ilk satırı sütun başlıkları olmalı. " +
                                "\"Başlık\", \"Tutar\", \"Vade\", \"Tür\" gibi adlar " +
                                "otomatik tanınır.",
                            style = LkTypography.getMetadata(),
                            color = LkTextSecondary
                        )
                        LkButton(
                            text = "CSV dosyası seç",
                            onClick = dosyaSec,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    is ImportUiState.Yukleniyor -> {
                        Text(
                            text = "Dosya okunuyor...",
                            style = LkTypography.getBody(),
                            color = LkTextSecondary
                        )
                    }

                    is ImportUiState.Onizleme -> {
                        Text(
                            text = d.dosyaAdi,
                            style = LkTypography.getBody(),
                            color = LkTextPrimary
                        )
                        Text(
                            text = "${d.sonuc.totalRows} satır okundu · " +
                                "${d.sonuc.validRows} satır aktarılabilir",
                            style = LkTypography.getMetadata(),
                            color = LkTextSecondary
                        )

                        /*
                         * TANINAN SUTUNLAR YAZILIYOR. Kullanici hangi
                         * sutunun hangi alana gittigini gormeden onay
                         * verirse, yanlis sutundan tutar aktardigini
                         * ancak kayitlar olustuktan sonra anlar.
                         */
                        Spacer(Modifier.height(LkSpacing.Space1))
                        Text(
                            text = "TANINAN SÜTUNLAR",
                            style = LkTypography.getMicro(),
                            color = LkTextMuted
                        )
                        d.eslesme.forEach { (alan, sutun) ->
                            Text(
                                text = "${alanEtiketi(alan)}  ←  $sutun",
                                style = LkTypography.getMetadata(),
                                color = LkTextSecondary
                            )
                        }

                        /*
                         * Hatalar SATIR NUMARASIYLA: kullanici dosyada
                         * hangi satiri duzeltecegini bilmeli.
                         *
                         * 🔴 ALAN BASINA TEK MESAJ. Sunucu ayni alan icin
                         * IKI hata donduruyor: once kendi Turkce mesaji
                         * ("Geçersiz sayı formatı"), sonra dogrulama
                         * kutuphanesinin ham Ingilizcesi ("Expected
                         * number, received string"). Olculdu (07.09.2026,
                         * emulator): ekranda dort satir cikiyordu ve
                         * ikisi kullaniciya hicbir sey anlatmayan
                         * kutuphane ciktisiydi.
                         *
                         * Ilki tutuluyor cunku sunucunun KENDI yazdigi,
                         * kullanici icin yazilmis mesaj o. Ham mesaji
                         * metne bakarak elemek (Ingilizce mi degil mi)
                         * kirilgan olurdu; "alan basina tek mesaj" hem
                         * saglam hem zaten dogru olan kural.
                         */
                        val benzersizHatalar = d.sonuc.errors
                            .distinctBy { it.row to it.field }

                        if (benzersizHatalar.isNotEmpty()) {
                            Spacer(Modifier.height(LkSpacing.Space1))
                            Text(
                                text = "AKTARILAMAYAN SATIRLAR",
                                style = LkTypography.getMicro(),
                                color = LkTextMuted
                            )
                            benzersizHatalar.take(5).forEach { hata ->
                                Text(
                                    /* Alan adi da CEVIRILIYOR: "amount"
                                       yerine "Tutar". Sunucunun alan
                                       kodlari kullanicinin dosyasindaki
                                       sutun adlariyla ayni degil. */
                                    text = listOfNotNull(
                                        "${hata.row}. satır",
                                        hata.field?.let { alanEtiketi(it) },
                                        hata.message
                                    ).joinToString(" · "),
                                    style = LkTypography.getMetadata(),
                                    color = LkWarning
                                )
                            }
                            if (benzersizHatalar.size > 5) {
                                Text(
                                    text = "ve ${benzersizHatalar.size - 5} satır daha",
                                    style = LkTypography.getMicro(),
                                    color = LkTextMuted
                                )
                            }
                        }

                        Spacer(Modifier.height(LkSpacing.Space2))
                        LkButton(
                            text = "${d.sonuc.validRows} kaydı oluştur",
                            onClick = { viewModel.onayla() },
                            enabled = d.sonuc.validRows > 0,
                            modifier = Modifier.fillMaxWidth()
                        )
                        LkButton(
                            text = "Başka dosya seç",
                            variant = LkButtonVariant.SECONDARY,
                            onClick = { viewModel.bastanBasla() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    is ImportUiState.Yaziliyor -> {
                        Text(
                            text = "Kayıtlar oluşturuluyor...",
                            style = LkTypography.getBody(),
                            color = LkTextSecondary
                        )
                    }

                    is ImportUiState.Bitti -> {
                        Text(
                            text = "${d.eklenen} kayıt oluşturuldu.",
                            style = LkTypography.getBody(),
                            color = LkTextPrimary
                        )
                        LkButton(
                            text = "Kayıtlara dön",
                            onClick = onBitti,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    is ImportUiState.Hata -> {
                        Text(
                            text = d.mesaj,
                            style = LkTypography.getBody(),
                            color = LkTextSecondary
                        )
                        LkButton(
                            text = "Başka dosya seç",
                            onClick = { viewModel.bastanBasla() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                LkButton(
                    text = "Kapat",
                    variant = LkButtonVariant.QUIET,
                    onClick = onKapat,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/** Sunucunun alan adlari kullaniciya HAM gosterilmiyor. */
private fun alanEtiketi(alan: String): String = when (alan) {
    "type" -> "Tür"
    "title" -> "Başlık"
    "description" -> "Açıklama"
    "direction" -> "Yön"
    "amount" -> "Tutar"
    "currency" -> "Para birimi"
    "priority" -> "Öncelik"
    "dueAt" -> "Vade"
    "contactId" -> "Kişi"
    "assignedToId" -> "Sorumlu"
    "recurrenceRule" -> "Tekrar"
    else -> alan
}
