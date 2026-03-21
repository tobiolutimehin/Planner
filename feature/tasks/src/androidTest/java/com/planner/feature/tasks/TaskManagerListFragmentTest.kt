package com.planner.feature.tasks

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.planner.core.data.entity.Task
import com.planner.core.data.entity.TaskManagerType
import com.planner.feature.tasks.fragment.TaskManagerListFragment
import com.planner.feature.tasks.testing.BaseTaskFragmentTest
import com.planner.feature.tasks.testing.seedTaskManager
import com.planner.test.android.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TaskManagerListFragmentTest : BaseTaskFragmentTest() {
    @Test
    fun showsEmptyState_whenNoManagersExist() {
        val scenario = launchFragmentInHiltContainer(
            instantiate = { TaskManagerListFragment.newInstance(TaskManagerType.TODO_LIST) },
        )

        onView(withId(R.id.no_tasks_image)).check(matches(isDisplayed()))
        onView(withId(R.id.no_tasks_text)).check(matches(isDisplayed()))
        onView(withId(R.id.tasks_recycler_view)).check(matches(withEffectiveVisibility(GONE)))

        scenario.close()
    }

    @Test
    fun filtersManagersByType() {
        taskManagerDao.seedTaskManager(
            name = "Weekend Chores",
            type = TaskManagerType.TODO_LIST,
            tasks = listOf(Task(description = "Buy groceries")),
        )
        taskManagerDao.seedTaskManager(
            name = "Home Renovation",
            type = TaskManagerType.PROJECT,
            tasks = listOf(Task(description = "Paint walls")),
        )

        val scenario = launchFragmentInHiltContainer(
            instantiate = { TaskManagerListFragment.newInstance(TaskManagerType.PROJECT) },
        )

        onView(withText("Home Renovation")).check(matches(isDisplayed()))
        onView(withText("Weekend Chores")).check(doesNotExist())
        onView(withId(R.id.no_tasks_text)).check(matches(withEffectiveVisibility(GONE)))
        onView(withId(R.id.tasks_recycler_view)).check(matches(isDisplayed()))

        scenario.close()
    }
}
