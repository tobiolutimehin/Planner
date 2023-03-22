package com.planner.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.fragment.findNavController
import com.planner.core.ui.BaseApplication
import com.planner.databinding.FragmentHomeBinding
import com.planner.feature.tasks.adapter.TaskManagerListAdapter
import com.planner.feature.tasks.viewmodel.TasksViewModel
import com.planner.feature.tasks.viewmodel.TasksViewModelFactory
import com.planner.feature.trips.viewmodel.TripsViewModel
import com.planner.feature.trips.viewmodel.TripsViewModelFactory
import java.util.Date

/**
 * This fragment displays a summary of pending tasks and upcoming trips.
 */
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val tasksViewModel: TasksViewModel by activityViewModels {
        TasksViewModelFactory(((activity?.application as BaseApplication).database).taskManagerDao())
    }

    private val tripViewModel: TripsViewModel by activityViewModels {
        TripsViewModelFactory(((activity?.application as BaseApplication).database).tripDao())
    }

    private lateinit var adapter: TaskManagerListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = TaskManagerListAdapter(
            context = context,
            openDetail = { openPendingTaskManagement(it) },
        )

        binding.apply {
            fab.setOnClickListener { openPlanListDialog() }
            seeMore.setOnClickListener { openTasksFragment() }
            tasksRecyclerView.adapter = adapter

            tasksViewModel.tasks.observe(viewLifecycleOwner) { managerWithTasks ->
                val pendingTasks = managerWithTasks.filter { manager ->
                    manager.tasks.any { !it.isDone }
                }
                adapter.submitList(pendingTasks)
                this.pendingTasks.text = resources.getQuantityString(
                    com.planner.core.ui.R.plurals.pending_x_tasks,
                    pendingTasks.size,
                    pendingTasks.size,
                )
            }
            tripViewModel.trips.observe(viewLifecycleOwner) { trips ->
                val upcomingTrips = trips.filter { it.departureTime > Date().time }.size
                planningTrips.text = resources.getQuantityString(
                    com.planner.core.ui.R.plurals.planning_x_trips,
                    upcomingTrips,
                    upcomingTrips,
                )
            }
        }
    }

    /** Navigates to the Tasks List */
    private fun openTasksFragment() {
        val action = HomeFragmentDirections.actionHomeFragmentToTasksNavGraph()
        findNavController().navigate(action)
    }

    /**
     * Navigates to the PlanListDialogFragment dialog.
     */
    private fun openPlanListDialog() {
        val action = HomeFragmentDirections.actionHomeFragmentToPlanListDialogFragment()
        findNavController().navigate(action)
    }

    /**
     * Opens the task details screen for the given task manager [id].
     */
    private fun openPendingTaskManagement(id: Long) {
        val request = NavDeepLinkRequest.Builder
            .fromUri("android-app://com.planner.tasks/taskManagerDetailFragment?taskManagerId=$id".toUri())
            .build()
        findNavController().navigate(request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
