package com.localkarar.app.ui.screens.community

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.community.CommunityViewModel
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.theme.*

@Composable
fun ComposePostSheet(
    viewModel: CommunityViewModel
) {
    AlertDialog(
        onDismissRequest = { viewModel.dismissCompose() },
        backgroundColor = LkSurfacePanel,
        title = { Text("Yeni Gönderi", style = LkTypography.getBodyStrong(), color = LkTextPrimary) },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = viewModel.titleInput,
                    onValueChange = { viewModel.onTitleChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Başlık (en az 5 karakter)") },
                    singleLine = true,
                    textStyle = LkTypography.getBody()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = viewModel.summaryInput,
                    onValueChange = { viewModel.onSummaryChange(it) },
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    label = { Text("İçerik (en az 20 karakter)") },
                    textStyle = LkTypography.getBody()
                )
                viewModel.notice?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, style = LkTypography.getBodySmall(), color = LkDanger)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.submitPost() }) {
                Text("Yayınla", color = LkPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.dismissCompose() }) { Text("Vazgeç", color = LkTextSecondary) }
        }
    )
}