package com.localkarar.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mikepenz.markdown.m2.Markdown

@Composable
fun MarkdownViewer(
    content: String,
    modifier: Modifier = Modifier
) {
    Markdown(
        content = content,
        modifier = modifier.fillMaxWidth()
    )
}
