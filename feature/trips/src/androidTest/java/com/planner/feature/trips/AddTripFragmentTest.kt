package com.planner.feature.trips

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.textfield.TextInputEditText
import com.planner.core.data.entity.TripEntity
import com.planner.feature.trips.fragment.AddTripFragment
import com.planner.feature.trips.fragment.AddTripFragmentArgs
import com.planner.feature.trips.testing.BaseTripFragmentTest
import com.planner.feature.trips.testing.departureTime
import com.planner.feature.trips.testing.seedTrip
import com.planner.feature.trips.testing.tripNavController
import com.planner.test.android.launchFragmentInHiltContainer
import com.planner.test.android.waitUntil
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AddTripFragmentTest : BaseTripFragmentTest() {
    @Test
    fun enablesSave_andCreatesTrip() {
        val navController = tripNavController(
            currentDestination = R.id.addTripFragment,
        )

        val scenario = launchFragmentInHiltContainer<AddTripFragment>(
            fragmentArgs = AddTripFragmentArgs(
                tripId = -1,
                title = R.string.add_a_trip,
            ).toBundle(),
            navController = navController,
        )

        onView(withId(R.id.save_button)).check(matches(not(isEnabled())))
        onView(withId(R.id.trip_title_edit_text))
            .perform(replaceText("Summer Break"), closeSoftKeyboard())
        onView(withId(R.id.destination_edit_text))
            .perform(replaceText("Barcelona"), closeSoftKeyboard())
        onView(withId(R.id.save_button)).check(matches(not(isEnabled())))

        scenario.onActivity { activity ->
            activity.findViewById<TextInputEditText>(R.id.departure_date_edit_text)
                .setText("20/03/2026")
        }

        onView(withId(R.id.save_button)).check(matches(isEnabled()))
        onView(withId(R.id.save_button)).perform(scrollTo(), androidx.test.espresso.action.ViewActions.click())

        waitUntil {
            val trips = runBlocking { tripDao.getTrips().first() }
            trips.any {
                it.title == "Summer Break" &&
                    it.destination == "Barcelona"
            } && navController.currentDestination?.id == R.id.listTripFragment
        }

        scenario.close()
    }

    @Test
    fun preloadsExistingTrip_andUpdatesIt() {
        tripDao.seedTrip(
            TripEntity(
                tripId = 22,
                title = "Old Trip",
                destination = "Lagos",
                departureTime = departureTime("21/03/2026"),
            ),
        )

        val navController = tripNavController(
            currentDestination = R.id.addTripFragment,
        )

        val scenario = launchFragmentInHiltContainer<AddTripFragment>(
            fragmentArgs = AddTripFragmentArgs(
                tripId = 22,
                title = R.string.edit_trip,
            ).toBundle(),
            navController = navController,
        )

        waitUntil {
            var loadedTitle = ""
            scenario.onActivity { activity ->
                loadedTitle = activity.findViewById<TextInputEditText>(R.id.trip_title_edit_text)
                    .text?.toString().orEmpty()
            }
            loadedTitle == "Old Trip"
        }

        onView(withId(R.id.trip_title_edit_text))
            .perform(replaceText("Updated Trip"), closeSoftKeyboard())
        onView(withId(R.id.destination_edit_text))
            .perform(replaceText("Accra"), closeSoftKeyboard())
        onView(withId(R.id.save_button)).perform(scrollTo(), androidx.test.espresso.action.ViewActions.click())

        waitUntil {
            val trip = runBlocking { tripDao.getTrip(22).first() }
            trip.title == "Updated Trip" &&
                trip.destination == "Accra" &&
                navController.currentDestination?.id == R.id.listTripFragment
        }

        scenario.close()
    }
}
