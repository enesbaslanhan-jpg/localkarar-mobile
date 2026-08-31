package com.localkarar.app.ui.screens.community

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.localkarar.app.community.CommunityNotificationsViewModel
import com.localkarar.app.community.CommunityViewModel
import com.localkarar.app.community.SocialViewModel
import com.localkarar.app.community.ThreadsViewModel
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.network.dto.CommunityPostDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*

enum class CommunityInternalTab(val title: String) {
    FEED("Akış"),
    MEMBERS("Kişiler"),
    CHATS("Sohbetler"),
    PROFILE("Profil")
}

@Composable
fun CommunityFeedScreen(
    communityViewModel: CommunityViewModel,
    socialViewModel: SocialViewModel,
    threadsViewModel: ThreadsViewModel,
    notificationsViewModel: CommunityNotificationsViewModel,
    currentUserId: Int? = null,
    onOpenPost: (String) -> Unit,
    onOpenProfile: (Int) -> Unit,
    onOpenThread: (String) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenFollowers: (Int, String) -> Unit,
    onOpenProductCenter: (() -> Unit)? = null
) {
    var currentSubTab by remember { mutableStateOf(CommunityInternalTab.FEED) }
    val unreadNotifs = notificationsViewModel.unreadCount

    LkPageLayout(
        title = "Topluluk",
        onBack = null,
        actions = {
            // Notification Bell Icon with Badge
            Box(modifier = Modifier.padding(end = 8.dp)) {
                IconButton(onClick = onOpenNotifications) {
                    Icon(Icons.Default.Notifications, contentDescription = "Bildirimler", tint = LkTextPrimary)
                }
                if (unreadNotifs > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 6.dp, end = 6.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(LkDanger),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (unreadNotifs > 9) "9+" else "$unreadNotifs",
                            style = LkTypography.getMicro(),
                            color = LkOnPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            if (onOpenProductCenter != null) {
                IconButton(onClick = onOpenProductCenter) {
                    Icon(Icons.Default.Apps, contentDescription = "Ürünler", tint = LkPrimary)
                }
            }
        }
    ) {
        Column(Modifier.fillMaxSize()) {
            // Top internal navigation tabs (Akış, Kişiler, Sohbetler, Profil)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LkSurfaceCanvas)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CommunityInternalTab.values().forEach { tab ->
                    val isSelected = tab == currentSubTab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(LkShapes.MD)
                            .background(if (isSelected) LkPrimary else LkSurfacePanel)
                            .border(1.dp, if (isSelected) LkPrimary else LkLineSoft, LkShapes.MD)
                            .clickable { currentSubTab = tab }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.title,
                            style = LkTypography.getMicro(),
                            color = if (isSelected) LkOnPrimary else LkTextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Divider(color = LkLineSoft)

            when (currentSubTab) {
                CommunityInternalTab.FEED -> {
                    FeedTabContent(
                        viewModel = communityViewModel,
                        onOpenPost = onOpenPost,
                        onOpenProfile = onOpenProfile
                    )
                }
                CommunityInternalTab.MEMBERS -> {
                    PeopleScreen(
                        viewModel = socialViewModel,
                        onOpenProfile = onOpenProfile
                    )
                }
                CommunityInternalTab.CHATS -> {
                    ThreadsScreen(
                        viewModel = threadsViewModel,
                        currentUserId = currentUserId,
                        onOpenThread = onOpenThread
                    )
                }
                CommunityInternalTab.PROFILE -> {
                    ProfileScreen(
                        userId = null, // Own profile
                        socialViewModel = socialViewModel,
                        communityViewModel = communityViewModel,
                        onBack = null,
                        onOpenFollowers = onOpenFollowers,
                        onOpenPost = onOpenPost,
                        onOpenProfile = onOpenProfile
                    )
                }
            }
        }
    }

    if (communityViewModel.composing) {
        ComposePostSheet(viewModel = communityViewModel)
    }
}

@Composable
private fun FeedTabContent(
    viewModel: CommunityViewModel,
    onOpenPost: (String) -> Unit,
    onOpenProfile: (Int) -> Unit
) {
    val feedState by viewModel.feedState.collectAsState()
    var reportingPostId by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize()) {
        // Feed Filter Pills
        LazyRow(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewModel.tabs.size) { index ->
                val (value, label) = viewModel.tabs[index]
                val isSelected = value == viewModel.selectedType
                Box(
                    modifier = Modifier
                        .clip(LkShapes.MD)
                        .background(if (isSelected) LkPrimarySoft else LkSurfaceSunken)
                        .border(1.dp, if (isSelected) LkPrimary else LkLineSoft, LkShapes.MD)
                        .clickable { viewModel.selectType(value) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        label,
                        style = LkTypography.getMicro(),
                        color = if (isSelected) LkPrimary else LkTextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        when (val s = feedState) {
            is CommunityViewModel.FeedUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LkPrimary)
                }
            }
            is CommunityViewModel.FeedUiState.Error -> {
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(s.message, color = LkDanger, style = LkTypography.getBody())
                    Spacer(Modifier.height(12.dp))
                    LkButton(text = "Tekrar Dene", variant = LkButtonVariant.SECONDARY, onClick = { viewModel.refreshFeed() })
                }
            }
            is CommunityViewModel.FeedUiState.Content -> {
                if (s.posts.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Henüz gönderi bulunmuyor", style = LkTypography.getBodyStrong(), color = LkTextPrimary)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "İlk gönderini paylaş, topluluğa katkıda bulun.",
                                style = LkTypography.getBodySmall(),
                                color = LkTextSecondary
                            )
                            Spacer(Modifier.height(16.dp))
                            LkButton(text = "Paylaşım Yap", onClick = { viewModel.startCompose() })
                        }
                    }
                } else {
                    Box(Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(s.posts, key = { it.id }) { post ->
                                PostFeedCard(
                                    post = post,
                                    onClick = { onOpenPost(post.id) },
                                    onAuthorClick = { post.author?.id?.let(onOpenProfile) },
                                    onLike = { viewModel.toggleLike(post.id, post.begendim) },
                                    onBookmark = { viewModel.toggleBookmark(post.id, post.kaydettim) },
                                    onQuote = { viewModel.startCompose(quoteOf = post) },
                                    onReply = { viewModel.startCompose(replyTo = post) },
                                    onReport = { reportingPostId = post.id }
                                )
                            }
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (s.loadingMore) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = LkPrimary)
                                    } else {
                                        LkButton(
                                            text = "Daha Fazla Yükle",
                                            variant = LkButtonVariant.SECONDARY,
                                            onClick = { viewModel.loadMore() },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }

                        // FAB to create post
                        FloatingActionButton(
                            onClick = { viewModel.startCompose() },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(20.dp),
                            backgroundColor = LkPrimary
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Gönderi Oluştur",
                                tint = LkOnPrimary
                            )
                        }
                    }
                }
            }
        }
    }

    // Report dialog
    reportingPostId?.let { postId ->
        PostReportDialog(
            onDismiss = { reportingPostId = null },
            onSubmit = { reason, details ->
                viewModel.reportPost(postId, reason, details)
                reportingPostId = null
            }
        )
    }
}

@Composable
fun PostFeedCard(
    post: CommunityPostDto,
    onClick: () -> Unit,
    onAuthorClick: () -> Unit,
    onLike: () -> Unit,
    onBookmark: () -> Unit,
    onQuote: () -> Unit,
    onReply: () -> Unit,
    onReport: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        backgroundColor = LkSurfacePanel,
        shape = LkShapes.MD,
        elevation = 0.dp
    ) {
        Column(Modifier.padding(14.dp)) {
            // Author Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (post.postType == "official") LkPrimary else LkPrimarySoft)
                        .clickable(onClick = onAuthorClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (post.postType == "official") "LK" else (post.author?.name?.take(1) ?: "U").uppercase(),
                        style = LkTypography.getMicro(),
                        color = if (post.postType == "official") LkOnPrimary else LkPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.width(10.dp))

                Column(Modifier.weight(1f).clickable(onClick = onAuthorClick)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (post.postType == "official") "Resmi Duyuru" else (post.author?.name ?: "Bilinmeyen"),
                            style = LkTypography.getBodyStrong(),
                            color = LkTextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (post.postType == "official") {
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.Verified, contentDescription = "Resmi", tint = LkPrimary, modifier = Modifier.size(14.dp))
                        }
                    }
                    Text(
                        post.publishedAt?.let { LkDateUtils.formatDateTime(it) } ?: "",
                        style = LkTypography.getMicro(),
                        color = LkTextMuted
                    )
                }

                if (onReport != null && post.postType != "official") {
                    IconButton(onClick = onReport, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Seçenekler", tint = LkTextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Post Text
            Text(
                post.summary,
                style = LkTypography.getBody(),
                color = LkTextPrimary,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )

            // Attached Media preview
            post.media?.let { media ->
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(LkShapes.MD)
                        .background(LkSurfaceSunken)
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (media.kind == "image") Icons.Default.Image else Icons.Default.InsertDriveFile,
                            contentDescription = null,
                            tint = LkPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            media.originalName ?: "Ekli dosya",
                            style = LkTypography.getMicro(),
                            color = LkTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Quoted Post Card
            post.quotedPost?.let { q ->
                Spacer(Modifier.height(8.dp))
                QuotedPostCard(quotedPost = q, onClick = onClick)
            }

            Spacer(Modifier.height(12.dp))

            // Interaction Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like
                Row(
                    modifier = Modifier
                        .clip(LkShapes.SM)
                        .clickable(onClick = onLike)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (post.begendim) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Beğen",
                        tint = if (post.begendim) LkDanger else LkTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${post.begeniSayisi}",
                        style = LkTypography.getMicro(),
                        color = if (post.begendim) LkDanger else LkTextSecondary
                    )
                }

                // Reply
                Row(
                    modifier = Modifier
                        .clip(LkShapes.SM)
                        .clickable(onClick = onReply)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Yanıtla",
                        tint = LkTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${post.yanitSayisi}",
                        style = LkTypography.getMicro(),
                        color = LkTextSecondary
                    )
                }

                // Quote
                Row(
                    modifier = Modifier
                        .clip(LkShapes.SM)
                        .clickable(onClick = onQuote)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = "Alıntıla",
                        tint = LkTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${post.alintiSayisi}",
                        style = LkTypography.getMicro(),
                        color = LkTextSecondary
                    )
                }

                // Bookmark
                IconButton(onClick = onBookmark, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (post.kaydettim) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Kaydet",
                        tint = if (post.kaydettim) LkPrimary else LkTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}