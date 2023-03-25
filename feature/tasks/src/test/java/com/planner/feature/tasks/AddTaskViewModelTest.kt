package com.planner.feature.tasks

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.planner.core.data.entity.Task
import com.planner.core.data.entity.TaskManagerType
import com.planner.feature.tasks.viewmodel.AddTaskViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AddTaskViewModelTest {
    private val viewModel = AddTaskViewModel()
    private val task1 = Task(id = 1L, description = "Complete math homework", contributor = "Alice")
    private val task2 = Task(id = 2L, description = "Buy groceries", isDone = true)
    private val task3 = Task(id = 3L, description = "Call mom", contributor = "Bob", isDone = true)
    private val task4 = Task(id = 4L, description = "Read book", contributor = "Charlie")

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `test initial task list is empty`() {
        assertTrue(viewModel.taskList.value.isNullOrEmpty())
    }

    @Test
    fun `get initial task manager type`() {
        assertNull(viewModel.taskManagerType.value)
    }

    @Test
    fun `add task`() {
        viewModel.addTask(task1)
        assertTrue(viewModel.taskList.value!!.isNotEmpty())
    }

    @Test
    fun `test add null task`() {
        viewModel.addTasks(listOf(task1, task2, task3))
        assertTrue(viewModel.taskList.value!!.contains(task1))
        viewModel.removeTask(task4)
        assertEquals(viewModel.taskList.value?.size, 3)
    }

    @Test
    fun `add tasks`() {
        viewModel.addTasks(listOf(task1, task2, task3))
        assertEquals(viewModel.taskList.value!!.size, 3)
        assertTrue(viewModel.taskList.value!!.contains(task1))
    }

    @Test
    fun `remove task`() {
        viewModel.addTasks(listOf(task1, task2, task3))
        assertTrue(viewModel.taskList.value!!.contains(task1))
        viewModel.removeTask(task1)
        assertTrue(!viewModel.taskList.value!!.contains(task1))
    }

    @Test
    fun `set task management type to todo list`() {
        viewModel.setTaskManagementType(TaskManagerType.TODO_LIST)
        assertEquals(viewModel.taskManagerType.value, TaskManagerType.TODO_LIST)
    }

    @Test
    fun `set task management type to project`() {
        viewModel.setTaskManagementType(TaskManagerType.PROJECT)
        assertEquals(viewModel.taskManagerType.value, TaskManagerType.PROJECT)
    }
}
