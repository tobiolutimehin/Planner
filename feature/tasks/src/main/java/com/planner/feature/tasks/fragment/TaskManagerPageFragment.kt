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
import com.planner.feature.tasks.databinding.FragmentTaskManagerPageBinding
import com.planner.feature.tasks.utils.Converters.toInt
import com.planner.feature.tasks.utils.Converters.toTaskManagerType

class TaskManagerPageFragment : Fragment() {
    private val arguments: TaskManagerPageFragmentArgs by navArgs()

    private var _binding: FragmentTaskManagerPageBinding? = null
    private val binding
        get() = _binding!!

    private var currentPosition = 0
    private var selectedManagerType = TaskManagerType.TODO_LIST
    private lateinit var taskManagerType: TaskManagerType
    private lateinit var tabsAdapter: TaskManagerTabsAdapter

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
        taskManagerType = arguments.managerType
        tabsAdapter = TaskManagerTabsAdapter(this@TaskManagerPageFragment)

        binding.apply {
            viewPager.adapter = tabsAdapter
            fab.setOnClickListener { openAddTaskFragment() }
            viewPager.setCurrentItem(taskManagerType.toInt(), false)

            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = tabsAdapter.getPageTitle(position)
            }.attach()

            tabLayout.addOnTabSelectedListener(
                object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        currentPosition = tab.position
                        selectedManagerType = currentPosition.toTaskManagerType()
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab) {}

                    override fun onTabReselected(tab: TabLayout.Tab) {}
                },
            )
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun openAddTaskFragment() {
        val action =
            TaskManagerPageFragmentDirections.actionTaskManagerListFragmentToAddTaskManagerFragment(
                selectedManagerType = selectedManagerType,
            )
        findNavController().navigate(action)
    }
}
