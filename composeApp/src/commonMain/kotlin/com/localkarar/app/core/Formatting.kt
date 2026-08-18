package com.localkarar.app.core

object LkFormatting {

    private val CURRENCY_SYMBOLS = mapOf(
        "TRY" to "₺",
        "USD" to "$",
        "EUR" to "€",
        "GBP" to "£"
    )

    fun formatNumber(value: Double?): String {
        if (value == null) return ""
        val isNegative = value < 0
        val abs = kotlin.math.abs(value)
        val scaled = kotlin.math.round(abs * 100.0) / 100.0
        val integerPart = scaled.toLong()
        val fraction = kotlin.math.round((scaled - integerPart) * 100).toInt()
        val grouped = integerPart.toString()
            .reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()
        val fractionPart = if (fraction == 0) "" else {
            val s = fraction.toString().padStart(2, '0')
            if (s.endsWith("0")) ",${s.dropLast(1)}" else ",$s"
        }
        return (if (isNegative) "-" else "") + grouped + fractionPart
    }

    fun formatMoney(value: Double?, currency: String? = "TRY"): String {
        if (value == null) return ""
        val symbol = CURRENCY_SYMBOLS[currency?.uppercase() ?: "TRY"] ?: "${currency ?: "TRY"} "
        return "$symbol${formatNumber(value)}"
    }

    fun formatPercent(value: Double?): String {
        if (value == null) return ""
        val scaled = kotlin.math.round(value * 100.0) / 100.0
        return "${formatNumber(scaled)}%"
    }

    fun formatRatio(value: Double?): String {
        if (value == null) return ""
        val scaled = kotlin.math.round(value * 100.0) / 100.0
        return formatNumber(scaled)
    }

    fun parseDecimal(text: String): Double? {
        val cleaned = text.trim().replace(".", "").replace(",", ".")
        if (cleaned.isEmpty()) return null
        return cleaned.toDoubleOrNull()
    }
}