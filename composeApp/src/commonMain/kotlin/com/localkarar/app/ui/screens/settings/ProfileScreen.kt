package com.localkarar.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.localkarar.app.auth.UserDto
import com.localkarar.app.core.rememberFilePicker
import com.localkarar.app.settings.SettingsViewModel
import com.localkarar.app.settings.roleLabel
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*

@Composable
fun ProfileScreen(
    viewModel: SettingsViewModel,
    user: UserDto?,
    onNewSession: (String, UserDto) -> Unit,
    onBack: () -> Unit
) {
    val currentUser = viewModel.user ?: user
    var isEditingName by remember { mutableStateOf(false) }

    val launchFilePicker = rememberFilePicker { file ->
        if (file != null) {
            viewModel.uploadAvatar(file.name, file.bytes, onNewSession)
        }
    }

    LkPageLayout(title = "Profil Bilgileri", onBack = onBack) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (currentUser != null) {
                // Avatar Block
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(LkSurfaceSignature)
                        .border(2.dp, LkLineStrong, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentUser.name.take(1).uppercase().ifBlank { "U" },
                        style = LkTypography.getPageTitle(),
                        color = LkPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (viewModel.avatarLoading) {
                    CircularProgressIndicator(color = LkPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { launchFilePicker() },
                            colors = ButtonDefaults.outlinedButtonColors(
                                backgroundColor = LkSurfacePanel,
                                contentColor = LkTextPrimary
                            ),
                            border = ButtonDefaults.outlinedBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(LkLineStrong))
                        ) {
                            Text("Fotoğrafı Değiştir", style = LkTypography.getBodySmall())
                        }
                        if (!currentUser.avatarUrl.isNullOrBlank()) {
                            OutlinedButton(
                                onClick = { viewModel.removeAvatar { _, u -> onNewSession("", u) } },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    backgroundColor = LkSurfacePanel,
                                    contentColor = LkDanger
                                ),
                                border = ButtonDefaults.outlinedBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(LkDanger.copy(alpha = 0.5f)))
                            ) {
                                Text("Kaldır", style = LkTypography.getBodySmall())
                            }
                        }
                    }
                }

                Text(
                    "Fotoğraf PNG veya JPEG, en fazla 5 MB olabilir.",
                    style = LkTypography.getMicro(),
                    color = LkTextSecondary,
                    textAlign = TextAlign.Center
                )

                // Info Cards
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(LkShapes.MD)
                        .border(1.dp, LkLineSoft, LkShapes.MD),
                    backgroundColor = LkSurfacePanel,
                    elevation = 0.dp
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Display Name
                        Column(
                            modifier = if (!isEditingName) Modifier.clickable { isEditingName = true } else Modifier
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Görünen Ad", style = LkTypography.getMicro(), color = LkTextSecondary)
                                if (!isEditingName) {
                                    IconButton(
                                        onClick = { isEditingName = true },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Düzenle",
                                            tint = LkPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                            if (isEditingName) {
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = viewModel.editName,
                                        onValueChange = { viewModel.onEditNameChange(it) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        textStyle = LkTypography.getBody()
                                    )
                                    if (viewModel.nameLoading) {
                                        CircularProgressIndicator(color = LkPrimary, modifier = Modifier.size(24.dp))
                                    } else {
                                        IconButton(
                                            onClick = {
                                                viewModel.updateDisplayName { updated ->
                                                    isEditingName = false
                                                    onNewSession("", updated)
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = "Kaydet", tint = LkSuccess)
                                        }
                                    }
                                }
                            } else {
                                Text(currentUser.name, style = LkTypography.getBodyStrong(), color = LkTextPrimary)
                            }
                        }

                        Divider(color = LkLineSoft)

                        // Email
                        Column {
                            Text("E-posta Adresi", style = LkTypography.getMicro(), color = LkTextSecondary)
                            Spacer(Modifier.height(2.dp))
                            Text(currentUser.email, style = LkTypography.getBodyStrong(), color = LkTextPrimary)
                        }

                        Divider(color = LkLineSoft)

                        // Role
                        Column {
                            Text("Hesap Rolü", style = LkTypography.getMicro(), color = LkTextSecondary)
                            Spacer(Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LkPrimary.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = roleLabel(currentUser.role),
                                    style = LkTypography.getMicro(),
                                    color = LkPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            viewModel.notice?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = if (viewModel.noticeIsError) LkDanger.copy(alpha = 0.15f) else LkSuccess.copy(alpha = 0.15f),
                    elevation = 0.dp
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = it,
                            style = LkTypography.getBodySmall(),
                            color = if (viewModel.noticeIsError) LkDanger else LkSuccess,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.clearNotice() }) {
                            Text("Tamam", color = LkTextPrimary)
                        }
                    }
                }
            }
        }
    }
}