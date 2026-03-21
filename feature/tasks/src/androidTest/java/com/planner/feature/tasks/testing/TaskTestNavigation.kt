package com.planner.feature.tasks.testing

import androidx.navigation.testing.TestNavHostController
import com.planner.feature.tasks.R
import com.planner.test.android.createNavController

internal fun taskNavController(currentDestination: Int): TestNavHostController = createNavController(
    graphId = R.navigation.tasks_nav_graph,
    currentDestination = currentDestination,
)
