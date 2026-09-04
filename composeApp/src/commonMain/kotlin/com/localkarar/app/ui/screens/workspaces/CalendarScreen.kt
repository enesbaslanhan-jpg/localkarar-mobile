package com.localkarar.app.ui.screens.workspaces

import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Today
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.core.LkFormatting
import com.localkarar.app.network.dto.BusinessRecordDto
import com.localkarar.app.ui.components.LkErrorState
import com.localkarar.app.ui.components.LkInfoPanel
import com.localkarar.app.ui.components.LkLoadingState
import com.localkarar.app.ui.components.LkPageLayout
import com.localkarar.app.ui.theme.*
import com.localkarar.app.workspaces.CalendarUiState
import com.localkarar.app.workspaces.CalendarViewModel
import kotlinx.datetime.LocalDate

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onOpenRecord: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    LkPageLayout(title = "Takvim", onBack = onBack) {
        when (val state = uiState) {
            is CalendarUiState.Loading -> LkLoadingState()
            is CalendarUiState.Error -> LkErrorState(
                message = state.message,
                onRetry = { viewModel.loadMonth() }
            )
            is CalendarUiState.Content -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(LkSpacing.Space4),
                    verticalArrangement = Arrangement.spacedBy(LkSpacing.Space4)
                ) {
                    item {
                        MonthHeader(
                            title = state.month.title,
                            onPrevious = { viewModel.goToPreviousMonth() },
                            onNext = { viewModel.goToNextMonth() },
                            onToday = { viewModel.goToToday() }
                        )
                        Spacer(modifier = Modifier.height(LkSpacing.Space3))
                        MonthGrid(
                            state = state,
                            selectedDate = selectedDate,
                            onDateSelected = { selectedDate = it }
                        )
                    }

                    val dayRecords = selectedDate?.let { state.recordsByDate[it] } ?: emptyList()
                    item {
                        LkInfoPanel(
                            title = selectedDate?.let { LkDateUtils.formatDate(it) } ?: "Gün Seçin"
                        ) {
                            if (dayRecords.isEmpty()) {
                                Text(
                                    text = "Bu güne ait kayıt yok.",
                                    style = LkTypography.getBodySmall(),
                                    color = LkTextSecondary
                                )
                            } else {
                                dayRecords.forEach { record ->
                                    DayRecordRow(record, onClick = { onOpenRecord(record.id) })
                                    Spacer(modifier = Modifier.height(LkSpacing.Space2))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthHeader(
    title: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Önceki ay",
                tint = LkTextSecondary
            )
        }
        Text(
            text = title,
            style = LkTypography.getSectionTitle(),
            color = LkTextPrimary,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Sonraki ay",
                tint = LkTextSecondary
            )
        }
        IconButton(onClick = onToday) {
            Icon(
                imageVector = Icons.Outlined.Today,
                contentDescription = "Bugün",
                tint = LkPrimary
            )
        }
    }
}

@Composable
private fun MonthGrid(
    state: CalendarUiState.Content,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = LkDateUtils.today()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LkSurfacePanel, LkShapes.MD)
            .border(1.dp, LkLineStrong, LkShapes.MD)
            .padding(LkSpacing.Space3)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            (0..6).forEach { index ->
                Text(
                    text = LkDateUtils.shortDayName(index),
                    style = LkTypography.getMicro(),
                    color = LkTextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(LkSpacing.Space2))
        state.month.weeks.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    Box(
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (date != null) {
                            val dayRecords = state.recordsByDate[date].orEmpty()
                            val isSelected = date == selectedDate
                            val isToday = date == today
                            Column(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        when {
                                            isSelected -> LkPrimary
                                            isToday -> LkPrimary.copy(alpha = 0.25f)
                                            else -> Color.Transparent
                                        },
                                        CircleShape
                                    )
                                    .clickable { onDateSelected(date) },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    style = LkTypography.getBodySmall(),
                                    color = if (isSelected) LkOnPrimary else LkTextPrimary
                                )
                                if (dayRecords.isNotEmpty() && !isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .background(if (dayRecords.any { it.direction == "payable" }) LkDanger else LkSuccess, CircleShape)
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.size(38.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayRecordRow(
    record: BusinessRecordDto,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.title,
                style = LkTypography.getBodySmall(),
                color = LkTextPrimary,
                maxLines = 1
            )
            Text(
                text = recordTypeLabel(record.type) + " • " + recordStatusLabel(record.status),
                style = LkTypography.getMicro(),
                color = LkTextMuted
            )
        }
        record.amount?.let {
            Text(
                text = LkFormatting.formatMoney(it, record.currency),
                style = LkTypography.getBodyStrong(),
                color = when (record.direction) {
                    "payable" -> LkDanger
                    "receivable" -> LkSuccess
                    else -> LkTextPrimary
                }
            )
        }
    }
}