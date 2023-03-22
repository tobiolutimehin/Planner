package com.planner.core.ui

import android.app.Application
import com.planner.core.data.database.PlannerDatabase

/**
 * The base application class for the Planner app.
 */
class BaseApplication : Application() {

    /**
     * The instance of the [PlannerDatabase].
     */
    val database by lazy { PlannerDatabase.getDatabase(context = this@BaseApplication) }
}
