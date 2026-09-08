package com.localkarar.app.ui.screens.community

import com.localkarar.app.ui.components.LkLoadingDesen
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkCard
import com.localkarar.app.ui.components.LkAvatar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.localkarar.app.community.SocialViewModel
import com.localkarar.app.network.dto.PersonDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkHeroPage
import com.localkarar.app.ui.theme.*

@Composable
fun FollowersScreen(
    userId: Int,
    mode: String, // "followers" | "following"
    viewModel: SocialViewModel,
    onBack: () -> Unit,
    onOpenProfile: (Int) -> Unit
) {
    val followListState by viewModel.followListState.collectAsState()

    LaunchedEffect(userId, mode) {
        viewModel.loadFollowList(userId, mode)
    }

    LkHeroPage(
        title = if (mode == "followers") "Takipçiler" else "Takip Edilenler",
        onBack = onBack
    ) {
        when (val s = followListState) {
            is SocialViewModel.FollowListUiState.Loading -> {
                LkLoadingState(desen = LkLoadingDesen.LISTE)
            }
            is SocialViewModel.FollowListUiState.Error -> {
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(s.message, color = LkDanger, style = LkTypography.getBody())
                    Spacer(Modifier.height(12.dp))
                    LkButton(text = "Tekrar Dene", variant = LkButtonVariant.SECONDARY, onClick = { viewModel.loadFollowList(userId, mode) })
                }
            }
            is SocialViewModel.FollowListUiState.Content -> {
                if (s.people.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            if (mode == "followers") "Henüz takipçisi bulunmuyor." else "Henüz kimseyi takip etmiyor.",
                            style = LkTypography.getBodySmall(),
                            color = LkTextMuted
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(s.people, key = { it.id }) { person ->
                            FollowerRow(
                                person = person,
                                isFollowing = viewModel.followingIds.contains(person.id),
                                onClick = { onOpenProfile(person.id) },
                                onToggleFollow = { viewModel.toggleFollow(person.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FollowerRow(
    person: PersonDto,
    isFollowing: Boolean,
    onClick: () -> Unit,
    onToggleFollow: () -> Unit
) {
    /* §24: kenarlikli Material Card degil yukseltilmis LkCard. */
    LkCard(
        modifier = Modifier.clickable(onClick = onClick),
        padding = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LkAvatar(ad = person.name, avatarUrl = person.avatarUrl, boyut = 40.dp)

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    person.name,
                    style = LkTypography.getBodyStrong(),
                    color = LkTextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!person.bio.isNullOrBlank()) {
                    Text(
                        person.bio,
                        style = LkTypography.getMicro(),
                        color = LkTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            LkButton(
                text = if (isFollowing) "Takip Ediliyor" else "Takip Et",
                variant = if (isFollowing) LkButtonVariant.SECONDARY else LkButtonVariant.PRIMARY,
                onClick = onToggleFollow
            )
        }
    }
}
