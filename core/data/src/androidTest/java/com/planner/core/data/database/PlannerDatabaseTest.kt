package com.planner.core.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.planner.core.data.dao.TripDao
import com.planner.core.data.entity.TripEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/** Test class for [PlannerDatabase] */
@RunWith(AndroidJUnit4::class)
class PlannerDatabaseTest {
    private lateinit var tripDao: TripDao
    private lateinit var db: PlannerDatabase

    private val usaTrip = TripEntity(
        tripId = 1,
        tripImageUrl = null,
        title = "Go to USA",
        destination = "Maryland",
        departureTime = 100000L,
        arrivalTime = null,
        notes = null,
    )

    private val nigeriaTrip = TripEntity(
        tripId = 2,
        tripImageUrl = null,
        title = "Go to Nigeria",
        destination = "Maryland",
        departureTime = 100000L,
        arrivalTime = null,
        notes = null,
    )

    /** Create the [PlannerDatabase] before each test is run */
    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            PlannerDatabase::class.java,
        ).build()
        tripDao = db.tripDao()
    }

    /** Close the [PlannerDatabase] after each test is run */
    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    /** Test [PlannerDatabase.tripDao] is empty by calling */
    @Test
    fun `trip dao is empty`() = runBlocking {
        assertTrue(tripDao.getTrips().first().isEmpty())
    }

    /** Test insert a new trip to the [PlannerDatabase.tripDao] */
    @Test
    fun `insert a new trip`() = runBlocking {
        tripDao.insert(usaTrip)

        val trips = tripDao.getTrips().first()

        assertTrue(trips.isNotEmpty())
    }

    /** Test insert conflicting new trip to the [PlannerDatabase.tripDao] */
    @Test
    fun `insert conflicting new trip`() = runBlocking {
        tripDao.insert(usaTrip)
        tripDao.insert(usaTrip)

        val trips = tripDao.getTrips().first()

        assertTrue(trips.size < 2)
    }

    /** Test insert two new trips to the [PlannerDatabase.tripDao] */
    @Test
    fun `insert two new trips`() = runBlocking {
        tripDao.insert(usaTrip)
        tripDao.insert(nigeriaTrip)

        val trips = tripDao.getTrips().first()

        assertTrue(trips.size == 2)
    }

    /** Test fetch a new trip to the [PlannerDatabase.tripDao] */
    @Test
    fun `fetch a new trip`() = runBlocking {
        tripDao.insert(usaTrip)

        val trips = tripDao.getTrips().first()

        assertEquals(trips.first().tripId, 1)
    }

    /** Test fetch exact trip to the [PlannerDatabase.tripDao] */
    @Test
    fun `fetch exact trip`() = runBlocking {
        tripDao.insert(usaTrip)
        tripDao.insert(nigeriaTrip)

        val nigerianTrip = tripDao.getTrip(2).first()

        assertEquals(nigerianTrip.title, "Go to Nigeria")
    }

    /** Test fetch unavailable trip to the [PlannerDatabase.tripDao] */
    @Test
    fun `fetch unavailable trip`() = runBlocking {
        val nigerianTrip = tripDao.getTrip(2).first()

        assertNull(nigerianTrip)
    }

    /** Test delete trip from the [PlannerDatabase.tripDao] */
    @Test
    fun `delete trip`() = runBlocking {
        tripDao.insert(usaTrip)
        tripDao.delete(usaTrip)

        assertTrue(tripDao.getTrips().first().isEmpty())
    }

    /** Test delete unavailable trip from the [PlannerDatabase.tripDao] */
    @Test
    fun `delete unavailable trip`() = runBlocking {
        tripDao.insert(usaTrip)
        tripDao.delete(nigeriaTrip)

        assertTrue(tripDao.getTrips().first().isNotEmpty())
    }

    /** Test update trip from the [PlannerDatabase.tripDao] */
    @Test
    fun `update trip`() = runBlocking {
        val updatedUsaTrip = TripEntity(
            tripId = 1,
            tripImageUrl = null,
            title = "Go to Nigeria",
            destination = "Maryland",
            departureTime = 100000L,
            arrivalTime = null,
            notes = null,
        )

        tripDao.insert(usaTrip)
        tripDao.update(updatedUsaTrip)

        assertEquals(tripDao.getTrip(1).first().title, "Go to Nigeria")
    }

    /** Test update unavailable trip in the [PlannerDatabase.tripDao] */
    @Test
    fun `update unavailable trip`() = runBlocking {
        val unavailableTrip = TripEntity(
            tripId = 2,
            tripImageUrl = null,
            title = "Go to Nigeria",
            destination = "Maryland",
            departureTime = 100000L,
            arrivalTime = null,
            notes = null,
        )

        tripDao.insert(usaTrip)
        tripDao.update(unavailableTrip)

        assertNull(tripDao.getTrip(2).first())
    }
}
