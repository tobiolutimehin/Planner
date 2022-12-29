package com.planner.feature.trips

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.planner.core.data.dao.TripDao
import com.planner.core.data.entity.TripEntity
import kotlinx.coroutines.launch

class TripsViewModel(private val dao: TripDao) : ViewModel() {
    val trips: LiveData<List<TripEntity>> = dao.getTrips().asLiveData()

    fun insert(
        tripImageUrl: String?,
        departureTime: Long,
        destination: String,
        title: String
    ) = viewModelScope.launch {
        dao.insert(
            TripEntity(
                tripImageUrl = tripImageUrl,
                departureTime = departureTime,
                destination = destination,
                title = title
            )
        )
    }
}

class TripsViewModelFactory(private val dao: TripDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TripsViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}