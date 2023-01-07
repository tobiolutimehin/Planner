package com.planner.feature.trips.adapter

import android.content.Context
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
import com.planner.feature.trips.R
import com.planner.feature.trips.databinding.FragmentItemTripBinding

class TripRecyclerViewAdapter(
    private var context: Context,
    private val onTripClicked: (TripEntity) -> Unit
) :
    ListAdapter<TripEntity, TripRecyclerViewAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentItemTripBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            context,
            onTripClicked
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trip = getItem(position)
        holder.bind(trip)
    }

    class ViewHolder(
        private var binding: FragmentItemTripBinding,
        private var context: Context,
        private var onTripClicked: (TripEntity) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(trip: TripEntity) {
            binding.apply {
                trip.tripImageUrl?.let {
                    tripsCardImage.isVisible = true
                    tripsCardImage.setImageURI(it.toUri())
                    tripsCardImage.contentDescription =
                        context.getString(R.string.image_description, trip.title)
                }
                tripsCardDate.text =
                    trip.departureTime.let { FormatDateUseCase(DatePattern.LITERAL).format(it) }
                tripsCardDestination.text = trip.destination
                tripsCardView.setOnClickListener { onTripClicked(trip) }
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
