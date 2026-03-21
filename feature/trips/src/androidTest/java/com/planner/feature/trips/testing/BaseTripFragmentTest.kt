package com.planner.feature.trips.testing

import com.planner.core.data.dao.TripDao
import com.planner.core.data.database.PlannerDatabase
import dagger.hilt.android.testing.HiltAndroidRule
import javax.inject.Inject
import org.junit.After
import org.junit.Before
import org.junit.Rule

abstract class BaseTripFragmentTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var database: PlannerDatabase

    @Inject lateinit var tripDao: TripDao

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
