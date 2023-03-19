package com.planner.core.data.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.planner.core.data.entity.TaskManagerType.PROJECT
import com.planner.core.data.entity.TaskManagerType.TODO_LIST

/**
 * Represents a Task Manager stored in the local database.
 * @property managerId The unique identifier of the Task Manager.
 * @property name The name of the Task Manager.
 * @property type The type of the Task Manager,
 * can be [TaskManagerType.TODO_LIST] or [TaskManagerType.PROJECT].
 */
@Entity(tableName = "task_manager")
data class TaskManagerEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "manager_id")
    val managerId: Long = 0,
    val name: String,
    val type: TaskManagerType = TODO_LIST,
)

/**
 * Represents a Task Manager with its associated Tasks.
 * @property taskManager The [TaskManagerEntity] associated with the Tasks.
 * @property tasks The list of [TaskEntity] associated with the Task Manager.
 */
data class ManagerWithTasks(
    @Embedded val taskManager: TaskManagerEntity,
    @Relation(
        parentColumn = "manager_id",
        entityColumn = "task_manager_id",
    )
    val tasks: List<TaskEntity>,
)

/**
 * Represents the types of Task Managers.
 * Can be [TODO_LIST] or [PROJECT].
 */
enum class TaskManagerType { TODO_LIST, PROJECT }
