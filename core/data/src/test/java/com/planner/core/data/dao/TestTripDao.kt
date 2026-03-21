package com.planner.core.data.dao

import androidx.paging.PagingSource
import androidx.paging.PagingState
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

    override fun getTripsPaged(): PagingSource<Int, TripEntity> =
        TripPagingSource {
            entitiesStateFlow.value.sortedByDescending { it.departureTime }
        }

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

private class TripPagingSource<T : Any>(
    private val itemsProvider: () -> List<T>,
) : PagingSource<Int, T>() {
    override fun getRefreshKey(state: PagingState<Int, T>): Int? = state.anchorPosition

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val items = itemsProvider()
        val start = params.key ?: 0
        if (start < 0) {
            return LoadResult.Error(IllegalArgumentException("Invalid start index: $start"))
        }

        val end = (start + params.loadSize).coerceAtMost(items.size)
        val pageItems = if (start >= items.size) emptyList() else items.subList(start, end)
        val previousKey = (start - params.loadSize).takeIf { it >= 0 }
        val nextKey = end.takeIf { it < items.size }

        return LoadResult.Page(
            data = pageItems,
            prevKey = previousKey,
            nextKey = nextKey,
        )
    }
}
