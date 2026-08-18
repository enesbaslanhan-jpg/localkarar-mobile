package com.localkarar.app.ui.screens.workspaces

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.ui.components.LkEmptyState
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.ActivityUiState
import com.localkarar.app.workspaces.ActivityViewModel

@Composable
fun ActivityScreen(
    viewModel: ActivityViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LkPageLayout(title = "Etkinlik", onBack = onBack) {
        when (val state = uiState) {
            is ActivityUiState.Loading -> LkLoadingState()
            is ActivityUiState.Error -> LkErrorState(
                message = state.message,
                onRetry = { viewModel.load() }
            )
            is ActivityUiState.Content -> {
                if (state.items.isEmpty()) {
                    LkEmptyState(
                        title = "Henüz etkinlik yok",
                        description = "İşletme içi değişiklikler burada izlenir.",
                        icon = Icons.Default.Construction
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(LkSpacing.Space4),
                        verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
                    ) {
                        items(state.items, key = { it.id }) { item ->
                            ActivityRow(
                                action = activityLabel(item.action),
                                detail = activityDetail(item.action, item.entityType),
                                createdAt = item.createdAt
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(
    action: String,
    detail: String,
    createdAt: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LkSurfacePanel, LkShapes.MD)
            .border(1.dp, LkLineStrong, LkShapes.MD)
            .padding(LkSpacing.PadPanel)
    ) {
        Text(
            text = action,
            style = LkTypography.getBodySmall(),
            color = LkTextPrimary
        )
        if (detail.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = detail,
                style = LkTypography.getMetadata(),
                color = LkTextSecondary
            )
        }
        createdAt?.let {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = LkDateUtils.formatTimeAgo(it),
                style = LkTypography.getMicro(),
                color = LkTextMuted
            )
        }
    }
}

fun activityLabel(action: String): String {
    return when (action) {
        "workspace.created" -> "İşletme oluşturuldu"
        "workspace.updated" -> "İşletme güncellendi"
        "member.invited" -> "Üye davet edildi"
        "member.joined" -> "Üye katıldı"
        "member.removed" -> "Üye çıkarıldı"
        "member.role_changed" -> "Üye rolü değişti"
        "record.created" -> "Kayıt oluşturuldu"
        "record.updated" -> "Kayıt güncellendi"
        "record.completed" -> "Kayıt tamamlandı"
        "record.cancelled" -> "Kayıt iptal edildi"
        "record.deferred" -> "Kayıt ertelendi"
        "contact.created" -> "Kişi eklendi"
        "contact.updated" -> "Kişi güncellendi"
        "contact.archived" -> "Kişi arşivlendi"
        "document.uploaded" -> "Belge yüklendi"
        "document.archived" -> "Belge arşivlendi"
        else -> action.replace('.', ' ').replaceFirstChar { it.uppercase() }
    }
}

private fun activityDetail(action: String, entityType: String?): String {
    val type = entityType ?: return ""
    return when (type) {
        "businessRecord" -> "Kayıt"
        "businessMember" -> "Üye"
        "businessContact" -> "Kişi"
        "uploadedDocument" -> "Belge"
        "workspace" -> "İşletme"
        else -> type.replaceFirstChar { it.uppercase() }
    }
}