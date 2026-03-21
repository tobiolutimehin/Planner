package com.planner.feature.tasks.fragment

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
import com.planner.core.data.entity.TaskManagerType
import com.planner.feature.tasks.adapter.TaskManagerListAdapter
import com.planner.feature.tasks.databinding.FragmentTaskManagerListBinding
import com.planner.feature.tasks.viewmodel.TasksViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TaskManagerListFragment : Fragment() {
    private var taskManagerType: TaskManagerType? = null
    private var _binding: FragmentTaskManagerListBinding? = null
    private val binding get() = _binding!!

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: TaskManagerListAdapter
    private var hasRestoredListState = false

    private val tasksViewModel: TasksViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            taskManagerType = it.getSerializable(TASK_MANAGER_TYPE, TaskManagerType::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTaskManagerListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hasRestoredListState = false

        val managerType = taskManagerType ?: TaskManagerType.TODO_LIST
        adapter = TaskManagerListAdapter(
            context = context,
            openDetail = { id, position, itemTopOffset ->
                tasksViewModel.markTaskManagerClicked(managerType, id, position, itemTopOffset)
                openTaskManagerDetail(id)
            },
        )

        layoutManager =
            (binding.tasksRecyclerView.layoutManager as? LinearLayoutManager)
                ?: LinearLayoutManager(requireContext()).also {
                    binding.tasksRecyclerView.layoutManager = it
                }

        binding.tasksRecyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val pagingState =
                    if (managerType == TaskManagerType.PROJECT) {
                        tasksViewModel.projectTaskManagers
                    } else {
                        tasksViewModel.todoTaskManagers
                    }

                launch {
                    pagingState.collectLatest { pagingData ->
                        adapter.submitData(pagingData)
                    }
                }
                launch {
                    adapter.loadStateFlow.collectLatest { loadState ->
                        val isEmpty =
                            loadState.refresh is LoadState.NotLoading &&
                                adapter.itemCount == 0

                        binding.tasksRecyclerView.isVisible = !isEmpty
                        binding.noTasksImage.isVisible = isEmpty
                        binding.noTasksText.isVisible = isEmpty

                        if (!hasRestoredListState && loadState.refresh is LoadState.NotLoading && adapter.itemCount > 0) {
                            restoreScrollPosition(managerType)
                            hasRestoredListState = true
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        val managerType = taskManagerType ?: TaskManagerType.TODO_LIST
        saveScrollState(managerType)
        super.onPause()
    }

    private fun saveScrollState(managerType: TaskManagerType) {
        if (!::layoutManager.isInitialized) return
        val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
        if (firstVisibleItem == RecyclerView.NO_POSITION) return

        val topOffset = layoutManager.findViewByPosition(firstVisibleItem)?.top ?: 0
        tasksViewModel.saveTaskManagerListState(managerType, firstVisibleItem, topOffset)
    }

    private fun restoreScrollPosition(managerType: TaskManagerType) {
        val listState = tasksViewModel.getTaskManagerListState(managerType)
        val clickedItemPosition =
            listState.clickedItemId?.let { clickedItemId ->
                (0 until adapter.itemCount)
                    .firstOrNull { index ->
                        adapter.peek(index)?.taskManager?.managerId == clickedItemId
                    }
            }

        val targetPosition = clickedItemPosition ?: listState.anchorPosition
        val targetOffset = if (clickedItemPosition != null) {
            listState.clickedItemOffset ?: listState.anchorOffset
        } else {
            listState.anchorOffset
        }

        binding.tasksRecyclerView.post {
            layoutManager.scrollToPositionWithOffset(targetPosition, targetOffset)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun openTaskManagerDetail(id: Long) {
        val action =
            TaskManagerPageFragmentDirections.actionTaskManagerListFragmentToTaskManagerDetailFragment(
                id,
            )
        findNavController().navigate(action)
    }

    companion object {
        private const val TASK_MANAGER_TYPE = "task_manager_type"

        @JvmStatic
        fun newInstance(taskManagerType: TaskManagerType) =
            TaskManagerListFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(TASK_MANAGER_TYPE, taskManagerType)
                }
            }
    }
}
