package com.localkarar.app.ui.screens.news

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
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
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkInfoPanel
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*

@Composable
fun NewsDetailScreen(
    articleId: String,
    viewModel: NewsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val article = viewModel.articleById(articleId)

    LkPageLayout(title = "Haber Detayı", onBack = onBack) {
        if (article == null && uiState is NewsViewModel.UiState.Loading) {
            LkLoadingState()
            return@LkPageLayout
        }

        if (article == null) {
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = LkWarning, modifier = Modifier.size(48.dp))
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
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

            // Metadata: Source & Published Date
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(LkShapes.MD)
                    .border(1.dp, LkLineSoft, LkShapes.MD),
                backgroundColor = LkSurfacePanel,
                elevation = 0.dp
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kaynak: ${article.sourceName}",
                        style = LkTypography.getMicro(),
                        color = LkTextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = LkDateUtils.formatDateTime(article.sourcePublishedAt),
                        style = LkTypography.getMicro(),
                        color = LkTextSecondary
                    )
                }
            }

            // Summary
            article.summary?.let { summary ->
                if (summary.isNotBlank()) {
                    Text(
                        text = summary,
                        style = LkTypography.getBody(),
                        color = LkTextPrimary,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // Why It Matters Panel
            article.whyItMatters?.let { why ->
                if (why.isNotBlank()) {
                    LkInfoPanel(title = "Neden Önemli?") {
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        article.tags.take(6).forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LkSurfacePanel)
                                    .border(1.dp, LkLineSoft, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "#$tag",
                                    style = LkTypography.getMicro(),
                                    color = LkPrimary
                                )
                            }
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