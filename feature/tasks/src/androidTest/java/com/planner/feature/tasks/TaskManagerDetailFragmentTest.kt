package com.planner.feature.tasks

import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.planner.core.data.entity.Task
import com.planner.core.data.entity.TaskManagerType
import com.planner.feature.tasks.fragment.AddTaskManagerFragmentArgs
import com.planner.feature.tasks.fragment.TaskManagerDetailFragment
import com.planner.feature.tasks.fragment.TaskManagerDetailFragmentArgs
import com.planner.feature.tasks.testing.BaseTaskFragmentTest
import com.planner.feature.tasks.testing.seedTaskManager
import com.planner.feature.tasks.testing.taskNavController
import com.planner.test.android.launchFragmentInHiltContainer
import com.planner.test.android.waitUntil
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TaskManagerDetailFragmentTest : BaseTaskFragmentTest() {
    @Test
    fun rendersManager_andNavigatesToEdit() {
        val managerId = taskManagerDao.seedTaskManager(
            name = "Home Renovation",
            type = TaskManagerType.PROJECT,
            tasks = listOf(Task(description = "Paint walls")),
        )

        val navController = taskNavController(
            currentDestination = R.id.taskManagerDetailFragment,
        )

        val scenario = launchFragmentInHiltContainer<TaskManagerDetailFragment>(
            fragmentArgs = TaskManagerDetailFragmentArgs(taskManagerId = managerId).toBundle(),
            navController = navController,
        )

        onView(withText("Home Renovation")).check(matches(isDisplayed()))
        onView(withText("Paint walls")).check(matches(isDisplayed()))
        onView(withId(R.id.detail_edit_list_button)).perform(scrollTo(), click())

        waitUntil {
            navController.currentDestination?.id == R.id.addTaskManagerFragment
        }

        val arguments = requireNotNull(navController.currentBackStackEntry?.arguments)
        val editArgs = AddTaskManagerFragmentArgs.fromBundle(arguments)
        assertEquals(managerId, editArgs.taskManagerId)
        assertEquals(TaskManagerType.PROJECT, editArgs.selectedManagerType)

        scenario.close()
    }

    @Test
    fun deletesManager_andNavigatesBackToList() {
        val managerId = taskManagerDao.seedTaskManager(
            name = "Move House",
            type = TaskManagerType.TODO_LIST,
            tasks = listOf(Task(description = "Pack boxes")),
        )

        val navController = taskNavController(
            currentDestination = R.id.taskManagerDetailFragment,
        )

        val scenario = launchFragmentInHiltContainer<TaskManagerDetailFragment>(
            fragmentArgs = TaskManagerDetailFragmentArgs(taskManagerId = managerId).toBundle(),
            navController = navController,
        )

        onView(withId(R.id.delete_list)).perform(scrollTo(), click())
        onView(withText(com.planner.core.ui.R.string.yes)).perform(click())

        waitUntil {
            runBlocking { taskManagerDao.getTaskManagers().first().isEmpty() } &&
                navController.currentDestination?.id == R.id.taskManagerListFragment
        }

        scenario.close()
    }

    @Test
    fun persistsCheckboxState_whenPaused() {
        val managerId = taskManagerDao.seedTaskManager(
            name = "Trip Checklist",
            type = TaskManagerType.TODO_LIST,
            tasks = listOf(Task(description = "Pack bags")),
        )

        val scenario = launchFragmentInHiltContainer<TaskManagerDetailFragment>(
            fragmentArgs = TaskManagerDetailFragmentArgs(taskManagerId = managerId).toBundle(),
        )

        onView(withId(R.id.checkbox)).perform(click())
        scenario.moveToState(Lifecycle.State.CREATED)

        waitUntil {
            runBlocking {
                taskManagerDao.getTaskManager(managerId).first().tasks.single().isDone
            }
        }

        scenario.close()

        val relaunch = launchFragmentInHiltContainer<TaskManagerDetailFragment>(
            fragmentArgs = TaskManagerDetailFragmentArgs(taskManagerId = managerId).toBundle(),
        )

        onView(withId(R.id.checkbox)).check(matches(isChecked()))

        val persistedTask = runBlocking { taskManagerDao.getTaskManager(managerId).first().tasks.single() }
        assertTrue(persistedTask.isDone)

        relaunch.close()
    }
}
