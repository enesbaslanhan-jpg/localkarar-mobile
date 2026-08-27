package com.localkarar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.material.IconButton
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.*

@Composable
fun LkPageLayout(
    title: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        backgroundColor = LkSurfaceCanvas,
        topBar = {
            if (title != null) {
                Surface(
                    color = LkSurfaceCanvas,
                    elevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                        TopAppBar(
                            title = { Text(title, style = LkTypography.getSectionTitle()) },
                            backgroundColor = LkSurfaceCanvas,
                            contentColor = LkTextPrimary,
                            elevation = 0.dp,
                            navigationIcon = if (onBack != null) {
                                {
                                    IconButton(onClick = onBack) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = LkPrimary)
                                    }
                                }
                            } else null,
                            actions = actions
                        )
                        Divider(color = LkLineSoft, thickness = 1.dp)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(LkSurfaceCanvas)
        ) {
            content()
        }
    }
}
