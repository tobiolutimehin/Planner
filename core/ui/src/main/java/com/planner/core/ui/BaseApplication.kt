package com.planner.core.ui

import android.app.Application
import com.planner.core.data.database.PlannerDatabase

class BaseApplication : Application() {
    val database by lazy { PlannerDatabase.getDatabase(context = this@BaseApplication) }
}