package com.planner.feature.tasks.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.planner.core.data.entity.TaskEntity
import com.planner.core.data.entity.TaskManagerWithTasksAndContributors
import com.planner.feature.tasks.R
import com.planner.feature.tasks.adapter.ManagerDetailRecyclerViewAdapter
import com.planner.feature.tasks.databinding.FragmentTaskManagerDetailBinding
import com.planner.feature.tasks.utils.Converters.toTitleName
import com.planner.feature.tasks.viewmodel.TasksViewModel
import com.planner.library.contacts_manager.ContactListRecyclerAdapter
import com.planner.library.contacts_manager.PickerContact

class TaskManagerDetailFragment : Fragment() {
    private val arguments: TaskManagerDetailFragmentArgs by navArgs()
    private lateinit var adapter: ManagerDetailRecyclerViewAdapter
    private lateinit var contactsAdapter: ContactListRecyclerAdapter
    private lateinit var taskManagerWithDetails: TaskManagerWithTasksAndContributors

    private var _binding: FragmentTaskManagerDetailBinding? = null
    private val binding get() = _binding!!

    private val tasksViewModel: TasksViewModel by activityViewModels()

    private val checkedTasks = mutableSetOf<TaskEntity>()
    private var delete = false

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

        adapter =
            ManagerDetailRecyclerViewAdapter(
                onCheckChangeListener = { isChecked, task ->
                    if (isChecked) {
                        checkedTasks.add(task)
                    } else {
                        checkedTasks.remove(task)
                    }
                },
            )
        contactsAdapter = ContactListRecyclerAdapter(showSelection = false)

        tasksViewModel.getTaskManagerWithContributors(taskManagerId).observe(viewLifecycleOwner) { manager ->
            taskManagerWithDetails = manager
            adapter.submitList(taskManagerWithDetails.tasks)
            contactsAdapter.submitList(
                taskManagerWithDetails.contributors.map {
                    PickerContact(id = it.contactId, name = it.name, phone = it.phone)
                }.sortedBy { it.name },
            )

            checkedTasks.clear()
            checkedTasks.addAll(taskManagerWithDetails.tasks.filter { it.isDone })

            (activity as AppCompatActivity).supportActionBar?.title =
                context?.getString(taskManagerWithDetails.taskManager.type.toTitleName())

            binding.apply {
                fragment = this@TaskManagerDetailFragment
                recyclerView.adapter = adapter
                contributorsRecyclerView.adapter = contactsAdapter
                contributorsGroup.isVisible =
                    taskManagerWithDetails.taskManager.type == com.planner.core.data.entity.TaskManagerType.PROJECT &&
                        taskManagerWithDetails.contributors.isNotEmpty()
                taskTitleDetail.text = taskManagerWithDetails.taskManager.name.ifBlank {
                    context?.getString(taskManagerWithDetails.taskManager.type.toTitleName())
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

    fun openEditTaskManager() {
        val taskManager = taskManagerWithDetails.taskManager
        val action =
            TaskManagerDetailFragmentDirections
                .actionTaskManagerDetailFragmentToAddTaskManagerFragment(
                    selectedManagerType = taskManager.type,
                    taskManagerId = taskManager.managerId,
                )
        findNavController().navigate(action)
    }

    private fun deleteTaskManager() {
        tasksViewModel.deleteTaskManager(taskManagerWithDetails.taskManager)
        delete = true
        goBackToTaskManagerList()
    }

    private fun goBackToTaskManagerList() {
        val action =
            TaskManagerDetailFragmentDirections.actionTaskManagerDetailFragmentToTaskManagerListFragment(
                managerType = taskManagerWithDetails.taskManager.type,
            )
        findNavController().navigate(action)
    }

    fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(com.planner.core.ui.R.string.delete_sure))
            .setMessage(getString(R.string.delete_trip_confirmation_message))
            .setCancelable(true)
            .setNegativeButton(getString(com.planner.core.ui.R.string.no)) { _, _ -> }
            .setPositiveButton(getString(com.planner.core.ui.R.string.yes)) { _, _ -> deleteTaskManager() }
            .show()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onPause() {
        super.onPause()
        if (!delete) {
            tasksViewModel.updateTaskManagerWithTaskEntity(
                taskManagerWithDetails.tasks.map {
                    it.copy(
                        isDone = it in checkedTasks,
                    )
                },
            )
        }
    }
}
