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

class TripDetailFragment : Fragment() {

    private val arguments: TripDetailFragmentArgs by navArgs()
    lateinit var trip: TripEntity

    private val tripViewModel: TripsViewModel by activityViewModels {
        TripsViewModelFactory(((activity?.application as BaseApplication).database).tripDao())
    }

    private var _binding: FragmentTripDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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

    private fun bind(trip: TripEntity) {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = tripViewModel
            fragment = this@TripDetailFragment
            tripModel = trip
            tripDetailDate.text = FormatDateUseCase(DatePattern.LITERAL).format(trip.departureTime)

            trip.tripImageUrl?.let {
                tripDetailImage.isVisible = true
                tripDetailImage.setImageURI(it.toUri())
            }
        }
    }

    fun deleteTrip() {
        showConfirmationDialog()
    }

    fun editTrip() {
    }

    private fun confirmDelete() {
        tripViewModel.delete(trip)
        val action = TripDetailFragmentDirections.actionTripDetailFragmentToListTripFragment()
        findNavController().navigate(action)
    }

    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(android.R.string.dialog_alert_title))
            .setMessage(getString(R.string.delete_question))
            .setCancelable(false)
            .setNegativeButton(getString(com.planner.core.ui.R.string.no)) { _, _ -> }
            .setPositiveButton(getString(com.planner.core.ui.R.string.yes)) { _, _ ->
                confirmDelete()
            }
            .show()
    }
}
