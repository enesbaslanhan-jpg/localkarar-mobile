package com.localkarar.app.ui.screens.community

import com.localkarar.app.ui.components.LkLoadingSpinner
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.localkarar.app.community.ThreadsViewModel
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.network.dto.ThreadMessageDto
import com.localkarar.app.ui.components.LkLoadingDesen
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*

@Composable
fun ThreadDetailScreen(
    threadId: String,
    viewModel: ThreadsViewModel,
    currentUserId: Int?,
    onBack: () -> Unit
) {
    val messagesState by viewModel.messagesState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(threadId) {
        viewModel.loadMessages(threadId)
    }

    LkPageLayout(
        title = "Sohbet",
        onBack = onBack
    ) {
        Column(Modifier.fillMaxSize().imePadding()) {
            when (val s = messagesState) {
                is ThreadsViewModel.MessagesUiState.Loading, ThreadsViewModel.MessagesUiState.Idle -> {
                    LkLoadingState(modifier = Modifier.weight(1f), desen = LkLoadingDesen.LISTE)
                }
                is ThreadsViewModel.MessagesUiState.Error -> {
                    Column(
                        Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(s.message, color = LkDanger, style = LkTypography.getBody())
                        Spacer(Modifier.height(12.dp))
                        LkButton(text = "Tekrar Dene", variant = LkButtonVariant.SECONDARY, onClick = { viewModel.loadMessages(threadId) })
                    }
                }
                is ThreadsViewModel.MessagesUiState.Content -> {
                    LaunchedEffect(s.messages.size) {
                        if (s.messages.isNotEmpty()) {
                            listState.animateScrollToItem(s.messages.size - 1)
                        }
                    }

                    if (s.messages.isEmpty()) {
                        Box(Modifier.weight(1f).fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Henüz mesaj yok. İlk mesajı siz gönderin!", style = LkTypography.getBodySmall(), color = LkTextMuted)
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(s.messages, key = { it.id }) { msg ->
                                val isMe = currentUserId != null && msg.senderId == currentUserId
                                MessageBubble(message = msg, isMe = isMe)
                            }
                        }
                    }
                }
            }

            // Sticky Bottom Input Composer
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = LkSurfaceCanvas,
                elevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    /*
                     * Yazma alani: sistem alani, hap yaricapli. Mockup
                     * "Topluluk 5"te yazma alani klavyeye yapisik duruyor —
                     * kaydirmayla birlikte hareket etmiyor.
                     */
                    LkTextField(
                        value = viewModel.messageInput,
                        onValueChange = { viewModel.onMessageInputChange(it) },
                        placeholder = "Mesaj yazın...",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.size(48.dp).clip(CircleShape).background(LkPrimaryFill), contentAlignment = Alignment.Center) {
                      IconButton(
                        onClick = { viewModel.sendMessage(threadId) },
                        enabled = viewModel.messageInput.isNotBlank() && !viewModel.isSendingMessage
                      ) {
                        if (viewModel.isSendingMessage) {
                            LkLoadingSpinner(size = 20.dp, renk = LkOnPrimary)
                        } else {
                            Icon(Icons.Outlined.Send, contentDescription = "Gönder", tint = if (viewModel.messageInput.isNotBlank()) LkOnPrimary else LkTextMuted)
                        }
                      }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ThreadMessageDto,
    isMe: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        if (!isMe && message.sender?.name != null) {
            Text(
                message.sender.name,
                style = LkTypography.getMicro(),
                color = LkTextSecondary,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 16.dp
                    )
                )
                .background(if (isMe) LkPrimaryFill else LkSurfaceRaised)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text(
                    message.body,
                    style = LkTypography.getBody(),
                    color = if (isMe) LkOnPrimary else LkTextPrimary
                )
                Spacer(Modifier.height(4.dp))
                message.createdAt?.let { ts ->
                    Text(
                        LkDateUtils.formatDateTime(ts),
                        style = LkTypography.getMicro(),
                        color = if (isMe) LkOnPrimary.copy(alpha = 0.7f) else LkTextMuted,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}
