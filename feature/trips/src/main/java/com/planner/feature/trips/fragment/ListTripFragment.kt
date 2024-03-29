package com.planner.feature.trips.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.planner.feature.trips.R
import com.planner.feature.trips.adapter.TripRecyclerViewAdapter
import com.planner.feature.trips.databinding.FragmentItemListTripBinding
import com.planner.feature.trips.viewmodel.TripsViewModel

/**
 * A [Fragment] subclass for displaying a list of trips.
 */
class ListTripFragment : Fragment() {

    private val tripViewModel: TripsViewModel by activityViewModels()

    private var _binding: FragmentItemListTripBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentItemListTripBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = TripRecyclerViewAdapter(requireContext()) {
            openTripDetail(it.tripId, it.title)
        }

        tripViewModel.trips.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.recyclerView.visibility = View.GONE
                binding.noTripsText.visibility = View.VISIBLE
            } else {
                binding.recyclerView.visibility = View.VISIBLE
                binding.noTripsText.visibility = View.GONE
            }

            adapter.submitList(it)
        }

        binding.apply {
            recyclerView.adapter = adapter
            fab.setOnClickListener { openAddTripFragment() }
        }
    }

    /**
     * Navigates to the [TripDetailFragment] for the specified trip.
     * @param tripId The ID of the trip to display.
     * @param title The title of the fragment.
     */
    private fun openTripDetail(tripId: Int, title: String) {
        val action = ListTripFragmentDirections.actionListTripFragmentToTripDetailFragment(
            fragmentTitle = title,
            tripId = tripId,
        )
        findNavController().navigate(action)
    }

    /**
     * Navigates to the [AddTripFragment].
     */
    private fun openAddTripFragment() {
        val action =
            ListTripFragmentDirections.actionListTripFragmentToAddTripFragment(title = R.string.add_a_trip)
        findNavController().navigate(action)
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
