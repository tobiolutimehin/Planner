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
import com.planner.feature.trips.R
import com.planner.feature.trips.databinding.FragmentTripDetailBinding
import com.planner.feature.trips.viewmodel.TripsViewModel
import com.planner.library.contacts_manager.ContactListRecyclerAdapter
import com.planner.library.contacts_manager.PickerContact

class TripDetailFragment : Fragment() {
    private val tripViewModel: TripsViewModel by activityViewModels()

    private var _binding: FragmentTripDetailBinding? = null
    private val binding get() = _binding!!

    private val arguments: TripDetailFragmentArgs by navArgs()
    private lateinit var contactsAdapter: ContactListRecyclerAdapter

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
        contactsAdapter = ContactListRecyclerAdapter(showSelection = false)
        binding.tripMatesRecyclerView.adapter = contactsAdapter

        val id = arguments.tripId
        tripViewModel.getTripWithMates(id).observe(this.viewLifecycleOwner) { tripWithMates ->
            trip = tripWithMates.trip
            bind(trip)
            contactsAdapter.submitList(
                tripWithMates.mates
                    .map { PickerContact(id = it.contactId, name = it.name, phone = it.phone) }
                    .sortedBy { it.name },
            )
            binding.tripMatesSection.isVisible = tripWithMates.mates.isNotEmpty()
        }
    }

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

    fun deleteTrip() = showConfirmationDialog()

    fun editTrip() {
        val action =
            TripDetailFragmentDirections.actionTripDetailFragmentToAddTripFragment(
                title = R.string.edit_trip,
                tripId = trip.tripId,
            )
        findNavController().navigate(action)
    }

    private fun confirmDelete() {
        val tripImage = trip.tripImageUrl

        tripViewModel.delete(trip)
        tripImage?.let {
            tripViewModel.deleteBitmapUsingAbsolutePath(tripImage)
        }

        val action = TripDetailFragmentDirections.actionTripDetailFragmentToListTripFragment()
        findNavController().navigate(action)
    }

    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(com.planner.core.ui.R.string.delete_sure))
            .setMessage(getString(R.string.delete_question))
            .setCancelable(true)
            .setNegativeButton(getString(com.planner.core.ui.R.string.no)) { _, _ -> }
            .setPositiveButton(getString(com.planner.core.ui.R.string.yes)) { _, _ -> confirmDelete() }
            .show()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
