package com.planner.core.data.dao

import com.planner.core.data.entity.ManagerWithTasks
import com.planner.core.data.entity.Task
import com.planner.core.data.entity.TaskEntity
import com.planner.core.data.entity.TaskManagerEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TestTaskManagerDao : TaskManagerDao {
    private val taskManagers: MutableMap<Long, ManagerWithTasks> = mutableMapOf()
    private val tasks: MutableMap<Long, List<TaskEntity>> = mutableMapOf()

    override fun getTaskManagers(): Flow<List<ManagerWithTasks>> = flow {
        emit(taskManagers.values.toList())
    }

    override fun getTaskManager(id: Long): Flow<ManagerWithTasks> = flow {
        taskManagers[id]?.let { emit(it) }
    }

    override suspend fun insertTasks(taskEntity: List<TaskEntity>) {
        val managerId = taskEntity.first().taskManagerId
        tasks.merge(managerId, taskEntity) { oldTasks, newTasks -> oldTasks + newTasks }
    }

    override suspend fun insertTaskManager(taskManagerEntity: TaskManagerEntity): Long {
        val id = taskManagers.size.toLong() + 1
        val managerWithTasks = ManagerWithTasks(taskManagerEntity, emptyList())
        taskManagers[id] = managerWithTasks
        return id
    }

    override suspend fun updateTask(taskEntity: TaskEntity) {
        val managerId = taskEntity.taskManagerId
        tasks[managerId] = tasks[managerId].orEmpty()
            .map { if (it.taskId == taskEntity.taskId) taskEntity else it }
    }

    override suspend fun updateTaskManager(taskManagerEntity: TaskManagerEntity) {
        taskManagers[taskManagerEntity.managerId] =
            ManagerWithTasks(taskManagerEntity, tasks[taskManagerEntity.managerId].orEmpty())
    }

    override suspend fun deleteTaskManager(taskManagerEntity: TaskManagerEntity) {
        taskManagers.remove(taskManagerEntity.managerId)
        tasks.remove(taskManagerEntity.managerId)
    }

    override suspend fun deleteTasks(taskEntities: List<TaskEntity>) {
        val managerId = taskEntities.firstOrNull()?.taskManagerId ?: return
        tasks[managerId] =
            tasks[managerId].orEmpty().filter { it.taskId !in taskEntities.map { it.taskId } }
    }

    override suspend fun deleteTasksByManagerId(managerId: Long) {
        tasks.remove(managerId)
    }

    override suspend fun insertTaskManagerWithTasks(
        taskManager: TaskManagerEntity,
        tasks: List<Task>,
    ) {
        val taskManagerId = insertTaskManager(taskManager)

        val taskEntities = tasks.map { it.toTaskEntity(taskManagerId) }
        insertTasks(taskEntities)
    }
}
