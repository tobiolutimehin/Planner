package com.planner.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.planner.core.data.entity.ManagerWithTasks
import com.planner.core.data.entity.Task
import com.planner.core.data.entity.TaskEntity
import com.planner.core.data.entity.TaskManagerEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Dao
interface TaskManagerDao {
    @Transaction
    @Query("SELECT * FROM task_manager ORDER BY manager_id DESC")
    fun getTaskManagers(): Flow<List<ManagerWithTasks>>

    @Transaction
    @Query("SELECT * FROM task_manager WHERE :id == manager_id")
    fun getTaskManager(id: Long): Flow<ManagerWithTasks>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(taskEntity: List<TaskEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTaskManager(taskManagerEntity: TaskManagerEntity): Long

    @Transaction
    suspend fun insertTaskManagerWithTasks(taskManager: TaskManagerEntity, tasks: List<Task>) {
        val taskManagerId = insertTaskManager(taskManager)

        val taskEntities = tasks.map { it.toTaskEntity(taskManagerId) }
        insertTasks(taskEntities)
    }

    @Update
    suspend fun updateTask(taskEntity: TaskEntity)

    @Update
    suspend fun updateTaskManager(taskManagerEntity: TaskManagerEntity)

    @Transaction
    suspend fun updateTaskManagerWithTasks(
        taskManagerEntity: TaskManagerEntity,
        tasks: List<Task>,
    ) {
        val mWithTasks = getTaskManager(taskManagerEntity.managerId)
        deleteTasks(mWithTasks.first().tasks)
        updateTaskManager(taskManagerEntity)
        insertTasks(tasks.toTaskEntities(taskManagerEntity.managerId))
    }

    @Delete
    suspend fun deleteTaskManager(taskManagerEntity: TaskManagerEntity)

    @Delete
    suspend fun deleteTasks(taskEntities: List<TaskEntity>)

    @Transaction
    suspend fun deleteTaskManagerWithTasks(taskManager: TaskManagerEntity) {
        deleteTasksByManagerId(taskManager.managerId)
        deleteTaskManager(taskManager)
    }

    @Query("DELETE FROM task WHERE task_manager_id = :managerId")
    suspend fun deleteTasksByManagerId(managerId: Long)
}

private fun List<Task>.toTaskEntities(managerId: Long): List<TaskEntity> =
    this.map { it.toTaskEntity(managerId) }
