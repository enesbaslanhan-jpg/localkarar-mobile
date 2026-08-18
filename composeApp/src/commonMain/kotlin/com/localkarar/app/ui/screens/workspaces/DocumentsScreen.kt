package com.localkarar.app.ui.screens.workspaces

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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.network.dto.WorkspaceDocumentDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkChip
import com.localkarar.app.ui.components.LkEmptyState
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkInfoPanel
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.DocumentsUiState
import com.localkarar.app.workspaces.DocumentsViewModel

@Composable
fun DocumentsScreen(
    viewModel: DocumentsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var deleteConfirmId by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }

    LkPageLayout(title = "Belgeler", onBack = onBack) {
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
                            description = "Web sürümünden belge yükleyebilirsiniz. Yüklenen belgeler burada listelenir.",
                            icon = Icons.Default.AttachFile
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(LkSpacing.Space4),
                            verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
                        ) {
                            item {
                                LkInfoPanel(title = "Bilgi", icon = Icons.Default.Info) {
                                    Text(
                                        text = "Belge yükleme şu an için web sürümünde kullanılabilir. Yüklediğiniz belgelerin analiz özetleri burada görünür.",
                                        style = LkTypography.getMetadata(),
                                        color = LkTextSecondary
                                    )
                                }
                            }
                            items(state.documents, key = { it.id }) { document ->
                                DocumentCard(
                                    document = document,
                                    onDelete = { deleteConfirmId = document.id }
                                )
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LkSurfacePanel, LkShapes.MD)
            .border(1.dp, LkLineStrong, LkShapes.MD)
            .padding(LkSpacing.PadPanel)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AttachFile,
                contentDescription = null,
                tint = LkPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(LkSpacing.Space2))
            Text(
                text = document.originalName,
                style = LkTypography.getBodyStrong(),
                color = LkTextPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = LkTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(LkSpacing.Space2))
        Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
            document.category?.let { LkChip(text = documentCategoryLabel(it)) }
            document.analysisStatus?.let {
                LkChip(
                    text = when (it) {
                        "completed" -> "Analiz edildi"
                        "processing" -> "Analiz ediliyor"
                        "failed" -> "Analiz başarısız"
                        else -> it
                    },
                    contentColor = if (it == "completed") LkSuccess else LkWarning
                )
            }
        }
        document.documentDate?.let {
            Spacer(modifier = Modifier.height(LkSpacing.Space2))
            Text(
                text = "Belge Tarihi: ${LkDateUtils.formatDate(it)}",
                style = LkTypography.getMetadata(),
                color = LkTextMuted
            )
        }
    }
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