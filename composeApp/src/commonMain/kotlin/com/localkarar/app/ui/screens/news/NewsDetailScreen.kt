package com.localkarar.app.ui.screens.news

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.core.openExternalUrl
import com.localkarar.app.news.NewsViewModel
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkInfoPanel
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*

@Composable
fun NewsDetailScreen(
    articleId: String,
    viewModel: NewsViewModel
) {
    val article = viewModel.articleById(articleId)

    LkPageLayout(title = "Haber Detayı", onBack = null) {
        if (article == null) {
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                LkInfoPanel(title = "Haber bulunamadı") {
                    Text("Haber listesini yenileyip tekrar deneyin.", style = LkTypography.getBodySmall())
                }
            }
            return@LkPageLayout
        }
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    categoryLabel(article.category),
                    style = LkTypography.getBodyStrong(),
                    color = LkPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                article.importance?.let {
                    Text(
                        importanceLabel(it),
                        style = LkTypography.getBodyStrong(),
                        color = importanceColor(it)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                article.title,
                style = LkTypography.getPageTitle(),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "${article.sourceName} • ${LkDateUtils.formatDateTime(article.sourcePublishedAt)}",
                style = LkTypography.getBodySmall(),
                color = LkTextSecondary
            )
            Spacer(Modifier.height(16.dp))
            article.summary?.let {
                Text(
                    it,
                    style = LkTypography.getBodyStrong(),
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(16.dp))
            }
            article.whyItMatters?.let {
                LkInfoPanel(title = "Neden önemli?") {
                    Text(it, style = LkTypography.getBody())
                }
                Spacer(Modifier.height(16.dp))
            }
            if (article.tags.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    article.tags.take(6).forEach { tag ->
                        Text(
                            "#$tag",
                            style = LkTypography.getMetadata(),
                            color = LkTextSecondary
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            article.canonicalUrl?.let { url ->
                LkButton(
                    text = "Kaynakta Aç",
                    onClick = { openExternalUrl(url) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}