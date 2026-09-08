package com.localkarar.app.ui.screens.news

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.Launch
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.core.openExternalUrl
import com.localkarar.app.news.NewsViewModel
import com.localkarar.app.ui.components.LkChip
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkInfoPanel
import com.localkarar.app.ui.components.LkLoadingDesen
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkPageLayout
import androidx.compose.ui.unit.sp
import com.localkarar.app.ui.theme.*

@Composable
fun NewsDetailScreen(
    articleId: String,
    viewModel: NewsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val article = viewModel.articleById(articleId)
    val scrollState = rememberScrollState()

    LaunchedEffect(articleId) {
        scrollState.scrollTo(0)
    }

    LkPageLayout(title = "Haber", onBack = onBack) {
        if (article == null && uiState is NewsViewModel.UiState.Loading) {
            LkLoadingState(desen = LkLoadingDesen.DETAY)
            return@LkPageLayout
        }

        if (article == null) {
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Outlined.Warning, contentDescription = null, tint = LkWarning, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(12.dp))
                Text("Haber bulunamadı.", style = LkTypography.getBodyStrong(), color = LkTextPrimary)
                Spacer(Modifier.height(6.dp))
                Text("Haber listesini yenileyip tekrar deneyin.", style = LkTypography.getBodySmall(), color = LkTextSecondary)
                Spacer(Modifier.height(16.dp))
                LkButton(text = "Yenile", onClick = { viewModel.refresh() })
                Spacer(Modifier.height(8.dp))
                LkButton(text = "Haberlere Dön", onClick = onBack)
            }
            return@LkPageLayout
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(LkSpacing.Space5),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category & Importance Header
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
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        categoryLabel(article.category),
                        style = LkTypography.getBodyStrong(),
                        color = LkPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                article.importance?.let { imp ->
                    if (imp.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(importanceColor(imp).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
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

            // Headline
            Text(
                text = article.title,
                style = LkTypography.getPageTitle(),
                color = LkTextPrimary,
                fontWeight = FontWeight.Bold
            )

            /*
             * Kaynak ve tarih: mockup'ta bu satir KUTU DEGIL, basligin
             * altindaki ince ust bilgi satiri. Kenarlikli kart, okuma
             * ekranina gereksiz bir cerceve koyuyordu.
             */
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kaynak: ${article.sourceName}",
                    style = LkTypography.getMetadata(),
                    color = LkTextSecondary
                )
                Text(
                    text = LkDateUtils.formatDateTime(article.sourcePublishedAt),
                    style = LkTypography.getMetadata(),
                    color = LkTextMuted
                )
            }

            // Summary
            article.summary?.let { summary ->
                if (summary.isNotBlank()) {
                    Text(
                        text = summary,
                        style = LkTypography.getBody().copy(fontSize = 15.sp, lineHeight = 25.5.sp),
                        color = LkTextPrimary,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // Why It Matters Panel
            article.whyItMatters?.let { why ->
                if (why.isNotBlank()) {
                    LkInfoPanel(title = "SENİ NASIL ETKİLER") {
                        Text(
                            text = why,
                            style = LkTypography.getBodySmall(),
                            color = LkTextPrimary
                        )
                    }
                }
            }

            // Tags
            if (article.tags.isNotEmpty()) {
                Column {
                    Text("İlgili Etiketler", style = LkTypography.getMicro(), color = LkTextSecondary)
                    Spacer(Modifier.height(6.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(end = 8.dp)
                    ) {
                        items(article.tags.take(6)) { tag ->
                            /* Sistem hapi; kenarlikli 4dp kutu degil. */
                            LkChip(text = "#" + tag)
                        }
                    }
                }
            }

            // External Source Action
            val validUrl = article.canonicalUrl?.takeIf { it.startsWith("http://", ignoreCase = true) || it.startsWith("https://", ignoreCase = true) }
            if (validUrl != null) {
                Spacer(Modifier.height(8.dp))
                LkButton(
                    text = "Kaynakta Aç (${article.sourceName})",
                    onClick = { openExternalUrl(validUrl) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
