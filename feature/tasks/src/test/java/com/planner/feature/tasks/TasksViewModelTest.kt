package com.planner.feature.tasks

import com.planner.core.data.dao.TaskManagerDao
import com.planner.core.data.entity.ManagerWithTasks
import com.planner.core.data.entity.TaskEntity
import com.planner.core.data.entity.TaskManagerEntity
import com.planner.core.data.entity.TaskManagerType
import com.planner.feature.tasks.viewmodel.TasksViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking

@ExperimentalCoroutinesApi
class TasksViewModelTest {
    private lateinit var taskDao: TaskManagerDao
    private lateinit var viewModel: TasksViewModel

    private val manager1 = TaskManagerEntity(1, "manager1", TaskManagerType.PROJECT)
    private val manager2 = TaskManagerEntity(2, "manager2", TaskManagerType.TODO_LIST)

    private val task1 = TaskEntity(1, "task1", false, "contributor1", 1)
    private val task2 = TaskEntity(2, "task2", true, "contributor2", 1)
    private val task3 = TaskEntity(3, "task3", false, "contributor3", 2)

    private val managerWithTasks1 = ManagerWithTasks(manager1, listOf(task1, task2))
    private val managerWithTasks2 = ManagerWithTasks(manager2, listOf(task3))

    @OptIn(DelicateCoroutinesApi::class)
    private val mainThreadSurrogate = newSingleThreadContext("test thread")

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        taskDao = mock(TaskManagerDao::class.java)
        `when`(taskDao.getTaskManagers()).thenReturn(
            flowOf(
                listOf(
                    managerWithTasks1,
                    managerWithTasks2,
                ),
            ),
        )

        viewModel = TasksViewModel(taskDao)
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test get task manager by id`() {
        `when`(taskDao.getTaskManager(1)).thenReturn(flowOf(managerWithTasks1))

        viewModel.getTaskManager(1)
        verify(taskDao).getTaskManager(1)
    }

    @Test
    fun `test save task manager`() = runTest {
        viewModel.saveTaskManager(
            string = manager1.name,
            tasks = listOf(task1.toTask(), task2.toTask()),
            taskManagerType = manager1.type,
        )

        verifyBlocking(taskDao) {
            insertTaskManagerWithTasks(
                TaskManagerEntity(
                    name = manager1.name,
                    type = manager1.type,
                ),
                listOf(task1.toTask(), task2.toTask()),
            )
        }
    }

    @Test
    fun `test delete task manager`() = runTest {
        viewModel.deleteTaskManager(manager1)

        verifyBlocking(taskDao) { deleteTaskManagerWithTasks(manager1) }
    }

    @Test
    fun `test update task manager`() = runTest {
        viewModel.updateTaskManager(manager1, listOf(task1.toTask(), task2.toTask()))

        verifyBlocking(taskDao) {
            updateTaskManagerWithTasks(
                manager1,
                listOf(task1.toTask(), task2.toTask()),
            )
        }
    }

    @Test
    fun `test update task`() = runTest {
        viewModel.updateTaskManagerWithTaskEntity(listOf(task1, task2, task3))

        verifyBlocking(taskDao) {
            getTaskManagers()
            updateTask(task1)
        }
    }
}
