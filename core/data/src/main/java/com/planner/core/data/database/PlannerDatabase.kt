package com.planner.core.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.planner.core.data.DataConverters
import com.planner.core.data.dao.TaskManagerDao
import com.planner.core.data.dao.TripDao
import com.planner.core.data.entity.TaskEntity
import com.planner.core.data.entity.TaskManagerEntity
import com.planner.core.data.entity.TripEntity

@Database(
    entities = [TripEntity::class, TaskManagerEntity::class, TaskEntity::class],
    version = 5,
    exportSchema = false,
)
@TypeConverters(DataConverters::class)
abstract class PlannerDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun taskManagerDao(): TaskManagerDao

    companion object {
        private var INSTANCE: PlannerDatabase? = null

        fun getDatabase(context: Context): PlannerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room
                    .databaseBuilder(
                        context,
                        PlannerDatabase::class.java,
                        "planner_database",
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
