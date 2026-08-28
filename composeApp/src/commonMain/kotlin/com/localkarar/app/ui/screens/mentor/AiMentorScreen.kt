package com.localkarar.app.ui.screens.mentor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.mentor.MemoryViewModel
import com.localkarar.app.mentor.MentorTab
import com.localkarar.app.mentor.MentorViewModel
import com.localkarar.app.network.dto.ConversationListItemDto
import com.localkarar.app.network.dto.MemoryDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*

@Composable
fun AiMentorScreen(
    viewModel: MentorViewModel,
    memoryViewModel: MemoryViewModel,
    onOpenConversation: (Int) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val state by viewModel.state.collectAsState()
    var showMemorySheet by remember { mutableStateOf(false) }

    // Dialog states
    var conversationToRename by remember { mutableStateOf<ConversationListItemDto?>(null) }
    var renameText by remember { mutableStateOf("") }
    var conversationToDelete by remember { mutableStateOf<ConversationListItemDto?>(null) }

    LkPageLayout(
        title = "AI Mentor",
        onBack = onBack,
        actions = {
            IconButton(onClick = { showMemorySheet = true }) {
                Icon(Icons.Default.Psychology, contentDescription = "Hatıralar", tint = LkPrimary)
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                // Segmented Tabs: Aktif vs Arşiv
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LkSurfaceCanvas)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TabPill(
                        title = "Aktif",
                        selected = viewModel.selectedTab == MentorTab.ACTIVE,
                        onClick = { viewModel.setTab(MentorTab.ACTIVE) },
                        modifier = Modifier.weight(1f)
                    )
                    TabPill(
                        title = "Arşiv",
                        selected = viewModel.selectedTab == MentorTab.ARCHIVED,
                        onClick = { viewModel.setTab(MentorTab.ARCHIVED) },
                        modifier = Modifier.weight(1f)
                    )
                }

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
                            LkButton(
                                text = "Tekrar Dene",
                                variant = LkButtonVariant.SECONDARY,
                                onClick = { viewModel.refresh() }
                            )
                        }
                    }
                    is MentorViewModel.UiState.Content -> {
                        if (s.loading) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth().height(2.dp),
                                color = LkPrimary
                            )
                        }

                        if (s.conversations.isEmpty() && !s.loading) {
                            val emptyText = if (viewModel.selectedTab == MentorTab.ACTIVE) {
                                "Henüz aktif bir sohbetiniz yok."
                            } else {
                                "Arşivlenmiş sohbet bulunmuyor."
                            }
                            Column(
                                Modifier.fillMaxSize().padding(48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (viewModel.selectedTab == MentorTab.ACTIVE) Icons.Default.ChatBubbleOutline else Icons.Default.Archive,
                                    contentDescription = null,
                                    tint = LkTextSecondary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    emptyText,
                                    style = LkTypography.getBodyStrong(),
                                    color = LkTextPrimary
                                )
                                Spacer(Modifier.height(8.dp))
                                if (viewModel.selectedTab == MentorTab.ACTIVE) {
                                    Text(
                                        "İşletmeniz, kararlarınız veya hedefleriniz hakkında sormak istediğiniz her şeyi mentora iletebilirsiniz.",
                                        style = LkTypography.getBodySmall(),
                                        color = LkTextSecondary
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    LkButton(
                                        text = "Yeni Sohbet Başlat",
                                        onClick = { viewModel.onCreateNew(onCreated = onOpenConversation) }
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                items(s.conversations, key = { it.id }) { conversation ->
                                    ConversationCard(
                                        conversation = conversation,
                                        isArchived = viewModel.selectedTab == MentorTab.ARCHIVED,
                                        onClick = { onOpenConversation(conversation.id) },
                                        onRename = {
                                            conversationToRename = conversation
                                            renameText = conversation.title
                                        },
                                        onArchiveToggle = {
                                            if (viewModel.selectedTab == MentorTab.ACTIVE) {
                                                viewModel.onArchive(conversation.id)
                                            } else {
                                                viewModel.onUnarchive(conversation.id)
                                            }
                                        },
                                        onDelete = { conversationToDelete = conversation }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Floating Action Button for New Chat (only on Active tab)
            if (viewModel.selectedTab == MentorTab.ACTIVE) {
                FloatingActionButton(
                    onClick = { viewModel.onCreateNew(onCreated = onOpenConversation) },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
                    backgroundColor = LkPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Yeni Sohbet", tint = LkOnPrimary)
                }
            }
        }
    }

    // Rename Dialog
    conversationToRename?.let { conv ->
        AlertDialog(
            onDismissRequest = { conversationToRename = null },
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
                        viewModel.onRename(conv.id, renameText)
                        conversationToRename = null
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = LkPrimary)
                ) {
                    Text("Kaydet", color = LkOnPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { conversationToRename = null }) {
                    Text("İptal", color = LkTextSecondary)
                }
            },
            backgroundColor = LkSurfacePanel
        )
    }

    // Delete Confirmation Dialog
    conversationToDelete?.let { conv ->
        AlertDialog(
            onDismissRequest = { conversationToDelete = null },
            title = { Text("Sohbeti Sil", style = LkTypography.getSectionTitle()) },
            text = {
                Text(
                    "\"${conv.title}\" başlıklı sohbet silinsin mi? Bu işlem geri alınamaz.",
                    style = LkTypography.getBody()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onDelete(conv.id)
                        conversationToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = LkDanger)
                ) {
                    Text("Sil", color = LkOnPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { conversationToDelete = null }) {
                    Text("İptal", color = LkTextSecondary)
                }
            },
            backgroundColor = LkSurfacePanel
        )
    }

    // Memory Sheet
    if (showMemorySheet) {
        MemorySheet(
            viewModel = memoryViewModel,
            onDismiss = { showMemorySheet = false }
        )
    }
}

@Composable
private fun TabPill(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) LkPrimary else LkSurfacePanel)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = LkTypography.getBodyStrong(),
            color = if (selected) LkOnPrimary else LkTextSecondary
        )
    }
}

@Composable
private fun ConversationCard(
    conversation: ConversationListItemDto,
    isArchived: Boolean,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onArchiveToggle: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        backgroundColor = LkSurfacePanel,
        elevation = 0.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .background(LkPrimary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isArchived) Icons.Default.Archive else Icons.Default.Psychology,
                    contentDescription = null,
                    tint = LkPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    conversation.title.ifEmpty { "Yeni Sohbet" },
                    style = LkTypography.getBodyStrong(),
                    color = LkTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val lastSnippet = conversation.lastMessage?.content
                if (!lastSnippet.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        lastSnippet,
                        style = LkTypography.getBodySmall(),
                        color = LkTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                conversation.lastMessageAt?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        LkDateUtils.formatDateTime(it),
                        style = LkTypography.getMicro(),
                        color = LkTextSecondary.copy(alpha = 0.8f)
                    )
                }
            }

            // Options Overflow Menu
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Seçenekler",
                        tint = LkTextSecondary
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(LkSurfacePanel)
                ) {
                    DropdownMenuItem(onClick = {
                        menuExpanded = false
                        onRename()
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = LkPrimary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Yeniden Adlandır", color = LkTextPrimary)
                    }
                    DropdownMenuItem(onClick = {
                        menuExpanded = false
                        onArchiveToggle()
                    }) {
                        Icon(
                            if (isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                            contentDescription = null,
                            tint = LkPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (isArchived) "Arşivden Çıkar" else "Arşivle", color = LkTextPrimary)
                    }
                    DropdownMenuItem(onClick = {
                        menuExpanded = false
                        onDelete()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = LkDanger, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Sil", color = LkDanger)
                    }
                }
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
    var showClearAllConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    ModalBottomSheetLayout(
        sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Expanded),
        sheetBackgroundColor = LkSurfacePanel,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetContent = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(16.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = LkPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text("AI Hatıraları", style = LkTypography.getSectionTitle(), color = LkTextPrimary)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = LkTextSecondary)
                    }
                }
                Text(
                    "Mentorun hakkınızda hatırladığı işletme ve profil bilgileri.",
                    style = LkTypography.getBodySmall(),
                    color = LkTextSecondary
                )
                Spacer(Modifier.height(12.dp))

                // New Memory Input
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = LkSurfaceCanvas,
                    shape = RoundedCornerShape(8.dp),
                    elevation = 0.dp
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Yeni Hatıra Ekle", style = LkTypography.getBodyStrong(), color = LkTextPrimary)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = viewModel.input,
                            onValueChange = { viewModel.onInputChange(it) },
                            placeholder = { Text("Örn: E-ticaret sitemde kargo süresi 2 gündür") },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = LkTypography.getBodySmall()
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Tür: ${viewModel.selectedType}",
                                style = LkTypography.getMicro(),
                                color = LkPrimary
                            )
                            LkButton(
                                text = "Kaydet",
                                onClick = { viewModel.addMemory() }
                            )
                        }
                    }
                }

                viewModel.notice?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, style = LkTypography.getMicro(), color = LkPrimary)
                }

                Spacer(Modifier.height(12.dp))

                // Memory List Header & Clear All
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Kayıtlı Hatıralar", style = LkTypography.getBodyStrong(), color = LkTextPrimary)
                    TextButton(onClick = { showClearAllConfirm = true }) {
                        Text("Tümünü Temizle", color = LkDanger, style = LkTypography.getMicro())
                    }
                }

                when (val s = state) {
                    is MemoryViewModel.UiState.Loading -> {
                        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = LkPrimary)
                        }
                    }
                    is MemoryViewModel.UiState.Error -> {
                        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            Text(s.message, color = LkDanger)
                        }
                    }
                    is MemoryViewModel.UiState.Content -> {
                        if (s.memories.isEmpty()) {
                            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                Text("Kayıtlı hatıra bulunmuyor.", style = LkTypography.getBodySmall(), color = LkTextSecondary)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(s.memories, key = { it.id }) { memory ->
                                    MemoryCard(
                                        memory = memory,
                                        onDelete = { viewModel.deleteMemory(memory.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) {
        // Content underlying sheet
    }

    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = { Text("Tüm Hatıraları Temizle", style = LkTypography.getSectionTitle()) },
            text = {
                Text(
                    "Mentorun hafızasındaki tüm kayıtlar silinecektir. Devam etmek istiyor musunuz?",
                    style = LkTypography.getBody()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllMemories { showClearAllConfirm = false }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = LkDanger)
                ) {
                    Text("Tümünü Temizle", color = LkOnPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) {
                    Text("İptal", color = LkTextSecondary)
                }
            },
            backgroundColor = LkSurfacePanel
        )
    }
}

@Composable
private fun MemoryCard(
    memory: MemoryDto,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = LkSurfaceCanvas,
        shape = RoundedCornerShape(8.dp),
        elevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    memory.value,
                    style = LkTypography.getBodySmall(),
                    color = LkTextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        memory.type.uppercase(),
                        style = LkTypography.getMicro(),
                        color = LkPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    memory.createdAt?.let {
                        Text(
                            LkDateUtils.formatDateTime(it),
                            style = LkTypography.getMicro(),
                            color = LkTextSecondary
                        )
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = LkTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}