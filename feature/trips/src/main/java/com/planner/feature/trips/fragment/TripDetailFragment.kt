package com.planner.feature.trips.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.planner.core.data.entity.TripEntity
import com.planner.core.domain.DatePattern
import com.planner.core.domain.FormatDateUseCase
import com.planner.core.ui.BaseApplication
import com.planner.feature.trips.R
import com.planner.feature.trips.databinding.FragmentTripDetailBinding
import com.planner.feature.trips.viewmodel.TripsViewModel
import com.planner.feature.trips.viewmodel.TripsViewModelFactory

/**
 * A [Fragment] subclass to display the details of a selected trip.
 */
class TripDetailFragment : Fragment() {

    private val tripViewModel: TripsViewModel by activityViewModels {
        TripsViewModelFactory(((activity?.application as BaseApplication).database).tripDao())
    }

    private var _binding: FragmentTripDetailBinding? = null
    private val binding get() = _binding!!

    private val arguments: TripDetailFragmentArgs by navArgs()

    /**
     * The [TripEntity] to be displayed.
     */
    private lateinit var trip: TripEntity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTripDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = arguments.tripId
        tripViewModel.getTrip(id).observe(this.viewLifecycleOwner) { tripModel ->
            trip = tripModel
            bind(trip)
        }
    }

    /**
     * Binds the [TripEntity] data to the UI.
     *
     * @param trip the [TripEntity] to bind
     */
    private fun bind(trip: TripEntity) {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            fragment = this@TripDetailFragment
            tripModel = trip
            tripDetailDate.text = FormatDateUseCase(DatePattern.LITERAL).format(trip.departureTime)

            trip.tripImageUrl?.let {
                tripDetailImage.isVisible = true
                tripDetailImage.setImageURI(it.toUri())
            }
        }
    }

    /**
     * Deletes the [TripEntity].
     */
    fun deleteTrip() = showConfirmationDialog()

    /**
     * Edits the [TripEntity].
     */
    fun editTrip() {
        val action = TripDetailFragmentDirections.actionTripDetailFragmentToAddTripFragment(
            title = R.string.edit_trip,
            tripId = trip.tripId,
        )
        findNavController().navigate(action)
    }

    /**
     * Deletes the [TripEntity] after confirmation from the user.
     */
    private fun confirmDelete() {
        val tripImage = trip.tripImageUrl

        tripViewModel.delete(trip)
        tripImage?.let {
            tripViewModel.deleteBitmapUsingAbsolutePath(tripImage)
        }

        val action = TripDetailFragmentDirections.actionTripDetailFragmentToListTripFragment()
        findNavController().navigate(action)
    }

    /**
     * Shows the delete confirmation dialog.
     */
    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(com.planner.core.ui.R.string.delete_sure))
            .setMessage(getString(R.string.delete_question))
            .setCancelable(true)
            .setNegativeButton(getString(com.planner.core.ui.R.string.no)) { _, _ -> }
            .setPositiveButton(getString(com.planner.core.ui.R.string.yes)) { _, _ -> confirmDelete() }
            .show()
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
