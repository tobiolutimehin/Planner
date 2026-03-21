package com.planner.test.android

import android.content.Context
import androidx.room.Room
import com.planner.core.data.dao.TaskManagerDao
import com.planner.core.data.dao.TripDao
import com.planner.core.data.database.PlannerDatabase
import com.planner.core.data.di.DatabaseModule
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class],
)
object TestDatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): PlannerDatabase = Room
        .inMemoryDatabaseBuilder(context, PlannerDatabase::class.java)
        .allowMainThreadQueries()
        .build()

    @Provides
    fun provideTaskManagerDao(database: PlannerDatabase): TaskManagerDao = database.taskManagerDao()

    @Provides
    fun provideTripDao(database: PlannerDatabase): TripDao = database.tripDao()
}
