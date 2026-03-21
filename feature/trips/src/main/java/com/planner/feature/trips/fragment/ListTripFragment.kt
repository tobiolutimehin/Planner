package com.planner.feature.trips.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.planner.feature.trips.R
import com.planner.feature.trips.adapter.TripRecyclerViewAdapter
import com.planner.feature.trips.databinding.FragmentItemListTripBinding
import com.planner.feature.trips.viewmodel.TripsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A [Fragment] subclass for displaying a list of trips.
 */
class ListTripFragment : Fragment() {

    private val tripViewModel: TripsViewModel by activityViewModels()

    private var _binding: FragmentItemListTripBinding? = null
    private val binding get() = _binding!!

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: TripRecyclerViewAdapter
    private var hasRestoredListState = false

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
        hasRestoredListState = false

        adapter = TripRecyclerViewAdapter(requireContext()) { trip, position, itemTopOffset ->
            tripViewModel.markTripClicked(trip.tripId, position, itemTopOffset)
            openTripDetail(trip.tripId, trip.title)
        }

        layoutManager =
            (binding.recyclerView.layoutManager as? LinearLayoutManager)
                ?: LinearLayoutManager(requireContext()).also {
                    binding.recyclerView.layoutManager = it
                }

        binding.apply {
            recyclerView.adapter = adapter
            fab.setOnClickListener { openAddTripFragment() }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    tripViewModel.pagedTrips.collectLatest { pagingData ->
                        adapter.submitData(pagingData)
                    }
                }
                launch {
                    adapter.loadStateFlow.collectLatest { loadState ->
                        val isEmpty =
                            loadState.refresh is LoadState.NotLoading &&
                                adapter.itemCount == 0

                        binding.recyclerView.isVisible = !isEmpty
                        binding.noTripsText.isVisible = isEmpty

                        if (!hasRestoredListState && loadState.refresh is LoadState.NotLoading && adapter.itemCount > 0) {
                            restoreScrollPosition()
                            hasRestoredListState = true
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        saveScrollState()
        super.onPause()
    }

    private fun saveScrollState() {
        if (!::layoutManager.isInitialized) return
        val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
        if (firstVisibleItem == RecyclerView.NO_POSITION) return

        val topOffset = layoutManager.findViewByPosition(firstVisibleItem)?.top ?: 0
        tripViewModel.saveTripListState(firstVisibleItem, topOffset)
    }

    private fun restoreScrollPosition() {
        val listState = tripViewModel.getTripListState()
        val clickedItemPosition =
            listState.clickedTripId?.let { clickedTripId ->
                (0 until adapter.itemCount)
                    .firstOrNull { index ->
                        adapter.peek(index)?.tripId == clickedTripId
                    }
            }

        val targetPosition = clickedItemPosition ?: listState.anchorPosition
        val targetOffset = if (clickedItemPosition != null) {
            listState.clickedItemOffset ?: listState.anchorOffset
        } else {
            listState.anchorOffset
        }

        binding.recyclerView.post {
            layoutManager.scrollToPositionWithOffset(targetPosition, targetOffset)
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

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
