package com.planner.feature.tasks

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.textfield.TextInputEditText
import com.planner.core.data.entity.Task
import com.planner.core.data.entity.TaskManagerType
import com.planner.feature.tasks.fragment.AddTaskManagerFragment
import com.planner.feature.tasks.fragment.AddTaskManagerFragmentArgs
import com.planner.feature.tasks.testing.BaseTaskFragmentTest
import com.planner.feature.tasks.testing.seedTaskManager
import com.planner.feature.tasks.testing.taskNavController
import com.planner.test.android.launchFragmentInHiltContainer
import com.planner.test.android.waitUntil
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AddTaskManagerFragmentTest : BaseTaskFragmentTest() {
    @Test
    fun createsManager_andNavigatesBackToList() {
        val navController = taskNavController(
            currentDestination = R.id.addTaskManagerFragment,
        )

        val scenario = launchFragmentInHiltContainer<AddTaskManagerFragment>(
            fragmentArgs = AddTaskManagerFragmentArgs(
                selectedManagerType = TaskManagerType.TODO_LIST,
                taskManagerId = -1L,
            ).toBundle(),
            navController = navController,
        )

        onView(withId(R.id.task_manager_title_edit_text))
            .perform(replaceText("Weekend Chores"), closeSoftKeyboard())
        onView(withId(R.id.add_to_list_button)).perform(scrollTo(), click())
        onView(withId(R.id.task_title_edit_text))
            .perform(replaceText("Buy groceries"), closeSoftKeyboard())
        onView(withId(R.id.add_task)).perform(scrollTo(), click())
        onView(withText("Buy groceries")).check(matches(isDisplayed()))
        onView(withId(R.id.save_button)).perform(scrollTo(), click())

        waitUntil {
            val managers = runBlocking { taskManagerDao.getTaskManagers().first() }
            managers.any { manager ->
                manager.taskManager.name == "Weekend Chores" &&
                    manager.taskManager.type == TaskManagerType.TODO_LIST &&
                    manager.tasks.any { it.description == "Buy groceries" }
            } && navController.currentDestination?.id == R.id.taskManagerListFragment
        }

        scenario.close()
    }

    @Test
    fun preloadsExistingManager_andUpdatesIt() {
        val managerId = taskManagerDao.seedTaskManager(
            name = "Launch Prep",
            type = TaskManagerType.PROJECT,
            tasks = listOf(Task(description = "Draft wireframes")),
        )

        val navController = taskNavController(
            currentDestination = R.id.addTaskManagerFragment,
        )

        val scenario = launchFragmentInHiltContainer<AddTaskManagerFragment>(
            fragmentArgs = AddTaskManagerFragmentArgs(
                selectedManagerType = TaskManagerType.PROJECT,
                taskManagerId = managerId,
            ).toBundle(),
            navController = navController,
        )

        waitUntil {
            var loadedTitle = ""
            scenario.onActivity { activity ->
                loadedTitle = activity.findViewById<TextInputEditText>(
                    R.id.task_manager_title_edit_text,
                ).text?.toString().orEmpty()
            }
            loadedTitle == "Launch Prep"
        }

        onView(withText("Draft wireframes")).check(matches(isDisplayed()))
        onView(withId(R.id.task_manager_title_edit_text))
            .perform(replaceText("Updated Launch Prep"), closeSoftKeyboard())
        onView(withId(R.id.add_to_list_button)).perform(scrollTo(), click())
        onView(withId(R.id.task_title_edit_text))
            .perform(replaceText("Book venue"), closeSoftKeyboard())
        onView(withId(R.id.add_task)).perform(scrollTo(), click())
        onView(withId(R.id.save_button)).perform(scrollTo(), click())

        waitUntil {
            val manager = runBlocking { taskManagerDao.getTaskManager(managerId).first() }
            manager.taskManager.name == "Updated Launch Prep" &&
                manager.tasks.map { it.description }.toSet() ==
                setOf("Draft wireframes", "Book venue")
        }

        scenario.close()
    }
}
