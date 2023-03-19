package com.planner.core.data

import com.planner.core.data.dao.TestTaskManagerDao
import com.planner.core.data.entity.Task
import com.planner.core.data.entity.TaskManagerEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TaskManagerDaoTest {
    private lateinit var dao: TestTaskManagerDao

    @Before
    fun setup() {
        dao = TestTaskManagerDao()
    }

    @Test
    fun insertTaskManager() = runBlocking {
        val taskManagerEntity = TaskManagerEntity(managerId = 1, name = "Test Manager")
        dao.insertTaskManager(taskManagerEntity)
        val managers = dao.getTaskManagers().first()
        assertEquals(managers.size, 1)
        assertEquals(managers[0].taskManager, taskManagerEntity)
    }

    @Test
    fun insertTaskManagerWithTasks() = runBlocking {
        val taskManagerEntity = TaskManagerEntity(managerId = 1, name = "Test Manager")
        val tasks = listOf(
            Task(id = 1, description = "Task 1"),
            Task(id = 2, description = "Task 2"),
        )
        dao.insertTaskManagerWithTasks(taskManagerEntity, tasks)
        val managers = dao.getTaskManagers().first()
        assertEquals(managers.size, 1)
        assertEquals(managers[0].taskManager, taskManagerEntity)
    }

    @Test
    fun updateTaskManagerWithTasks() = runBlocking {
        val taskManagerEntity = TaskManagerEntity(managerId = 1, name = "Test Manager")
        val tasks = listOf(
            Task(id = 1, description = "Task 1"),
            Task(id = 2, description = "Task 2"),
        )
        dao.insertTaskManagerWithTasks(taskManagerEntity, tasks)

        val updatedTaskManagerEntity = TaskManagerEntity(managerId = 1, name = "Updated Manager")
        val updatedTasks = listOf(
            Task(id = 2, description = "Updated Task 2"),
            Task(id = 3, description = "New Task"),
        )
        dao.updateTaskManagerWithTasks(updatedTaskManagerEntity, updatedTasks)

        val managers = dao.getTaskManagers().first()
        assertEquals(managers.size, 1)
        assertEquals(managers[0].taskManager, updatedTaskManagerEntity)
    }

    @Test
    fun deleteTaskManagerWithTasks() = runBlocking {
        val taskManagerEntity = TaskManagerEntity(managerId = 1, name = "Test Manager")
        val tasks = listOf(
            Task(id = 1, description = "Task 1"),
            Task(id = 2, description = "Task 2"),
        )
        dao.insertTaskManagerWithTasks(taskManagerEntity, tasks)
        dao.deleteTaskManagerWithTasks(taskManagerEntity)

        val managers = dao.getTaskManagers().first()
        assert(managers.isEmpty())
    }
}
