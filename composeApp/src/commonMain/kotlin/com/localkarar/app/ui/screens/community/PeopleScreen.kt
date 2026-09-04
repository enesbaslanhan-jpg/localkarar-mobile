package com.localkarar.app.ui.screens.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.localkarar.app.community.SocialViewModel
import com.localkarar.app.network.dto.PersonDto
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.components.LkButtonVariant
import com.localkarar.app.ui.theme.*

@Composable
fun PeopleScreen(
    viewModel: SocialViewModel,
    onOpenProfile: (Int) -> Unit
) {
    val peopleState by viewModel.peopleState.collectAsState()
    var reportingPersonId by remember { mutableStateOf<Int?>(null) }

    Column(Modifier.fillMaxSize()) {
        // Search bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                placeholder = { Text("Toplulukta kişi ara...", style = LkTypography.getBodySmall(), color = LkTextMuted) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = LkTextSecondary) },
                trailingIcon = {
                    if (viewModel.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Outlined.Clear, contentDescription = "Temizle", tint = LkTextSecondary)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = LkSurfacePanel,
                    textColor = LkTextPrimary,
                    cursorColor = LkPrimary,
                    focusedBorderColor = LkPrimary,
                    unfocusedBorderColor = LkLineSoft
                ),
                shape = LkShapes.MD,
                singleLine = true
            )
        }

        when (val s = peopleState) {
            is SocialViewModel.PeopleUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LkPrimary)
                }
            }
            is SocialViewModel.PeopleUiState.Error -> {
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(s.message, color = LkDanger, style = LkTypography.getBody())
                    Spacer(Modifier.height(12.dp))
                    LkButton(text = "Tekrar Dene", variant = LkButtonVariant.SECONDARY, onClick = { viewModel.loadPeople(viewModel.searchQuery) })
                }
            }
            is SocialViewModel.PeopleUiState.Content -> {
                if (s.people.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.PeopleOutline, contentDescription = null, tint = LkTextMuted, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                if (viewModel.searchQuery.isNotEmpty()) "Kullanıcı bulunamadı" else "Henüz kimse bulunmuyor",
                                style = LkTypography.getBodyStrong(),
                                color = LkTextPrimary
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(s.people, key = { it.id }) { person ->
                            PersonRowItem(
                                person = person,
                                isFollowing = viewModel.followingIds.contains(person.id),
                                isBlocked = viewModel.blockedIds.contains(person.id),
                                onClick = { onOpenProfile(person.id) },
                                onToggleFollow = { viewModel.toggleFollow(person.id) },
                                onToggleBlock = { viewModel.toggleBlock(person.id) },
                                onReport = { reportingPersonId = person.id }
                            )
                        }
                    }
                }
            }
        }
    }

    // User report dialog
    reportingPersonId?.let { personId ->
        UserReportDialog(
            onDismiss = { reportingPersonId = null },
            onSubmit = { reason, details ->
                viewModel.reportUser(personId, reason, details)
                reportingPersonId = null
            }
        )
    }
}

@Composable
fun PersonRowItem(
    person: PersonDto,
    isFollowing: Boolean,
    isBlocked: Boolean,
    onClick: () -> Unit,
    onToggleFollow: () -> Unit,
    onToggleBlock: () -> Unit,
    onReport: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        backgroundColor = LkSurfacePanel,
        elevation = 0.dp,
        shape = LkShapes.MD
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isBlocked) LkSurfaceSunken else LkPrimarySoft),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    (person.name.take(1)).uppercase(),
                    style = LkTypography.getBodyStrong(),
                    color = if (isBlocked) LkTextMuted else LkPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(12.dp))

            // User Info
            Column(Modifier.weight(1f)) {
                Text(
                    person.name,
                    style = LkTypography.getBodyStrong(),
                    color = if (isBlocked) LkTextMuted else LkTextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!person.bio.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        person.bio,
                        style = LkTypography.getMicro(),
                        color = LkTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Action Buttons
            if (isBlocked) {
                LkButton(
                    text = "Engeli Kaldır",
                    variant = LkButtonVariant.SECONDARY,
                    onClick = onToggleBlock
                )
            } else {
                LkButton(
                    text = if (isFollowing) "Takip Ediliyor" else "Takip Et",
                    variant = if (isFollowing) LkButtonVariant.SECONDARY else LkButtonVariant.PRIMARY,
                    onClick = onToggleFollow
                )
            }

            // More actions menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "Seçenekler", tint = LkTextSecondary)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(LkSurfacePanel)
                ) {
                    DropdownMenuItem(onClick = {
                        showMenu = false
                        onToggleBlock()
                    }) {
                        Text(if (isBlocked) "Engeli Kaldır" else "Engelle", color = if (isBlocked) LkTextPrimary else LkDanger)
                    }
                    if (!isBlocked) {
                        DropdownMenuItem(onClick = {
                            showMenu = false
                            onReport()
                        }) {
                            Text("Şikayet Et", color = LkTextPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserReportDialog(
    onDismiss: () -> Unit,
    onSubmit: (reason: String, details: String?) -> Unit
) {
    var reason by remember { mutableStateOf("harassment") }
    var details by remember { mutableStateOf("") }

    val reasons = listOf(
        "harassment" to "Taciz / Hakaret",
        "spam" to "Spam / İstenmeyen Davranış",
        "impersonation" to "Başkası Gibi Davranma",
        "unsafe" to "Güvensiz / Zararlı İçerik",
        "other" to "Diğer"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        backgroundColor = LkSurfacePanel,
        shape = RoundedCornerShape(16.dp),
        title = { Text("Kullanıcıyı Şikayet Et", style = LkTypography.getSectionTitle(), color = LkTextPrimary) },
        text = {
            Column(Modifier.fillMaxWidth()) {
                reasons.forEach { (key, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { reason = key }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = reason == key,
                            onClick = { reason = key },
                            colors = RadioButtonDefaults.colors(selectedColor = LkPrimary, unselectedColor = LkTextMuted)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(label, style = LkTypography.getBodySmall(), color = LkTextPrimary)
                    }
                }

                if (reason == "other") {
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        placeholder = { Text("Açıklama belirtiniz...", style = LkTypography.getBodySmall(), color = LkTextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            backgroundColor = LkSurfaceSunken,
                            textColor = LkTextPrimary,
                            cursorColor = LkPrimary,
                            focusedBorderColor = LkPrimary,
                            unfocusedBorderColor = LkLineSoft
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSubmit(reason, details.ifBlank { null })
                }
            ) {
                Text("Gönder", color = LkPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç", color = LkTextSecondary)
            }
        }
    )
}
