package com.localkarar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.LkDateUtils
import kotlinx.datetime.LocalDate

@Composable
fun LkDatePickerDialog(
    initialDate: LocalDate? = null,
    title: String = "Tarih Seç",
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    var year by remember { mutableStateOf(initialDate?.year ?: LkDateUtils.today().year) }
    var month by remember { mutableStateOf(initialDate?.monthNumber ?: LkDateUtils.today().monthNumber) }
    var selected by remember { mutableStateOf(initialDate) }
    val today = LkDateUtils.today()

    androidx.compose.material.AlertDialog(
        onDismissRequest = onDismiss,
        backgroundColor = com.localkarar.app.ui.theme.LkSurfacePanel,
        title = {
            Text(
                text = title,
                style = com.localkarar.app.ui.theme.LkTypography.getBodyStrong(),
                color = com.localkarar.app.ui.theme.LkTextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        month -= 1
                        if (month == 0) {
                            month = 12
                            year -= 1
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Önceki ay",
                            tint = com.localkarar.app.ui.theme.LkTextSecondary
                        )
                    }
                    Text(
                        text = "${LkDateUtils.formatMonthName(LocalDate(year, month, 1))}",
                        style = com.localkarar.app.ui.theme.LkTypography.getBodyStrong(),
                        color = com.localkarar.app.ui.theme.LkTextPrimary
                    )
                    IconButton(onClick = {
                        month += 1
                        if (month == 13) {
                            month = 1
                            year += 1
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Sonraki ay",
                            tint = com.localkarar.app.ui.theme.LkTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    (0..6).forEach { index ->
                        Text(
                            text = LkDateUtils.shortDayName(index),
                            style = com.localkarar.app.ui.theme.LkTypography.getMicro(),
                            color = com.localkarar.app.ui.theme.LkTextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                LkDateUtils.calendarMonth(year, month).weeks.forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        week.forEach { date ->
                            Box(
                                modifier = Modifier.weight(1f).aspectRatio(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                if (date != null) {
                                    val isSelected = date == selected
                                    val isToday = date == today
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(
                                                when {
                                                    isSelected -> com.localkarar.app.ui.theme.LkPrimary
                                                    isToday -> com.localkarar.app.ui.theme.LkPrimary.copy(alpha = 0.25f)
                                                    else -> androidx.compose.ui.graphics.Color.Transparent
                                                },
                                                CircleShape
                                            )
                                            .clickable {
                                                selected = date
                                                onDateSelected(date)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = date.dayOfMonth.toString(),
                                            style = com.localkarar.app.ui.theme.LkTypography.getBodySmall(),
                                            color = if (isSelected) {
                                                com.localkarar.app.ui.theme.LkOnPrimary
                                            } else {
                                                com.localkarar.app.ui.theme.LkTextPrimary
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            LkButton(
                text = "Seç",
                onClick = {
                    onDateSelected(selected ?: today)
                    onDismiss()
                }
            )
        },
        dismissButton = {
            LkButton(
                text = "Vazgeç",
                variant = LkButtonVariant.QUIET,
                onClick = onDismiss
            )
        }
    )
}

@Composable
fun LkDateField(
    label: String,
    date: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }
    LkTextField(
        value = date?.let { LkDateUtils.formatDate(it) } ?: "",
        onValueChange = {},
        modifier = modifier,
        label = label,
        placeholder = "Tarih seçin",
        trailingContent = {
            IconButton(onClick = { showPicker = true }) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Event,
                    contentDescription = "Tarih seç",
                    tint = com.localkarar.app.ui.theme.LkTextSecondary
                )
            }
        }
    )
    if (showPicker) {
        LkDatePickerDialog(
            initialDate = date,
            title = label,
            onDismiss = { showPicker = false },
            onDateSelected = onDateSelected
        )
    }
}
