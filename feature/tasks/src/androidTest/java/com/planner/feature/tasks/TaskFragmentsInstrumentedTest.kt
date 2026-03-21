package com.planner.feature.tasks

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.viewpager2.widget.ViewPager2
import com.planner.core.data.dao.TaskManagerDao
import com.planner.core.data.database.PlannerDatabase
import com.planner.core.data.entity.Task
import com.planner.core.data.entity.TaskManagerEntity
import com.planner.core.data.entity.TaskManagerType
import com.planner.feature.tasks.fragment.AddTaskManagerFragment
import com.planner.feature.tasks.fragment.AddTaskManagerFragmentArgs
import com.planner.feature.tasks.fragment.TaskManagerDetailFragment
import com.planner.feature.tasks.fragment.TaskManagerDetailFragmentArgs
import com.planner.feature.tasks.fragment.TaskManagerListFragment
import com.planner.feature.tasks.fragment.TaskManagerPageFragment
import com.planner.feature.tasks.fragment.TaskManagerPageFragmentArgs
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.planner.test.android.createTestNavController
import com.planner.test.android.launchFragmentInHiltContainer
import com.planner.test.android.waitUntil

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TaskFragmentsInstrumentedTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var database: PlannerDatabase

    @Inject lateinit var taskManagerDao: TaskManagerDao

    @Before
    fun setUp() {
        hiltRule.inject()
        database.clearAllTables()
    }

    @After
    fun tearDown() {
        database.clearAllTables()
    }

    @Test
    fun taskManagerPageFragment_startsOnRequestedTab_andNavigatesFromFab() {
        val navController = createNavController(
            graphId = R.navigation.tasks_nav_graph,
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

    @Test
    fun taskManagerListFragment_showsEmptyState_whenNoManagersExist() {
        val scenario = launchFragmentInHiltContainer(
            instantiate = { TaskManagerListFragment.newInstance(TaskManagerType.TODO_LIST) },
        )

        onView(withId(R.id.no_tasks_image)).check(matches(isDisplayed()))
        onView(withId(R.id.no_tasks_text)).check(matches(isDisplayed()))
        onView(withId(R.id.tasks_recycler_view)).check(matches(withEffectiveVisibility(GONE)))

        scenario.close()
    }

    @Test
    fun taskManagerListFragment_filtersManagersByType() {
        seedTaskManager(
            name = "Weekend Chores",
            type = TaskManagerType.TODO_LIST,
            tasks = listOf(Task(description = "Buy groceries")),
        )
        seedTaskManager(
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

    @Test
    fun addTaskManagerFragment_createsManager_andNavigatesBackToList() {
        val navController = createNavController(
            graphId = R.navigation.tasks_nav_graph,
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
    fun addTaskManagerFragment_preloadsExistingManager_andUpdatesIt() {
        val managerId = seedTaskManager(
            name = "Launch Prep",
            type = TaskManagerType.PROJECT,
            tasks = listOf(Task(description = "Draft wireframes")),
        )

        val navController = createNavController(
            graphId = R.navigation.tasks_nav_graph,
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
                loadedTitle = activity.findViewById<com.google.android.material.textfield.TextInputEditText>(
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

    @Test
    fun taskManagerDetailFragment_rendersManager_andNavigatesToEdit() {
        val managerId = seedTaskManager(
            name = "Home Renovation",
            type = TaskManagerType.PROJECT,
            tasks = listOf(Task(description = "Paint walls")),
        )

        val navController = createNavController(
            graphId = R.navigation.tasks_nav_graph,
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
    fun taskManagerDetailFragment_deletesManager_andNavigatesBackToList() {
        val managerId = seedTaskManager(
            name = "Move House",
            type = TaskManagerType.TODO_LIST,
            tasks = listOf(Task(description = "Pack boxes")),
        )

        val navController = createNavController(
            graphId = R.navigation.tasks_nav_graph,
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
    fun taskManagerDetailFragment_persistsCheckboxState_whenPaused() {
        val managerId = seedTaskManager(
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

    private fun seedTaskManager(
        name: String,
        type: TaskManagerType,
        tasks: List<Task>,
    ): Long = runBlocking {
        val managerId = taskManagerDao.insertTaskManager(
            TaskManagerEntity(
                name = name,
                type = type,
            ),
        )
        taskManagerDao.insertTasks(tasks.map { it.toTaskEntity(managerId) })
        managerId
    }

    private fun createNavController(
        graphId: Int,
        currentDestination: Int,
    ) = createTestNavController(
        context = ApplicationProvider.getApplicationContext<Context>(),
        graphId = graphId,
        currentDestination = currentDestination,
    )
}
