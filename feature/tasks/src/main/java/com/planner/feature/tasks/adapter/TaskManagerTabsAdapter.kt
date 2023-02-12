package com.planner.feature.tasks.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.planner.core.data.entity.TaskManagerType
import com.planner.feature.tasks.R
import com.planner.feature.tasks.fragment.TaskManagerListContentFragment

class TaskManagerTabsAdapter(private val fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TaskManagerListContentFragment.newInstance(TaskManagerType.TODO_LIST)
            1 -> TaskManagerListContentFragment.newInstance(TaskManagerType.PROJECT)
            else -> throw IllegalStateException("Invalid position: $position")
        }
    }

    fun getPageTitle(position: Int): CharSequence {
        return fragment.requireContext().getString(
            when (position) {
                0 -> R.string.to_do_lists
                1 -> R.string.projects
                else -> throw IllegalStateException("Invalid position: $position")
            },
        )
    }
}
