package com.localkarar.app.workspaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localkarar.app.core.LkDateUtils
import com.localkarar.app.network.dto.BusinessRecordDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

sealed class CalendarUiState {
    object Loading : CalendarUiState()
    data class Content(
        val month: LkDateUtils.CalendarMonth,
        val recordsByDate: Map<LocalDate, List<BusinessRecordDto>> = emptyMap(),
        val totals: com.localkarar.app.network.dto.CalendarTotalsDto? = null,
        val isLoadingMonth: Boolean = false
    ) : CalendarUiState()
    data class Error(val message: String) : CalendarUiState()
}

class CalendarViewModel(
    private val workspaceId: String,
    private val repository: WorkspaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CalendarUiState>(CalendarUiState.Loading)
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private var year: Int = LkDateUtils.today().year
    private var month: Int = LkDateUtils.today().monthNumber

    init {
        loadMonth()
    }

    fun goToPreviousMonth() {
        month -= 1
        if (month == 0) {
            month = 12
            year -= 1
        }
        loadMonth()
    }

    fun goToNextMonth() {
        month += 1
        if (month == 13) {
            month = 1
            year += 1
        }
        loadMonth()
    }

    fun goToToday() {
        val today = LkDateUtils.today()
        year = today.year
        month = today.monthNumber
        loadMonth()
    }

    fun loadMonth() {
        val calendarMonth = LkDateUtils.calendarMonth(year, month)
        val dates = calendarMonth.weeks.flatten().filterNotNull()
        val from = dates.first()
        val to = dates.last()
        val current = _uiState.value
        if (current is CalendarUiState.Content) {
            _uiState.value = current.copy(isLoadingMonth = true)
        } else {
            _uiState.value = CalendarUiState.Loading
        }
        viewModelScope.launch {
            val result = repository.getCalendar(
                workspaceId,
                from = "${from.year}-${from.monthNumber.toString().padStart(2, '0')}-${from.dayOfMonth.toString().padStart(2, '0')}T00:00:00.000Z",
                to = "${to.year}-${to.monthNumber.toString().padStart(2, '0')}-${to.dayOfMonth.toString().padStart(2, '0')}T23:59:59.999Z"
            )
            if (result.isSuccess) {
                val response = result.getOrThrow()
                val byDate = mutableMapOf<LocalDate, List<BusinessRecordDto>>()
                response.dayOfMonths.forEach { (dayString, records) ->
                    val parsed = parseLocalDate(dayString) ?: return@forEach
                    byDate[parsed] = records
                }
                _uiState.value = CalendarUiState.Content(
                    month = calendarMonth,
                    recordsByDate = byDate,
                    totals = response.totals
                )
            } else if (current is CalendarUiState.Content) {
                _uiState.value = current.copy(isLoadingMonth = false)
            } else {
                _uiState.value = CalendarUiState.Error(
                    result.exceptionOrNull()?.message ?: "Takvim yüklenemedi."
                )
            }
        }
    }

    private fun parseLocalDate(value: String): LocalDate? {
        return try {
            val parts = value.split("-")
            if (parts.size != 3) return null
            LocalDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        } catch (e: Exception) {
            null
        }
    }
}