package com.planner.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FormatDateUseCaseTest {
    private lateinit var formatDateUseCase: FormatDateUseCase

    @Before
    fun setup() {
        formatDateUseCase = FormatDateUseCase()
    }

    @Test
    fun testFormatNumericalSlash() {
        val time = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2022)
            set(Calendar.MONTH, Calendar.DECEMBER)
            set(Calendar.DAY_OF_MONTH, 31)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis

        val formattedDate = formatDateUseCase.format(time)

        assertEquals("31/12/2022", formattedDate)
    }

    @Test
    fun testFormatLiteral() {
        // Given a date in milliseconds
        val time = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2022)
            set(Calendar.MONTH, Calendar.DECEMBER)
            set(Calendar.DAY_OF_MONTH, 31)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis

        // When formatting the date
        val formatDateUseCase = FormatDateUseCase(DatePattern.LITERAL)
        val formattedDate = formatDateUseCase.format(time)

        // Then the date should be in the correct format
        assertEquals("31, December 2022", formattedDate)
    }

    @Test
    fun testGetTimeLong() {
        val date = "01/01/2022"
        val expectedTime = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(date)?.time
        val actualTime = formatDateUseCase.getTimeLong(date)
        assertEquals(expectedTime, actualTime)
    }

    @Test
    fun testGetTimeLongWithNullInput() {
        val date: String? = null
        val time = formatDateUseCase.getTimeLong(date)
        assertNull(time)
    }

    @Test
    fun `format returns expected string when given valid time`() {
        // Given a valid time in milliseconds
        val time = 1662038399000L

        // When formatting the time
        val result = formatDateUseCase.format(time)

        // Then the result should be a string in the expected format
        assertEquals("01/09/2022", result)
    }

    @Test
    fun `format returns expected string when given custom time pattern`() {
        val time = 1662038399000L
        val datePattern = DatePattern.LITERAL

        val formatDateUseCase = FormatDateUseCase(datePattern)
        val result = formatDateUseCase.format(time)

        // Then the result should be a string in the expected format for the custom pattern
        assertEquals("1, September 2022", result)
    }
}
