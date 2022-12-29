package com.planner.core.domain

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class FormatDateUseCase {

    private val outputDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }

    fun getTimeLong(time: String?): Long? {
        return try {
            if (time != null) {
                outputDateFormat.parse(time)?.time
            } else {
                null
            }
        } catch (_: ParseException) {
            return null
        }
    }

    fun format(time: Long): String = outputDateFormat.format(time)
}