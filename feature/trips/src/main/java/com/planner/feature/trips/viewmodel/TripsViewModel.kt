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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject

/**
 * View model for [TripEntity] and [TripDao] interaction.
 *
 * @param dao the data access object for trips
 */
@HiltViewModel
class TripsViewModel @Inject constructor(
    private val dao: TripDao
) : ViewModel() {

    /**
     * LiveData object for observing a list of all trips from the DAO.
     */
    val trips: LiveData<List<TripEntity>> = dao.getTrips().asLiveData()

    /**
     * Inserts a new trip into the DAO.
     *
     * @param tripImageUrl the URL of the trip image, if any
     * @param departureTime the departure time of the trip in milliseconds
     * @param destination the destination of the trip
     * @param title the title of the trip
     */
    fun insert(
        tripImageUrl: String?,
        departureTime: Long,
        destination: String,
        title: String,
    ) = viewModelScope.launch {
        dao.insert(
            TripEntity(
                tripImageUrl = tripImageUrl,
                departureTime = departureTime,
                destination = destination,
                title = title,
            ),
        )
    }

    /**
     * Retrieves a single trip with the given ID from the DAO.
     *
     * @param id the ID of the trip to retrieve
     * @return a LiveData object for observing the retrieved trip
     */
    fun getTrip(id: Int): LiveData<TripEntity> = dao.getTrip(id).asLiveData()

    /**
     * Decodes a bitmap from the given URI.
     *
     * @param context the context in which to perform the operation
     * @param uri the URI from which to decode the bitmap
     * @return the decoded bitmap, or null if decoding failed
     */
    internal fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor?.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor?.close()
        return image
    }

    /**
     * Saves the given bitmap to internal storage and returns the file path.
     *
     * @param bitmap the bitmap to save
     * @param context the context in which to perform the operation
     * @return the file path of the saved bitmap
     */
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

    /**
     * Deletes the bitmap file at the specified absolute path.
     *
     * @param path the absolute path of the bitmap file to delete
     */
    fun deleteBitmapUsingAbsolutePath(path: String) {
        val file = File(path)
        file.delete()
    }

    /**
     * Deletes a [TripEntity] from the database asynchronously using the [viewModelScope].
     *
     * @param trip The [TripEntity] to be deleted.
     */
    fun delete(trip: TripEntity) = viewModelScope.launch {
        dao.delete(trip)
    }

    /**
     * Updates a [TripEntity] in the database asynchronously using the [viewModelScope].
     *
     * @param tripImageUrl The URL of the image for the [TripEntity], or null if there is no image.
     * @param departureTime The departure time of the [TripEntity].
     * @param destination The destination of the [TripEntity].
     * @param title The title of the [TripEntity].
     * @param trip The [TripEntity] to be updated.
     */
    fun update(
        tripImageUrl: String?,
        departureTime: Long,
        destination: String,
        title: String,
        trip: TripEntity,
    ) = viewModelScope.launch {
        val newTrip = trip.copy(
            tripImageUrl = tripImageUrl,
            departureTime = departureTime,
            destination = destination,
            title = title,
        )
        dao.update(newTrip)
    }
}

/**
 * Factory class that creates [TripsViewModel] instances.
 * @property dao the [TripDao] instance used by the [TripsViewModel] to access data.
 */
class TripsViewModelFactory(private val dao: TripDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TripsViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
