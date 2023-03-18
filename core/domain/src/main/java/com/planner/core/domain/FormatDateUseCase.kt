package com.planner.core.domain

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class FormatDateUseCase(pattern: DatePattern = DatePattern.NUMERICAL_SLASH) {

    private val datePattern = when (pattern) {
        DatePattern.NUMERICAL_SLASH -> "dd/MM/yyyy"
        DatePattern.LITERAL -> "d, MMMM yyyy"
    }

    private val outputDateFormat = SimpleDateFormat(datePattern, Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }

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

    fun format(time: Long): String = outputDateFormat.format(time)
}

enum class DatePattern {
    NUMERICAL_SLASH, LITERAL
}
