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

/**
 * RecyclerView adapter for displaying a list of [TripEntity] objects.
 *
 * @param context The [Context] of the parent activity or fragment.
 * @param onTripClicked The callback function to be executed when a trip item is clicked.
 */
class TripRecyclerViewAdapter(
    private var context: Context,
    private val onTripClicked: (TripEntity) -> Unit,
) : ListAdapter<TripEntity, TripRecyclerViewAdapter.ViewHolder>(DiffCallback) {

    /**
     * Creates a new [ViewHolder] object to represent an item view in the RecyclerView.
     *
     * @param parent The parent [ViewGroup] of the item view.
     * @param viewType The type of view to be created.
     * @return A new [ViewHolder] object.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentItemTripBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
            context,
            onTripClicked,
        )
    }

    /**
     * Updates the contents of a [ViewHolder] to reflect the data at the given position.
     *
     * @param holder The [ViewHolder] to be updated.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trip = getItem(position)
        holder.bind(trip)
    }

    /**
     * ViewHolder class for holding references to the views in an item view.
     *
     * @param binding The [FragmentItemTripBinding] object containing references to the views.
     * @param context The [Context] of the parent activity or fragment.
     * @param onTripClicked The callback function to be executed when a trip item is clicked.
     */
    class ViewHolder(
        private var binding: FragmentItemTripBinding,
        private var context: Context,
        private var onTripClicked: (TripEntity) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        /**
         * Binds the data of a [TripEntity] object to the views in the item view.
         *
         * @param trip The [TripEntity] object to bind to the views.
         */
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

    /**
     * Companion object containing the [DiffCallback] for comparing [TripEntity] objects.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<TripEntity>() {
        /**
         * Returns true if the old and new items have the same ID.
         *
         * @param oldItem The old [TripEntity] item.
         * @param newItem The new [TripEntity] item.
         * @return True if the items have the same ID, false otherwise.
         */
        override fun areItemsTheSame(oldItem: TripEntity, newItem: TripEntity) =
            oldItem.tripId == newItem.tripId

        /**
         * Returns true if the old and new items have the same contents.
         *
         * @param oldItem The old [TripEntity] item.
         * @param newItem The new [TripEntity] item.
         */
        override fun areContentsTheSame(oldItem: TripEntity, newItem: TripEntity) =
            oldItem == newItem
    }
}
