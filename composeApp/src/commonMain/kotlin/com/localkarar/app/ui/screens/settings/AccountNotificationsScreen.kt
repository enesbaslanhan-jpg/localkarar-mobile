package com.localkarar.app.ui.screens.settings

import com.localkarar.app.ui.components.LkHeroPage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.settings.AccountNotificationDto
import com.localkarar.app.settings.AccountNotificationsUiState
import com.localkarar.app.settings.AccountNotificationsViewModel
import com.localkarar.app.ui.components.LkEmptyState
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.theme.*

/**
 * Hesap bildirimleri.
 *
 * Webdeki `/app/bildirimler` sayfasinin karsiligi; mobilde HIC YOKTU.
 *
 * Mobilde iki bildirim ekrani zaten vardi (calisma alani ve topluluk) ama
 * HESAP bildirimleri yoktu: uyelik uyarilari, sifre degisikligi bildirimleri
 * ve odeme sonuclari bu kanaldan geliyor.
 */
@Composable
fun AccountNotificationsScreen(
    viewModel: AccountNotificationsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.yukle() }

    /* §0: ham Scaffold + TopAppBar yerine hero kabugu. */
    LkHeroPage(
        title = "Bildirimler",
        onBack = onNavigateBack,
        actions = {
            val d = uiState
            if (d is AccountNotificationsUiState.Content && d.okunmamis > 0) {
                IconButton(onClick = { viewModel.tumunuOkunduIsaretle() }) {
                    Icon(
                        Icons.Outlined.DoneAll,
                        contentDescription = "Tümünü okundu işaretle",
                        tint = LkHero.OnHero
                    )
                }
            }
        },
        heroExtra = {
            val d = uiState
            if (d is AccountNotificationsUiState.Content && d.okunmamis > 0) {
                Text(
                    text = "${d.okunmamis} okunmamış",
                    style = LkTypography.getMetadata(),
                    color = LkHero.OnHeroSecondary,
                    modifier = Modifier.padding(
                        start = LkSpacing.Space5,
                        top = LkSpacing.Space2
                    )
                )
            }
        }
    ) {
        when (val durum = uiState) {
            is AccountNotificationsUiState.Loading ->
                LkLoadingState(modifier = Modifier)

            is AccountNotificationsUiState.Error ->
                LkErrorState(
                    message = durum.mesaj,
                    onRetry = { viewModel.yukle() },
                    modifier = Modifier,
                    hata = durum.hata
                )

            is AccountNotificationsUiState.Content -> {
                if (durum.bildirimler.isEmpty()) {
                    LkEmptyState(
                        title = "Bildirim yok",
                        description = "Hesabınızla ilgili bir gelişme olduğunda burada görürsünüz.",
                        icon = Icons.Outlined.NotificationsNone,
                        modifier = Modifier
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(LkSpacing.Space4),
                        verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)
                    ) {
                        items(durum.bildirimler, key = { it.id }) { bildirim ->
                            BildirimSatiri(bildirim)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BildirimSatiri(bildirim: AccountNotificationDto) {
    val okunmamis = bildirim.readAt == null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Okunmamis olan hafifce one cikiyor. Renk YERINE zemin farki
            // kullaniliyor: bildirimlerin cogu notr, kirmizi/sari bir vurgu
            // hepsini acil gosterirdi.
            .background(
                if (okunmamis) LkSurfaceRaised else LkSurfacePanel,
                RoundedCornerShape(8.dp)
            )
            .padding(LkSpacing.Space3)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = bildirim.title,
                style = if (okunmamis) LkTypography.getBodyStrong() else LkTypography.getBodySmall(),
                color = LkTextPrimary
            )
            if (!bildirim.body.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(LkSpacing.Space1))
                Text(
                    text = bildirim.body,
                    style = LkTypography.getBodySmall(),
                    color = LkTextSecondary
                )
            }
            if (!bildirim.createdAt.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(LkSpacing.Space1))
                Text(
                    text = bildirim.createdAt,
                    style = LkTypography.getMetadata(),
                    color = LkTextMuted
                )
            }
        }
    }
}
