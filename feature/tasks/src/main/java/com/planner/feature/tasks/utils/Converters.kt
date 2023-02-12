package com.planner.feature.tasks.utils

import com.planner.core.data.entity.TaskManagerType
import com.planner.feature.tasks.R

object Converters {
    fun Int.toTaskManagerType(): TaskManagerType = when (this@toTaskManagerType) {
        0 -> TaskManagerType.TODO_LIST
        1 -> TaskManagerType.PROJECT
        else -> TaskManagerType.TODO_LIST
    }

    fun TaskManagerType.toInt() = when (this@toInt) {
        TaskManagerType.TODO_LIST -> 0
        TaskManagerType.PROJECT -> 1
    }

    fun TaskManagerType.toTitleName() = when (this@toTitleName) {
        TaskManagerType.TODO_LIST -> R.string.personal_todo_list
        TaskManagerType.PROJECT -> R.string.group_project
    }

    fun TaskManagerType.toTypeName() = when (this@toTypeName) {
        TaskManagerType.TODO_LIST -> R.string.to_do_list
        TaskManagerType.PROJECT -> R.string.group_project
    }
}
