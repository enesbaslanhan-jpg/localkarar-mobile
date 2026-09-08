package com.localkarar.app.ui.screens.workspaces

import com.localkarar.app.ui.components.LkAvatar
import com.localkarar.app.ui.components.LkHairline
import com.localkarar.app.ui.components.LkRowGroup
import com.localkarar.app.ui.components.LkListRow
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
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
import com.localkarar.app.ui.components.LkHeroPage
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

    LkHeroPage(
        title = "Kişiler",
        onBack = onBack,
        actions = {
            IconButton(onClick = { showCreate = true }) {
                Icon(Icons.Outlined.Add, contentDescription = "Kişi ekle", tint = LkHero.OnHero)
            }
        },
        heroExtra = {
            val contacts = (uiState as? ContactsUiState.Content)?.contacts.orEmpty()
            if (contacts.isNotEmpty()) {
                Text(
                    text = "${contacts.size} kişi · ${contacts.count { it.type == "customer" }} müşteri · ${contacts.count { it.type == "supplier" }} tedarikçi",
                    style = LkTypography.getMetadata(), color = LkHero.OnHeroSecondary,
                    modifier = Modifier.padding(start = LkSpacing.Space5, top = LkSpacing.Space2)
                )
            }
        }
    ) {
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
                                icon = Icons.Outlined.Contacts,
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
                                /* Tek yukseltilmis yuzey; kart yigini degil. */
                                item {
                                    LkRowGroup {
                                        state.contacts.forEachIndexed { i, contact ->
                                            ContactCard(
                                                contact = contact,
                                                onEdit = { editing = contact },
                                                onDelete = {
                                                    actionError = null
                                                    viewModel.delete(contact.id) { actionError = it }
                                                }
                                            )
                                            if (i != state.contacts.lastIndex) LkHairline()
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

    /*
     * §24 satir anatomisi. Onceden her kisi kendi kenarlikli kartindaydi;
     * on kisilik liste on ayri cerceve oluyordu.
     *
     * Avatar bas harften; kisilerin fotografi yok, uydurma gorsel konmuyor.
     * Rol (Musteri / Tedarikci) dikey ayracli kategori sutununda.
     */
    Column(Modifier.fillMaxWidth()) {
        LkListRow(
            baslik = contact.name,
            altBaslik = listOfNotNull(
                contact.contactPerson?.takeIf { it.isNotBlank() },
                listOfNotNull(contact.phone, contact.email).joinToString(" · ").ifBlank { null }
            ).joinToString(" · ").ifBlank { null },
            kategori = contactTypeLabel(contact.type),
            onClick = onEdit,
            ikon = { LkAvatar(ad = contact.name, boyut = 44.dp) },
            sag = {
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Sil",
                        tint = LkTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        )
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
