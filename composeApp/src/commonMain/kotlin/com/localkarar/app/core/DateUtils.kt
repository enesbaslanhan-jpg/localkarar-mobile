package com.localkarar.app.core

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

object LkDateUtils {

    private val MONTH_NAMES = listOf(
        "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
        "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
    )

    private val SHORT_MONTH_NAMES = listOf(
        "Oca", "Şub", "Mar", "Nis", "May", "Haz",
        "Tem", "Ağu", "Eyl", "Eki", "Kas", "Ara"
    )

    private val DAY_NAMES = listOf(
        "Pazartesi", "Salı", "Çarşamba", "Perşembe",
        "Cuma", "Cumartesi", "Pazar"
    )

    private val SHORT_DAY_NAMES = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")

    fun now(): Instant = Clock.System.now()

    fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

    fun parseInstant(value: String?): Instant? {
        if (value.isNullOrBlank()) return null
        return try {
            Instant.parse(value)
        } catch (e: Exception) {
            null
        }
    }

    fun toLocalDateTime(instant: Instant?): LocalDateTime? {
        if (instant == null) return null
        return try {
            instant.toLocalDateTime(TimeZone.currentSystemDefault())
        } catch (e: Exception) {
            null
        }
    }

    fun parseDate(value: String?): LocalDate? {
        val instant = parseInstant(value) ?: return null
        return try {
            instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
        } catch (e: Exception) {
            null
        }
    }

    fun formatDate(value: String?): String {
        val date = parseDate(value) ?: return ""
        return formatDate(date)
    }

    fun formatDate(date: LocalDate): String {
        return "${date.dayOfMonth} ${MONTH_NAMES[date.monthNumber - 1]} ${date.year}"
    }

    fun formatShortDate(date: LocalDate): String {
        return "${date.dayOfMonth} ${SHORT_MONTH_NAMES[date.monthNumber - 1]} ${date.year}"
    }

    fun formatDateTime(value: String?): String {
        val ldt = toLocalDateTime(parseInstant(value)) ?: return ""
        val date = ldt.date
        val hh = ldt.hour.toString().padStart(2, '0')
        val mm = ldt.minute.toString().padStart(2, '0')
        return "${date.dayOfMonth} ${MONTH_NAMES[date.monthNumber - 1]} ${date.year} $hh:$mm"
    }

    fun formatMonthName(date: LocalDate): String {
        return "${MONTH_NAMES[date.monthNumber - 1]} ${date.year}"
    }

    fun dayOfWeekName(date: LocalDate): String {
        return DAY_NAMES[date.dayOfWeek.ordinal]
    }

    fun shortDayName(index: Int): String {
        return SHORT_DAY_NAMES[index]
    }

    fun formatTimeAgo(value: String?): String {
        val instant = parseInstant(value) ?: return ""
        val now = now()
        val diff = now.epochSeconds - instant.epochSeconds
        if (diff < 0) return "az önce"
        val minutes = diff / 60
        if (minutes < 1) return "az önce"
        if (minutes < 60) return "$minutes dk önce"
        val hours = minutes / 60
        if (hours < 24) return "$hours saat önce"
        val days = hours / 24
        if (days < 7) return "$days gün önce"
        if (days < 30) {
            val weeks = days / 7
            return "$weeks hafta önce"
        }
        if (days < 365) {
            val months = days / 30
            return "$months ay önce"
        }
        return "${days / 365} yıl önce"
    }

    fun isSameDay(a: LocalDate, b: LocalDate): Boolean {
        return a.year == b.year && a.monthNumber == b.monthNumber && a.dayOfMonth == b.dayOfMonth
    }

    fun isToday(date: LocalDate): Boolean = isSameDay(date, today())

    fun daysUntil(date: LocalDate): Int {
        return date.toEpochDays() - today().toEpochDays()
    }

    fun dateAt(offsetDays: Int): LocalDate {
        return today().plus(offsetDays, kotlinx.datetime.DateTimeUnit.DAY)
    }

    data class CalendarMonth(
        val year: Int,
        val monthNumber: Int,
        val title: String,
        val weeks: List<List<LocalDate?>>
    )

    fun calendarMonth(year: Int, monthNumber: Int): CalendarMonth {
        val firstDay = LocalDate(year, monthNumber, 1)
        // kotlinx LocalDate.dayOfWeek.ordinal: MONDAY=0 ... SUNDAY=6
        val leadingBlanks = firstDay.dayOfWeek.ordinal
        val daysInMonth = daysInMonth(year, monthNumber)
        val cells = mutableListOf<LocalDate?>()
        repeat(leadingBlanks) { cells.add(null) }
        for (day in 1..daysInMonth) {
            cells.add(LocalDate(year, monthNumber, day))
        }
        while (cells.size % 7 != 0) {
            cells.add(null)
        }
        val weeks = cells.chunked(7)
        return CalendarMonth(
            year = year,
            monthNumber = monthNumber,
            title = "${MONTH_NAMES[monthNumber - 1]} $year",
            weeks = weeks
        )
    }

    fun daysInMonth(year: Int, monthNumber: Int): Int {
        return when (monthNumber) {
            2 -> if (isLeapYear(year)) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }
    }

    fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0
    }
}

