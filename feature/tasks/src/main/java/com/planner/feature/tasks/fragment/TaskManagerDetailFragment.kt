package com.planner.feature.tasks.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.planner.core.data.entity.TaskManagerType
import com.planner.core.ui.BaseApplication
import com.planner.feature.tasks.adapter.ManagerDetailRecyclerViewAdapter
import com.planner.feature.tasks.databinding.FragmentTaskManagerDetailBinding
import com.planner.feature.tasks.utils.Converters.toTitleName
import com.planner.feature.tasks.viewmodel.TasksViewModel
import com.planner.feature.tasks.viewmodel.TasksViewModelFactory

class TaskManagerDetailFragment : Fragment() {
    private val arguments: TaskManagerDetailFragmentArgs by navArgs()
    private lateinit var adapter: ManagerDetailRecyclerViewAdapter

    private var _binding: FragmentTaskManagerDetailBinding? = null
    private val binding get() = _binding!!

    private val tasksViewModel: TasksViewModel by activityViewModels {
        TasksViewModelFactory(((activity?.application as BaseApplication).database).taskManagerDao())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTaskManagerDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val taskManagerId = arguments.taskManagerId

        adapter = ManagerDetailRecyclerViewAdapter(
            onCheckChangeListener = { isChecked, task ->
                tasksViewModel.updateTask(task.copy(isDone = isChecked))
            },
        )

        tasksViewModel.getTaskManager(taskManagerId).observe(viewLifecycleOwner) {
            adapter.submitList(it.tasks)
            (activity as AppCompatActivity).supportActionBar?.title =
                context?.getString(it.taskManager.type.toTitleName())

            binding.apply {
                recyclerView.adapter = adapter
                taskTitleDetail.text =
                    it.taskManager.name.ifBlank { context?.getString(it.taskManager.type.toTitleName()) }

                detailEditListButton.setOnClickListener { _ ->
                    it.taskManager.apply {
                        openEditTaskManager(managerId, type)
                    }
                }
            }
        }

        binding.recyclerView.addItemDecoration(
            androidx.recyclerview.widget.DividerItemDecoration(
                requireContext(),
                LinearLayoutManager.VERTICAL,
            ),
        )
    }

    private fun openEditTaskManager(managerId: Long, type: TaskManagerType) {
        val action = TaskManagerDetailFragmentDirections
            .actionTaskManagerDetailFragmentToAddTaskManagerFragment(
                selectedManagerType = type,
                taskManagerId = managerId,
            )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
