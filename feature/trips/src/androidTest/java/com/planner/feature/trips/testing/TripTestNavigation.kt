package com.planner.feature.trips.testing

import androidx.navigation.testing.TestNavHostController
import com.planner.feature.trips.R
import com.planner.test.android.createNavController

internal fun tripNavController(currentDestination: Int): TestNavHostController = createNavController(
    graphId = R.navigation.trips_nav_graph,
    currentDestination = currentDestination,
)
