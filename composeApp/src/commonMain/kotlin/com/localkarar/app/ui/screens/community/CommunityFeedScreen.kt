package com.localkarar.app.ui.screens.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.localkarar.app.community.CommunityViewModel
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.network.dto.CommunityPostDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*

@Composable
fun CommunityFeedScreen(
    viewModel: CommunityViewModel,
    onOpenPost: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LkPageLayout(title = "Topluluk", onBack = null) {
        Column(Modifier.fillMaxSize()) {
            LazyRow(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.tabs.size) { index ->
                    val (value, label) = viewModel.tabs[index]
                    val isSelected = value == viewModel.selectedType
                    Box(
                        modifier = Modifier
                            .background(if (isSelected) LkPrimary else LkSurfaceSunken, LkShapes.MD)
                            .clickable { viewModel.selectType(value) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            label,
                            style = LkTypography.getMicro(),
                            color = if (isSelected) LkOnPrimary else LkTextSecondary
                        )
                    }
                }
            }
            when (val s = uiState) {
                is CommunityViewModel.UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LkPrimary)
                    }
                }
                is CommunityViewModel.UiState.Error -> {
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
                is CommunityViewModel.UiState.Content -> {
                    if (s.posts.isEmpty()) {
                        Column(
                            Modifier.fillMaxSize().padding(48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Henüz gönderi yok", style = LkTypography.getBodyStrong())
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "İlk gönderini paylaş, topluluğa katkıda bulun.",
                                style = LkTypography.getBodySmall(),
                                color = LkTextSecondary
                            )
                            Spacer(Modifier.height(16.dp))
                            LkButton(text = "Gönderi Oluştur", onClick = { viewModel.startCompose() })
                        }
                    } else {
                        Box(Modifier.fillMaxSize()) {
                            LazyColumn(
                                Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(s.posts, key = { it.id }) { post ->
                                    PostCard(
                                        post = post,
                                        onClick = { onOpenPost(post.id) }
                                    )
                                }
                                item {
                                    LkButton(
                                        text = "Daha Fazla Yükle",
                                        variant = LkButtonVariant.SECONDARY,
                                        onClick = { viewModel.loadMore() },
                                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                                    )
                                }
                            }
                            FloatingActionButton(
                                onClick = { viewModel.startCompose() },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(20.dp),
                                backgroundColor = LkPrimary
                            ) {
                                Text("+", style = LkTypography.getSectionTitle(), color = LkOnPrimary)
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
}

@Composable
fun PostCard(
    post: CommunityPostDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        backgroundColor = LkSurfacePanel,
        elevation = 0.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (post.postType == "official") "Resmi" else "Topluluk",
                    style = LkTypography.getMicro(),
                    color = LkPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    post.author?.name ?: "Bilinmeyen",
                    style = LkTypography.getMicro(),
                    color = LkTextSecondary
                )
                Spacer(Modifier.weight(1f))
                Text(
                    post.publishedAt?.let { LkDateUtils.formatDateTime(it) } ?: "",
                    style = LkTypography.getMicro(),
                    color = LkTextSecondary
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                post.title,
                style = LkTypography.getBodyStrong(),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                post.summary,
                style = LkTypography.getBodySmall(),
                color = LkTextSecondary,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}