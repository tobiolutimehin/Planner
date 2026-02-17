package com.planner.feature.trips.viewmodel

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.planner.core.data.dao.TripDao
import com.planner.core.data.entity.SavedContactEntity
import com.planner.core.data.entity.TripEntity
import com.planner.core.data.entity.TripWithMates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TripsViewModel @Inject constructor(
    private val dao: TripDao,
) : ViewModel() {
    val trips: LiveData<List<TripEntity>> = dao.getTrips().asLiveData()

    fun insert(
        tripImageUrl: String?,
        departureTime: Long,
        destination: String,
        title: String,
        mates: List<SavedContactEntity>,
    ) =
        viewModelScope.launch {
            dao.insertTripWithMates(
                TripEntity(
                    tripImageUrl = tripImageUrl,
                    departureTime = departureTime,
                    destination = destination,
                    title = title,
                ),
                mates,
            )
        }

    fun getTrip(id: Int): LiveData<TripEntity> = dao.getTrip(id).asLiveData()

    fun getTripWithMates(id: Int): LiveData<TripWithMates> = dao.getTripWithMates(id).asLiveData()

    internal fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor?.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor?.close()
        return image
    }

    internal fun saveBitmapToInternalStorage(bitmap: Bitmap, context: Context): String {
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file.absolutePath
    }

    fun deleteBitmapUsingAbsolutePath(path: String) {
        val file = File(path)
        file.delete()
    }

    fun delete(trip: TripEntity) =
        viewModelScope.launch {
            dao.delete(trip)
        }

    fun update(
        tripImageUrl: String?,
        departureTime: Long,
        destination: String,
        title: String,
        trip: TripEntity,
        mates: List<SavedContactEntity>,
    ) =
        viewModelScope.launch {
            val newTrip =
                trip.copy(
                    tripImageUrl = tripImageUrl,
                    departureTime = departureTime,
                    destination = destination,
                    title = title,
                )
            dao.updateTripWithMates(newTrip, mates)
        }
}
