package com.localkarar.app.ui.screens.workspaces

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.ui.components.LkAvatar
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkChip
import com.localkarar.app.ui.components.LkEmptyState
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkHairline
import com.localkarar.app.ui.components.LkListRow
import com.localkarar.app.ui.components.LkRowGroup
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkHeroPage
import com.localkarar.app.ui.components.LkSectionHeader
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.TeamUiState
import com.localkarar.app.workspaces.TeamViewModel

private val INVITE_ROLES = listOf("manager", "staff", "accountant", "viewer")

@Composable
fun TeamScreen(
    viewModel: TeamViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showInviteDialog by remember { mutableStateOf(false) }
    var actionError by remember { mutableStateOf<String?>(null) }

    LkHeroPage(title = "Ekip", onBack = onBack) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (actionError != null) {
                Text(
                    text = actionError!!,
                    color = LkDanger,
                    style = LkTypography.getBodySmall(),
                    modifier = Modifier.padding(horizontal = LkSpacing.Space4, vertical = LkSpacing.Space2)
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                when (val state = uiState) {
                    is TeamUiState.Loading -> LkLoadingState()
                    is TeamUiState.Error -> LkErrorState(
                        message = state.message,
                        onRetry = { viewModel.load() }
                    )
                    is TeamUiState.Content -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(LkSpacing.Space4),
                            verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
                        ) {
                            item {
                                LkButton(
                                    text = "Üye Davet Et",
                                    onClick = { showInviteDialog = true },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            item { LkSectionHeader(title = "Üyeler", subtitle = "${state.members.size} üye") }
                            /* Tek yukseltilmis yuzey; her uye kendi kartinda degil. */
                            item {
                                LkRowGroup {
                                    state.members.forEachIndexed { i, member ->
                                        MemberCard(
                                            name = member.name,
                                            email = member.email,
                                            role = member.role,
                                            status = member.status,
                                            joinedAt = member.joinedAt,
                                            onRoleChange = { newRole ->
                                                actionError = null
                                                viewModel.changeRole(member.id, newRole) { actionError = it }
                                            },
                                            onRemove = {
                                                actionError = null
                                                viewModel.removeMember(member.id) { actionError = it }
                                            }
                                        )
                                        if (i != state.members.lastIndex) LkHairline()
                                    }
                                }
                            }

                            if (state.invitations.isNotEmpty()) {
                                item { LkSectionHeader(title = "Bekleyen Davetler") }
                                item {
                                    LkRowGroup {
                                        state.invitations.forEachIndexed { i, invitation ->
                                            InvitationCard(
                                                email = invitation.email,
                                                role = invitation.role,
                                                createdAt = invitation.createdAt,
                                                onCancel = {
                                                    actionError = null
                                                    viewModel.cancelInvitation(invitation.id) { actionError = it }
                                                }
                                            )
                                            if (i != state.invitations.lastIndex) LkHairline()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showInviteDialog) {
        InviteDialog(
            isInviting = (uiState as? TeamUiState.Content)?.isInviting == true,
            onDismiss = { showInviteDialog = false },
            onInvite = { email, role ->
                actionError = null
                viewModel.invite(email, role) { actionError = it }
                showInviteDialog = false
            }
        )
    }
}

@Composable
private fun MemberCard(
    name: String,
    email: String,
    role: String,
    status: String,
    joinedAt: String?,
    onRoleChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    var showRoleDialog by remember { mutableStateOf(false) }
    var showRemoveConfirm by remember { mutableStateOf(false) }

    /*
     * §24 satir anatomisi. Onceden her uye kenarlikli bir karttaydi ve rol
     * bir IconButton'in icine sikismis duz metindi -- tiklanabilir oldugu
     * anlasilmiyordu.
     *
     * Artik rol dikey ayracli kategori sutununda ve SATIRIN KENDISI rol
     * degistirme kutusunu aciyor. Cikarma dugmesi sagda ayri duruyor.
     *
     * Daveti kabul etmemis uye "Davet bekliyor" diye KELIMEYLE belirtiliyor;
     * status alani onceden hic gosterilmiyordu.
     */
    val altSatir = listOfNotNull(
        email,
        if (status != "active") "Davet bekliyor"
        else joinedAt?.let { "Katılım: ${LkDateUtils.formatDate(it)}" }
    ).joinToString(" · ")

    LkListRow(
        baslik = name,
        altBaslik = altSatir,
        kategori = roleLabel(role),
        onClick = { showRoleDialog = true },
        ikon = { LkAvatar(ad = name, boyut = 44.dp) },
        sag = {
            IconButton(onClick = { showRemoveConfirm = true }) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Çıkar",
                    tint = LkTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    )

    if (showRoleDialog) {
        androidx.compose.material.AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            backgroundColor = LkSurfacePanel,
            title = {
                Text(text = "Rol Değiştir", style = LkTypography.getBodyStrong(), color = LkTextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                    INVITE_ROLES.forEach { candidate ->
                        LkButton(
                            text = roleLabel(candidate) + if (candidate == role) " (mevcut)" else "",
                            variant = if (candidate == role) LkButtonVariant.SECONDARY else LkButtonVariant.QUIET,
                            onClick = {
                                if (candidate != role) onRoleChange(candidate)
                                showRoleDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                LkButton(text = "Vazgeç", variant = LkButtonVariant.QUIET, onClick = { showRoleDialog = false })
            }
        )
    }

    if (showRemoveConfirm) {
        androidx.compose.material.AlertDialog(
            onDismissRequest = { showRemoveConfirm = false },
            backgroundColor = LkSurfacePanel,
            title = {
                Text(text = "Üyeyi Çıkar", style = LkTypography.getBodyStrong(), color = LkTextPrimary)
            },
            text = {
                Text(
                    text = "$name ekipten çıkarılacak. Devam etmek istiyor musunuz?",
                    style = LkTypography.getBodySmall(),
                    color = LkTextSecondary
                )
            },
            confirmButton = {
                LkButton(text = "Evet, Çıkar", onClick = {
                    showRemoveConfirm = false
                    onRemove()
                })
            },
            dismissButton = {
                LkButton(text = "Vazgeç", variant = LkButtonVariant.QUIET, onClick = { showRemoveConfirm = false })
            }
        )
    }
}

@Composable
private fun InvitationCard(
    email: String,
    role: String,
    createdAt: String?,
    onCancel: () -> Unit
) {
    LkListRow(
        baslik = email,
        altBaslik = createdAt?.let { "Davet: ${LkDateUtils.formatDate(it)}" },
        kategori = roleLabel(role),
        ikon = {
            Icon(
                imageVector = Icons.Outlined.Mail,
                contentDescription = null,
                tint = LkWarning,
                modifier = Modifier.size(21.dp)
            )
        },
        sag = {
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "İptal",
                    tint = LkTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    )
}

@Composable
private fun InviteDialog(
    isInviting: Boolean,
    onDismiss: () -> Unit,
    onInvite: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("staff") }
    var error by remember { mutableStateOf<String?>(null) }

    androidx.compose.material.AlertDialog(
        onDismissRequest = { if (!isInviting) onDismiss() },
        backgroundColor = LkSurfacePanel,
        title = {
            Text(text = "Üye Davet Et", style = LkTypography.getBodyStrong(), color = LkTextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)) {
                if (error != null) {
                    Text(text = error!!, style = LkTypography.getBodySmall(), color = LkDanger)
                }
                LkTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "E-posta",
                    placeholder = "ornek@sirket.com"
                )
                Text(
                    text = "Rol",
                    style = LkTypography.getBodyStrong().copy(color = LkTextSecondary)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                    INVITE_ROLES.forEach { candidate ->
                        LkChip(
                            text = roleLabel(candidate),
                            background = if (role == candidate) LkPrimary else LkSurfaceRaised,
                            contentColor = if (role == candidate) LkOnPrimary else LkTextSecondary,
                            modifier = Modifier.clickable { role = candidate }
                        )
                    }
                }
            }
        },
        confirmButton = {
            LkButton(
                text = if (isInviting) "Gönderiliyor..." else "Davet Gönder",
                enabled = email.isNotBlank() && !isInviting,
                onClick = {
                    if (!email.contains("@")) {
                        error = "Geçerli bir e-posta girin"
                    } else {
                        error = null
                        onInvite(email, role)
                    }
                }
            )
        },
        dismissButton = {
            LkButton(text = "Vazgeç", variant = LkButtonVariant.QUIET, onClick = onDismiss)
        }
    )
}

fun roleLabel(role: String): String {
    return when (role) {
        "owner" -> "Sahip"
        "manager" -> "Yönetici"
        "staff" -> "Personel"
        "accountant" -> "Muhasebe"
        "viewer" -> "İzleyici"
        else -> role
    }
}