package com.localkarar.app.ui.screens.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.localkarar.app.community.CommunityNotificationsViewModel
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.network.dto.CommunityNotificationDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*

@Composable
fun NotificationsScreen(
    viewModel: CommunityNotificationsViewModel,
    onBack: () -> Unit,
    onOpenPost: (String) -> Unit,
    onOpenProfile: (Int) -> Unit,
    onOpenThread: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    LkPageLayout(
        title = "Bildirimler",
        onBack = onBack,
        actions = {
            if (viewModel.unreadCount > 0) {
                TextButton(onClick = { viewModel.markAllRead() }) {
                    Text("Tümünü Oku", style = LkTypography.getMicro(), color = LkPrimary)
                }
            }
        }
    ) {
        when (val s = uiState) {
            is CommunityNotificationsViewModel.NotificationsUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LkPrimary)
                }
            }
            is CommunityNotificationsViewModel.NotificationsUiState.Error -> {
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(s.message, color = LkDanger, style = LkTypography.getBody())
                    Spacer(Modifier.height(12.dp))
                    LkButton(text = "Tekrar Dene", variant = LkButtonVariant.SECONDARY, onClick = { viewModel.loadNotifications() })
                }
            }
            is CommunityNotificationsViewModel.NotificationsUiState.Content -> {
                if (s.items.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = LkTextMuted, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("Henüz bildiriminiz yok", style = LkTypography.getBodyStrong(), color = LkTextPrimary)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(s.items, key = { it.id }) { notif ->
                            NotificationRow(
                                notification = notif,
                                onClick = {
                                    when (notif.type) {
                                        "follow" -> notif.actor?.id?.let(onOpenProfile)
                                        "like", "reply", "quote" -> notif.postId?.let(onOpenPost)
                                        "message" -> notif.threadId?.let(onOpenThread)
                                        "thread_invite" -> notif.threadId?.let(onOpenThread)
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
private fun NotificationRow(
    notification: CommunityNotificationDto,
    onClick: () -> Unit
) {
    val isUnread = notification.readAt == null

    val (icon, actionText) = when (notification.type) {
        "follow" -> Icons.Default.PersonAdd to "seni takip etmeye başladı"
        "like" -> Icons.Default.Favorite to "gönderini beğendi"
        "reply" -> Icons.Default.Reply to "gönderine yanıt verdi"
        "quote" -> Icons.Default.FormatQuote to "gönderini alıntıladı"
        "message" -> Icons.Default.Chat to "sohbette mesaj gönderdi"
        "thread_invite" -> Icons.Default.GroupAdd to "seni bir gruba davet etti"
        else -> Icons.Default.Notifications to "bir etkileşimde bulundu"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        backgroundColor = if (isUnread) LkSurfaceSunken else LkSurfacePanel,
        shape = LkShapes.MD,
        elevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isUnread) LkPrimarySoft else LkSurfacePanel),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = if (isUnread) LkPrimary else LkTextSecondary, modifier = Modifier.size(20.dp))
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = "${notification.actor?.name ?: "Biri"} $actionText",
                    style = LkTypography.getBodySmall(),
                    color = LkTextPrimary,
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal
                )

                notification.post?.ozet?.let { ozet ->
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "\"$ozet\"",
                        style = LkTypography.getMicro(),
                        color = LkTextMuted,
                        maxLines = 1
                    )
                }

                Spacer(Modifier.height(4.dp))
                notification.createdAt?.let { ts ->
                    Text(
                        LkDateUtils.formatDateTime(ts),
                        style = LkTypography.getMicro(),
                        color = LkTextMuted
                    )
                }
            }

            if (isUnread) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(LkPrimary)
                )
            }
        }
    }
}
