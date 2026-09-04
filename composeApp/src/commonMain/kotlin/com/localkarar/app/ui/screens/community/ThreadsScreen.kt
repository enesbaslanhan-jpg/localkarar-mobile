package com.localkarar.app.ui.screens.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.localkarar.app.community.ThreadsViewModel
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.network.dto.CommunityThreadDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.theme.*

@Composable
fun ThreadsScreen(
    viewModel: ThreadsViewModel,
    currentUserId: Int? = null,
    onOpenThread: (String) -> Unit
) {
    val threadsState by viewModel.threadsState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadThreads()
    }

    Box(Modifier.fillMaxSize()) {
        when (val s = threadsState) {
            is ThreadsViewModel.ThreadsUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LkPrimary)
                }
            }
            is ThreadsViewModel.ThreadsUiState.Error -> {
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(s.message, color = LkDanger, style = LkTypography.getBody())
                    Spacer(Modifier.height(12.dp))
                    LkButton(text = "Tekrar Dene", variant = LkButtonVariant.SECONDARY, onClick = { viewModel.loadThreads() })
                }
            }
            is ThreadsViewModel.ThreadsUiState.Content -> {
                if (s.threads.isEmpty() && s.invitations.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = LkTextMuted, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("Henüz sohbetiniz bulunmuyor", style = LkTypography.getBodyStrong(), color = LkTextPrimary)
                            Spacer(Modifier.height(6.dp))
                            Text("Topluluk üyeleriyle sohbet başlatmak için aşağıdaki düğmeyi kullanın.", style = LkTypography.getBodySmall(), color = LkTextSecondary)
                            Spacer(Modifier.height(16.dp))
                            LkButton(text = "Sohbet Başlat", onClick = { viewModel.openCreateThreadSheet() })
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Pending Invitations Section
                        if (s.invitations.isNotEmpty()) {
                            item {
                                Text("Grup Davetleri (${s.invitations.size})", style = LkTypography.getSectionTitle(), color = LkPrimary)
                                Spacer(Modifier.height(4.dp))
                            }
                            items(s.invitations, key = { "inv_${it.id}" }) { inv ->
                                InvitationCard(
                                    thread = inv,
                                    onAccept = { viewModel.handleInvitation(inv.id, true) },
                                    onDecline = { viewModel.handleInvitation(inv.id, false) }
                                )
                            }
                            item {
                                Spacer(Modifier.height(8.dp))
                                Divider(color = LkLineSoft)
                                Spacer(Modifier.height(8.dp))
                                Text("Sohbetler", style = LkTypography.getSectionTitle(), color = LkTextPrimary)
                            }
                        }

                        // Joined Threads
                        items(s.threads, key = { it.id }) { thread ->
                            ThreadCard(
                                thread = thread,
                                currentUserId = currentUserId,
                                onClick = { onOpenThread(thread.id) }
                            )
                        }
                    }
                }
            }
        }

        // FAB to create thread
        FloatingActionButton(
            onClick = { viewModel.openCreateThreadSheet() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            backgroundColor = LkPrimary
        ) {
            Icon(Icons.Outlined.AddComment, contentDescription = "Yeni Sohbet", tint = LkOnPrimary)
        }
    }

    if (viewModel.showCreateThreadSheet) {
        CreateThreadSheet(
            viewModel = viewModel,
            onThreadCreated = { threadId ->
                onOpenThread(threadId)
            }
        )
    }
}

@Composable
private fun ThreadCard(
    thread: CommunityThreadDto,
    currentUserId: Int?,
    onClick: () -> Unit
) {
    val displayName = if (thread.isGroup) {
        thread.name ?: "Grup Sohbeti (${thread.members.size})"
    } else {
        val otherMember = thread.members.firstOrNull { it.userId != currentUserId }
        otherMember?.user?.name ?: thread.name ?: "Sohbet"
    }

    val lastMessage = thread.messages.firstOrNull()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        backgroundColor = LkSurfacePanel,
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
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (thread.isGroup) LkPrimarySoft else LkSurfaceSunken),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (thread.isGroup) Icons.Outlined.Group else Icons.Outlined.Person,
                    contentDescription = null,
                    tint = if (thread.isGroup) LkPrimary else LkTextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        displayName,
                        style = LkTypography.getBodyStrong(),
                        color = LkTextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    thread.updatedAt?.let { ts ->
                        Spacer(Modifier.width(8.dp))
                        Text(
                            LkDateUtils.formatDateTime(ts),
                            style = LkTypography.getMicro(),
                            color = LkTextMuted
                        )
                    }
                }

                Spacer(Modifier.height(2.dp))

                Text(
                    lastMessage?.body ?: "Henüz mesaj yok",
                    style = LkTypography.getBodySmall(),
                    color = if (lastMessage != null) LkTextSecondary else LkTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun InvitationCard(
    thread: CommunityThreadDto,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = LkSurfaceSunken,
        shape = LkShapes.MD,
        elevation = 0.dp
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.MailOutline, contentDescription = null, tint = LkPrimary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    thread.name ?: "Grup Sohbeti Daveti",
                    style = LkTypography.getBodyStrong(),
                    color = LkTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Bu gruba katılmak için davet edildiniz (${thread.members.size} üye).",
                style = LkTypography.getBodySmall(),
                color = LkTextSecondary
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDecline) {
                    Text("Reddet", color = LkDanger)
                }
                Spacer(Modifier.width(8.dp))
                LkButton(text = "Kabul Et", onClick = onAccept)
            }
        }
    }
}
