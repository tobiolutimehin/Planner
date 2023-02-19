package com.planner.core.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "task_id")
    val taskId: Int = 0,
    val description: String,
    @ColumnInfo(name = "is_done")
    val isDone: Boolean = false,
    val contributor: String? = null,
    @ColumnInfo(name = "task_manager_id")
    val taskManagerId: Long,
) {
    fun toTask() =
        Task(description = this.description, contributor = this.contributor, isDone = this.isDone)
}

data class Task(
    val id: Long? = null,
    val description: String,
    val contributor: String? = null,
    val isDone: Boolean = false,
) {
    fun toTaskEntity(taskManagerId: Long) = TaskEntity(
        description = this.description,
        isDone = this.isDone,
        contributor = this.contributor,
        taskManagerId = taskManagerId,
    )
}
