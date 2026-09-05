package com.localkarar.app.ui.screens.workspaces

import androidx.compose.foundation.background
import androidx.compose.material.IconButton
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.network.dto.WorkspaceSummaryDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkHairline
import com.localkarar.app.ui.components.LkListRow
import com.localkarar.app.ui.components.LkRowGroup
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkEmptyState
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkHeroPage
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.WorkspacesUiState
import com.localkarar.app.workspaces.WorkspacesViewModel

@Composable
fun WorkspacesScreen(
    viewModel: WorkspacesViewModel,
    activeWorkspaceId: String?,
    onOpenWorkspace: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var actionError by remember { mutableStateOf<String?>(null) }

    LkHeroPage(title = "İşletme Takibi", onBack = onBack) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (actionError != null) {
                Text(
                    text = actionError!!,
                    color = LkDanger,
                    style = LkTypography.getBodySmall(),
                    modifier = Modifier.padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space2)
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                when (val state = uiState) {
                    is WorkspacesUiState.Loading -> LkLoadingState()
                    is WorkspacesUiState.Error -> LkErrorState(
                        message = state.message,
                        onRetry = { viewModel.load() }
                    )
                    is WorkspacesUiState.Content -> {
                        if (state.workspaces.isEmpty()) {
                            LkEmptyState(
                                title = "Henüz işletme yok",
                                description = "İşletmenizi ekleyerek ödeme, tahsilat ve takip kayıtlarını yönetin.",
                                icon = Icons.Outlined.Business,
                                action = {
                                    LkButton(
                                        text = "İşletme Ekle",
                                        onClick = { showCreateDialog = true }
                                    )
                                }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(LkSpacing.Space4),
                                verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
                            ) {
                                item {
                                    LkButton(
                                        text = "Yeni İşletme",
                                        onClick = { showCreateDialog = true },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                /* Tek yukseltilmis yuzey; kart yigini degil. */
                                item {
                                    LkRowGroup {
                                        state.workspaces.forEachIndexed { i, workspace ->
                                            WorkspaceCard(
                                                workspace = workspace,
                                                isActive = workspace.id == activeWorkspaceId,
                                                onOpen = { onOpenWorkspace(workspace.id) },
                                                onDelete = {
                                                    viewModel.deleteWorkspace(workspace.id) { success ->
                                                        actionError = if (success) null else "İşletme silinemedi."
                                                    }
                                                }
                                            )
                                            if (i != state.workspaces.lastIndex) LkHairline()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateWorkspaceDialog(
            isCreating = (uiState as? WorkspacesUiState.Content)?.isCreating == true,
            onDismiss = { showCreateDialog = false },
            onCreate = { name, legalName, sector, city ->
                actionError = null
                viewModel.createWorkspace(
                    name = name,
                    legalName = legalName,
                    sector = sector,
                    city = city,
                    onSuccess = { workspaceId ->
                        showCreateDialog = false
                        onOpenWorkspace(workspaceId)
                    },
                    onError = { actionError = it }
                )
            }
        )
    }
}

@Composable
private fun WorkspaceCard(
    workspace: WorkspaceSummaryDto,
    isActive: Boolean,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    /*
     * §24 satir anatomisi. Onceden her isletme kenarlikli bir karttaydi ve
     * AKTIF isletme yalnizca kenarlik renginden anlasiliyordu -- renk tek
     * basina bilgi tasiyamaz (§19).
     *
     * Artik aktiflik "Aktif" kelimesiyle alt satirda yaziyor, ustune yesil
     * tik ikonu esligiyle.
     */
    LkListRow(
        baslik = workspace.name,
        altBaslik = listOfNotNull(
            if (isActive) "Aktif" else null,
            workspace.sector?.takeIf { it.isNotBlank() },
            "${workspace.memberCount} üye"
        ).joinToString(" · "),
        onClick = onOpen,
        ikon = {
            Icon(
                imageVector = Icons.Outlined.Business,
                contentDescription = null,
                tint = if (isActive) LkSuccess else LkTileInk,
                modifier = Modifier.size(21.dp)
            )
        },
        sag = {
            if (isActive) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "Aktif",
                    tint = LkSuccess,
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Sil",
                    tint = LkTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    )
    if (showDeleteConfirm) {
        androidx.compose.material.AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            backgroundColor = LkSurfacePanel,
            title = {
                Text(
                    text = "İşletmeyi Sil",
                    style = LkTypography.getBodyStrong(),
                    color = LkTextPrimary
                )
            },
            text = {
                Text(
                    text = "\"${workspace.name}\" işletmesi ve tüm kayıtları silinecek. Bu işlem geri alınamaz. Devam etmek istiyor musunuz?",
                    style = LkTypography.getBodySmall(),
                    color = LkTextSecondary
                )
            },
            confirmButton = {
                LkButton(
                    text = "Evet, Sil",
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                )
            },
            dismissButton = {
                LkButton(
                    text = "Vazgeç",
                    variant = com.localkarar.app.ui.components.LkButtonVariant.QUIET,
                    onClick = { showDeleteConfirm = false }
                )
            }
        )
    }
}

@Composable
private fun CreateWorkspaceDialog(
    isCreating: Boolean,
    onDismiss: () -> Unit,
    onCreate: (name: String, legalName: String?, sector: String?, city: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var legalName by remember { mutableStateOf("") }
    var sector by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    androidx.compose.material.AlertDialog(
        onDismissRequest = { if (!isCreating) onDismiss() },
        backgroundColor = LkSurfacePanel,
        title = {
            Text(
                text = "Yeni İşletme",
                style = LkTypography.getBodyStrong(),
                color = LkTextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)) {
                if (error != null) {
                    Text(text = error!!, style = LkTypography.getBodySmall(), color = LkDanger)
                }
                LkTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "İşletme Adı",
                    placeholder = "Örn: Bakırköy Pastanesi"
                )
                LkTextField(
                    value = legalName,
                    onValueChange = { legalName = it },
                    label = "Resmi Unvan (isteğe bağlı)",
                    placeholder = "Örn: XYZ Tic. Ltd. Şti."
                )
                LkTextField(
                    value = sector,
                    onValueChange = { sector = it },
                    label = "Sektör (isteğe bağlı)",
                    placeholder = "Örn: Perakende"
                )
                LkTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = "Şehir (isteğe bağlı)",
                    placeholder = "Örn: İstanbul"
                )
            }
        },
        confirmButton = {
            LkButton(
                text = if (isCreating) "Oluşturuluyor..." else "Oluştur",
                enabled = name.isNotBlank() && !isCreating,
                onClick = {
                    if (name.isBlank()) {
                        error = "İşletme adı gerekli"
                    } else {
                        error = null
                        onCreate(name, legalName, sector, city)
                    }
                }
            )
        },
        dismissButton = {
            LkButton(
                text = "Vazgeç",
                variant = com.localkarar.app.ui.components.LkButtonVariant.QUIET,
                onClick = onDismiss
            )
        }
    )
}