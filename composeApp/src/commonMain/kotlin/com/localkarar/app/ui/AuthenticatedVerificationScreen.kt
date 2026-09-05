package com.localkarar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.localkarar.app.auth.AuthViewModel
import com.localkarar.app.auth.UserDto
import com.localkarar.app.ui.components.LkAvatar
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.theme.LkSpacing
import com.localkarar.app.ui.theme.LkSurfaceCanvas
import com.localkarar.app.ui.theme.LkTextPrimary
import com.localkarar.app.ui.theme.LkTextSecondary
import com.localkarar.app.ui.theme.LkTypography
import androidx.compose.ui.unit.dp

/**
 * Oturum dogrulama ekrani.
 *
 * 🔴 §0 IHLALI DUZELTILDI: `MaterialTheme.typography.h4/body1/body2` ve ham
 * `Button` kullaniyordu. Material kendi olcegini getiriyordu; ekran
 * uygulamanin geri kalaniyla ayni yazi tipini bile paylasmiyordu.
 *
 * Cikis yap YIKICI bir islem degil ama geri donusu kullanicidan yeniden
 * giris ister; bu yuzden birincil degil ikincil dugme.
 */
@Composable
fun AuthenticatedVerificationScreen(user: UserDto, viewModel: AuthViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LkSurfaceCanvas)
            .padding(LkSpacing.Space6),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LkAvatar(
            ad = user.name,
            avatarUrl = user.avatarUrl,
            boyut = 72.dp
        )

        Spacer(Modifier.height(LkSpacing.Space5))

        Text(
            text = user.name,
            style = LkTypography.getTitleL(),
            color = LkTextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(LkSpacing.Space2))

        Text(
            text = user.email,
            style = LkTypography.getBodyM(),
            color = LkTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(LkSpacing.Space8))

        LkButton(
            text = "Çıkış yap",
            variant = LkButtonVariant.SECONDARY,
            onClick = { viewModel.logout() },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
