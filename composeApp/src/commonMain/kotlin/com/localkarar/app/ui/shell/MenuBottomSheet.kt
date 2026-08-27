package com.localkarar.app.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.navigation.Destination
import com.localkarar.app.ui.theme.*

@Composable
fun MenuBottomSheet(
    firstName: String?,
    onNavigate: (Destination) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LkSurfacePanel, shape = LkShapes.LG)
            .padding(vertical = LkSpacing.Space6)
    ) {
        // User Info header
        Column(modifier = Modifier.padding(horizontal = LkSpacing.Space8, vertical = LkSpacing.Space4)) {
            Text(text = "Hesap", style = LkTypography.getMetadata(), color = LkTextSecondary)
            Spacer(modifier = Modifier.height(LkSpacing.Space2))
            Text(text = firstName ?: "Kullanıcı", style = LkTypography.getSectionTitle(), color = LkTextPrimary)
        }
        
        Divider(color = LkLineSoft, modifier = Modifier.padding(vertical = LkSpacing.Space2))
        
// Navigation Options
        MenuItem("Hesaplamalar") { onNavigate(Destination.Calculations) }
        MenuItem("İşletme Takibi") { onNavigate(Destination.Workspaces) }
        MenuItem("Haberler") { onNavigate(Destination.News) }
        MenuItem("Topluluk") { onNavigate(Destination.Community) }

        Divider(color = LkLineSoft, modifier = Modifier.padding(vertical = LkSpacing.Space2))


        MenuItem("Ayarlar") { onNavigate(Destination.Settings) }
        
        // Logout
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLogout() }
                .padding(horizontal = LkSpacing.Space8, vertical = LkSpacing.Space4)
        ) {
            Text(text = "Çıkış Yap", style = LkTypography.getBodyStrong(), color = LkDanger)
        }
    }
}

@Composable
private fun MenuItem(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = LkSpacing.Space8, vertical = LkSpacing.Space4)
    ) {
        Text(text = label, style = LkTypography.getBody(), color = LkTextPrimary)
    }
}



