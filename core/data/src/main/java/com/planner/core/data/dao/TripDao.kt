package com.planner.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.planner.core.data.entity.SavedContactEntity
import com.planner.core.data.entity.TripEntity
import com.planner.core.data.entity.TripMateCrossRef
import com.planner.core.data.entity.TripWithMates
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trip: TripEntity): Long

    @Delete
    suspend fun delete(trip: TripEntity)

    @Query("SELECT * FROM trip ORDER BY departure_time DESC")
    fun getTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trip where id = :id")
    fun getTrip(id: Int): Flow<TripEntity>

    @Transaction
    @Query("SELECT * FROM trip where id = :id")
    fun getTripWithMates(id: Int): Flow<TripWithMates>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertContacts(contacts: List<SavedContactEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTripMates(crossRefs: List<TripMateCrossRef>)

    @Query("DELETE FROM trip_mate WHERE trip_id = :tripId")
    suspend fun deleteTripMatesByTripId(tripId: Int)

    @Transaction
    suspend fun replaceTripMates(
        tripId: Int,
        mates: List<SavedContactEntity>,
    ) {
        deleteTripMatesByTripId(tripId)
        if (mates.isEmpty()) return

        upsertContacts(mates)
        insertTripMates(
            mates.map { TripMateCrossRef(tripId = tripId, contactId = it.contactId) },
        )
    }

    @Transaction
    suspend fun insertTripWithMates(
        trip: TripEntity,
        mates: List<SavedContactEntity>,
    ): Int {
        val tripId = insert(trip).toInt()
        replaceTripMates(tripId, mates)
        return tripId
    }

    @Update
    suspend fun update(trip: TripEntity)

    @Transaction
    suspend fun updateTripWithMates(
        trip: TripEntity,
        mates: List<SavedContactEntity>,
    ) {
        update(trip)
        replaceTripMates(trip.tripId, mates)
    }
}
