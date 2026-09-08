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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkEmptyState
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkHairline
import com.localkarar.app.ui.components.LkPressable
import com.localkarar.app.ui.components.LkRowGroup
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkHeroPage
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.NotificationsUiState
import com.localkarar.app.workspaces.NotificationsViewModel

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LkHeroPage(
        title = "Bildirimler",
        onBack = onBack,
        actions = {
            val unread = (uiState as? NotificationsUiState.Content)?.unreadCount ?: 0
            if (unread > 0) {
                androidx.compose.material.TextButton(onClick = { viewModel.markAllRead() }) {
                    Text("Tümünü oku", color = LkHero.OnHero, style = LkTypography.getBodySmall())
                }
            }
        },
        heroExtra = {
            val unread = (uiState as? NotificationsUiState.Content)?.unreadCount
            unread?.let {
                Text("$it okunmamış", style = LkTypography.getMetadata(), color = LkHero.OnHeroSecondary,
                    modifier = Modifier.padding(start = LkSpacing.Space5, top = LkSpacing.Space2))
            }
        }
    ) {
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
                        icon = Icons.Outlined.Notifications
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(LkSpacing.Space4),
                        verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
                    ) {
                        /* Tek yukseltilmis yuzey; her bildirim kendi cercevesinde degil. */
                        item {
                            LkRowGroup {
                                state.notifications.forEachIndexed { i, notification ->
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
                                    if (i != state.notifications.lastIndex) LkHairline()
                                }
                            }
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
    /*
     * §24 satir anatomisi -- ama LkListRow KULLANILMADI: bildirimin govdesi
     * cumle uzunlugunda ve LkListRow alt satiri tek satira kirpiyor. Burada
     * govde iki satira kadar aciliyor, cunku bildirimi acacak bir detay
     * ekrani yok; okunacak yer bu satirin kendisi.
     *
     * Okunmamis olma OKUNMUS/OKUNMAMIS ayrimi yalnizca nokta rengiyle degil,
     * baslik agirligiyla da veriliyor.
     */
    LkPressable(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(LkSpacing.PadCard),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(8.dp)
                    .background(
                        if (isRead) LkLineStrong else LkPrimary,
                        androidx.compose.foundation.shape.CircleShape
                    )
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
                        color = LkTextSecondary,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
}
