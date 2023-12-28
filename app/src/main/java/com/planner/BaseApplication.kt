package com.planner

import android.app.Application
import com.planner.core.data.database.PlannerDatabase
import dagger.hilt.android.HiltAndroidApp

/**
 * The base application class for the Planner app.
 */
@HiltAndroidApp
class BaseApplication : Application() {

    /**
     * The instance of the [PlannerDatabase].
     */
    val database by lazy { PlannerDatabase.getDatabase(context = this@BaseApplication) }
}
