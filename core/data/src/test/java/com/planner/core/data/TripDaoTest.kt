package com.planner.core.data

import com.planner.core.data.dao.TestTripDao
import com.planner.core.data.entity.TripEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test class for [TestTripDao]
 */
class TripDaoTest {
    private lateinit var tripDao: TestTripDao

    private val nigeriaTrip = TripEntity(
        tripId = 2,
        tripImageUrl = null,
        title = "Go to Nigeria",
        destination = "Maryland",
        departureTime = 100000L,
        arrivalTime = null,
        notes = null
    )

    private val usaTrip = TripEntity(
        tripId = 1,
        tripImageUrl = null,
        title = "Go to USA",
        destination = "Maryland",
        departureTime = 100000L,
        arrivalTime = null,
        notes = null
    )


    /** create dao */
    @Before
    fun setupDao() { tripDao = TestTripDao() }

    /** test fetch trips */
    @Test
    fun `fetch trips`() = runBlocking {
        assertEquals(tripDao.getTrips().first().first().tripId, 1)
    }

    /** test get nonexistent trip that should throw a [NoSuchElementException] */
    @Test(expected = NoSuchElementException::class)
    fun `get nonexistent trip`() {
        runBlocking {
            tripDao.getTrip(10).first()
        }
    }

    /** test fetch trip using a valid trip id */
    @Test
    fun `fetch trip`() = runBlocking {
        val trip = tripDao.getTrip(1).first()
        assertEquals(trip.title, "Go to USA")
    }

    /** test insert trip */
    @Test
    fun `insert trip`() = runBlocking {
        tripDao.insert(nigeriaTrip)
        val trip = tripDao.getTrip(2).first()

        assertTrue(trip.tripId == 2)
        assertEquals(trip.title, "Go to Nigeria")
    }

    /** test nsert conflicting trip */
    @Test
    fun `insert conflicting trip`() = runBlocking {
        tripDao.insert(usaTrip)

        val trips = tripDao.getTrips().first()

        assertTrue(trips.size < 2)
    }

    /** test delete trip */
    @Test
    fun `delete trip`() = runBlocking {
        tripDao.insert(usaTrip)
        tripDao.delete(usaTrip)

        assertTrue(tripDao.getTrips().first().isEmpty())
    }

    /** test delete unavailable trip */
    @Test
    fun `delete unavailable trip`() = runBlocking {
        tripDao.delete(nigeriaTrip)

        assertTrue(tripDao.getTrips().first().isNotEmpty())
    }

    /** test update trip */
    @Test
    fun `update trip`() = runBlocking {
        val updatedUsaTrip = TripEntity(
            tripId = 1,
            tripImageUrl = null,
            title = "Go to Nigeria",
            destination = "Maryland",
            departureTime = 100000L,
            arrivalTime = null,
            notes = null
        )

        tripDao.update(updatedUsaTrip)

        assertEquals(tripDao.getTrip(1).first().title, "Go to Nigeria")
    }

    /** test update unavailable trip */
    @Test(expected = NoSuchElementException::class)
    fun `update unavailable trip`() = runBlocking {
        val unavailableTrip = TripEntity(
            tripId = 2,
            tripImageUrl = null,
            title = "Go to Nigeria",
            destination = "Maryland",
            departureTime = 100000L,
            arrivalTime = null,
            notes = null
        )

        tripDao.update(unavailableTrip)

        Assert.assertNull(tripDao.getTrip(2).first())
    }
}