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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.network.dto.WorkspaceSummaryDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkEmptyState
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkPageLayout
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

    LkPageLayout(title = "İşletme Takibi", onBack = onBack) {
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
                                icon = Icons.Default.Business,
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
                                items(state.workspaces) { workspace ->
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LkSurfacePanel, LkShapes.MD)
            .border(1.dp, if (isActive) LkPrimary.copy(alpha = 0.6f) else LkLineStrong, LkShapes.MD)
            .clickable(onClick = onOpen)
            .padding(LkSpacing.PadPanel)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Business,
                contentDescription = null,
                tint = LkPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(LkSpacing.Space2))
            Text(
                text = workspace.name,
                style = LkTypography.getBodyStrong(),
                color = LkTextPrimary,
                modifier = Modifier.weight(1f)
            )
            if (isActive) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Aktif",
                    tint = LkSuccess,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        if (!workspace.sector.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(LkSpacing.Space1))
            Text(
                text = workspace.sector,
                style = LkTypography.getMetadata(),
                color = LkTextSecondary
            )
        }
        Spacer(modifier = Modifier.height(LkSpacing.Space2))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${workspace.memberCount} üye",
                style = LkTypography.getMetadata(),
                color = LkTextMuted,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = LkTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
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