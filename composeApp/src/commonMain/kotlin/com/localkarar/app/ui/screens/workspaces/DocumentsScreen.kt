package com.localkarar.app.ui.screens.workspaces

import com.localkarar.app.ui.components.LkHairline
import com.localkarar.app.ui.components.LkRowGroup
import com.localkarar.app.ui.components.LkListRow
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.core.rememberFilePicker
import com.localkarar.app.network.dto.WorkspaceDocumentDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkChip
import com.localkarar.app.ui.components.LkEmptyState
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkInfoPanel
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkHeroPage
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.DocumentsUiState
import com.localkarar.app.workspaces.DocumentsViewModel

@Composable
fun DocumentsScreen(
    viewModel: DocumentsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val yukleniyor by viewModel.yukleniyor.collectAsState()
    var deleteConfirmId by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }

    /*
     * Dosya secici. Platform karsiliklari (`rememberFilePicker`) ZATEN VARDI,
     * yalnizca kullanilmiyordu -- ekran "belge yukleme su an icin web
     * suruminde kullanilabilir" diyordu.
     *
     * Kategori GONDERILMIYOR (null): sunucu icerigi kendisi cozumluyor ve
     * e-faturayi taniyor. Yukleme aninda kullaniciya kategori sordurmak,
     * dogru cevabi zaten bilen bir sisteme gereksiz bir adim eklemek olurdu.
     */
    val dosyaSec = rememberFilePicker { secilen ->
        if (secilen != null) {
            viewModel.belgeYukle(secilen.name, secilen.bytes)
        }
    }

    LkHeroPage(title = "Belgeler", onBack = onBack) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is DocumentsUiState.Loading -> LkLoadingState()
                is DocumentsUiState.Error -> LkErrorState(
                    message = state.message,
                    onRetry = { viewModel.load() }
                )
                is DocumentsUiState.Content -> {
                    if (state.documents.isEmpty()) {
                        LkEmptyState(
                            title = "Henüz belge yok",
                            description = "Fatura, sözleşme veya makbuz yükleyin. e-Fatura XML dosyaları otomatik olarak çözümlenir.",
                            icon = Icons.Outlined.AttachFile,
                            action = {
                                LkButton(
                                    text = if (yukleniyor) "Yükleniyor..." else "Belge yükle",
                                    onClick = dosyaSec,
                                    enabled = !yukleniyor
                                )
                            }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(LkSpacing.Space4),
                            verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
                        ) {
                            item {
                                LkButton(
                                    text = if (yukleniyor) "Yükleniyor..." else "Belge yükle",
                                    onClick = dosyaSec,
                                    enabled = !yukleniyor,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            /* Tek yukseltilmis yuzey; kart yigini degil. */
                            item {
                                LkRowGroup {
                                    state.documents.forEachIndexed { i, document ->
                                        DocumentCard(
                                            document = document,
                                            onDelete = { deleteConfirmId = document.id }
                                        )
                                        if (i != state.documents.lastIndex) LkHairline()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (notice != null) {
        androidx.compose.material.AlertDialog(
            onDismissRequest = { notice = null },
            backgroundColor = LkSurfacePanel,
            title = { Text(text = "Bilgi", style = LkTypography.getBodyStrong(), color = LkTextPrimary) },
            text = { Text(text = notice!!, style = LkTypography.getBodySmall(), color = LkTextSecondary) },
            confirmButton = { LkButton(text = "Tamam", onClick = { notice = null }) }
        )
    }

    deleteConfirmId?.let { documentId ->
        androidx.compose.material.AlertDialog(
            onDismissRequest = { deleteConfirmId = null },
            backgroundColor = LkSurfacePanel,
            title = {
                Text(
                    text = "Belgeyi Sil",
                    style = LkTypography.getBodyStrong(),
                    color = LkTextPrimary
                )
            },
            text = {
                Text(
                    text = "Bu belge kalıcı olarak arşivlenecek. Devam etmek istiyor musunuz?",
                    style = LkTypography.getBodySmall(),
                    color = LkTextSecondary
                )
            },
            confirmButton = {
                LkButton(
                    text = "Evet, Sil",
                    onClick = {
                        deleteConfirmId = null
                        viewModel.delete(documentId) { success ->
                            notice = if (success) null else "Belge silinemedi."
                        }
                    }
                )
            },
            dismissButton = {
                LkButton(
                    text = "Vazgeç",
                    variant = LkButtonVariant.QUIET,
                    onClick = { deleteConfirmId = null }
                )
            }
        )
    }
}

@Composable
private fun DocumentCard(
    document: WorkspaceDocumentDto,
    onDelete: () -> Unit
) {
    /*
     * §24 satir anatomisi. Onceden her belge kenarlikli bir karttaydi ve
     * icinde iki hap daha vardi — liste uc kat yer kapliyordu.
     *
     * Analiz durumu alt satirda KELIMEYLE yaziliyor; hap renginden
     * okunmasi gerekmiyor.
     */
    val analiz = document.analysisStatus?.let {
        when (it) {
            "completed" -> "Analiz edildi"
            "processing" -> "Analiz ediliyor"
            "failed" -> "Analiz başarısız"
            else -> it
        }
    }
    LkListRow(
        baslik = document.originalName,
        altBaslik = listOfNotNull(
            analiz,
            document.documentDate?.let { LkDateUtils.formatDate(it) }
        ).joinToString(" · ").ifBlank { null },
        kategori = document.category?.let { documentCategoryLabel(it) },
        ikon = {
            Icon(
                imageVector = Icons.Outlined.AttachFile,
                contentDescription = null,
                tint = if (document.analysisStatus == "failed") LkWarning else LkTileInk,
                modifier = Modifier.size(21.dp)
            )
        },
        sag = {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Sil",
                    tint = LkTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    )
}

fun documentCategoryLabel(category: String): String {
    return when (category) {
        "invoice" -> "Fatura"
        "receipt" -> "Fiş"
        "contract" -> "Sözleşme"
        "promissory_note" -> "Senet"
        "shipment" -> "Sevkiyat"
        "purchase" -> "Satın Alma"
        else -> "Diğer"
    }
}