package com.planner.feature.tasks.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.planner.core.data.entity.SavedContactEntity
import com.planner.core.data.entity.Task
import com.planner.core.data.entity.TaskManagerType
import com.planner.core.data.entity.TaskManagerWithTasksAndContributors
import com.planner.feature.tasks.R
import com.planner.feature.tasks.adapter.CreateTasksRecyclerViewAdapter
import com.planner.feature.tasks.databinding.FragmentAddTaskManagerBinding
import com.planner.feature.tasks.utils.Converters.toTitleName
import com.planner.feature.tasks.viewmodel.AddTaskViewModel
import com.planner.feature.tasks.viewmodel.TasksViewModel
import com.planner.library.contacts_manager.ContactListRecyclerAdapter
import com.planner.library.contacts_manager.ContactPickerArgs
import com.planner.library.contacts_manager.ContactSelectionResult
import com.planner.library.contacts_manager.PickerContact

class AddTaskManagerFragment : Fragment() {
    private val arguments: AddTaskManagerFragmentArgs by navArgs()

    private lateinit var taskManager: TaskManagerWithTasksAndContributors
    private lateinit var adapter: CreateTasksRecyclerViewAdapter
    private lateinit var contactsAdapter: ContactListRecyclerAdapter

    private val selectedProjectContacts = mutableListOf<PickerContact>()
    private val contactsResultKey = "add_task_manager_contacts_result"

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
        observeContactsSelectionResult()

        val selectedType = arguments.selectedManagerType
        val managerId = arguments.taskManagerId

        if (managerId > 0) {
            tasksViewModel.getTaskManagerWithContributors(managerId).observe(viewLifecycleOwner) { manager ->
                taskManager = manager
                addTaskViewModel.resetTasks(manager.tasks.map { it.toTask() })
                selectedProjectContacts.clear()
                selectedProjectContacts.addAll(
                    manager.contributors.map {
                        PickerContact(id = it.contactId, name = it.name, phone = it.phone)
                    },
                )
                renderSelectedContacts()
                bindForEdit()
            }
        }

        adapter = CreateTasksRecyclerViewAdapter(removeTask = { removeTask(it) })
        contactsAdapter = ContactListRecyclerAdapter(showSelection = false)

        addTaskViewModel.apply {
            taskList.observe(viewLifecycleOwner) { adapter.submitList(it) }
            setTaskManagementType(selectedType)
            taskManagerType.observe(viewLifecycleOwner) { type ->
                binding.contactLayout.isGone = type != TaskManagerType.PROJECT
            }
        }

        binding.apply {
            tasksViewModel = tasksViewModel
            addTasksViewModel = addTaskViewModel
            fragment = this@AddTaskManagerFragment
            tasksRecyclerView.adapter = adapter
            goingRecyclerView.adapter = contactsAdapter
            addGoing.setOnClickListener { openContactManager() }
            lifecycleOwner = viewLifecycleOwner
            tasksRecyclerView.addItemDecoration(
                androidx.recyclerview.widget.DividerItemDecoration(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                ),
            )
        }

        renderSelectedContacts()
    }

    private fun observeContactsSelectionResult() {
        val navBackStackEntry = findNavController().currentBackStackEntry ?: return
        val observer =
            Observer<ContactSelectionResult> { result ->
                selectedProjectContacts.clear()
                selectedProjectContacts.addAll(result.contacts)
                renderSelectedContacts()
                navBackStackEntry.savedStateHandle.remove<ContactSelectionResult>(contactsResultKey)
            }

        navBackStackEntry.savedStateHandle
            .getLiveData<ContactSelectionResult>(contactsResultKey)
            .observe(viewLifecycleOwner, observer)
    }

    private fun renderSelectedContacts() {
        contactsAdapter.submitList(selectedProjectContacts.sortedBy { it.name })
        binding.goingRecyclerView.isVisible = selectedProjectContacts.isNotEmpty()
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

    private fun openContactManager() {
        findNavController().navigate(
            R.id.action_addTaskManagerFragment_to_contacts_manager_navigation_graph,
            bundleOf(
                ContactPickerArgs.PRESELECTED_CONTACT_IDS to selectedProjectContacts.map { it.id }.toLongArray(),
                ContactPickerArgs.RESULT_KEY to contactsResultKey,
            ),
        )
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
                name = title.toString(),
                tasks = it,
                taskManagerType = presentManagerType,
                contributors = selectedContributorsForType(presentManagerType),
            )
        }

        goToTaskManagerList(presentManagerType)
    }

    private fun selectedContributorsForType(type: TaskManagerType): List<SavedContactEntity> {
        return if (type == TaskManagerType.PROJECT) {
            selectedProjectContacts.toSavedContacts()
        } else {
            emptyList()
        }
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
            val managerType = addTaskViewModel.taskManagerType.value ?: taskManager.taskManager.type
            tasksViewModel.updateTaskManager(
                taskManagerEntity = taskManager.taskManager,
                tasks = it,
                contributors = selectedContributorsForType(managerType),
            )
            goToTaskManagerDetail()
        }
    }
}

private fun List<PickerContact>.toSavedContacts(): List<SavedContactEntity> =
    map {
        SavedContactEntity(
            contactId = it.id,
            name = it.name,
            phone = it.phone,
        )
    }
