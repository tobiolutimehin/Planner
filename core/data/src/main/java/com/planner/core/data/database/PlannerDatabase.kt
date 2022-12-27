package com.planner.core.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.planner.core.data.dao.TripDao
import com.planner.core.data.entity.TripEntity

@Database(entities = [TripEntity::class], version = 1, exportSchema = false)
abstract class PlannerDatabase: RoomDatabase() {
    abstract fun tripDao(): TripDao

    companion object {
        private var INSTANCE: PlannerDatabase? = null

        fun getDatabase(context: Context): PlannerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    PlannerDatabase::class.java,
                    "planner_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }
        }

    }

}
