package com.planner.feature.tasks.utils

import com.planner.core.data.entity.TaskManagerType
import com.planner.feature.tasks.R
import com.planner.feature.tasks.utils.Converters.toInt
import com.planner.feature.tasks.utils.Converters.toTaskManagerType
import com.planner.feature.tasks.utils.Converters.toTitleName
import com.planner.feature.tasks.utils.Converters.toTypeName
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {

    @Test
    fun `test toTaskManagerType with valid values`() {
        assertEquals(TaskManagerType.TODO_LIST, 0.toTaskManagerType())
        assertEquals(TaskManagerType.PROJECT, 1.toTaskManagerType())
    }

    @Test
    fun `test toTaskManagerType with invalid values`() {
        assertEquals(TaskManagerType.TODO_LIST, (-1).toTaskManagerType())
        assertEquals(TaskManagerType.TODO_LIST, 2.toTaskManagerType())
    }

    @Test
    fun `test TaskManagerType toInt`() {
        assertEquals(0, TaskManagerType.TODO_LIST.toInt())
        assertEquals(1, TaskManagerType.PROJECT.toInt())
    }

    @Test
    fun `test TaskManagerType toTitleName`() {
        assertEquals(R.string.personal_todo_list, TaskManagerType.TODO_LIST.toTitleName())
        assertEquals(R.string.group_project, TaskManagerType.PROJECT.toTitleName())
    }

    @Test
    fun `test TaskManagerType toTypeName`() {
        assertEquals(R.string.to_do_list, TaskManagerType.TODO_LIST.toTypeName())
        assertEquals(R.string.group_project, TaskManagerType.PROJECT.toTypeName())
    }
}
