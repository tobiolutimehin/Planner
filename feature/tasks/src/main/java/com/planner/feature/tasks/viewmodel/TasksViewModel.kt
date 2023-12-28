package com.planner.feature.tasks.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.planner.core.data.dao.TaskManagerDao
import com.planner.core.data.entity.ManagerWithTasks
import com.planner.core.data.entity.Task
import com.planner.core.data.entity.TaskEntity
import com.planner.core.data.entity.TaskManagerEntity
import com.planner.core.data.entity.TaskManagerType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing tasks and task managers.
 *
 * @param dao Data Access Object for accessing the TaskManagerEntity and TaskEntity tables in the database.
 */
@HiltViewModel
class TasksViewModel @Inject constructor(private val dao: TaskManagerDao) : ViewModel() {

    /**
     * LiveData object containing a list of all task managers and their associated tasks.
     */
    val tasks: LiveData<List<ManagerWithTasks>> = dao.getTaskManagers().asLiveData()

    /**
     * Returns the task manager with the given ID.
     *
     * @param id ID of the task manager to retrieve.
     * @return LiveData object containing the task manager and its associated tasks.
     */
    fun getTaskManager(id: Long): LiveData<ManagerWithTasks> = dao.getTaskManager(id).asLiveData()

    /**
     * Inserts a new task manager and its associated tasks into the database.
     *
     * @param string Name of the task manager.
     * @param tasks List of tasks to associate with the task manager.
     * @param taskManagerType Type of the task manager.
     */
    fun saveTaskManager(string: String, tasks: List<Task>, taskManagerType: TaskManagerType) {
        viewModelScope.launch {
            dao.insertTaskManagerWithTasks(
                TaskManagerEntity(name = string, type = taskManagerType),
                tasks,
            )
        }
    }

    /**
     * Deletes the given task manager and its associated tasks from the database.
     *
     * @param taskManagerEntity TaskManagerEntity to delete.
     */
    fun deleteTaskManager(taskManagerEntity: TaskManagerEntity) = viewModelScope.launch {
        dao.deleteTaskManagerWithTasks(taskManagerEntity)
    }

    /**
     * Updates the given task in the database.
     *
     * @param taskEntity TaskEntity to update.
     */
    private fun updateTask(taskEntity: TaskEntity) = viewModelScope.launch {
        dao.updateTask(taskEntity)
    }

    /**
     * Updates the given task manager and its associated tasks in the database.
     *
     * @param taskManagerEntity TaskManagerEntity to update.
     * @param tasks List of tasks to associate with the updated task manager.
     */
    fun updateTaskManager(taskManagerEntity: TaskManagerEntity, tasks: List<Task>) =
        viewModelScope.launch {
            dao.updateTaskManagerWithTasks(taskManagerEntity, tasks)
        }

    /**
     * Updates the task manager and its associated tasks in the database using the given TaskEntity objects.
     *
     * @param tasks List of TaskEntity objects representing the updated tasks.
     */
    fun updateTaskManagerWithTaskEntity(tasks: List<TaskEntity>) =
        viewModelScope.launch {
            tasks.forEach { updateTask(it) }
        }
}

/**
 * Factory for creating TasksViewModel objects.
 *
 * @param dao Data Access Object for accessing the TaskManagerEntity and TaskEntity tables in the database.
 */
class TasksViewModelFactory(private val dao: TaskManagerDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TasksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TasksViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
