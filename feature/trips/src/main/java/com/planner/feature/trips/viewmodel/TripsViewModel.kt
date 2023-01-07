package com.planner.feature.trips.viewmodel

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.planner.core.data.dao.TripDao
import com.planner.core.data.entity.TripEntity
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

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

    fun getTrip(id: Int): LiveData<TripEntity> = dao.getTrip(id).asLiveData()

    internal fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor?.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor?.close()
        return image
    }

    internal fun saveBitmapToInternalStorage(bitmap: Bitmap, context: Context): String {
        // Get the context wrapper
        val wrapper = ContextWrapper(context)

        // Initialize a new file instance to save bitmap object
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            // Compress the bitmap and save in jpg format
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Return the saved image path to uri
        return file.absolutePath
    }

    fun delete(trip: TripEntity) = viewModelScope.launch {
        dao.delete(trip)
    }

    fun update(
        tripImageUrl: String?,
        departureTime: Long,
        destination: String,
        title: String,
        trip: TripEntity
    ) = viewModelScope.launch {
        val newTrip = trip.copy(
            tripImageUrl = tripImageUrl,
            departureTime = departureTime,
            destination = destination,
            title = title
        )
        dao.update(newTrip)
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
