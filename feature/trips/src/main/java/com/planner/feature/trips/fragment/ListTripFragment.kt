package com.planner.feature.trips.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.planner.core.ui.BaseApplication
import com.planner.feature.trips.R
import com.planner.feature.trips.TripsViewModel
import com.planner.feature.trips.TripsViewModelFactory
import com.planner.feature.trips.databinding.FragmentItemListTripBinding

/** A fragment representing a list of Items. */
class ListTripFragment : Fragment() {

    private val tripViewModel: TripsViewModel by activityViewModels {
        TripsViewModelFactory(((activity?.application as BaseApplication).database).tripDao())
    }

    private var _binding: FragmentItemListTripBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemListTripBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = TripRecyclerViewAdapter()

        tripViewModel.trips.observe(viewLifecycleOwner) { adapter.submitList(it) }

        binding.apply {
            recyclerView.adapter = adapter
            fab.setOnClickListener { openAddTripFragment() }
        }
    }

    private fun openAddTripFragment() =
        findNavController().navigate(R.id.action_listTripFragment_to_addTripFragment)
}