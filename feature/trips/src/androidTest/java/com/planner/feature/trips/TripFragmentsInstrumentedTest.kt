package com.planner.feature.trips

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.textfield.TextInputEditText
import com.planner.core.data.dao.TripDao
import com.planner.core.data.database.PlannerDatabase
import com.planner.core.data.entity.TripEntity
import com.planner.core.domain.DatePattern
import com.planner.core.domain.FormatDateUseCase
import com.planner.feature.trips.fragment.AddTripFragment
import com.planner.feature.trips.fragment.AddTripFragmentArgs
import com.planner.feature.trips.fragment.ListTripFragment
import com.planner.feature.trips.fragment.TripDetailFragment
import com.planner.feature.trips.fragment.TripDetailFragmentArgs
import com.planner.test.android.createTestNavController
import com.planner.test.android.launchFragmentInHiltContainer
import com.planner.test.android.waitUntil
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TripFragmentsInstrumentedTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var database: PlannerDatabase

    @Inject lateinit var tripDao: TripDao

    @Before
    fun setUp() {
        hiltRule.inject()
        database.clearAllTables()
    }

    @After
    fun tearDown() {
        database.clearAllTables()
    }

    @Test
    fun listTripFragment_showsEmptyState_whenNoTripsExist() {
        val scenario = launchFragmentInHiltContainer<ListTripFragment>()

        onView(withId(R.id.no_trips_text)).check(matches(isDisplayed()))
        onView(withId(R.id.recycler_view)).check(matches(withEffectiveVisibility(GONE)))

        scenario.close()
    }

    @Test
    fun listTripFragment_showsTrips_andNavigatesToAddTrip() {
        seedTrip(
            TripEntity(
                tripId = 11,
                title = "Barcelona Break",
                destination = "Barcelona",
                departureTime = FormatDateUseCase().getTimeLong("20/03/2026")!!,
            ),
        )

        val navController = createNavController(
            graphId = R.navigation.trips_nav_graph,
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

    @Test
    fun addTripFragment_enablesSave_andCreatesTrip() {
        val navController = createNavController(
            graphId = R.navigation.trips_nav_graph,
            currentDestination = R.id.addTripFragment,
        )

        val scenario = launchFragmentInHiltContainer<AddTripFragment>(
            fragmentArgs = AddTripFragmentArgs(
                tripId = -1,
                title = R.string.add_a_trip,
            ).toBundle(),
            navController = navController,
        )

        onView(withId(R.id.save_button)).check(matches(org.hamcrest.Matchers.not(isEnabled())))
        onView(withId(R.id.trip_title_edit_text))
            .perform(replaceText("Summer Break"), closeSoftKeyboard())
        onView(withId(R.id.destination_edit_text))
            .perform(replaceText("Barcelona"), closeSoftKeyboard())
        onView(withId(R.id.save_button)).check(matches(org.hamcrest.Matchers.not(isEnabled())))

        scenario.onActivity { activity ->
            activity.findViewById<TextInputEditText>(R.id.departure_date_edit_text)
                .setText("20/03/2026")
        }

        onView(withId(R.id.save_button)).check(matches(isEnabled()))
        onView(withId(R.id.save_button)).perform(scrollTo(), click())

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
    fun addTripFragment_preloadsExistingTrip_andUpdatesIt() {
        val departureTime = FormatDateUseCase().getTimeLong("21/03/2026")!!
        seedTrip(
            TripEntity(
                tripId = 22,
                title = "Old Trip",
                destination = "Lagos",
                departureTime = departureTime,
            ),
        )

        val navController = createNavController(
            graphId = R.navigation.trips_nav_graph,
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
        onView(withId(R.id.save_button)).perform(scrollTo(), click())

        waitUntil {
            val trip = runBlocking { tripDao.getTrip(22).first() }
            trip.title == "Updated Trip" &&
                trip.destination == "Accra" &&
                navController.currentDestination?.id == R.id.listTripFragment
        }

        scenario.close()
    }

    @Test
    fun tripDetailFragment_rendersTrip_andNavigatesToEdit() {
        val departureTime = FormatDateUseCase().getTimeLong("25/03/2026")!!
        seedTrip(
            TripEntity(
                tripId = 33,
                title = "Rome Getaway",
                destination = "Rome",
                departureTime = departureTime,
            ),
        )

        val navController = createNavController(
            graphId = R.navigation.trips_nav_graph,
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
        onView(withText(FormatDateUseCase(DatePattern.LITERAL).format(departureTime)))
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
    fun tripDetailFragment_deletesTrip_andNavigatesBackToList() {
        seedTrip(
            TripEntity(
                tripId = 44,
                title = "Delete Me",
                destination = "Madrid",
                departureTime = FormatDateUseCase().getTimeLong("26/03/2026")!!,
            ),
        )

        val navController = createNavController(
            graphId = R.navigation.trips_nav_graph,
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

    private fun seedTrip(trip: TripEntity) = runBlocking {
        tripDao.insert(trip)
    }

    private fun createNavController(
        graphId: Int,
        currentDestination: Int,
    ) = createTestNavController(
        context = ApplicationProvider.getApplicationContext(),
        graphId = graphId,
        currentDestination = currentDestination,
    )
}
