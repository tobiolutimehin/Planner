package com.planner.feature.trips

import com.planner.core.data.dao.TripDao
import com.planner.core.data.entity.TripEntity
import com.planner.feature.trips.viewmodel.TripsViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking

@ExperimentalCoroutinesApi
class TripsViewModelTest {
    private lateinit var testTripDao: TripDao
    private lateinit var viewModel: TripsViewModel

    @OptIn(DelicateCoroutinesApi::class)
    private val mainThreadSurrogate = newSingleThreadContext("test thread")

    private val testTripObject =
        TripEntity(
            title = "Trip to the US",
            destination = "USA",
            departureTime = 1000L,
        )

    private val testTrips = listOf(testTripObject)

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        testTripDao = mock(TripDao::class.java)
        `when`(testTripDao.getTrips()).thenReturn(flowOf(testTrips))

        viewModel = TripsViewModel(testTripDao)
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @Test
    fun `test insert`() =
        runTest {
            insertTestTrip()

            verifyBlocking(testTripDao) { insert(testTripObject) }
        }

    @Test
    fun `test delete`() =
        runTest {
            insertTestTrip()

            viewModel.delete(testTripObject)
            verifyBlocking(testTripDao) { delete(testTripObject) }
        }

    @Test
    fun `test update`() =
        runTest {
            insertTestTrip()

            viewModel.update(
                title = "Trip to the US",
                destination = "USA",
                departureTime = 1000L,
                tripImageUrl = null,
                trip = testTripObject,
            )
            verifyBlocking(testTripDao) { update(testTripObject) }
        }

    @Test
    fun `test fetch trip`() {
        insertTestTrip()

        `when`(testTripDao.getTrip(0)).thenReturn(flowOf(testTripObject))
        viewModel.getTrip(0)
        verify(testTripDao).getTrip(0)
    }

    private fun insertTestTrip() {
        viewModel.insert(
            title = "Trip to the US",
            destination = "USA",
            departureTime = 1000L,
            tripImageUrl = null,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
