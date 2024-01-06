package com.planner.feature.tasks.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.planner.core.data.entity.ManagerWithTasks
import com.planner.core.data.entity.Task
import com.planner.core.data.entity.TaskManagerType
import com.planner.core.ui.ContactListRecyclerAdapter
import com.planner.feature.tasks.R
import com.planner.feature.tasks.adapter.CreateTasksRecyclerViewAdapter
import com.planner.feature.tasks.databinding.FragmentAddTaskManagerBinding
import com.planner.feature.tasks.utils.Converters.toTitleName
import com.planner.feature.tasks.viewmodel.AddTaskViewModel
import com.planner.feature.tasks.viewmodel.TasksViewModel

class AddTaskManagerFragment : Fragment() {
    private val arguments: AddTaskManagerFragmentArgs by navArgs()
    private lateinit var taskManager: ManagerWithTasks
    private lateinit var adapter: CreateTasksRecyclerViewAdapter
    private lateinit var contactsAdapter: ContactListRecyclerAdapter

    private var _binding: FragmentAddTaskManagerBinding? = null
    private val binding get() = _binding!!

    private val addTaskViewModel: AddTaskViewModel by viewModels()
    private val tasksViewModel: TasksViewModel by activityViewModels()

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
        val selectedType = arguments.selectedManagerType
        val managerId = arguments.taskManagerId

        if (managerId > 0) {
            tasksViewModel.getTaskManager(managerId).observe(viewLifecycleOwner) { manager ->
                taskManager = manager
                addTaskViewModel.addTasks(manager.tasks.map { it.toTask() })
                bindForEdit()
            }
        }

        adapter = CreateTasksRecyclerViewAdapter(removeTask = { removeTask(it) })
        contactsAdapter = ContactListRecyclerAdapter {}

        addTaskViewModel.apply {
            taskList.observe(viewLifecycleOwner) { adapter.submitList(it) }
            setTaskManagementType(selectedType)
        }

        binding.apply {
            contactLayout.isGone = true
            tasksViewModel = tasksViewModel
            addTasksViewModel = addTaskViewModel
            fragment = this@AddTaskManagerFragment
            tasksRecyclerView.adapter = adapter
            lifecycleOwner = viewLifecycleOwner
            tasksRecyclerView.addItemDecoration(
                androidx.recyclerview.widget.DividerItemDecoration(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                ),
            )
        }
    }

    private fun bindForEdit() {
        val manager = taskManager.taskManager
        val typeString = requireContext().getString(manager.type.toTitleName())
        binding.apply {
            taskManagerTitleEditText.setText(manager.name)
            title.text = context?.getString(R.string.editing_task_manager, typeString)
            topButtonToggle.isVisible = false
            saveButton.setOnClickListener { updateTaskManager() }
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
        val presentManagerType = addTaskViewModel.taskManagerType.value ?: TaskManagerType.TODO_LIST
        addTaskViewModel.taskList.value?.let {
            val title = binding.taskManagerTitleEditText.text
            tasksViewModel.saveTaskManager(
                title.toString(),
                it,
                presentManagerType,
            )
        }

        goToTaskManagerList(presentManagerType)
    }

    private fun goToTaskManagerList(presentManagerType: TaskManagerType) {
        val action =
            AddTaskManagerFragmentDirections.actionAddTaskManagerFragmentToTaskManagerListFragment(
                presentManagerType,
            )
        findNavController().navigate(action)
    }

    private fun goToTaskManagerDetail() {
        val action =
            AddTaskManagerFragmentDirections.actionAddTaskManagerFragmentToTaskManagerDetailFragment(
                taskManager.taskManager.managerId,
            )
        findNavController().navigate(action)
    }

    fun addToListClicked() {
        binding.apply {
            taskTitleInputLayout.isVisible = true
            taskButtonRow.isVisible = true
            addToListButton.isInvisible = true
        }
    }

    fun closeAddToList() {
        binding.apply {
            taskTitleInputLayout.isVisible = false
            taskButtonRow.isVisible = false
            addToListButton.isInvisible = false
        }
    }

    fun setTaskManagementType(taskManagerType: TaskManagerType) =
        addTaskViewModel.setTaskManagementType(taskManagerType)

    fun close() {
        if (!findNavController().popBackStack()) activity?.finish()
    }

    private fun updateTaskManager() {
        addTaskViewModel.taskList.value?.let {
            tasksViewModel.updateTaskManager(taskManager.taskManager, it)
            goToTaskManagerDetail()
        }
    }
}
