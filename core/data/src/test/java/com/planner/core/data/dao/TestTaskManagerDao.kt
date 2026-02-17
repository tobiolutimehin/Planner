package com.planner.core.data.dao

import com.planner.core.data.entity.ManagerWithTasks
import com.planner.core.data.entity.ProjectContributorCrossRef
import com.planner.core.data.entity.SavedContactEntity
import com.planner.core.data.entity.Task
import com.planner.core.data.entity.TaskEntity
import com.planner.core.data.entity.TaskManagerEntity
import com.planner.core.data.entity.TaskManagerWithTasksAndContributors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TestTaskManagerDao : TaskManagerDao {
    private val taskManagers: MutableMap<Long, TaskManagerEntity> = mutableMapOf()
    private val tasks: MutableMap<Long, List<TaskEntity>> = mutableMapOf()
    private val contacts: MutableMap<Long, SavedContactEntity> = mutableMapOf()
    private val contributors: MutableMap<Long, Set<Long>> = mutableMapOf()

    override fun getTaskManagers(): Flow<List<ManagerWithTasks>> =
        flow {
            emit(
                taskManagers.values.map {
                    ManagerWithTasks(
                        taskManager = it,
                        tasks = tasks[it.managerId].orEmpty(),
                    )
                },
            )
        }

    override fun getTaskManager(id: Long): Flow<ManagerWithTasks> =
        flow {
            taskManagers[id]?.let {
                emit(ManagerWithTasks(it, tasks[id].orEmpty()))
            }
        }

    override fun getTaskManagerWithContributors(id: Long): Flow<TaskManagerWithTasksAndContributors> =
        flow {
            val manager = taskManagers[id] ?: return@flow
            emit(
                TaskManagerWithTasksAndContributors(
                    taskManager = manager,
                    tasks = tasks[id].orEmpty(),
                    contributors = contributors[id].orEmpty().mapNotNull { contacts[it] },
                ),
            )
        }

    override suspend fun insertTasks(taskEntity: List<TaskEntity>) {
        if (taskEntity.isEmpty()) return
        val managerId = taskEntity.first().taskManagerId
        tasks.merge(managerId, taskEntity) { oldTasks, newTasks -> oldTasks + newTasks }
    }

    override suspend fun insertTaskManager(taskManagerEntity: TaskManagerEntity): Long {
        val id = if (taskManagerEntity.managerId > 0) taskManagerEntity.managerId else taskManagers.size.toLong() + 1
        taskManagers[id] = taskManagerEntity.copy(managerId = id)
        return id
    }

    override suspend fun upsertContacts(contacts: List<SavedContactEntity>) {
        contacts.forEach { this.contacts[it.contactId] = it }
    }

    override suspend fun insertProjectContributors(crossRefs: List<ProjectContributorCrossRef>) {
        crossRefs.groupBy { it.managerId }.forEach { (managerId, refs) ->
            val existing = contributors[managerId].orEmpty().toMutableSet()
            existing.addAll(refs.map { it.contactId })
            contributors[managerId] = existing
        }
    }

    override suspend fun deleteProjectContributorsByManagerId(managerId: Long) {
        contributors.remove(managerId)
    }

    override suspend fun replaceProjectContributors(
        managerId: Long,
        contributors: List<SavedContactEntity>,
    ) {
        deleteProjectContributorsByManagerId(managerId)
        if (contributors.isEmpty()) return
        upsertContacts(contributors)
        insertProjectContributors(
            contributors.map { ProjectContributorCrossRef(managerId = managerId, contactId = it.contactId) },
        )
    }

    override suspend fun updateTask(taskEntity: TaskEntity) {
        val managerId = taskEntity.taskManagerId
        tasks[managerId] =
            tasks[managerId]
                .orEmpty()
                .map { if (it.taskId == taskEntity.taskId) taskEntity else it }
    }

    override suspend fun updateTaskManager(taskManagerEntity: TaskManagerEntity) {
        taskManagers[taskManagerEntity.managerId] = taskManagerEntity
    }

    override suspend fun deleteTaskManager(taskManagerEntity: TaskManagerEntity) {
        taskManagers.remove(taskManagerEntity.managerId)
        tasks.remove(taskManagerEntity.managerId)
        contributors.remove(taskManagerEntity.managerId)
    }

    override suspend fun deleteTasks(taskEntities: List<TaskEntity>) {
        val managerId = taskEntities.firstOrNull()?.taskManagerId ?: return
        tasks[managerId] =
            tasks[managerId].orEmpty().filter { it.taskId !in taskEntities.map { it.taskId } }
    }

    override suspend fun deleteTasksByManagerId(managerId: Long) {
        tasks.remove(managerId)
    }
}
