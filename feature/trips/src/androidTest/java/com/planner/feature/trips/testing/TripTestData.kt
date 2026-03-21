package com.planner.feature.trips.testing

import com.planner.core.data.dao.TripDao
import com.planner.core.data.entity.TripEntity
import com.planner.core.domain.FormatDateUseCase
import kotlinx.coroutines.runBlocking

internal fun TripDao.seedTrip(trip: TripEntity) = runBlocking {
    insert(trip)
}

internal fun departureTime(date: String): Long = requireNotNull(FormatDateUseCase().getTimeLong(date))
