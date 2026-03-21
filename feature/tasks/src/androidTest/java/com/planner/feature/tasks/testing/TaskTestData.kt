package com.planner.feature.tasks.testing

import com.planner.core.data.dao.TaskManagerDao
import com.planner.core.data.entity.Task
import com.planner.core.data.entity.TaskManagerEntity
import com.planner.core.data.entity.TaskManagerType
import kotlinx.coroutines.runBlocking

internal fun TaskManagerDao.seedTaskManager(
    name: String,
    type: TaskManagerType,
    tasks: List<Task>,
): Long = runBlocking {
    val managerId = insertTaskManager(
        TaskManagerEntity(
            name = name,
            type = type,
        ),
    )
    insertTasks(tasks.map { it.toTaskEntity(managerId) })
    managerId
}
