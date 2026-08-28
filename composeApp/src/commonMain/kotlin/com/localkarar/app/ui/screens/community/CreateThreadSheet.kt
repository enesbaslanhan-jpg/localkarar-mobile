package com.localkarar.app.ui.screens.community

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.localkarar.app.community.ThreadsViewModel
import com.localkarar.app.ui.components.LkButton
import com.localkarar.app.ui.theme.*

@Composable
fun CreateThreadSheet(
    viewModel: ThreadsViewModel,
    onThreadCreated: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredPeople = remember(viewModel.availablePeople, searchQuery) {
        if (searchQuery.isBlank()) viewModel.availablePeople
        else viewModel.availablePeople.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val isGroup = viewModel.selectedMemberIds.size > 1

    AlertDialog(
        onDismissRequest = { viewModel.dismissCreateThreadSheet() },
        backgroundColor = LkSurfacePanel,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Yeni Sohbet Başlat", style = LkTypography.getSectionTitle(), color = LkTextPrimary)
                IconButton(onClick = { viewModel.dismissCreateThreadSheet() }) {
                    Icon(Icons.Default.Close, contentDescription = "Kapat", tint = LkTextSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                // Group Name input (if > 1 participants selected)
                if (isGroup) {
                    OutlinedTextField(
                        value = viewModel.newThreadName,
                        onValueChange = { viewModel.onNewThreadNameChange(it) },
                        placeholder = { Text("Grup Adı (İsteğe bağlı)", style = LkTypography.getBodySmall(), color = LkTextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            backgroundColor = LkSurfaceSunken,
                            textColor = LkTextPrimary,
                            cursorColor = LkPrimary,
                            focusedBorderColor = LkPrimary,
                            unfocusedBorderColor = LkLineSoft
                        ),
                        shape = LkShapes.MD,
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // Search box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Kişi ara...", style = LkTypography.getBodySmall(), color = LkTextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LkTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = LkSurfaceSunken,
                        textColor = LkTextPrimary,
                        cursorColor = LkPrimary,
                        focusedBorderColor = LkPrimary,
                        unfocusedBorderColor = LkLineSoft
                    ),
                    shape = LkShapes.MD,
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Seçilen (${viewModel.selectedMemberIds.size})",
                    style = LkTypography.getMicro(),
                    color = LkTextSecondary
                )

                Spacer(Modifier.height(4.dp))

                if (viewModel.isLoadingPeople) {
                    Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LkPrimary, strokeWidth = 2.dp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredPeople, key = { it.id }) { person ->
                            val isSelected = viewModel.selectedMemberIds.contains(person.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(LkShapes.SM)
                                    .background(if (isSelected) LkPrimarySoft else LkSurfaceSunken)
                                    .clickable { viewModel.toggleMemberSelection(person.id) }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) LkPrimary else LkSurfacePanel),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        person.name.take(1).uppercase(),
                                        style = LkTypography.getMicro(),
                                        color = if (isSelected) LkOnPrimary else LkTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    person.name,
                                    style = LkTypography.getBodySmall(),
                                    color = if (isSelected) LkPrimary else LkTextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { viewModel.toggleMemberSelection(person.id) },
                                    colors = CheckboxDefaults.colors(checkedColor = LkPrimary, uncheckedColor = LkTextMuted)
                                )
                            }
                        }
                    }
                }
            }
        },
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { viewModel.dismissCreateThreadSheet() }) {
                    Text("Vazgeç", color = LkTextSecondary)
                }
                Spacer(Modifier.width(8.dp))
                LkButton(
                    text = if (isGroup) "Grubu Başlat" else "Sohbet Başlat",
                    enabled = viewModel.selectedMemberIds.isNotEmpty(),
                    onClick = {
                        viewModel.createThread { threadId ->
                            onThreadCreated(threadId)
                        }
                    }
                )
            }
        }
    )
}
