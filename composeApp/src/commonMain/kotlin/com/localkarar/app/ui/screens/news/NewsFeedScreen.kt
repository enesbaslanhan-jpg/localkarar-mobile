package com.localkarar.app.ui.screens.news

import androidx.compose.runtime.Composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.network.dto.NewsArticleDto
import com.localkarar.app.news.NewsViewModel
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*

fun categoryIcon(category: String?): ImageVector {
    return when (category?.uppercase()) {
        "FINANS" -> Icons.Outlined.AccountBalance
        "MEVZUAT" -> Icons.Outlined.Gavel
        "VERGI" -> Icons.Outlined.Receipt
        "IS_DUNYASI" -> Icons.Outlined.Business
        "DIJITALLESME" -> Icons.Outlined.Smartphone
        "DESTEK" -> Icons.Outlined.Handshake
        "GENEL_EKONOMI" -> Icons.Outlined.TrendingUp
        else -> Icons.Outlined.Article
    }
}

fun categoryLabel(category: String?): String {
    return when (category?.uppercase()) {
        "FINANS" -> "Finans"
        "MEVZUAT" -> "Mevzuat"
        "VERGI" -> "Vergi"
        "IS_DUNYASI" -> "İş Dünyası"
        "DIJITALLESME" -> "Dijitalleşme"
        "DESTEK" -> "Destek"
        "GENEL_EKONOMI" -> "Genel Ekonomi"
        else -> category ?: "Genel"
    }
}

fun importanceLabel(importance: String?): String {
    return when (importance?.uppercase()) {
        "CRITICAL" -> "Kritik"
        "HIGH" -> "Yüksek Öncelik"
        "MEDIUM" -> "Önemli"
        "LOW" -> "Bilgi"
        else -> importance ?: ""
    }
}

@Composable
fun importanceColor(importance: String?): Color {
    return when (importance?.uppercase()) {
        "CRITICAL" -> LkDanger
        "HIGH" -> Color(0xFFF97316) // Orange
        "MEDIUM" -> LkWarning
        "LOW" -> LkTextSecondary
        else -> LkTextSecondary
    }
}

@Composable
fun NewsFeedScreen(
    viewModel: NewsViewModel,
    onOpenArticle: (String) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    LkPageLayout(title = "Haberler", onBack = onBack) {
        Column(Modifier.fillMaxSize()) {
            CategoryRow(
                categories = viewModel.categories,
                selected = viewModel.selectedCategory,
                onSelect = { viewModel.selectCategory(it) }
            )
            when (val s = uiState) {
                is NewsViewModel.UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LkPrimary)
                    }
                }
                is NewsViewModel.UiState.Error -> {
                    Column(
                        Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(s.message, color = LkDanger, style = LkTypography.getBody())
                        Spacer(Modifier.height(12.dp))
                        LkButton(text = "Tekrar Dene", variant = LkButtonVariant.SECONDARY, onClick = { viewModel.refresh() })
                    }
                }
                is NewsViewModel.UiState.Content -> {
                    if (s.articles.isEmpty() && !s.loading) {
                        Column(
                            Modifier.fillMaxSize().padding(48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Outlined.Article, contentDescription = null, tint = LkTextSecondary, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("Bu kategoride henüz haber bulunmuyor.", style = LkTypography.getBody(), color = LkTextSecondary)
                        }
                    } else {
                        LazyColumn(
                            Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(s.articles, key = { it.id }) { article ->
                                NewsCard(
                                    article = article,
                                    onClick = { onOpenArticle(article.id) }
                                )
                            }
                            if (s.loadingMore) {
                                item {
                                    Row(
                                        Modifier.fillMaxWidth().padding(8.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp, color = LkPrimary)
                                    }
                                }
                            } else if (viewModel.canLoadMore()) {
                                item {
                                    LkButton(
                                        text = "Daha Fazla Yükle",
                                        variant = LkButtonVariant.SECONDARY,
                                        onClick = { viewModel.loadMore() },
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(
    categories: List<Pair<String?, String>>,
    selected: String?,
    onSelect: (String?) -> Unit
) {
    LazyRow(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories.size) { index ->
            val (value, label) = categories[index]
            val isSelected = value == selected
            Box(
                modifier = Modifier
                    .clip(LkShapes.MD)
                    .background(if (isSelected) LkPrimary else LkSurfacePanel)
                    .border(
                        1.dp,
                        if (isSelected) LkPrimary else LkLineSoft,
                        LkShapes.MD
                    )
                    .clickable { onSelect(value) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = categoryIcon(value),
                        contentDescription = null,
                        tint = if (isSelected) LkOnPrimary else LkTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        label,
                        style = LkTypography.getMicro(),
                        color = if (isSelected) LkOnPrimary else LkTextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun NewsCard(
    article: NewsArticleDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LkShapes.MD)
            .border(1.dp, LkLineSoft, LkShapes.MD)
            .clickable(onClick = onClick),
        backgroundColor = LkSurfacePanel,
        elevation = 0.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = categoryIcon(article.category),
                        contentDescription = null,
                        tint = LkPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        categoryLabel(article.category),
                        style = LkTypography.getMicro(),
                        color = LkPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                article.importance?.let { imp ->
                    if (imp.isNotBlank() && imp.uppercase() != "LOW") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(importanceColor(imp).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                importanceLabel(imp),
                                style = LkTypography.getMicro(),
                                color = importanceColor(imp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                article.title,
                style = LkTypography.getBodyStrong(),
                color = LkTextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            article.summary?.let { summary ->
                if (summary.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        summary,
                        style = LkTypography.getBodySmall(),
                        color = LkTextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${article.sourceName} • ${LkDateUtils.formatDateTime(article.sourcePublishedAt)}",
                    style = LkTypography.getMicro(),
                    color = LkTextSecondary
                )
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Detay",
                    tint = LkTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}