package com.planner.feature.tasks.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.planner.core.ui.BaseApplication
import com.planner.feature.tasks.databinding.FragmentTaskManagerDetailBinding
import com.planner.feature.tasks.viewmodel.TasksViewModel
import com.planner.feature.tasks.viewmodel.TasksViewModelFactory

class TaskManagerDetailFragment : Fragment() {
    private val arguments: TaskManagerDetailFragmentArgs by navArgs()

    private var _binding: FragmentTaskManagerDetailBinding? = null
    private val binding = _binding!!

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
        tasksViewModel.getTaskManager(taskManagerId)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
