package com.planner.feature.tasks.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.planner.core.data.entity.Task
import com.planner.core.data.entity.TaskManagerType
import com.planner.core.ui.BaseApplication
import com.planner.feature.tasks.adapter.TasksRecyclerViewAdapter
import com.planner.feature.tasks.databinding.FragmentAddTaskManagerBinding
import com.planner.feature.tasks.viewmodel.AddTaskViewModel
import com.planner.feature.tasks.viewmodel.TasksViewModel
import com.planner.feature.tasks.viewmodel.TasksViewModelFactory

class AddTaskManagerFragment : Fragment() {

    private var _binding: FragmentAddTaskManagerBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TasksRecyclerViewAdapter

    private val addTaskViewModel: AddTaskViewModel by viewModels()
    private val tasksViewModel: TasksViewModel by activityViewModels {
        TasksViewModelFactory(((activity?.application as BaseApplication).database).taskManagerDao())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddTaskManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = TasksRecyclerViewAdapter(removeTask = { removeTask(it) })
        addTaskViewModel.taskList.observe(viewLifecycleOwner) { adapter.submitList(it) }

        binding.apply {
            tasksViewModel = tasksViewModel
            addTasksViewModel = addTaskViewModel
            fragment = this@AddTaskManagerFragment
            tasksRecyclerView.adapter = adapter
            lifecycleOwner = viewLifecycleOwner
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    fun addTask() {
        binding.apply {
            val text = taskTitleEditText.text
            if (!text.isNullOrBlank()) {
                addTaskViewModel.addTask(Task(description = text.toString()))
                taskTitleEditText.setText("")
            }
        }
    }

    private fun removeTask(task: Task) {
        addTaskViewModel.removeTask(task)
    }

    fun saveTaskManager() {
        addTaskViewModel.taskList.value?.let {
            val title = binding.taskManagerTitleEditText.text
            tasksViewModel.saveTaskManager(
                title.toString(),
                it,
                addTaskViewModel.taskManagerType.value ?: TaskManagerType.TODO_LIST,
            )
        }
        close()
    }

    fun addToListClicked() {
        binding.apply {
            taskTitleInputLayout.isVisible = true
            taskButtonRow.isVisible = true
            addToListButton.isVisible = false
        }
    }

    fun closeAddToList() {
        binding.apply {
            taskTitleInputLayout.isVisible = false
            taskButtonRow.isVisible = false
            addToListButton.isVisible = true
        }
    }

    fun setTaskManagementType(taskManagerType: TaskManagerType) {
        addTaskViewModel.setTaskManagementType(taskManagerType)
    }

    fun close() {
        if (!findNavController().popBackStack()) activity?.finish()
    }
}