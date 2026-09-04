package com.localkarar.app.ui.screens.community

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.localkarar.app.community.CommunityViewModel
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.network.dto.CommunityPostDto
import com.localkarar.app.network.dto.QuotedPostDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*

@Composable
fun CommunityPostDetailScreen(
    postId: String,
    viewModel: CommunityViewModel,
    currentUserId: Int? = null,
    onBack: () -> Unit,
    onOpenProfile: (Int) -> Unit,
    onOpenPost: (String) -> Unit
) {
    val detailState by viewModel.detailState.collectAsState()
    var showReportDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(postId) {
        viewModel.loadPostDetail(postId)
    }

    LkPageLayout(
        title = "Gönderi",
        onBack = onBack
    ) {
        when (val s = detailState) {
            is CommunityViewModel.DetailUiState.Loading, CommunityViewModel.DetailUiState.Idle -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LkPrimary)
                }
            }
            is CommunityViewModel.DetailUiState.Error -> {
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(s.message, color = LkDanger, style = LkTypography.getBody())
                    Spacer(Modifier.height(12.dp))
                    LkButton(text = "Tekrar Dene", variant = LkButtonVariant.SECONDARY, onClick = { viewModel.loadPostDetail(postId) })
                }
            }
            is CommunityViewModel.DetailUiState.Content -> {
                val post = s.post
                val isAuthor = currentUserId != null && post.author?.id == currentUserId

                Box(Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 70.dp),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Parent Post Context (if this is a reply)
                        s.parent?.let { parentPost ->
                            item {
                                ParentPostCard(
                                    post = parentPost,
                                    onAuthorClick = { parentPost.author?.id?.let(onOpenProfile) },
                                    onClick = { onOpenPost(parentPost.id) }
                                )
                                Spacer(Modifier.height(4.dp))
                            }
                        }

                        // Main Post Detail
                        item {
                            MainPostCard(
                                post = post,
                                isAuthor = isAuthor,
                                onAuthorClick = { post.author?.id?.let(onOpenProfile) },
                                onLike = { viewModel.toggleLike(post.id, post.begendim) },
                                onBookmark = { viewModel.toggleBookmark(post.id, post.kaydettim) },
                                onReply = { viewModel.startCompose(replyTo = post) },
                                onQuote = { viewModel.startCompose(quoteOf = post) },
                                onReport = { showReportDialog = true },
                                onDelete = { showDeleteConfirm = true },
                                onQuotedPostClick = { qId -> onOpenPost(qId) }
                            )
                        }

                        // Replies section header
                        item {
                            Divider(color = LkLineSoft, modifier = Modifier.padding(vertical = 4.dp))
                            Text(
                                "Yanıtlar (${post.replies.size})",
                                style = LkTypography.getSectionTitle(),
                                color = LkTextPrimary
                            )
                        }

                        if (post.replies.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Henüz yanıt yok. İlk yanıtı sen yaz!",
                                        style = LkTypography.getBodySmall(),
                                        color = LkTextMuted
                                    )
                                }
                            }
                        } else {
                            items(post.replies.size, key = { post.replies[it].id }) { idx ->
                                ReplyItemView(
                                    reply = post.replies[idx],
                                    depth = 0,
                                    onAuthorClick = { post.replies[idx].author?.id?.let(onOpenProfile) },
                                    onLike = { rId, liked -> viewModel.toggleLike(rId, liked) },
                                    onReply = { rPost -> viewModel.startCompose(replyTo = rPost) }
                                )
                            }
                        }
                    }

                    // Sticky Bottom Bar for Replying
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(),
                        color = LkSurfacePanel,
                        elevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(LkShapes.MD)
                                    .background(LkSurfaceSunken)
                                    .border(1.dp, LkLineSoft, LkShapes.MD)
                                    .clickable { viewModel.startCompose(replyTo = post) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    "Yanıt yaz...",
                                    style = LkTypography.getBodySmall(),
                                    color = LkTextMuted
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            IconButton(onClick = { viewModel.startCompose(replyTo = post) }) {
                                Icon(Icons.Outlined.Send, contentDescription = "Yanıtla", tint = LkPrimary)
                            }
                        }
                    }
                }
            }
        }
    }

    if (viewModel.composing) {
        ComposePostSheet(viewModel = viewModel)
    }

    if (showReportDialog) {
        PostReportDialog(
            onDismiss = { showReportDialog = false },
            onSubmit = { reason, details ->
                viewModel.reportPost(postId, reason, details)
                showReportDialog = false
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            backgroundColor = LkSurfacePanel,
            title = { Text("Gönderiyi Kaldır", style = LkTypography.getBodyStrong(), color = LkTextPrimary) },
            text = { Text("Bu gönderiyi kaldırmak istediğinize emin misiniz?", style = LkTypography.getBody(), color = LkTextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePost(postId) { onBack() }
                    showDeleteConfirm = false
                }) {
                    Text("Kaldır", color = LkDanger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Vazgeç", color = LkTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun MainPostCard(
    post: CommunityPostDto,
    isAuthor: Boolean,
    onAuthorClick: () -> Unit,
    onLike: () -> Unit,
    onBookmark: () -> Unit,
    onReply: () -> Unit,
    onQuote: () -> Unit,
    onReport: () -> Unit,
    onDelete: () -> Unit,
    onQuotedPostClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = LkSurfacePanel,
        elevation = 0.dp,
        shape = LkShapes.MD
    ) {
        Column(Modifier.padding(16.dp)) {
            // Author Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(LkPrimarySoft)
                        .clickable(onClick = onAuthorClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (post.author?.name?.take(1) ?: "U").uppercase(),
                        style = LkTypography.getBodyStrong(),
                        color = LkPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f).clickable(onClick = onAuthorClick)) {
                    Text(
                        post.author?.name ?: "Bilinmeyen",
                        style = LkTypography.getBodyStrong(),
                        color = LkTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        post.publishedAt?.let { LkDateUtils.formatDateTime(it) } ?: "",
                        style = LkTypography.getMicro(),
                        color = LkTextMuted
                    )
                }

                if (isAuthor) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Sil", tint = LkDanger)
                    }
                } else {
                    IconButton(onClick = onReport) {
                        Icon(Icons.Outlined.Flag, contentDescription = "Şikayet Et", tint = LkTextMuted)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Post Text
            Text(
                post.summary,
                style = LkTypography.getBody(),
                color = LkTextPrimary
            )

            // Attached Media
            post.media?.let { media ->
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(LkShapes.MD)
                        .background(LkSurfaceSunken)
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (media.kind == "image") Icons.Outlined.Image else Icons.Outlined.InsertDriveFile,
                            contentDescription = null,
                            tint = LkPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            media.originalName ?: "Ekli dosya",
                            style = LkTypography.getBodySmall(),
                            color = LkTextPrimary
                        )
                    }
                }
            }

            // Quoted Post
            post.quotedPost?.let { q ->
                Spacer(Modifier.height(10.dp))
                QuotedPostCard(quotedPost = q, onClick = { onQuotedPostClick(q.id) })
            }

            Spacer(Modifier.height(16.dp))
            Divider(color = LkLineSoft)
            Spacer(Modifier.height(8.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like Button
                Row(
                    modifier = Modifier
                        .clip(LkShapes.SM)
                        .clickable(onClick = onLike)
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (post.begendim) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Beğen",
                        tint = if (post.begendim) LkDanger else LkTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${post.begeniSayisi}",
                        style = LkTypography.getMicro(),
                        color = if (post.begendim) LkDanger else LkTextSecondary
                    )
                }

                // Reply Button
                Row(
                    modifier = Modifier
                        .clip(LkShapes.SM)
                        .clickable(onClick = onReply)
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Yanıtla",
                        tint = LkTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${post.yanitSayisi}",
                        style = LkTypography.getMicro(),
                        color = LkTextSecondary
                    )
                }

                // Quote Button
                Row(
                    modifier = Modifier
                        .clip(LkShapes.SM)
                        .clickable(onClick = onQuote)
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FormatQuote,
                        contentDescription = "Alıntıla",
                        tint = LkTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${post.alintiSayisi}",
                        style = LkTypography.getMicro(),
                        color = LkTextSecondary
                    )
                }

                // Bookmark Button
                IconButton(onClick = onBookmark, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (post.kaydettim) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Kaydet",
                        tint = if (post.kaydettim) LkPrimary else LkTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ParentPostCard(
    post: CommunityPostDto,
    onAuthorClick: () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LkShapes.MD)
            .border(1.dp, LkLineSoft, LkShapes.MD)
            .background(LkSurfaceSunken)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Yanıtlanan Üst Gönderi",
                    style = LkTypography.getMicro(),
                    color = LkPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "• @${post.author?.name ?: "kullanıcı"}",
                    style = LkTypography.getMicro(),
                    color = LkTextSecondary,
                    modifier = Modifier.clickable(onClick = onAuthorClick)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                post.summary,
                style = LkTypography.getBodySmall(),
                color = LkTextSecondary,
                maxLines = 3
            )
        }
    }
}

@Composable
fun QuotedPostCard(
    quotedPost: QuotedPostDto,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LkShapes.MD)
            .border(1.dp, LkLineSoft, LkShapes.MD)
            .background(LkSurfaceSunken)
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        if (quotedPost.kaldirildi) {
            Text(
                "Bu gönderi kaldırıldı.",
                style = LkTypography.getMicro(),
                color = LkTextMuted
            )
        } else {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "@${quotedPost.author?.name ?: "kullanıcı"}",
                        style = LkTypography.getMicro(),
                        color = LkPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    quotedPost.summary ?: "",
                    style = LkTypography.getBodySmall(),
                    color = LkTextPrimary,
                    maxLines = 3
                )
            }
        }
    }
}

@Composable
private fun ReplyItemView(
    reply: CommunityPostDto,
    depth: Int,
    onAuthorClick: () -> Unit,
    onLike: (String, Boolean) -> Unit,
    onReply: (CommunityPostDto) -> Unit
) {
    val indent = (depth * 16).dp

    Column(modifier = Modifier.fillMaxWidth().padding(start = indent)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(LkShapes.MD)
                .background(LkSurfacePanel)
                .padding(12.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(LkPrimarySoft)
                            .clickable(onClick = onAuthorClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            (reply.author?.name?.take(1) ?: "U").uppercase(),
                            style = LkTypography.getMicro(),
                            color = LkPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        reply.author?.name ?: "Bilinmeyen",
                        style = LkTypography.getBodySmall(),
                        fontWeight = FontWeight.Bold,
                        color = LkTextPrimary,
                        modifier = Modifier.clickable(onClick = onAuthorClick)
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        reply.publishedAt?.let { LkDateUtils.formatDateTime(it) } ?: "",
                        style = LkTypography.getMicro(),
                        color = LkTextMuted
                    )
                }

                Spacer(Modifier.height(6.dp))
                Text(reply.summary, style = LkTypography.getBodySmall(), color = LkTextPrimary)

                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(LkShapes.SM)
                            .clickable { onLike(reply.id, reply.begendim) }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (reply.begendim) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Beğen",
                            tint = if (reply.begendim) LkDanger else LkTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text("${reply.begeniSayisi}", style = LkTypography.getMicro(), color = LkTextMuted)
                    }

                    Spacer(Modifier.width(12.dp))

                    Text(
                        "Yanıtla",
                        style = LkTypography.getMicro(),
                        color = LkPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(LkShapes.SM)
                            .clickable { onReply(reply) }
                            .padding(4.dp)
                    )
                }
            }
        }

        // Render nested child replies
        reply.replies.forEach { childReply ->
            Spacer(Modifier.height(6.dp))
            ReplyItemView(
                reply = childReply,
                depth = minOf(depth + 1, 2),
                onAuthorClick = onAuthorClick,
                onLike = onLike,
                onReply = onReply
            )
        }
    }
}

@Composable
fun PostReportDialog(
    onDismiss: () -> Unit,
    onSubmit: (reason: String, details: String?) -> Unit
) {
    var reason by remember { mutableStateOf("spam") }
    var details by remember { mutableStateOf("") }

    val reasons = listOf(
        "spam" to "Spam / İstenmeyen İçerik",
        "misinformation" to "Yanıltıcı / Yanlış Bilgi",
        "harassment" to "Taciz / Hakaret",
        "unsafe" to "Güvensiz / Zararlı İçerik",
        "copyright" to "Telif Hakkı İhlali",
        "other" to "Diğer"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        backgroundColor = LkSurfacePanel,
        shape = RoundedCornerShape(16.dp),
        title = { Text("Gönderiyi Şikayet Et", style = LkTypography.getSectionTitle(), color = LkTextPrimary) },
        text = {
            Column(Modifier.fillMaxWidth()) {
                reasons.forEach { (key, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { reason = key }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = reason == key,
                            onClick = { reason = key },
                            colors = RadioButtonDefaults.colors(selectedColor = LkPrimary, unselectedColor = LkTextMuted)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(label, style = LkTypography.getBodySmall(), color = LkTextPrimary)
                    }
                }

                if (reason == "other") {
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        placeholder = { Text("Açıklama belirtiniz...", style = LkTypography.getBodySmall(), color = LkTextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            backgroundColor = LkSurfaceSunken,
                            textColor = LkTextPrimary,
                            cursorColor = LkPrimary,
                            focusedBorderColor = LkPrimary,
                            unfocusedBorderColor = LkLineSoft
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSubmit(reason, details.ifBlank { null })
                }
            ) {
                Text("Gönder", color = LkPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç", color = LkTextSecondary)
            }
        }
    )
}