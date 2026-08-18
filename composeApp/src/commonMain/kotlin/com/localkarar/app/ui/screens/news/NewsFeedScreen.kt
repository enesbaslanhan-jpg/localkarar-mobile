package com.localkarar.app.ui.screens.news

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
import androidx.compose.ui.graphics.Color
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

@Composable
fun NewsFeedScreen(
    viewModel: NewsViewModel,
    onOpenArticle: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LkPageLayout(title = "Haberler", onBack = null) {
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
                        Text(s.message, color = LkDanger)
                        Spacer(Modifier.height(12.dp))
                        LkButton(text = "Tekrar Dene", variant = LkButtonVariant.SECONDARY, onClick = { viewModel.refresh() })
                    }
                }
                is NewsViewModel.UiState.Content -> {
                    if (s.articles.isEmpty() && !s.loading) {
                        Column(
                            Modifier.fillMaxSize().padding(48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Bu kategoride haber yok", style = LkTypography.getBody())
                        }
                    } else {
                        LazyColumn(
                            Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = LkPrimary)
                                    }
                                }
                            } else if (viewModel.canLoadMore()) {
                                item {
                                    LkButton(
                                        text = "Daha Fazla Yükle",
                                        variant = LkButtonVariant.SECONDARY,
                                        onClick = { viewModel.loadMore() },
                                        modifier = Modifier.fillMaxWidth().padding(8.dp)
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
                    .background(if (isSelected) LkPrimary else LkSurfaceSunken, LkShapes.MD)
                    .clickable { onSelect(value) }
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
}

@Composable
private fun NewsCard(
    article: NewsArticleDto,
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
                    categoryLabel(article.category),
                    style = LkTypography.getMicro(),
                    color = LkPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    article.sourceName,
                    style = LkTypography.getMicro(),
                    color = LkTextSecondary
                )
                Spacer(Modifier.weight(1f))
                article.importance?.let {
                    Text(
                        importanceLabel(it),
                        style = LkTypography.getMicro(),
                        color = importanceColor(it)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                article.title,
                style = LkTypography.getBodyStrong(),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            article.summary?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    it,
                    style = LkTypography.getBodySmall(),
                    color = LkTextSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    LkDateUtils.formatDateTime(article.sourcePublishedAt),
                    style = LkTypography.getMicro(),
                    color = LkTextSecondary
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "Detay →",
                    style = LkTypography.getMicro(),
                    color = LkPrimary
                )
            }
        }
    }
}

fun categoryLabel(category: String): String {
    return when (category) {
        "FINANS" -> "Finans"
        "MEVZUAT" -> "Mevzuat"
        "VERGI" -> "Vergi"
        "IS_DUNYASI" -> "İş Dünyası"
        "DIJITALLESME" -> "Dijitalleşme"
        "DESTEK" -> "Destek"
        "GENEL_EKONOMI" -> "Genel Ekonomi"
        else -> category
    }
}

fun importanceLabel(importance: String): String {
    return when (importance) {
        "CRITICAL" -> "Kritik"
        "HIGH" -> "Yüksek"
        "MEDIUM" -> "Orta"
        "LOW" -> "Düşük"
        else -> importance
    }
}

fun importanceColor(importance: String): Color {
    return when (importance) {
        "CRITICAL" -> LkDanger
        "HIGH" -> LkWarning
        "MEDIUM" -> Color(0xFFF9A825)
        else -> LkTextMuted
    }
}