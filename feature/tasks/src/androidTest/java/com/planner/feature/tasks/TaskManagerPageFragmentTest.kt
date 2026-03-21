package com.planner.feature.tasks

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.viewpager2.widget.ViewPager2
import com.planner.core.data.entity.TaskManagerType
import com.planner.feature.tasks.fragment.TaskManagerPageFragment
import com.planner.feature.tasks.fragment.TaskManagerPageFragmentArgs
import com.planner.feature.tasks.testing.BaseTaskFragmentTest
import com.planner.feature.tasks.testing.taskNavController
import com.planner.test.android.launchFragmentInHiltContainer
import com.planner.test.android.waitUntil
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TaskManagerPageFragmentTest : BaseTaskFragmentTest() {
    @Test
    fun startsOnRequestedTab_andNavigatesFromFab() {
        val navController = taskNavController(
            currentDestination = R.id.taskManagerListFragment,
        )

        val scenario = launchFragmentInHiltContainer<TaskManagerPageFragment>(
            fragmentArgs = TaskManagerPageFragmentArgs(TaskManagerType.PROJECT).toBundle(),
            navController = navController,
        )

        onView(withText(R.string.to_do_lists)).check(matches(isDisplayed()))
        onView(withText(R.string.projects)).check(matches(isDisplayed()))

        waitUntil {
            var currentItem = -1
            scenario.onActivity { activity ->
                val fragment =
                    activity.supportFragmentManager.findFragmentById(android.R.id.content)
                        as TaskManagerPageFragment
                currentItem = fragment.requireView().findViewById<ViewPager2>(R.id.view_pager).currentItem
            }
            currentItem == 1
        }

        onView(withId(R.id.fab)).perform(click())

        waitUntil {
            navController.currentDestination?.id == R.id.addTaskManagerFragment
        }

        scenario.close()
    }
}
