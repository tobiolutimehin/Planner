package com.planner.core.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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
    val notes: String? = null
)
