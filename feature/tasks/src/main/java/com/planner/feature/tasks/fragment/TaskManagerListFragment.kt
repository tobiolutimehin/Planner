package com.planner.feature.tasks.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.planner.core.data.entity.TaskManagerType
import com.planner.feature.tasks.adapter.TaskManagerTabsAdapter
import com.planner.feature.tasks.databinding.FragmentTaskManagerListBinding

class TaskManagerListFragment : Fragment() {
    private val arguments: TaskManagerListFragmentArgs by navArgs()

    private var _binding: FragmentTaskManagerListBinding? = null
    private val binding
        get() = _binding!!

    private var currentPosition = 0
    private var selectedManagerType = TaskManagerType.TODO_LIST

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
        val taskManagerType = arguments.managerType

        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager

        val adapter = TaskManagerTabsAdapter(this@TaskManagerListFragment)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()

        tabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    currentPosition = tab.position
                    selectedManagerType = currentPosition.toSelectedManagerType()
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}

                override fun onTabReselected(tab: TabLayout.Tab) {}
            },
        )

        binding.fab.setOnClickListener {
            val action =
                TaskManagerListFragmentDirections.actionTaskManagerListFragmentToAddTaskManagerFragment(
                    selectedManagerType = selectedManagerType,
                )
            findNavController().navigate(action)
        }

        viewPager.setCurrentItem(taskManagerType.toInt(), false)
    }

    private fun Int.toSelectedManagerType(): TaskManagerType = when (this@toSelectedManagerType) {
        0 -> TaskManagerType.TODO_LIST
        1 -> TaskManagerType.PROJECT
        else -> TaskManagerType.TODO_LIST
    }

    private fun TaskManagerType.toInt() = when (this@toInt) {
        TaskManagerType.TODO_LIST -> 0
        TaskManagerType.PROJECT -> 1
    }
}
