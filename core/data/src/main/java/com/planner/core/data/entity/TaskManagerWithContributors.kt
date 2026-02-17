package com.planner.core.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class TaskManagerWithTasksAndContributors(
    @Embedded val taskManager: TaskManagerEntity,
    @Relation(
        parentColumn = "manager_id",
        entityColumn = "task_manager_id",
    )
    val tasks: List<TaskEntity>,
    @Relation(
        parentColumn = "manager_id",
        entityColumn = "contact_id",
        associateBy =
            Junction(
                value = ProjectContributorCrossRef::class,
                parentColumn = "manager_id",
                entityColumn = "contact_id",
            ),
    )
    val contributors: List<SavedContactEntity>,
)
