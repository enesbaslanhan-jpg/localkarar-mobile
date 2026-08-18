package com.localkarar.app.ui.screens.mentor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.mentor.ConversationViewModel
import com.localkarar.app.network.dto.MessageDto
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*

@Composable
fun ConversationScreen(
    conversationId: Int,
    viewModel: ConversationViewModel
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(conversationId) {
        viewModel.setConversation(conversationId)
    }

    LaunchedEffect(state, viewModel.streamText) {
        val content = state as? ConversationViewModel.UiState.Content ?: return@LaunchedEffect
        val count = content.messages.size + if (viewModel.isStreaming) 1 else 0
        if (count > 0) {
            listState.animateScrollToItem(count - 1)
        }
    }

    LkPageLayout(
        title = (state as? ConversationViewModel.UiState.Content)?.conversation?.title ?: "Sohbet",
        onBack = null
    ) {
        Column(Modifier.fillMaxSize()) {
            when (val s = state) {
                is ConversationViewModel.UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LkPrimary)
                    }
                }
                is ConversationViewModel.UiState.Error -> {
                    Column(
                        Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(s.message, color = LkDanger)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = { viewModel.setConversation(conversationId) }) {
                            Text("Tekrar Dene", color = LkPrimary)
                        }
                    }
                }
                is ConversationViewModel.UiState.Content -> {
                    val messages = s.messages
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages, key = { it.id }) { message ->
                            MessageBubble(message)
                        }
                        if (viewModel.isStreaming) {
                            item(key = "streaming") {
                                StreamingBubble(
                                    text = viewModel.streamText,
                                    provider = viewModel.providerName
                                )
                            }
                        }
                    }
                    viewModel.streamError?.let {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                it,
                                style = LkTypography.getBodySmall(),
                                color = LkDanger,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    if (viewModel.isStreaming) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextButton(onClick = { viewModel.stopStreaming() }) {
                                Text("Yanıtı Durdur", color = LkDanger)
                            }
                        }
                    }
                    ChatInputBar(
                        value = viewModel.input,
                        onValueChange = { viewModel.onInputChange(it) },
                        enabled = !viewModel.isStreaming,
                        onSend = { viewModel.sendMessage() }
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: MessageDto) {
    val isUser = message.role == "user"
    val failed = message.generationStatus == "failed" || message.error != null
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.85f),
            backgroundColor = if (isUser) LkPrimary else LkSurfacePanel,
            elevation = 0.dp,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            )
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(
                    message.content.ifEmpty { "…" },
                    style = LkTypography.getBody(),
                    color = if (isUser) LkOnPrimary else LkTextPrimary
                )
                if (failed) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        message.error ?: "Yanıt oluşturulamadı",
                        style = LkTypography.getMicro(),
                        color = LkDanger
                    )
                }
                message.createdAt?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        LkDateUtils.formatDateTime(it),
                        style = LkTypography.getMicro(),
                        color = if (isUser) LkOnPrimary.copy(alpha = 0.7f) else LkTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun StreamingBubble(text: String, provider: String?) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Card(
            modifier = Modifier.fillMaxWidth(0.85f),
            backgroundColor = LkSurfacePanel,
            elevation = 0.dp,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = 4.dp,
                bottomEnd = 16.dp
            )
        ) {
            Column(Modifier.padding(12.dp)) {
                provider?.let {
                    Text(
                        it,
                        style = LkTypography.getMicro(),
                        color = LkPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                }
                if (text.isEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = LkPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text("Yanıt hazırlanıyor…", style = LkTypography.getBodySmall())
                    }
                } else {
                    Text(text, style = LkTypography.getBody(), color = LkTextPrimary)
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    onSend: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Mesajını yaz…") },
            enabled = enabled,
            maxLines = 4,
            textStyle = LkTypography.getBody()
        )
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = onSend,
            enabled = enabled && value.isNotBlank(),
            modifier = Modifier.height(56.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = LkPrimary, contentColor = LkOnPrimary)
        ) {
            Text("➤", fontWeight = FontWeight.Bold)
        }
    }
}