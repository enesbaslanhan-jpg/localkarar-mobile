package com.localkarar.app.ui.screens.community

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.localkarar.app.community.CommunityViewModel
import com.localkarar.app.community.SocialViewModel
import com.localkarar.app.core.openExternalUrl
import com.localkarar.app.network.dto.CommunityPostDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*

@Composable
fun ProfileScreen(
    userId: Int? = null, // null = own profile, non-null = other user profile
    socialViewModel: SocialViewModel,
    communityViewModel: CommunityViewModel,
    onBack: (() -> Unit)? = null,
    onOpenFollowers: (Int, String) -> Unit,
    onOpenPost: (String) -> Unit,
    onOpenProfile: (Int) -> Unit
) {
    val isOwnProfile = userId == null
    val ownState by socialViewModel.ownProfileState.collectAsState()
    val otherState by socialViewModel.otherProfileState.collectAsState()

    var showReportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (isOwnProfile) {
            socialViewModel.loadOwnProfile()
        } else {
            socialViewModel.loadOtherProfile(userId!!)
        }
    }

    LkPageLayout(
        title = if (isOwnProfile) "Profilim" else "Profil",
        onBack = onBack
    ) {
        if (isOwnProfile) {
            when (val s = ownState) {
                is SocialViewModel.OwnProfileUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LkPrimary)
                    }
                }
                is SocialViewModel.OwnProfileUiState.Error -> {
                    Column(
                        Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(s.message, color = LkDanger, style = LkTypography.getBody())
                        Spacer(Modifier.height(12.dp))
                        LkButton(text = "Tekrar Dene", variant = LkButtonVariant.SECONDARY, onClick = { socialViewModel.loadOwnProfile() })
                    }
                }
                is SocialViewModel.OwnProfileUiState.Content -> {
                    OwnProfileContent(
                        state = s,
                        activeTab = socialViewModel.ownProfileTab,
                        onTabSelect = { socialViewModel.ownProfileTab = it },
                        onOpenFollowers = { onOpenFollowers(0, it) },
                        onOpenPost = onOpenPost,
                        onLike = { pId, liked -> communityViewModel.toggleLike(pId, liked) },
                        onBookmark = { pId, saved -> communityViewModel.toggleBookmark(pId, saved) },
                        onQuote = { p -> communityViewModel.startCompose(quoteOf = p) },
                        onReply = { p -> communityViewModel.startCompose(replyTo = p) }
                    )
                }
            }
        } else {
            when (val s = otherState) {
                is SocialViewModel.OtherProfileUiState.Loading, SocialViewModel.OtherProfileUiState.Idle -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LkPrimary)
                    }
                }
                is SocialViewModel.OtherProfileUiState.Error -> {
                    Column(
                        Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(s.message, color = LkDanger, style = LkTypography.getBody())
                        Spacer(Modifier.height(12.dp))
                        LkButton(text = "Tekrar Dene", variant = LkButtonVariant.SECONDARY, onClick = { socialViewModel.loadOtherProfile(userId!!) })
                    }
                }
                is SocialViewModel.OtherProfileUiState.Content -> {
                    OtherProfileContent(
                        state = s,
                        isFollowing = socialViewModel.followingIds.contains(s.profile.id),
                        isBlocked = socialViewModel.blockedIds.contains(s.profile.id),
                        activeTab = socialViewModel.otherProfileTab,
                        onTabSelect = { socialViewModel.otherProfileTab = it },
                        onToggleFollow = { socialViewModel.toggleFollow(s.profile.id) },
                        onToggleBlock = { socialViewModel.toggleBlock(s.profile.id) },
                        onReport = { showReportDialog = true },
                        onOpenFollowers = { onOpenFollowers(s.profile.id, it) },
                        onOpenPost = onOpenPost,
                        onLike = { pId, liked -> communityViewModel.toggleLike(pId, liked) },
                        onBookmark = { pId, saved -> communityViewModel.toggleBookmark(pId, saved) },
                        onQuote = { p -> communityViewModel.startCompose(quoteOf = p) },
                        onReply = { p -> communityViewModel.startCompose(replyTo = p) }
                    )
                }
            }
        }
    }

    if (showReportDialog && userId != null) {
        UserReportDialog(
            onDismiss = { showReportDialog = false },
            onSubmit = { reason, details ->
                socialViewModel.reportUser(userId, reason, details)
                showReportDialog = false
            }
        )
    }
}

@Composable
private fun OwnProfileContent(
    state: SocialViewModel.OwnProfileUiState.Content,
    activeTab: String,
    onTabSelect: (String) -> Unit,
    onOpenFollowers: (String) -> Unit,
    onOpenPost: (String) -> Unit,
    onLike: (String, Boolean) -> Unit,
    onBookmark: (String, Boolean) -> Unit,
    onQuote: (CommunityPostDto) -> Unit,
    onReply: (CommunityPostDto) -> Unit
) {
    val posts = when (activeTab) {
        "likes" -> state.likes
        "bookmarks" -> state.bookmarks
        else -> state.posts
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCounter(count = state.summary.paylasim, label = "Paylaşım", onClick = { onTabSelect("posts") })
                StatCounter(count = state.summary.takipci, label = "Takipçi", onClick = { onOpenFollowers("followers") })
                StatCounter(count = state.summary.takipEdilen, label = "Takip Edilen", onClick = { onOpenFollowers("following") })
            }

            Divider(color = LkLineSoft)

            // Tabs Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileTabPill(title = "Paylaşımlarım", count = state.summary.paylasim, isSelected = activeTab == "posts", onClick = { onTabSelect("posts") })
                ProfileTabPill(title = "Beğenilerim", count = state.summary.begeni, isSelected = activeTab == "likes", onClick = { onTabSelect("likes") })
                ProfileTabPill(title = "Kaydettiklerim", count = state.summary.kayit, isSelected = activeTab == "bookmarks", onClick = { onTabSelect("bookmarks") })
            }
        }

        if (posts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        when (activeTab) {
                            "likes" -> "Henüz beğendiğiniz bir gönderi yok."
                            "bookmarks" -> "Henüz kaydettiğiniz bir gönderi yok."
                            else -> "Henüz bir paylaşım yapmadınız."
                        },
                        style = LkTypography.getBodySmall(),
                        color = LkTextMuted
                    )
                }
            }
        } else {
            items(posts, key = { it.id }) { post ->
                Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    PostFeedCard(
                        post = post,
                        onClick = { onOpenPost(post.id) },
                        onAuthorClick = {},
                        onLike = { onLike(post.id, post.begendim) },
                        onBookmark = { onBookmark(post.id, post.kaydettim) },
                        onQuote = { onQuote(post) },
                        onReply = { onReply(post) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OtherProfileContent(
    state: SocialViewModel.OtherProfileUiState.Content,
    isFollowing: Boolean,
    isBlocked: Boolean,
    activeTab: String,
    onTabSelect: (String) -> Unit,
    onToggleFollow: () -> Unit,
    onToggleBlock: () -> Unit,
    onReport: () -> Unit,
    onOpenFollowers: (String) -> Unit,
    onOpenPost: (String) -> Unit,
    onLike: (String, Boolean) -> Unit,
    onBookmark: (String, Boolean) -> Unit,
    onQuote: (CommunityPostDto) -> Unit,
    onReply: (CommunityPostDto) -> Unit
) {
    val posts = if (activeTab == "media") state.mediaPosts else state.posts
    val profile = state.profile
    val sayilar = state.sayilar

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            // Profile Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                backgroundColor = LkSurfacePanel,
                shape = LkShapes.MD,
                elevation = 0.dp
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(LkPrimarySoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                (profile.name.take(1)).uppercase(),
                                style = LkTypography.getPageTitle(),
                                color = LkPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.width(16.dp))

                        Column(Modifier.weight(1f)) {
                            Text(profile.name, style = LkTypography.getSectionTitle(), color = LkTextPrimary, fontWeight = FontWeight.Bold)
                            profile.role?.let { role ->
                                Text(role, style = LkTypography.getMicro(), color = LkPrimary)
                            }
                        }
                    }

                    if (!profile.bio.isNullOrBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text(profile.bio, style = LkTypography.getBodySmall(), color = LkTextSecondary)
                    }

                    if (!profile.location.isNullOrBlank() || !profile.websiteUrl.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            profile.location?.let { loc ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = LkTextMuted, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(loc, style = LkTypography.getMicro(), color = LkTextMuted)
                                }
                                Spacer(Modifier.width(12.dp))
                            }
                            profile.websiteUrl?.let { web ->
                                Row(
                                    modifier = Modifier.clickable { openExternalUrl(web) },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Link, contentDescription = null, tint = LkPrimary, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(web, style = LkTypography.getMicro(), color = LkPrimary)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatCounter(count = sayilar.paylasim, label = "Paylaşım", onClick = { onTabSelect("posts") })
                        StatCounter(count = sayilar.takipci, label = "Takipçi", onClick = { onOpenFollowers("followers") })
                        StatCounter(count = sayilar.takipEdilen, label = "Takip Edilen", onClick = { onOpenFollowers("following") })
                    }

                    Spacer(Modifier.height(16.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isBlocked) {
                            LkButton(
                                text = "Engeli Kaldır",
                                variant = LkButtonVariant.SECONDARY,
                                onClick = onToggleBlock,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            LkButton(
                                text = if (isFollowing) "Takip Ediliyor" else "Takip Et",
                                variant = if (isFollowing) LkButtonVariant.SECONDARY else LkButtonVariant.PRIMARY,
                                onClick = onToggleFollow,
                                modifier = Modifier.weight(1f)
                            )
                            LkButton(
                                text = "Engelle",
                                variant = LkButtonVariant.SECONDARY,
                                onClick = onToggleBlock
                            )
                            IconButton(onClick = onReport) {
                                Icon(Icons.Default.Flag, contentDescription = "Şikayet Et", tint = LkTextMuted)
                            }
                        }
                    }
                }
            }

            // Tabs Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileTabPill(title = "Paylaşımlar", count = sayilar.paylasim, isSelected = activeTab == "posts", onClick = { onTabSelect("posts") })
                ProfileTabPill(title = "Medya", isSelected = activeTab == "media", onClick = { onTabSelect("media") })
            }
        }

        if (posts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Henüz paylaşım bulunmuyor.", style = LkTypography.getBodySmall(), color = LkTextMuted)
                }
            }
        } else {
            items(posts, key = { it.id }) { post ->
                Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    PostFeedCard(
                        post = post,
                        onClick = { onOpenPost(post.id) },
                        onAuthorClick = {},
                        onLike = { onLike(post.id, post.begendim) },
                        onBookmark = { onBookmark(post.id, post.kaydettim) },
                        onQuote = { onQuote(post) },
                        onReply = { onReply(post) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCounter(
    count: Int,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(LkShapes.SM)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("$count", style = LkTypography.getSectionTitle(), color = LkTextPrimary, fontWeight = FontWeight.Bold)
        Text(label, style = LkTypography.getMicro(), color = LkTextSecondary)
    }
}

@Composable
private fun ProfileTabPill(
    title: String,
    count: Int? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val text = if (count != null) "$title ($count)" else title
    Box(
        modifier = Modifier
            .clip(LkShapes.MD)
            .background(if (isSelected) LkPrimarySoft else LkSurfaceSunken)
            .border(1.dp, if (isSelected) LkPrimary else LkLineSoft, LkShapes.MD)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text,
            style = LkTypography.getMicro(),
            color = if (isSelected) LkPrimary else LkTextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
