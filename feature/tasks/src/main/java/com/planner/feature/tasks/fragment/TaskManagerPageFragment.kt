package com.planner.feature.tasks.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.planner.feature.tasks.adapter.TaskManagerTabsAdapter
import com.planner.feature.tasks.databinding.FragmentTaskManagerPageBinding
import com.planner.feature.tasks.utils.Converters.toInt
import com.planner.feature.tasks.utils.Converters.toTaskManagerType
import com.planner.feature.tasks.viewmodel.TasksViewModel

class TaskManagerPageFragment : Fragment() {
    private val arguments: TaskManagerPageFragmentArgs by navArgs()

    private var _binding: FragmentTaskManagerPageBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var tabsAdapter: TaskManagerTabsAdapter
    private val tasksViewModel: TasksViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTaskManagerPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            tasksViewModel.setSelectedManagerType(arguments.managerType)
        } else {
            tasksViewModel.initializeSelectedManagerType(arguments.managerType)
        }
        tabsAdapter = TaskManagerTabsAdapter(this@TaskManagerPageFragment)

        binding.apply {
            viewPager.adapter = tabsAdapter
            fab.setOnClickListener { openAddTaskFragment() }

            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = tabsAdapter.getPageTitle(position)
            }.attach()

            tabLayout.addOnTabSelectedListener(
                object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        tasksViewModel.setSelectedManagerType(tab.position.toTaskManagerType())
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab) {}

                    override fun onTabReselected(tab: TabLayout.Tab) {}
                },
            )
        }

        tasksViewModel.selectedManagerType.observe(viewLifecycleOwner) { type ->
            val targetPosition = type.toInt()
            if (binding.viewPager.currentItem != targetPosition) {
                binding.viewPager.setCurrentItem(targetPosition, false)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun openAddTaskFragment() {
        val action =
            TaskManagerPageFragmentDirections.actionTaskManagerListFragmentToAddTaskManagerFragment(
                selectedManagerType =
                    tasksViewModel.selectedManagerType.value ?: binding.viewPager.currentItem.toTaskManagerType(),
            )
        findNavController().navigate(action)
    }
}
