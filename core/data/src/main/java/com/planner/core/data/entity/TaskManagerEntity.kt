package com.planner.core.data.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "task_manager")
data class TaskManagerEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "manager_id")
    val managerId: Long = 0,
    val name: String,
    val type: TaskManagerType = TaskManagerType.TODO_LIST,
)

data class ManagerWithTasks(
    @Embedded val taskManager: TaskManagerEntity,
    @Relation(
        parentColumn = "manager_id",
        entityColumn = "task_manager_id",
    )
    val tasks: List<TaskEntity>,
)

enum class TaskManagerType { TODO_LIST, PROJECT }
