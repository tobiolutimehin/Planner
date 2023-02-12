package com.planner.feature.tasks.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.planner.core.data.entity.TaskManagerType
import com.planner.core.ui.BaseApplication
import com.planner.feature.tasks.adapter.TaskManagerListAdapter
import com.planner.feature.tasks.databinding.FragmentTaskManagerListContentBinding
import com.planner.feature.tasks.viewmodel.TasksViewModel
import com.planner.feature.tasks.viewmodel.TasksViewModelFactory

class TaskManagerListContentFragment : Fragment() {
    private var taskManagerType: TaskManagerType? = null
    private var _binding: FragmentTaskManagerListContentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TaskManagerListAdapter

    private val tasksViewModel: TasksViewModel by activityViewModels {
        TasksViewModelFactory(((activity?.application as BaseApplication).database).taskManagerDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            taskManagerType = it.getSerializable(TASK_MANAGER_TYPE) as TaskManagerType
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTaskManagerListContentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = TaskManagerListAdapter(requireContext())

        tasksViewModel.tasks.observe(viewLifecycleOwner) { managerWithTasks ->
            adapter.submitList(managerWithTasks.filter { it.taskManager.type == taskManagerType })
        }

        binding.tasksRecyclerView.adapter = adapter
    }

    companion object {
        private const val TASK_MANAGER_TYPE = "task_manager_type"

        @JvmStatic
        fun newInstance(taskManagerType: TaskManagerType) =
            TaskManagerListContentFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(TASK_MANAGER_TYPE, taskManagerType)
                }
            }
    }
}
