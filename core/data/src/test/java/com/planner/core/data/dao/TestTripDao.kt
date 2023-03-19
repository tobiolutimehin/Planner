package com.planner.core.data.dao

import com.planner.core.data.entity.TripEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class TestTripDao : TripDao {

    private var entitiesStateFlow = MutableStateFlow(
        listOf(
            TripEntity(
                tripId = 1,
                tripImageUrl = null,
                title = "Go to USA",
                destination = "Maryland",
                departureTime = 100000L,
                arrivalTime = null,
                notes = null,
            ),
        ),
    )
    override suspend fun insert(trip: TripEntity) {
        entitiesStateFlow.update { entities ->
            entities.find { it.tripId == trip.tripId }?.let {
                entities
            } ?: entities.plus(trip)
        }
    }

    override suspend fun delete(trip: TripEntity) {
        entitiesStateFlow.update { entities ->
            entities.filterNot { trip == it }
        }
    }

    override fun getTrips(): Flow<List<TripEntity>> =
        entitiesStateFlow

    override fun getTrip(id: Int): Flow<TripEntity> {
        return entitiesStateFlow.map { tripEntityList ->
            tripEntityList.first { it.tripId == id }
        }
    }

    override suspend fun update(trip: TripEntity) {
        entitiesStateFlow.update { tripEntities ->
            tripEntities.map {
                if (it.tripId == trip.tripId) {
                    trip
                } else { it }
            }
        }
    }
}
