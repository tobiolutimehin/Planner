package com.planner.core.data.dao

import com.planner.core.data.entity.SavedContactEntity
import com.planner.core.data.entity.TripEntity
import com.planner.core.data.entity.TripMateCrossRef
import com.planner.core.data.entity.TripWithMates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class TestTripDao : TripDao {
    private val entitiesStateFlow =
        MutableStateFlow(
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

    private val contacts = mutableMapOf<Long, SavedContactEntity>()
    private val mates = mutableMapOf<Int, Set<Long>>()

    override suspend fun insert(trip: TripEntity): Long {
        entitiesStateFlow.update { entities ->
            entities.find { it.tripId == trip.tripId }?.let {
                entities
            } ?: entities.plus(trip)
        }
        return trip.tripId.toLong()
    }

    override suspend fun delete(trip: TripEntity) {
        entitiesStateFlow.update { entities ->
            entities.filterNot { trip == it }
        }
        mates.remove(trip.tripId)
    }

    override fun getTrips(): Flow<List<TripEntity>> = entitiesStateFlow

    override fun getTrip(id: Int): Flow<TripEntity> {
        return entitiesStateFlow.map { tripEntityList ->
            tripEntityList.first { it.tripId == id }
        }
    }

    override fun getTripWithMates(id: Int): Flow<TripWithMates> {
        return entitiesStateFlow.map { tripEntityList ->
            val trip = tripEntityList.first { it.tripId == id }
            TripWithMates(
                trip = trip,
                mates = mates[id].orEmpty().mapNotNull { contacts[it] },
            )
        }
    }

    override suspend fun upsertContacts(contacts: List<SavedContactEntity>) {
        contacts.forEach { this.contacts[it.contactId] = it }
    }

    override suspend fun insertTripMates(crossRefs: List<TripMateCrossRef>) {
        crossRefs.groupBy { it.tripId }.forEach { (tripId, refs) ->
            val existing = mates[tripId].orEmpty().toMutableSet()
            existing.addAll(refs.map { it.contactId })
            mates[tripId] = existing
        }
    }

    override suspend fun deleteTripMatesByTripId(tripId: Int) {
        mates.remove(tripId)
    }

    override suspend fun update(trip: TripEntity) {
        entitiesStateFlow.update { tripEntities ->
            tripEntities.map {
                if (it.tripId == trip.tripId) {
                    trip
                } else {
                    it
                }
            }
        }
    }
}
