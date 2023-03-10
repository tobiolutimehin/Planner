package com.planner.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.planner.core.ui.BaseApplication
import com.planner.databinding.FragmentHomeBinding
import com.planner.feature.tasks.adapter.TaskManagerListAdapter
import com.planner.feature.tasks.viewmodel.TasksViewModel
import com.planner.feature.tasks.viewmodel.TasksViewModelFactory
import com.planner.feature.trips.viewmodel.TripsViewModel
import com.planner.feature.trips.viewmodel.TripsViewModelFactory
import java.util.Date

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
        binding.fab.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToPlanListDialogFragment()
            findNavController().navigate(action)
        }

        adapter = TaskManagerListAdapter(
            context = context,
            openDetail = {},
        )
        binding.tasksRecyclerView.adapter = adapter

        tasksViewModel.tasks.observe(viewLifecycleOwner) { managerWithTasks ->
            val pending = managerWithTasks.filter {
                it.tasks.any { !it.isDone }
            }
            adapter.submitList(
                pending,
            )
            binding.pendingTasks.text = resources.getQuantityString(
                com.planner.core.ui.R.plurals.pending_x_tasks,
                pending.size,
                pending.size,
            )
        }

        tripViewModel.trips.observe(viewLifecycleOwner) { trips ->
            val toGo = trips.filter { it.departureTime > Date().time }.size

            binding.planningTrips.text = resources.getQuantityString(
                com.planner.core.ui.R.plurals.planning_x_trips,
                toGo,
                toGo,
            )
        }
    }
}
