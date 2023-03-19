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

/**
 * Data Access Object for managing [TaskManagerEntity] and related [TaskEntity] entities in the database.
 */
@Dao
interface TaskManagerDao {

    /**
     * Returns a [Flow] of all [TaskManagerEntity]s with their associated tasks, ordered by manager_id in descending order.
     */
    @Transaction
    @Query("SELECT * FROM task_manager ORDER BY manager_id DESC")
    fun getTaskManagers(): Flow<List<ManagerWithTasks>>

    /**
     * Returns a [Flow] of a specific [TaskManagerEntity] with its associated tasks.
     *
     * @param id The id of the [TaskManagerEntity] to retrieve.
     */
    @Transaction
    @Query("SELECT * FROM task_manager WHERE :id == manager_id")
    fun getTaskManager(id: Long): Flow<ManagerWithTasks>

    /**
     * Inserts a list of [TaskEntity] objects into the database.
     *
     * @param taskEntity The list of [TaskEntity] objects to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(taskEntity: List<TaskEntity>)

    /**
     * Inserts a [TaskManagerEntity] into the database, ignoring any conflicts.
     *
     * @param taskManagerEntity The [TaskManagerEntity] to insert.
     * @return The id of the inserted [TaskManagerEntity].
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTaskManager(taskManagerEntity: TaskManagerEntity): Long

    /**
     * Inserts a [TaskManagerEntity] with its associated [Task]s into the database.
     *
     * @param taskManager The [TaskManagerEntity] to insert.
     * @param tasks The list of [Task] objects to associate with the [TaskManagerEntity].
     */
    @Transaction
    suspend fun insertTaskManagerWithTasks(taskManager: TaskManagerEntity, tasks: List<Task>) {
        val taskManagerId = insertTaskManager(taskManager)

        val taskEntities = tasks.map { it.toTaskEntity(taskManagerId) }
        insertTasks(taskEntities)
    }

    /**
     * Updates a [TaskEntity] in the database.
     *
     * @param taskEntity The [TaskEntity] to update.
     */
    @Update
    suspend fun updateTask(taskEntity: TaskEntity)

    /**
     * Updates a [TaskManagerEntity] in the database.
     *
     * @param taskManagerEntity The [TaskManagerEntity] to update.
     */
    @Update
    suspend fun updateTaskManager(taskManagerEntity: TaskManagerEntity)

    /***
     * Updates a task manager along with its associated tasks.
     *
     * @param taskManagerEntity The [TaskManagerEntity] object to update.
     * @param tasks The list of [Task] objects to update.
     */
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

    /**
     * Deletes a task manager from the TaskManagerEntity table.
     * @param taskManagerEntity The [TaskManagerEntity] object to delete.
     */
    @Delete
    suspend fun deleteTaskManager(taskManagerEntity: TaskManagerEntity)

    /**
     * Deletes a list of tasks from the TaskEntity table.
     *
     * @param taskEntities The list of [TaskEntity] objects to delete.
     */
    @Delete
    suspend fun deleteTasks(taskEntities: List<TaskEntity>)

    /**
     * Deletes the given [taskManager] and its associated tasks in a transaction.
     *
     * @param taskManager the [TaskManagerEntity] to delete.
     */
    @Transaction
    suspend fun deleteTaskManagerWithTasks(taskManager: TaskManagerEntity) {
        deleteTasksByManagerId(taskManager.managerId)
        deleteTaskManager(taskManager)
    }

    /**
     * Deletes all tasks associated with the [TaskManagerEntity] with the given [managerId].
     *
     * @param managerId the ID of the [TaskManagerEntity] whose tasks should be deleted.
     */
    @Query("DELETE FROM task WHERE task_manager_id = :managerId")
    suspend fun deleteTasksByManagerId(managerId: Long)
}

/**
 * Converts a list of [Task] objects to a list of [TaskEntity] objects associated with the given [managerId].
 *
 * @param managerId the ID of the [TaskManagerEntity] to which the tasks belong.
 * @return a list of [TaskEntity] objects associated with the given [managerId].
 */
private fun List<Task>.toTaskEntities(managerId: Long): List<TaskEntity> =
    this.map { it.toTaskEntity(managerId) }
