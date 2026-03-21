package com.planner.feature.trips

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.planner.core.data.entity.TripEntity
import com.planner.feature.trips.fragment.ListTripFragment
import com.planner.feature.trips.testing.BaseTripFragmentTest
import com.planner.feature.trips.testing.departureTime
import com.planner.feature.trips.testing.seedTrip
import com.planner.feature.trips.testing.tripNavController
import com.planner.test.android.launchFragmentInHiltContainer
import com.planner.test.android.waitUntil
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ListTripFragmentTest : BaseTripFragmentTest() {
    @Test
    fun showsEmptyState_whenNoTripsExist() {
        val scenario = launchFragmentInHiltContainer<ListTripFragment>()

        onView(withId(R.id.no_trips_text)).check(matches(isDisplayed()))
        onView(withId(R.id.recycler_view)).check(matches(withEffectiveVisibility(GONE)))

        scenario.close()
    }

    @Test
    fun showsTrips_andNavigatesToAddTrip() {
        tripDao.seedTrip(
            TripEntity(
                tripId = 11,
                title = "Barcelona Break",
                destination = "Barcelona",
                departureTime = departureTime("20/03/2026"),
            ),
        )

        val navController = tripNavController(
            currentDestination = R.id.listTripFragment,
        )

        val scenario = launchFragmentInHiltContainer<ListTripFragment>(
            navController = navController,
        )

        onView(withText("Barcelona")).check(matches(isDisplayed()))
        onView(withId(R.id.no_trips_text)).check(matches(withEffectiveVisibility(GONE)))
        onView(withId(R.id.recycler_view)).check(matches(isDisplayed()))
        onView(withId(R.id.fab)).perform(click())

        waitUntil {
            navController.currentDestination?.id == R.id.addTripFragment
        }

        scenario.close()
    }
}
