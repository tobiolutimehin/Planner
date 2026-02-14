package com.planner.core.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "saved_contact",
)
data class SavedContactEntity(
    @PrimaryKey
    @ColumnInfo(name = "contact_id")
    val contactId: Long,
    val name: String,
    val phone: String,
)

@Entity(
    tableName = "project_contributor",
    primaryKeys = ["manager_id", "contact_id"],
    foreignKeys = [
        ForeignKey(
            entity = TaskManagerEntity::class,
            parentColumns = ["manager_id"],
            childColumns = ["manager_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SavedContactEntity::class,
            parentColumns = ["contact_id"],
            childColumns = ["contact_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("manager_id"), Index("contact_id")],
)
data class ProjectContributorCrossRef(
    @ColumnInfo(name = "manager_id")
    val managerId: Long,
    @ColumnInfo(name = "contact_id")
    val contactId: Long,
)

@Entity(
    tableName = "trip_mate",
    primaryKeys = ["trip_id", "contact_id"],
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["trip_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SavedContactEntity::class,
            parentColumns = ["contact_id"],
            childColumns = ["contact_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("trip_id"), Index("contact_id")],
)
data class TripMateCrossRef(
    @ColumnInfo(name = "trip_id")
    val tripId: Int,
    @ColumnInfo(name = "contact_id")
    val contactId: Long,
)
