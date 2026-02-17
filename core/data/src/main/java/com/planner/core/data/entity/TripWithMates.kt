package com.planner.core.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class TripWithMates(
    @Embedded val trip: TripEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "contact_id",
        associateBy =
            Junction(
                value = TripMateCrossRef::class,
                parentColumn = "trip_id",
                entityColumn = "contact_id",
            ),
    )
    val mates: List<SavedContactEntity>,
)
