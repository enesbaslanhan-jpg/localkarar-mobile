package com.localkarar.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.localkarar.app.ui.theme.*

@Composable
fun LkLoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = LkPrimary)
            Spacer(modifier = Modifier.height(LkSpacing.Space4))
            Text(text = "Yükleniyor...", style = LkTypography.getBodySmall(), color = LkTextSecondary)
        }
    }
}

@Composable
fun LkErrorState(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(LkSpacing.Space8)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Hata",
                tint = LkDanger,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(LkSpacing.Space4))
            Text(
                text = "Bir Hata Oluştu",
                style = LkTypography.getSectionTitle(),
                color = LkTextPrimary
            )
            Spacer(modifier = Modifier.height(LkSpacing.Space2))
            Text(
                text = message,
                style = LkTypography.getBody(),
                color = LkTextSecondary,
                textAlign = TextAlign.Center
            )
            
            if (onRetry != null) {
                Spacer(modifier = Modifier.height(LkSpacing.Space8))
                LkButton(text = "Tekrar Dene", onClick = onRetry)
            }
        }
    }
}

