package com.planner.core.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity class representing a trip that can be persisted in the local database.
 * @param tripId the unique identifier for the trip
 * @param tripImageUrl the URL of the image associated with the trip, can be null
 * @param title the title of the trip
 * @param destination the destination of the trip
 * @param departureTime the time at which the trip departs
 * @param arrivalTime the time at which the trip arrives, can be null
 * @param notes additional notes about the trip, can be null
 */
@Entity(tableName = "trip")
data class TripEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val tripId: Int = 0,
    @ColumnInfo(name = "image_url")
    val tripImageUrl: String? = null,
    val title: String,
    val destination: String,
    @ColumnInfo(name = "departure_time")
    val departureTime: Long,
    @ColumnInfo(name = "arrival_time")
    val arrivalTime: Long? = null,
    val notes: String? = null,
)
