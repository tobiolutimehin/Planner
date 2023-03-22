package com.planner.core.domain

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * A use case for formatting dates and converting them to a time long value.
 *
 * @param pattern The pattern to use for formatting dates.
 */
class FormatDateUseCase(pattern: DatePattern = DatePattern.NUMERICAL_SLASH) {

    private val datePattern = when (pattern) {
        DatePattern.NUMERICAL_SLASH -> "dd/MM/yyyy"
        DatePattern.LITERAL -> "d, MMMM yyyy"
    }

    private val outputDateFormat = SimpleDateFormat(datePattern, Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }

    /**
     * Gets the time long value for a given date string.
     *
     * @param time The date string to convert to a time long value.
     * @return The time long value for the given date string, or null if the string is invalid.
     */
    fun getTimeLong(time: String?): Long? {
        return try {
            if (time != null) {
                outputDateFormat.parse(time)?.time
            } else {
                null
            }
        } catch (e: ParseException) {
            null
        }
    }

    /**
     * Formats a time long value as a date string.
     *
     * @param time The time long value to format.
     * @return The formatted date string.
     */
    fun format(time: Long): String = outputDateFormat.format(time)
}

/**
 * An enumeration of date patterns that can be used for formatting dates.
 */
enum class DatePattern {
    NUMERICAL_SLASH, LITERAL
}
