package com.localkarar.app.ui.screens.community

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.localkarar.app.community.CommunityViewModel
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*

@Composable
fun CommunityPostDetailScreen(
    postId: String,
    viewModel: CommunityViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val post = (uiState as? CommunityViewModel.UiState.Content)?.posts?.firstOrNull { it.id == postId }
    var reportDialog by remember { mutableStateOf(false) }

    LkPageLayout(title = "Gönderi", onBack = null) {
        if (post == null) {
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Gönderi bulunamadı", style = LkTypography.getBody())
            }
            return@LkPageLayout
        }
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    post.author?.name ?: "Bilinmeyen",
                    style = LkTypography.getBodyStrong(),
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Text(
                    post.publishedAt?.let { LkDateUtils.formatDateTime(it) } ?: "",
                    style = LkTypography.getBodySmall(),
                    color = LkTextSecondary
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                post.title,
                style = LkTypography.getPageTitle(),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(post.summary, style = LkTypography.getBody())
            Spacer(Modifier.height(24.dp))
            LkButton(
                text = "Şikayet Et",
                variant = LkButtonVariant.SECONDARY,
                onClick = { reportDialog = true },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (reportDialog) {
        ReportDialog(
            viewModel = viewModel,
            postId = post?.id ?: "",
            onDismiss = { reportDialog = false }
        )
    }
}

@Composable
private fun ReportDialog(
    viewModel: CommunityViewModel,
    postId: String,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("spam") }
    val reasons = listOf(
        "spam" to "Spam",
        "misinformation" to "Yanlış bilgi",
        "harassment" to "Taciz",
        "unsafe" to "Güvensiz içerik",
        "copyright" to "Telif hakkı",
        "other" to "Diğer"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        backgroundColor = LkSurfacePanel,
        title = { Text("Şikayet Et", style = LkTypography.getBodyStrong(), color = LkTextPrimary) },
        text = {
            Column {
                reasons.forEach { (value, label) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { reason = value }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (reason == value) "◉ " else "○ ",
                            color = LkPrimary
                        )
                        Text(label, style = LkTypography.getBody())
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                viewModel.reportPost(postId, reason, null)
                onDismiss()
            }) {
                Text("Gönder", color = LkPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Vazgeç", color = LkTextSecondary) }
        }
    )
}