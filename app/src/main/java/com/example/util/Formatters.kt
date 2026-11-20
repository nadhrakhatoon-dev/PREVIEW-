package com.example.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val MAX_PAISE = 10_000_000_000L // ₹10,00,00,000.00 safety cap

fun formatInrFromPaise(paise: Long): String {
    val isNegative = paise < 0
    val absPaise = kotlin.math.abs(paise)
    val wholeRupees = absPaise / 100
    val remainderPaise = absPaise % 100

    val groupedWhole = formatIndianIntegerGrouping(wholeRupees)
    val sign = if (isNegative) "-" else ""
    return if (remainderPaise == 0L) {
        "${sign}₹$groupedWhole"
    } else {
        "${sign}₹$groupedWhole.${remainderPaise.toString().padStart(2, '0')}"
    }
}

private fun formatIndianIntegerGrouping(value: Long): String {
    val digits = value.toString()
    if (digits.length <= 3) return digits

    val lastThree = digits.takeLast(3)
    val remaining = digits.dropLast(3)
    val sb = StringBuilder()
    var count = 0
    for (i in remaining.length - 1 downTo 0) {
        sb.append(remaining[i])
        count++
        if (count == 2 && i > 0) {
            sb.append(',')
            count = 0
        }
    }
    return "${sb.reverse()},$lastThree"
}

fun sanitizeAmountInput(raw: String): String {
    val filtered = StringBuilder()
    var hasDecimal = false
    var decimalDigits = 0
    var integerDigits = 0

    for (ch in raw) {
        when {
            ch.isDigit() -> {
                if (hasDecimal) {
                    if (decimalDigits < 2) {
                        filtered.append(ch)
                        decimalDigits++
                    }
                } else {
                    if (integerDigits < 8) {
                        filtered.append(ch)
                        integerDigits++
                    }
                }
            }
            ch == '.' && !hasDecimal -> {
                if (filtered.isEmpty()) {
                    filtered.append('0')
                    integerDigits = 1
                }
                filtered.append('.')
                hasDecimal = true
            }
        }
    }
    return filtered.toString()
}

fun parseRupeesToPaise(input: String): Long? {
    val trimmed = input.trim()
    if (trimmed.isEmpty() || trimmed == ".") return null
    return try {
        val bd = BigDecimal(trimmed)
        if (bd <= BigDecimal.ZERO) return null
        val paise = bd.multiply(BigDecimal(100))
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
        if (paise in 1L..MAX_PAISE) paise else null
    } catch (_: Exception) {
        null
    }
}

fun formatTodayDate(nowMillis: Long = System.currentTimeMillis()): String {
    val formatter = SimpleDateFormat("EEEE, d MMM", Locale.getDefault())
    return formatter.format(Date(nowMillis))
}

fun formatEntryTimestamp(timestampMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
    val entryCal = Calendar.getInstance().apply { timeInMillis = timestampMillis }
    val nowCal = Calendar.getInstance().apply { timeInMillis = nowMillis }
    val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    val timeText = timeFormatter.format(Date(timestampMillis))

    val isSameYear = entryCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
    val dayDiff = nowCal.get(Calendar.DAY_OF_YEAR) - entryCal.get(Calendar.DAY_OF_YEAR)

    return when {
        isSameYear && dayDiff == 0 -> "Today, $timeText"
        isSameYear && dayDiff == 1 -> "Yesterday, $timeText"
        else -> {
            val dateFormatter = SimpleDateFormat("d MMM, h:mm a", Locale.getDefault())
            dateFormatter.format(Date(timestampMillis))
        }
    }
}
