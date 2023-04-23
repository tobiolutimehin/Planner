package com.planner.core.data

import com.planner.core.data.entity.TaskManagerType
import org.junit.Assert.assertEquals
import org.junit.Test

class DataConvertersTest {

    private val converters = DataConverters()

    @Test
    fun `taskManagerTypeStringToEnum should convert valid strings to enum values`() {
        assertEquals(TaskManagerType.TODO_LIST, converters.taskManagerTypeStringToEnum("TODO_LIST"))
        assertEquals(TaskManagerType.PROJECT, converters.taskManagerTypeStringToEnum("PROJECT"))
    }

    @Test
    fun `taskManagerTypeStringToEnum should return null for invalid strings`() {
        assertEquals(null, converters.taskManagerTypeStringToEnum("INVALID"))
        assertEquals(null, converters.taskManagerTypeStringToEnum(""))
        assertEquals(null, converters.taskManagerTypeStringToEnum(" "))
    }

    @Test
    fun `taskManagerTypeEnumToString should convert enum values to their string representations`() {
        assertEquals("TODO_LIST", converters.taskManagerTypeEnumToString(TaskManagerType.TODO_LIST))
        assertEquals("PROJECT", converters.taskManagerTypeEnumToString(TaskManagerType.PROJECT))
    }
}
