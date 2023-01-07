package com.planner.feature.trips.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.planner.core.data.entity.TripEntity
import com.planner.core.domain.DatePattern
import com.planner.core.domain.FormatDateUseCase
import com.planner.feature.trips.databinding.FragmentItemTripBinding

class TripRecyclerViewAdapter :
    ListAdapter<TripEntity, TripRecyclerViewAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentItemTripBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trip = getItem(position)
        holder.bind(trip)
    }

    class ViewHolder(private var binding: FragmentItemTripBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(trip: TripEntity) {
            binding.apply {
                trip.tripImageUrl?.let {
                    tripsCardImage.isVisible = true
                    tripsCardImage.setImageURI(it.toUri())
                }
                tripsCardDate.text =
                    trip.departureTime.let { FormatDateUseCase(DatePattern.LITERAL).format(it) }
                tripsCardDestination.text = trip.destination
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<TripEntity>() {
        override fun areItemsTheSame(oldItem: TripEntity, newItem: TripEntity) =
            oldItem.tripId == newItem.tripId

        override fun areContentsTheSame(oldItem: TripEntity, newItem: TripEntity) =
            oldItem == newItem
    }
}