package com.planner.feature.tasks.utils

import com.planner.core.data.entity.TaskManagerType
import com.planner.feature.tasks.R

/**
 * Utility class for converting between [TaskManagerType] and its corresponding integer and string values.
 */
object Converters {

    /**
     * Converts an integer value to its corresponding [TaskManagerType].
     * @return The corresponding [TaskManagerType] value.
     */
    fun Int.toTaskManagerType(): TaskManagerType = when (this@toTaskManagerType) {
        0 -> TaskManagerType.TODO_LIST
        1 -> TaskManagerType.PROJECT
        else -> TaskManagerType.TODO_LIST
    }

    /**
     * Converts a [TaskManagerType] value to its corresponding integer value.
     * @return The corresponding integer value.
     */
    fun TaskManagerType.toInt() = when (this@toInt) {
        TaskManagerType.TODO_LIST -> 0
        TaskManagerType.PROJECT -> 1
    }

    /**
     * Gets the title name of a [TaskManagerType].
     * @return The string resource ID of the title name.
     */
    fun TaskManagerType.toTitleName() = when (this@toTitleName) {
        TaskManagerType.TODO_LIST -> R.string.personal_todo_list
        TaskManagerType.PROJECT -> R.string.group_project
    }

    /**
     * Gets the type name of a [TaskManagerType].
     * @return The string resource ID of the type name.
     */
    fun TaskManagerType.toTypeName() = when (this@toTypeName) {
        TaskManagerType.TODO_LIST -> R.string.to_do_list
        TaskManagerType.PROJECT -> R.string.group_project
    }
}
