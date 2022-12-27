package com.planner.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.planner.core.data.entity.TripEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(trip: TripEntity)

    @Delete
    suspend fun delete(trip: TripEntity)

    @Query("SELECT * FROM trip ORDER BY departure_time DESC")
    fun getTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trip where id = :id")
    fun getTrip(id: Int): Flow<TripEntity>

    @Update
    suspend fun update(trip: TripEntity)
}