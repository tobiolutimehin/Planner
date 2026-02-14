package com.planner.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.planner.core.data.entity.ManagerWithTasks
import com.planner.core.data.entity.ProjectContributorCrossRef
import com.planner.core.data.entity.SavedContactEntity
import com.planner.core.data.entity.Task
import com.planner.core.data.entity.TaskEntity
import com.planner.core.data.entity.TaskManagerEntity
import com.planner.core.data.entity.TaskManagerWithTasksAndContributors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Dao
interface TaskManagerDao {
    @Transaction
    @Query("SELECT * FROM task_manager ORDER BY manager_id DESC")
    fun getTaskManagers(): Flow<List<ManagerWithTasks>>

    @Transaction
    @Query("SELECT * FROM task_manager WHERE manager_id = :id")
    fun getTaskManager(id: Long): Flow<ManagerWithTasks>

    @Transaction
    @Query("SELECT * FROM task_manager WHERE manager_id = :id")
    fun getTaskManagerWithContributors(id: Long): Flow<TaskManagerWithTasksAndContributors>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(taskEntity: List<TaskEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTaskManager(taskManagerEntity: TaskManagerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertContacts(contacts: List<SavedContactEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjectContributors(crossRefs: List<ProjectContributorCrossRef>)

    @Query("DELETE FROM project_contributor WHERE manager_id = :managerId")
    suspend fun deleteProjectContributorsByManagerId(managerId: Long)

    @Transaction
    suspend fun replaceProjectContributors(
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

    @Transaction
    suspend fun insertTaskManagerWithTasks(
        taskManager: TaskManagerEntity,
        tasks: List<Task>,
        contributors: List<SavedContactEntity>,
    ): Long {
        val taskManagerId = insertTaskManager(taskManager)
        val taskEntities = tasks.map { it.toTaskEntity(taskManagerId) }
        insertTasks(taskEntities)
        replaceProjectContributors(taskManagerId, contributors)
        return taskManagerId
    }

    @Update
    suspend fun updateTask(taskEntity: TaskEntity)

    @Update
    suspend fun updateTaskManager(taskManagerEntity: TaskManagerEntity)

    @Transaction
    suspend fun updateTaskManagerWithTasks(
        taskManagerEntity: TaskManagerEntity,
        tasks: List<Task>,
        contributors: List<SavedContactEntity>,
    ) {
        val mWithTasks = getTaskManager(taskManagerEntity.managerId)
        deleteTasks(mWithTasks.first().tasks)
        updateTaskManager(taskManagerEntity)
        insertTasks(tasks.toTaskEntities(taskManagerEntity.managerId))
        replaceProjectContributors(taskManagerEntity.managerId, contributors)
    }

    @Delete
    suspend fun deleteTaskManager(taskManagerEntity: TaskManagerEntity)

    @Delete
    suspend fun deleteTasks(taskEntities: List<TaskEntity>)

    @Transaction
    suspend fun deleteTaskManagerWithTasks(taskManager: TaskManagerEntity) {
        deleteTasksByManagerId(taskManager.managerId)
        deleteProjectContributorsByManagerId(taskManager.managerId)
        deleteTaskManager(taskManager)
    }

    @Query("DELETE FROM task WHERE task_manager_id = :managerId")
    suspend fun deleteTasksByManagerId(managerId: Long)
}

private fun List<Task>.toTaskEntities(managerId: Long): List<TaskEntity> =
    map { it.toTaskEntity(managerId) }
