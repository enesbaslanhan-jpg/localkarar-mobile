package com.localkarar.app.ui.screens.mentor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.mentor.ConversationViewModel
import com.localkarar.app.mentor.StreamStatus
import com.localkarar.app.network.dto.CitationDto
import com.localkarar.app.network.dto.MessageDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkMarkdown
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*

data class ParsedDisclaimer(
    val mainContent: String,
    val disclaimer: String?
)

fun splitDisclaimer(content: String): ParsedDisclaimer {
    if (content.isBlank()) return ParsedDisclaimer("", null)
    val delimiter = "\n\n---\n"
    val parts = content.split(delimiter)
    return if (parts.size > 1) {
        val disclaimer = parts.last().trim()
        val mainContent = parts.dropLast(1).joinToString(delimiter).trim()
        ParsedDisclaimer(mainContent, disclaimer.ifBlank { null })
    } else {
        ParsedDisclaimer(content, null)
    }
}

@Composable
fun ConversationScreen(
    conversationId: Int,
    viewModel: ConversationViewModel,
    onBack: (() -> Unit)? = null
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    var toastMessage by remember { mutableStateOf<String?>(null) }

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

    val conversationTitle = (state as? ConversationViewModel.UiState.Content)?.conversation?.title ?: "Sohbet"

    LkPageLayout(
        title = conversationTitle,
        onBack = onBack,
        actions = {
            IconButton(onClick = {
                renameText = conversationTitle
                showRenameDialog = true
            }) {
                Icon(Icons.Default.Edit, contentDescription = "Yeniden Adlandır", tint = LkTextSecondary)
            }
        }
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
                        Text(s.message, color = LkDanger, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        LkButton(
                            text = "Tekrar Dene",
                            variant = LkButtonVariant.SECONDARY,
                            onClick = { viewModel.setConversation(conversationId) }
                        )
                    }
                }
                is ConversationViewModel.UiState.Content -> {
                    val messages = s.messages

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(messages, key = { it.id }) { message ->
                            MessageBubble(
                                message = message,
                                onRegenerate = { viewModel.regenerateMessage(message.id) },
                                onEdit = { viewModel.startEditingMessage(message) },
                                onFeedback = { toastMessage = "Geri bildiriminiz kaydedildi" }
                            )
                        }

                        if (viewModel.isStreaming) {
                            item(key = "streaming_bubble") {
                                StreamingBubble(
                                    text = viewModel.streamText,
                                    provider = viewModel.providerName,
                                    onStop = { viewModel.stopStreaming() }
                                )
                            }
                        }
                    }

                    // Toast message banner
                    toastMessage?.let { msg ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .background(LkSurfacePanel, RoundedCornerShape(8.dp))
                                .border(1.dp, LkPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(msg, style = LkTypography.getBodySmall(), color = LkPrimary)
                        }
                        LaunchedEffect(msg) {
                            kotlinx.coroutines.delay(2500)
                            toastMessage = null
                        }
                    }

                    // Stream Error banner
                    viewModel.streamError?.let { err ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            backgroundColor = LkDanger.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            elevation = 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = LkDanger, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = err,
                                    style = LkTypography.getBodySmall(),
                                    color = LkDanger,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { viewModel.clearError() }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Kapat", tint = LkDanger, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // Edit Message Mode vs Normal Input Bar
                    if (viewModel.editingMessageId != null) {
                        EditMessageBar(
                            text = viewModel.editingMessageText,
                            onTextChange = { viewModel.onEditingTextChange(it) },
                            onCancel = { viewModel.cancelEditing() },
                            onSubmit = { viewModel.submitEditAndRegenerate() },
                            enabled = !viewModel.isStreaming
                        )
                    } else {
                        ChatInputBar(
                            value = viewModel.input,
                            onValueChange = { viewModel.onInputChange(it) },
                            enabled = !viewModel.isStreaming,
                            isStreaming = viewModel.isStreaming,
                            onSend = { viewModel.sendMessage() },
                            onStop = { viewModel.stopStreaming() }
                        )
                    }
                }
            }
        }
    }

    // Rename Dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Sohbeti Yeniden Adlandır", style = LkTypography.getSectionTitle()) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Sohbet Başlığı") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.renameConversation(renameText)
                        showRenameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = LkPrimary)
                ) {
                    Text("Kaydet", color = LkOnPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("İptal", color = LkTextSecondary)
                }
            },
            backgroundColor = LkSurfacePanel
        )
    }
}

@Composable
private fun MessageBubble(
    message: MessageDto,
    onRegenerate: () -> Unit,
    onEdit: () -> Unit,
    onFeedback: () -> Unit
) {
    val isUser = message.role == "user"
    val isCancelled = message.generationStatus == "cancelled" || message.error == "GENERATION_CANCELLED"
    val isFailed = message.generationStatus == "failed"
    val clipboardManager = LocalClipboardManager.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(if (isUser) 0.85f else 0.95f),
            backgroundColor = if (isUser) LkPrimary else LkSurfacePanel,
            elevation = 0.dp,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            )
        ) {
            Column(Modifier.padding(14.dp)) {
                if (isUser) {
                    // User Message
                    Text(
                        text = message.content,
                        style = LkTypography.getBody().copy(color = LkOnPrimary, lineHeight = 22.sp)
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        message.createdAt?.let {
                            Text(
                                text = LkDateUtils.formatDateTime(it),
                                style = LkTypography.getMicro(),
                                color = LkOnPrimary.copy(alpha = 0.7f)
                            )
                        }
                        IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Mesajı Düzenle",
                                tint = LkOnPrimary.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                } else {
                    // Assistant Message
                    val parsed = remember(message.content) { splitDisclaimer(message.content) }

                    // Main Markdown Answer
                    LkMarkdown(
                        content = parsed.mainContent.ifEmpty { "…" },
                        textColor = LkTextPrimary
                    )

                    // Disclaimer Section if present
                    parsed.disclaimer?.let { disclaimerText ->
                        Spacer(Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = LkWarning.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LkWarning.copy(alpha = 0.3f)),
                            elevation = 0.dp
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = LkWarning,
                                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = disclaimerText,
                                    style = LkTypography.getBodySmall().copy(
                                        color = LkTextSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                )
                            }
                        }
                    }

                    // Cancelled Note
                    if (isCancelled) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(LkTextSecondary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.StopCircle, contentDescription = null, tint = LkTextSecondary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Yanıt durduruldu", style = LkTypography.getMicro(), color = LkTextSecondary)
                        }
                    }

                    // Failed Error Note
                    if (isFailed && message.error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(message.error, style = LkTypography.getMicro(), color = LkDanger)
                    }

                    // Citations / Sources Badge List
                    val sources = message.knowledgeObjects ?: message.citations
                    if (!sources.isNullOrEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Text("Kaynaklar", style = LkTypography.getMicro(), color = LkPrimary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            sources.forEach { source ->
                                CitationCard(source)
                            }
                        }
                    }

                    // Bottom action toolbar (Regenerate, Copy, Feedback)
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        message.createdAt?.let {
                            Text(
                                text = LkDateUtils.formatDateTime(it),
                                style = LkTypography.getMicro(),
                                color = LkTextSecondary.copy(alpha = 0.8f)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { clipboardManager.setText(AnnotatedString(message.content)) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Kopyala", tint = LkTextSecondary, modifier = Modifier.size(15.dp))
                            }
                            IconButton(onClick = onFeedback, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Outlined.ThumbUp, contentDescription = "Faydalı", tint = LkTextSecondary, modifier = Modifier.size(15.dp))
                            }
                            IconButton(onClick = onFeedback, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Outlined.ThumbDown, contentDescription = "Faydasız", tint = LkTextSecondary, modifier = Modifier.size(15.dp))
                            }
                            IconButton(onClick = onRegenerate, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Refresh, contentDescription = "Yeniden Oluştur", tint = LkPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Mentor atif karti.
 *
 * ⚠️ TIKLANABILIR DEGIL ve bu bilincli. Webde ayni atif
 * `/app/knowledge/:code` sayfasina goturuyor, ama urun sahibi karari
 * (03.09.2026) Bilgi Nesnelerinin uygulamada erisilebilir OLMAMASI
 * yonunde. Atif yalniz cevabin neye dayandigini gosteriyor.
 */
@Composable
private fun CitationCard(citation: CitationDto) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(LkSurfaceCanvas)
            .border(1.dp, LkLineSoft, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.MenuBook, contentDescription = null, tint = LkPrimary, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = citation.title ?: citation.knowledgeObjectCode ?: citation.code ?: "İlgili Doküman",
                style = LkTypography.getMicro().copy(fontWeight = FontWeight.SemiBold),
                color = LkTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
@Composable
private fun StreamingBubble(
    text: String,
    provider: String?,
    onStop: () -> Unit
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f),
            backgroundColor = LkSurfacePanel,
            elevation = 0.dp,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
        ) {
            Column(Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = LkPrimary)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = provider ?: "AI Mentor Yanıtlıyor…",
                            style = LkTypography.getMicro().copy(fontWeight = FontWeight.Bold),
                            color = LkPrimary
                        )
                    }
                    TextButton(onClick = onStop, modifier = Modifier.height(28.dp)) {
                        Text("Durdur", color = LkDanger, style = LkTypography.getMicro())
                    }
                }
                Spacer(Modifier.height(8.dp))
                if (text.isBlank()) {
                    Text("Yanıt oluşturuluyor…", style = LkTypography.getBodySmall(), color = LkTextSecondary)
                } else {
                    LkMarkdown(content = text, textColor = LkTextPrimary)
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
    isStreaming: Boolean,
    onSend: () -> Unit,
    onStop: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(LkSurfaceCanvas)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Mesajınızı yazın… (En fazla 8000 karakter)") },
            enabled = enabled,
            maxLines = 5,
            textStyle = LkTypography.getBody(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = LkSurfacePanel,
                focusedBorderColor = LkPrimary,
                unfocusedBorderColor = LkLineSoft
            )
        )
        Spacer(Modifier.width(8.dp))
        if (isStreaming) {
            Button(
                onClick = onStop,
                modifier = Modifier.height(54.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = LkDanger, contentColor = LkOnPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Stop, contentDescription = "Durdur")
            }
        } else {
            Button(
                onClick = onSend,
                enabled = enabled && value.isNotBlank(),
                modifier = Modifier.height(54.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = LkPrimary, contentColor = LkOnPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Gönder")
            }
        }
    }
}

@Composable
private fun EditMessageBar(
    text: String,
    onTextChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSubmit: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        backgroundColor = LkSurfacePanel,
        shape = RoundedCornerShape(8.dp),
        elevation = 0.dp
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mesajı Düzenle", style = LkTypography.getBodyStrong(), color = LkPrimary)
                IconButton(onClick = onCancel, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "İptal", tint = LkTextSecondary, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4,
                enabled = enabled,
                textStyle = LkTypography.getBody()
            )
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onCancel) {
                    Text("İptal", color = LkTextSecondary)
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = onSubmit,
                    enabled = enabled && text.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = LkPrimary)
                ) {
                    Text("Yeniden Oluştur", color = LkOnPrimary)
                }
            }
        }
    }
}