package com.planner.feature.trips

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.planner.core.data.entity.TripEntity
import com.planner.core.domain.DatePattern
import com.planner.core.domain.FormatDateUseCase
import com.planner.feature.trips.fragment.AddTripFragmentArgs
import com.planner.feature.trips.fragment.TripDetailFragment
import com.planner.feature.trips.fragment.TripDetailFragmentArgs
import com.planner.feature.trips.testing.BaseTripFragmentTest
import com.planner.feature.trips.testing.departureTime
import com.planner.feature.trips.testing.seedTrip
import com.planner.feature.trips.testing.tripNavController
import com.planner.test.android.launchFragmentInHiltContainer
import com.planner.test.android.waitUntil
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TripDetailFragmentTest : BaseTripFragmentTest() {
    @Test
    fun rendersTrip_andNavigatesToEdit() {
        val departure = departureTime("25/03/2026")
        tripDao.seedTrip(
            TripEntity(
                tripId = 33,
                title = "Rome Getaway",
                destination = "Rome",
                departureTime = departure,
            ),
        )

        val navController = tripNavController(
            currentDestination = R.id.tripDetailFragment,
        )

        val scenario = launchFragmentInHiltContainer<TripDetailFragment>(
            fragmentArgs = TripDetailFragmentArgs(
                tripId = 33,
                fragmentTitle = "Rome Getaway",
            ).toBundle(),
            navController = navController,
        )

        onView(withText("Rome Getaway")).check(matches(isDisplayed()))
        onView(withText("Rome")).check(matches(isDisplayed()))
        onView(withText(FormatDateUseCase(DatePattern.LITERAL).format(departure)))
            .check(matches(isDisplayed()))
        onView(withId(R.id.trip_detail_edit_button)).perform(scrollTo(), click())

        waitUntil {
            navController.currentDestination?.id == R.id.addTripFragment
        }

        val arguments = requireNotNull(navController.currentBackStackEntry?.arguments)
        val editArgs = AddTripFragmentArgs.fromBundle(arguments)
        assertEquals(33, editArgs.tripId)
        assertEquals(R.string.edit_trip, editArgs.title)

        scenario.close()
    }

    @Test
    fun deletesTrip_andNavigatesBackToList() {
        tripDao.seedTrip(
            TripEntity(
                tripId = 44,
                title = "Delete Me",
                destination = "Madrid",
                departureTime = departureTime("26/03/2026"),
            ),
        )

        val navController = tripNavController(
            currentDestination = R.id.tripDetailFragment,
        )

        val scenario = launchFragmentInHiltContainer<TripDetailFragment>(
            fragmentArgs = TripDetailFragmentArgs(
                tripId = 44,
                fragmentTitle = "Delete Me",
            ).toBundle(),
            navController = navController,
        )

        onView(withId(R.id.trip_detail_delete_button)).perform(scrollTo(), click())
        onView(withText(com.planner.core.ui.R.string.yes)).perform(click())

        waitUntil {
            runBlocking { tripDao.getTrips().first().isEmpty() } &&
                navController.currentDestination?.id == R.id.listTripFragment
        }

        scenario.close()
    }
}
