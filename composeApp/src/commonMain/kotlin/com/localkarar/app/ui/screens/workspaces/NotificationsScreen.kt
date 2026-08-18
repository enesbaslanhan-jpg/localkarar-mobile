package com.localkarar.app.ui.screens.workspaces

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkEmptyState
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.NotificationsUiState
import com.localkarar.app.workspaces.NotificationsViewModel

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LkPageLayout(title = "Bildirimler", onBack = onBack) {
        when (val state = uiState) {
            is NotificationsUiState.Loading -> LkLoadingState()
            is NotificationsUiState.Error -> LkErrorState(
                message = state.message,
                onRetry = { viewModel.load() }
            )
            is NotificationsUiState.Content -> {
                if (state.notifications.isEmpty()) {
                    LkEmptyState(
                        title = "Bildirim yok",
                        description = "Yeni bildirimleriniz burada görünür.",
                        icon = Icons.Default.Notifications
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(LkSpacing.Space4),
                        verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
                    ) {
                        item {
                            if (state.unreadCount > 0) {
                                LkButton(
                                    text = "Tümünü Okundu İşaretle",
                                    variant = LkButtonVariant.QUIET,
                                    onClick = { viewModel.markAllRead() },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        items(state.notifications, key = { it.id }) { notification ->
                            NotificationCard(
                                title = notification.title ?: "Bildirim",
                                body = notification.body,
                                createdAt = notification.createdAt,
                                isRead = notification.readAt != null,
                                onClick = {
                                    if (notification.readAt == null) {
                                        viewModel.markRead(notification.id)
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

@Composable
private fun NotificationCard(
    title: String,
    body: String?,
    createdAt: String?,
    isRead: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LkSurfacePanel, LkShapes.MD)
            .border(1.dp, if (isRead) LkLineSoft else LkPrimary.copy(alpha = 0.5f), LkShapes.MD)
            .clickable(onClick = onClick)
            .padding(LkSpacing.PadPanel),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(if (isRead) LkTextMuted else LkPrimary, androidx.compose.foundation.shape.CircleShape)
                .padding(top = LkSpacing.Space2)
        )
        Spacer(modifier = Modifier.width(LkSpacing.Space3))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = if (isRead) LkTypography.getBodySmall() else LkTypography.getBodyStrong(),
                color = LkTextPrimary
            )
            if (!body.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(LkSpacing.Space1))
                Text(
                    text = body,
                    style = LkTypography.getMetadata(),
                    color = LkTextSecondary
                )
            }
            createdAt?.let {
                Spacer(modifier = Modifier.height(LkSpacing.Space1))
                Text(
                    text = LkDateUtils.formatTimeAgo(it),
                    style = LkTypography.getMicro(),
                    color = LkTextMuted
                )
            }
        }
    }
}