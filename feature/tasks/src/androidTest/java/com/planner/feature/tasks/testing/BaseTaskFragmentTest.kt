package com.planner.feature.tasks.testing

import com.planner.core.data.dao.TaskManagerDao
import com.planner.core.data.database.PlannerDatabase
import dagger.hilt.android.testing.HiltAndroidRule
import javax.inject.Inject
import org.junit.After
import org.junit.Before
import org.junit.Rule

abstract class BaseTaskFragmentTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var database: PlannerDatabase

    @Inject lateinit var taskManagerDao: TaskManagerDao

    @Before
    fun injectAndClearDatabase() {
        hiltRule.inject()
        database.clearAllTables()
    }

    @After
    fun clearDatabase() {
        database.clearAllTables()
    }
}
