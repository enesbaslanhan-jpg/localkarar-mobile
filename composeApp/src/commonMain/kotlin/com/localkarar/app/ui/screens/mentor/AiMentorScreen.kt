package com.localkarar.app.ui.screens.mentor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.mentor.MentorViewModel
import com.localkarar.app.mentor.MemoryViewModel
import com.localkarar.app.network.dto.ConversationListItemDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkInfoPanel
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*

@Composable
fun AiMentorScreen(
    viewModel: MentorViewModel,
    memoryViewModel: MemoryViewModel,
    onOpenConversation: (Int) -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showMemorySheet by remember { mutableStateOf(false) }

    LkPageLayout(
        title = "AI Mentor",
        onBack = null
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (val s = state) {
                is MentorViewModel.UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LkPrimary)
                    }
                }
                is MentorViewModel.UiState.Error -> {
                    Column(
                        Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(s.message, color = LkDanger)
                        Spacer(Modifier.height(12.dp))
                        LkButton(text = "Tekrar Dene", variant = LkButtonVariant.SECONDARY, onClick = { viewModel.refresh() })
                    }
                }
                is MentorViewModel.UiState.Content -> {
                    Column(Modifier.fillMaxSize()) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${s.conversations.size} sohbet",
                                style = LkTypography.getBodySmall(),
                                color = LkTextSecondary
                            )
                            LkButton(
                                text = "Hatıralar",
                                variant = LkButtonVariant.SECONDARY,
                                onClick = { showMemorySheet = true }
                            )
                        }
                        if (s.loading) {
                            Row(
                                Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = LkPrimary)
                            }
                        }
                        if (s.conversations.isEmpty() && !s.loading) {
                            Column(
                                Modifier.fillMaxWidth().padding(48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Henüz sohbet yok", style = LkTypography.getBodyStrong())
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "İşletmenle ilgili sorular sor, mentordan yanıt al.",
                                    style = LkTypography.getBodySmall(),
                                    color = LkTextSecondary
                                )
                                Spacer(Modifier.height(16.dp))
                                LkButton(text = "Yeni Sohbet Başlat", onClick = { viewModel.onCreateNew(onCreated = onOpenConversation) })
                            }
                        } else {
                            LazyColumn(
                                Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                items(s.conversations, key = { it.id }) { conversation ->
                                    ConversationCard(
                                        conversation = conversation,
                                        onClick = { onOpenConversation(conversation.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            FloatingActionButton(
                onClick = { viewModel.onCreateNew(onCreated = onOpenConversation) },
                modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
                backgroundColor = LkPrimary
            ) {
                Text("+", style = LkTypography.getSectionTitle(), color = LkOnPrimary)
            }
        }
    }

    if (showMemorySheet) {
        MemorySheet(
            viewModel = memoryViewModel,
            onDismiss = { showMemorySheet = false }
        )
    }
}

@Composable
private fun ConversationCard(
    conversation: ConversationListItemDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable(onClick = onClick),
        backgroundColor = LkSurfacePanel,
        elevation = 0.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(36.dp).background(LkPrimary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        conversation.title.firstOrNull()?.toString() ?: "?",
                        color = LkPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        conversation.title,
                        style = LkTypography.getBodyStrong(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${conversation.messageCount} mesaj",
                        style = LkTypography.getBodySmall(),
                        color = LkTextSecondary
                    )
                }
                Text(
                    conversation.lastMessageAt?.let { LkDateUtils.formatTimeAgo(it) } ?: "",
                    style = LkTypography.getMetadata(),
                    color = LkTextSecondary
                )
            }
        }
    }
}

@Composable
private fun MemorySheet(
    viewModel: MemoryViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        backgroundColor = LkSurfacePanel,
        title = {
            Text("Hatıralar", style = LkTypography.getBodyStrong(), color = LkTextPrimary)
        },
        text = {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    "AI Mentor seni hatırlamak için bu bilgileri kullanır.",
                    style = LkTypography.getBodySmall(),
                    color = LkTextSecondary
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = viewModel.input,
                    onValueChange = { viewModel.onInputChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Yeni hatıra…") },
                    singleLine = true,
                    textStyle = LkTypography.getBody()
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    viewModel.memoryTypes.forEach { type ->
                        Chip(
                            label = type,
                            selected = type == viewModel.selectedType,
                            onClick = { viewModel.onTypeChange(type) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                LkButton(
                    text = "Kaydet",
                    onClick = { viewModel.addMemory() },
                    enabled = viewModel.input.isNotBlank()
                )
                viewModel.notice?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, style = LkTypography.getBodySmall(), color = LkPrimary)
                }
                Spacer(Modifier.height(16.dp))
                when (val s = state) {
                    is MemoryViewModel.UiState.Loading -> {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp, color = LkPrimary)
                        }
                    }
                    is MemoryViewModel.UiState.Error -> {
                        LkInfoPanel(title = "Hata") { Text(s.message) }
                    }
                    is MemoryViewModel.UiState.Content -> {
                        if (s.memories.isEmpty()) {
                            Text(
                                "Henüz hatıra yok",
                                style = LkTypography.getBodySmall(),
                                color = LkTextSecondary
                            )
                        } else {
                            LazyColumn(
                                Modifier.heightIn(max = 320.dp).fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(s.memories, key = { it.id }) { memory ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        backgroundColor = LkSurfaceSunken,
                                        elevation = 0.dp
                                    ) {
                                        Row(
                                            Modifier.fillMaxWidth().padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(Modifier.weight(1f)) {
                                                Text(
                                                    memory.value,
                                                    style = LkTypography.getBodySmall()
                                                )
                                                Text(
                                                    memory.type,
                                                    style = LkTypography.getMicro(),
                                                    color = LkPrimary
                                                )
                                            }
                                            IconButton(onClick = { viewModel.deleteMemory(memory.id) }) {
                                                Text("✕", style = LkTypography.getBody())
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Kapat", color = LkPrimary) }
        }
    )
}

@Composable
private fun Chip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(if (selected) LkPrimary else LkSurfaceSunken, LkShapes.MD)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            style = LkTypography.getMicro(),
            color = if (selected) LkOnPrimary else LkTextSecondary
        )
    }
}