package com.planner.core.data.dao

import android.content.Context
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.Update
import com.planner.core.data.database.PlannerDatabase
import com.planner.core.data.entity.TripEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

/**
 * Data Access Object ([Dao]) for managing [TripEntity] objects in the database.
 */
@Dao
interface TripDao {
    /**
     * Inserts a new trip into the database. If a trip with the same id already exists, it will be ignored.
     * @param trip The trip to insert.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(trip: TripEntity)

    /**
     * Deletes the specified trip from the database.
     * @param trip The trip to delete.
     */
    @Delete
    suspend fun delete(trip: TripEntity)

    /**
     * Retrieves a list of all trips in the database, ordered by departure time in descending order.
     * @return A [Flow] of [List] of [TripEntity] objects.
     */
    @Query("SELECT * FROM trip ORDER BY departure_time DESC")
    fun getTrips(): Flow<List<TripEntity>>

    /**
     * Retrieves the trip with the specified id from the database.
     * @param id The id of the trip to retrieve.
     * @return A [Flow] of [TripEntity] object with the specified id.
     */
    @Query("SELECT * FROM trip where id = :id")
    fun getTrip(id: Int): Flow<TripEntity>

    /**
     * Updates the specified trip in the database.
     * @param trip The trip to update.
     */
    @Update
    suspend fun update(trip: TripEntity)
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun providesNiaDatabase(
        @ApplicationContext context: Context,
    ): PlannerDatabase = Room
        .databaseBuilder(
            context,
            PlannerDatabase::class.java,
            "planner_database",
        )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun providesTripDao(
        database: PlannerDatabase,
    ): TripDao = database.tripDao()

    @Provides
    fun providesTaskManagerDao(
        database: PlannerDatabase,
    ): TaskManagerDao = database.taskManagerDao()
}
