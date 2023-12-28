package com.planner.core.data.di

import android.content.Context
import androidx.room.Room
import com.planner.core.data.dao.TaskManagerDao
import com.planner.core.data.dao.TripDao
import com.planner.core.data.database.PlannerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
