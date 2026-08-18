package com.localkarar.app.ui.screens.workspaces

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.network.dto.BusinessContactDto
import com.localkarar.app.network.dto.ContactInputDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.components.LkChip
import com.localkarar.app.ui.components.LkEmptyState
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.components.LkSectionHeader
import com.localkarar.app.ui.components.LkTextField
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.ContactsUiState
import com.localkarar.app.workspaces.ContactsViewModel

private val CONTACT_TYPES = listOf("customer", "supplier", "partner", "other")

@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var editing by remember { mutableStateOf<BusinessContactDto?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var actionError by remember { mutableStateOf<String?>(null) }

    LkPageLayout(title = "Kişiler", onBack = onBack) {
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
                    is ContactsUiState.Loading -> LkLoadingState()
                    is ContactsUiState.Error -> LkErrorState(
                        message = state.message,
                        onRetry = { viewModel.load() }
                    )
                    is ContactsUiState.Content -> {
                        if (state.contacts.isEmpty()) {
                            LkEmptyState(
                                title = "Henüz kişi yok",
                                description = "Müşteri, tedarikçi ve iş ortaklarınızı ekleyin.",
                                icon = Icons.Default.Contacts,
                                action = {
                                    LkButton(text = "Kişi Ekle", onClick = { showCreate = true })
                                }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(LkSpacing.Space4),
                                verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
                            ) {
                                item {
                                    LkButton(
                                        text = "Yeni Kişi",
                                        onClick = { showCreate = true },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                items(state.contacts, key = { it.id }) { contact ->
                                    ContactCard(
                                        contact = contact,
                                        onEdit = { editing = contact },
                                        onDelete = {
                                            actionError = null
                                            viewModel.delete(contact.id) { actionError = it }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreate || editing != null) {
        ContactEditDialog(
            contact = editing,
            isSaving = (uiState as? ContactsUiState.Content)?.isSaving == true,
            onDismiss = {
                showCreate = false
                editing = null
            },
            onSave = { input ->
                actionError = null
                viewModel.save(editing?.id, input) { actionError = it }
                showCreate = false
                editing = null
            }
        )
    }
}

@Composable
private fun ContactCard(
    contact: BusinessContactDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LkSurfacePanel, LkShapes.MD)
            .border(1.dp, LkLineStrong, LkShapes.MD)
            .padding(LkSpacing.PadPanel)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = LkTypography.getBodyStrong(),
                    color = LkTextPrimary
                )
                if (!contact.contactPerson.isNullOrBlank()) {
                    Text(
                        text = contact.contactPerson,
                        style = LkTypography.getMetadata(),
                        color = LkTextSecondary
                    )
                }
            }
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Düzenle",
                    tint = LkTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = LkTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(LkSpacing.Space2))
        LkChip(text = contactTypeLabel(contact.type))
        if (!contact.phone.isNullOrBlank() || !contact.email.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(LkSpacing.Space2))
            Text(
                text = listOfNotNull(contact.phone, contact.email).joinToString(" • "),
                style = LkTypography.getMetadata(),
                color = LkTextMuted
            )
        }
    }
    if (showDeleteConfirm) {
        androidx.compose.material.AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            backgroundColor = LkSurfacePanel,
            title = {
                Text(text = "Kişiyi Sil", style = LkTypography.getBodyStrong(), color = LkTextPrimary)
            },
            text = {
                Text(
                    text = "\"${contact.name}\" kişisi silinecek. Devam etmek istiyor musunuz?",
                    style = LkTypography.getBodySmall(),
                    color = LkTextSecondary
                )
            },
            confirmButton = {
                LkButton(text = "Evet, Sil", onClick = {
                    showDeleteConfirm = false
                    onDelete()
                })
            },
            dismissButton = {
                LkButton(text = "Vazgeç", variant = LkButtonVariant.QUIET, onClick = { showDeleteConfirm = false })
            }
        )
    }
}

@Composable
private fun ContactEditDialog(
    contact: BusinessContactDto?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (ContactInputDto) -> Unit
) {
    var type by remember(contact?.id) { mutableStateOf(contact?.type ?: "customer") }
    var name by remember(contact?.id) { mutableStateOf(contact?.name ?: "") }
    var legalName by remember(contact?.id) { mutableStateOf(contact?.legalName ?: "") }
    var contactPerson by remember(contact?.id) { mutableStateOf(contact?.contactPerson ?: "") }
    var email by remember(contact?.id) { mutableStateOf(contact?.email ?: "") }
    var phone by remember(contact?.id) { mutableStateOf(contact?.phone ?: "") }
    var city by remember(contact?.id) { mutableStateOf(contact?.city ?: "") }
    var address by remember(contact?.id) { mutableStateOf(contact?.address ?: "") }
    var notes by remember(contact?.id) { mutableStateOf(contact?.notes ?: "") }
    var error by remember(contact?.id) { mutableStateOf<String?>(null) }

    androidx.compose.material.AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        backgroundColor = LkSurfacePanel,
        title = {
            Text(
                text = if (contact == null) "Yeni Kişi" else "Kişiyi Düzenle",
                style = LkTypography.getBodyStrong(),
                color = LkTextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(LkSpacing.Space3)
            ) {
                if (error != null) {
                    Text(text = error!!, style = LkTypography.getBodySmall(), color = LkDanger)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(LkSpacing.Space2)) {
                    CONTACT_TYPES.forEach { candidate ->
                        LkChip(
                            text = contactTypeLabel(candidate),
                            background = if (type == candidate) LkPrimary else LkSurfaceRaised,
                            contentColor = if (type == candidate) LkOnPrimary else LkTextSecondary,
                            modifier = Modifier.clickable { type = candidate }
                        )
                    }
                }
                LkTextField(value = name, onValueChange = { name = it }, label = "Ad", placeholder = "Örn: ABC Dağıtım")
                LkTextField(value = legalName, onValueChange = { legalName = it }, label = "Resmi Unvan", placeholder = "İsteğe bağlı")
                LkTextField(value = contactPerson, onValueChange = { contactPerson = it }, label = "İlgili Kişi", placeholder = "İsteğe bağlı")
                LkTextField(value = email, onValueChange = { email = it }, label = "E-posta", placeholder = "İsteğe bağlı")
                LkTextField(value = phone, onValueChange = { phone = it }, label = "Telefon", placeholder = "İsteğe bağlı")
                LkTextField(value = city, onValueChange = { city = it }, label = "Şehir", placeholder = "İsteğe bağlı")
                LkTextField(value = address, onValueChange = { address = it }, label = "Adres", placeholder = "İsteğe bağlı")
                LkTextField(value = notes, onValueChange = { notes = it }, label = "Notlar", placeholder = "İsteğe bağlı")
            }
        },
        confirmButton = {
            LkButton(
                text = if (isSaving) "Kaydediliyor..." else "Kaydet",
                enabled = name.isNotBlank() && !isSaving,
                onClick = {
                    if (name.isBlank()) {
                        error = "Ad gerekli"
                    } else {
                        error = null
                        onSave(
                            ContactInputDto(
                                type = type,
                                name = name.trim(),
                                legalName = legalName.trim().ifBlank { null },
                                contactPerson = contactPerson.trim().ifBlank { null },
                                email = email.trim().ifBlank { null },
                                phone = phone.trim().ifBlank { null },
                                city = city.trim().ifBlank { null },
                                address = address.trim().ifBlank { null },
                                notes = notes.trim().ifBlank { null }
                            )
                        )
                    }
                }
            )
        },
        dismissButton = {
            LkButton(text = "Vazgeç", variant = LkButtonVariant.QUIET, onClick = onDismiss)
        }
    )
}

fun contactTypeLabel(type: String): String {
    return when (type) {
        "customer" -> "Müşteri"
        "supplier" -> "Tedarikçi"
        "partner" -> "İş Ortağı"
        else -> "Diğer"
    }
}